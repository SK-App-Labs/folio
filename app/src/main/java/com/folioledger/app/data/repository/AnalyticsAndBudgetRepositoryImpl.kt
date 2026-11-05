// File: app/src/main/java/com/folioledger/app/data/repository/AnalyticsAndBudgetRepositoryImpl.kt
package com.folioledger.app.data.repository

import com.folioledger.app.data.local.dao.AccountDao
import com.folioledger.app.data.local.dao.BudgetAndRecurringDao
import com.folioledger.app.data.local.dao.CategoryAndTagDao
import com.folioledger.app.data.local.dao.TransactionDao
import com.folioledger.app.data.local.entity.BudgetEntity
import com.folioledger.app.data.local.entity.CategoryEntity
import com.folioledger.app.data.local.entity.RecurringRuleEntity
import com.folioledger.app.data.local.entity.TransactionEntity
import com.folioledger.app.data.local.entity.TransactionWithDetails
import com.folioledger.app.domain.model.BudgetProgress
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.InstallmentPlanWithProgress
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.PeriodLedgerSummary
import com.folioledger.app.domain.model.RecurringCommitmentItem
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.SpendRankingItem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.domain.repository.AnalyticsAndBudgetRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import com.folioledger.app.domain.usecase.AccountingRulesEngine
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.domain.util.RecurringScheduleCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.room.withTransaction
import com.folioledger.app.data.local.FolioDatabase

class AnalyticsAndBudgetRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val categoryAndTagDao: CategoryAndTagDao,
    private val budgetAndRecurringDao: BudgetAndRecurringDao,
    private val settingsRepository: SettingsAndBackupRepository? = null,
    private val accountDao: AccountDao? = null,
    private val database: FolioDatabase? = null
) : AnalyticsAndBudgetRepository {

    private val userPreferencesFlow: Flow<UserPreferences> =
        settingsRepository?.observeUserPreferences() ?: flowOf(UserPreferences())

    private fun convertToPrimaryMinor(
        amountMinor: Long,
        currencyCode: String,
        primaryCurrencyCode: String,
        secondaryMap: Map<String, Double>
    ): Long {
        if (amountMinor == 0L) return 0L
        val txCurrency = currencyCode.trim().uppercase()
        val primaryCode = primaryCurrencyCode.trim().uppercase()
        if (txCurrency == primaryCode) {
            return amountMinor
        }
        val rate = secondaryMap[txCurrency] ?: return amountMinor
        if (rate <= 0.0) return amountMinor
        val txSpec = SupportedCurrencies.fromCode(txCurrency)
        val primarySpec = SupportedCurrencies.fromCode(primaryCode)
        return MoneyFormatterAndCalculator.convertMinorMoney(
            sourceMinor = amountMinor,
            sourceFractionDigits = txSpec.fractionDigits,
            targetFractionDigits = primarySpec.fractionDigits,
            rateToTarget = rate
        )
    }

    override fun observePeriodLedgerSummary(
        startEpochDay: Long,
        endEpochDay: Long,
        includeCarryOver: Boolean
    ): Flow<PeriodLedgerSummary> {
        val transactionsFlow = if (includeCarryOver) {
            transactionDao.observeTransactionsInRange(0L, endEpochDay)
        } else {
            transactionDao.observeTransactionsInRange(startEpochDay, endEpochDay)
        }
        return combine(transactionsFlow, userPreferencesFlow) { transactions, prefs ->
            val primaryCode = prefs.primaryCurrencyCode
            val secondaryMap = prefs.secondaryCurrencies.associate { it.code.trim().uppercase() to it.exchangeRateToPrimary }

            var priorNetMinor = 0L
            var periodIncomeMinor = 0L
            var periodExpenseRawMinor = 0L

            for (txWithDetails in transactions) {
                val tx = txWithDetails.transaction
                val day = tx.ledgerDateEpochDay

                if (day < startEpochDay) {
                    if (includeCarryOver) {
                        priorNetMinor = applyCashflowDelta(priorNetMinor, txWithDetails, primaryCode, secondaryMap)
                    }
                } else if (day in startEpochDay..endEpochDay) {
                    val rawAmount = Math.abs(tx.amountMinor)
                    val rawFee = Math.abs(tx.transferFeeMinor)
                    val amount = convertToPrimaryMinor(rawAmount, tx.currencyCode, primaryCode, secondaryMap)
                    val fee = convertToPrimaryMinor(rawFee, tx.currencyCode, primaryCode, secondaryMap)

                    when (tx.type) {
                        TransactionType.INCOME -> {
                            periodIncomeMinor = Math.addExact(periodIncomeMinor, amount)
                        }
                        TransactionType.EXPENSE -> {
                            val totalCharge = Math.addExact(amount, fee)
                            periodExpenseRawMinor = Math.addExact(periodExpenseRawMinor, totalCharge)
                        }
                        TransactionType.REFUND -> {
                            // Refunds reduce Period Expense rather than inflating Period Income
                            periodExpenseRawMinor = Math.subtractExact(periodExpenseRawMinor, amount)
                        }
                        TransactionType.TRANSFER,
                        TransactionType.CC_SETTLEMENT -> {
                            // Transfer & CC settlement principal is excluded; transfer fee counts as Expense
                            if (fee > 0L) {
                                periodExpenseRawMinor = Math.addExact(periodExpenseRawMinor, fee)
                            }
                        }
                        TransactionType.OPENING_BALANCE,
                        TransactionType.RECONCILIATION -> {
                            // Capital adjustments do not count toward operating cashflow
                        }
                    }
                }
            }

            val excessRefundMinor = if (periodExpenseRawMinor < 0L) Math.abs(periodExpenseRawMinor) else 0L
            val adjustedIncomeMinor = Math.addExact(periodIncomeMinor, excessRefundMinor)
            val effectiveExpenseMinor = periodExpenseRawMinor.coerceAtLeast(0L)

            val totalIncome = MinorMoney(adjustedIncomeMinor)
            val totalExpense = MinorMoney(effectiveExpenseMinor)
            val carryOver = if (includeCarryOver) MinorMoney(priorNetMinor) else MinorMoney.ZERO
            val netCashflow = (totalIncome - totalExpense) + carryOver

            PeriodLedgerSummary(
                startEpochDay = startEpochDay,
                endEpochDay = endEpochDay,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                netCashflow = netCashflow,
                carryOverFromPreviousPeriod = carryOver
            )
        }
    }

    override fun observeCategorySpendStats(
        startEpochDay: Long,
        endEpochDay: Long,
        type: CategoryType
    ): Flow<List<CategorySpendStat>> {
        return combine(
            transactionDao.observeTransactionsInRange(startEpochDay, endEpochDay),
            categoryAndTagDao.observeAllCategories(),
            userPreferencesFlow
        ) { periodTransactions, categories, prefs ->
            val primaryCode = prefs.primaryCurrencyCode
            val secondaryMap = prefs.secondaryCurrencies.associate { it.code.trim().uppercase() to it.exchangeRateToPrimary }
            val categoriesById = categories.associateBy { it.id }
            val netMinorByCategoryId = HashMap<Long, Long>()
            val txCountByCategoryId = HashMap<Long, Int>()
            val subcategoryMinorMap = HashMap<Long, HashMap<Long, Long>>()
            val subcategoryCountMap = HashMap<Long, HashMap<Long, Int>>()

            fun getTargetMainCategoryId(rawCatId: Long?): Long {
                val catId = rawCatId ?: UNATEGORIZED_BUCKET_ID
                val catEntity = categoriesById[catId] ?: return catId
                return catEntity.parentId ?: catId
            }

            fun addCategoryDelta(categoryId: Long?, deltaMinor: Long, countIncrement: Int) {
                val catId = categoryId ?: UNATEGORIZED_BUCKET_ID
                val catEntity = categoriesById[catId]
                val targetMainId = catEntity?.parentId ?: catId

                val currentAmount = netMinorByCategoryId[targetMainId] ?: 0L
                netMinorByCategoryId[targetMainId] = Math.addExact(currentAmount, deltaMinor)
                if (countIncrement > 0) {
                    txCountByCategoryId[targetMainId] = (txCountByCategoryId[targetMainId] ?: 0) + countIncrement
                }

                if (catEntity?.parentId != null) {
                    val subMinors = subcategoryMinorMap.getOrPut(targetMainId) { HashMap() }
                    val currentSub = subMinors[catId] ?: 0L
                    subMinors[catId] = Math.addExact(currentSub, deltaMinor)

                    val subCounts = subcategoryCountMap.getOrPut(targetMainId) { HashMap() }
                    subCounts[catId] = (subCounts[catId] ?: 0) + countIncrement
                }
            }

            for (txWithDetails in periodTransactions) {
                val tx = txWithDetails.transaction
                val rawAmount = Math.abs(tx.amountMinor)
                val rawFee = Math.abs(tx.transferFeeMinor)
                val amount = convertToPrimaryMinor(rawAmount, tx.currencyCode, primaryCode, secondaryMap)
                val fee = convertToPrimaryMinor(rawFee, tx.currencyCode, primaryCode, secondaryMap)

                when (type) {
                    CategoryType.EXPENSE -> {
                        when (tx.type) {
                            TransactionType.EXPENSE -> {
                                if (tx.isSplit && txWithDetails.splits.isNotEmpty()) {
                                    for (split in txWithDetails.splits) {
                                        val splitAmount = convertToPrimaryMinor(
                                            Math.abs(split.amountMinor),
                                            tx.currencyCode,
                                            primaryCode,
                                            secondaryMap
                                        )
                                        addCategoryDelta(
                                            categoryId = split.categoryId,
                                            deltaMinor = splitAmount,
                                            countIncrement = 1
                                        )
                                    }
                                } else {
                                    addCategoryDelta(
                                        categoryId = tx.categoryId,
                                        deltaMinor = Math.addExact(amount, fee),
                                        countIncrement = 1
                                    )
                                }
                            }
                            TransactionType.REFUND -> {
                                // Refunds subtract directly from Category Spend
                                addCategoryDelta(
                                    categoryId = tx.categoryId,
                                    deltaMinor = Math.negateExact(amount),
                                    countIncrement = 0
                                )
                            }
                            TransactionType.TRANSFER,
                            TransactionType.CC_SETTLEMENT -> {
                                if (fee > 0L) {
                                    addCategoryDelta(
                                        categoryId = tx.categoryId,
                                        deltaMinor = fee,
                                        countIncrement = 1
                                    )
                                }
                            }
                            TransactionType.INCOME,
                            TransactionType.OPENING_BALANCE,
                            TransactionType.RECONCILIATION -> Unit
                        }
                    }

                    CategoryType.INCOME -> {
                        if (tx.type == TransactionType.INCOME) {
                            if (tx.isSplit && txWithDetails.splits.isNotEmpty()) {
                                for (split in txWithDetails.splits) {
                                    val splitAmount = convertToPrimaryMinor(
                                        Math.abs(split.amountMinor),
                                        tx.currencyCode,
                                        primaryCode,
                                        secondaryMap
                                    )
                                    addCategoryDelta(
                                        categoryId = split.categoryId,
                                        deltaMinor = splitAmount,
                                        countIncrement = 1
                                    )
                                }
                            } else {
                                addCategoryDelta(
                                    categoryId = tx.categoryId,
                                    deltaMinor = amount,
                                    countIncrement = 1
                                )
                            }
                        }
                    }
                }
            }

            val positiveEntries = netMinorByCategoryId.entries.filter { it.value > 0L }
            var grandTotalMinor = 0L
            for (entry in positiveEntries) {
                grandTotalMinor = Math.addExact(grandTotalMinor, entry.value)
            }

            positiveEntries
                .map { (catId, netMinor) ->
                    val catEntity: CategoryEntity? = categoriesById[catId]
                    val pct = computePercentage(netMinor, grandTotalMinor)
                    val subList = subcategoryMinorMap[catId]?.entries
                        ?.filter { it.value > 0L }
                        ?.map { (subId, subMinor) ->
                            val subEntity = categoriesById[subId]
                            val subPct = computePercentage(subMinor, netMinor)
                            CategorySpendStat(
                                categoryId = subId,
                                categoryName = subEntity?.name ?: "Subcategory",
                                colorHex = subEntity?.colorHex ?: catEntity?.colorHex ?: "#64748B",
                                iconName = subEntity?.iconName ?: catEntity?.iconName ?: "category",
                                netAmount = MinorMoney(subMinor),
                                percentageOfTotal = subPct,
                                transactionCount = (subcategoryCountMap[catId]?.get(subId) ?: 1).coerceAtLeast(1)
                            )
                        }
                        ?.sortedByDescending { it.netAmount }
                        ?: emptyList()

                    CategorySpendStat(
                        categoryId = catId,
                        categoryName = catEntity?.name ?: "Uncategorized",
                        colorHex = catEntity?.colorHex ?: "#64748B",
                        iconName = catEntity?.iconName ?: "category",
                        netAmount = MinorMoney(netMinor),
                        percentageOfTotal = pct,
                        transactionCount = (txCountByCategoryId[catId] ?: 1).coerceAtLeast(1),
                        subcategories = subList
                    )
                }
                .sortedByDescending { it.netAmount }
        }
    }

    override fun observeSixMonthTrendBars(
        referenceDateEpochDay: Long,
        monthStartDay: Int
    ): Flow<List<MonthlyTrendBar>> {
        val safeStartDay = monthStartDay.coerceIn(1, 31)
        val cyclesReversed = mutableListOf<Pair<LocalDate, LocalDate>>()
        var cursorEpochDay = referenceDateEpochDay
        repeat(6) {
            val (startEpoch, endEpoch) = AccountingRulesEngine.computeFinancialMonthRange(
                referenceDateEpochDay = cursorEpochDay,
                monthStartDay = safeStartDay
            )
            cyclesReversed.add(LocalDate.ofEpochDay(startEpoch) to LocalDate.ofEpochDay(endEpoch))
            cursorEpochDay = startEpoch - 1L
        }
        val sixCycles: List<Pair<LocalDate, LocalDate>> = cyclesReversed.reversed()

        val overallStartEpochDay = sixCycles.first().first.toEpochDay()
        val overallEndEpochDay = sixCycles.last().second.toEpochDay()
        val labelFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)

        return combine(
            transactionDao.observeTransactionsInRange(
                startDay = overallStartEpochDay,
                endDay = overallEndEpochDay
            ),
            userPreferencesFlow
        ) { transactions, prefs ->
            val primaryCode = prefs.primaryCurrencyCode
            val secondaryMap = prefs.secondaryCurrencies.associate { it.code.trim().uppercase() to it.exchangeRateToPrimary }

            sixCycles.map { (startDate, endDate) ->
                val cycleStart = startDate.toEpochDay()
                val cycleEnd = endDate.toEpochDay()

                var incomeMinor = 0L
                var expenseRawMinor = 0L

                for (txWithDetails in transactions) {
                    val tx = txWithDetails.transaction
                    if (tx.ledgerDateEpochDay !in cycleStart..cycleEnd) continue

                    val rawAmount = Math.abs(tx.amountMinor)
                    val rawFee = Math.abs(tx.transferFeeMinor)
                    val amount = convertToPrimaryMinor(rawAmount, tx.currencyCode, primaryCode, secondaryMap)
                    val fee = convertToPrimaryMinor(rawFee, tx.currencyCode, primaryCode, secondaryMap)

                    when (tx.type) {
                        TransactionType.INCOME -> {
                            incomeMinor = Math.addExact(incomeMinor, amount)
                        }
                        TransactionType.EXPENSE -> {
                            expenseRawMinor = Math.addExact(expenseRawMinor, Math.addExact(amount, fee))
                        }
                        TransactionType.REFUND -> {
                            expenseRawMinor = Math.subtractExact(expenseRawMinor, amount)
                        }
                        TransactionType.TRANSFER,
                        TransactionType.CC_SETTLEMENT -> {
                            if (fee > 0L) {
                                expenseRawMinor = Math.addExact(expenseRawMinor, fee)
                            }
                        }
                        TransactionType.OPENING_BALANCE,
                        TransactionType.RECONCILIATION -> {
                            // Capital adjustments do not count toward monthly income or expense trends
                        }
                    }
                }

                val excessRefundMinor = if (expenseRawMinor < 0L) Math.abs(expenseRawMinor) else 0L
                val adjustedIncomeMinor = Math.addExact(incomeMinor, excessRefundMinor)
                val effectiveExpenseMinor = expenseRawMinor.coerceAtLeast(0L)

                val totalIncome = MinorMoney(adjustedIncomeMinor)
                val totalExpense = MinorMoney(effectiveExpenseMinor)
                val netSavings = totalIncome - totalExpense

                MonthlyTrendBar(
                    periodLabel = startDate.format(labelFormatter),
                    startEpochDay = cycleStart,
                    endEpochDay = cycleEnd,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    netSavings = netSavings
                )
            }
        }
    }

    override fun observeTagSpendRankings(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<SpendRankingItem>> {
        return combine(
            transactionDao.observeTransactionsInRange(startEpochDay, endEpochDay),
            userPreferencesFlow
        ) { transactions, prefs ->
            val primaryCode = prefs.primaryCurrencyCode
            val secondaryMap = prefs.secondaryCurrencies.associate { it.code.trim().uppercase() to it.exchangeRateToPrimary }
            val amountByTag = HashMap<String, Long>()
            val countByTag = HashMap<String, Int>()
            val displayLabelByKey = HashMap<String, String>()

            for (txWithDetails in transactions) {
                val tx = txWithDetails.transaction
                if (txWithDetails.tags.isEmpty()) continue

                val rawAmount = Math.abs(tx.amountMinor)
                val rawFee = Math.abs(tx.transferFeeMinor)
                val amount = convertToPrimaryMinor(rawAmount, tx.currencyCode, primaryCode, secondaryMap)
                val fee = convertToPrimaryMinor(rawFee, tx.currencyCode, primaryCode, secondaryMap)

                val deltaMinor = when (tx.type) {
                    TransactionType.EXPENSE ->
                        Math.addExact(amount, fee)
                    TransactionType.REFUND ->
                        Math.negateExact(amount)
                    TransactionType.TRANSFER,
                    TransactionType.CC_SETTLEMENT ->
                        fee
                    TransactionType.INCOME,
                    TransactionType.OPENING_BALANCE,
                    TransactionType.RECONCILIATION -> 0L
                }
                if (deltaMinor == 0L) continue

                for (tag in txWithDetails.tags) {
                    val key = tag.name.trim().lowercase()
                    if (key.isEmpty()) continue
                    displayLabelByKey.putIfAbsent(key, tag.name.trim())
                    amountByTag[key] = Math.addExact(amountByTag[key] ?: 0L, deltaMinor)
                    if (deltaMinor > 0L) {
                        countByTag[key] = (countByTag[key] ?: 0) + 1
                    }
                }
            }

            val positiveEntries = amountByTag.entries.filter { it.value > 0L }
            var grandTotalMinor = 0L
            for (entry in positiveEntries) {
                grandTotalMinor = Math.addExact(grandTotalMinor, entry.value)
            }

            positiveEntries
                .map { (key, netMinor) ->
                    SpendRankingItem(
                        label = displayLabelByKey[key] ?: key,
                        totalAmount = MinorMoney(netMinor),
                        transactionCount = (countByTag[key] ?: 1).coerceAtLeast(1),
                        percentageOfTotal = computePercentage(netMinor, grandTotalMinor)
                    )
                }
                .sortedByDescending { it.totalAmount }
        }
    }

    override fun observePayeeOrNoteRankings(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<SpendRankingItem>> {
        return combine(
            transactionDao.observeTransactionsInRange(startEpochDay, endEpochDay),
            userPreferencesFlow
        ) { transactions, prefs ->
            val primaryCode = prefs.primaryCurrencyCode
            val secondaryMap = prefs.secondaryCurrencies.associate { it.code.trim().uppercase() to it.exchangeRateToPrimary }
            val amountByLabel = HashMap<String, Long>()
            val countByLabel = HashMap<String, Int>()
            val displayLabelByKey = HashMap<String, String>()

            for (txWithDetails in transactions) {
                val tx = txWithDetails.transaction
                val rawLabel = tx.payeeOrDescription.trim().ifEmpty { tx.note.trim() }
                if (rawLabel.isEmpty()) continue

                val rawAmount = Math.abs(tx.amountMinor)
                val rawFee = Math.abs(tx.transferFeeMinor)
                val amount = convertToPrimaryMinor(rawAmount, tx.currencyCode, primaryCode, secondaryMap)
                val fee = convertToPrimaryMinor(rawFee, tx.currencyCode, primaryCode, secondaryMap)

                val deltaMinor = when (tx.type) {
                    TransactionType.EXPENSE ->
                        Math.addExact(amount, fee)
                    TransactionType.REFUND ->
                        Math.negateExact(amount)
                    TransactionType.TRANSFER,
                    TransactionType.CC_SETTLEMENT ->
                        fee
                    TransactionType.INCOME,
                    TransactionType.OPENING_BALANCE,
                    TransactionType.RECONCILIATION -> 0L
                }
                if (deltaMinor == 0L) continue

                val key = rawLabel.lowercase()
                displayLabelByKey.putIfAbsent(key, rawLabel)
                amountByLabel[key] = Math.addExact(amountByLabel[key] ?: 0L, deltaMinor)
                if (deltaMinor > 0L) {
                    countByLabel[key] = (countByLabel[key] ?: 0) + 1
                }
            }

            val positiveEntries = amountByLabel.entries.filter { it.value > 0L }
            var grandTotalMinor = 0L
            for (entry in positiveEntries) {
                grandTotalMinor = Math.addExact(grandTotalMinor, entry.value)
            }

            positiveEntries
                .map { (key, netMinor) ->
                    SpendRankingItem(
                        label = displayLabelByKey[key] ?: key,
                        totalAmount = MinorMoney(netMinor),
                        transactionCount = (countByLabel[key] ?: 1).coerceAtLeast(1),
                        percentageOfTotal = computePercentage(netMinor, grandTotalMinor)
                    )
                }
                .sortedByDescending { it.totalAmount }
        }
    }

    override fun observeBudgetProgressList(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<BudgetProgress>> {
        return combine(
            budgetAndRecurringDao.observeAllBudgets(),
            categoryAndTagDao.observeAllCategories(),
            transactionDao.observeTransactionsInRange(startEpochDay, endEpochDay),
            userPreferencesFlow
        ) { budgets, categories, periodTransactions, prefs ->
            val primaryCode = prefs.primaryCurrencyCode
            val secondaryMap = prefs.secondaryCurrencies.associate { it.code.trim().uppercase() to it.exchangeRateToPrimary }
            val categoriesById = categories.associateBy { it.id }
            val childCategoryIdsByParentId = categories
                .filter { it.parentId != null }
                .groupBy({ it.parentId!! }, { it.id })

            // Compute net spend per category ID and overall period expense
            val netSpendByCategoryId = HashMap<Long, Long>()
            var overallNetExpenseMinor = 0L

            fun recordCategoryExpense(catId: Long?, deltaMinor: Long) {
                if (catId != null) {
                    val current = netSpendByCategoryId[catId] ?: 0L
                    netSpendByCategoryId[catId] = Math.addExact(current, deltaMinor)
                }
            }

            for (txWithDetails in periodTransactions) {
                val tx = txWithDetails.transaction
                val rawAmount = Math.abs(tx.amountMinor)
                val rawFee = Math.abs(tx.transferFeeMinor)
                val amount = convertToPrimaryMinor(rawAmount, tx.currencyCode, primaryCode, secondaryMap)
                val fee = convertToPrimaryMinor(rawFee, tx.currencyCode, primaryCode, secondaryMap)

                when (tx.type) {
                    TransactionType.EXPENSE -> {
                        val totalCharge = Math.addExact(amount, fee)
                        overallNetExpenseMinor = Math.addExact(overallNetExpenseMinor, totalCharge)
                        if (tx.isSplit && txWithDetails.splits.isNotEmpty()) {
                            for (split in txWithDetails.splits) {
                                val splitAmount = convertToPrimaryMinor(
                                    Math.abs(split.amountMinor),
                                    tx.currencyCode,
                                    primaryCode,
                                    secondaryMap
                                )
                                recordCategoryExpense(split.categoryId, splitAmount)
                            }
                        } else {
                            recordCategoryExpense(tx.categoryId, totalCharge)
                        }
                    }
                    TransactionType.REFUND -> {
                        overallNetExpenseMinor = Math.subtractExact(overallNetExpenseMinor, amount)
                        recordCategoryExpense(tx.categoryId, Math.negateExact(amount))
                    }
                    TransactionType.TRANSFER,
                    TransactionType.CC_SETTLEMENT -> {
                        if (fee > 0L) {
                            overallNetExpenseMinor = Math.addExact(overallNetExpenseMinor, fee)
                            recordCategoryExpense(tx.categoryId, fee)
                        }
                    }
                    TransactionType.INCOME,
                    TransactionType.OPENING_BALANCE,
                    TransactionType.RECONCILIATION -> Unit
                }
            }

            budgets.map { budget ->
                val catId = budget.categoryId
                val spentMinor = if (catId == null) {
                    overallNetExpenseMinor.coerceAtLeast(0L)
                } else {
                    // Sum spend for both the target category and any child subcategories
                    var sum = netSpendByCategoryId[catId] ?: 0L
                    val childIds = childCategoryIdsByParentId[catId].orEmpty()
                    for (childId in childIds) {
                        sum = Math.addExact(sum, netSpendByCategoryId[childId] ?: 0L)
                    }
                    sum.coerceAtLeast(0L)
                }

                val limitMoney = MinorMoney(budget.limitAmountMinor)
                val spentMoney = MinorMoney(spentMinor)
                val remainingMoney = limitMoney - spentMoney
                val usagePct = computePercentage(spentMinor, budget.limitAmountMinor)

                val categoryEntity = catId?.let { categoriesById[it] }
                BudgetProgress(
                    budgetId = budget.id,
                    categoryId = catId,
                    categoryName = categoryEntity?.name ?: "Overall Monthly Budget",
                    colorHex = categoryEntity?.colorHex ?: "#10B981",
                    limitAmount = limitMoney,
                    spentAmount = spentMoney,
                    remainingAmount = remainingMoney,
                    usagePercentage = usagePct,
                    alertThresholdPercent = budget.alertThresholdPercent
                )
            }
        }
    }

    override fun observeActiveRecurringCommitments(): Flow<List<RecurringCommitmentItem>> {
        return observeAllRecurringRules().map { rules -> rules.filter { it.isActive } }
    }

    override fun observeAllRecurringRules(): Flow<List<RecurringCommitmentItem>> {
        val accountsFlow = accountDao?.observeAllAccounts() ?: flowOf(emptyList())
        return combine(
            budgetAndRecurringDao.observeAllRecurringRules(),
            categoryAndTagDao.observeAllCategories(),
            accountsFlow
        ) { rules, categories, accounts ->
            val categoriesById = categories.associateBy { it.id }
            val accountsById = accounts.associateBy { it.id }

            rules.map { rule ->
                val cat = categoriesById[rule.categoryId]
                val acc = accountsById[rule.accountId]
                val targetAcc = accountsById[rule.transferToAccountId]
                RecurringCommitmentItem(
                    id = rule.id,
                    title = rule.title,
                    type = rule.type,
                    amount = MinorMoney(rule.amountMinor),
                    currencyCode = rule.currencyCode,
                    accountId = rule.accountId,
                    accountName = acc?.name ?: "Account #${rule.accountId}",
                    transferToAccountId = rule.transferToAccountId,
                    transferToAccountName = targetAcc?.name,
                    frequency = rule.frequency,
                    nextOccurrenceEpochDay = rule.nextOccurrenceEpochDay,
                    payeeOrDescription = rule.payeeOrDescription,
                    isActive = rule.isActive,
                    categoryId = rule.categoryId,
                    categoryName = cat?.name,
                    categoryColorHex = cat?.colorHex,
                    categoryIconName = cat?.iconName,
                    startDateEpochDay = rule.startDateEpochDay,
                    note = rule.note
                )
            }
        }
    }

    override suspend fun upsertRecurringRule(rule: RecurringCommitmentItem): Result<Long> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
        val entity = RecurringRuleEntity(
            id = rule.id,
            title = rule.title.trim(),
            type = rule.type,
            amountMinor = rule.amount.amountMinor,
            currencyCode = rule.currencyCode,
            accountId = rule.accountId,
            transferToAccountId = rule.transferToAccountId,
            categoryId = rule.categoryId,
            frequency = rule.frequency,
            startDateEpochDay = rule.startDateEpochDay,
            nextOccurrenceEpochDay = rule.nextOccurrenceEpochDay,
            payeeOrDescription = rule.payeeOrDescription.trim(),
            note = rule.note.trim(),
            isActive = rule.isActive
        )
            budgetAndRecurringDao.upsertRecurringRule(entity)
        }
    }

    override suspend fun updateRecurringRuleActive(ruleId: Long, isActive: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            budgetAndRecurringDao.updateRecurringRuleActive(ruleId, isActive)
        }
    }

    override suspend fun deleteRecurringRule(ruleId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            budgetAndRecurringDao.deleteRecurringRuleById(ruleId)
        }
    }

    override fun observeInstallmentPlansWithProgress(): Flow<List<InstallmentPlanWithProgress>> {
        val accountsFlow = accountDao?.observeAllAccounts() ?: flowOf(emptyList())
        return combine(
            transactionDao.observeAllInstallmentPlans(),
            transactionDao.observeAllInstallmentTransactions(),
            accountsFlow,
            categoryAndTagDao.observeAllCategories()
        ) { plans, installmentTxns, accounts, categories ->
            val accountsById = accounts.associateBy { it.id }
            val categoriesById = categories.associateBy { it.id }
            val today = LocalDate.now().toEpochDay()

            plans.map { plan ->
                val planTxns = installmentTxns.filter { it.installmentPlanId == plan.id }
                val totalCount = plan.monthsCount
                val paidTxns = planTxns.filter { it.ledgerDateEpochDay <= today }
                val paidCount = paidTxns.size.coerceAtMost(totalCount)
                val remainingCount = (totalCount - paidCount).coerceAtLeast(0)
                val paidMinor = paidTxns.sumOf { it.amountMinor }
                val remainingMinor = (plan.totalAmountMinor - paidMinor).coerceAtLeast(0L)
                val monthlyMinor = if (plan.monthsCount > 0) plan.totalAmountMinor / plan.monthsCount else 0L
                val nextDueTx = planTxns.filter { it.ledgerDateEpochDay > today }
                    .minByOrNull { it.ledgerDateEpochDay }
                val acc = accountsById[plan.accountId]
                val cat = categoriesById[plan.categoryId]

                InstallmentPlanWithProgress(
                    id = plan.id,
                    title = plan.title,
                    accountId = plan.accountId,
                    accountName = acc?.name ?: "Credit Card",
                    categoryId = plan.categoryId,
                    categoryName = cat?.name,
                    categoryColorHex = cat?.colorHex,
                    categoryIconName = cat?.iconName,
                    totalAmount = MinorMoney(plan.totalAmountMinor),
                    monthsCount = totalCount,
                    paidInstallmentsCount = paidCount,
                    remainingInstallmentsCount = remainingCount,
                    paidAmount = MinorMoney(paidMinor),
                    remainingAmount = MinorMoney(remainingMinor),
                    monthlyAmount = MinorMoney(monthlyMinor),
                    startDateEpochDay = plan.startDateEpochDay,
                    nextDueDateEpochDay = nextDueTx?.ledgerDateEpochDay
                )
            }
        }
    }

    override suspend fun deleteInstallmentPlan(planId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            transactionDao.deleteInstallmentPlanById(planId)
        }
    }

    override suspend fun processDueRecurringRulesOnce(todayEpochDay: Long): Result<Int> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
        val executeBatch: suspend () -> Int = {
            val dueRules = budgetAndRecurringDao.getDueRecurringRulesOnce(todayEpochDay)
            var generatedCount = 0
            for (rule in dueRules) {
                if (!rule.isActive) continue

                val txEntity = TransactionEntity(
                    id = 0L,
                    type = rule.type,
                    timestampEpochMillis = System.currentTimeMillis(),
                    ledgerDateEpochDay = rule.nextOccurrenceEpochDay,
                    amountMinor = rule.amountMinor,
                    currencyCode = rule.currencyCode,
                    accountId = rule.accountId,
                    transferToAccountId = rule.transferToAccountId,
                    categoryId = rule.categoryId,
                    isSplit = false,
                    payeeOrDescription = rule.payeeOrDescription.ifEmpty { rule.title },
                    note = rule.note,
                    recurringRuleId = rule.id
                )
                transactionDao.insertTransactionRaw(txEntity)
                generatedCount++

                val nextDate = RecurringScheduleCalculator.computeNextOccurrence(
                    currentDate = LocalDate.ofEpochDay(rule.nextOccurrenceEpochDay),
                    frequency = rule.frequency
                )
                val shouldDeactivate = rule.endDateEpochDay != null && nextDate.toEpochDay() > rule.endDateEpochDay
                val updatedRule = rule.copy(
                    nextOccurrenceEpochDay = nextDate.toEpochDay(),
                    isActive = !shouldDeactivate
                )
                budgetAndRecurringDao.updateRecurringRule(updatedRule)
            }
            generatedCount
        }

            if (database != null) {
                database.withTransaction { executeBatch() }
            } else {
                executeBatch()
            }
        }
    }

    override suspend fun upsertBudget(
        budgetId: Long,
        categoryId: Long?,
        limitAmount: MinorMoney,
        alertThresholdPercent: Int
    ): Result<Long> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
        require(limitAmount.isPositive) {
            "Budget limit amount must be strictly positive."
        }
        val validatedThreshold = alertThresholdPercent.coerceIn(1, 100)

        val existingForCategory = budgetAndRecurringDao.getBudgetByCategoryIdOnce(categoryId)
        val resolvedId = when {
            budgetId != 0L -> budgetId
            existingForCategory != null -> existingForCategory.id
            else -> 0L
        }

        val entity = BudgetEntity(
            id = resolvedId,
            categoryId = categoryId,
            limitAmountMinor = limitAmount.amountMinor,
            alertThresholdPercent = validatedThreshold,
            updatedAtEpochMillis = System.currentTimeMillis()
        )
            budgetAndRecurringDao.upsertBudget(entity)
        }
    }

    override suspend fun deleteBudget(budgetId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            budgetAndRecurringDao.deleteBudgetById(budgetId)
        }
    }

    private fun applyCashflowDelta(
        currentNetMinor: Long,
        txWithDetails: TransactionWithDetails,
        primaryCode: String,
        secondaryMap: Map<String, Double>
    ): Long {
        val tx = txWithDetails.transaction
        val amount = convertToPrimaryMinor(Math.abs(tx.amountMinor), tx.currencyCode, primaryCode, secondaryMap)
        val fee = convertToPrimaryMinor(Math.abs(tx.transferFeeMinor), tx.currencyCode, primaryCode, secondaryMap)

        return when (tx.type) {
            TransactionType.INCOME,
            TransactionType.REFUND -> Math.addExact(currentNetMinor, amount)
            TransactionType.EXPENSE -> Math.subtractExact(currentNetMinor, Math.addExact(amount, fee))
            TransactionType.TRANSFER,
            TransactionType.CC_SETTLEMENT -> Math.subtractExact(currentNetMinor, fee)
            TransactionType.OPENING_BALANCE,
            TransactionType.RECONCILIATION -> currentNetMinor
        }
    }

    private fun computePercentage(numeratorMinor: Long, denominatorMinor: Long): Float {
        if (numeratorMinor <= 0L || denominatorMinor <= 0L) return 0f
        return BigDecimal.valueOf(numeratorMinor)
            .multiply(BigDecimal.valueOf(100L))
            .divide(BigDecimal.valueOf(denominatorMinor), 2, RoundingMode.HALF_UP)
            .toFloat()
    }

    companion object {
        private const val UNATEGORIZED_BUCKET_ID = -1L
    }
}
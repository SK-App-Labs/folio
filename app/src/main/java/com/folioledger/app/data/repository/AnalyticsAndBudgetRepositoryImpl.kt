// File: app/src/main/java/com/folioledger/app/data/repository/AnalyticsAndBudgetRepositoryImpl.kt
package com.folioledger.app.data.repository

import com.folioledger.app.data.local.dao.BudgetAndRecurringDao
import com.folioledger.app.data.local.dao.CategoryAndTagDao
import com.folioledger.app.data.local.dao.TransactionDao
import com.folioledger.app.data.local.entity.BudgetEntity
import com.folioledger.app.data.local.entity.CategoryEntity
import com.folioledger.app.data.local.entity.TransactionWithDetails
import com.folioledger.app.domain.model.BudgetProgress
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.PeriodLedgerSummary
import com.folioledger.app.domain.model.SpendRankingItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.repository.AnalyticsAndBudgetRepository
import com.folioledger.app.domain.usecase.AccountingRulesEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AnalyticsAndBudgetRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val categoryAndTagDao: CategoryAndTagDao,
    private val budgetAndRecurringDao: BudgetAndRecurringDao
) : AnalyticsAndBudgetRepository {

    override fun observePeriodLedgerSummary(
        startEpochDay: Long,
        endEpochDay: Long,
        includeCarryOver: Boolean
    ): Flow<PeriodLedgerSummary> {
        return transactionDao.observeAllTransactions().map { allTransactions ->
            var priorNetMinor = 0L
            var periodIncomeMinor = 0L
            var periodExpenseRawMinor = 0L

            for (txWithDetails in allTransactions) {
                val tx = txWithDetails.transaction
                val day = tx.ledgerDateEpochDay

                if (day < startEpochDay) {
                    if (includeCarryOver) {
                        priorNetMinor = applyCashflowDelta(priorNetMinor, txWithDetails)
                    }
                } else if (day in startEpochDay..endEpochDay) {
                    val amount = Math.abs(tx.amountMinor)
                    val fee = Math.abs(tx.transferFeeMinor)

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
                    }
                }
            }

            val totalIncome = MinorMoney(periodIncomeMinor)
            val totalExpense = MinorMoney(periodExpenseRawMinor.coerceAtLeast(0L))
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
            categoryAndTagDao.observeAllCategories()
        ) { periodTransactions, categories ->
            val categoriesById = categories.associateBy { it.id }
            val netMinorByCategoryId = HashMap<Long, Long>()
            val txCountByCategoryId = HashMap<Long, Int>()

            fun getTargetMainCategoryId(rawCatId: Long?): Long {
                val catId = rawCatId ?: UNATEGORIZED_BUCKET_ID
                val catEntity = categoriesById[catId] ?: return catId
                return catEntity.parentId ?: catId
            }

            fun addCategoryDelta(categoryId: Long?, deltaMinor: Long, countIncrement: Int) {
                val targetMainId = getTargetMainCategoryId(categoryId)
                val currentAmount = netMinorByCategoryId[targetMainId] ?: 0L
                netMinorByCategoryId[targetMainId] = Math.addExact(currentAmount, deltaMinor)
                if (countIncrement > 0) {
                    txCountByCategoryId[targetMainId] = (txCountByCategoryId[targetMainId] ?: 0) + countIncrement
                }
            }

            for (txWithDetails in periodTransactions) {
                val tx = txWithDetails.transaction
                val amount = Math.abs(tx.amountMinor)
                val fee = Math.abs(tx.transferFeeMinor)

                when (type) {
                    CategoryType.EXPENSE -> {
                        when (tx.type) {
                            TransactionType.EXPENSE -> {
                                if (tx.isSplit && txWithDetails.splits.isNotEmpty()) {
                                    for (split in txWithDetails.splits) {
                                        addCategoryDelta(
                                            categoryId = split.categoryId,
                                            deltaMinor = Math.abs(split.amountMinor),
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
                            TransactionType.INCOME -> Unit
                        }
                    }

                    CategoryType.INCOME -> {
                        if (tx.type == TransactionType.INCOME) {
                            if (tx.isSplit && txWithDetails.splits.isNotEmpty()) {
                                for (split in txWithDetails.splits) {
                                    addCategoryDelta(
                                        categoryId = split.categoryId,
                                        deltaMinor = Math.abs(split.amountMinor),
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
                    CategorySpendStat(
                        categoryId = catId,
                        categoryName = catEntity?.name ?: "Uncategorized",
                        colorHex = catEntity?.colorHex ?: "#64748B",
                        iconName = catEntity?.iconName ?: "category",
                        netAmount = MinorMoney(netMinor),
                        percentageOfTotal = pct,
                        transactionCount = (txCountByCategoryId[catId] ?: 1).coerceAtLeast(1)
                    )
                }
                .sortedByDescending { it.netAmount }
        }
    }

    override fun observeSixMonthTrendBars(
        referenceDateEpochDay: Long,
        monthStartDay: Int
    ): Flow<List<MonthlyTrendBar>> {
        val safeStartDay = monthStartDay.coerceIn(1, 28)
        val (currentCycleStartEpochDay, _) = AccountingRulesEngine.computeFinancialMonthRange(
            referenceDateEpochDay = referenceDateEpochDay,
            monthStartDay = safeStartDay
        )
        val currentCycleStartDate = LocalDate.ofEpochDay(currentCycleStartEpochDay)

        val sixCycles: List<Pair<LocalDate, LocalDate>> = (5 downTo 0).map { monthsBack ->
            val start = currentCycleStartDate.minusMonths(monthsBack.toLong())
            val end = start.plusMonths(1).minusDays(1)
            start to end
        }

        val overallStartEpochDay = sixCycles.first().first.toEpochDay()
        val overallEndEpochDay = sixCycles.last().second.toEpochDay()
        val labelFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)

        return transactionDao.observeTransactionsInRange(
            startDay = overallStartEpochDay,
            endDay = overallEndEpochDay
        ).map { transactions ->
            sixCycles.map { (startDate, endDate) ->
                val cycleStart = startDate.toEpochDay()
                val cycleEnd = endDate.toEpochDay()

                var incomeMinor = 0L
                var expenseRawMinor = 0L

                for (txWithDetails in transactions) {
                    val tx = txWithDetails.transaction
                    if (tx.ledgerDateEpochDay !in cycleStart..cycleEnd) continue

                    val amount = Math.abs(tx.amountMinor)
                    val fee = Math.abs(tx.transferFeeMinor)

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
                    }
                }

                val totalIncome = MinorMoney(incomeMinor)
                val totalExpense = MinorMoney(expenseRawMinor.coerceAtLeast(0L))
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
        return transactionDao.observeTransactionsInRange(startEpochDay, endEpochDay)
            .map { transactions ->
                val amountByTag = HashMap<String, Long>()
                val countByTag = HashMap<String, Int>()
                val displayLabelByKey = HashMap<String, String>()

                for (txWithDetails in transactions) {
                    val tx = txWithDetails.transaction
                    if (txWithDetails.tags.isEmpty()) continue

                    val deltaMinor = when (tx.type) {
                        TransactionType.EXPENSE ->
                            Math.addExact(Math.abs(tx.amountMinor), Math.abs(tx.transferFeeMinor))
                        TransactionType.REFUND ->
                            Math.negateExact(Math.abs(tx.amountMinor))
                        TransactionType.TRANSFER,
                        TransactionType.CC_SETTLEMENT ->
                            Math.abs(tx.transferFeeMinor)
                        TransactionType.INCOME -> 0L
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
        return transactionDao.observeTransactionsInRange(startEpochDay, endEpochDay)
            .map { transactions ->
                val amountByLabel = HashMap<String, Long>()
                val countByLabel = HashMap<String, Int>()
                val displayLabelByKey = HashMap<String, String>()

                for (txWithDetails in transactions) {
                    val tx = txWithDetails.transaction
                    val rawLabel = tx.payeeOrDescription.trim().ifEmpty { tx.note.trim() }
                    if (rawLabel.isEmpty()) continue

                    val deltaMinor = when (tx.type) {
                        TransactionType.EXPENSE ->
                            Math.addExact(Math.abs(tx.amountMinor), Math.abs(tx.transferFeeMinor))
                        TransactionType.REFUND ->
                            Math.negateExact(Math.abs(tx.amountMinor))
                        TransactionType.TRANSFER,
                        TransactionType.CC_SETTLEMENT ->
                            Math.abs(tx.transferFeeMinor)
                        TransactionType.INCOME -> 0L
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
            transactionDao.observeTransactionsInRange(startEpochDay, endEpochDay)
        ) { budgets, categories, periodTransactions ->
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
                val amount = Math.abs(tx.amountMinor)
                val fee = Math.abs(tx.transferFeeMinor)

                when (tx.type) {
                    TransactionType.EXPENSE -> {
                        val totalCharge = Math.addExact(amount, fee)
                        overallNetExpenseMinor = Math.addExact(overallNetExpenseMinor, totalCharge)
                        if (tx.isSplit && txWithDetails.splits.isNotEmpty()) {
                            for (split in txWithDetails.splits) {
                                recordCategoryExpense(split.categoryId, Math.abs(split.amountMinor))
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
                    TransactionType.INCOME -> Unit
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

    override suspend fun upsertBudget(
        budgetId: Long,
        categoryId: Long?,
        limitAmount: MinorMoney,
        alertThresholdPercent: Int
    ): Result<Long> = runCatching {
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

    override suspend fun deleteBudget(budgetId: Long): Result<Unit> = runCatching {
        budgetAndRecurringDao.deleteBudgetById(budgetId)
    }

    private fun applyCashflowDelta(
        currentNetMinor: Long,
        txWithDetails: TransactionWithDetails
    ): Long {
        val tx = txWithDetails.transaction
        val amount = Math.abs(tx.amountMinor)
        val fee = Math.abs(tx.transferFeeMinor)

        return when (tx.type) {
            TransactionType.INCOME,
            TransactionType.REFUND -> Math.addExact(currentNetMinor, amount)
            TransactionType.EXPENSE -> Math.subtractExact(currentNetMinor, Math.addExact(amount, fee))
            TransactionType.TRANSFER,
            TransactionType.CC_SETTLEMENT -> Math.subtractExact(currentNetMinor, fee)
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
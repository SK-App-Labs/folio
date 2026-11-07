package com.folioledger.app.domain.util

import com.folioledger.app.domain.model.AccountPeriodFlowSummary
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CalendarDayCell
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.DailyLedgerGroup
import com.folioledger.app.domain.model.FinancialPeriodRange
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.model.WeeklyLedgerBucket
import com.folioledger.app.domain.usecase.AccountingRulesEngine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object LedgerBucketingCalculator {

    private val SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
    private val MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)
    private val shortMonthYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)
    private val shortDayMonthFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
    private val fullDayMonthYearFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

    fun resolveClampedDate(yearMonth: YearMonth, desiredDayOfMonth: Int): LocalDate {
        val safeDay = desiredDayOfMonth.coerceIn(1, 31)
        val clampedDay = minOf(safeDay, yearMonth.lengthOfMonth())
        return yearMonth.atDay(clampedDay)
    }

    fun computeFinancialMonthRange(
        referenceDateEpochDay: Long,
        monthStartDay: Int
    ): FinancialPeriodRange {
        val safeStartDay = monthStartDay.coerceIn(1, 31)
        val (startEpoch, endEpoch) = AccountingRulesEngine.computeFinancialMonthRange(
            referenceDateEpochDay = referenceDateEpochDay,
            monthStartDay = safeStartDay
        )
        val startDate = LocalDate.ofEpochDay(startEpoch)
        val endDate = LocalDate.ofEpochDay(endEpoch)

        val title = startDate.format(shortMonthYearFormatter)
        val subtitle = "${startDate.format(shortDayMonthFormatter)} – ${endDate.format(fullDayMonthYearFormatter)}"

        return FinancialPeriodRange(
            startEpochDay = startEpoch,
            endEpochDay = endEpoch,
            title = title,
            subtitle = subtitle,
            anchorYear = startDate.year,
            anchorMonthValue = startDate.monthValue
        )
    }

    fun stepFinancialMonth(
        currentReferenceEpochDay: Long,
        monthStartDay: Int,
        deltaMonths: Long
    ): Long {
        val safeStartDay = monthStartDay.coerceIn(1, 31)
        val currentRange = computeFinancialMonthRange(currentReferenceEpochDay, safeStartDay)
        val anchorYearMonth = YearMonth.of(currentRange.anchorYear, currentRange.anchorMonthValue)
        val targetYearMonth = anchorYearMonth.plusMonths(deltaMonths)
        return resolveClampedDate(targetYearMonth, safeStartDay).toEpochDay()
    }

    fun computeFinancialYearMonthRanges(
        anchorYear: Int,
        monthStartDay: Int
    ): List<FinancialPeriodRange> {
        val safeStartDay = monthStartDay.coerceIn(1, 31)
        return (1..12).map { monthIndex ->
            val ym = YearMonth.of(anchorYear, monthIndex)
            val monthAnchorDate = resolveClampedDate(ym, safeStartDay)
            computeFinancialMonthRange(monthAnchorDate.toEpochDay(), safeStartDay)
        }
    }

    fun computeIncomeAndExpenseTotals(
        transactions: List<TransactionItem>,
        scopedAccountId: Long? = null,
        primaryCurrencySpec: CurrencySpec? = null,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): Pair<MinorMoney, MinorMoney> {
        val secondaryMap = if (scopedAccountId == null && primaryCurrencySpec != null && secondaryCurrencies.isNotEmpty()) {
            secondaryCurrencies.associateBy { it.code }
        } else {
            emptyMap()
        }

        fun toTargetMinor(amountMinor: Long, txCurrencyCode: String): Long {
            if (scopedAccountId != null || primaryCurrencySpec == null || txCurrencyCode == primaryCurrencySpec.code) {
                return amountMinor
            }
            val rate = secondaryMap[txCurrencyCode]?.exchangeRateToPrimary ?: 1.0
            val txCurrencySpec = SupportedCurrencies.fromCode(txCurrencyCode)
            return MoneyFormatterAndCalculator.convertMinorMoney(
                sourceMinor = amountMinor,
                sourceFractionDigits = txCurrencySpec.fractionDigits,
                targetFractionDigits = primaryCurrencySpec.fractionDigits,
                rateToTarget = rate
            )
        }

        var incomeMinor = 0L
        var expenseMinor = 0L
        transactions.forEach { tx ->
            val effectiveAmountMinor = toTargetMinor(tx.amount.amountMinor, tx.currencyCode)
            when (tx.type) {
                TransactionType.INCOME -> {
                    incomeMinor = Math.addExact(incomeMinor, effectiveAmountMinor)
                }
                TransactionType.EXPENSE -> {
                    expenseMinor = Math.addExact(expenseMinor, effectiveAmountMinor)
                }
                TransactionType.REFUND -> {
                    if (scopedAccountId != null) {
                        incomeMinor = Math.addExact(incomeMinor, effectiveAmountMinor)
                    } else {
                        expenseMinor = Math.subtractExact(expenseMinor, effectiveAmountMinor)
                    }
                }
                TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> {
                    if (scopedAccountId != null) {
                        if (tx.transferToAccountId == scopedAccountId) {
                            incomeMinor = Math.addExact(incomeMinor, tx.amount.amountMinor)
                        } else if (tx.accountId == scopedAccountId) {
                            expenseMinor = Math.addExact(
                                expenseMinor,
                                Math.addExact(tx.amount.amountMinor, tx.transferFee.amountMinor)
                            )
                        }
                    } else if (tx.transferFee.isPositive) {
                        val feeMinor = toTargetMinor(tx.transferFee.amountMinor, tx.currencyCode)
                        expenseMinor = Math.addExact(expenseMinor, feeMinor)
                    }
                }
                TransactionType.OPENING_BALANCE,
                TransactionType.RECONCILIATION -> {
                    // Capital events do not count toward operating income or expense totals
                }
            }
        }
        if (scopedAccountId == null && expenseMinor < 0L) {
            val excessRefund = Math.abs(expenseMinor)
            incomeMinor = Math.addExact(incomeMinor, excessRefund)
            expenseMinor = 0L
        }
        return MinorMoney(incomeMinor) to MinorMoney(expenseMinor)
    }

    fun computeTransferTotal(
        transactions: List<TransactionItem>,
        primaryCurrencySpec: CurrencySpec? = null,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): MinorMoney {
        val transferTxns = transactions.filter { it.type == TransactionType.TRANSFER || it.type == TransactionType.CC_SETTLEMENT }
        val transferTotalMinor = transferTxns.sumOf { tx ->
            if (primaryCurrencySpec != null && tx.currencyCode != primaryCurrencySpec.code) {
                val rate = secondaryCurrencies.firstOrNull { it.code == tx.currencyCode }?.exchangeRateToPrimary ?: 1.0
                val sourceSpec = SupportedCurrencies.fromCode(tx.currencyCode)
                MoneyFormatterAndCalculator.convertMinorMoney(
                    sourceMinor = tx.amount.amountMinor,
                    sourceFractionDigits = sourceSpec.fractionDigits,
                    targetFractionDigits = primaryCurrencySpec.fractionDigits,
                    rateToTarget = rate
                )
            } else {
                tx.amount.amountMinor
            }
        }
        return MinorMoney(transferTotalMinor)
    }

    fun buildDailyGroups(
        transactions: List<TransactionItem>,
        todayEpochDay: Long,
        scopedAccountId: Long? = null,
        primaryCurrencySpec: CurrencySpec? = null,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): List<DailyLedgerGroup> {
        if (transactions.isEmpty()) return emptyList()

        return transactions
            .groupBy { it.ledgerDateEpochDay }
            .entries
            .sortedByDescending { it.key }
            .map { (epochDay, dayTxns) ->
                val date = LocalDate.ofEpochDay(epochDay)
                val sortedTxns = dayTxns.sortedByDescending { it.timestampEpochMillis }
                val (dayInc, dayExp) = computeIncomeAndExpenseTotals(
                    transactions = sortedTxns,
                    scopedAccountId = scopedAccountId,
                    primaryCurrencySpec = primaryCurrencySpec,
                    secondaryCurrencies = secondaryCurrencies
                )
                val transferTxns = sortedTxns.filter { it.type == TransactionType.TRANSFER || it.type == TransactionType.CC_SETTLEMENT }
                val transferTotalMinor = transferTxns.sumOf { tx ->
                    if (primaryCurrencySpec != null && tx.currencyCode != primaryCurrencySpec.code) {
                        val rate = secondaryCurrencies.firstOrNull { it.code == tx.currencyCode }?.exchangeRateToPrimary ?: 1.0
                        val sourceSpec = SupportedCurrencies.fromCode(tx.currencyCode)
                        MoneyFormatterAndCalculator.convertMinorMoney(
                            sourceMinor = tx.amount.amountMinor,
                            sourceFractionDigits = sourceSpec.fractionDigits,
                            targetFractionDigits = primaryCurrencySpec.fractionDigits,
                            rateToTarget = rate
                        )
                    } else {
                        tx.amount.amountMinor
                    }
                }
                DailyLedgerGroup(
                    epochDay = epochDay,
                    dayOfMonthText = String.format(Locale.US, "%02d", date.dayOfMonth),
                    dayOfWeekText = date.dayOfWeek
                        .getDisplayName(JavaTextStyle.SHORT, Locale.US)
                        .uppercase(Locale.US),
                    monthYearText = date.format(MONTH_YEAR_FORMATTER),
                    isToday = epochDay == todayEpochDay,
                    dailyIncome = dayInc,
                    dailyExpense = dayExp,
                    dailyNet = dayInc - dayExp,
                    dailyTransferTotal = MinorMoney(transferTotalMinor),
                    transferCount = transferTxns.size,
                    transactions = sortedTxns
                )
            }
    }

    fun buildWeekdayHeaders(weekStartDayIso: Int): List<String> {
        val startIso = weekStartDayIso.coerceIn(1, 7)
        return (0 until 7).map { offset ->
            val dowNumber = ((startIso - 1 + offset) % 7) + 1
            DayOfWeek.of(dowNumber).getDisplayName(JavaTextStyle.SHORT, Locale.US)
        }
    }

    fun buildCalendarGridCells(
        periodRange: FinancialPeriodRange,
        weekStartDayIso: Int,
        transactions: List<TransactionItem>,
        todayEpochDay: Long,
        scopedAccountId: Long? = null,
        primaryCurrencySpec: CurrencySpec? = null,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList(),
        numberGrouping: NumberGroupingSystem = NumberGroupingSystem.INDIAN
    ): List<CalendarDayCell> {
        val startDate = LocalDate.ofEpochDay(periodRange.startEpochDay)
        val endDate = LocalDate.ofEpochDay(periodRange.endEpochDay)
        val startDow = DayOfWeek.of(weekStartDayIso.coerceIn(1, 7))

        val firstGridDate = startDate.with(TemporalAdjusters.previousOrSame(startDow))
        val endDow = startDow.minus(1)
        val lastGridDate = endDate.with(TemporalAdjusters.nextOrSame(endDow))

        val txByDay = transactions.groupBy { it.ledgerDateEpochDay }
        val currency = primaryCurrencySpec ?: SupportedCurrencies.INR

        val cells = mutableListOf<CalendarDayCell>()
        var cursor = firstGridDate
        while (!cursor.isAfter(lastGridDate)) {
            val epoch = cursor.toEpochDay()
            val inPeriod = epoch in periodRange.startEpochDay..periodRange.endEpochDay
            if (!inPeriod) {
                cells.add(
                    CalendarDayCell(
                        epochDay = null,
                        dayNumber = null,
                        isInActivePeriod = false
                    )
                )
            } else {
                val dayTxns = txByDay[epoch].orEmpty()
                val (inc, exp) = computeIncomeAndExpenseTotals(
                    transactions = dayTxns,
                    scopedAccountId = scopedAccountId,
                    primaryCurrencySpec = primaryCurrencySpec,
                    secondaryCurrencies = secondaryCurrencies
                )
                val transferTxns = dayTxns.filter { it.type == TransactionType.TRANSFER || it.type == TransactionType.CC_SETTLEMENT }
                val transferTotalMinor = transferTxns.sumOf { tx ->
                    if (primaryCurrencySpec != null && tx.currencyCode != primaryCurrencySpec.code) {
                        val rate = secondaryCurrencies.firstOrNull { it.code == tx.currencyCode }?.exchangeRateToPrimary ?: 1.0
                        val sourceSpec = SupportedCurrencies.fromCode(tx.currencyCode)
                        MoneyFormatterAndCalculator.convertMinorMoney(
                            sourceMinor = tx.amount.amountMinor,
                            sourceFractionDigits = sourceSpec.fractionDigits,
                            targetFractionDigits = primaryCurrencySpec.fractionDigits,
                            rateToTarget = rate
                        )
                    } else {
                        tx.amount.amountMinor
                    }
                }

                val formattedInc = if (inc.isPositive) {
                    MoneyFormatterAndCalculator.formatCompactMinorMoney(inc, currency, numberGrouping)
                } else ""
                val formattedExp = if (exp.isPositive) {
                    MoneyFormatterAndCalculator.formatCompactMinorMoney(exp, currency, numberGrouping)
                } else ""
                val formattedTransfer = if (transferTotalMinor > 0) {
                    MoneyFormatterAndCalculator.formatCompactMinorMoney(MinorMoney(transferTotalMinor), currency, numberGrouping)
                } else ""

                cells.add(
                    CalendarDayCell(
                        epochDay = epoch,
                        dayNumber = cursor.dayOfMonth,
                        dateLabel = cursor.format(SHORT_DATE_FORMATTER),
                        isInActivePeriod = true,
                        isToday = epoch == todayEpochDay,
                        incomeTotal = inc,
                        expenseTotal = exp,
                        netTotal = inc - exp,
                        transferTotal = MinorMoney(transferTotalMinor),
                        transferCount = transferTxns.size,
                        transactionCount = dayTxns.size,
                        formattedCompactIncome = formattedInc,
                        formattedCompactExpense = formattedExp,
                        formattedCompactTransfer = formattedTransfer
                    )
                )
            }
            cursor = cursor.plusDays(1)
        }
        return cells
    }

    fun buildWeeklyBucketsForPeriod(
        periodRange: FinancialPeriodRange,
        weekStartDayIso: Int,
        transactions: List<TransactionItem>,
        scopedAccountId: Long? = null,
        primaryCurrencySpec: CurrencySpec? = null,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): List<WeeklyLedgerBucket> {
        val periodStart = LocalDate.ofEpochDay(periodRange.startEpochDay)
        val periodEnd = LocalDate.ofEpochDay(periodRange.endEpochDay)
        val startDow = DayOfWeek.of(weekStartDayIso.coerceIn(1, 7))
        val endDow = startDow.minus(1)

        val buckets = mutableListOf<WeeklyLedgerBucket>()
        var currentStart = periodStart
        var weekNum = 1

        while (!currentStart.isAfter(periodEnd)) {
            val candidateEnd = currentStart.with(TemporalAdjusters.nextOrSame(endDow))
            val currentEnd = if (candidateEnd.isAfter(periodEnd)) periodEnd else candidateEnd

            val startEpoch = currentStart.toEpochDay()
            val endEpoch = currentEnd.toEpochDay()
            val weekTxns = transactions
                .filter { it.ledgerDateEpochDay in startEpoch..endEpoch }
                .sortedWith(
                    compareByDescending<TransactionItem> { it.ledgerDateEpochDay }
                        .thenByDescending { it.timestampEpochMillis }
                )
            val (inc, exp) = computeIncomeAndExpenseTotals(
                transactions = weekTxns,
                scopedAccountId = scopedAccountId,
                primaryCurrencySpec = primaryCurrencySpec,
                secondaryCurrencies = secondaryCurrencies
            )

            buckets.add(
                WeeklyLedgerBucket(
                    weekIndex = weekNum,
                    label = "Week $weekNum",
                    dateRangeSubtitle = "${currentStart.format(SHORT_DATE_FORMATTER)} – ${currentEnd.format(SHORT_DATE_FORMATTER)}",
                    startEpochDay = startEpoch,
                    endEpochDay = endEpoch,
                    incomeTotal = inc,
                    expenseTotal = exp,
                    netTotal = inc - exp,
                    transactions = weekTxns
                )
            )

            currentStart = currentEnd.plusDays(1)
            weekNum++
        }
        return buckets
    }

    fun buildAccountPeriodFlows(
        transactions: List<TransactionItem>,
        accounts: List<AccountSummary>
    ): List<AccountPeriodFlowSummary> {
        if (transactions.isEmpty()) return emptyList()

        val accountsById = accounts.associateBy { it.id }
        val accountNamesById = mutableMapOf<Long, String>()
        val txnsByAccountId = mutableMapOf<Long, MutableList<TransactionItem>>()

        for (tx in transactions) {
            accountNamesById.putIfAbsent(tx.accountId, tx.accountName)
            txnsByAccountId.getOrPut(tx.accountId) { mutableListOf() }.add(tx)

            val destId = tx.transferToAccountId
            val destName = tx.transferToAccountName
            if (destId != null && destName != null) {
                accountNamesById.putIfAbsent(destId, destName)
                txnsByAccountId.getOrPut(destId) { mutableListOf() }.add(tx)
            }
        }

        return txnsByAccountId.map { (accId, txns) ->
            val account = accountsById[accId]
            val accName = account?.name ?: accountNamesById[accId] ?: "Account #$accId"
            val currencyCode = account?.currencyCode ?: txns.firstOrNull()?.currencyCode ?: "INR"
            val (inc, exp) = computeIncomeAndExpenseTotals(txns, accId)
            AccountPeriodFlowSummary(
                accountId = accId,
                accountName = accName,
                currencyCode = currencyCode,
                incomeTotal = inc,
                expenseTotal = exp,
                netChange = inc - exp,
                transactionCount = txns.size
            )
        }.sortedByDescending { it.transactionCount }
    }

    fun buildScopedCategorySpendStats(
        transactions: List<TransactionItem>,
        categories: List<CategoryNode>,
        maxCategories: Int = 6
    ): List<CategorySpendStat> {
        val netMinorByCategoryId = mutableMapOf<Long, Long>()
        val txCountByCategoryId = mutableMapOf<Long, Int>()
        val categoryDetails = mutableMapOf<Long, Triple<String, String, String>>()

        fun indexCategory(node: CategoryNode) {
            categoryDetails[node.id] = Triple(node.name, node.colorHex, node.iconName)
            node.subcategories.forEach { sub ->
                categoryDetails[sub.id] = Triple(node.name, node.colorHex, node.iconName)
            }
        }
        categories.forEach { indexCategory(it) }

        fun addDelta(catId: Long?, amountMinor: Long) {
            val resolvedId = catId ?: -1L
            val current = netMinorByCategoryId[resolvedId] ?: 0L
            netMinorByCategoryId[resolvedId] = current + amountMinor
            txCountByCategoryId[resolvedId] = (txCountByCategoryId[resolvedId] ?: 0) + 1
        }

        for (tx in transactions) {
            when (tx.type) {
                TransactionType.EXPENSE -> {
                    if (tx.isSplit && tx.splits.isNotEmpty()) {
                        for (split in tx.splits) {
                            addDelta(split.categoryId, split.amount.amountMinor)
                        }
                    } else {
                        val total = tx.amount.amountMinor + tx.transferFee.amountMinor
                        addDelta(tx.categoryId, total)
                    }
                }
                TransactionType.REFUND -> {
                    addDelta(tx.categoryId, -tx.amount.amountMinor)
                }
                TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> {
                    if (tx.transferFee.isPositive) {
                        addDelta(tx.categoryId, tx.transferFee.amountMinor)
                    }
                }
                else -> Unit
            }
        }

        val positive = netMinorByCategoryId.filter { it.value > 0L }
        val grandTotal = positive.values.sum()
        if (grandTotal <= 0L) return emptyList()

        return positive.map { (catId, minor) ->
            val details = categoryDetails[catId]
            val name = details?.first ?: if (catId == -1L) "Uncategorized" else "Category #$catId"
            val color = details?.second ?: "#64748B"
            val icon = details?.third ?: "category"
            val pct = (minor.toFloat() / grandTotal.toFloat()) * 100f
            CategorySpendStat(
                categoryId = catId,
                categoryName = name,
                colorHex = color,
                iconName = icon,
                netAmount = MinorMoney(minor),
                percentageOfTotal = pct,
                transactionCount = txCountByCategoryId[catId] ?: 1
            )
        }.sortedByDescending { it.netAmount.amountMinor }.take(maxCategories)
    }
}

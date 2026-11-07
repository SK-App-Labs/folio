package com.folioledger.app.ui.ledger

import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MonthlyLedgerCalculationsTest {

    private val inrSpec = SupportedCurrencies.INR // 2 fraction digits
    private val usdSpec = SupportedCurrencies.USD // 2 fraction digits
    private val jpySpec = SupportedCurrencies.JPY // 0 fraction digits

    @Test
    fun testMultiCurrencyConversionUsingSecondaryRates() {
        val secondaryCurrencies = listOf(
            SecondaryCurrency(code = "USD", exchangeRateToPrimary = 85.0),
            SecondaryCurrency(code = "JPY", exchangeRateToPrimary = 0.55)
        )
        val secondaryMap = secondaryCurrencies.associateBy { it.code }

        fun toTargetMinor(
            amountMinor: Long,
            txCurrencyCode: String,
            primarySpec: CurrencySpec
        ): Long {
            if (txCurrencyCode == primarySpec.code) return amountMinor
            val rate = secondaryMap[txCurrencyCode]?.exchangeRateToPrimary ?: 1.0
            val txCurrencySpec = SupportedCurrencies.fromCode(txCurrencyCode)
            return MoneyFormatterAndCalculator.convertMinorMoney(
                sourceMinor = amountMinor,
                sourceFractionDigits = txCurrencySpec.fractionDigits,
                targetFractionDigits = primarySpec.fractionDigits,
                rateToTarget = rate
            )
        }

        // 10 USD (1000 cents) at 85.0 -> 850.00 INR (85000 paise)
        val usdConverted = toTargetMinor(1000L, "USD", inrSpec)
        assertEquals(85000L, usdConverted)

        // 1000 JPY (0 fraction digits) at 0.55 -> 550.00 INR (55000 paise)
        val jpyConverted = toTargetMinor(1000L, "JPY", inrSpec)
        assertEquals(55000L, jpyConverted)

        // INR to INR (same currency) -> no conversion
        val inrConverted = toTargetMinor(50000L, "INR", inrSpec)
        assertEquals(50000L, inrConverted)
    }

    @Test
    fun testAccordionExpansionToggleLogic() {
        // Initial state: expandedYearlyMonths is null -> defaults to active month (e.g. month 10)
        var expandedYearlyMonths: Set<Int>? = null
        val activeMonth = 10

        val initialEffective = expandedYearlyMonths ?: setOf(activeMonth)
        assertEquals(setOf(10), initialEffective)
        assertTrue(10 in initialEffective)
        assertFalse(9 in initialEffective)

        // User clicks month 10 to collapse it
        fun toggle(month: Int) {
            val base = expandedYearlyMonths ?: setOf(activeMonth)
            expandedYearlyMonths = if (month in base) base - month else base + month
        }

        toggle(10)
        // Now expandedYearlyMonths is emptySet() (not null)
        val collapsedEffective = expandedYearlyMonths ?: setOf(activeMonth)
        assertEquals(emptySet<Int>(), collapsedEffective)
        assertFalse(10 in collapsedEffective)

        // User clicks month 5 to expand it
        toggle(5)
        val month5Effective = expandedYearlyMonths ?: setOf(activeMonth)
        assertEquals(setOf(5), month5Effective)
        assertTrue(5 in month5Effective)
        assertFalse(10 in month5Effective)

        // User clicks month 5 again to collapse it
        toggle(5)
        val reCollapsedEffective = expandedYearlyMonths ?: setOf(activeMonth)
        assertEquals(emptySet<Int>(), reCollapsedEffective)
    }

    @Test
    fun testYearlyTotalsSummation() {
        // Mock 12 months with various income and expense totals
        val monthlyIncomes = listOf(100L, 200L, 150L, 300L, 0L, 250L, 400L, 500L, 100L, 200L, 350L, 450L)
        val monthlyExpenses = listOf(80L, 120L, 100L, 250L, 50L, 200L, 300L, 400L, 90L, 150L, 300L, 400L)

        val totalIncomeMinor = monthlyIncomes.sum()
        val totalExpenseMinor = monthlyExpenses.sum()
        val totalNetMinor = totalIncomeMinor - totalExpenseMinor

        val yearlyIncome = MinorMoney(totalIncomeMinor)
        val yearlyExpense = MinorMoney(totalExpenseMinor)
        val yearlyNet = yearlyIncome - yearlyExpense

        assertEquals(3000L, yearlyIncome.amountMinor)
        assertEquals(2440L, yearlyExpense.amountMinor)
        assertEquals(560L, yearlyNet.amountMinor)
    }

    @Test
    fun testWeeklyDefaultExpansionAndToggleLogic() {
        val week1Start = java.time.LocalDate.of(2026, 10, 1).toEpochDay()
        val week1End = java.time.LocalDate.of(2026, 10, 4).toEpochDay()

        val week5Start = java.time.LocalDate.of(2026, 10, 26).toEpochDay()
        val week5End = java.time.LocalDate.of(2026, 10, 31).toEpochDay()

        val mockBuckets = listOf(
            WeeklyLedgerBucket(
                weekIndex = 1,
                label = "Week 1",
                dateRangeSubtitle = "01 Oct – 04 Oct",
                startEpochDay = week1Start,
                endEpochDay = week1End,
                incomeTotal = MinorMoney.ZERO,
                expenseTotal = MinorMoney.ZERO,
                netTotal = MinorMoney.ZERO,
                transactions = emptyList()
            ),
            WeeklyLedgerBucket(
                weekIndex = 5,
                label = "Week 5",
                dateRangeSubtitle = "26 Oct – 31 Oct",
                startEpochDay = week5Start,
                endEpochDay = week5End,
                incomeTotal = MinorMoney.ZERO,
                expenseTotal = MinorMoney.ZERO,
                netTotal = MinorMoney.ZERO,
                transactions = emptyList()
            )
        )

        // Today is Oct 30th (in week 5)
        val today = java.time.LocalDate.of(2026, 10, 30).toEpochDay()
        val currentPeriodStart = java.time.LocalDate.of(2026, 10, 1).toEpochDay()
        val currentPeriodEnd = java.time.LocalDate.of(2026, 10, 31).toEpochDay()

        // In Current Month: smart default expands Week 5 (containing today)
        val isCurrentPeriod = today in currentPeriodStart..currentPeriodEnd
        val defaultExpandedWeeksCurrentMonth = if (isCurrentPeriod) {
            val activeWeek = mockBuckets.firstOrNull { bucket ->
                today in bucket.startEpochDay..bucket.endEpochDay
            }?.weekIndex ?: mockBuckets.lastOrNull()?.weekIndex ?: 1
            setOf(activeWeek)
        } else {
            emptySet()
        }

        assertEquals(setOf(5), defaultExpandedWeeksCurrentMonth)

        // In Past or Future Month: all weeks start collapsed
        val pastPeriodStart = java.time.LocalDate.of(2026, 9, 1).toEpochDay()
        val pastPeriodEnd = java.time.LocalDate.of(2026, 9, 30).toEpochDay()
        val isPastPeriod = today in pastPeriodStart..pastPeriodEnd
        val defaultExpandedWeeksPastMonth = if (isPastPeriod) {
            setOf(1)
        } else {
            emptySet()
        }

        assertEquals(emptySet<Int>(), defaultExpandedWeeksPastMonth)

        // Toggling logic
        var expandedWeekIndices: Set<Int>? = null
        val initialEffective = expandedWeekIndices ?: defaultExpandedWeeksCurrentMonth
        assertEquals(setOf(5), initialEffective)

        fun toggle(week: Int, currentRendered: Set<Int>) {
            val base = expandedWeekIndices ?: currentRendered
            expandedWeekIndices = if (week in base) base - week else base + week
        }

        // Collapse week 5
        toggle(5, initialEffective)
        val collapsedEffective = expandedWeekIndices ?: defaultExpandedWeeksCurrentMonth
        assertEquals(emptySet<Int>(), collapsedEffective)

        // Expand week 1
        toggle(1, collapsedEffective)
        val week1Effective = expandedWeekIndices ?: defaultExpandedWeeksCurrentMonth
        assertEquals(setOf(1), week1Effective)
    }

    @Test
    fun testWeeklyTransactionDateFormatting() {
        val date = java.time.LocalDate.of(2026, 10, 2)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM", java.util.Locale.US)
        val formatted = date.format(formatter)
        assertEquals("Fri, 02 Oct", formatted)
    }

    @Test
    fun testSavingsRateBadgeDisplayLogic() {
        fun resolveSavingsBadge(incomeMinor: Long, expenseMinor: Long): String {
            val hasIncome = incomeMinor > 0L
            val hasExpense = expenseMinor > 0L
            return when {
                hasIncome -> {
                    val net = incomeMinor - expenseMinor
                    val pct = ((net.toFloat() / incomeMinor.toFloat()) * 100f).coerceIn(-100f, 100f)
                    String.format(java.util.Locale.US, "Savings Rate: %.1f%%", pct)
                }
                hasExpense -> "Deficit (No Income)"
                else -> "No Activity"
            }
        }

        assertEquals("Savings Rate: 60.0%", resolveSavingsBadge(500000L, 200000L))
        assertEquals("Savings Rate: -50.0%", resolveSavingsBadge(200000L, 300000L))
        assertEquals("Deficit (No Income)", resolveSavingsBadge(0L, 150000L))
        assertEquals("No Activity", resolveSavingsBadge(0L, 0L))
    }

    @Test
    fun testAccountPeriodFlowSummaryMultiCurrency() {
        val acc1Flow = AccountPeriodFlowSummary(
            accountId = 1L,
            accountName = "HDFC Bank",
            currencyCode = "INR",
            incomeTotal = MinorMoney(5000000L),
            expenseTotal = MinorMoney(1200000L),
            netChange = MinorMoney(3800000L),
            transactionCount = 12
        )

        val acc2Flow = AccountPeriodFlowSummary(
            accountId = 2L,
            accountName = "US Dollar Savings",
            currencyCode = "USD",
            incomeTotal = MinorMoney(100000L),
            expenseTotal = MinorMoney(25000L),
            netChange = MinorMoney(75000L),
            transactionCount = 3
        )

        assertEquals("INR", acc1Flow.currencyCode)
        assertEquals(3800000L, acc1Flow.netChange.amountMinor)
        assertEquals("USD", acc2Flow.currencyCode)
        assertEquals(75000L, acc2Flow.netChange.amountMinor)
    }

    @Test
    fun testBudgetDisplaySelectionLogic() {
        data class MockBudget(val id: Long, val categoryId: Long?, val name: String)

        val overall = MockBudget(1L, null, "Overall Monthly Budget")
        val groceries = MockBudget(2L, 10L, "Groceries")
        val dining = MockBudget(3L, 11L, "Dining")
        val entertainment = MockBudget(4L, 12L, "Entertainment")

        // Case 1: Overall budget exists along with categories
        val listWithOverall = listOf(overall, groceries, dining, entertainment)
        val selectedWithOverall = if (listWithOverall.any { it.categoryId == null }) {
            val ov = listWithOverall.first { it.categoryId == null }
            val cats = listWithOverall.filter { it.categoryId != null }
            listOf(ov) + cats.take(2)
        } else {
            listWithOverall.take(3)
        }

        assertEquals(3, selectedWithOverall.size)
        assertEquals("Overall Monthly Budget", selectedWithOverall[0].name)
        assertEquals("Groceries", selectedWithOverall[1].name)
        assertEquals("Dining", selectedWithOverall[2].name)

        // Case 2: No overall budget exists, only categories
        val listCategoriesOnly = listOf(groceries, dining, entertainment)
        val selectedCatsOnly = if (listCategoriesOnly.any { it.categoryId == null }) {
            val ov = listCategoriesOnly.first { it.categoryId == null }
            val cats = listCategoriesOnly.filter { it.categoryId != null }
            listOf(ov) + cats.take(2)
        } else {
            listCategoriesOnly.take(3)
        }

        assertEquals(3, selectedCatsOnly.size)
        assertEquals("Groceries", selectedCatsOnly[0].name)
        assertEquals("Dining", selectedCatsOnly[1].name)
        assertEquals("Entertainment", selectedCatsOnly[2].name)
    }

    @Test
    fun testSmartYearAwareMonthExpansion() {
        val today = java.time.LocalDate.of(2026, 10, 30).toEpochDay()
        val year2026Start = java.time.LocalDate.of(2026, 1, 1).toEpochDay()
        val year2026End = java.time.LocalDate.of(2026, 12, 31).toEpochDay()

        val year2025Start = java.time.LocalDate.of(2025, 1, 1).toEpochDay()
        val year2025End = java.time.LocalDate.of(2025, 12, 31).toEpochDay()

        fun resolveDefaultExpandedMonths(yearStart: Long, yearEnd: Long, anchorMonthVal: Int): Set<Int> {
            val isCurrentYear = today in yearStart..yearEnd
            return if (isCurrentYear) {
                setOf(anchorMonthVal)
            } else {
                emptySet()
            }
        }

        // Current Year (2026): Oct (10) is auto-expanded
        val expandedCurrentYear = resolveDefaultExpandedMonths(year2026Start, year2026End, 10)
        assertEquals(setOf(10), expandedCurrentYear)

        // Past Year (2025): Starts completely collapsed for high-level bird's-eye view
        val expandedPastYear = resolveDefaultExpandedMonths(year2025Start, year2025End, 10)
        assertEquals(emptySet<Int>(), expandedPastYear)

        // Toggling logic in past year
        var userExpandedMonths: Set<Int>? = null
        val baseSet = userExpandedMonths ?: expandedPastYear
        assertEquals(emptySet<Int>(), baseSet)

        // User expands Month 5 (May 2025)
        val afterToggleMay = if (5 in baseSet) baseSet - 5 else baseSet + 5
        assertEquals(setOf(5), afterToggleMay)

        // User toggles May again -> collapses
        val afterToggleMayAgain = if (5 in afterToggleMay) afterToggleMay - 5 else afterToggleMay + 5
        assertEquals(emptySet<Int>(), afterToggleMayAgain)
    }

    @Test
    fun testMonthlyCompositeKeyUniquenessAcrossYears() {
        val oct2026Start = java.time.LocalDate.of(2026, 10, 1).toEpochDay()
        val oct2025Start = java.time.LocalDate.of(2025, 10, 1).toEpochDay()

        val key2026 = "${oct2026Start}_10"
        val key2025 = "${oct2025Start}_10"

        // Keys for the same month across different years must not collide
        assertFalse(key2026 == key2025)
        assertEquals("${oct2026Start}_10", key2026)
        assertEquals("${oct2025Start}_10", key2025)
    }

    @Test
    fun testIsCurrentActiveMonthOnlyMatchesRealTimeCurrentMonth() {
        val today = java.time.LocalDate.of(2026, 10, 30).toEpochDay()

        // Months in 2026 (Active Year)
        val months2026 = (1..12).map { m ->
            val start = java.time.LocalDate.of(2026, m, 1).toEpochDay()
            val end = java.time.LocalDate.of(2026, m, java.time.YearMonth.of(2026, m).lengthOfMonth()).toEpochDay()
            m to (start..end)
        }
        val currentMonths2026 = months2026.filter { today in it.second }.map { it.first }
        assertEquals(listOf(10), currentMonths2026) // Only October 2026 is current

        // Months in 2025 (Past Year) - switching years should NEVER show "Current" on any month
        val months2025 = (1..12).map { m ->
            val start = java.time.LocalDate.of(2025, m, 1).toEpochDay()
            val end = java.time.LocalDate.of(2025, m, java.time.YearMonth.of(2025, m).lengthOfMonth()).toEpochDay()
            m to (start..end)
        }
        val currentMonths2025 = months2025.filter { today in it.second }.map { it.first }
        assertTrue(currentMonths2025.isEmpty())

        // Months in 2027 (Future Year) - switching years should NEVER show "Current" on any month
        val months2027 = (1..12).map { m ->
            val start = java.time.LocalDate.of(2027, m, 1).toEpochDay()
            val end = java.time.LocalDate.of(2027, m, java.time.YearMonth.of(2027, m).lengthOfMonth()).toEpochDay()
            m to (start..end)
        }
        val currentMonths2027 = months2027.filter { today in it.second }.map { it.first }
        assertTrue(currentMonths2027.isEmpty())
    }

    @Test
    fun testDrillDownToWeekStateTransitions() {
        // Given reference control state currently on Monthly subtab
        val octStartEpoch = java.time.LocalDate.of(2026, 10, 1).toEpochDay()
        var selectedSubTab = LedgerSubTab.MONTHLY
        var referenceEpochDay = java.time.LocalDate.of(2026, 5, 1).toEpochDay()
        var expandedWeekIndices: Set<Int>? = null

        // When user drills down to Week 3 of October
        val targetMonthStart = octStartEpoch
        val targetWeekIndex = 3

        // Simulate drillDownToWeek(targetMonthStart, targetWeekIndex)
        referenceEpochDay = targetMonthStart
        selectedSubTab = LedgerSubTab.WEEKLY
        expandedWeekIndices = setOf(targetWeekIndex)

        // Then verify target state
        assertEquals(LedgerSubTab.WEEKLY, selectedSubTab)
        assertEquals(octStartEpoch, referenceEpochDay)
        assertEquals(setOf(3), expandedWeekIndices)

        // Verify only Week 3 is expanded in the Weekly subtab
        val weekIndicesInMonth = 1..5
        val expandedWeeks = weekIndicesInMonth.filter { it in expandedWeekIndices }
        assertEquals(listOf(3), expandedWeeks)
    }

    @Test
    fun testFormatCompactMinorMoneyVariousRanges() {
        // ₹450 (45000 paise) -> "450"
        assertEquals("450", MoneyFormatterAndCalculator.formatCompactMinorMoney(MinorMoney(45000L), inrSpec))

        // ₹1,200 (120000 paise) -> "1.2k"
        assertEquals("1.2k", MoneyFormatterAndCalculator.formatCompactMinorMoney(MinorMoney(120000L), inrSpec))

        // ₹75,000 (7500000 paise) -> "75k"
        assertEquals("75k", MoneyFormatterAndCalculator.formatCompactMinorMoney(MinorMoney(7500000L), inrSpec))

        // ₹1,50,000 (15000000 paise) with Indian grouping -> "1.5L"
        assertEquals("1.5L", MoneyFormatterAndCalculator.formatCompactMinorMoney(
            money = MinorMoney(15000000L),
            currencySpec = inrSpec,
            grouping = com.folioledger.app.domain.model.NumberGroupingSystem.INDIAN
        ))

        // $150,000 with International grouping -> "150k"
        assertEquals("150k", MoneyFormatterAndCalculator.formatCompactMinorMoney(
            money = MinorMoney(15000000L),
            currencySpec = usdSpec,
            grouping = com.folioledger.app.domain.model.NumberGroupingSystem.INTERNATIONAL
        ))

        // ₹2,50,00,000 (2.5 Crore) with Indian grouping -> "2.5Cr"
        assertEquals("2.5Cr", MoneyFormatterAndCalculator.formatCompactMinorMoney(
            money = MinorMoney(2500000000L),
            currencySpec = inrSpec,
            grouping = com.folioledger.app.domain.model.NumberGroupingSystem.INDIAN
        ))

        // $1,500,000 with International grouping -> "1.5M"
        assertEquals("1.5M", MoneyFormatterAndCalculator.formatCompactMinorMoney(
            money = MinorMoney(150000000L),
            currencySpec = usdSpec,
            grouping = com.folioledger.app.domain.model.NumberGroupingSystem.INTERNATIONAL
        ))

        // Zero
        assertEquals("0", MoneyFormatterAndCalculator.formatCompactMinorMoney(MinorMoney.ZERO, inrSpec))
    }

    @Test
    fun testCalendarSmartPeriodNavigationSelectsTodayInCurrentMonth() {
        val today = java.time.LocalDate.of(2026, 10, 30).toEpochDay()
        val octStart = java.time.LocalDate.of(2026, 10, 1).toEpochDay()
        val octEnd = java.time.LocalDate.of(2026, 10, 31).toEpochDay()
        val sepStart = java.time.LocalDate.of(2026, 9, 1).toEpochDay()
        val sepEnd = java.time.LocalDate.of(2026, 9, 30).toEpochDay()

        // When navigating to Sep 2026 (past month): today (Oct 30) is NOT in Sep
        val resolvedSep = if (today in sepStart..sepEnd) today else sepStart
        assertEquals(sepStart, resolvedSep) // Defaults to 1st of month

        // When navigating to Oct 2026 (current month): today (Oct 30) IS in Oct
        val resolvedOct = if (today in octStart..octEnd) today else octStart
        assertEquals(today, resolvedOct) // Defaults to Today!
    }

    @Test
    fun testDailyLedgerGroupTransferTrackingAndConversion() {
        val secondaryCurrencies = listOf(
            SecondaryCurrency(code = "USD", exchangeRateToPrimary = 85.0)
        )
        val transferTx1 = TransactionItem(
            id = 1L,
            type = TransactionType.TRANSFER,
            timestampEpochMillis = 1000L,
            ledgerDateEpochDay = 20000L,
            amount = MinorMoney(500000L), // ₹5,000 INR
            currencyCode = "INR",
            accountId = 1L,
            accountName = "Checking",
            transferToAccountId = 2L,
            transferToAccountName = "Savings"
        )
        val transferTx2 = TransactionItem(
            id = 2L,
            type = TransactionType.TRANSFER,
            timestampEpochMillis = 2000L,
            ledgerDateEpochDay = 20000L,
            amount = MinorMoney(1000L), // $10 USD (1000 cents) -> ₹850 (85000 paise)
            currencyCode = "USD",
            accountId = 3L,
            accountName = "Forex",
            transferToAccountId = 2L,
            transferToAccountName = "Savings"
        )
        val dayTxns = listOf(transferTx1, transferTx2)

        val transferTxns = dayTxns.filter { it.type == TransactionType.TRANSFER || it.type == TransactionType.CC_SETTLEMENT }
        val transferTotalMinor = transferTxns.sumOf { tx ->
            if (tx.currencyCode != inrSpec.code) {
                val rate = secondaryCurrencies.firstOrNull { it.code == tx.currencyCode }?.exchangeRateToPrimary ?: 1.0
                val sourceSpec = SupportedCurrencies.fromCode(tx.currencyCode)
                MoneyFormatterAndCalculator.convertMinorMoney(
                    sourceMinor = tx.amount.amountMinor,
                    sourceFractionDigits = sourceSpec.fractionDigits,
                    targetFractionDigits = inrSpec.fractionDigits,
                    rateToTarget = rate
                )
            } else {
                tx.amount.amountMinor
            }
        }

        val dailyGroup = DailyLedgerGroup(
            epochDay = 20000L,
            dayOfMonthText = "15",
            dayOfWeekText = "MON",
            monthYearText = "Oct 2026",
            isToday = false,
            dailyIncome = MinorMoney.ZERO,
            dailyExpense = MinorMoney.ZERO,
            dailyNet = MinorMoney.ZERO,
            dailyTransferTotal = MinorMoney(transferTotalMinor),
            transferCount = transferTxns.size,
            transactions = dayTxns
        )

        assertEquals(2, dailyGroup.transferCount)
        // ₹5,000 (500000 paise) + ₹850 (85000 paise) = ₹5,850 (585000 paise)
        assertEquals(585000L, dailyGroup.dailyTransferTotal.amountMinor)
    }

    @Test
    fun testCategoryHierarchyFormatting() {
        fun formatCategoryTitle(parentCategoryName: String?, categoryName: String?, type: TransactionType): String {
            return when {
                !parentCategoryName.isNullOrBlank() && !categoryName.isNullOrBlank() ->
                    "$parentCategoryName › $categoryName"
                !categoryName.isNullOrBlank() ->
                    categoryName
                else ->
                    type.name.replace('_', ' ')
            }
        }

        // Parent and child present
        assertEquals("Food & Drinks › Coffee", formatCategoryTitle("Food & Drinks", "Coffee", TransactionType.EXPENSE))

        // Only child present
        assertEquals("Groceries", formatCategoryTitle(null, "Groceries", TransactionType.EXPENSE))
        assertEquals("Groceries", formatCategoryTitle("", "Groceries", TransactionType.EXPENSE))

        // Neither present -> falls back to type name formatted
        assertEquals("EXPENSE", formatCategoryTitle(null, null, TransactionType.EXPENSE))
        assertEquals("CC SETTLEMENT", formatCategoryTitle("", "", TransactionType.CC_SETTLEMENT))
    }

    @Test
    fun testCurrencySpecResolutionInDetailSheet() {
        // Transaction recorded in USD
        val usdTx = TransactionItem(
            id = 1L,
            type = TransactionType.EXPENSE,
            timestampEpochMillis = 1000L,
            ledgerDateEpochDay = 20000L,
            amount = MinorMoney(5000L), // $50.00
            currencyCode = "USD",
            accountId = 1L,
            accountName = "US Card"
        )
        val resolvedSpec = SupportedCurrencies.getByCode(usdTx.currencyCode) ?: inrSpec
        assertEquals("USD", resolvedSpec.code)
        assertEquals("$", resolvedSpec.symbol)
        assertEquals(2, resolvedSpec.fractionDigits)

        // Formatted amount uses USD symbol, not primary INR ₹
        val formatted = MoneyFormatterAndCalculator.formatMinorMoney(usdTx.amount, resolvedSpec)
        assertTrue(formatted.contains("$"))
        assertFalse(formatted.contains("₹"))
    }

    @Test
    fun testInstallmentMetadataFormatting() {
        fun formatInstallmentLabel(index: Int?, total: Int?): String? {
            return if (index != null && total != null) "$index of $total" else null
        }

        val installmentTx = TransactionItem(
            id = 10L,
            type = TransactionType.EXPENSE,
            timestampEpochMillis = 5000L,
            ledgerDateEpochDay = 20000L,
            amount = MinorMoney(1000000L),
            currencyCode = "INR",
            accountId = 2L,
            accountName = "HDFC Credit Card",
            installmentIndex = 3,
            installmentTotalCount = 12
        )
        val regularTx = installmentTx.copy(installmentIndex = null, installmentTotalCount = null)

        assertEquals("3 of 12", formatInstallmentLabel(installmentTx.installmentIndex, installmentTx.installmentTotalCount))
        assertEquals(null, formatInstallmentLabel(regularTx.installmentIndex, regularTx.installmentTotalCount))
    }

    @Test
    fun testTransTabDoubleTapResetToDailyLogic() {
        var currentSubTab = LedgerSubTab.MONTHLY
        var lastTransTabTapTime = 0L
        var jumpToTodayCalls = 0
        var resetToDailyCalls = 0

        fun onTransTabTapped(now: Long) {
            if (now - lastTransTabTapTime < 500L) {
                // Double tap
                currentSubTab = LedgerSubTab.DAILY
                resetToDailyCalls++
                lastTransTabTapTime = 0L
            } else {
                // Single tap
                lastTransTabTapTime = now
                jumpToTodayCalls++
            }
        }

        // 1. Single tap at t = 1000ms: only triggers jump to today, stays on MONTHLY
        onTransTabTapped(1000L)
        assertEquals(1, jumpToTodayCalls)
        assertEquals(0, resetToDailyCalls)
        assertEquals(LedgerSubTab.MONTHLY, currentSubTab)

        // 2. Second tap at t = 1250ms (250ms delta < 500ms): triggers double-tap reset to DAILY
        onTransTabTapped(1250L)
        assertEquals(1, jumpToTodayCalls)
        assertEquals(1, resetToDailyCalls)
        assertEquals(LedgerSubTab.DAILY, currentSubTab)

        // 3. User switches to CALENDAR
        currentSubTab = LedgerSubTab.CALENDAR

        // 4. Tap at t = 3000ms, then tap at t = 3800ms (delta 800ms > 500ms): both are single taps
        onTransTabTapped(3000L)
        assertEquals(2, jumpToTodayCalls)
        assertEquals(1, resetToDailyCalls)
        assertEquals(LedgerSubTab.CALENDAR, currentSubTab)

        onTransTabTapped(3800L)
        assertEquals(3, jumpToTodayCalls)
        assertEquals(1, resetToDailyCalls)
        assertEquals(LedgerSubTab.CALENDAR, currentSubTab)

        // 5. Quick follow-up tap at t = 4000ms (delta 200ms < 500ms): resets to DAILY!
        onTransTabTapped(4000L)
        assertEquals(3, jumpToTodayCalls)
        assertEquals(2, resetToDailyCalls)
        assertEquals(LedgerSubTab.DAILY, currentSubTab)
    }

    @Test
    fun testFilterTabCountsAndCategorySeparation() {
        val expenseCat1 = com.folioledger.app.domain.model.CategoryNode(
            id = 1L,
            name = "Food",
            type = com.folioledger.app.domain.model.CategoryType.EXPENSE,
            parentId = null,
            iconName = "fastfood",
            colorHex = "#FF0000"
        )
        val expenseCat2 = com.folioledger.app.domain.model.CategoryNode(
            id = 2L,
            name = "Shopping",
            type = com.folioledger.app.domain.model.CategoryType.EXPENSE,
            parentId = null,
            iconName = "shopping_bag",
            colorHex = "#FF5500"
        )
        val incomeCat1 = com.folioledger.app.domain.model.CategoryNode(
            id = 3L,
            name = "Salary",
            type = com.folioledger.app.domain.model.CategoryType.INCOME,
            parentId = null,
            iconName = "payments",
            colorHex = "#00FF00"
        )
        val allCategories = listOf(expenseCat1, expenseCat2, incomeCat1)

        val expenseCats = allCategories.filter { it.type == com.folioledger.app.domain.model.CategoryType.EXPENSE }
        val incomeCats = allCategories.filter { it.type == com.folioledger.app.domain.model.CategoryType.INCOME }

        assertEquals(2, expenseCats.size)
        assertEquals(1, incomeCats.size)

        val expenseCategoryIds = setOf(1L, 2L)
        val incomeCategoryIds = setOf(3L)

        // Filter with 1 expense category, 1 tag, 1 income type, 1 account, and transfers
        val filter = com.folioledger.app.domain.model.TransactionSearchFilter(
            categoryIds = setOf(1L), // 1 expense category
            tags = setOf("lunch"),  // 1 tag
            types = setOf(TransactionType.INCOME, TransactionType.TRANSFER), // 1 income, 1 transfer
            accountIds = setOf(10L, 20L), // 2 accounts
            minAmount = MinorMoney(100L) // 1 amount bound
        )

        var expFilterCount = 0
        if (TransactionType.EXPENSE in filter.types) expFilterCount++
        if (TransactionType.REFUND in filter.types) expFilterCount++
        expFilterCount += filter.categoryIds.count { it in expenseCategoryIds }
        expFilterCount += filter.tags.size
        assertEquals(2, expFilterCount) // 1 category + 1 tag

        var incFilterCount = 0
        if (TransactionType.INCOME in filter.types) incFilterCount++
        incFilterCount += filter.categoryIds.count { it in incomeCategoryIds }
        assertEquals(1, incFilterCount) // 1 income type

        var accFilterCount = 0
        accFilterCount += filter.accountIds.size
        if (TransactionType.TRANSFER in filter.types || TransactionType.CC_SETTLEMENT in filter.types) accFilterCount++
        if (filter.minAmount != null || filter.maxAmount != null) accFilterCount++
        assertEquals(4, accFilterCount) // 2 accounts + 1 transfer + 1 amount bound
    }

    @Test
    fun testToggleFilterTransfersLogic() {
        var types = emptySet<TransactionType>()

        fun toggleTransfers() {
            val hasTransfers = TransactionType.TRANSFER in types || TransactionType.CC_SETTLEMENT in types
            types = if (hasTransfers) {
                types - TransactionType.TRANSFER - TransactionType.CC_SETTLEMENT
            } else {
                types + TransactionType.TRANSFER + TransactionType.CC_SETTLEMENT
            }
        }

        // Initially no transfers
        assertFalse(TransactionType.TRANSFER in types)
        assertFalse(TransactionType.CC_SETTLEMENT in types)

        // Toggle on: both TRANSFER and CC_SETTLEMENT added
        toggleTransfers()
        assertTrue(TransactionType.TRANSFER in types)
        assertTrue(TransactionType.CC_SETTLEMENT in types)

        // Toggle off: both removed
        toggleTransfers()
        assertFalse(TransactionType.TRANSFER in types)
        assertFalse(TransactionType.CC_SETTLEMENT in types)
    }

    @Test
    fun testSmartAdaptiveFilterSummaryBannerLogic() {
        fun resolveBannerType(income: MinorMoney, expense: MinorMoney, transfer: MinorMoney): String {
            val hasIncome = income.isPositive
            val hasExpense = expense.isPositive
            val hasTransfer = transfer.isPositive
            return when {
                hasIncome && hasExpense -> "THREE_COLUMN_MIXED"
                hasExpense && !hasIncome && !hasTransfer -> "SINGLE_EXPENSE"
                hasIncome && !hasExpense && !hasTransfer -> "SINGLE_INCOME"
                hasTransfer && !hasIncome && !hasExpense -> "SINGLE_TRANSFER"
                else -> "THREE_COLUMN_FALLBACK"
            }
        }

        // 1. Only expenses
        assertEquals("SINGLE_EXPENSE", resolveBannerType(MinorMoney.ZERO, MinorMoney(1450000L), MinorMoney.ZERO))

        // 2. Only income
        assertEquals("SINGLE_INCOME", resolveBannerType(MinorMoney(8500000L), MinorMoney.ZERO, MinorMoney.ZERO))

        // 3. Only transfers
        assertEquals("SINGLE_TRANSFER", resolveBannerType(MinorMoney.ZERO, MinorMoney.ZERO, MinorMoney(500000L)))

        // 4. Mixed income & expense
        assertEquals("THREE_COLUMN_MIXED", resolveBannerType(MinorMoney(5000000L), MinorMoney(2000000L), MinorMoney.ZERO))
    }

    @Test
    fun testFilteredTransfersSummationWithForeignCurrency() {
        val transfer1 = TransactionItem(
            id = 1L,
            type = TransactionType.TRANSFER,
            timestampEpochMillis = 1000L,
            ledgerDateEpochDay = 20000L,
            amount = MinorMoney(100000L), // ₹1,000 INR
            currencyCode = "INR",
            accountId = 1L,
            accountName = "Checking",
            transferToAccountId = 2L,
            transferToAccountName = "Savings"
        )
        val transfer2 = TransactionItem(
            id = 2L,
            type = TransactionType.CC_SETTLEMENT,
            timestampEpochMillis = 2000L,
            ledgerDateEpochDay = 20000L,
            amount = MinorMoney(2000L), // $20 USD -> ₹1,700 at 85.0
            currencyCode = "USD",
            accountId = 1L,
            accountName = "Checking",
            transferToAccountId = 3L,
            transferToAccountName = "US Credit Card"
        )
        val nonTransfer = TransactionItem(
            id = 3L,
            type = TransactionType.EXPENSE,
            timestampEpochMillis = 3000L,
            ledgerDateEpochDay = 20000L,
            amount = MinorMoney(50000L),
            currencyCode = "INR",
            accountId = 1L,
            accountName = "Checking"
        )

        val filteredTxns = listOf(transfer1, transfer2, nonTransfer)
        val secondaryCurrencies = listOf(com.folioledger.app.domain.model.SecondaryCurrency(code = "USD", exchangeRateToPrimary = 85.0))

        val transferMinor = filteredTxns
            .filter { it.type == TransactionType.TRANSFER || it.type == TransactionType.CC_SETTLEMENT }
            .sumOf { tx ->
                if (tx.currencyCode != inrSpec.code) {
                    val rate = secondaryCurrencies.firstOrNull { it.code == tx.currencyCode }?.exchangeRateToPrimary ?: 1.0
                    val sourceSpec = SupportedCurrencies.fromCode(tx.currencyCode)
                    MoneyFormatterAndCalculator.convertMinorMoney(
                        sourceMinor = tx.amount.amountMinor,
                        sourceFractionDigits = sourceSpec.fractionDigits,
                        targetFractionDigits = inrSpec.fractionDigits,
                        rateToTarget = rate
                    )
                } else {
                    tx.amount.amountMinor
                }
            }

        // ₹1,000 (100000 paise) + ₹1,700 (170000 paise) = ₹2,700 (270000 paise)
        assertEquals(270000L, transferMinor)
    }

    @Test
    fun testCalendarTransferIncludesCCSettlement() {
        val transferTx = TransactionItem(
            id = 1L,
            type = TransactionType.TRANSFER,
            timestampEpochMillis = 1000L,
            ledgerDateEpochDay = 20000L,
            amount = MinorMoney(500000L), // ₹5,000
            currencyCode = "INR",
            accountId = 1L,
            accountName = "Bank A",
            transferToAccountId = 2L,
            transferToAccountName = "Bank B"
        )
        val ccSettlementTx = TransactionItem(
            id = 2L,
            type = TransactionType.CC_SETTLEMENT,
            timestampEpochMillis = 2000L,
            ledgerDateEpochDay = 20000L,
            amount = MinorMoney(300000L), // ₹3,000
            currencyCode = "INR",
            accountId = 1L,
            accountName = "Bank A",
            transferToAccountId = 3L,
            transferToAccountName = "Credit Card"
        )
        val expenseTx = TransactionItem(
            id = 3L,
            type = TransactionType.EXPENSE,
            timestampEpochMillis = 3000L,
            ledgerDateEpochDay = 20000L,
            amount = MinorMoney(10000L), // ₹100
            currencyCode = "INR",
            accountId = 1L,
            accountName = "Bank A"
        )

        val dayTxns = listOf(transferTx, ccSettlementTx, expenseTx)
        val calendarTransferTxns = dayTxns.filter { it.type == TransactionType.TRANSFER || it.type == TransactionType.CC_SETTLEMENT }

        assertEquals(2, calendarTransferTxns.size)
        val calendarTransferTotalMinor = calendarTransferTxns.sumOf { it.amount.amountMinor }
        assertEquals(800000L, calendarTransferTotalMinor)
    }

    @Test
    fun testCalendarDayCellTransferDisplayScenarios() {
        // Scenario 1: Only transfer present -> show transfer pill
        val cellOnlyTransfer = CalendarDayCell(
            epochDay = 20000L,
            dayNumber = 15,
            formattedCompactIncome = "",
            formattedCompactExpense = "",
            transferCount = 1,
            formattedCompactTransfer = "5k"
        )
        val showTransferPill1 = cellOnlyTransfer.transferCount > 0 && cellOnlyTransfer.formattedCompactTransfer.isNotEmpty() &&
            (cellOnlyTransfer.formattedCompactIncome.isEmpty() || cellOnlyTransfer.formattedCompactExpense.isEmpty())
        assertTrue("Only transfer should show transfer pill", showTransferPill1)
        val showHeaderBadge1 = cellOnlyTransfer.transferCount > 0 && cellOnlyTransfer.formattedCompactIncome.isNotEmpty() && cellOnlyTransfer.formattedCompactExpense.isNotEmpty()
        assertFalse("Only transfer should NOT show header indicator badge", showHeaderBadge1)

        // Scenario 2: Income + Transfer (Expense empty) -> show income and transfer pill
        val cellIncomeAndTransfer = CalendarDayCell(
            epochDay = 20000L,
            dayNumber = 16,
            formattedCompactIncome = "10k",
            formattedCompactExpense = "",
            transferCount = 1,
            formattedCompactTransfer = "5k"
        )
        val showTransferPill2 = cellIncomeAndTransfer.transferCount > 0 && cellIncomeAndTransfer.formattedCompactTransfer.isNotEmpty() &&
            (cellIncomeAndTransfer.formattedCompactIncome.isEmpty() || cellIncomeAndTransfer.formattedCompactExpense.isEmpty())
        assertTrue("Income + transfer should show transfer pill", showTransferPill2)
        val showHeaderBadge2 = cellIncomeAndTransfer.transferCount > 0 && cellIncomeAndTransfer.formattedCompactIncome.isNotEmpty() && cellIncomeAndTransfer.formattedCompactExpense.isNotEmpty()
        assertFalse("Income + transfer should not show header badge (transfer pill is visible)", showHeaderBadge2)

        // Scenario 3: Expense + Transfer (Income empty) -> show expense and transfer pill
        val cellExpenseAndTransfer = CalendarDayCell(
            epochDay = 20000L,
            dayNumber = 17,
            formattedCompactIncome = "",
            formattedCompactExpense = "2k",
            transferCount = 1,
            formattedCompactTransfer = "5k"
        )
        val showTransferPill3 = cellExpenseAndTransfer.transferCount > 0 && cellExpenseAndTransfer.formattedCompactTransfer.isNotEmpty() &&
            (cellExpenseAndTransfer.formattedCompactIncome.isEmpty() || cellExpenseAndTransfer.formattedCompactExpense.isEmpty())
        assertTrue("Expense + transfer should show transfer pill", showTransferPill3)

        // Scenario 4: Income + Expense + Transfer -> bottom column shows income & expense; header badge shows transfer arrow
        val cellAllThree = CalendarDayCell(
            epochDay = 20000L,
            dayNumber = 18,
            formattedCompactIncome = "10k",
            formattedCompactExpense = "2k",
            transferCount = 1,
            formattedCompactTransfer = "5k"
        )
        val showTransferPill4 = cellAllThree.transferCount > 0 && cellAllThree.formattedCompactTransfer.isNotEmpty() &&
            (cellAllThree.formattedCompactIncome.isEmpty() || cellAllThree.formattedCompactExpense.isEmpty())
        assertFalse("When both income and expense are present, bottom transfer pill is omitted to prevent overflow", showTransferPill4)
        val showHeaderBadge4 = cellAllThree.transferCount > 0 && cellAllThree.formattedCompactIncome.isNotEmpty() && cellAllThree.formattedCompactExpense.isNotEmpty()
        assertTrue("When both income and expense are present, transfer arrow badge is displayed in the day header", showHeaderBadge4)
    }
}

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
}

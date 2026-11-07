package com.folioledger.app.domain

import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.FinancialPeriodRange
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.SplitLineItem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.util.LedgerBucketingCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LedgerBucketingCalculatorTest {

    private val inrSpec = SupportedCurrencies.INR
    private val usdSpec = SupportedCurrencies.USD

    @Test
    fun testComputeIncomeAndExpenseTotals_OperatingVsCapitalEvents() {
        val txns = listOf(
            TransactionItem(
                id = 1L,
                type = TransactionType.INCOME,
                timestampEpochMillis = 1000L,
                ledgerDateEpochDay = 20000L,
                amount = MinorMoney(500000L), // ₹5,000.00
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Checking"
            ),
            TransactionItem(
                id = 2L,
                type = TransactionType.EXPENSE,
                timestampEpochMillis = 2000L,
                ledgerDateEpochDay = 20000L,
                amount = MinorMoney(150000L), // ₹1,500.00
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Checking"
            ),
            TransactionItem(
                id = 3L,
                type = TransactionType.REFUND,
                timestampEpochMillis = 3000L,
                ledgerDateEpochDay = 20000L,
                amount = MinorMoney(20000L), // ₹200.00 refund against expense
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Checking"
            ),
            TransactionItem(
                id = 4L,
                type = TransactionType.OPENING_BALANCE,
                timestampEpochMillis = 4000L,
                ledgerDateEpochDay = 20000L,
                amount = MinorMoney(10000000L), // ₹100,000 capital opening balance (ignored)
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Checking"
            ),
            TransactionItem(
                id = 5L,
                type = TransactionType.TRANSFER,
                timestampEpochMillis = 5000L,
                ledgerDateEpochDay = 20000L,
                amount = MinorMoney(500000L), // ₹5,000 transfer between accounts
                transferFee = MinorMoney(1000L), // ₹10 transfer fee (expense)
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Checking",
                transferToAccountId = 2L,
                transferToAccountName = "Savings"
            )
        )

        val (income, expense) = LedgerBucketingCalculator.computeIncomeAndExpenseTotals(
            transactions = txns,
            primaryCurrencySpec = inrSpec
        )

        // Income: 500,000 paise (₹5,000)
        assertEquals(500000L, income.amountMinor)
        // Expense: 150,000 - 20,000 (refund) + 1,000 (fee) = 131,000 paise (₹1,310)
        assertEquals(131000L, expense.amountMinor)
    }

    @Test
    fun testComputeIncomeAndExpenseTotals_MultiCurrencyConversion() {
        val secondaryCurrencies = listOf(
            SecondaryCurrency(code = "USD", exchangeRateToPrimary = 85.0)
        )
        val txns = listOf(
            TransactionItem(
                id = 1L,
                type = TransactionType.EXPENSE,
                timestampEpochMillis = 1000L,
                ledgerDateEpochDay = 20000L,
                amount = MinorMoney(1000L), // $10.00 USD -> ₹850.00 at 85.0
                currencyCode = "USD",
                accountId = 1L,
                accountName = "US Travel Card"
            )
        )

        val (income, expense) = LedgerBucketingCalculator.computeIncomeAndExpenseTotals(
            transactions = txns,
            scopedAccountId = null,
            primaryCurrencySpec = inrSpec,
            secondaryCurrencies = secondaryCurrencies
        )

        assertEquals(0L, income.amountMinor)
        assertEquals(85000L, expense.amountMinor) // 85,000 paise = ₹850.00
    }

    @Test
    fun testComputeIncomeAndExpenseTotals_ScopedAccountTransfers() {
        val transferIn = TransactionItem(
            id = 1L,
            type = TransactionType.TRANSFER,
            timestampEpochMillis = 1000L,
            ledgerDateEpochDay = 20000L,
            amount = MinorMoney(200000L), // ₹2,000
            currencyCode = "INR",
            accountId = 2L,
            accountName = "Bank B",
            transferToAccountId = 1L,
            transferToAccountName = "Target Bank A"
        )
        val transferOut = TransactionItem(
            id = 2L,
            type = TransactionType.TRANSFER,
            timestampEpochMillis = 2000L,
            ledgerDateEpochDay = 20000L,
            amount = MinorMoney(50000L), // ₹500
            transferFee = MinorMoney(500L), // ₹5 fee
            currencyCode = "INR",
            accountId = 1L,
            accountName = "Target Bank A",
            transferToAccountId = 2L,
            transferToAccountName = "Bank B"
        )

        // When viewing account 1 (Target Bank A)
        val (scopedIncome, scopedExpense) = LedgerBucketingCalculator.computeIncomeAndExpenseTotals(
            transactions = listOf(transferIn, transferOut),
            scopedAccountId = 1L,
            primaryCurrencySpec = inrSpec
        )

        // Transfer In counts as income to this account
        assertEquals(200000L, scopedIncome.amountMinor)
        // Transfer Out + fee counts as expense from this account (50,000 + 500 = 50,500)
        assertEquals(50500L, scopedExpense.amountMinor)
    }

    @Test
    fun testComputeTransferTotal() {
        val txns = listOf(
            TransactionItem(
                id = 1L,
                type = TransactionType.TRANSFER,
                timestampEpochMillis = 1000L,
                ledgerDateEpochDay = 20000L,
                amount = MinorMoney(400000L), // ₹4,000
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Bank A",
                transferToAccountId = 2L,
                transferToAccountName = "Bank B"
            ),
            TransactionItem(
                id = 2L,
                type = TransactionType.CC_SETTLEMENT,
                timestampEpochMillis = 2000L,
                ledgerDateEpochDay = 20000L,
                amount = MinorMoney(100000L), // ₹1,000 CC payment
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Bank A",
                transferToAccountId = 3L,
                transferToAccountName = "Credit Card"
            ),
            TransactionItem(
                id = 3L,
                type = TransactionType.EXPENSE,
                timestampEpochMillis = 3000L,
                ledgerDateEpochDay = 20000L,
                amount = MinorMoney(50000L),
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Bank A"
            )
        )

        val transferTotal = LedgerBucketingCalculator.computeTransferTotal(
            transactions = txns,
            primaryCurrencySpec = inrSpec
        )

        // Both TRANSFER (4,000) and CC_SETTLEMENT (1,000) should be included = 5,000 (500,000 paise)
        assertEquals(500000L, transferTotal.amountMinor)
    }

    @Test
    fun testBuildDailyGroups() {
        val day1 = 20000L // e.g. 2024-10-04
        val day2 = 19999L // e.g. 2024-10-03
        val txns = listOf(
            TransactionItem(
                id = 1L,
                type = TransactionType.EXPENSE,
                timestampEpochMillis = 1000L,
                ledgerDateEpochDay = day1,
                amount = MinorMoney(10000L),
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Bank"
            ),
            TransactionItem(
                id = 2L,
                type = TransactionType.INCOME,
                timestampEpochMillis = 2000L,
                ledgerDateEpochDay = day1,
                amount = MinorMoney(50000L),
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Bank"
            ),
            TransactionItem(
                id = 3L,
                type = TransactionType.TRANSFER,
                timestampEpochMillis = 3000L,
                ledgerDateEpochDay = day2,
                amount = MinorMoney(25000L),
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Bank",
                transferToAccountId = 2L,
                transferToAccountName = "Savings"
            )
        )

        val groups = LedgerBucketingCalculator.buildDailyGroups(
            transactions = txns,
            todayEpochDay = day1,
            primaryCurrencySpec = inrSpec
        )

        assertEquals(2, groups.size)
        // Group 1: day1
        assertEquals(day1, groups[0].epochDay)
        assertTrue(groups[0].isToday)
        assertEquals(50000L, groups[0].dailyIncome.amountMinor)
        assertEquals(10000L, groups[0].dailyExpense.amountMinor)
        assertEquals(40000L, groups[0].dailyNet.amountMinor)
        assertEquals(2, groups[0].transactions.size)

        // Group 2: day2
        assertEquals(day2, groups[1].epochDay)
        assertFalse(groups[1].isToday)
        assertEquals(1, groups[1].transferCount)
        assertEquals(25000L, groups[1].dailyTransferTotal.amountMinor)
    }

    @Test
    fun testBuildWeekdayHeaders() {
        // weekStartDayIso = 1 (Monday)
        val monHeaders = LedgerBucketingCalculator.buildWeekdayHeaders(1)
        assertEquals(7, monHeaders.size)
        assertEquals("Mon", monHeaders[0])
        assertEquals("Sun", monHeaders[6])

        // weekStartDayIso = 7 (Sunday)
        val sunHeaders = LedgerBucketingCalculator.buildWeekdayHeaders(7)
        assertEquals("Sun", sunHeaders[0])
        assertEquals("Sat", sunHeaders[6])
    }

    @Test
    fun testBuildCalendarGridCells() {
        val periodRange = FinancialPeriodRange(
            startEpochDay = 20000L,
            endEpochDay = 20030L, // 31 days
            title = "Oct 2024",
            subtitle = "01 Oct – 31 Oct",
            anchorYear = 2024,
            anchorMonthValue = 10
        )

        val cells = LedgerBucketingCalculator.buildCalendarGridCells(
            periodRange = periodRange,
            weekStartDayIso = 1, // Monday
            transactions = emptyList(),
            todayEpochDay = 20015L,
            primaryCurrencySpec = inrSpec,
            numberGrouping = NumberGroupingSystem.INDIAN
        )

        // Grid must be a multiple of 7
        assertEquals(0, cells.size % 7)
        // The active period days must have epochDay set
        val activeCells = cells.filter { it.isInActivePeriod }
        assertEquals(31, activeCells.size)

        // Verify today cell
        val todayCell = cells.firstOrNull { it.isToday }
        assertNotNull(todayCell)
        assertEquals(20015L, todayCell?.epochDay)
    }

    @Test
    fun testBuildAccountPeriodFlows() {
        val accounts = listOf(
            AccountSummary(
                id = 1L,
                name = "Checking",
                groupType = AccountGroup.BANK_ACCOUNT,
                currencyCode = "INR",
                currentBalance = MinorMoney(1000000L)
            ),
            AccountSummary(
                id = 2L,
                name = "Savings",
                groupType = AccountGroup.BANK_ACCOUNT,
                currencyCode = "INR",
                currentBalance = MinorMoney(5000000L)
            )
        )
        val txns = listOf(
            TransactionItem(
                id = 1L,
                type = TransactionType.INCOME,
                timestampEpochMillis = 1000L,
                ledgerDateEpochDay = 20000L,
                amount = MinorMoney(100000L), // ₹1,000 into Checking
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Checking"
            ),
            TransactionItem(
                id = 2L,
                type = TransactionType.EXPENSE,
                timestampEpochMillis = 2000L,
                ledgerDateEpochDay = 20000L,
                amount = MinorMoney(30000L), // ₹300 out of Checking
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Checking"
            )
        )

        val flows = LedgerBucketingCalculator.buildAccountPeriodFlows(txns, accounts)
        assertEquals(1, flows.size)
        val checkingFlow = flows[0]
        assertEquals(1L, checkingFlow.accountId)
        assertEquals("Checking", checkingFlow.accountName)
        assertEquals(100000L, checkingFlow.incomeTotal.amountMinor)
        assertEquals(30000L, checkingFlow.expenseTotal.amountMinor)
        assertEquals(70000L, checkingFlow.netChange.amountMinor)
        assertEquals(2, checkingFlow.transactionCount)
    }

    @Test
    fun testBuildScopedCategorySpendStats() {
        val categories = listOf(
            CategoryNode(
                id = 1L,
                name = "Food",
                type = CategoryType.EXPENSE,
                parentId = null,
                iconName = "restaurant",
                colorHex = "#FF5722"
            ),
            CategoryNode(
                id = 2L,
                name = "Transport",
                type = CategoryType.EXPENSE,
                parentId = null,
                iconName = "directions_car",
                colorHex = "#2196F3"
            )
        )

        val txns = listOf(
            TransactionItem(
                id = 1L,
                type = TransactionType.EXPENSE,
                timestampEpochMillis = 1000L,
                ledgerDateEpochDay = 20000L,
                amount = MinorMoney(30000L), // ₹300 Food
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Checking",
                categoryId = 1L
            ),
            TransactionItem(
                id = 2L,
                type = TransactionType.EXPENSE,
                timestampEpochMillis = 2000L,
                ledgerDateEpochDay = 20000L,
                amount = MinorMoney(70000L), // ₹700 Transport
                currencyCode = "INR",
                accountId = 1L,
                accountName = "Checking",
                categoryId = 2L
            )
        )

        val stats = LedgerBucketingCalculator.buildScopedCategorySpendStats(txns, categories, maxCategories = 6)
        assertEquals(2, stats.size)
        // Transport was ₹700 (70%), Food was ₹300 (30%). Sorted descending:
        assertEquals("Transport", stats[0].categoryName)
        assertEquals(70000L, stats[0].netAmount.amountMinor)
        assertEquals(70f, stats[0].percentageOfTotal, 0.01f)

        assertEquals("Food", stats[1].categoryName)
        assertEquals(30000L, stats[1].netAmount.amountMinor)
        assertEquals(30f, stats[1].percentageOfTotal, 0.01f)
    }

    @Test
    fun testFinancialPeriodRangeStepping() {
        val startEpoch = LocalDate.of(2024, 1, 15).toEpochDay()
        val nextMonthEpoch = LedgerBucketingCalculator.stepFinancialMonth(startEpoch, 1, 1L)
        val nextDate = LocalDate.ofEpochDay(nextMonthEpoch)
        assertEquals(2, nextDate.monthValue)
        assertEquals(2024, nextDate.year)

        val yearRanges = LedgerBucketingCalculator.computeFinancialYearMonthRanges(2024, 1)
        assertEquals(12, yearRanges.size)
        assertEquals(1, yearRanges[0].anchorMonthValue)
        assertEquals(12, yearRanges[11].anchorMonthValue)
    }
}

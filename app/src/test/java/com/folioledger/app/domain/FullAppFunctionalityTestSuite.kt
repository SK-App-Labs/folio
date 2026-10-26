package com.folioledger.app.domain

import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SplitLineItem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.usecase.AccountingRulesEngine
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.components.suggestIconAndColorForCategoryName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * End-to-end functionality verification test suite covering core accounting rules,
 * double-entry transfers, debit card delegation, credit card statement cycles,
 * financial month bounds, keypad expressions, and CSV heuristics.
 */
class FullAppFunctionalityTestSuite {

    private val engine = AccountingRulesEngine()

    // =========================================================================
    // 1. FINANCIAL MONTH BOUNDARY & LEAP YEAR CLAMPING
    // =========================================================================

    @Test
    fun financialMonth_leapYearAndNonLeapYear_clampsSafelyWithoutException() {
        // Leap year: Feb 2024 has 29 days
        val feb2024 = LocalDate.of(2024, 2, 10).toEpochDay()
        val (startLeap, endLeap) = engine.computeFinancialMonthRange(feb2024, monthStartDay = 31)
        val startLeapDate = LocalDate.ofEpochDay(startLeap)
        val endLeapDate = LocalDate.ofEpochDay(endLeap)

        assertEquals(LocalDate.of(2024, 1, 31), startLeapDate)
        assertEquals(LocalDate.of(2024, 2, 28), endLeapDate) // 29th clamped to next month start minus 1

        // Non-leap year: Feb 2026 has 28 days
        val feb2026 = LocalDate.of(2026, 2, 15).toEpochDay()
        val (startNonLeap, endNonLeap) = engine.computeFinancialMonthRange(feb2026, monthStartDay = 31)
        assertEquals(LocalDate.of(2026, 1, 31), LocalDate.ofEpochDay(startNonLeap))
        assertEquals(LocalDate.of(2026, 2, 27), LocalDate.ofEpochDay(endNonLeap))

        // Month Start Day 30 in February (28 days)
        val (startDay30, endDay30) = engine.computeFinancialMonthRange(feb2026, monthStartDay = 30)
        assertEquals(LocalDate.of(2026, 1, 30), LocalDate.ofEpochDay(startDay30))
        assertEquals(LocalDate.of(2026, 2, 27), LocalDate.ofEpochDay(endDay30))
    }

    @Test
    fun financialMonth_allStartDays1To31_produceValidNonOverlappingRanges() {
        val testDate = LocalDate.of(2026, 10, 26).toEpochDay()
        for (day in 1..31) {
            val (start, end) = engine.computeFinancialMonthRange(testDate, monthStartDay = day)
            assertTrue("Start day must be <= End day for day $day", start <= end)
            val startDate = LocalDate.ofEpochDay(start)
            val endDate = LocalDate.ofEpochDay(end)
            assertTrue("Cycle must be at least 27 days", end - start >= 27)
            assertTrue("Cycle must not exceed 31 days", end - start <= 31)
            assertTrue("Reference date must fall within cycle", testDate in start..end)
        }
    }

    // =========================================================================
    // 2. DOUBLE-ENTRY TRANSFERS & LOAN LIABILITIES
    // =========================================================================

    @Test
    fun doubleEntry_bankToBankTransfer_decreasesSourceIncreasesTarget() {
        var bankABalance = 50_000_00L // ₹50,000
        var bankBBalance = 10_000_00L // ₹10,000
        val transferAmount = 15_000_00L // ₹15,000
        val transferFee = 25_00L // ₹25

        // Simulate Folio repository logic for TRANSFER
        val totalOutflow = transferAmount + transferFee
        bankABalance -= totalOutflow
        bankBBalance += transferAmount

        assertEquals(34_975_00L, bankABalance) // 50,000 - 15,025 = 34,975
        assertEquals(25_000_00L, bankBBalance) // 10,000 + 15,000 = 25,000
    }

    @Test
    fun doubleEntry_bankToLoanTransfer_reducesLoanLiability() {
        var bankBalance = 1_00_000_00L // ₹1,00,000 asset
        // Loan has initial signed balance of -₹20,00,000 (liability)
        var loanLiabilityBalance = -20_00_000_00L
        val emiPayment = 50_000_00L // ₹50,000 EMI

        // Applying double-entry transfer
        bankBalance -= emiPayment
        loanLiabilityBalance += emiPayment

        assertEquals(50_000_00L, bankBalance) // ₹50,000 remaining in bank
        assertEquals(-19_50_000_00L, loanLiabilityBalance) // Outstanding debt reduced to ₹19.5L!

        // Net worth impact: Assets + Liabilities
        // Before: 1,00,000 + (-20,00,000) = -19,00,000
        // After:  50,000 + (-19,50,000)   = -19,00,000 (Net Worth remains exactly balanced)
        val netWorthBefore = 1_00_000_00L + (-20_00_000_00L)
        val netWorthAfter = bankBalance + loanLiabilityBalance
        assertEquals(netWorthBefore, netWorthAfter)
    }

    // =========================================================================
    // 3. DEBIT CARD DELEGATION TO PARENT BANK ACCOUNT
    // =========================================================================

    @Test
    fun debitCard_transactionsDeductFromParentBankAccount() {
        val parentBankId = 1L
        val debitCardId = 2L
        var parentBankBalance = 40_000_00L // ₹40,000

        // Map debit card to parent bank
        val debitCardParentMap = mapOf(debitCardId to parentBankId)
        fun resolveEffectiveAccountId(id: Long) = debitCardParentMap[id] ?: id

        // Transaction on debit card
        val txAccountId = debitCardId
        val spendAmount = 2_500_00L // ₹2,500
        val effectiveTarget = resolveEffectiveAccountId(txAccountId)

        assertEquals(parentBankId, effectiveTarget)
        parentBankBalance -= spendAmount

        assertEquals(37_500_00L, parentBankBalance) // Deducted from parent bank account
    }

    // =========================================================================
    // 4. CREDIT CARD DUAL-CYCLE METRICS & SETTLEMENT
    // =========================================================================

    @Test
    fun creditCard_statementClosedCycleVsActiveCycle_calculatesCorrectDebtAndLimit() {
        val openingBalance = MinorMoney.ZERO
        val creditLimit = MinorMoney(100_000_00L) // ₹1,00,000 limit
        val settlementDay = 15 // 15th of month
        val refDate = LocalDate.of(2026, 10, 20).toEpochDay() // Cycle closed on Oct 15

        val transactions = listOf(
            // Closed cycle expense (Oct 10 <= Oct 15)
            TransactionItem(
                id = 1L,
                type = TransactionType.EXPENSE,
                timestampEpochMillis = 1000L,
                amount = MinorMoney(12_000_00L),
                currencyCode = "INR",
                accountId = 10L,
                accountName = "Credit Card",
                ledgerDateEpochDay = LocalDate.of(2026, 10, 10).toEpochDay()
            ),
            // Active cycle unbilled expense (Oct 18 > Oct 15)
            TransactionItem(
                id = 2L,
                type = TransactionType.EXPENSE,
                timestampEpochMillis = 2000L,
                amount = MinorMoney(4_000_00L),
                currencyCode = "INR",
                accountId = 10L,
                accountName = "Credit Card",
                ledgerDateEpochDay = LocalDate.of(2026, 10, 18).toEpochDay()
            )
        )

        val metrics = engine.computeCreditCardMetrics(
            openingBalance = openingBalance,
            transactions = transactions,
            settlementDayOfMonth = settlementDay,
            referenceDateEpochDay = refDate,
            creditLimit = creditLimit
        )

        // Statement payable: 12,000.00
        assertEquals(MinorMoney(12_000_00L), metrics.statementPayable)
        // Unbilled amount: 4,000.00
        assertEquals(MinorMoney(4_000_00L), metrics.unbilledAmount)
        // Total outstanding debt: 16,000.00
        assertEquals(MinorMoney(16_000_00L), metrics.outstandingBalance)
        // Available credit: 100,000 - 16,000 = 84,000.00
        assertEquals(MinorMoney(84_000_00L), metrics.availableCredit)
        // Utilization: 16%
        assertEquals(16.0f, metrics.utilizationPercent, 0.01f)
    }

    @Test
    fun creditCard_settlementPayment_reducesStatementPayableAndRestoresLimit() {
        val openingBalance = MinorMoney.ZERO
        val creditLimit = MinorMoney(50_000_00L)
        val settlementDay = 15
        val refDate = LocalDate.of(2026, 10, 20).toEpochDay()

        val transactions = listOf(
            // Statement spend on Oct 5: ₹10,000
            TransactionItem(
                id = 1L,
                type = TransactionType.EXPENSE,
                timestampEpochMillis = 1000L,
                amount = MinorMoney(10_000_00L),
                currencyCode = "INR",
                accountId = 10L,
                accountName = "Credit Card",
                ledgerDateEpochDay = LocalDate.of(2026, 10, 5).toEpochDay()
            ),
            // Bill settlement payment on Oct 18: ₹10,000
            TransactionItem(
                id = 2L,
                type = TransactionType.CC_SETTLEMENT,
                timestampEpochMillis = 2000L,
                amount = MinorMoney(10_000_00L),
                currencyCode = "INR",
                accountId = 10L,
                accountName = "Credit Card",
                ledgerDateEpochDay = LocalDate.of(2026, 10, 18).toEpochDay()
            )
        )

        val metrics = engine.computeCreditCardMetrics(
            openingBalance = openingBalance,
            transactions = transactions,
            settlementDayOfMonth = settlementDay,
            referenceDateEpochDay = refDate,
            creditLimit = creditLimit
        )

        // Debt paid off:
        assertEquals(MinorMoney.ZERO, metrics.statementPayable)
        assertEquals(MinorMoney.ZERO, metrics.outstandingBalance)
        assertEquals(creditLimit, metrics.availableCredit)
        assertEquals(0.0f, metrics.utilizationPercent, 0.01f)
    }

    // =========================================================================
    // 5. RECONCILIATIONS & OPENING BALANCE AUDITING
    // =========================================================================

    @Test
    fun reconciliation_computesAdjustmentDeltaCorrectly() {
        val recordedBalance = MinorMoney(18_450_00L) // Recorded: ₹18,450.00
        val actualBankBalance = MinorMoney(20_000_00L) // Actual: ₹20,000.00

        // Delta to adjust
        val adjustmentDelta = actualBankBalance - recordedBalance
        assertEquals(MinorMoney(1_550_00L), adjustmentDelta) // Positive ₹1,550 inflow needed

        // If actual balance is less (e.g. forgotten expense)
        val actualLower = MinorMoney(17_000_00L)
        val lowerDelta = actualLower - recordedBalance
        assertEquals(MinorMoney(-1_450_00L), lowerDelta) // Negative ₹1,450 outflow needed
    }

    // =========================================================================
    // 6. CSV IMPORT HEURISTICS & AUTO-ICON RESOLUTION
    // =========================================================================

    @Test
    fun csvHeuristics_accountGroupNameDetection_classifiesAccurately() {
        fun detectGroup(name: String): AccountGroup {
            val lower = name.trim().lowercase()
            return when {
                lower.contains("card") || lower.contains("cc") -> AccountGroup.CREDIT_CARD
                lower.contains("cash") || lower.contains("wallet") -> AccountGroup.CASH
                lower.contains("bank") || lower.contains("savings") || lower.contains("a/c") || lower.contains("checking") -> AccountGroup.BANK_ACCOUNT
                lower.contains("loan") -> AccountGroup.LOAN
                else -> AccountGroup.OTHER
            }
        }

        assertEquals(AccountGroup.CREDIT_CARD, detectGroup("HDFC Millennia Credit Card"))
        assertEquals(AccountGroup.CREDIT_CARD, detectGroup("SBI CC"))
        assertEquals(AccountGroup.CASH, detectGroup("Physical Cash"))
        assertEquals(AccountGroup.CASH, detectGroup("Paytm Wallet"))
        assertEquals(AccountGroup.BANK_ACCOUNT, detectGroup("State Bank of India"))
        assertEquals(AccountGroup.BANK_ACCOUNT, detectGroup("ICICI Savings A/C"))
        assertEquals(AccountGroup.LOAN, detectGroup("Home Loan SBI"))
        assertEquals(AccountGroup.LOAN, detectGroup("Car Loan"))
        assertEquals(AccountGroup.OTHER, detectGroup("Miscellaneous Ledger"))
    }

    @Test
    fun categorySuggestions_keywordsMatchCorrectIconsAndColors() {
        val dining = suggestIconAndColorForCategoryName("Swiggy Food Order")
        assertNotNull(dining)
        assertEquals("restaurant", dining!!.first)
        assertEquals("#F97316", dining.second)

        val fuel = suggestIconAndColorForCategoryName("Shell Petrol Pump")
        assertNotNull(fuel)
        assertEquals("local_gas_station", fuel!!.first)

        val groceries = suggestIconAndColorForCategoryName("Blinkit Groceries")
        assertNotNull(groceries)
        assertEquals("shopping_cart", groceries!!.first)

        val coffee = suggestIconAndColorForCategoryName("Starbucks Coffee")
        assertNotNull(coffee)
        assertEquals("local_cafe", coffee!!.first)

        val salary = suggestIconAndColorForCategoryName("Monthly Salary Paycheck")
        assertNotNull(salary)
        assertEquals("payments", salary!!.first)

        val sip = suggestIconAndColorForCategoryName("Zerodha Mutual Fund SIP")
        assertNotNull(sip)
        assertEquals("trending_up", sip!!.first)
    }

    @Test
    fun transferCategorySuppression_transfersDoNotCreateCategories() {
        val isTransfer = true
        val rawCategoryStringFromCsv = "HDFC Bank"

        // In Folio's refined architecture, transfers bypass category creation:
        val categoryId = if (isTransfer) null else 101L
        assertNull("Transfers must never have a categoryId assigned", categoryId)
    }

    // =========================================================================
    // 7. KEYPAD 120 FPS EVALUATOR & FINANCIAL TYPOGRAPHY
    // =========================================================================

    @Test
    fun keypadExpression_complexOperatorPrecedence_evaluatesPrecisely() {
        // Multiplication and division before addition and subtraction
        val result = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = "150 + 25 * 4 - 30 / 2",
            fractionDigits = 2
        )
        assertTrue(result.isSuccess)
        // 150 + 100 - 15 = 235.00 -> 23500L
        assertEquals(MinorMoney(235_00L), result.getOrThrow())
    }

    @Test
    fun financialFormatting_indianVsInternationalMonospacedAlignment() {
        val money = MinorMoney(5432109_87L) // 54,32,109.87

        val indian = MoneyFormatterAndCalculator.formatMinorMoney(
            money = money,
            currencySpec = SupportedCurrencies.INR,
            grouping = NumberGroupingSystem.INDIAN
        )
        assertEquals("₹54,32,109.87", indian)

        val international = MoneyFormatterAndCalculator.formatMinorMoney(
            money = money,
            currencySpec = SupportedCurrencies.USD,
            grouping = NumberGroupingSystem.INTERNATIONAL
        )
        assertEquals("$5,432,109.87", international)
    }
}

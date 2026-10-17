// File: app/src/test/java/com/folioledger/app/domain/AccountingRulesEngineTest.kt
package com.folioledger.app.domain

import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SplitLineItem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.usecase.AccountingRulesEngine
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AccountingRulesEngineTest {

    private val engine = AccountingRulesEngine()

    @Test
    fun minorMoney_basicArithmeticAndComparisons_areExact() {
        val a = MinorMoney(125_50L)
        val b = MinorMoney(74_50L)

        val sum = a + b
        val diff = a - b
        val negated = -a

        assertEquals(MinorMoney(200_00L), sum)
        assertEquals(MinorMoney(51_00L), diff)
        assertEquals(MinorMoney(-125_50L), negated)
        assertTrue(a > b)
        assertTrue(sum.isPositive)
        assertTrue(negated.isNegative)
        assertTrue(MinorMoney.ZERO.isZero)
    }

    @Test(expected = ArithmeticException::class)
    fun minorMoney_additionOverflow_throwsArithmeticException() {
        val max = MinorMoney(Long.MAX_VALUE)
        val one = MinorMoney(1L)
        val unused = max + one
        assertEquals(0L, unused.amountMinor)
    }

    @Test(expected = ArithmeticException::class)
    fun minorMoney_subtractionOverflow_throwsArithmeticException() {
        val min = MinorMoney(Long.MIN_VALUE)
        val one = MinorMoney(1L)
        val unused = min - one
        assertEquals(0L, unused.amountMinor)
    }

    @Test
    fun formatMinorMoney_indianAndInternationalGrouping_formatsCorrectly() {
        val sample = MinorMoney(1234567_89L) // 12,34,567.89
        val oneLakh = MinorMoney(100000_00L)  // 1,00,000.00
        val negative = MinorMoney(-500_50L)   // -500.50

        val formattedIndian = MoneyFormatterAndCalculator.formatMinorMoney(
            money = sample,
            currencySpec = SupportedCurrencies.INR,
            grouping = NumberGroupingSystem.INDIAN
        )
        assertEquals("₹12,34,567.89", formattedIndian)

        val formattedOneLakh = MoneyFormatterAndCalculator.formatMinorMoney(
            money = oneLakh,
            currencySpec = SupportedCurrencies.INR,
            grouping = NumberGroupingSystem.INDIAN
        )
        assertEquals("₹1,00,000.00", formattedOneLakh)

        val formattedNegativeInr = MoneyFormatterAndCalculator.formatMinorMoney(
            money = negative,
            currencySpec = SupportedCurrencies.INR,
            grouping = NumberGroupingSystem.INDIAN
        )
        assertEquals("-₹500.50", formattedNegativeInr)

        val formattedInternationalUsd = MoneyFormatterAndCalculator.formatMinorMoney(
            money = sample,
            currencySpec = SupportedCurrencies.USD,
            grouping = NumberGroupingSystem.INTERNATIONAL
        )
        assertEquals("$1,234,567.89", formattedInternationalUsd)

        val formattedNoSymbolIndian = MoneyFormatterAndCalculator.formatMinorMoney(
            money = sample,
            currencySpec = SupportedCurrencies.INR,
            grouping = NumberGroupingSystem.INDIAN,
            includeSymbol = false
        )
        assertEquals("12,34,567.89", formattedNoSymbolIndian)

        val formattedNoSymbolInternational = MoneyFormatterAndCalculator.formatMinorMoney(
            money = sample,
            currencySpec = SupportedCurrencies.INR,
            grouping = NumberGroupingSystem.INTERNATIONAL,
            includeSymbol = false
        )
        assertEquals("1,234,567.89", formattedNoSymbolInternational)
    }

    @Test
    fun formatMinorMoney_zeroTwoThreeDecimalCurrencies_respectsFractionDigits() {
        // JPY: 0 fraction digits
        val jpyFormatted = MoneyFormatterAndCalculator.formatMinorMoney(
            money = MinorMoney(1234567L),
            currencySpec = SupportedCurrencies.JPY
        )
        assertEquals("¥1,234,567", jpyFormatted)

        // INR: 2 fraction digits
        val inrFormatted = MoneyFormatterAndCalculator.formatMinorMoney(
            money = MinorMoney(2500_05L),
            currencySpec = SupportedCurrencies.INR
        )
        assertEquals("₹2,500.05", inrFormatted)

        // KWD: 3 fraction digits
        val kwdFormatted = MoneyFormatterAndCalculator.formatMinorMoney(
            money = MinorMoney(1234_567L),
            currencySpec = SupportedCurrencies.KWD
        )
        assertEquals("KD1,234.567", kwdFormatted)
    }

    @Test
    fun evaluateKeypadExpressionToMinor_precedenceAndRounding_evaluatesCorrectly() {
        // 250 + 49.95 * 2 = 250 + 99.90 = 349.90 -> 34990L minor units
        val result1 = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = "250+49.95*2",
            fractionDigits = 2
        )
        assertTrue(result1.isSuccess)
        assertEquals(MinorMoney(349_90L), result1.getOrThrow())

        // 120 + 45.50 * 2 - 10 = 120 + 91.00 - 10 = 201.00 -> 20100L minor units
        val result2 = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = "120 + 45.50 * 2 - 10",
            fractionDigits = 2
        )
        assertTrue(result2.isSuccess)
        assertEquals(MinorMoney(201_00L), result2.getOrThrow())

        // Division with HALF_UP rounding: 100 / 3 = 33.3333... -> 33.33 -> 3333L
        val result3 = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = "100 / 3",
            fractionDigits = 2
        )
        assertTrue(result3.isSuccess)
        assertEquals(MinorMoney(33_33L), result3.getOrThrow())
    }

    @Test
    fun evaluateKeypadExpressionToMinor_rejectsZeroNegativeAndDivideByZero() {
        val zeroResult = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = "50 - 50",
            fractionDigits = 2
        )
        assertTrue(zeroResult.isFailure)

        val negativeResult = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = "25 - 100",
            fractionDigits = 2
        )
        assertTrue(negativeResult.isFailure)

        val divByZeroResult = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = "100 / 0",
            fractionDigits = 2
        )
        assertTrue(divByZeroResult.isFailure)
    }

    @Test
    fun validateSplitTransaction_enforcesExactSumAndMinimumTwoSplits() {
        val total = MinorMoney(1000_00L)
        val validSplits = listOf(
            SplitLineItem(categoryId = 1L, categoryName = "Groceries", amount = MinorMoney(650_00L)),
            SplitLineItem(categoryId = 2L, categoryName = "Household", amount = MinorMoney(350_00L))
        )
        val validResult = engine.validateSplitTransaction(total, validSplits)
        assertTrue(validResult.isSuccess)

        val mismatchedSplits = listOf(
            SplitLineItem(categoryId = 1L, categoryName = "Groceries", amount = MinorMoney(600_00L)),
            SplitLineItem(categoryId = 2L, categoryName = "Household", amount = MinorMoney(350_00L))
        )
        val mismatchResult = engine.validateSplitTransaction(total, mismatchedSplits)
        assertFalse(mismatchResult.isSuccess)

        val singleSplit = listOf(
            SplitLineItem(categoryId = 1L, categoryName = "Groceries", amount = MinorMoney(1000_00L))
        )
        val singleSplitResult = engine.validateSplitTransaction(total, singleSplit)
        assertFalse(singleSplitResult.isSuccess)
    }

    @Test
    fun generateInstallmentSchedule_placesRemainderOnFirstInstallment() {
        val total = MinorMoney(100000L) // ₹1,000.00 in paise
        val startDate = LocalDate.of(2026, 10, 15)

        val schedule = engine.generateInstallmentSchedule(
            totalAmount = total,
            monthsCount = 3,
            startDateEpochDay = startDate.toEpochDay()
        )

        assertEquals(3, schedule.size)
        assertEquals(MinorMoney(33334L), schedule[0].second)
        assertEquals(MinorMoney(33333L), schedule[1].second)
        assertEquals(MinorMoney(33333L), schedule[2].second)

        assertEquals(LocalDate.of(2026, 10, 15).toEpochDay(), schedule[0].first)
        assertEquals(LocalDate.of(2026, 11, 15).toEpochDay(), schedule[1].first)
        assertEquals(LocalDate.of(2026, 12, 15).toEpochDay(), schedule[2].first)

        val sum = schedule.fold(MinorMoney.ZERO) { acc, pair -> acc + pair.second }
        assertEquals(total, sum)
    }

    @Test
    fun computeFinancialMonthRange_customMonthStartDay25_computesExactBoundaries() {
        // Case 1: Reference date is 2026-10-17 (before the 25th) -> 2026-09-25 .. 2026-10-24
        val refBefore25 = LocalDate.of(2026, 10, 17).toEpochDay()
        val (start1, end1) = engine.computeFinancialMonthRange(
            referenceDateEpochDay = refBefore25,
            monthStartDay = 25
        )
        assertEquals(LocalDate.of(2026, 9, 25).toEpochDay(), start1)
        assertEquals(LocalDate.of(2026, 10, 24).toEpochDay(), end1)

        // Case 2: Reference date is 2026-10-27 (on/after the 25th) -> 2026-10-25 .. 2026-11-24
        val refAfter25 = LocalDate.of(2026, 10, 27).toEpochDay()
        val (start2, end2) = engine.computeFinancialMonthRange(
            referenceDateEpochDay = refAfter25,
            monthStartDay = 25
        )
        assertEquals(LocalDate.of(2026, 10, 25).toEpochDay(), start2)
        assertEquals(LocalDate.of(2026, 11, 24).toEpochDay(), end2)
    }

    @Test
    fun computeCreditCardMetrics_tracksStatementPayableUnbilledAndSettlementWithoutDoubleCounting() {
        // Statement closes on the 15th of each month.
        // Reference date: 2026-10-22 -> most recent statement closed on 2026-10-15.
        val oct10 = LocalDate.of(2026, 10, 10).toEpochDay()
        val oct15 = LocalDate.of(2026, 10, 15).toEpochDay()
        val oct18 = LocalDate.of(2026, 10, 18).toEpochDay()
        val oct20 = LocalDate.of(2026, 10, 20).toEpochDay()
        val oct22 = LocalDate.of(2026, 10, 22).toEpochDay()

        val transactions = listOf(
            // Billed in closed statement cycle (on or before Oct 15): ₹8,000 + ₹2,000 = ₹10,000
            createSampleTx(
                id = 1L,
                type = TransactionType.EXPENSE,
                epochDay = oct10,
                amount = MinorMoney(8000_00L)
            ),
            createSampleTx(
                id = 2L,
                type = TransactionType.EXPENSE,
                epochDay = oct15,
                amount = MinorMoney(2000_00L)
            ),
            // Active unbilled spend after Oct 15: ₹3,500
            createSampleTx(
                id = 3L,
                type = TransactionType.EXPENSE,
                epochDay = oct18,
                amount = MinorMoney(3500_00L)
            ),
            // Partial bill settlement on Oct 20: ₹6,000 (plus duplicate ID to verify deduplication)
            createSampleTx(
                id = 4L,
                type = TransactionType.CC_SETTLEMENT,
                epochDay = oct20,
                amount = MinorMoney(6000_00L)
            ),
            createSampleTx(
                id = 4L,
                type = TransactionType.CC_SETTLEMENT,
                epochDay = oct20,
                amount = MinorMoney(6000_00L)
            ),
            // Refund received on Oct 20: ₹500
            createSampleTx(
                id = 5L,
                type = TransactionType.REFUND,
                epochDay = oct20,
                amount = MinorMoney(500_00L)
            )
        )

        val metrics = engine.computeCreditCardMetrics(
            openingBalance = MinorMoney.ZERO,
            transactions = transactions,
            settlementDayOfMonth = 15,
            referenceDateEpochDay = oct22,
            creditLimit = MinorMoney(50000_00L)
        )

        // Closed cycle debt = ₹10,000. Post-cutoff credits = ₹6,000 (settlement) + ₹500 (refund) = ₹6,500.
        // Remaining statementPayable = ₹10,000 - ₹6,500 = ₹3,500.
        // Unbilled spend in active cycle = ₹3,500.
        // Total outstandingBalance = ₹3,500 + ₹3,500 = ₹7,000 (signed currentBalance = -₹7,000).
        // Utilization = 7,000 / 50,000 = 14.00%.
        assertEquals(MinorMoney(3500_00L), metrics.statementPayable)
        assertEquals(MinorMoney(3500_00L), metrics.unbilledAmount)
        assertEquals(MinorMoney(7000_00L), metrics.outstandingBalance)
        assertEquals(MinorMoney(-7000_00L), metrics.currentBalance)
        assertEquals(43000_00L, metrics.availableCredit?.amountMinor)
        assertEquals(14.00f, metrics.utilizationPercent, 0.001f)
    }

    private fun createSampleTx(
        id: Long,
        type: TransactionType,
        epochDay: Long,
        amount: MinorMoney
    ): TransactionItem = TransactionItem(
        id = id,
        type = type,
        timestampEpochMillis = epochDay * 86_400_000L,
        ledgerDateEpochDay = epochDay,
        amount = amount,
        currencyCode = "INR",
        accountId = 10L,
        accountName = "HDFC Regalia Credit Card",
        transferToAccountId = if (type == TransactionType.CC_SETTLEMENT) 10L else null,
        transferToAccountName = if (type == TransactionType.CC_SETTLEMENT) "HDFC Regalia Credit Card" else null,
        transferFee = MinorMoney.ZERO,
        categoryId = 1L,
        categoryName = "Shopping",
        parentCategoryName = null,
        categoryColorHex = "#10B981",
        categoryIconName = "shopping_bag",
        isSplit = false,
        splits = emptyList(),
        tags = emptyList(),
        payeeOrDescription = "Merchant",
        note = "",
        installmentIndex = null,
        installmentTotalCount = null
    )
}
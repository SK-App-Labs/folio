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
}

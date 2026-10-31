// File: app/src/test/java/com/folioledger/app/ui/stats/NetWorthCockpitCalculationsTest.kt
package com.folioledger.app.ui.stats

import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.SecondaryCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetWorthCockpitCalculationsTest {

    private fun createTestAccount(
        id: Long,
        name: String,
        group: AccountGroup,
        balanceMinor: Long,
        currencyCode: String = "INR",
        isArchived: Boolean = false
    ) = AccountSummary(
        id = id,
        name = name,
        groupType = group,
        currencyCode = currencyCode,
        currentBalance = MinorMoney(balanceMinor),
        colorHex = "#10B981",
        iconName = "account_balance",
        includeInNetWorth = true,
        isArchived = isArchived
    )

    private fun createTrendBar(
        periodLabel: String,
        expenseMinor: Long
    ) = MonthlyTrendBar(
        periodLabel = periodLabel,
        startEpochDay = 0L,
        endEpochDay = 30L,
        totalIncome = MinorMoney(100_000_00L),
        totalExpense = MinorMoney(expenseMinor),
        netSavings = MinorMoney(100_000_00L - expenseMinor)
    )

    @Test
    fun testNetWorthVelocity_positiveGrowth() {
        val trendItems = listOf(
            NetWorthGrowthTrendItem(100, "Aug 2026", MinorMoney(5_000_00L), MinorMoney.ZERO, MinorMoney(500_000_00L)),
            NetWorthGrowthTrendItem(130, "Sep 2026", MinorMoney(5_200_00L), MinorMoney.ZERO, MinorMoney(520_000_00L)),
            NetWorthGrowthTrendItem(160, "Oct 2026", MinorMoney(5_616_00L), MinorMoney.ZERO, MinorMoney(561_600_00L))
        )

        val velocity = StatsPredictiveCalculations.computeNetWorthVelocity(trendItems)

        assertEquals(561_600_00L, velocity.currentNetWorth.amountMinor)
        assertEquals(520_000_00L, velocity.previousMonthNetWorth.amountMinor)
        assertEquals(41_600_00L, velocity.absoluteChange.amountMinor)
        assertEquals(8.0f, velocity.percentageChange, 0.01f)
        assertTrue(velocity.isPositive)
    }

    @Test
    fun testNetWorthVelocity_negativeGrowth() {
        val trendItems = listOf(
            NetWorthGrowthTrendItem(100, "Sep 2026", MinorMoney(5_000_00L), MinorMoney.ZERO, MinorMoney(500_000_00L)),
            NetWorthGrowthTrendItem(130, "Oct 2026", MinorMoney(4_500_00L), MinorMoney.ZERO, MinorMoney(450_000_00L))
        )

        val velocity = StatsPredictiveCalculations.computeNetWorthVelocity(trendItems)

        assertEquals(450_000_00L, velocity.currentNetWorth.amountMinor)
        assertEquals(500_000_00L, velocity.previousMonthNetWorth.amountMinor)
        assertEquals(-50_000_00L, velocity.absoluteChange.amountMinor)
        assertEquals(-10.0f, velocity.percentageChange, 0.01f)
        assertFalse(velocity.isPositive)
    }

    @Test
    fun testNetWorthVelocity_singleMonthAndEmpty() {
        val emptyVelocity = StatsPredictiveCalculations.computeNetWorthVelocity(emptyList())
        assertEquals(0L, emptyVelocity.currentNetWorth.amountMinor)
        assertEquals(0f, emptyVelocity.percentageChange, 0.01f)
        assertTrue(emptyVelocity.isPositive)

        val singleItem = listOf(
            NetWorthGrowthTrendItem(100, "Oct 2026", MinorMoney(3_000_00L), MinorMoney.ZERO, MinorMoney(300_000_00L))
        )
        val singleVelocity = StatsPredictiveCalculations.computeNetWorthVelocity(singleItem)
        assertEquals(300_000_00L, singleVelocity.currentNetWorth.amountMinor)
        assertEquals(300_000_00L, singleVelocity.previousMonthNetWorth.amountMinor)
        assertEquals(0L, singleVelocity.absoluteChange.amountMinor)
        assertEquals(0f, singleVelocity.percentageChange, 0.01f)
        assertTrue(singleVelocity.isPositive)
    }

    @Test
    fun testEmergencyRunway_liquidFundsCalculation() {
        val accounts = listOf(
            createTestAccount(1, "Main Bank", AccountGroup.BANK_ACCOUNT, 100_000_00L),
            createTestAccount(2, "Cash Wallet", AccountGroup.CASH, 20_000_00L),
            createTestAccount(3, "Emergency Savings", AccountGroup.SAVINGS, 80_000_00L),
            createTestAccount(4, "Old Closed Account", AccountGroup.BANK_ACCOUNT, 50_000_00L, isArchived = true),
            createTestAccount(5, "Overdraft Debt", AccountGroup.BANK_ACCOUNT, -15_000_00L), // Negative balance ignored
            createTestAccount(6, "Stocks", AccountGroup.INVESTMENT, 300_000_00L) // Investment not in liquid
        )

        // 4 months of expenses: 40k, 60k, 50k, 50k -> Average = 50k/mo
        val bars = listOf(
            createTrendBar("Jul", 40_000_00L),
            createTrendBar("Aug", 60_000_00L),
            createTrendBar("Sep", 50_000_00L),
            createTrendBar("Oct", 50_000_00L)
        )

        val runway = StatsPredictiveCalculations.computeEmergencyRunway(
            allAccounts = accounts,
            trendBars = bars,
            primaryCurrencyCode = "INR",
            secondaryCurrencies = emptyList(),
            includesInvestments = false
        )

        // Liquid funds = 100k + 20k + 80k = 200,000.00
        assertEquals(200_000_00L, runway.totalLiquidFunds.amountMinor)
        assertEquals(300_000_00L, runway.totalInvestmentFunds.amountMinor)
        assertEquals(50_000_00L, runway.averageMonthlyBurnMinor)
        // Runway = 200k / 50k = 4.0 months
        assertEquals(4.0f, runway.runwayMonths, 0.01f)
        assertEquals(RunwayHealthTier.ADEQUATE, runway.healthTier)
        assertFalse(runway.includesInvestments)
    }

    @Test
    fun testEmergencyRunway_includingInvestmentsToggle() {
        val accounts = listOf(
            createTestAccount(1, "Bank", AccountGroup.BANK_ACCOUNT, 100_000_00L),
            createTestAccount(2, "Mutual Funds", AccountGroup.INVESTMENT, 250_000_00L)
        )
        val bars = listOf(createTrendBar("Oct", 50_000_00L))

        // Without investments: 100k / 50k = 2.0 mos (VULNERABLE)
        val liquidOnly = StatsPredictiveCalculations.computeEmergencyRunway(
            allAccounts = accounts,
            trendBars = bars,
            primaryCurrencyCode = "INR",
            secondaryCurrencies = emptyList(),
            includesInvestments = false
        )
        assertEquals(2.0f, liquidOnly.runwayMonths, 0.01f)
        assertEquals(RunwayHealthTier.VULNERABLE, liquidOnly.healthTier)

        // With investments: (100k + 250k) / 50k = 7.0 mos (RESILIENT)
        val withInvest = StatsPredictiveCalculations.computeEmergencyRunway(
            allAccounts = accounts,
            trendBars = bars,
            primaryCurrencyCode = "INR",
            secondaryCurrencies = emptyList(),
            includesInvestments = true
        )
        assertEquals(7.0f, withInvest.runwayMonths, 0.01f)
        assertEquals(RunwayHealthTier.RESILIENT, withInvest.healthTier)
        assertTrue(withInvest.includesInvestments)
    }

    @Test
    fun testEmergencyRunway_zeroMonthlyBurnFallback() {
        val accounts = listOf(
            createTestAccount(1, "Bank", AccountGroup.BANK_ACCOUNT, 50_000_00L)
        )
        // No expenses recorded yet
        val zeroBurnRunway = StatsPredictiveCalculations.computeEmergencyRunway(
            allAccounts = accounts,
            trendBars = emptyList(),
            primaryCurrencyCode = "INR",
            secondaryCurrencies = emptyList(),
            includesInvestments = false
        )
        assertEquals(99.9f, zeroBurnRunway.runwayMonths, 0.01f)
        assertEquals(RunwayHealthTier.RESILIENT, zeroBurnRunway.healthTier)

        // No funds and no expenses
        val emptyRunway = StatsPredictiveCalculations.computeEmergencyRunway(
            allAccounts = emptyList(),
            trendBars = emptyList(),
            primaryCurrencyCode = "INR",
            secondaryCurrencies = emptyList(),
            includesInvestments = false
        )
        assertEquals(0f, emptyRunway.runwayMonths, 0.01f)
        assertEquals(RunwayHealthTier.VULNERABLE, emptyRunway.healthTier)
    }

    @Test
    fun testEmergencyRunway_multiCurrencyConversion() {
        val secondaryCurrencies = listOf(
            SecondaryCurrency(code = "USD", exchangeRateToPrimary = 85.0)
        )
        val accounts = listOf(
            createTestAccount(1, "US Bank Checking", AccountGroup.BANK_ACCOUNT, 1_000_00L, currencyCode = "USD"), // $1,000 -> ₹85,000
            createTestAccount(2, "Local Wallet", AccountGroup.CASH, 15_000_00L, currencyCode = "INR") // ₹15,000
        )
        val bars = listOf(createTrendBar("Oct", 25_000_00L)) // 25k/mo

        val runway = StatsPredictiveCalculations.computeEmergencyRunway(
            allAccounts = accounts,
            trendBars = bars,
            primaryCurrencyCode = "INR",
            secondaryCurrencies = secondaryCurrencies,
            includesInvestments = false
        )

        // Total liquid = 85,000 + 15,000 = 100,000.00
        assertEquals(100_000_00L, runway.totalLiquidFunds.amountMinor)
        // Runway = 100k / 25k = 4.0 months
        assertEquals(4.0f, runway.runwayMonths, 0.01f)
        assertEquals(RunwayHealthTier.ADEQUATE, runway.healthTier)
    }
}

// File: app/src/test/java/com/folioledger/app/domain/NetWorthPredictiveCalculatorTest.kt
package com.folioledger.app.domain

import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.NetWorthGrowthTrendItem
import com.folioledger.app.domain.model.RunwayHealthTier
import com.folioledger.app.domain.util.NetWorthPredictiveCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetWorthPredictiveCalculatorTest {

    @Test
    fun testNetWorthVelocity_positiveGrowth() {
        val trend = listOf(
            NetWorthGrowthTrendItem(
                epochDay = 20700L,
                monthYearText = "Sep 2026",
                assets = MinorMoney(500_000_00L),
                liabilities = MinorMoney(100_000_00L),
                netWorth = MinorMoney(400_000_00L)
            ),
            NetWorthGrowthTrendItem(
                epochDay = 20730L,
                monthYearText = "Oct 2026",
                assets = MinorMoney(550_000_00L),
                liabilities = MinorMoney(90_000_00L),
                netWorth = MinorMoney(460_000_00L)
            )
        )

        val velocity = NetWorthPredictiveCalculator.computeNetWorthVelocity(trend)

        assertTrue(velocity.isPositive)
        assertEquals(MinorMoney(60_000_00L), velocity.absoluteChange)
        // 60,000 / 400,000 = +15%
        assertEquals(15.0f, velocity.percentageChange, 0.01f)
    }

    @Test
    fun testEmergencyRunway_tierClassification() {
        val accounts = listOf(
            AccountSummary(
                id = 1L,
                name = "Liquid Bank",
                groupType = AccountGroup.BANK_ACCOUNT,
                currencyCode = "INR",
                currentBalance = MinorMoney(300_000_00L) // 3,00,000
            ),
            AccountSummary(
                id = 2L,
                name = "Mutual Funds",
                groupType = AccountGroup.INVESTMENT,
                currencyCode = "INR",
                currentBalance = MinorMoney(500_000_00L) // 5,00,000
            )
        )

        val trendBars = listOf(
            MonthlyTrendBar("Jul", 0L, 0L, MinorMoney.ZERO, MinorMoney(50_000_00L), MinorMoney.ZERO),
            MonthlyTrendBar("Aug", 0L, 0L, MinorMoney.ZERO, MinorMoney(50_000_00L), MinorMoney.ZERO),
            MonthlyTrendBar("Sep", 0L, 0L, MinorMoney.ZERO, MinorMoney(50_000_00L), MinorMoney.ZERO)
        )

        // Average monthly burn = 50,000
        // Liquid runway = 300,000 / 50,000 = 6.0 months -> RESILIENT
        val runwayNoInvestments = NetWorthPredictiveCalculator.computeEmergencyRunway(
            allAccounts = accounts,
            trendBars = trendBars,
            primaryCurrencyCode = "INR",
            secondaryCurrencies = emptyList(),
            includesInvestments = false
        )

        assertEquals(6.0f, runwayNoInvestments.runwayMonths, 0.01f)
        assertEquals(RunwayHealthTier.RESILIENT, runwayNoInvestments.healthTier)

        // With investments: (300,000 + 500,000) / 50,000 = 16.0 months -> RESILIENT
        val runwayWithInvestments = NetWorthPredictiveCalculator.computeEmergencyRunway(
            allAccounts = accounts,
            trendBars = trendBars,
            primaryCurrencyCode = "INR",
            secondaryCurrencies = emptyList(),
            includesInvestments = true
        )

        assertEquals(16.0f, runwayWithInvestments.runwayMonths, 0.01f)
        assertEquals(RunwayHealthTier.RESILIENT, runwayWithInvestments.healthTier)
    }

    @Test
    fun testEmergencyRunway_vulnerableTier() {
        val accounts = listOf(
            AccountSummary(
                id = 1L,
                name = "Low Cash",
                groupType = AccountGroup.BANK_ACCOUNT,
                currencyCode = "INR",
                currentBalance = MinorMoney(80_000_00L) // 80,000
            )
        )

        val trendBars = listOf(
            MonthlyTrendBar("Sep", 0L, 0L, MinorMoney.ZERO, MinorMoney(50_000_00L), MinorMoney.ZERO)
        )

        // 80,000 / 50,000 = 1.6 months -> VULNERABLE (< 3 months)
        val runway = NetWorthPredictiveCalculator.computeEmergencyRunway(
            allAccounts = accounts,
            trendBars = trendBars,
            primaryCurrencyCode = "INR",
            secondaryCurrencies = emptyList(),
            includesInvestments = false
        )

        assertEquals(1.6f, runway.runwayMonths, 0.01f)
        assertEquals(RunwayHealthTier.VULNERABLE, runway.healthTier)
    }
}

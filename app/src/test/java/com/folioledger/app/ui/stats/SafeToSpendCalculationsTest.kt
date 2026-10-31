// File: app/src/test/java/com/folioledger/app/ui/stats/SafeToSpendCalculationsTest.kt
package com.folioledger.app.ui.stats

import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.RecurringCommitmentItem
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeToSpendCalculationsTest {

    private val refEpochDay = 20757L // Reference "today"

    private fun createTestAccount(
        id: Long,
        name: String,
        group: AccountGroup,
        balanceMinor: Long,
        currencyCode: String = "INR"
    ) = AccountSummary(
        id = id,
        name = name,
        groupType = group,
        currencyCode = currencyCode,
        currentBalance = MinorMoney(balanceMinor)
    )

    private fun createTestRecurringRule(
        id: Long,
        title: String,
        amountMinor: Long,
        accountId: Long,
        nextDueEpochDay: Long,
        frequency: RepeatFrequency = RepeatFrequency.MONTHLY,
        type: TransactionType = TransactionType.EXPENSE
    ) = RecurringCommitmentItem(
        id = id,
        title = title,
        type = type,
        amount = MinorMoney(amountMinor),
        currencyCode = "INR",
        accountId = accountId,
        frequency = frequency,
        nextOccurrenceEpochDay = nextDueEpochDay,
        isActive = true
    )

    @Test
    fun testSafeToSpend_positiveDiscretionaryCash() {
        val accounts = listOf(
            createTestAccount(1, "HDFC Salary", AccountGroup.BANK_ACCOUNT, 70_000_00L),
            createTestAccount(2, "Cash in Wallet", AccountGroup.CASH, 15_000_00L),
            createTestAccount(3, "Axis Credit Card", AccountGroup.CREDIT_CARD, -30_000_00L) // CC balance negative when owing
        )

        val recurringRules = listOf(
            createTestRecurringRule(101, "House Rent", 10_000_00L, accountId = 1, nextDueEpochDay = refEpochDay + 5),
            createTestRecurringRule(102, "Internet Fiber", 2_500_00L, accountId = 1, nextDueEpochDay = refEpochDay + 10),
            createTestRecurringRule(103, "Annual Insurance", 50_000_00L, accountId = 1, nextDueEpochDay = refEpochDay + 45) // Outside 14 days
        )

        val safeToSpend = StatsPredictiveCalculations.computeSafeToSpend(
            allAccounts = accounts,
            recurringRules = recurringRules,
            futureTransactions = emptyList(),
            todayEpochDay = refEpochDay,
            periodStartEpochDay = refEpochDay - 10,
            periodEndEpochDay = refEpochDay + 20,
            primaryCurrencyCode = "INR"
        )

        assertEquals("Liquid cash should sum bank + cash", MinorMoney(85_000_00L), safeToSpend.totalLiquidBankAndCash)
        assertEquals("Credit card debt should be 30k", MinorMoney(30_000_00L), safeToSpend.totalCreditCardDebt)
        assertEquals("Upcoming 14d commitments should sum rent + internet (12.5k)", MinorMoney(12_500_00L), safeToSpend.upcoming14DayCommitments)
        // 85,000 - 30,000 - 12,500 = 42,500
        assertEquals(MinorMoney(42_500_00L), safeToSpend.trueSafeToSpend)
        assertTrue(safeToSpend.isPositive)
        assertTrue(safeToSpend.isCurrentPeriod)
    }

    @Test
    fun testSafeToSpend_negativeEncumberedCash() {
        val accounts = listOf(
            createTestAccount(1, "Bank Account", AccountGroup.BANK_ACCOUNT, 15_000_00L),
            createTestAccount(2, "Credit Card", AccountGroup.CREDIT_CARD, -20_000_00L)
        )

        val recurringRules = listOf(
            createTestRecurringRule(101, "Bill", 10_000_00L, accountId = 1, nextDueEpochDay = refEpochDay + 3)
        )

        val safeToSpend = StatsPredictiveCalculations.computeSafeToSpend(
            allAccounts = accounts,
            recurringRules = recurringRules,
            futureTransactions = emptyList(),
            todayEpochDay = refEpochDay,
            periodStartEpochDay = refEpochDay - 10,
            periodEndEpochDay = refEpochDay + 20,
            primaryCurrencyCode = "INR"
        )

        // 15,000 - 20,000 - 10,000 = -15,000
        assertEquals(MinorMoney(-15_000_00L), safeToSpend.trueSafeToSpend)
        assertFalse("Should be marked non-positive when commitments exceed cash", safeToSpend.isPositive)
    }

    @Test
    fun testShortfallAlert_detectionAndUrgencySort() {
        val accounts = listOf(
            createTestAccount(1, "HDFC Salary", AccountGroup.BANK_ACCOUNT, 12_000_00L),
            createTestAccount(2, "ICICI Emergency", AccountGroup.SAVINGS, 50_000_00L),
            createTestAccount(3, "SBI Petty", AccountGroup.BANK_ACCOUNT, 1_000_00L)
        )

        val recurringRules = listOf(
            createTestRecurringRule(101, "Rent", 18_000_00L, accountId = 1, nextDueEpochDay = refEpochDay + 4), // Shortfall of 6k on day 4
            createTestRecurringRule(102, "Gym", 5_000_00L, accountId = 2, nextDueEpochDay = refEpochDay + 2),   // Covered (50k > 5k)
            createTestRecurringRule(103, "Loan EMI", 5_000_00L, accountId = 3, nextDueEpochDay = refEpochDay + 1) // Shortfall of 4k on day 1
        )

        val alerts = StatsPredictiveCalculations.computeShortfallAlerts(
            allAccounts = accounts,
            recurringRules = recurringRules,
            futureTransactions = emptyList(),
            todayEpochDay = refEpochDay,
            primaryCurrencyCode = "INR"
        )

        assertEquals("Should detect 2 shortfalls", 2, alerts.size)
        // First should be SBI due to daysUntilShortfall = 1
        assertEquals("SBI Petty", alerts[0].accountName)
        assertEquals(MinorMoney(4_000_00L), alerts[0].shortfallAmount)
        assertEquals(1L, alerts[0].daysUntilShortfall)

        // Second should be HDFC due to daysUntilShortfall = 4
        assertEquals("HDFC Salary", alerts[1].accountName)
        assertEquals(MinorMoney(6_000_00L), alerts[1].shortfallAmount)
        assertEquals(4L, alerts[1].daysUntilShortfall)
    }

    @Test
    fun testRecurringOccurrencesProjection() {
        // Weekly rule starting today
        val occurrences = StatsPredictiveCalculations.projectRecurringOccurrencesInHorizon(
            startDay = refEpochDay,
            frequency = RepeatFrequency.WEEKLY,
            todayEpochDay = refEpochDay,
            horizonEpochDay = refEpochDay + 14L
        )

        // Should hit on Day 0, Day 7, Day 14 (3 occurrences in 14-day window)
        assertEquals(3, occurrences.size)
        assertEquals(refEpochDay, occurrences[0])
        assertEquals(refEpochDay + 7L, occurrences[1])
        assertEquals(refEpochDay + 14L, occurrences[2])
    }
}

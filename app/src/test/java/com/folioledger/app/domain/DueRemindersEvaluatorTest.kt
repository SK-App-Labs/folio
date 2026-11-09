// File: app/src/test/java/com/folioledger/app/domain/DueRemindersEvaluatorTest.kt
package com.folioledger.app.domain

import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.RecurringCommitmentItem
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.util.DueRemindersEvaluator
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DueRemindersEvaluatorTest {

    @Test
    fun testCreditCardReminder_triggeredWithinNoticeWindow() {
        // Statement closes on 15th, due on 5th of next month.
        // Today is 2026-11-02 (3 days before due date 2026-11-05).
        val today = LocalDate.of(2026, 11, 2)
        val card = AccountSummary(
            id = 101L,
            name = "ICICI Amazon Pay",
            groupType = AccountGroup.CREDIT_CARD,
            currencyCode = "INR",
            currentBalance = MinorMoney(-15_000_00L),
            creditLimit = MinorMoney(100_000_00L),
            creditCardStatementPayable = MinorMoney(12_500_00L),
            creditCardUnbilledAmount = MinorMoney(2_500_00L),
            settlementDayOfMonth = 15,
            paymentDueDayOfMonth = 5
        )

        val reminders = DueRemindersEvaluator.evaluateCreditCardReminders(
            accounts = listOf(card),
            todayEpochDay = today.toEpochDay(),
            leadDaysNotice = 3
        )

        assertEquals(1, reminders.size)
        val alert = reminders.first()
        assertEquals(101L, alert.accountId)
        assertEquals("ICICI Amazon Pay", alert.accountName)
        assertEquals(MinorMoney(12_500_00L), alert.amountDue)
        assertEquals(LocalDate.of(2026, 11, 5), alert.dueDate)
        assertEquals(3L, alert.daysRemaining)
        assertFalse(alert.isOverdue)
    }

    @Test
    fun testCreditCardReminder_skippedOutsideNoticeWindow() {
        // Today is 2026-10-25 (11 days before due date 2026-11-05).
        val today = LocalDate.of(2026, 10, 25)
        val card = AccountSummary(
            id = 101L,
            name = "ICICI Amazon Pay",
            groupType = AccountGroup.CREDIT_CARD,
            currencyCode = "INR",
            currentBalance = MinorMoney(-15_000_00L),
            creditLimit = MinorMoney(100_000_00L),
            creditCardStatementPayable = MinorMoney(12_500_00L),
            settlementDayOfMonth = 15,
            paymentDueDayOfMonth = 5
        )

        val reminders = DueRemindersEvaluator.evaluateCreditCardReminders(
            accounts = listOf(card),
            todayEpochDay = today.toEpochDay(),
            leadDaysNotice = 3
        )

        assertTrue(reminders.isEmpty())
    }

    @Test
    fun testCreditCardReminder_skippedWhenZeroBalance() {
        // Today is 2026-11-04 (1 day before due), but card was fully paid off
        val today = LocalDate.of(2026, 11, 4)
        val card = AccountSummary(
            id = 101L,
            name = "HDFC Regalia",
            groupType = AccountGroup.CREDIT_CARD,
            currencyCode = "INR",
            currentBalance = MinorMoney.ZERO,
            creditLimit = MinorMoney(200_000_00L),
            creditCardStatementPayable = MinorMoney.ZERO,
            settlementDayOfMonth = 15,
            paymentDueDayOfMonth = 5
        )

        val reminders = DueRemindersEvaluator.evaluateCreditCardReminders(
            accounts = listOf(card),
            todayEpochDay = today.toEpochDay(),
            leadDaysNotice = 3
        )

        assertTrue(reminders.isEmpty())
    }

    @Test
    fun testCreditCardReminder_overdueTriggersAlert() {
        // Today is 2026-11-06 (1 day after due date 2026-11-05)
        val today = LocalDate.of(2026, 11, 6)
        val card = AccountSummary(
            id = 101L,
            name = "Axis Bank Magnus",
            groupType = AccountGroup.CREDIT_CARD,
            currencyCode = "INR",
            currentBalance = MinorMoney(-25_000_00L),
            creditLimit = MinorMoney(300_000_00L),
            creditCardStatementPayable = MinorMoney(25_000_00L),
            settlementDayOfMonth = 15,
            paymentDueDayOfMonth = 5
        )

        val reminders = DueRemindersEvaluator.evaluateCreditCardReminders(
            accounts = listOf(card),
            todayEpochDay = today.toEpochDay(),
            leadDaysNotice = 3
        )

        assertEquals(1, reminders.size)
        val alert = reminders.first()
        assertTrue(alert.isOverdue)
        assertEquals(-1L, alert.daysRemaining)
    }

    @Test
    fun testBillReminder_triggeredWithinNoticeWindow() {
        val today = LocalDate.of(2026, 11, 10)
        val rule = RecurringCommitmentItem(
            id = 1L,
            title = "Netflix Subscription",
            type = TransactionType.EXPENSE,
            amount = MinorMoney(649_00L),
            currencyCode = "INR",
            accountId = 5L,
            frequency = RepeatFrequency.MONTHLY,
            nextOccurrenceEpochDay = LocalDate.of(2026, 11, 11).toEpochDay(),
            accountName = "HDFC Salary Account",
            isActive = true
        )

        val reminders = DueRemindersEvaluator.evaluateBillReminders(
            recurringRules = listOf(rule),
            todayEpochDay = today.toEpochDay(),
            leadDaysNotice = 1
        )

        assertEquals(1, reminders.size)
        val alert = reminders.first()
        assertEquals(1L, alert.ruleId)
        assertEquals("Netflix Subscription", alert.title)
        assertEquals(MinorMoney(649_00L), alert.amount)
        assertEquals(1L, alert.daysRemaining)
    }

    @Test
    fun testBillReminder_inactiveRuleSkipped() {
        val today = LocalDate.of(2026, 11, 10)
        val rule = RecurringCommitmentItem(
            id = 2L,
            title = "Gym Membership",
            type = TransactionType.EXPENSE,
            amount = MinorMoney(2_000_00L),
            currencyCode = "INR",
            accountId = 5L,
            frequency = RepeatFrequency.MONTHLY,
            nextOccurrenceEpochDay = LocalDate.of(2026, 11, 11).toEpochDay(),
            accountName = "HDFC Salary Account",
            isActive = false
        )

        val reminders = DueRemindersEvaluator.evaluateBillReminders(
            recurringRules = listOf(rule),
            todayEpochDay = today.toEpochDay(),
            leadDaysNotice = 1
        )

        assertTrue(reminders.isEmpty())
    }
}

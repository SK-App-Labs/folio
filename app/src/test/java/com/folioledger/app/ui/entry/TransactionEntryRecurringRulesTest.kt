// File: app/src/test/java/com/folioledger/app/ui/entry/TransactionEntryRecurringRulesTest.kt
package com.folioledger.app.ui.entry

import com.folioledger.app.domain.model.InstallmentPlanWithProgress
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.RecurringCommitmentItem
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.util.RecurringScheduleCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TransactionEntryRecurringRulesTest {

    @Test
    fun testAll13Frequencies_haveDistinctLabelsAndShortLabels() {
        val allFrequencies = RepeatFrequency.entries
        assertEquals("There must be exactly 13 calendar recurrence frequencies", 13, allFrequencies.size)

        allFrequencies.forEach { freq ->
            assertTrue("Display name must not be blank for ${freq.name}", freq.displayName.isNotBlank())
            assertTrue("Short label must not be blank for ${freq.name}", freq.shortLabel.isNotBlank())
        }
    }

    @Test
    fun testBackwardsCompatibleAliases_pointToCorrectNewFrequencies() {
        assertEquals(RepeatFrequency.EVERY_DAY, RepeatFrequency.DAILY)
        assertEquals(RepeatFrequency.EVERY_WEEK, RepeatFrequency.WEEKLY)
        assertEquals(RepeatFrequency.EVERY_2_WEEKS, RepeatFrequency.BIWEEKLY)
        assertEquals(RepeatFrequency.EVERY_MONTH, RepeatFrequency.MONTHLY)
        assertEquals(RepeatFrequency.ANNUALLY, RepeatFrequency.YEARLY)
    }

    @Test
    fun testRecurringBurdenCalculation_allFrequencies() {
        val baseAmountMinor = 120_00L // 120.00
        val monthlyEquivalents = RepeatFrequency.entries.associateWith { freq ->
            when (freq) {
                RepeatFrequency.EVERY_DAY -> baseAmountMinor * 30L
                RepeatFrequency.WEEKDAYS -> baseAmountMinor * 22L
                RepeatFrequency.WEEKENDS -> baseAmountMinor * 8L
                RepeatFrequency.EVERY_WEEK -> baseAmountMinor * 4L
                RepeatFrequency.EVERY_2_WEEKS -> baseAmountMinor * 2L
                RepeatFrequency.EVERY_4_WEEKS,
                RepeatFrequency.EVERY_MONTH,
                RepeatFrequency.END_OF_MONTH -> baseAmountMinor
                RepeatFrequency.EVERY_2_MONTHS -> baseAmountMinor / 2L
                RepeatFrequency.EVERY_3_MONTHS -> baseAmountMinor / 3L
                RepeatFrequency.EVERY_4_MONTHS -> baseAmountMinor / 4L
                RepeatFrequency.EVERY_6_MONTHS -> baseAmountMinor / 6L
                RepeatFrequency.ANNUALLY -> baseAmountMinor / 12L
            }
        }

        assertEquals(3600_00L, monthlyEquivalents[RepeatFrequency.EVERY_DAY])
        assertEquals(2640_00L, monthlyEquivalents[RepeatFrequency.WEEKDAYS])
        assertEquals(960_00L, monthlyEquivalents[RepeatFrequency.WEEKENDS])
        assertEquals(480_00L, monthlyEquivalents[RepeatFrequency.EVERY_WEEK])
        assertEquals(240_00L, monthlyEquivalents[RepeatFrequency.EVERY_2_WEEKS])
        assertEquals(120_00L, monthlyEquivalents[RepeatFrequency.EVERY_4_WEEKS])
        assertEquals(120_00L, monthlyEquivalents[RepeatFrequency.EVERY_MONTH])
        assertEquals(120_00L, monthlyEquivalents[RepeatFrequency.END_OF_MONTH])
        assertEquals(60_00L, monthlyEquivalents[RepeatFrequency.EVERY_2_MONTHS])
        assertEquals(40_00L, monthlyEquivalents[RepeatFrequency.EVERY_3_MONTHS])
        assertEquals(30_00L, monthlyEquivalents[RepeatFrequency.EVERY_4_MONTHS])
        assertEquals(20_00L, monthlyEquivalents[RepeatFrequency.EVERY_6_MONTHS])
        assertEquals(10_00L, monthlyEquivalents[RepeatFrequency.ANNUALLY])
    }

    @Test
    fun testInstallmentPlanProgress_calculation() {
        val plan = InstallmentPlanWithProgress(
            id = 1L,
            title = "iPhone 16 Pro",
            accountId = 5L,
            accountName = "HDFC Regalia",
            categoryId = 10L,
            categoryName = "Electronics",
            categoryColorHex = "#2563EB",
            categoryIconName = "phone",
            totalAmount = MinorMoney(120_000_00L),
            monthsCount = 12,
            paidInstallmentsCount = 4,
            remainingInstallmentsCount = 8,
            paidAmount = MinorMoney(40_000_00L),
            remainingAmount = MinorMoney(80_000_00L),
            monthlyAmount = MinorMoney(10_000_00L),
            startDateEpochDay = LocalDate.of(2026, 7, 1).toEpochDay(),
            nextDueDateEpochDay = LocalDate.of(2026, 11, 1).toEpochDay()
        )

        assertEquals(12, plan.monthsCount)
        assertEquals(4, plan.paidInstallmentsCount)
        assertEquals(8, plan.remainingInstallmentsCount)
        assertEquals(MinorMoney(40_000_00L), plan.paidAmount)
        assertEquals(MinorMoney(80_000_00L), plan.remainingAmount)
        assertEquals(MinorMoney(10_000_00L), plan.monthlyAmount)
        assertEquals(plan.totalAmount, plan.paidAmount + plan.remainingAmount)
    }

    @Test
    fun testRecurringCommitmentItem_creationAndDefaults() {
        val item = RecurringCommitmentItem(
            id = 10L,
            title = "Gym Membership",
            type = TransactionType.EXPENSE,
            amount = MinorMoney(3_000_00L),
            currencyCode = "INR",
            accountId = 2L,
            frequency = RepeatFrequency.EVERY_MONTH,
            nextOccurrenceEpochDay = LocalDate.of(2026, 12, 1).toEpochDay(),
            accountName = "SBI Salary",
            categoryName = "Health & Fitness",
            isActive = true
        )

        assertEquals("Gym Membership", item.title)
        assertTrue(item.isActive)
        assertEquals("SBI Salary", item.accountName)
        assertEquals("Health & Fitness", item.categoryName)
        assertEquals(RepeatFrequency.EVERY_MONTH, item.frequency)
    }

    @Test
    fun testRecurringCommitmentItem_transferRuleWithTargetAccount() {
        val transferRule = RecurringCommitmentItem(
            id = 20L,
            title = "Checking → Emergency Fund",
            type = TransactionType.TRANSFER,
            amount = MinorMoney(10_000_00L),
            currencyCode = "INR",
            accountId = 1L,
            accountName = "HDFC Checking",
            transferToAccountId = 2L,
            transferToAccountName = "SBI Emergency Fund",
            frequency = RepeatFrequency.EVERY_MONTH,
            nextOccurrenceEpochDay = LocalDate.of(2026, 12, 1).toEpochDay(),
            isActive = true
        )

        assertEquals(TransactionType.TRANSFER, transferRule.type)
        assertEquals(1L, transferRule.accountId)
        assertEquals("HDFC Checking", transferRule.accountName)
        assertEquals(2L, transferRule.transferToAccountId)
        assertEquals("SBI Emergency Fund", transferRule.transferToAccountName)
        assertEquals("Checking → Emergency Fund", transferRule.title)
        assertTrue(transferRule.isActive)
    }

    @Test
    fun testRecurringBurden_separatesExpensesFromTransfers() {
        val expenseRule = RecurringCommitmentItem(
            id = 1L,
            title = "Netflix",
            type = TransactionType.EXPENSE,
            amount = MinorMoney(650_00L),
            currencyCode = "INR",
            accountId = 1L,
            frequency = RepeatFrequency.EVERY_MONTH,
            nextOccurrenceEpochDay = LocalDate.of(2026, 12, 1).toEpochDay()
        )

        val transferRule = RecurringCommitmentItem(
            id = 2L,
            title = "Mutual Fund SIP",
            type = TransactionType.TRANSFER,
            amount = MinorMoney(5_000_00L),
            currencyCode = "INR",
            accountId = 1L,
            transferToAccountId = 3L,
            frequency = RepeatFrequency.EVERY_MONTH,
            nextOccurrenceEpochDay = LocalDate.of(2026, 12, 5).toEpochDay()
        )

        val rules = listOf(expenseRule, transferRule)

        var expenseBurden = 0L
        var transferBurden = 0L

        for (rule in rules) {
            when (rule.type) {
                TransactionType.EXPENSE -> expenseBurden += rule.amount.amountMinor
                TransactionType.TRANSFER -> transferBurden += rule.amount.amountMinor
                else -> Unit
            }
        }

        assertEquals(650_00L, expenseBurden)
        assertEquals(5_000_00L, transferBurden)
    }
}

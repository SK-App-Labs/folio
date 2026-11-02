package com.folioledger.app.domain

import com.folioledger.app.domain.model.CardMilestoneConfig
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.util.CardMilestoneCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CardMilestoneCalculatorTest {

    @Test
    fun anniversaryWindow_firstYear_matchesExpectedDates() {
        val startEpochDay = LocalDate.of(2026, 1, 15).toEpochDay()
        val referenceEpochDay = LocalDate.of(2026, 6, 1).toEpochDay()

        val (cycleStart, cycleEnd) = CardMilestoneCalculator.computeCurrentAnniversaryWindow(
            startDateEpochDay = startEpochDay,
            referenceDateEpochDay = referenceEpochDay,
            isAutoRenewing = true
        )

        assertEquals(LocalDate.of(2026, 1, 15), cycleStart)
        assertEquals(LocalDate.of(2027, 1, 14), cycleEnd)
    }

    @Test
    fun anniversaryWindow_autoRenew_rollsForwardMultipleYears() {
        // Card was issued 3 years ago on Oct 10, 2023
        val startEpochDay = LocalDate.of(2023, 10, 10).toEpochDay()
        // Today is Nov 2, 2026 -> Active window is Oct 10, 2026 to Oct 9, 2027
        val referenceEpochDay = LocalDate.of(2026, 11, 2).toEpochDay()

        val (cycleStart, cycleEnd) = CardMilestoneCalculator.computeCurrentAnniversaryWindow(
            startDateEpochDay = startEpochDay,
            referenceDateEpochDay = referenceEpochDay,
            isAutoRenewing = true
        )

        assertEquals(LocalDate.of(2026, 10, 10), cycleStart)
        assertEquals(LocalDate.of(2027, 10, 9), cycleEnd)
    }

    @Test
    fun anniversaryWindow_noAutoRenew_retainsInitialWindow() {
        val startEpochDay = LocalDate.of(2023, 10, 10).toEpochDay()
        val referenceEpochDay = LocalDate.of(2026, 11, 2).toEpochDay()

        val (cycleStart, cycleEnd) = CardMilestoneCalculator.computeCurrentAnniversaryWindow(
            startDateEpochDay = startEpochDay,
            referenceDateEpochDay = referenceEpochDay,
            isAutoRenewing = false
        )

        // Without auto-renew, window remains the 1-time window from 2023
        assertEquals(LocalDate.of(2023, 10, 10), cycleStart)
        assertEquals(LocalDate.of(2024, 10, 9), cycleEnd)
    }

    @Test
    fun anniversaryWindow_leapYearHandling() {
        val startEpochDay = LocalDate.of(2024, 2, 29).toEpochDay()
        // In 2025 (non-leap year), rolls forward
        val ref2025EpochDay = LocalDate.of(2025, 3, 1).toEpochDay()

        val (cycleStart, cycleEnd) = CardMilestoneCalculator.computeCurrentAnniversaryWindow(
            startDateEpochDay = startEpochDay,
            referenceDateEpochDay = ref2025EpochDay,
            isAutoRenewing = true
        )

        assertEquals(LocalDate.of(2025, 2, 28), cycleStart)
        assertEquals(LocalDate.of(2026, 2, 27), cycleEnd)
    }

    @Test
    fun evaluateMilestone_inProgress_calculatesPacingAndDailySpend() {
        val config = CardMilestoneConfig(
            accountId = 101L,
            milestoneTitle = "Infinia Annual Fee Waiver",
            cycleStartDateEpochDay = LocalDate.of(2026, 1, 1).toEpochDay(),
            targetAmountMinor = 1_000_000_00L, // 10,00,000
            annualFeeSavedMinor = 12_500_00L,  // 12,500
            isAutoRenewing = true
        )

        // Halfway through the year with 6,00,000 spend (Ahead of pace)
        val today = LocalDate.of(2026, 7, 2)
        val qualifyingSpend = 600_000_00L

        val result = CardMilestoneCalculator.evaluateMilestone(
            config = config,
            qualifyingSpendMinor = qualifyingSpend,
            referenceDate = today
        )

        assertEquals("Infinia Annual Fee Waiver", result.milestoneTitle)
        assertEquals(MinorMoney(600_000_00L), result.qualifyingSpend)
        assertEquals(MinorMoney(400_000_00L), result.remainingSpend)
        assertEquals(60f, result.progressPercentage, 0.1f)
        assertTrue(result.isAheadOfPace)
        assertFalse(result.isAchieved)
        assertEquals(MinorMoney(12_500_00L), result.annualFeeSaved)
        assertTrue(result.daysRemaining in 180..185)
        assertTrue(result.requiredDailySpend.amountMinor > 0L)
    }

    @Test
    fun evaluateMilestone_targetReached_marksAchieved() {
        val config = CardMilestoneConfig(
            accountId = 102L,
            milestoneTitle = "Amex Platinum Travel 4L Milestone",
            cycleStartDateEpochDay = LocalDate.of(2026, 3, 15).toEpochDay(),
            targetAmountMinor = 400_000_00L,
            annualFeeSavedMinor = null,
            isAutoRenewing = true
        )

        val today = LocalDate.of(2026, 8, 1)
        val qualifyingSpend = 420_000_00L

        val result = CardMilestoneCalculator.evaluateMilestone(
            config = config,
            qualifyingSpendMinor = qualifyingSpend,
            referenceDate = today
        )

        assertTrue(result.isAchieved)
        assertTrue(result.isAheadOfPace)
        assertEquals(MinorMoney.ZERO, result.remainingSpend)
        assertEquals(MinorMoney.ZERO, result.requiredDailySpend)
        assertEquals(100f, result.progressPercentage, 0.01f)
    }
}

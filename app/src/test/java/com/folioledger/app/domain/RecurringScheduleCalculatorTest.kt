package com.folioledger.app.domain

import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.util.RecurringScheduleCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class RecurringScheduleCalculatorTest {

    @Test
    fun computeNextOccurrence_everyDay_advancesOneDay() {
        val date = LocalDate.of(2026, 11, 3)
        val next = RecurringScheduleCalculator.computeNextOccurrence(date, RepeatFrequency.EVERY_DAY)
        assertEquals(LocalDate.of(2026, 11, 4), next)
    }

    @Test
    fun computeNextOccurrence_weekdays_skipsWeekends() {
        // Monday -> Tuesday
        val monday = LocalDate.of(2026, 11, 2)
        assertEquals(DayOfWeek.MONDAY, monday.dayOfWeek)
        val tuesday = RecurringScheduleCalculator.computeNextOccurrence(monday, RepeatFrequency.WEEKDAYS)
        assertEquals(LocalDate.of(2026, 11, 3), tuesday)

        // Friday -> Monday (skips Saturday & Sunday)
        val friday = LocalDate.of(2026, 11, 6)
        assertEquals(DayOfWeek.FRIDAY, friday.dayOfWeek)
        val nextMonday = RecurringScheduleCalculator.computeNextOccurrence(friday, RepeatFrequency.WEEKDAYS)
        assertEquals(LocalDate.of(2026, 11, 9), nextMonday)
        assertEquals(DayOfWeek.MONDAY, nextMonday.dayOfWeek)
    }

    @Test
    fun computeNextOccurrence_weekends_navigatesSaturdayAndSunday() {
        // Saturday -> Sunday
        val saturday = LocalDate.of(2026, 11, 7)
        assertEquals(DayOfWeek.SATURDAY, saturday.dayOfWeek)
        val sunday = RecurringScheduleCalculator.computeNextOccurrence(saturday, RepeatFrequency.WEEKENDS)
        assertEquals(LocalDate.of(2026, 11, 8), sunday)
        assertEquals(DayOfWeek.SUNDAY, sunday.dayOfWeek)

        // Sunday -> Next Saturday
        val nextSaturday = RecurringScheduleCalculator.computeNextOccurrence(sunday, RepeatFrequency.WEEKENDS)
        assertEquals(LocalDate.of(2026, 11, 14), nextSaturday)
        assertEquals(DayOfWeek.SATURDAY, nextSaturday.dayOfWeek)
    }

    @Test
    fun computeNextOccurrence_every4Weeks_advancesExactly28Days() {
        // Telecom recharge cycle
        val date = LocalDate.of(2026, 1, 1)
        val next = RecurringScheduleCalculator.computeNextOccurrence(date, RepeatFrequency.EVERY_4_WEEKS)
        assertEquals(LocalDate.of(2026, 1, 29), next)
    }

    @Test
    fun computeNextOccurrence_endOfMonth_handlesVariableMonthLengths() {
        // 31 Jan -> 28 Feb (in 2026 non-leap year)
        val jan31 = LocalDate.of(2026, 1, 31)
        val febEnd = RecurringScheduleCalculator.computeNextOccurrence(jan31, RepeatFrequency.END_OF_MONTH)
        assertEquals(LocalDate.of(2026, 2, 28), febEnd)

        // 28 Feb -> 31 Mar
        val marEnd = RecurringScheduleCalculator.computeNextOccurrence(febEnd, RepeatFrequency.END_OF_MONTH)
        assertEquals(LocalDate.of(2026, 3, 31), marEnd)

        // 31 Mar -> 30 Apr
        val aprEnd = RecurringScheduleCalculator.computeNextOccurrence(marEnd, RepeatFrequency.END_OF_MONTH)
        assertEquals(LocalDate.of(2026, 4, 30), aprEnd)
    }

    @Test
    fun computeNextOccurrence_multiMonthFrequencies() {
        val date = LocalDate.of(2026, 1, 15)

        assertEquals(LocalDate.of(2026, 3, 15), RecurringScheduleCalculator.computeNextOccurrence(date, RepeatFrequency.EVERY_2_MONTHS))
        assertEquals(LocalDate.of(2026, 4, 15), RecurringScheduleCalculator.computeNextOccurrence(date, RepeatFrequency.EVERY_3_MONTHS))
        assertEquals(LocalDate.of(2026, 5, 15), RecurringScheduleCalculator.computeNextOccurrence(date, RepeatFrequency.EVERY_4_MONTHS))
        assertEquals(LocalDate.of(2026, 7, 15), RecurringScheduleCalculator.computeNextOccurrence(date, RepeatFrequency.EVERY_6_MONTHS))
        assertEquals(LocalDate.of(2027, 1, 15), RecurringScheduleCalculator.computeNextOccurrence(date, RepeatFrequency.ANNUALLY))
    }

    @Test
    fun projectOccurrencesInHorizon_returnsCorrectEpochDays() {
        val startDay = LocalDate.of(2026, 11, 1).toEpochDay()
        val today = LocalDate.of(2026, 11, 1).toEpochDay()
        val horizon = LocalDate.of(2026, 11, 30).toEpochDay()

        val weeklyOccurrences = RecurringScheduleCalculator.projectOccurrencesInHorizon(
            startDay = startDay,
            frequency = RepeatFrequency.EVERY_WEEK,
            todayEpochDay = today,
            horizonEpochDay = horizon
        )

        // Nov 1 (Sun), Nov 8, Nov 15, Nov 22, Nov 29 -> 5 occurrences
        assertEquals(5, weeklyOccurrences.size)
        assertEquals(LocalDate.of(2026, 11, 1).toEpochDay(), weeklyOccurrences[0])
        assertEquals(LocalDate.of(2026, 11, 29).toEpochDay(), weeklyOccurrences[4])
    }

    @Test
    fun formatHumanReadableSummary_producesValidDescriptions() {
        val date = LocalDate.of(2026, 11, 5) // 5th of Nov

        val monthlySummary = RecurringScheduleCalculator.formatHumanReadableSummary(RepeatFrequency.EVERY_MONTH, date)
        assertEquals("Repeats every month on the 5th", monthlySummary)

        val endOfMonthSummary = RecurringScheduleCalculator.formatHumanReadableSummary(RepeatFrequency.END_OF_MONTH, date)
        assertEquals("Repeats on the last day of each month", endOfMonthSummary)

        val preview = RecurringScheduleCalculator.formatNextOccurrencesPreview(RepeatFrequency.EVERY_MONTH, date, 2)
        assertTrue(preview.contains("05 Dec"))
        assertTrue(preview.contains("05 Jan"))
    }
}

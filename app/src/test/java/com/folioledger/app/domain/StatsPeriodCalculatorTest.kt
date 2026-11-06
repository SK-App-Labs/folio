// File: app/src/test/java/com/folioledger/app/domain/StatsPeriodCalculatorTest.kt
package com.folioledger.app.domain

import com.folioledger.app.domain.model.StatsPeriodGranularity
import com.folioledger.app.domain.util.StatsPeriodCalculator
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsPeriodCalculatorTest {

    @Test
    fun testWeeklyRange_startsOnConfiguredIsoDay() {
        // 2026-11-06 is Friday.
        val refDate = LocalDate.of(2026, 11, 6)
        // Week start = Monday (ISO 1)
        val range = StatsPeriodCalculator.computeWeeklyRange(
            referenceDateEpochDay = refDate.toEpochDay(),
            weekStartDayIso = 1
        )

        val startDate = LocalDate.ofEpochDay(range.startEpochDay)
        val endDate = LocalDate.ofEpochDay(range.endEpochDay)

        assertEquals("Week should start on Monday 2026-11-02", LocalDate.of(2026, 11, 2), startDate)
        assertEquals("Week should end on Sunday 2026-11-08", LocalDate.of(2026, 11, 8), endDate)
        assertEquals(2026, range.anchorYear)
    }

    @Test
    fun testFinancialMonthRange_calendarMonthDefault() {
        // Reference: 2026-10-15 with monthStartDay = 1
        val refDate = LocalDate.of(2026, 10, 15)
        val range = StatsPeriodCalculator.computeFinancialMonthRange(
            referenceDateEpochDay = refDate.toEpochDay(),
            monthStartDay = 1
        )

        val startDate = LocalDate.ofEpochDay(range.startEpochDay)
        val endDate = LocalDate.ofEpochDay(range.endEpochDay)

        assertEquals("Should start on 1st of month", LocalDate.of(2026, 10, 1), startDate)
        assertEquals("Should end on 31st of month", LocalDate.of(2026, 10, 31), endDate)
        assertEquals("Oct 2026", range.title)
    }

    @Test
    fun testFinancialMonthRange_midMonthSalaryAnchor() {
        // Reference: 2026-10-26 with salary start day = 25
        val refDate = LocalDate.of(2026, 10, 26)
        val range = StatsPeriodCalculator.computeFinancialMonthRange(
            referenceDateEpochDay = refDate.toEpochDay(),
            monthStartDay = 25
        )

        val startDate = LocalDate.ofEpochDay(range.startEpochDay)
        val endDate = LocalDate.ofEpochDay(range.endEpochDay)

        assertEquals(LocalDate.of(2026, 10, 25), startDate)
        assertEquals(LocalDate.of(2026, 11, 24), endDate)
    }

    @Test
    fun testAnnualRange_fullYearSpan() {
        val refDate = LocalDate.of(2026, 6, 15)
        val range = StatsPeriodCalculator.computeAnnualRange(
            referenceDateEpochDay = refDate.toEpochDay(),
            monthStartDay = 1
        )

        val startDate = LocalDate.ofEpochDay(range.startEpochDay)
        val endDate = LocalDate.ofEpochDay(range.endEpochDay)

        assertEquals(LocalDate.of(2026, 1, 1), startDate)
        assertEquals(LocalDate.of(2026, 12, 31), endDate)
        assertEquals("Year 2026", range.title)
    }

    @Test
    fun testCustomRange_normalizesOrder() {
        val day1 = LocalDate.of(2026, 5, 1).toEpochDay()
        val day2 = LocalDate.of(2026, 5, 20).toEpochDay()

        // Passed in reverse order
        val range = StatsPeriodCalculator.computeCustomRange(
            customStartEpochDay = day2,
            customEndEpochDay = day1
        )

        assertEquals(day1, range.startEpochDay)
        assertEquals(day2, range.endEpochDay)
        assertTrue(range.subtitle.contains("20 days"))
    }

    @Test
    fun testStepFinancialMonth_clampsLeapYears() {
        // 2024 is a leap year (Feb has 29 days).
        val jan31 = LocalDate.of(2024, 1, 31).toEpochDay()
        val steppedFebEpoch = StatsPeriodCalculator.stepFinancialMonth(
            currentReferenceEpochDay = jan31,
            monthStartDay = 31,
            deltaMonths = 1
        )

        val steppedDate = LocalDate.ofEpochDay(steppedFebEpoch)
        assertEquals("Feb should clamp to 29th in leap year", LocalDate.of(2024, 2, 29), steppedDate)
    }
}

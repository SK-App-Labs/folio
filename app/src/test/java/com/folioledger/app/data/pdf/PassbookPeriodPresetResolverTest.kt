package com.folioledger.app.data.pdf

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PassbookPeriodPresetResolverTest {

    private val fixedToday = LocalDate.of(2026, 11, 7)

    @Test
    fun `CURRENT_MONTH resolves from start of current month to today`() {
        val (start, end) = PassbookPeriodPresetResolver.resolvePresetRange(
            PassbookPeriodPreset.CURRENT_MONTH,
            fixedToday
        )
        assertEquals(LocalDate.of(2026, 11, 1).toEpochDay(), start)
        assertEquals(LocalDate.of(2026, 11, 7).toEpochDay(), end)
    }

    @Test
    fun `LAST_MONTH resolves full previous calendar month`() {
        val (start, end) = PassbookPeriodPresetResolver.resolvePresetRange(
            PassbookPeriodPreset.LAST_MONTH,
            fixedToday
        )
        assertEquals(LocalDate.of(2026, 10, 1).toEpochDay(), start)
        assertEquals(LocalDate.of(2026, 10, 31).toEpochDay(), end)
    }

    @Test
    fun `LAST_THREE_MONTHS resolves three months ago first day to today`() {
        val (start, end) = PassbookPeriodPresetResolver.resolvePresetRange(
            PassbookPeriodPreset.LAST_THREE_MONTHS,
            fixedToday
        )
        assertEquals(LocalDate.of(2026, 8, 1).toEpochDay(), start)
        assertEquals(LocalDate.of(2026, 11, 7).toEpochDay(), end)
    }

    @Test
    fun `YEAR_TO_DATE resolves Jan 1 of current year to today`() {
        val (start, end) = PassbookPeriodPresetResolver.resolvePresetRange(
            PassbookPeriodPreset.YEAR_TO_DATE,
            fixedToday
        )
        assertEquals(LocalDate.of(2026, 1, 1).toEpochDay(), start)
        assertEquals(LocalDate.of(2026, 11, 7).toEpochDay(), end)
    }

    @Test
    fun `ALL_TIME resolves from year 2000 to today`() {
        val (start, end) = PassbookPeriodPresetResolver.resolvePresetRange(
            PassbookPeriodPreset.ALL_TIME,
            fixedToday
        )
        assertEquals(LocalDate.of(2000, 1, 1).toEpochDay(), start)
        assertEquals(LocalDate.of(2026, 11, 7).toEpochDay(), end)
    }

    @Test
    fun `CUSTOM_RANGE defaults to one month ago to today`() {
        val (start, end) = PassbookPeriodPresetResolver.resolvePresetRange(
            PassbookPeriodPreset.CUSTOM_RANGE,
            fixedToday
        )
        assertEquals(LocalDate.of(2026, 10, 7).toEpochDay(), start)
        assertEquals(LocalDate.of(2026, 11, 7).toEpochDay(), end)
    }
}

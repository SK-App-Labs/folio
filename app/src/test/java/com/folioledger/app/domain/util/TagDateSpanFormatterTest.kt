package com.folioledger.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TagDateSpanFormatterTest {

    @Test
    fun `null epoch days return Active`() {
        assertEquals("Active", TagDateSpanFormatter.formatSpan(null, null))
        assertEquals("Active", TagDateSpanFormatter.formatSpan(100L, null))
        assertEquals("Active", TagDateSpanFormatter.formatSpan(null, 200L))
    }

    @Test
    fun `identical epoch days return single formatted date`() {
        val day = LocalDate.of(2026, 10, 15).toEpochDay()
        assertEquals("15 Oct", TagDateSpanFormatter.formatSpan(day, day))
    }

    @Test
    fun `distinct epoch days return range string`() {
        val startDay = LocalDate.of(2026, 10, 15).toEpochDay()
        val endDay = LocalDate.of(2026, 10, 22).toEpochDay()
        assertEquals("15 Oct – 22 Oct", TagDateSpanFormatter.formatSpan(startDay, endDay))
    }
}

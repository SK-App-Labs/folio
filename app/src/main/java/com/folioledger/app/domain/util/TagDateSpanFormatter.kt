package com.folioledger.app.domain.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pure domain utility for formatting date bounds of project, trip, or event passbooks.
 */
object TagDateSpanFormatter {

    private val shortDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM", Locale.US)

    /**
     * Formats min and max epoch days into a concise range string:
     * - null inputs -> "Active"
     * - same day -> "15 Oct"
     * - distinct days -> "15 Oct – 22 Oct"
     */
    fun formatSpan(minEpochDay: Long?, maxEpochDay: Long?): String {
        if (minEpochDay == null || maxEpochDay == null) return "Active"
        val start = LocalDate.ofEpochDay(minEpochDay).format(shortDateFormatter)
        val end = LocalDate.ofEpochDay(maxEpochDay).format(shortDateFormatter)
        return if (minEpochDay == maxEpochDay) start else "$start – $end"
    }
}

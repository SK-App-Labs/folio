package com.folioledger.app.domain.util

import com.folioledger.app.domain.model.RepeatFrequency
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object RecurringScheduleCalculator {

    private val previewDateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
    private val annualDateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)

    /**
     * Calculates the next calendar occurrence from [currentDate] given the [frequency].
     */
    fun computeNextOccurrence(currentDate: LocalDate, frequency: RepeatFrequency): LocalDate {
        return when (frequency) {
            RepeatFrequency.EVERY_DAY -> currentDate.plusDays(1)

            RepeatFrequency.WEEKDAYS -> {
                var next = currentDate.plusDays(1)
                while (next.dayOfWeek == DayOfWeek.SATURDAY || next.dayOfWeek == DayOfWeek.SUNDAY) {
                    next = next.plusDays(1)
                }
                next
            }

            RepeatFrequency.WEEKENDS -> {
                var next = currentDate.plusDays(1)
                while (next.dayOfWeek != DayOfWeek.SATURDAY && next.dayOfWeek != DayOfWeek.SUNDAY) {
                    next = next.plusDays(1)
                }
                next
            }

            RepeatFrequency.EVERY_WEEK -> currentDate.plusWeeks(1)

            RepeatFrequency.EVERY_2_WEEKS -> currentDate.plusWeeks(2)

            RepeatFrequency.EVERY_4_WEEKS -> currentDate.plusDays(28)

            RepeatFrequency.EVERY_MONTH -> currentDate.plusMonths(1)

            RepeatFrequency.END_OF_MONTH -> currentDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth())

            RepeatFrequency.EVERY_2_MONTHS -> currentDate.plusMonths(2)

            RepeatFrequency.EVERY_3_MONTHS -> currentDate.plusMonths(3)

            RepeatFrequency.EVERY_4_MONTHS -> currentDate.plusMonths(4)

            RepeatFrequency.EVERY_6_MONTHS -> currentDate.plusMonths(6)

            RepeatFrequency.ANNUALLY -> currentDate.plusYears(1)
        }
    }

    /**
     * Projects all recurring dates falling between [todayEpochDay] and [horizonEpochDay] inclusive.
     */
    fun projectOccurrencesInHorizon(
        startDay: Long,
        frequency: RepeatFrequency,
        todayEpochDay: Long,
        horizonEpochDay: Long
    ): List<Long> {
        val occurrences = mutableListOf<Long>()
        var cursor = LocalDate.ofEpochDay(startDay)

        // Advance cursor until it reaches or exceeds today
        while (cursor.toEpochDay() < todayEpochDay) {
            cursor = computeNextOccurrence(cursor, frequency)
        }

        while (cursor.toEpochDay() <= horizonEpochDay) {
            occurrences.add(cursor.toEpochDay())
            cursor = computeNextOccurrence(cursor, frequency)
        }

        return occurrences
    }

    /**
     * Formats a friendly human-readable sentence describing the recurrence rule.
     */
    fun formatHumanReadableSummary(frequency: RepeatFrequency, baseDate: LocalDate): String {
        return when (frequency) {
            RepeatFrequency.EVERY_DAY -> "Repeats every day"
            RepeatFrequency.WEEKDAYS -> "Repeats every weekday (Mon–Fri)"
            RepeatFrequency.WEEKENDS -> "Repeats every weekend (Sat & Sun)"
            RepeatFrequency.EVERY_WEEK -> {
                val dayName = baseDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                "Repeats every week on $dayName"
            }
            RepeatFrequency.EVERY_2_WEEKS -> {
                val dayName = baseDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                "Repeats every 2 weeks on $dayName"
            }
            RepeatFrequency.EVERY_4_WEEKS -> "Repeats every 4 weeks (28 days)"
            RepeatFrequency.EVERY_MONTH -> "Repeats every month on the ${formatOrdinal(baseDate.dayOfMonth)}"
            RepeatFrequency.END_OF_MONTH -> "Repeats on the last day of each month"
            RepeatFrequency.EVERY_2_MONTHS -> "Repeats every 2 months on the ${formatOrdinal(baseDate.dayOfMonth)}"
            RepeatFrequency.EVERY_3_MONTHS -> "Repeats every 3 months (Quarterly)"
            RepeatFrequency.EVERY_4_MONTHS -> "Repeats every 4 months"
            RepeatFrequency.EVERY_6_MONTHS -> "Repeats every 6 months (Half-yearly)"
            RepeatFrequency.ANNUALLY -> "Repeats annually on ${baseDate.format(annualDateFormatter)}"
        }
    }

    /**
     * Generates a preview string of upcoming dates: e.g. "Next: 30 Nov, 31 Dec, 31 Jan".
     */
    fun formatNextOccurrencesPreview(frequency: RepeatFrequency, baseDate: LocalDate, count: Int = 3): String {
        val dates = mutableListOf<String>()
        var cursor = baseDate
        repeat(count) {
            cursor = computeNextOccurrence(cursor, frequency)
            dates.add(cursor.format(previewDateFormatter))
        }
        return "Next: " + dates.joinToString(", ")
    }

    private fun formatOrdinal(day: Int): String {
        return when {
            day in 11..13 -> "${day}th"
            day % 10 == 1 -> "${day}st"
            day % 10 == 2 -> "${day}nd"
            day % 10 == 3 -> "${day}rd"
            else -> "${day}th"
        }
    }
}

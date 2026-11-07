package com.folioledger.app.data.pdf

import java.time.LocalDate

object PassbookPeriodPresetResolver {

    /**
     * Resolves the start and end epoch day for a given [PassbookPeriodPreset].
     */
    fun resolvePresetRange(
        preset: PassbookPeriodPreset,
        today: LocalDate = LocalDate.now()
    ): Pair<Long, Long> {
        return when (preset) {
            PassbookPeriodPreset.CURRENT_MONTH -> {
                val start = today.withDayOfMonth(1).toEpochDay()
                val end = today.toEpochDay()
                start to end
            }
            PassbookPeriodPreset.LAST_MONTH -> {
                val lastMonth = today.minusMonths(1)
                val start = lastMonth.withDayOfMonth(1).toEpochDay()
                val end = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).toEpochDay()
                start to end
            }
            PassbookPeriodPreset.LAST_THREE_MONTHS -> {
                val start = today.minusMonths(3).withDayOfMonth(1).toEpochDay()
                val end = today.toEpochDay()
                start to end
            }
            PassbookPeriodPreset.YEAR_TO_DATE -> {
                val start = today.withDayOfYear(1).toEpochDay()
                val end = today.toEpochDay()
                start to end
            }
            PassbookPeriodPreset.ALL_TIME -> {
                val start = LocalDate.of(2000, 1, 1).toEpochDay()
                val end = today.toEpochDay()
                start to end
            }
            PassbookPeriodPreset.CUSTOM_RANGE -> {
                today.minusMonths(1).toEpochDay() to today.toEpochDay()
            }
        }
    }
}

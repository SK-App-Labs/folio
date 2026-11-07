package com.folioledger.app.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object BackupTimeFormatter {

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    /**
     * Formats an epoch millisecond timestamp into a user-friendly relative backup label:
     * - null or non-positive -> "Never"
     * - today -> "Today, 10:30 AM"
     * - yesterday -> "Yesterday, 3:15 PM"
     * - 2..6 days ago -> "3d ago"
     * - 7+ days ago -> "15 Oct 2026"
     */
    fun formatBackupRelativeTime(
        epochMillis: Long?,
        zoneId: ZoneId = ZoneId.systemDefault(),
        referenceToday: LocalDate = LocalDate.now(zoneId)
    ): String {
        if (epochMillis == null || epochMillis <= 0L) return "Never"
        val instant = Instant.ofEpochMilli(epochMillis)
        val backupDate = instant.atZone(zoneId).toLocalDate()
        val timeStr = instant.atZone(zoneId).format(timeFormatter)

        return when {
            backupDate == referenceToday -> "Today, $timeStr"
            backupDate == referenceToday.minusDays(1) -> "Yesterday, $timeStr"
            ChronoUnit.DAYS.between(backupDate, referenceToday) in 2..6 -> {
                val days = ChronoUnit.DAYS.between(backupDate, referenceToday)
                "${days}d ago"
            }
            else -> backupDate.format(dateFormatter)
        }
    }
}

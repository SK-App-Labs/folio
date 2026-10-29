package com.folioledger.app.data.backup

import com.folioledger.app.domain.model.AutoBackupRetention
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Thread-safe tracker that records when data is modified in the app.
 * Includes a configurable debounce interval to prevent multiple writes
 * during rapid consecutive app switches.
 */
object SessionChangeTracker {
    private val isDirty = AtomicBoolean(false)
    private val lastBackupEpochMillis = AtomicLong(0L)

    fun markDirty() {
        isDirty.set(true)
    }

    fun isDirty(): Boolean = isDirty.get()

    fun resetDirty() {
        isDirty.set(false)
    }

    fun canBackupNow(minIntervalMillis: Long = 60_000L): Boolean {
        if (!isDirty.get()) return false
        val now = System.currentTimeMillis()
        val last = lastBackupEpochMillis.get()
        return (now - last) >= minIntervalMillis
    }

    fun recordBackupFinished() {
        isDirty.set(false)
        lastBackupEpochMillis.set(System.currentTimeMillis())
    }
}

/**
 * Pure logic engine that parses auto-backup filenames, timestamps,
 * and computes the exact set of files to retain based on the chosen retention policy.
 */
object AutoBackupRetentionEngine {

    data class BackupFileInfo(
        val file: File,
        val epochMillis: Long,
        val localDate: LocalDate,
        val yearMonth: YearMonth
    )

    private val fileNameRegex = Regex("""Folio_AutoBackup_(\d{8}_\d{6})""")
    private val fileNameFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    fun parseBackupFile(file: File, zoneId: ZoneId = ZoneId.systemDefault()): BackupFileInfo? {
        if (!file.isFile || !file.name.endsWith(".zip", ignoreCase = true)) return null
        
        val timestampFromName = runCatching {
            val dateStr = fileNameRegex.find(file.name)?.groupValues?.get(1)
            if (dateStr != null) {
                val ldt = java.time.LocalDateTime.parse(dateStr, fileNameFormatter)
                ldt.atZone(zoneId).toInstant().toEpochMilli()
            } else null
        }.getOrNull()

        val epochMillis = timestampFromName ?: file.lastModified()
        if (epochMillis <= 0L) return null
        val localDate = Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()
        return BackupFileInfo(
            file = file,
            epochMillis = epochMillis,
            localDate = localDate,
            yearMonth = YearMonth.from(localDate)
        )
    }

    fun computeFilesToRetain(
        files: List<File>,
        policy: AutoBackupRetention,
        nowEpochMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Set<File> {
        val parsed = files.mapNotNull { parseBackupFile(it, zoneId) }
            .sortedByDescending { it.epochMillis }
        if (parsed.isEmpty()) return emptySet()

        val today = Instant.ofEpochMilli(nowEpochMillis).atZone(zoneId).toLocalDate()

        return when (policy) {
            AutoBackupRetention.TEN_SESSIONS -> {
                parsed.take(10).map { it.file }.toSet()
            }

            AutoBackupRetention.THIRTY_DAYS -> {
                val retained = mutableSetOf<File>()

                // 1. Intra-day: Keep up to 3 most recent session backups from Today
                val todayBackups = parsed.filter { it.localDate == today }
                retained.addAll(todayBackups.take(3).map { it.file })

                // 2. Daily: Keep 1 newest backup per day for days 1..30 in the past
                val pastDaysBackups = parsed.filter {
                    val daysAgo = ChronoUnit.DAYS.between(it.localDate, today)
                    daysAgo in 1..30
                }
                pastDaysBackups.groupBy { it.localDate }
                    .values
                    .forEach { dayList ->
                        retained.add(dayList.maxByOrNull { it.epochMillis }!!.file)
                    }

                // 3. Monthly Anchors: Keep 1 newest backup for up to 3 months prior to 30 days ago
                val olderMonthlyBackups = parsed.filter {
                    val daysAgo = ChronoUnit.DAYS.between(it.localDate, today)
                    daysAgo > 30 && ChronoUnit.MONTHS.between(it.yearMonth, YearMonth.from(today)) in 1..3
                }
                olderMonthlyBackups.groupBy { it.yearMonth }
                    .values
                    .forEach { monthList ->
                        retained.add(monthList.maxByOrNull { it.epochMillis }!!.file)
                    }

                retained
            }

            AutoBackupRetention.NINETY_DAYS -> {
                val retained = mutableSetOf<File>()

                // 1. Intra-day: Keep up to 3 most recent session backups from Today
                val todayBackups = parsed.filter { it.localDate == today }
                retained.addAll(todayBackups.take(3).map { it.file })

                // 2. Daily: Keep 1 newest backup per day for days 1..90 in the past
                val pastDaysBackups = parsed.filter {
                    val daysAgo = ChronoUnit.DAYS.between(it.localDate, today)
                    daysAgo in 1..90
                }
                pastDaysBackups.groupBy { it.localDate }
                    .values
                    .forEach { dayList ->
                        retained.add(dayList.maxByOrNull { it.epochMillis }!!.file)
                    }

                retained
            }
        }
    }

    fun pruneDirectory(
        directory: File,
        policy: AutoBackupRetention,
        nowEpochMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Int {
        if (!directory.exists() || !directory.isDirectory) return 0
        val allFiles = directory.listFiles { file ->
            file.isFile && file.name.startsWith("Folio_AutoBackup_") && file.name.endsWith(".zip", ignoreCase = true)
        }?.toList() ?: return 0

        val toRetain = computeFilesToRetain(allFiles, policy, nowEpochMillis, zoneId)
        var deletedCount = 0
        allFiles.forEach { file ->
            if (file !in toRetain) {
                if (file.delete()) {
                    deletedCount++
                }
            }
        }
        return deletedCount
    }
}

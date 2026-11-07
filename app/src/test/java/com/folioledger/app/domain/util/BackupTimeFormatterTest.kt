package com.folioledger.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class BackupTimeFormatterTest {

    private val fixedZone = ZoneId.of("UTC")
    private val fixedToday = LocalDate.of(2026, 11, 7)

    @Test
    fun `null and non-positive timestamps return Never`() {
        assertEquals("Never", BackupTimeFormatter.formatBackupRelativeTime(null, fixedZone, fixedToday))
        assertEquals("Never", BackupTimeFormatter.formatBackupRelativeTime(0L, fixedZone, fixedToday))
        assertEquals("Never", BackupTimeFormatter.formatBackupRelativeTime(-500L, fixedZone, fixedToday))
    }

    @Test
    fun `today timestamp returns Today with time`() {
        val todayTime = fixedToday.atTime(LocalTime.of(14, 30))
            .atZone(fixedZone)
            .toInstant()
            .toEpochMilli()

        val result = BackupTimeFormatter.formatBackupRelativeTime(todayTime, fixedZone, fixedToday)
        org.junit.Assert.assertTrue(
            "Expected 'Today, 2:30 PM' (case insensitive), got '$result'",
            result.equals("Today, 2:30 PM", ignoreCase = true)
        )
    }

    @Test
    fun `yesterday timestamp returns Yesterday with time`() {
        val yesterdayTime = fixedToday.minusDays(1).atTime(LocalTime.of(9, 15))
            .atZone(fixedZone)
            .toInstant()
            .toEpochMilli()

        val result = BackupTimeFormatter.formatBackupRelativeTime(yesterdayTime, fixedZone, fixedToday)
        org.junit.Assert.assertTrue(
            "Expected 'Yesterday, 9:15 AM' (case insensitive), got '$result'",
            result.equals("Yesterday, 9:15 AM", ignoreCase = true)
        )
    }

    @Test
    fun `two to six days ago returns relative days`() {
        val threeDaysAgo = fixedToday.minusDays(3).atTime(LocalTime.of(10, 0))
            .atZone(fixedZone)
            .toInstant()
            .toEpochMilli()

        val result = BackupTimeFormatter.formatBackupRelativeTime(threeDaysAgo, fixedZone, fixedToday)
        assertEquals("3d ago", result)

        val sixDaysAgo = fixedToday.minusDays(6).atTime(LocalTime.of(10, 0))
            .atZone(fixedZone)
            .toInstant()
            .toEpochMilli()

        val result6 = BackupTimeFormatter.formatBackupRelativeTime(sixDaysAgo, fixedZone, fixedToday)
        assertEquals("6d ago", result6)
    }

    @Test
    fun `seven or more days ago returns formatted date`() {
        val tenDaysAgo = fixedToday.minusDays(10).atTime(LocalTime.of(10, 0))
            .atZone(fixedZone)
            .toInstant()
            .toEpochMilli()

        val result = BackupTimeFormatter.formatBackupRelativeTime(tenDaysAgo, fixedZone, fixedToday)
        assertEquals("28 Oct 2026", result)
    }
}

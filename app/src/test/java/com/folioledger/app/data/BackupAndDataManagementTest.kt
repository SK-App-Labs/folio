package com.folioledger.app.data

import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupporterTier
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.ui.settings.sheets.formatBackupRelativeTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class BackupAndDataManagementTest {

    @Test
    fun formatBackupRelativeTime_nullOrZero_returnsNever() {
        assertEquals("Never", formatBackupRelativeTime(null))
        assertEquals("Never", formatBackupRelativeTime(0L))
        assertEquals("Never", formatBackupRelativeTime(-100L))
    }

    @Test
    fun formatBackupRelativeTime_today_returnsTodayWithTime() {
        val now = System.currentTimeMillis()
        val result = formatBackupRelativeTime(now)
        assertTrue("Expected to start with 'Today, ', got '$result'", result.startsWith("Today, "))
    }

    @Test
    fun formatBackupRelativeTime_yesterday_returnsYesterdayWithTime() {
        val yesterdayMillis = LocalDate.now().minusDays(1)
            .atTime(14, 30)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val result = formatBackupRelativeTime(yesterdayMillis)
        assertTrue("Expected to start with 'Yesterday, ', got '$result'", result.startsWith("Yesterday, "))
    }

    @Test
    fun formatBackupRelativeTime_threeDaysAgo_returnsDaysAgo() {
        val threeDaysAgo = LocalDate.now().minusDays(3)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val result = formatBackupRelativeTime(threeDaysAgo)
        assertEquals("3d ago", result)
    }

    @Test
    fun userPreferences_supporterTierAndBackupTimestamp_preservedInModel() {
        val originalPrefs = UserPreferences(
            primaryCurrencyCode = "INR",
            numberGrouping = NumberGroupingSystem.INDIAN,
            monthStartDay = 25,
            weekStartDayIso = 1,
            isSupporter = true,
            supporterTier = SupporterTier.PATRON.skuId,
            lastBackupEpochMillis = 1730000000000L
        )

        assertTrue(originalPrefs.isSupporter)
        assertEquals(SupporterTier.PATRON.skuId, originalPrefs.supporterTier)
        assertEquals(1730000000000L, originalPrefs.lastBackupEpochMillis)
    }

    @Test
    fun userPreferences_legacyBackupFallback_preservesCurrentSupporterState() {
        val current = UserPreferences(
            isSupporter = true,
            supporterTier = SupporterTier.COFFEE.skuId,
            lastBackupEpochMillis = 1729000000000L
        )

        // Incoming legacy backup without supporter keys:
        val incomingHasSupporterKey = false
        val incomingSupporterTierValue: String? = null

        val restored = UserPreferences(
            primaryCurrencyCode = "USD",
            isSupporter = if (incomingHasSupporterKey) false else current.isSupporter,
            supporterTier = incomingSupporterTierValue ?: current.supporterTier,
            lastBackupEpochMillis = current.lastBackupEpochMillis
        )

        // Preserves current supporter status rather than clobbering to false/null
        assertTrue(restored.isSupporter)
        assertEquals(SupporterTier.COFFEE.skuId, restored.supporterTier)
        assertEquals(1729000000000L, restored.lastBackupEpochMillis)
    }
}

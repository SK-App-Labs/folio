// File: app/src/main/java/com/folioledger/app/data/notification/DueRemindersScheduler.kt
package com.folioledger.app.data.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object DueRemindersScheduler {
    const val UNIQUE_WORK_NAME = "folio_due_reminders_worker"

    fun scheduleDailyReminders(
        context: Context,
        hourOfDay: Int = 9,
        minute: Int = 0
    ) {
        val now = LocalDateTime.now()
        var target = now.withHour(hourOfDay.coerceIn(0, 23))
            .withMinute(minute.coerceIn(0, 59))
            .withSecond(0)
            .withNano(0)

        if (now.isAfter(target)) {
            target = target.plusDays(1)
        }

        val initialDelayMillis = Duration.between(now, target).toMillis().coerceAtLeast(0L)

        val workRequest = PeriodicWorkRequestBuilder<DueRemindersWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .addTag("folio_due_reminders")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelDailyReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}

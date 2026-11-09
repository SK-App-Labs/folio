// File: app/src/main/java/com/folioledger/app/FolioApplication.kt
package com.folioledger.app

import android.app.Application
import androidx.room.InvalidationTracker
import com.folioledger.app.data.backup.SessionChangeTracker
import com.folioledger.app.data.notification.DueRemindersScheduler
import com.folioledger.app.data.notification.FolioNotificationManager
import com.folioledger.app.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FolioApplication : Application() {
    lateinit var container: AppContainer
        private set

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Initialize notification channels
        FolioNotificationManager.createNotificationChannels(this)

        // Attach Room InvalidationTracker to automatically mark session dirty on any database write
        container.database.invalidationTracker.addObserver(
            object : InvalidationTracker.Observer(
                "accounts",
                "categories",
                "transactions",
                "transaction_splits",
                "tags",
                "budgets",
                "recurring_rules",
                "bookmarks",
                "installment_plans"
            ) {
                override fun onInvalidated(tables: Set<String>) {
                    SessionChangeTracker.markDirty()
                }
            }
        )

        // Automatically process due recurring commitments on launch & ensure worker is scheduled if enabled
        applicationScope.launch {
            try {
                container.analyticsAndBudgetRepository.processDueRecurringRulesOnce(
                    todayEpochDay = java.time.LocalDate.now().toEpochDay()
                )
            } catch (e: Exception) {
                android.util.Log.e("FolioApplication", "Failed to process due recurring rules on startup", e)
            }

            try {
                val prefs = container.settingsAndBackupRepository.observeUserPreferences().first()
                if (prefs.remindersEnabled) {
                    DueRemindersScheduler.scheduleDailyReminders(
                        context = this@FolioApplication,
                        hourOfDay = prefs.reminderHourOfDay,
                        minute = prefs.reminderMinute
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("FolioApplication", "Failed to schedule due reminders on startup", e)
            }
        }
    }
}
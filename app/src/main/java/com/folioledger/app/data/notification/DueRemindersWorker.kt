// File: app/src/main/java/com/folioledger/app/data/notification/DueRemindersWorker.kt
package com.folioledger.app.data.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.folioledger.app.FolioApplication
import com.folioledger.app.domain.util.DueRemindersEvaluator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate

class DueRemindersWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val app = applicationContext as? FolioApplication ?: return@withContext Result.failure()
        val container = app.container

        val prefs = container.settingsAndBackupRepository.observeUserPreferences().first()
        if (!prefs.remindersEnabled) {
            return@withContext Result.success()
        }

        val todayEpochDay = LocalDate.now().toEpochDay()
        val accounts = container.accountRepository.observeActiveAccounts(todayEpochDay).first()
        val recurringRules = container.analyticsAndBudgetRepository.observeActiveRecurringCommitments().first()

        val cardReminders = DueRemindersEvaluator.evaluateCreditCardReminders(
            accounts = accounts,
            todayEpochDay = todayEpochDay,
            leadDaysNotice = prefs.ccReminderLeadDays
        )

        val billReminders = DueRemindersEvaluator.evaluateBillReminders(
            recurringRules = recurringRules,
            todayEpochDay = todayEpochDay,
            leadDaysNotice = prefs.billReminderLeadDays
        )

        if (cardReminders.isNotEmpty() || billReminders.isNotEmpty()) {
            val notificationManager = FolioNotificationManager(applicationContext)
            notificationManager.postDueReminders(
                cardReminders = cardReminders,
                billReminders = billReminders,
                hideAmounts = prefs.hideAmountsOnLockScreen,
                primaryCurrencyCode = prefs.primaryCurrencyCode,
                grouping = prefs.numberGrouping
            )
        }

        Result.success()
    }
}

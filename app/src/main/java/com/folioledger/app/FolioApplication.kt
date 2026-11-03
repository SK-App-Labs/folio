// File: app/src/main/java/com/folioledger/app/FolioApplication.kt
package com.folioledger.app

import android.app.Application
import androidx.room.InvalidationTracker
import com.folioledger.app.data.backup.SessionChangeTracker
import com.folioledger.app.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FolioApplication : Application() {
    lateinit var container: AppContainer
        private set

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

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

        // Automatically process due recurring commitments on launch
        applicationScope.launch {
            try {
                container.analyticsAndBudgetRepository.processDueRecurringRulesOnce(
                    todayEpochDay = java.time.LocalDate.now().toEpochDay()
                )
            } catch (e: Exception) {
                android.util.Log.e("FolioApplication", "Failed to process due recurring rules on startup", e)
            }
        }
    }
}
// File: app/src/main/java/com/folioledger/app/di/FolioViewModelFactory.kt
package com.folioledger.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.folioledger.app.ui.accounts.AccountsViewModel
import com.folioledger.app.ui.entry.TransactionEntryViewModel
import com.folioledger.app.ui.ledger.LedgerHomeViewModel
import com.folioledger.app.ui.settings.SettingsViewModel
import com.folioledger.app.ui.stats.StatsAndBudgetsViewModel

class FolioViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LedgerHomeViewModel::class.java) -> {
                LedgerHomeViewModel(
                    transactionRepository = container.transactionRepository,
                    analyticsAndBudgetRepository = container.analyticsAndBudgetRepository,
                    accountRepository = container.accountRepository,
                    categoryAndTagRepository = container.categoryAndTagRepository,
                    settingsAndBackupRepository = container.settingsAndBackupRepository
                ) as T
            }
            modelClass.isAssignableFrom(AccountsViewModel::class.java) -> {
                AccountsViewModel(
                    accountRepository = container.accountRepository,
                    transactionRepository = container.transactionRepository,
                    settingsAndBackupRepository = container.settingsAndBackupRepository
                ) as T
            }
            modelClass.isAssignableFrom(TransactionEntryViewModel::class.java) -> {
                TransactionEntryViewModel(
                    transactionRepository = container.transactionRepository,
                    accountRepository = container.accountRepository,
                    categoryAndTagRepository = container.categoryAndTagRepository,
                    settingsAndBackupRepository = container.settingsAndBackupRepository
                ) as T
            }
            modelClass.isAssignableFrom(StatsAndBudgetsViewModel::class.java) -> {
                StatsAndBudgetsViewModel(
                    analyticsAndBudgetRepository = container.analyticsAndBudgetRepository,
                    categoryAndTagRepository = container.categoryAndTagRepository,
                    transactionRepository = container.transactionRepository,
                    settingsAndBackupRepository = container.settingsAndBackupRepository,
                    accountRepository = container.accountRepository
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    settingsAndBackupRepository = container.settingsAndBackupRepository,
                    categoryAndTagRepository = container.categoryAndTagRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
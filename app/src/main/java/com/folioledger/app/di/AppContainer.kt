// File: app/src/main/java/com/folioledger/app/di/AppContainer.kt
package com.folioledger.app.di

import android.content.Context
import com.folioledger.app.data.local.FolioDatabase
import com.folioledger.app.data.repository.AccountRepositoryImpl
import com.folioledger.app.data.repository.AnalyticsAndBudgetRepositoryImpl
import com.folioledger.app.data.repository.CategoryAndTagRepositoryImpl
import com.folioledger.app.data.repository.SettingsAndBackupRepositoryImpl
import com.folioledger.app.data.repository.TransactionRepositoryImpl
import com.folioledger.app.domain.repository.AccountRepository
import com.folioledger.app.domain.repository.AnalyticsAndBudgetRepository
import com.folioledger.app.domain.repository.CategoryAndTagRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import com.folioledger.app.domain.repository.TransactionRepository

class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    val database: FolioDatabase by lazy {
        FolioDatabase.getInstance(appContext)
    }

    val settingsAndBackupRepository: SettingsAndBackupRepository by lazy {
        SettingsAndBackupRepositoryImpl(
            context = appContext,
            database = database
        )
    }

    val accountRepository: AccountRepository by lazy {
        AccountRepositoryImpl(
            accountDao = database.accountDao(),
            transactionDao = database.transactionDao(),
            settingsRepository = settingsAndBackupRepository
        )
    }

    val categoryAndTagRepository: CategoryAndTagRepository by lazy {
        CategoryAndTagRepositoryImpl(
            categoryAndTagDao = database.categoryAndTagDao()
        )
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImpl(
            transactionDao = database.transactionDao(),
            accountDao = database.accountDao(),
            categoryAndTagDao = database.categoryAndTagDao()
        )
    }

    val analyticsAndBudgetRepository: AnalyticsAndBudgetRepository by lazy {
        AnalyticsAndBudgetRepositoryImpl(
            transactionDao = database.transactionDao(),
            categoryAndTagDao = database.categoryAndTagDao(),
            budgetAndRecurringDao = database.budgetAndRecurringDao()
        )
    }
}
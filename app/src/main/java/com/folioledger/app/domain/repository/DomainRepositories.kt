// File: app/src/main/java/com/folioledger/app/domain/repository/DomainRepositories.kt
package com.folioledger.app.domain.repository

import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.BudgetProgress
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.NetWorthOverview
import com.folioledger.app.domain.model.PeriodLedgerSummary
import com.folioledger.app.domain.model.SpendRankingItem
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionSearchFilter
import com.folioledger.app.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeNetWorthOverview(referenceDateEpochDay: Long): Flow<NetWorthOverview>
    fun observeActiveAccounts(referenceDateEpochDay: Long): Flow<List<AccountSummary>>
    fun observeAllAccounts(referenceDateEpochDay: Long): Flow<List<AccountSummary>>
    fun observeAccountById(accountId: Long, referenceDateEpochDay: Long): Flow<AccountSummary?>

    suspend fun upsertAccount(
        account: AccountSummary,
        openingBalance: MinorMoney = MinorMoney.ZERO
    ): Result<Long>

    suspend fun setAccountArchived(accountId: Long, isArchived: Boolean): Result<Unit>

    suspend fun settleCreditCardStatement(
        sourceAssetAccountId: Long,
        creditCardAccountId: Long,
        settlementAmount: MinorMoney,
        timestampEpochMillis: Long,
        ledgerDateEpochDay: Long,
        note: String = ""
    ): Result<Long>
}

interface TransactionRepository {
    fun observeTransactionsInRange(
        startEpochDay: Long,
        endEpochDay: Long,
        accountId: Long? = null
    ): Flow<List<TransactionItem>>

    fun observeTransactionsForDay(epochDay: Long): Flow<List<TransactionItem>>

    fun observeTransactionsForAccount(accountId: Long): Flow<List<TransactionItem>>

    fun observeTransactionById(transactionId: Long): Flow<TransactionItem?>

    fun searchAndFilterTransactions(filter: TransactionSearchFilter): Flow<List<TransactionItem>>

    suspend fun upsertSingleTransaction(transaction: TransactionItem): Result<Long>

    suspend fun upsertSplitTransaction(transaction: TransactionItem): Result<Long>

    suspend fun recordTransfer(
        fromAccountId: Long,
        toAccountId: Long,
        amount: MinorMoney,
        transferFee: MinorMoney = MinorMoney.ZERO,
        timestampEpochMillis: Long,
        ledgerDateEpochDay: Long,
        note: String = "",
        tags: List<String> = emptyList()
    ): Result<Long>

    suspend fun recordRefund(
        accountId: Long,
        categoryId: Long?,
        refundAmount: MinorMoney,
        timestampEpochMillis: Long,
        ledgerDateEpochDay: Long,
        originalTransactionId: Long? = null,
        payeeOrDescription: String = "",
        note: String = ""
    ): Result<Long>

    suspend fun createInstallmentPlanTransactions(
        baseTransaction: TransactionItem,
        totalInstallmentAmount: MinorMoney,
        monthsCount: Int,
        startDateEpochDay: Long
    ): Result<List<Long>>

    suspend fun deleteTransaction(transactionId: Long): Result<Unit>
}

interface CategoryAndTagRepository {
    fun observeCategoryTree(type: CategoryType): Flow<List<CategoryNode>>
    fun observeAllCategoryTrees(): Flow<List<CategoryNode>>
    fun observeAllTags(): Flow<List<String>>
    fun observeBookmarks(): Flow<List<TransactionBookmark>>

    suspend fun upsertCategory(category: CategoryNode): Result<Long>
    suspend fun deleteCategory(categoryId: Long, reassignToCategoryId: Long? = null): Result<Unit>

    suspend fun createTag(tagName: String): Result<Unit>
    suspend fun renameTag(oldTagName: String, newTagName: String): Result<Unit>
    suspend fun deleteTag(tagName: String): Result<Unit>

    suspend fun upsertBookmark(bookmark: TransactionBookmark): Result<Long>
    suspend fun deleteBookmark(bookmarkId: Long): Result<Unit>
}

interface AnalyticsAndBudgetRepository {
    fun observePeriodLedgerSummary(
        startEpochDay: Long,
        endEpochDay: Long,
        includeCarryOver: Boolean
    ): Flow<PeriodLedgerSummary>

    fun observeCategorySpendStats(
        startEpochDay: Long,
        endEpochDay: Long,
        type: CategoryType = CategoryType.EXPENSE
    ): Flow<List<CategorySpendStat>>

    fun observeSixMonthTrendBars(
        referenceDateEpochDay: Long,
        monthStartDay: Int
    ): Flow<List<MonthlyTrendBar>>

    fun observeTagSpendRankings(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<SpendRankingItem>>

    fun observePayeeOrNoteRankings(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<SpendRankingItem>>

    fun observeBudgetProgressList(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<BudgetProgress>>

    suspend fun upsertBudget(
        budgetId: Long = 0L,
        categoryId: Long?,
        limitAmount: MinorMoney,
        alertThresholdPercent: Int = 80
    ): Result<Long>

    suspend fun deleteBudget(budgetId: Long): Result<Unit>
}

interface SettingsAndBackupRepository {
    fun observeUserPreferences(): Flow<UserPreferences>

    suspend fun updateUserPreferences(
        transform: (UserPreferences) -> UserPreferences
    ): Result<Unit>

    suspend fun exportTransactionsToCsv(
        destinationUriString: String,
        startEpochDay: Long? = null,
        endEpochDay: Long? = null
    ): Result<Int>

    suspend fun createPortableZipBackup(destinationUriString: String): Result<String>

    suspend fun restoreFromZipBackup(sourceUriString: String): Result<Unit>
}
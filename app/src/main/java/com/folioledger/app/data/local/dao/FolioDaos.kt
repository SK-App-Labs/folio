// File: app/src/main/java/com/folioledger/app/data/local/dao/FolioDaos.kt
package com.folioledger.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.folioledger.app.data.local.entity.AccountEntity
import com.folioledger.app.data.local.entity.BookmarkEntity
import com.folioledger.app.data.local.entity.BudgetEntity
import com.folioledger.app.data.local.entity.CategoryEntity
import com.folioledger.app.data.local.entity.InstallmentPlanEntity
import com.folioledger.app.data.local.entity.RecurringRuleEntity
import com.folioledger.app.data.local.entity.TagEntity
import com.folioledger.app.data.local.entity.TransactionEntity
import com.folioledger.app.data.local.entity.TransactionSplitEntity
import com.folioledger.app.data.local.entity.TransactionTagCrossRef
import com.folioledger.app.data.local.entity.TransactionWithDetails
import com.folioledger.app.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY isArchived ASC, sortOrder ASC, id ASC")
    fun observeAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY sortOrder ASC, id ASC")
    fun observeActiveAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    fun observeAccountById(accountId: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts ORDER BY isArchived ASC, sortOrder ASC, id ASC")
    suspend fun getAllAccountsOnce(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    suspend fun getAccountByIdOnce(accountId: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>): List<Long>

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET isArchived = :isArchived WHERE id = :accountId")
    suspend fun setAccountArchived(accountId: Long, isArchived: Boolean)

    @Query("DELETE FROM accounts WHERE id = :accountId")
    suspend fun deleteAccountById(accountId: Long)

    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()
}

@Dao
interface CategoryAndTagDao {

    @Query("SELECT * FROM categories WHERE type = :type AND isArchived = 0 ORDER BY sortOrder ASC, id ASC")
    fun observeCategoriesByType(type: CategoryType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isArchived = 0 ORDER BY type ASC, sortOrder ASC, id ASC")
    fun observeAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY type ASC, sortOrder ASC, id ASC")
    suspend fun getAllCategoriesOnce(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun getCategoryByIdOnce(categoryId: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countCategories(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>): List<Long>

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Long)

    @Query("UPDATE transactions SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignTransactionsCategory(oldCategoryId: Long, newCategoryId: Long?)

    @Query("UPDATE transaction_splits SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignSplitsCategory(oldCategoryId: Long, newCategoryId: Long)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE ASC")
    fun observeAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllTagsOnce(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getTagByNameOnce(name: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>): List<Long>

    @Query("UPDATE tags SET name = :newName WHERE LOWER(name) = LOWER(:oldName)")
    suspend fun renameTag(oldName: String, newName: String)

    @Query("DELETE FROM tags WHERE LOWER(name) = LOWER(:tagName)")
    suspend fun deleteTagByName(tagName: String)

    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()

    @Query("SELECT * FROM bookmarks ORDER BY sortOrder ASC, id ASC")
    fun observeAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllBookmarksOnce(): List<BookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(bookmark: BookmarkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarks(bookmarks: List<BookmarkEntity>): List<Long>

    @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
    suspend fun deleteBookmarkById(bookmarkId: Long)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAllBookmarks()
}

@Dao
interface TransactionDao {

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE ledgerDateEpochDay BETWEEN :startDay AND :endDay
        ORDER BY ledgerDateEpochDay DESC, timestampEpochMillis DESC, id DESC
        """
    )
    fun observeTransactionsInRange(
        startDay: Long,
        endDay: Long
    ): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE ledgerDateEpochDay BETWEEN :startDay AND :endDay
          AND (accountId = :accountId OR transferToAccountId = :accountId)
        ORDER BY ledgerDateEpochDay DESC, timestampEpochMillis DESC, id DESC
        """
    )
    fun observeTransactionsInRangeForAccount(
        startDay: Long,
        endDay: Long,
        accountId: Long
    ): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE ledgerDateEpochDay = :epochDay
        ORDER BY timestampEpochMillis DESC, id DESC
        """
    )
    fun observeTransactionsForDay(epochDay: Long): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE accountId = :accountId OR transferToAccountId = :accountId
        ORDER BY ledgerDateEpochDay DESC, timestampEpochMillis DESC, id DESC
        """
    )
    fun observeTransactionsForAccount(accountId: Long): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        ORDER BY ledgerDateEpochDay DESC, timestampEpochMillis DESC, id DESC
        """
    )
    fun observeAllTransactions(): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    fun observeTransactionById(transactionId: Long): Flow<TransactionWithDetails?>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionByIdOnce(transactionId: Long): TransactionWithDetails?

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        ORDER BY ledgerDateEpochDay DESC, timestampEpochMillis DESC, id DESC
        """
    )
    suspend fun getAllTransactionsWithDetailsOnce(): List<TransactionWithDetails>

    @Query("SELECT * FROM transactions ORDER BY id ASC")
    suspend fun getAllTransactionsRawOnce(): List<TransactionEntity>

    @Query("SELECT * FROM transaction_splits ORDER BY id ASC")
    suspend fun getAllSplitsOnce(): List<TransactionSplitEntity>

    @Query("SELECT * FROM transaction_tags")
    suspend fun getAllTagCrossRefsOnce(): List<TransactionTagCrossRef>

    @Query("SELECT * FROM installment_plans ORDER BY id ASC")
    suspend fun getAllInstallmentPlansOnce(): List<InstallmentPlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionRaw(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionsRaw(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun updateTransactionRaw(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplits(splits: List<TransactionSplitEntity>): List<Long>

    @Query("DELETE FROM transaction_splits WHERE transactionId = :transactionId")
    suspend fun deleteSplitsForTransaction(transactionId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagIgnore(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagCrossRefs(crossRefs: List<TransactionTagCrossRef>)

    @Query("DELETE FROM transaction_tags WHERE transactionId = :transactionId")
    suspend fun deleteTagCrossRefsForTransaction(transactionId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallmentPlanRaw(plan: InstallmentPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallmentPlansRaw(plans: List<InstallmentPlanEntity>): List<Long>

    @Transaction
    suspend fun upsertTransactionWithDetails(
        transaction: TransactionEntity,
        splits: List<TransactionSplitEntity>,
        tagNames: List<String>
    ): Long {
        val persistedTransactionId = insertTransactionRaw(transaction)

        deleteSplitsForTransaction(persistedTransactionId)
        if (splits.isNotEmpty()) {
            val boundSplits = splits.map { split ->
                split.copy(id = 0L, transactionId = persistedTransactionId)
            }
            insertSplits(boundSplits)
        }

        deleteTagCrossRefsForTransaction(persistedTransactionId)
        val normalizedTags = tagNames
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }

        if (normalizedTags.isNotEmpty()) {
            val crossRefs = ArrayList<TransactionTagCrossRef>(normalizedTags.size)
            for (tagName in normalizedTags) {
                val insertedId = insertTagIgnore(TagEntity(name = tagName))
                val resolvedTagId = if (insertedId != -1L) {
                    insertedId
                } else {
                    getTagByName(tagName)?.id ?: continue
                }
                crossRefs.add(
                    TransactionTagCrossRef(
                        transactionId = persistedTransactionId,
                        tagId = resolvedTagId
                    )
                )
            }
            if (crossRefs.isNotEmpty()) {
                insertTagCrossRefs(crossRefs)
            }
        }

        return persistedTransactionId
    }

    @Transaction
    suspend fun insertInstallmentPlanWithTransactions(
        plan: InstallmentPlanEntity,
        installmentTransactions: List<TransactionEntity>,
        tagNames: List<String> = emptyList()
    ): List<Long> {
        val planId = insertInstallmentPlanRaw(plan)
        val normalizedTags = tagNames
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }

        val resolvedTagIds = ArrayList<Long>(normalizedTags.size)
        for (tagName in normalizedTags) {
            val insertedId = insertTagIgnore(TagEntity(name = tagName))
            val tagId = if (insertedId != -1L) {
                insertedId
            } else {
                getTagByName(tagName)?.id ?: continue
            }
            resolvedTagIds.add(tagId)
        }

        val insertedIds = ArrayList<Long>(installmentTransactions.size)
        for (tx in installmentTransactions) {
            val txId = insertTransactionRaw(tx.copy(installmentPlanId = planId))
            insertedIds.add(txId)
            if (resolvedTagIds.isNotEmpty()) {
                val crossRefs = resolvedTagIds.map { tagId ->
                    TransactionTagCrossRef(transactionId = txId, tagId = tagId)
                }
                insertTagCrossRefs(crossRefs)
            }
        }
        return insertedIds
    }

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM transaction_splits")
    suspend fun deleteAllSplits()

    @Query("DELETE FROM transaction_tags")
    suspend fun deleteAllTagCrossRefs()

    @Query("DELETE FROM installment_plans")
    suspend fun deleteAllInstallmentPlans()
}

@Dao
interface BudgetAndRecurringDao {

    @Query("SELECT * FROM budgets ORDER BY categoryId IS NOT NULL ASC, id ASC")
    fun observeAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets ORDER BY categoryId IS NOT NULL ASC, id ASC")
    suspend fun getAllBudgetsOnce(): List<BudgetEntity>

    @Query(
        """
        SELECT * FROM budgets
        WHERE (:categoryId IS NULL AND categoryId IS NULL)
           OR (categoryId = :categoryId)
        LIMIT 1
        """
    )
    suspend fun getBudgetByCategoryIdOnce(categoryId: Long?): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>): List<Long>

    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteBudgetById(budgetId: Long)

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()

    @Query("SELECT * FROM recurring_rules ORDER BY isActive DESC, nextOccurrenceEpochDay ASC, id ASC")
    fun observeAllRecurringRules(): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules ORDER BY isActive DESC, nextOccurrenceEpochDay ASC, id ASC")
    suspend fun getAllRecurringRulesOnce(): List<RecurringRuleEntity>

    @Query(
        """
        SELECT * FROM recurring_rules
        WHERE isActive = 1 AND nextOccurrenceEpochDay <= :todayEpochDay
        ORDER BY nextOccurrenceEpochDay ASC, id ASC
        """
    )
    fun observeDueRecurringRules(todayEpochDay: Long): Flow<List<RecurringRuleEntity>>

    @Query(
        """
        SELECT * FROM recurring_rules
        WHERE isActive = 1 AND nextOccurrenceEpochDay <= :todayEpochDay
        ORDER BY nextOccurrenceEpochDay ASC, id ASC
        """
    )
    suspend fun getDueRecurringRulesOnce(todayEpochDay: Long): List<RecurringRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecurringRule(rule: RecurringRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringRules(rules: List<RecurringRuleEntity>): List<Long>

    @Query("DELETE FROM recurring_rules WHERE id = :ruleId")
    suspend fun deleteRecurringRuleById(ruleId: Long)

    @Query("DELETE FROM recurring_rules")
    suspend fun deleteAllRecurringRules()
}
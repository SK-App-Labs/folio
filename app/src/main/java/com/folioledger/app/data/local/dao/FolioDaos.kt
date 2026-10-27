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
import com.folioledger.app.domain.model.TransactionType
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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNewAccountRaw(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>): List<Long>

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInitialTransactionRaw(transaction: TransactionEntity): Long

    /**
     * Safe upsert that uses SQL UPDATE when editing an existing account (id != 0L),
     * preventing OnConflictStrategy.REPLACE from triggering ForeignKey.CASCADE deletes
     * on transactions, installment plans, and bookmarks.
     */
    @Transaction
    suspend fun insertAccount(account: AccountEntity): Long {
        return if (account.id != 0L && getAccountByIdOnce(account.id) != null) {
            updateAccount(account)
            account.id
        } else {
            insertNewAccountRaw(account.copy(id = 0L))
        }
    }

    @Transaction
    suspend fun insertAccountWithInitialBalance(
        account: AccountEntity,
        initialTransaction: TransactionEntity?
    ): Long {
        val newAccountId = insertAccount(account)
        if (initialTransaction != null && initialTransaction.amountMinor > 0L) {
            val validTransferTarget = if (initialTransaction.transferToAccountId != null) newAccountId else null
            insertInitialTransactionRaw(initialTransaction.copy(accountId = newAccountId, transferToAccountId = validTransferTarget))
        }
        return newAccountId
    }

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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNewCategoryRaw(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>): List<Long>

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    /**
     * Safe upsert that uses SQL UPDATE when editing an existing category or subcategory,
     * preventing OnConflictStrategy.REPLACE from cascading deletes to subcategories/budgets
     * or unlinking historical transactions (SET NULL).
     */
    @Transaction
    suspend fun insertCategory(category: CategoryEntity): Long {
        return if (category.id != 0L && getCategoryByIdOnce(category.id) != null) {
            updateCategory(category)
            category.id
        } else {
            insertNewCategoryRaw(category.copy(id = 0L))
        }
    }

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

    @Query("SELECT * FROM bookmarks WHERE id = :bookmarkId LIMIT 1")
    suspend fun getBookmarkByIdOnce(bookmarkId: Long): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNewBookmarkRaw(bookmark: BookmarkEntity): Long

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Transaction
    suspend fun upsertBookmark(bookmark: BookmarkEntity): Long {
        return if (bookmark.id != 0L && getBookmarkByIdOnce(bookmark.id) != null) {
            updateBookmark(bookmark)
            bookmark.id
        } else {
            insertNewBookmarkRaw(bookmark.copy(id = 0L))
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarks(bookmarks: List<BookmarkEntity>): List<Long>

    @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
    suspend fun deleteBookmarkById(bookmarkId: Long)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAllBookmarks()
}

data class TransactionBalanceProjection(
    val id: Long,
    val type: TransactionType,
    val timestampEpochMillis: Long,
    val ledgerDateEpochDay: Long,
    val amountMinor: Long,
    val transferFeeMinor: Long,
    val accountId: Long,
    val transferToAccountId: Long?
)

@Dao
interface TransactionDao {

    @Query(
        """
        SELECT id, type, timestampEpochMillis, ledgerDateEpochDay, amountMinor, transferFeeMinor, accountId, transferToAccountId
        FROM transactions
        ORDER BY ledgerDateEpochDay ASC, timestampEpochMillis ASC, id ASC
        """
    )
    fun observeTransactionBalanceProjections(): Flow<List<TransactionBalanceProjection>>

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

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionEntityByIdOnce(transactionId: Long): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId OR transferToAccountId = :accountId")
    suspend fun getTransactionCountForAccount(accountId: Long): Int

    @Query("DELETE FROM transactions WHERE accountId = :accountId OR transferToAccountId = :accountId")
    suspend fun deleteTransactionsForAccount(accountId: Long): Int

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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNewTransactionRaw(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionsRaw(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun updateTransactionRaw(transaction: TransactionEntity)

    @Transaction
    suspend fun insertTransactionRaw(transaction: TransactionEntity): Long {
        return if (transaction.id != 0L && getTransactionEntityByIdOnce(transaction.id) != null) {
            updateTransactionRaw(transaction)
            transaction.id
        } else {
            insertNewTransactionRaw(transaction.copy(id = 0L))
        }
    }

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

    @Query("SELECT * FROM budgets WHERE id = :budgetId LIMIT 1")
    suspend fun getBudgetByIdOnce(budgetId: Long): BudgetEntity?

    @Query(
        """
        SELECT * FROM budgets
        WHERE (:categoryId IS NULL AND categoryId IS NULL)
           OR (categoryId = :categoryId)
        LIMIT 1
        """
    )
    suspend fun getBudgetByCategoryIdOnce(categoryId: Long?): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNewBudgetRaw(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Transaction
    suspend fun upsertBudget(budget: BudgetEntity): Long {
        return if (budget.id != 0L && getBudgetByIdOnce(budget.id) != null) {
            updateBudget(budget)
            budget.id
        } else {
            insertNewBudgetRaw(budget.copy(id = 0L))
        }
    }

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

    @Query("SELECT * FROM recurring_rules WHERE id = :ruleId LIMIT 1")
    suspend fun getRecurringRuleByIdOnce(ruleId: Long): RecurringRuleEntity?

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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNewRecurringRuleRaw(rule: RecurringRuleEntity): Long

    @Update
    suspend fun updateRecurringRule(rule: RecurringRuleEntity)

    @Transaction
    suspend fun upsertRecurringRule(rule: RecurringRuleEntity): Long {
        return if (rule.id != 0L && getRecurringRuleByIdOnce(rule.id) != null) {
            updateRecurringRule(rule)
            rule.id
        } else {
            insertNewRecurringRuleRaw(rule.copy(id = 0L))
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringRules(rules: List<RecurringRuleEntity>): List<Long>

    @Query("DELETE FROM recurring_rules WHERE id = :ruleId")
    suspend fun deleteRecurringRuleById(ruleId: Long)

    @Query("DELETE FROM recurring_rules")
    suspend fun deleteAllRecurringRules()
}
// File: app/src/main/java/com/folioledger/app/data/repository/TransactionRepositoryImpl.kt
package com.folioledger.app.data.repository

import androidx.room.withTransaction
import com.folioledger.app.data.local.FolioDatabase
import com.folioledger.app.data.local.dao.AccountDao
import com.folioledger.app.data.local.dao.BudgetAndRecurringDao
import com.folioledger.app.data.local.dao.CategoryAndTagDao
import com.folioledger.app.data.local.dao.TransactionDao
import com.folioledger.app.data.local.entity.AccountEntity
import com.folioledger.app.data.local.entity.CategoryEntity
import com.folioledger.app.data.local.entity.InstallmentPlanEntity
import com.folioledger.app.data.local.entity.RecurringRuleEntity
import com.folioledger.app.data.local.entity.TransactionEntity
import com.folioledger.app.data.local.entity.TransactionSplitEntity
import com.folioledger.app.data.local.entity.TransactionWithDetails
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.SplitLineItem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionSearchFilter
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.repository.TransactionRepository
import com.folioledger.app.domain.usecase.AccountingRulesEngine
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.domain.util.RecurringScheduleCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryAndTagDao: CategoryAndTagDao,
    private val budgetAndRecurringDao: BudgetAndRecurringDao,
    private val database: FolioDatabase
) : TransactionRepository {

    override fun observeTransactionsInRange(
        startEpochDay: Long,
        endEpochDay: Long,
        accountId: Long?
    ): Flow<List<TransactionItem>> {
        val rawFlow = if (accountId == null) {
            transactionDao.observeTransactionsInRange(startEpochDay, endEpochDay)
        } else {
            transactionDao.observeTransactionsInRangeForAccount(startEpochDay, endEpochDay, accountId)
        }
        return combineWithMetadata(rawFlow)
    }

    override fun observeTransactionsForDay(epochDay: Long): Flow<List<TransactionItem>> {
        return combineWithMetadata(transactionDao.observeTransactionsForDay(epochDay))
    }

    override fun observeTransactionsForAccount(accountId: Long): Flow<List<TransactionItem>> {
        return combineWithMetadata(transactionDao.observeTransactionsForAccount(accountId))
    }

    override fun observeTransactionById(transactionId: Long): Flow<TransactionItem?> {
        return combine(
            transactionDao.observeTransactionById(transactionId),
            accountDao.observeAllAccounts(),
            categoryAndTagDao.observeAllCategories()
        ) { txWithDetails, accounts, categories ->
            if (txWithDetails == null) {
                null
            } else {
                val accountsById = accounts.associateBy { it.id }
                val categoriesById = categories.associateBy { it.id }
                mapToTransactionItem(txWithDetails, accountsById, categoriesById)
            }
        }
    }

    override fun searchAndFilterTransactions(
        filter: TransactionSearchFilter
    ): Flow<List<TransactionItem>> {
        val rawTxFlow = if (filter.startEpochDay != null && filter.endEpochDay != null) {
            transactionDao.observeTransactionsInRange(filter.startEpochDay, filter.endEpochDay)
        } else {
            transactionDao.observeAllTransactions()
        }
        return combine(
            rawTxFlow,
            accountDao.observeAllAccounts(),
            categoryAndTagDao.observeAllCategories()
        ) { allTransactions, accounts, categories ->
            val accountsById = accounts.associateBy { it.id }
            val categoriesById = categories.associateBy { it.id }

            val expandedCategoryIds: Set<Long> = if (filter.categoryIds.isEmpty()) {
                emptySet()
            } else {
                buildSet {
                    addAll(filter.categoryIds)
                    for (cat in categories) {
                        if (cat.parentId != null && cat.parentId in filter.categoryIds) {
                            add(cat.id)
                        }
                    }
                }
            }

            val normalizedQuery = filter.queryText.trim().lowercase()
            val normalizedFilterTags = filter.tags.map { it.trim().lowercase() }.toSet()

            allTransactions
                .asSequence()
                .map { mapToTransactionItem(it, accountsById, categoriesById) }
                .filter { item ->
                    if (filter.startEpochDay != null && item.ledgerDateEpochDay < filter.startEpochDay) {
                        return@filter false
                    }
                    if (filter.endEpochDay != null && item.ledgerDateEpochDay > filter.endEpochDay) {
                        return@filter false
                    }
                    if (filter.accountIds.isNotEmpty()) {
                        val matchesSource = item.accountId in filter.accountIds
                        val matchesTarget = item.transferToAccountId != null &&
                                item.transferToAccountId in filter.accountIds
                        if (!matchesSource && !matchesTarget) return@filter false
                    }
                    if (expandedCategoryIds.isNotEmpty()) {
                        val matchesMain = item.categoryId != null && item.categoryId in expandedCategoryIds
                        val matchesSplit = item.splits.any { it.categoryId in expandedCategoryIds }
                        if (!matchesMain && !matchesSplit) return@filter false
                    }
                    if (filter.types.isNotEmpty() && item.type !in filter.types) {
                        return@filter false
                    }
                    if (normalizedFilterTags.isNotEmpty()) {
                        val hasMatchingTag = item.tags.any { it.lowercase() in normalizedFilterTags }
                        if (!hasMatchingTag) return@filter false
                    }
                    if (filter.minAmount != null && item.amount < filter.minAmount) {
                        return@filter false
                    }
                    if (filter.maxAmount != null && item.amount > filter.maxAmount) {
                        return@filter false
                    }
                    if (normalizedQuery.isNotEmpty()) {
                        val matchesText = item.payeeOrDescription.lowercase().contains(normalizedQuery) ||
                                item.note.lowercase().contains(normalizedQuery) ||
                                (item.categoryName?.lowercase()?.contains(normalizedQuery) == true) ||
                                (item.parentCategoryName?.lowercase()?.contains(normalizedQuery) == true) ||
                                item.accountName.lowercase().contains(normalizedQuery) ||
                                (item.transferToAccountName?.lowercase()?.contains(normalizedQuery) == true) ||
                                item.tags.any { it.lowercase().contains(normalizedQuery) } ||
                                item.splits.any {
                                    it.note.lowercase().contains(normalizedQuery) ||
                                            it.categoryName.lowercase().contains(normalizedQuery)
                                }
                        if (!matchesText) return@filter false
                    }
                    true
                }
                .toList()
        }.flowOn(Dispatchers.Default)
    }

    override suspend fun upsertSingleTransaction(transaction: TransactionItem): Result<Long> =
        runCatching {
            require(transaction.amount.isPositive) {
                "Transaction amount must be strictly positive."
            }
            require(!transaction.transferFee.isNegative) {
                "Transfer fee cannot be negative."
            }

            val account = accountDao.getAccountByIdOnce(transaction.accountId)
            val resolvedCurrency = transaction.currencyCode.ifBlank {
                account?.currencyCode ?: "INR"
            }

            val isTransferType = transaction.type == TransactionType.TRANSFER || transaction.type == TransactionType.CC_SETTLEMENT

            val entity = TransactionEntity(
                id = transaction.id,
                type = transaction.type,
                timestampEpochMillis = transaction.timestampEpochMillis,
                ledgerDateEpochDay = transaction.ledgerDateEpochDay,
                amountMinor = transaction.amount.amountMinor,
                currencyCode = resolvedCurrency,
                accountId = transaction.accountId,
                transferToAccountId = if (isTransferType) transaction.transferToAccountId else null,
                transferFeeMinor = if (isTransferType) transaction.transferFee.amountMinor else 0L,
                categoryId = if (isTransferType) null else transaction.categoryId,
                isSplit = false,
                payeeOrDescription = transaction.payeeOrDescription.trim(),
                note = transaction.note.trim(),
                installmentIndex = transaction.installmentIndex,
                installmentTotalCount = transaction.installmentTotalCount,
                recurringRuleId = transaction.recurringRuleId
            )

            transactionDao.upsertTransactionWithDetails(
                transaction = entity,
                splits = emptyList(),
                tagNames = transaction.tags
            )
        }

    override suspend fun upsertSplitTransaction(transaction: TransactionItem): Result<Long> =
        runCatching {
            AccountingRulesEngine.validateSplitTransaction(
                totalAmount = transaction.amount,
                splits = transaction.splits
            ).getOrThrow()

            val account = accountDao.getAccountByIdOnce(transaction.accountId)
            val resolvedCurrency = transaction.currencyCode.ifBlank {
                account?.currencyCode ?: "INR"
            }

            val entity = TransactionEntity(
                id = transaction.id,
                type = transaction.type,
                timestampEpochMillis = transaction.timestampEpochMillis,
                ledgerDateEpochDay = transaction.ledgerDateEpochDay,
                amountMinor = transaction.amount.amountMinor,
                currencyCode = resolvedCurrency,
                accountId = transaction.accountId,
                transferToAccountId = null,
                transferFeeMinor = 0L,
                categoryId = null,
                isSplit = true,
                payeeOrDescription = transaction.payeeOrDescription.trim(),
                note = transaction.note.trim(),
                installmentIndex = transaction.installmentIndex,
                installmentTotalCount = transaction.installmentTotalCount,
                recurringRuleId = transaction.recurringRuleId
            )

            val splitEntities = transaction.splits.map { split ->
                TransactionSplitEntity(
                    id = split.id,
                    transactionId = transaction.id,
                    categoryId = split.categoryId,
                    amountMinor = split.amount.amountMinor,
                    note = split.note.trim()
                )
            }

            transactionDao.upsertTransactionWithDetails(
                transaction = entity,
                splits = splitEntities,
                tagNames = transaction.tags
            )
        }

    override suspend fun recordTransfer(
        fromAccountId: Long,
        toAccountId: Long,
        amount: MinorMoney,
        transferFee: MinorMoney,
        timestampEpochMillis: Long,
        ledgerDateEpochDay: Long,
        note: String,
        tags: List<String>
    ): Result<Long> = runCatching {
        require(fromAccountId != toAccountId) {
            "Source and destination accounts for a transfer must be different."
        }
        require(amount.isPositive) {
            "Transfer amount must be strictly positive."
        }
        require(!transferFee.isNegative) {
            "Transfer fee cannot be negative."
        }

        val fromAccount = accountDao.getAccountByIdOnce(fromAccountId)
            ?: throw IllegalArgumentException("Source account #$fromAccountId not found.")
        val toAccount = accountDao.getAccountByIdOnce(toAccountId)
            ?: throw IllegalArgumentException("Destination account #$toAccountId not found.")

        val entity = TransactionEntity(
            id = 0L,
            type = TransactionType.TRANSFER,
            timestampEpochMillis = timestampEpochMillis,
            ledgerDateEpochDay = ledgerDateEpochDay,
            amountMinor = amount.amountMinor,
            currencyCode = fromAccount.currencyCode,
            accountId = fromAccountId,
            transferToAccountId = toAccountId,
            transferFeeMinor = transferFee.amountMinor,
            categoryId = null,
            isSplit = false,
            payeeOrDescription = "${fromAccount.name} → ${toAccount.name}",
            note = note.trim()
        )

        transactionDao.upsertTransactionWithDetails(
            transaction = entity,
            splits = emptyList(),
            tagNames = tags
        )
    }

    override suspend fun recordRefund(
        accountId: Long,
        categoryId: Long?,
        refundAmount: MinorMoney,
        timestampEpochMillis: Long,
        ledgerDateEpochDay: Long,
        originalTransactionId: Long?,
        payeeOrDescription: String,
        note: String
    ): Result<Long> = runCatching {
        require(refundAmount.isPositive) {
            "Refund amount must be strictly positive."
        }

        val account = accountDao.getAccountByIdOnce(accountId)
            ?: throw IllegalArgumentException("Account #$accountId not found.")

        val entity = TransactionEntity(
            id = 0L,
            type = TransactionType.REFUND,
            timestampEpochMillis = timestampEpochMillis,
            ledgerDateEpochDay = ledgerDateEpochDay,
            amountMinor = refundAmount.amountMinor,
            currencyCode = account.currencyCode,
            accountId = accountId,
            transferToAccountId = null,
            transferFeeMinor = 0L,
            categoryId = categoryId,
            isSplit = false,
            payeeOrDescription = payeeOrDescription.trim().ifEmpty { "Refund" },
            note = note.trim(),
            originalTransactionId = originalTransactionId
        )

        transactionDao.insertTransactionRaw(entity)
    }

    override suspend fun createInstallmentPlanTransactions(
        baseTransaction: TransactionItem,
        totalInstallmentAmount: MinorMoney,
        monthsCount: Int,
        startDateEpochDay: Long
    ): Result<List<Long>> = runCatching {
        val schedule = AccountingRulesEngine.generateInstallmentSchedule(
            totalAmount = totalInstallmentAmount,
            monthsCount = monthsCount,
            startDateEpochDay = startDateEpochDay
        )

        val account = accountDao.getAccountByIdOnce(baseTransaction.accountId)
        val resolvedCurrency = baseTransaction.currencyCode.ifBlank {
            account?.currencyCode ?: "INR"
        }
        val baseTitle = baseTransaction.payeeOrDescription.trim().ifEmpty { "Installment Plan" }

        val planEntity = InstallmentPlanEntity(
            id = 0L,
            title = baseTitle,
            accountId = baseTransaction.accountId,
            categoryId = baseTransaction.categoryId,
            totalAmountMinor = totalInstallmentAmount.amountMinor,
            monthsCount = monthsCount,
            startDateEpochDay = startDateEpochDay
        )

        val installmentEntities = schedule.mapIndexed { index, (scheduledEpochDay, installmentMoney) ->
            val installmentNumber = index + 1
            TransactionEntity(
                id = 0L,
                type = TransactionType.EXPENSE,
                timestampEpochMillis = scheduledEpochDay * MILLIS_PER_DAY,
                ledgerDateEpochDay = scheduledEpochDay,
                amountMinor = installmentMoney.amountMinor,
                currencyCode = resolvedCurrency,
                accountId = baseTransaction.accountId,
                transferToAccountId = null,
                transferFeeMinor = 0L,
                categoryId = baseTransaction.categoryId,
                isSplit = false,
                payeeOrDescription = "$baseTitle ($installmentNumber/$monthsCount)",
                note = baseTransaction.note.trim(),
                installmentIndex = installmentNumber,
                installmentTotalCount = monthsCount
            )
        }

        transactionDao.insertInstallmentPlanWithTransactions(
            plan = planEntity,
            installmentTransactions = installmentEntities,
            tagNames = baseTransaction.tags
        )
    }

    override suspend fun upsertRecurringTransaction(
        transaction: TransactionItem,
        frequency: RepeatFrequency
    ): Result<Long> = runCatching {
        database.withTransaction {
            val nextOccurrenceDate = RecurringScheduleCalculator.computeNextOccurrence(
                currentDate = LocalDate.ofEpochDay(transaction.ledgerDateEpochDay),
                frequency = frequency
            )
            val sourceAccount = accountDao.getAccountByIdOnce(transaction.accountId)
            val targetAccount = if (transaction.transferToAccountId != null) {
                accountDao.getAccountByIdOnce(transaction.transferToAccountId)
            } else null
            val resolvedCurrency = transaction.currencyCode.ifBlank {
                sourceAccount?.currencyCode ?: "INR"
            }

            val resolvedTitle = transaction.payeeOrDescription.trim().ifEmpty {
                when {
                    transaction.type == TransactionType.TRANSFER && targetAccount != null ->
                        "${sourceAccount?.name ?: "Account"} → ${targetAccount.name}"
                    transaction.categoryName != null -> transaction.categoryName
                    else -> "Recurring ${frequency.displayName}"
                }
            }

            val ruleEntity = RecurringRuleEntity(
                id = 0L,
                title = resolvedTitle,
                type = transaction.type,
                amountMinor = transaction.amount.amountMinor,
                currencyCode = resolvedCurrency,
                accountId = transaction.accountId,
                transferToAccountId = transaction.transferToAccountId,
                categoryId = transaction.categoryId,
                frequency = frequency,
                startDateEpochDay = transaction.ledgerDateEpochDay,
                nextOccurrenceEpochDay = nextOccurrenceDate.toEpochDay(),
                endDateEpochDay = null,
                payeeOrDescription = transaction.payeeOrDescription.trim().ifEmpty { resolvedTitle },
                note = transaction.note.trim(),
                isActive = true
            )
            val ruleId = budgetAndRecurringDao.upsertRecurringRule(ruleEntity)

            val txWithRule = transaction.copy(recurringRuleId = ruleId)
            if (transaction.isSplit) {
                upsertSplitTransaction(txWithRule).getOrThrow()
            } else {
                upsertSingleTransaction(txWithRule).getOrThrow()
            }
        }
    }

    override suspend fun deleteTransaction(transactionId: Long): Result<Unit> = runCatching {
        transactionDao.deleteTransactionById(transactionId)
    }

    private fun combineWithMetadata(
        sourceFlow: Flow<List<TransactionWithDetails>>
    ): Flow<List<TransactionItem>> {
        return combine(
            sourceFlow,
            accountDao.observeAllAccounts(),
            categoryAndTagDao.observeAllCategories()
        ) { txList, accounts, categories ->
            val accountsById = accounts.associateBy { it.id }
            val categoriesById = categories.associateBy { it.id }
            txList.map { txWithDetails ->
                mapToTransactionItem(txWithDetails, accountsById, categoriesById)
            }
        }
    }

    private fun mapToTransactionItem(
        txWithDetails: TransactionWithDetails,
        accountsById: Map<Long, AccountEntity>,
        categoriesById: Map<Long, CategoryEntity>
    ): TransactionItem {
        val tx = txWithDetails.transaction
        val sourceAccount = accountsById[tx.accountId]
        val targetAccount = tx.transferToAccountId?.let { accountsById[it] }
        val category = tx.categoryId?.let { categoriesById[it] }
        val parentCategory = category?.parentId?.let { categoriesById[it] }

        val mappedSplits = txWithDetails.splits.map { split ->
            val splitCategory = categoriesById[split.categoryId]
            SplitLineItem(
                id = split.id,
                categoryId = split.categoryId,
                categoryName = splitCategory?.name ?: "",
                amount = MinorMoney(split.amountMinor),
                note = split.note
            )
        }

        val mappedTags = txWithDetails.tags.map { it.name }

        val preservesTransferTarget = tx.type == TransactionType.TRANSFER ||
                tx.type == TransactionType.CC_SETTLEMENT ||
                tx.type == TransactionType.OPENING_BALANCE ||
                tx.type == TransactionType.RECONCILIATION
        val isTransferType = tx.type == TransactionType.TRANSFER || tx.type == TransactionType.CC_SETTLEMENT
        val effectiveTransferToAccountId = if (preservesTransferTarget) tx.transferToAccountId else null
        val effectiveTransferToAccountName = if (isTransferType) targetAccount?.name else null
        val effectiveTransferFee = if (isTransferType) MinorMoney(tx.transferFeeMinor) else MinorMoney.ZERO
        val effectiveCategoryId = if (isTransferType) null else tx.categoryId

        return TransactionItem(
            id = tx.id,
            type = tx.type,
            timestampEpochMillis = tx.timestampEpochMillis,
            ledgerDateEpochDay = tx.ledgerDateEpochDay,
            amount = MinorMoney(tx.amountMinor),
            currencyCode = tx.currencyCode,
            accountId = tx.accountId,
            accountName = sourceAccount?.name ?: "Unknown Account",
            transferToAccountId = effectiveTransferToAccountId,
            transferToAccountName = effectiveTransferToAccountName,
            transferFee = effectiveTransferFee,
            categoryId = effectiveCategoryId,
            categoryName = if (isTransferType) null else (if (tx.isSplit) "Split" else category?.name),
            parentCategoryName = if (isTransferType) null else parentCategory?.name,
            categoryColorHex = if (isTransferType) null else (category?.colorHex ?: parentCategory?.colorHex),
            categoryIconName = if (isTransferType) null else (category?.iconName ?: parentCategory?.iconName),
            isSplit = !isTransferType && tx.isSplit,
            splits = if (isTransferType) emptyList() else mappedSplits,
            tags = mappedTags,
            payeeOrDescription = tx.payeeOrDescription,
            note = tx.note,
            installmentIndex = transactionIdOrNull(tx.installmentIndex),
            installmentTotalCount = transactionIdOrNull(tx.installmentTotalCount),
            recurringRuleId = tx.recurringRuleId
        )
    }

    private fun transactionIdOrNull(value: Int?): Int? = value

    companion object {
        private const val MILLIS_PER_DAY = 86_400_000L
    }
}

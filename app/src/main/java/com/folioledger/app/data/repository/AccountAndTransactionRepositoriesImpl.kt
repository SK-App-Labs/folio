// File: app/src/main/java/com/folioledger/app/data/repository/AccountAndTransactionRepositoriesImpl.kt
package com.folioledger.app.data.repository

import com.folioledger.app.data.local.dao.AccountDao
import com.folioledger.app.data.local.dao.CategoryAndTagDao
import com.folioledger.app.data.local.dao.TransactionDao
import com.folioledger.app.data.local.entity.AccountEntity
import com.folioledger.app.data.local.entity.CategoryEntity
import com.folioledger.app.data.local.entity.InstallmentPlanEntity
import com.folioledger.app.data.local.entity.TransactionEntity
import com.folioledger.app.data.local.entity.TransactionSplitEntity
import com.folioledger.app.data.local.entity.TransactionWithDetails
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NetWorthOverview
import com.folioledger.app.domain.model.SplitLineItem
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionSearchFilter
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.repository.AccountRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import com.folioledger.app.domain.repository.TransactionRepository
import com.folioledger.app.domain.usecase.AccountingRulesEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class AccountRepositoryImpl(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val settingsRepository: SettingsAndBackupRepository
) : AccountRepository {

    override fun observeNetWorthOverview(referenceDateEpochDay: Long): Flow<NetWorthOverview> {
        return observeAllAccounts(referenceDateEpochDay).map { allSummaries ->
            val activeSummaries = allSummaries.filter { !it.isArchived }
            var totalAssets = MinorMoney.ZERO
            var totalLiabilities = MinorMoney.ZERO

            for (account in activeSummaries) {
                if (!account.includeInNetWorth || account.groupType == AccountGroup.DEBIT_CARD) {
                    continue
                }
                when {
                    account.currentBalance.isPositive -> {
                        totalAssets += account.currentBalance
                    }
                    account.currentBalance.isNegative -> {
                        totalLiabilities += account.currentBalance.abs()
                    }
                }
            }

            val netWorth = totalAssets - totalLiabilities
            val accountsByGroup = activeSummaries.groupBy { it.groupType }

            NetWorthOverview(
                totalAssets = totalAssets,
                totalLiabilities = totalLiabilities,
                netWorth = netWorth,
                accountsByGroup = accountsByGroup
            )
        }
    }

    override fun observeActiveAccounts(referenceDateEpochDay: Long): Flow<List<AccountSummary>> {
        return observeAllAccounts(referenceDateEpochDay).map { summaries ->
            summaries.filter { !it.isArchived }
        }
    }

    override fun observeAllAccounts(referenceDateEpochDay: Long): Flow<List<AccountSummary>> {
        return combine(
            accountDao.observeAllAccounts(),
            transactionDao.observeAllTransactions(),
            settingsRepository.observeUserPreferences()
        ) { accountEntities, allTxWithDetails, userPrefs ->
            computeAccountSummaries(
                accountEntities = accountEntities,
                allTransactions = allTxWithDetails,
                referenceDateEpochDay = referenceDateEpochDay,
                monthStartDay = userPrefs.monthStartDay
            )
        }
    }

    override fun observeAccountById(
        accountId: Long,
        referenceDateEpochDay: Long
    ): Flow<AccountSummary?> {
        return observeAllAccounts(referenceDateEpochDay).map { summaries ->
            summaries.firstOrNull { it.id == accountId }
        }
    }

    override suspend fun upsertAccount(
        account: AccountSummary,
        openingBalance: MinorMoney
    ): Result<Long> = runCatching {
        val trimmedName = account.name.trim()
        require(trimmedName.isNotEmpty()) { "Account name cannot be blank." }

        val existing = if (account.id != 0L) {
            accountDao.getAccountByIdOnce(account.id)
        } else {
            null
        }

        val resolvedOpeningMinor = if (existing != null) {
            if (openingBalance.isZero || openingBalance.amountMinor == existing.openingBalanceMinor) {
                existing.openingBalanceMinor
            } else {
                // Check live balance prior to edit so pre-filled currentBalance doesn't double-count transactions
                val liveBeforeEdit = observeAccountById(
                    accountId = existing.id,
                    referenceDateEpochDay = LocalDate.now().toEpochDay()
                ).first()
                val previousCurrentMinor = liveBeforeEdit?.currentBalance?.amountMinor
                    ?: existing.openingBalanceMinor

                if (openingBalance.amountMinor == previousCurrentMinor) {
                    existing.openingBalanceMinor
                } else {
                    val netTransactionsDelta = Math.subtractExact(
                        previousCurrentMinor,
                        existing.openingBalanceMinor
                    )
                    Math.subtractExact(openingBalance.amountMinor, netTransactionsDelta)
                }
            }
        } else {
            if (!openingBalance.isZero) {
                openingBalance.amountMinor
            } else {
                account.currentBalance.amountMinor
            }
        }

        val entity = AccountEntity(
            id = account.id,
            name = trimmedName,
            groupType = account.groupType,
            currencyCode = account.currencyCode.ifBlank { "INR" },
            openingBalanceMinor = resolvedOpeningMinor,
            initialUnbilledMinor = if (account.groupType == AccountGroup.CREDIT_CARD) {
                account.creditCardUnbilledAmount?.amountMinor
            } else {
                null
            },
            creditLimitMinor = account.creditLimit?.amountMinor,
            settlementDayOfMonth = account.settlementDayOfMonth?.coerceIn(1, 31),
            paymentDueDayOfMonth = account.paymentDueDayOfMonth?.coerceIn(1, 31),
            parentAccountId = if (account.groupType == AccountGroup.DEBIT_CARD) {
                account.parentAccountId
            } else {
                null
            },
            includeInNetWorth = if (account.groupType == AccountGroup.DEBIT_CARD) {
                false
            } else {
                account.includeInNetWorth
            },
            isArchived = account.isArchived,
            colorHex = account.colorHex.ifBlank { "#10B981" },
            iconName = account.iconName.ifBlank { "account_balance_wallet" },
            sortOrder = existing?.sortOrder ?: 0,
            createdAtEpochMillis = existing?.createdAtEpochMillis ?: System.currentTimeMillis()
        )

        accountDao.insertAccount(entity)
    }

    override suspend fun setAccountArchived(
        accountId: Long,
        isArchived: Boolean
    ): Result<Unit> = runCatching {
        accountDao.setAccountArchived(accountId = accountId, isArchived = isArchived)
    }

    override suspend fun settleCreditCardStatement(
        sourceAssetAccountId: Long,
        creditCardAccountId: Long,
        settlementAmount: MinorMoney,
        timestampEpochMillis: Long,
        ledgerDateEpochDay: Long,
        note: String
    ): Result<Long> = runCatching {
        require(sourceAssetAccountId != creditCardAccountId) {
            "Source payment account and destination credit card must be distinct."
        }
        require(settlementAmount.isPositive) {
            "Credit card settlement amount must be strictly positive."
        }

        val sourceAccount = accountDao.getAccountByIdOnce(sourceAssetAccountId)
            ?: throw IllegalArgumentException("Source account #$sourceAssetAccountId not found.")
        val creditCardAccount = accountDao.getAccountByIdOnce(creditCardAccountId)
            ?: throw IllegalArgumentException("Credit card account #$creditCardAccountId not found.")
        require(creditCardAccount.groupType == AccountGroup.CREDIT_CARD) {
            "Destination account must be a CREDIT_CARD account."
        }

        val settlementTx = TransactionEntity(
            id = 0L,
            type = TransactionType.CC_SETTLEMENT,
            timestampEpochMillis = timestampEpochMillis,
            ledgerDateEpochDay = ledgerDateEpochDay,
            amountMinor = settlementAmount.amountMinor,
            currencyCode = sourceAccount.currencyCode,
            accountId = sourceAssetAccountId,
            transferToAccountId = creditCardAccountId,
            transferFeeMinor = 0L,
            categoryId = null,
            isSplit = false,
            payeeOrDescription = "Bill Settlement - ${creditCardAccount.name}",
            note = note.trim()
        )

        transactionDao.insertTransactionRaw(settlementTx)
    }

    private fun computeAccountSummaries(
        accountEntities: List<AccountEntity>,
        allTransactions: List<TransactionWithDetails>,
        referenceDateEpochDay: Long,
        monthStartDay: Int
    ): List<AccountSummary> {
        val accountsById = accountEntities.associateBy { it.id }
        val (finMonthStartDay, finMonthEndDay) = AccountingRulesEngine.computeFinancialMonthRange(
            referenceDateEpochDay = referenceDateEpochDay,
            monthStartDay = monthStartDay.coerceIn(1, 31)
        )

        val debitCardParentMap: Map<Long, Long> = accountEntities
            .filter { it.groupType == AccountGroup.DEBIT_CARD && it.parentAccountId != null }
            .associate { it.id to it.parentAccountId!! }

        fun resolveEffectiveAccountId(rawAccountId: Long): Long =
            debitCardParentMap[rawAccountId] ?: rawAccountId

        val runningBalancesMinor = HashMap<Long, Long>(accountEntities.size)
        for (entity in accountEntities) {
            if (
                entity.groupType != AccountGroup.CREDIT_CARD &&
                entity.groupType != AccountGroup.DEBIT_CARD
            ) {
                val initialSigned = if (
                    entity.groupType.isLiabilityByDefault && entity.openingBalanceMinor > 0L
                ) {
                    Math.negateExact(entity.openingBalanceMinor)
                } else {
                    entity.openingBalanceMinor
                }
                runningBalancesMinor[entity.id] = initialSigned
            }
        }

        val debitCardPeriodSpendMap = HashMap<Long, Long>()
        for (entity in accountEntities) {
            if (entity.groupType == AccountGroup.DEBIT_CARD) {
                debitCardPeriodSpendMap[entity.id] = 0L
            }
        }

        for (txWithDetails in allTransactions) {
            val tx = txWithDetails.transaction
            if (tx.ledgerDateEpochDay > referenceDateEpochDay) continue

            val amountMinor = Math.abs(tx.amountMinor)
            val feeMinor = Math.abs(tx.transferFeeMinor)

            if (
                debitCardPeriodSpendMap.containsKey(tx.accountId) &&
                tx.ledgerDateEpochDay in finMonthStartDay..finMonthEndDay
            ) {
                val currentSpend = debitCardPeriodSpendMap[tx.accountId] ?: 0L
                val updatedSpend = when (tx.type) {
                    TransactionType.EXPENSE ->
                        Math.addExact(currentSpend, Math.addExact(amountMinor, feeMinor))
                    TransactionType.REFUND ->
                        Math.subtractExact(currentSpend, amountMinor)
                    TransactionType.TRANSFER,
                    TransactionType.CC_SETTLEMENT ->
                        Math.addExact(currentSpend, feeMinor)
                    TransactionType.INCOME -> currentSpend
                }
                debitCardPeriodSpendMap[tx.accountId] = updatedSpend
            }

            val effectiveSourceId = resolveEffectiveAccountId(tx.accountId)
            val effectiveTargetId = tx.transferToAccountId?.let { resolveEffectiveAccountId(it) }

            when (tx.type) {
                TransactionType.INCOME,
                TransactionType.REFUND -> {
                    runningBalancesMinor[effectiveSourceId]?.let { current ->
                        runningBalancesMinor[effectiveSourceId] = Math.addExact(current, amountMinor)
                    }
                }
                TransactionType.EXPENSE -> {
                    val totalOutflow = Math.addExact(amountMinor, feeMinor)
                    runningBalancesMinor[effectiveSourceId]?.let { current ->
                        runningBalancesMinor[effectiveSourceId] = Math.subtractExact(current, totalOutflow)
                    }
                }
                TransactionType.TRANSFER,
                TransactionType.CC_SETTLEMENT -> {
                    val totalOutflow = Math.addExact(amountMinor, feeMinor)
                    runningBalancesMinor[effectiveSourceId]?.let { current ->
                        runningBalancesMinor[effectiveSourceId] = Math.subtractExact(current, totalOutflow)
                    }
                    if (effectiveTargetId != null) {
                        runningBalancesMinor[effectiveTargetId]?.let { currentTarget ->
                            runningBalancesMinor[effectiveTargetId] = Math.addExact(currentTarget, amountMinor)
                        }
                    }
                }
            }
        }

        return accountEntities.map { entity ->
            when (entity.groupType) {
                AccountGroup.CREDIT_CARD -> {
                    val ccTransactions = allTransactions.mapNotNull { txWithDetails ->
                        val tx = txWithDetails.transaction
                        val isSource = tx.accountId == entity.id
                        val isTarget = tx.transferToAccountId == entity.id
                        if (!isSource && !isTarget) {
                            null
                        } else {
                            val normalizedType = if (
                                tx.type == TransactionType.TRANSFER && isTarget && !isSource
                            ) {
                                TransactionType.CC_SETTLEMENT
                            } else {
                                tx.type
                            }
                            val normalizedFee = if (isSource) {
                                MinorMoney(tx.transferFeeMinor)
                            } else {
                                MinorMoney.ZERO
                            }

                            TransactionItem(
                                id = tx.id,
                                type = normalizedType,
                                timestampEpochMillis = tx.timestampEpochMillis,
                                ledgerDateEpochDay = tx.ledgerDateEpochDay,
                                amount = MinorMoney(tx.amountMinor),
                                currencyCode = tx.currencyCode,
                                accountId = entity.id,
                                accountName = entity.name,
                                transferToAccountId = tx.transferToAccountId,
                                transferToAccountName = tx.transferToAccountId?.let { accountsById[it]?.name },
                                transferFee = normalizedFee,
                                categoryId = tx.categoryId,
                                payeeOrDescription = tx.payeeOrDescription,
                                note = tx.note
                            )
                        }
                    }

                    val settlementDay = (entity.settlementDayOfMonth ?: 15).coerceIn(1, 31)
                    val creditLimitMoney = entity.creditLimitMinor?.let { MinorMoney(it) }
                    val initialUnbilledMoney = entity.initialUnbilledMinor?.let { MinorMoney(it) }
                    val ccMetrics = AccountingRulesEngine.computeCreditCardMetrics(
                        openingBalance = MinorMoney(entity.openingBalanceMinor),
                        transactions = ccTransactions,
                        settlementDayOfMonth = settlementDay,
                        referenceDateEpochDay = referenceDateEpochDay,
                        creditLimit = creditLimitMoney,
                        initialUnbilledAmount = initialUnbilledMoney
                    )

                    AccountSummary(
                        id = entity.id,
                        name = entity.name,
                        groupType = entity.groupType,
                        currencyCode = entity.currencyCode,
                        currentBalance = ccMetrics.currentBalance,
                        creditLimit = creditLimitMoney,
                        creditCardStatementPayable = ccMetrics.statementPayable,
                        creditCardUnbilledAmount = ccMetrics.unbilledAmount,
                        settlementDayOfMonth = settlementDay,
                        paymentDueDayOfMonth = entity.paymentDueDayOfMonth,
                        parentAccountId = null,
                        debitCardPeriodSpend = null,
                        includeInNetWorth = entity.includeInNetWorth,
                        isArchived = entity.isArchived,
                        colorHex = entity.colorHex,
                        iconName = entity.iconName
                    )
                }

                AccountGroup.DEBIT_CARD -> {
                    val parentBalanceMinor = entity.parentAccountId?.let { parentId ->
                        runningBalancesMinor[parentId] ?: 0L
                    } ?: 0L
                    val periodSpendMinor = (debitCardPeriodSpendMap[entity.id] ?: 0L).coerceAtLeast(0L)

                    AccountSummary(
                        id = entity.id,
                        name = entity.name,
                        groupType = entity.groupType,
                        currencyCode = entity.currencyCode,
                        currentBalance = MinorMoney(parentBalanceMinor),
                        creditLimit = null,
                        creditCardStatementPayable = null,
                        creditCardUnbilledAmount = null,
                        settlementDayOfMonth = null,
                        paymentDueDayOfMonth = null,
                        parentAccountId = entity.parentAccountId,
                        debitCardPeriodSpend = MinorMoney(periodSpendMinor),
                        includeInNetWorth = false,
                        isArchived = entity.isArchived,
                        colorHex = entity.colorHex,
                        iconName = entity.iconName
                    )
                }

                else -> {
                    val liveBalanceMinor = runningBalancesMinor[entity.id] ?: entity.openingBalanceMinor
                    AccountSummary(
                        id = entity.id,
                        name = entity.name,
                        groupType = entity.groupType,
                        currencyCode = entity.currencyCode,
                        currentBalance = MinorMoney(liveBalanceMinor),
                        creditLimit = entity.creditLimitMinor?.let { MinorMoney(it) },
                        creditCardStatementPayable = null,
                        creditCardUnbilledAmount = null,
                        settlementDayOfMonth = entity.settlementDayOfMonth,
                        paymentDueDayOfMonth = entity.paymentDueDayOfMonth,
                        parentAccountId = entity.parentAccountId,
                        debitCardPeriodSpend = null,
                        includeInNetWorth = entity.includeInNetWorth,
                        isArchived = entity.isArchived,
                        colorHex = entity.colorHex,
                        iconName = entity.iconName
                    )
                }
            }
        }
    }
}

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryAndTagDao: CategoryAndTagDao
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
        return combine(
            transactionDao.observeAllTransactions(),
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
        }
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

            val entity = TransactionEntity(
                id = transaction.id,
                type = transaction.type,
                timestampEpochMillis = transaction.timestampEpochMillis,
                ledgerDateEpochDay = transaction.ledgerDateEpochDay,
                amountMinor = transaction.amount.amountMinor,
                currencyCode = resolvedCurrency,
                accountId = transaction.accountId,
                transferToAccountId = transaction.transferToAccountId,
                transferFeeMinor = transaction.transferFee.amountMinor,
                categoryId = transaction.categoryId,
                isSplit = false,
                payeeOrDescription = transaction.payeeOrDescription.trim(),
                note = transaction.note.trim(),
                installmentIndex = transaction.installmentIndex,
                installmentTotalCount = transaction.installmentTotalCount
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
                installmentTotalCount = transaction.installmentTotalCount
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

        return TransactionItem(
            id = tx.id,
            type = tx.type,
            timestampEpochMillis = tx.timestampEpochMillis,
            ledgerDateEpochDay = tx.ledgerDateEpochDay,
            amount = MinorMoney(tx.amountMinor),
            currencyCode = tx.currencyCode,
            accountId = tx.accountId,
            accountName = sourceAccount?.name ?: "Unknown Account",
            transferToAccountId = tx.transferToAccountId,
            transferToAccountName = targetAccount?.name,
            transferFee = MinorMoney(tx.transferFeeMinor),
            categoryId = tx.categoryId,
            categoryName = if (tx.isSplit) "Split" else category?.name,
            parentCategoryName = parentCategory?.name,
            categoryColorHex = category?.colorHex ?: parentCategory?.colorHex,
            categoryIconName = category?.iconName ?: parentCategory?.iconName,
            isSplit = tx.isSplit,
            splits = mappedSplits,
            tags = mappedTags,
            payeeOrDescription = tx.payeeOrDescription,
            note = tx.note,
            installmentIndex = transactionIdOrNull(tx.installmentIndex),
            installmentTotalCount = transactionIdOrNull(tx.installmentTotalCount)
        )
    }

    private fun transactionIdOrNull(value: Int?): Int? = value

    companion object {
        private const val MILLIS_PER_DAY = 86_400_000L
    }
}
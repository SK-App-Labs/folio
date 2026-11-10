package com.folioledger.app.data.repository

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.withTransaction
import com.folioledger.app.data.backup.AutoBackupRetentionEngine
import com.folioledger.app.data.backup.SessionChangeTracker
import com.folioledger.app.data.local.DefaultCategorySeeder
import com.folioledger.app.data.local.FolioDatabase
import com.folioledger.app.data.local.dao.CategoryAndTagDao
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
import com.folioledger.app.data.pdf.AccountSnapshotPdfItem
import com.folioledger.app.data.pdf.AccountStatementPdfData
import com.folioledger.app.data.pdf.AnnualTaxPdfData
import com.folioledger.app.data.pdf.CategoryAggregateItem
import com.folioledger.app.data.pdf.FiscalYearType
import com.folioledger.app.data.pdf.PdfExportResult
import com.folioledger.app.data.pdf.PdfStatementEngine
import com.folioledger.app.data.pdf.TaxDeductionPdfItem
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AutoBackupRetention
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.DuplicateTransactionSample
import com.folioledger.app.domain.model.ImportCsvPreview
import com.folioledger.app.domain.model.ImportCsvResult
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.SplitLineItem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.domain.repository.CategoryAndTagRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import com.folioledger.app.domain.usecase.AccountingRulesEngine
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.accounts.AccountPassbookEntryUiModel
import com.folioledger.app.ui.components.suggestIconAndColorForCategoryName
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.math.pow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class PdfStatementRepositoryHelper(
    private val context: Context,
    private val database: FolioDatabase,
    private val userPreferences: UserPreferencesDataSource
) {

    private data class FiscalRangeInfo(
        val startEpochDay: Long,
        val endEpochDay: Long,
        val title: String,
        val subtitle: String
    )

    private fun mapDetailsToTransactionItem(
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

        return TransactionItem(
            id = tx.id,
            type = tx.type,
            timestampEpochMillis = tx.timestampEpochMillis,
            ledgerDateEpochDay = tx.ledgerDateEpochDay,
            amount = MinorMoney(tx.amountMinor),
            currencyCode = tx.currencyCode,
            accountId = tx.accountId,
            accountName = sourceAccount?.name ?: "Unknown Account",
            transferToAccountId = if (preservesTransferTarget) tx.transferToAccountId else null,
            transferToAccountName = if (isTransferType) targetAccount?.name else null,
            transferFee = if (isTransferType) MinorMoney(tx.transferFeeMinor) else MinorMoney.ZERO,
            categoryId = if (isTransferType) null else tx.categoryId,
            categoryName = if (isTransferType) null else (if (tx.isSplit) "Split" else category?.name),
            parentCategoryName = if (isTransferType) null else parentCategory?.name,
            categoryColorHex = if (isTransferType) null else (category?.colorHex ?: parentCategory?.colorHex),
            categoryIconName = if (isTransferType) null else (category?.iconName ?: parentCategory?.iconName),
            isSplit = !isTransferType && tx.isSplit,
            splits = if (isTransferType) emptyList() else mappedSplits,
            tags = mappedTags,
            payeeOrDescription = tx.payeeOrDescription,
            note = tx.note
        )
    }

    suspend fun generateAccountStatementPdf(
        accountId: Long,
        startEpochDay: Long,
        endEpochDay: Long,
        includeNotesAndTags: Boolean,
        includeRunningBalance: Boolean
    ): Result<PdfExportResult> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            val prefs = userPreferences.observeUserPreferences().first()
            val allAccountEntities = database.accountDao().getAllAccountsOnce()
            val accountEntity = allAccountEntities.firstOrNull { it.id == accountId }
                ?: error("Account with ID $accountId not found")
            val allCategories = database.categoryAndTagDao().getAllCategoriesOnce().associateBy { it.id }
            val accountsById = allAccountEntities.associateBy { it.id }

            val allTxWithDetails = database.transactionDao().getAllTransactionsWithDetailsOnce()
            val allTransactions = allTxWithDetails.map { txDetails ->
                mapDetailsToTransactionItem(txDetails, accountsById, allCategories)
            }

            val childDebitCards = allAccountEntities.filter {
                it.groupType == AccountGroup.DEBIT_CARD && it.parentAccountId == accountId
            }
            val effectiveAccountIds: Set<Long> = buildSet {
                add(accountId)
                if (accountEntity.groupType == AccountGroup.DEBIT_CARD) {
                    accountEntity.parentAccountId?.let { add(it) }
                } else {
                    childDebitCards.forEach { add(it.id) }
                }
            }

            val matchingChronological = allTransactions
                .filter { tx ->
                    when (tx.type) {
                        TransactionType.EXPENSE,
                        TransactionType.INCOME,
                        TransactionType.REFUND,
                        TransactionType.OPENING_BALANCE,
                        TransactionType.RECONCILIATION -> tx.accountId in effectiveAccountIds
                        TransactionType.TRANSFER,
                        TransactionType.CC_SETTLEMENT -> tx.accountId in effectiveAccountIds ||
                                (tx.transferToAccountId != null && tx.transferToAccountId in effectiveAccountIds)
                    }
                }
                .sortedWith(
                    compareBy<TransactionItem> { it.ledgerDateEpochDay }
                        .thenBy { it.timestampEpochMillis }
                        .thenBy { it.id }
                )

            val deltasWithTx: List<Pair<TransactionItem, Long>> = matchingChronological.map { tx ->
                val deltaMinor = AccountingRulesEngine.computeSignedDeltaMinorForAccount(
                    tx = tx,
                    effectiveAccountIds = effectiveAccountIds
                )
                tx to deltaMinor
            }

            val hasOpeningTx = deltasWithTx.any { it.first.type == TransactionType.OPENING_BALANCE }
            val initialAnchor = if (hasOpeningTx) {
                0L
            } else if (accountEntity.groupType.isLiabilityByDefault && accountEntity.openingBalanceMinor > 0L) {
                Math.negateExact(accountEntity.openingBalanceMinor)
            } else {
                accountEntity.openingBalanceMinor
            }

            var runningBalanceMinor = initialAnchor
            var periodOpeningBalanceMinor = runningBalanceMinor
            var periodInflowsMinor = 0L
            var periodOutflowsMinor = 0L

            val periodEntries = mutableListOf<AccountPassbookEntryUiModel>()

            for ((tx, deltaMinor) in deltasWithTx) {
                if (tx.ledgerDateEpochDay < startEpochDay) {
                    runningBalanceMinor += deltaMinor
                    periodOpeningBalanceMinor = runningBalanceMinor
                } else if (tx.ledgerDateEpochDay in startEpochDay..endEpochDay) {
                    runningBalanceMinor += deltaMinor
                    if (deltaMinor > 0L) {
                        periodInflowsMinor += deltaMinor
                    } else if (deltaMinor < 0L) {
                        periodOutflowsMinor += (-deltaMinor)
                    }
                    val viaCardName = if (accountEntity.groupType != AccountGroup.DEBIT_CARD &&
                        tx.accountId != accountEntity.id &&
                        tx.accountId in effectiveAccountIds
                    ) {
                        accountsById[tx.accountId]?.name
                    } else {
                        null
                    }
                    periodEntries.add(
                        AccountPassbookEntryUiModel(
                            transaction = tx,
                            signedDelta = MinorMoney(deltaMinor),
                            isInflow = deltaMinor >= 0L,
                            runningBalanceAfter = MinorMoney(runningBalanceMinor),
                            viaLinkedDebitCardName = viaCardName
                        )
                    )
                } else {
                    runningBalanceMinor += deltaMinor
                }
            }

            val startDate = LocalDate.ofEpochDay(startEpochDay)
            val endDate = LocalDate.ofEpochDay(endEpochDay)
            val dFmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)
            val periodTitle = "${startDate.format(dFmt)} – ${endDate.format(dFmt)}"
            val currencySpec = SupportedCurrencies.fromCode(accountEntity.currencyCode)

            val isCreditCard = accountEntity.groupType == AccountGroup.CREDIT_CARD
            val groupTitle = when (accountEntity.groupType) {
                AccountGroup.BANK_ACCOUNT -> "Bank Account"
                AccountGroup.CREDIT_CARD -> "Credit Card"
                AccountGroup.DEBIT_CARD -> "Debit Card"
                AccountGroup.CASH -> "Cash & Wallet"
                AccountGroup.INVESTMENT -> "Investment & Wealth"
                AccountGroup.LOAN -> "Loan & Liability"
                AccountGroup.SAVINGS -> "Savings Account"
                else -> "Account"
            }

            val data = AccountStatementPdfData(
                accountName = accountEntity.name,
                accountGroupTitle = groupTitle,
                institutionOrDetails = null,
                periodTitle = periodTitle,
                periodSubtitle = "Statement Period: $periodTitle",
                currencySpec = currencySpec,
                numberGrouping = prefs.numberGrouping,
                openingBalance = MinorMoney(periodOpeningBalanceMinor),
                totalInflows = MinorMoney(periodInflowsMinor),
                totalOutflows = MinorMoney(periodOutflowsMinor),
                netPeriodFlow = MinorMoney(periodInflowsMinor - periodOutflowsMinor),
                closingBalance = MinorMoney(periodOpeningBalanceMinor + periodInflowsMinor - periodOutflowsMinor),
                transactionCount = periodEntries.size,
                includeRunningBalance = includeRunningBalance,
                includeNotesAndTags = includeNotesAndTags,
                isCreditCard = isCreditCard,
                creditCardLimit = accountEntity.creditLimitMinor?.let { MinorMoney(it) },
                creditCardStatementPayable = null,
                creditCardCycleText = if (isCreditCard) "Statement Settlement Day: ${accountEntity.settlementDayOfMonth ?: 18}" else null,
                creditCardDueDateText = if (isCreditCard) "Payment Due Day: ${accountEntity.paymentDueDayOfMonth ?: 5}" else null,
                entries = periodEntries,
                generatedTimestampText = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.US))
            )

            PdfStatementEngine.generateAccountStatementPdf(context, data).getOrThrow()
        }
    }

    suspend fun generateAnnualTaxStatementPdf(
        fiscalYearType: FiscalYearType,
        anchorYear: Int,
        customStartEpochDay: Long?,
        customEndEpochDay: Long?,
        selectedTaxTags: Set<String>
    ): Result<PdfExportResult> = withContext(Dispatchers.IO) {
        runCatching {
            val prefs = userPreferences.observeUserPreferences().first()
            val primaryCurrencySpec = SupportedCurrencies.fromCode(prefs.primaryCurrencyCode)
            val secondaryCurrencies = prefs.secondaryCurrencies
            val secondaryMap = secondaryCurrencies.associateBy { it.code }

            val rangeInfo = when (fiscalYearType) {
                FiscalYearType.CALENDAR_YEAR -> {
                    val s = LocalDate.of(anchorYear, 1, 1).toEpochDay()
                    val e = LocalDate.of(anchorYear, 12, 31).toEpochDay()
                    FiscalRangeInfo(s, e, "Calendar Year $anchorYear", "01 Jan $anchorYear – 31 Dec $anchorYear")
                }
                FiscalYearType.APRIL_FISCAL_YEAR -> {
                    val s = LocalDate.of(anchorYear, 4, 1).toEpochDay()
                    val e = LocalDate.of(anchorYear + 1, 3, 31).toEpochDay()
                    val nextShort = (anchorYear + 1) % 100
                    FiscalRangeInfo(s, e, "Fiscal Year $anchorYear–$nextShort", "01 Apr $anchorYear – 31 Mar ${anchorYear + 1}")
                }
                FiscalYearType.JULY_FISCAL_YEAR -> {
                    val s = LocalDate.of(anchorYear, 7, 1).toEpochDay()
                    val e = LocalDate.of(anchorYear + 1, 6, 30).toEpochDay()
                    val nextShort = (anchorYear + 1) % 100
                    FiscalRangeInfo(s, e, "Fiscal Year $anchorYear–$nextShort", "01 Jul $anchorYear – 30 Jun ${anchorYear + 1}")
                }
                FiscalYearType.CUSTOM_RANGE -> {
                    val s = customStartEpochDay ?: LocalDate.of(anchorYear, 1, 1).toEpochDay()
                    val e = customEndEpochDay ?: LocalDate.of(anchorYear, 12, 31).toEpochDay()
                    val sDate = LocalDate.ofEpochDay(s)
                    val eDate = LocalDate.ofEpochDay(e)
                    val dFmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)
                    FiscalRangeInfo(s, e, "Financial & Tax Audit Statement", "${sDate.format(dFmt)} – ${eDate.format(dFmt)}")
                }
            }

            val startEpochDay = rangeInfo.startEpochDay
            val endEpochDay = rangeInfo.endEpochDay

            val allAccountEntities = database.accountDao().getAllAccountsOnce()
            val accountsById = allAccountEntities.associateBy { it.id }
            val allCategories = database.categoryAndTagDao().getAllCategoriesOnce().associateBy { it.id }

            val allTxWithDetails = database.transactionDao().getAllTransactionsWithDetailsOnce()
            val allTransactions = allTxWithDetails.map { txDetails ->
                mapDetailsToTransactionItem(txDetails, accountsById, allCategories)
            }

            val periodTransactions = allTransactions.filter {
                it.ledgerDateEpochDay in startEpochDay..endEpochDay
            }

            var grossInflowsMinor = 0L
            var totalExpensesMinor = 0L

            val incomeByCategory = mutableMapOf<String, MutableList<Long>>()
            val expenseByCategory = mutableMapOf<String, MutableList<Long>>()
            val taxDeductions = mutableListOf<TaxDeductionPdfItem>()
            val dFmt = DateTimeFormatter.ofPattern("dd MMM", Locale.US)

            for (tx in periodTransactions) {
                val rate = secondaryMap[tx.currencyCode]?.exchangeRateToPrimary ?: 1.0
                val txCurrencySpec = SupportedCurrencies.fromCode(tx.currencyCode)
                val convertedMinor = MoneyFormatterAndCalculator.convertMinorMoney(
                    sourceMinor = tx.amount.amountMinor,
                    sourceFractionDigits = txCurrencySpec.fractionDigits,
                    targetFractionDigits = primaryCurrencySpec.fractionDigits,
                    rateToTarget = rate
                )

                when (tx.type) {
                    TransactionType.INCOME,
                    TransactionType.REFUND -> {
                        grossInflowsMinor += convertedMinor
                        val catName = tx.categoryName ?: "Miscellaneous Income"
                        incomeByCategory.getOrPut(catName) { mutableListOf() }.add(convertedMinor)
                    }
                    TransactionType.EXPENSE -> {
                        totalExpensesMinor += convertedMinor
                        val catName = tx.categoryName ?: "General Expense"
                        expenseByCategory.getOrPut(catName) { mutableListOf() }.add(convertedMinor)

                        val isTaxDeductible = if (selectedTaxTags.isNotEmpty()) {
                            tx.tags.any { it in selectedTaxTags }
                        } else {
                            tx.tags.any {
                                it.contains("tax", true) || it.contains("business", true) ||
                                        it.contains("deduct", true) || it.contains("medical", true) ||
                                        it.contains("donation", true) || it.contains("charity", true)
                            } || (tx.categoryName != null && (
                                    tx.categoryName.contains("tax", true) || tx.categoryName.contains("donation", true) ||
                                            tx.categoryName.contains("charity", true) || tx.categoryName.contains("medical", true) ||
                                            tx.categoryName.contains("insurance", true)
                                    ))
                        }

                        if (isTaxDeductible) {
                            val matchedTag = tx.tags.firstOrNull()?.let { "#$it" } ?: "Deductible"
                            val txDate = LocalDate.ofEpochDay(tx.ledgerDateEpochDay)
                            taxDeductions.add(
                                TaxDeductionPdfItem(
                                    dateText = txDate.format(dFmt),
                                    payee = tx.payeeOrDescription.ifBlank { tx.categoryName ?: "Expense" },
                                    categoryName = tx.categoryName ?: "General",
                                    tagOrDeductionType = matchedTag,
                                    note = tx.note.ifBlank { null },
                                    amountMinor = tx.amount.amountMinor,
                                    currencyCode = tx.currencyCode,
                                    convertedMinor = convertedMinor
                                )
                            )
                        }
                    }
                    else -> Unit
                }
            }

            val incomeSchedules = incomeByCategory.map { (catName, minors) ->
                val sum = minors.sum()
                val pct = if (grossInflowsMinor > 0L) (sum.toFloat() / grossInflowsMinor.toFloat()) * 100f else 0f
                CategoryAggregateItem(
                    categoryName = catName,
                    count = minors.size,
                    amountMinor = sum,
                    convertedAmountMinor = sum,
                    percentageOfTotal = pct
                )
            }.sortedByDescending { it.convertedAmountMinor }

            val expenseSchedules = expenseByCategory.map { (catName, minors) ->
                val sum = minors.sum()
                val pct = if (totalExpensesMinor > 0L) (sum.toFloat() / totalExpensesMinor.toFloat()) * 100f else 0f
                CategoryAggregateItem(
                    categoryName = catName,
                    count = minors.size,
                    amountMinor = sum,
                    convertedAmountMinor = sum,
                    percentageOfTotal = pct
                )
            }.sortedByDescending { it.convertedAmountMinor }

            val totalTaxDeductionsMinor = taxDeductions.sumOf { it.convertedMinor }
            val netSavingsMinor = grossInflowsMinor - totalExpensesMinor
            val savingsRate = if (grossInflowsMinor > 0L) {
                ((netSavingsMinor.toFloat() / grossInflowsMinor.toFloat()) * 100f).coerceAtLeast(0f)
            } else 0f

            // Account Balance Sheet Snapshot as of endEpochDay
            var totalAssetsMinor = 0L
            var totalLiabilitiesMinor = 0L
            val accountsSnapshot = mutableListOf<AccountSnapshotPdfItem>()

            for (acc in allAccountEntities.filter { !it.isArchived }) {
                val accEffectiveIds = setOf(acc.id)
                val accTxChronological = allTransactions
                    .filter { tx ->
                        when (tx.type) {
                            TransactionType.EXPENSE,
                            TransactionType.INCOME,
                            TransactionType.REFUND,
                            TransactionType.OPENING_BALANCE,
                            TransactionType.RECONCILIATION -> tx.accountId in accEffectiveIds
                            TransactionType.TRANSFER,
                            TransactionType.CC_SETTLEMENT -> tx.accountId in accEffectiveIds ||
                                    (tx.transferToAccountId != null && tx.transferToAccountId in accEffectiveIds)
                        }
                    }

                val hasOpening = accTxChronological.any { it.type == TransactionType.OPENING_BALANCE }
                val initialAnchor = if (hasOpening) {
                    0L
                } else if (acc.groupType.isLiabilityByDefault && acc.openingBalanceMinor > 0L) {
                    Math.negateExact(acc.openingBalanceMinor)
                } else {
                    acc.openingBalanceMinor
                }
                var running = initialAnchor
                for (tx in accTxChronological) {
                    if (tx.ledgerDateEpochDay <= endEpochDay) {
                        val delta = AccountingRulesEngine.computeSignedDeltaMinorForAccount(tx, accEffectiveIds)
                        running += delta
                    }
                }

                val rate = secondaryMap[acc.currencyCode]?.exchangeRateToPrimary ?: 1.0
                val accCurrencySpec = SupportedCurrencies.fromCode(acc.currencyCode)
                val converted = MoneyFormatterAndCalculator.convertMinorMoney(
                    sourceMinor = running,
                    sourceFractionDigits = accCurrencySpec.fractionDigits,
                    targetFractionDigits = primaryCurrencySpec.fractionDigits,
                    rateToTarget = rate
                )

                val isAsset = acc.groupType != AccountGroup.CREDIT_CARD && acc.groupType != AccountGroup.LOAN
                if (isAsset) {
                    totalAssetsMinor += converted
                } else {
                    totalLiabilitiesMinor += Math.abs(converted)
                }

                val groupName = when (acc.groupType) {
                    AccountGroup.BANK_ACCOUNT -> "Bank"
                    AccountGroup.CREDIT_CARD -> "Credit Card"
                    AccountGroup.DEBIT_CARD -> "Debit Card"
                    AccountGroup.CASH -> "Cash"
                    AccountGroup.INVESTMENT -> "Investment"
                    AccountGroup.LOAN -> "Loan"
                    AccountGroup.SAVINGS -> "Savings"
                    else -> "Other"
                }

                accountsSnapshot.add(
                    AccountSnapshotPdfItem(
                        accountName = acc.name,
                        groupName = groupName,
                        currencyCode = acc.currencyCode,
                        nativeBalanceMinor = running,
                        convertedBalanceMinor = converted,
                        isAsset = isAsset
                    )
                )
            }

            val netWorthMinor = totalAssetsMinor - totalLiabilitiesMinor

            val valuationNotes = secondaryCurrencies.map { sec ->
                "1 ${sec.code} = ${sec.exchangeRateToPrimary} ${primaryCurrencySpec.code}"
            }

            val data = AnnualTaxPdfData(
                fiscalPeriodTitle = rangeInfo.title,
                fiscalPeriodSubtitle = rangeInfo.subtitle,
                fiscalYearType = fiscalYearType,
                primaryCurrencySpec = primaryCurrencySpec,
                secondaryCurrencies = secondaryCurrencies,
                numberGrouping = prefs.numberGrouping,
                grossInflowsMinor = grossInflowsMinor,
                totalExpensesMinor = totalExpensesMinor,
                netSavingsMinor = netSavingsMinor,
                savingsRatePercent = savingsRate,
                incomeSources = incomeSchedules,
                expenseCategories = expenseSchedules,
                taxDeductions = taxDeductions,
                totalTaxDeductionsMinor = totalTaxDeductionsMinor,
                accountsSnapshot = accountsSnapshot,
                totalAssetsMinor = totalAssetsMinor,
                totalLiabilitiesMinor = totalLiabilitiesMinor,
                netWorthMinor = netWorthMinor,
                valuationExchangeNotes = valuationNotes,
                generatedTimestampText = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.US))
            )

            PdfStatementEngine.generateAnnualTaxStatementPdf(context, data).getOrThrow()
        }
    }

}

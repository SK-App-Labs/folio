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

class CsvExportImportEngine(
    private val context: Context,
    private val database: FolioDatabase,
    private val userPreferences: UserPreferencesDataSource
) {

    suspend fun exportTransactionsToCsv(
        destinationUriString: String,
        startEpochDay: Long?,
        endEpochDay: Long?,
        accountId: Long?
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            StorageAccessUtils.openOutputStreamForLocation(context, destinationUriString).use { rawStream ->
                exportTransactionsToCsvStream(
                    outputStream = rawStream,
                    startEpochDay = startEpochDay,
                    endEpochDay = endEpochDay,
                    accountId = accountId
                )
            }
        }
    }

    suspend fun exportCsvTemplate(
        destinationUriString: String
    ): Result<File?> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            StorageAccessUtils.openOutputStreamForLocation(context, destinationUriString).use { rawStream ->
                exportCsvTemplateStream(rawStream)
            }
            val cleanFileName = destinationUriString
                .substringAfterLast('/')
                .substringAfterLast('\\')
                .ifBlank { "Folio_Import_Template.csv" }
            val internalDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "FolioBackups")
            val internalFile = File(internalDir, cleanFileName)
            if (internalFile.exists()) internalFile else null
        }
    }

    suspend fun exportCsvTemplateStream(
        outputStream: OutputStream
    ): Unit = withContext(Dispatchers.IO) {
        outputStream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
        val writer = BufferedWriter(OutputStreamWriter(outputStream, StandardCharsets.UTF_8))
        writer.write(generateSideBySideCsvTemplateContent())
        writer.flush()
    }

    suspend fun previewTransactionsCsv(
        sourceUriString: String
    ): Result<ImportCsvPreview> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            // Cache incoming file locally to prevent Android Scoped Storage URI permission expiration
            val cachedFile = File(context.cacheDir, "pending_import_${System.currentTimeMillis()}.csv")
            StorageAccessUtils.openInputStreamForLocation(context, sourceUriString).use { input ->
                cachedFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            cachedFile.inputStream().use { cachedStream ->
                previewCsvStream(cachedStream, cachedFile.absolutePath)
            }
        }
    }

    suspend fun importTransactionsFromCsv(
        sourceUriString: String,
        skipDuplicates: Boolean
    ): Result<ImportCsvResult> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            StorageAccessUtils.openInputStreamForLocation(context, sourceUriString).use { rawStream ->
                importCsvStream(rawStream, skipDuplicates)
            }
        }.also {
            // Clean up cached pending import file if source was in cacheDir
            if (sourceUriString.contains("pending_import_")) {
                runCatching { File(sourceUriString).delete() }
            }
        }
    }

    suspend fun exportTransactionsToCsvStream(
        outputStream: OutputStream,
        startEpochDay: Long? = null,
        endEpochDay: Long? = null,
        accountId: Long? = null
    ): Int = withContext(Dispatchers.IO) {
        val allAccounts = database.accountDao().getAllAccountsOnce()
        val accountsById = allAccounts.associateBy { it.id }

        val allCategories = database.categoryAndTagDao().getAllCategoriesOnce()
        val categoriesById = allCategories.associateBy { it.id }

        val allTransactions = database.transactionDao().getAllTransactionsWithDetailsOnce()
        val openingDatesByAccountId = allTransactions
            .filter { it.transaction.type == TransactionType.OPENING_BALANCE }
            .associate { it.transaction.accountId to it.transaction.ledgerDateEpochDay }
        val filteredTransactions = allTransactions.filter { txWithDetails ->
            val day = txWithDetails.transaction.ledgerDateEpochDay
            val afterStart = startEpochDay == null || day >= startEpochDay
            val beforeEnd = endEpochDay == null || day <= endEpochDay
            val matchesAccount = accountId == null ||
                txWithDetails.transaction.accountId == accountId ||
                txWithDetails.transaction.transferToAccountId == accountId
            afterStart && beforeEnd && matchesAccount
        }

        // Write UTF-8 BOM so Microsoft Excel opens the CSV seamlessly with UTF-8 characters
        outputStream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
        val writer = BufferedWriter(OutputStreamWriter(outputStream, StandardCharsets.UTF_8))

        // 1. ACCOUNTS & STARTING BALANCES SECTION
        writer.write("[ACCOUNTS]")
        writer.newLine()
        writer.write("Name,Group,Currency,StartingBalance,AnchorDate,CreditLimit,SettlementDay")
        writer.newLine()
        writer.write("# Hint: (Account Name),(BANK_ACCOUNT|CREDIT_CARD|CASH|SAVINGS|PREPAID_WALLET|LOAN),(Currency ISO),(Starting Balance e.g. 50000.00),(Anchor Date YYYY-MM-DD),(Credit Limit),(Settlement Day 1-31)")
        writer.newLine()
        val exportedAccounts = if (accountId != null) {
            allAccounts.filter { it.id == accountId }
        } else {
            allAccounts
        }
        for (acc in exportedAccounts) {
            val spec = SupportedCurrencies.fromCode(acc.currencyCode)
            val balanceStr = formatDecimalAmountPlain(acc.openingBalanceMinor, spec.fractionDigits)
            val anchorEpochDay = openingDatesByAccountId[acc.id] ?: (acc.createdAtEpochMillis / 86400000L)
            val anchorDateStr = LocalDate.ofEpochDay(anchorEpochDay).toString()
            val limitStr = acc.creditLimitMinor?.let { formatDecimalAmountPlain(it, spec.fractionDigits) } ?: ""
            val billDayStr = acc.settlementDayOfMonth?.toString() ?: ""
            val row = listOf(
                escapeCsvField(acc.name),
                acc.groupType.name,
                acc.currencyCode,
                balanceStr,
                anchorDateStr,
                limitStr,
                billDayStr
            )
            writer.write(row.joinToString(","))
            writer.newLine()
        }
        writer.newLine()

        // 2. CATEGORIES & SUBCATEGORIES SECTION
        writer.write("[CATEGORIES]")
        writer.newLine()
        writer.write("MainCategory,Subcategory,Type,Icon,Color")
        writer.newLine()
        writer.write("# Hint: (Main Category Name),(Optional Subcategory),(EXPENSE|INCOME),(Icon Name e.g. restaurant),(Hex Color e.g. #FF5722)")
        writer.newLine()
        for (cat in allCategories) {
            val parent = cat.parentId?.let { categoriesById[it] }
            val mainName = parent?.name ?: cat.name
            val subName = if (parent != null) cat.name else ""
            val row = listOf(
                escapeCsvField(mainName),
                escapeCsvField(subName),
                cat.type.name,
                escapeCsvField(cat.iconName),
                escapeCsvField(cat.colorHex)
            )
            writer.write(row.joinToString(","))
            writer.newLine()
        }
        writer.newLine()

        // 3. TRANSACTIONS SECTION
        writer.write("[TRANSACTIONS]")
        writer.newLine()
        writer.write(
            "ID,Date,Type,Amount,Currency,Account,TransferToAccount,Category,Subcategory,TransferFee,PayeeOrDescription,Note,Tags"
        )
        writer.newLine()
        writer.write("# Hint: (Optional ID),(YYYY-MM-DD),(EXPENSE|INCOME|TRANSFER|CC_SETTLEMENT|REFUND),(Amount e.g. 250.50),(Currency),(Source Account),(Destination Account if Transfer),(Leave blank if Transfer),(Subcategory),(Transfer Fee),(Payee/Description),(Note),(Tags semicolon separated)")
        writer.newLine()

        for (item in filteredTransactions) {
            val tx = item.transaction
            val dateIso = LocalDate.ofEpochDay(tx.ledgerDateEpochDay).toString()
            val accountName = accountsById[tx.accountId]?.name ?: ""
            val transferAccountName = tx.transferToAccountId?.let { accountsById[it]?.name } ?: ""

            val isTransfer = tx.type == TransactionType.TRANSFER || tx.type == TransactionType.CC_SETTLEMENT
            val categoryEntity = if (isTransfer) null else tx.categoryId?.let { categoriesById[it] }
            val parentCategoryEntity = categoryEntity?.parentId?.let { categoriesById[it] }
            val mainCategoryName = when {
                tx.isSplit -> "Split Transaction"
                parentCategoryEntity != null -> parentCategoryEntity.name
                categoryEntity != null -> categoryEntity.name
                else -> ""
            }
            val subCategoryName = when {
                tx.isSplit -> item.splits.joinToString(" | ") { split ->
                    val splitCatName = categoriesById[split.categoryId]?.name ?: "Category"
                    "$splitCatName:${split.amountMinor}"
                }
                parentCategoryEntity != null -> categoryEntity.name
                else -> ""
            }

            val currencySpec = SupportedCurrencies.fromCode(tx.currencyCode)
            val formattedAmount = formatDecimalAmountPlain(tx.amountMinor, currencySpec.fractionDigits)
            val formattedFee = if (tx.transferFeeMinor > 0L) {
                formatDecimalAmountPlain(tx.transferFeeMinor, currencySpec.fractionDigits)
            } else {
                ""
            }
            val tagsJoined = item.tags.joinToString(";") { it.name }

            val rowColumns = listOf(
                tx.id.toString(),
                dateIso,
                tx.type.name,
                formattedAmount,
                tx.currencyCode,
                escapeCsvField(accountName),
                escapeCsvField(transferAccountName),
                escapeCsvField(mainCategoryName),
                escapeCsvField(subCategoryName),
                formattedFee,
                escapeCsvField(tx.payeeOrDescription),
                escapeCsvField(tx.note),
                escapeCsvField(tagsJoined)
            )
            writer.write(rowColumns.joinToString(","))
            writer.newLine()
        }

        writer.flush()
        filteredTransactions.size
    }


    private fun escapeCsvField(value: String): String {
        if (value.isEmpty()) return ""
        val needsQuotes = value.contains(',') ||
                value.contains('"') ||
                value.contains('\n') ||
                value.contains('\r')
        return if (needsQuotes) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }


    private suspend fun previewCsvStream(
        inputStream: java.io.InputStream,
        sourceUriString: String
    ): ImportCsvPreview {
        val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))
        var firstLine: String? = null
        while (true) {
            val raw = reader.readLine() ?: break
            val clean = raw.trim().removePrefix("\uFEFF").removePrefix("ï»¿")
            if (clean.isNotEmpty() && !clean.startsWith("#") && !clean.startsWith("//")) {
                firstLine = clean
                break
            }
        }
        if (firstLine == null) throw IllegalArgumentException("The selected CSV file is empty.")

        val existingAccounts = database.accountDao().getAllAccountsOnce()
        val existingAccountNames = existingAccounts.map { it.name.trim().lowercase() }.toSet()
        val existingCategories = database.categoryAndTagDao().getAllCategoriesOnce()
        val existingCategoryNames = existingCategories.map { it.name.trim().lowercase() }.toSet()
        val defaultCurrency = runCatching {
            userPreferences.observeUserPreferences().first().primaryCurrencyCode
        }.getOrNull()?.ifBlank { "INR" } ?: "INR"

        val existingTransactions = database.transactionDao().getAllTransactionsRawOnce()
        val accountMap = existingAccounts.associateBy { it.name.trim().lowercase() }

        val existingFingerprints = existingTransactions.map { tx ->
            "${tx.ledgerDateEpochDay}|${tx.accountId}|${tx.type.name}|${tx.amountMinor}|${tx.payeeOrDescription.trim().lowercase()}"
        }.toMutableSet()

        var currentSection = CsvSectionState.LEGACY
        var colMap: Map<String, Int> = emptyMap()
        var sideBySideColMap: SideBySideColMap? = null

        val detectedFirst = detectSectionHeader(firstLine)
        if (detectedFirst != null) {
            currentSection = detectedFirst
        } else {
            val headerTokens = parseCsvLine(firstLine)
            if (isSideBySideHeader(headerTokens)) {
                currentSection = CsvSectionState.SIDE_BY_SIDE
                sideBySideColMap = parseSideBySideColMap(headerTokens)
            } else {
                colMap = headerTokens.mapIndexed { idx, name ->
                    name.trim().lowercase().replace("_", "").replace(" ", "") to idx
                }.toMap()
            }
        }

        var totalRows = 0
        var validCount = 0
        var duplicateCount = 0
        val duplicateSamples = mutableListOf<DuplicateTransactionSample>()
        val newAccounts = mutableSetOf<String>()
        val newCategories = mutableSetOf<String>()
        val accountsWithOpeningBalance = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        var line = reader.readLine()
        var lineNum = 1
        while (line != null) {
            lineNum++
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.all { it == ',' || it.isWhitespace() }) {
                val sec = detectSectionHeader(trimmed)
                if (sec != null) {
                    currentSection = sec
                    colMap = emptyMap()
                    sideBySideColMap = null
                    line = reader.readLine()
                    continue
                }

                if (trimmed.startsWith("#") || trimmed.startsWith("//")) {
                    line = reader.readLine()
                    continue
                }

                if (colMap.isEmpty() && sideBySideColMap == null) {
                    val tokens = parseCsvLine(trimmed)
                    if (currentSection == CsvSectionState.SIDE_BY_SIDE || isSideBySideHeader(tokens)) {
                        currentSection = CsvSectionState.SIDE_BY_SIDE
                        sideBySideColMap = parseSideBySideColMap(tokens)
                    } else {
                        colMap = tokens.mapIndexed { idx, name ->
                            name.trim().lowercase().replace("_", "").replace(" ", "") to idx
                        }.toMap()
                    }
                    line = reader.readLine()
                    continue
                }

                totalRows++
                val tokens = parseCsvLine(trimmed)

                when (currentSection) {
                    CsvSectionState.SIDE_BY_SIDE -> {
                        val sbs = sideBySideColMap ?: parseSideBySideColMap(tokens).also { sideBySideColMap = it }
                        val accName = sbs.accNameIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                        if (accName.isNotEmpty()) {
                            if (!existingAccountNames.contains(accName.lowercase())) {
                                newAccounts.add(accName)
                            }
                            val balIdx = sbs.accStartingBalIdx
                            val dateIdx = sbs.accAnchorDateIdx
                            val rawBal = balIdx?.let { tokens.getOrNull(it)?.trim() }
                            val rawDate = dateIdx?.let { tokens.getOrNull(it)?.trim() }
                            if (!rawBal.isNullOrEmpty() && rawBal != "0" && rawBal != "0.00") {
                                val anchorStr = if (!rawDate.isNullOrEmpty()) " as of $rawDate" else ""
                                accountsWithOpeningBalance.add("$accName: Starting $rawBal$anchorStr")
                            }
                        }

                        val mainName = sbs.catMainIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                        val subName = sbs.catSubIdx?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { null }
                        if (mainName.isNotEmpty()) {
                            if (!existingCategoryNames.contains(mainName.lowercase())) {
                                newCategories.add(mainName)
                            }
                            if (subName != null) {
                                newCategories.add("$mainName › $subName")
                            }
                        }

                        val rawDate = sbs.txDateIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                        val rawAmount = sbs.txAmountIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                        val accountName = sbs.txAccountIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""

                        if (rawDate.isNotEmpty() || rawAmount.isNotEmpty() || accountName.isNotEmpty()) {
                            val epochDay = parseDateStringToEpochDay(rawDate)
                            val amountMinor = parseAmountDecimalToMinor(rawAmount, 2)

                            if (epochDay == null) {
                                if (warnings.size < 5) warnings.add("Row $lineNum: Unrecognized date format '$rawDate'")
                            } else if (amountMinor == null) {
                                if (warnings.size < 5) warnings.add("Row $lineNum: Invalid amount '$rawAmount'")
                            } else if (accountName.isEmpty()) {
                                if (warnings.size < 5) warnings.add("Row $lineNum: Missing account name")
                            } else {
                                if (!existingAccountNames.contains(accountName.lowercase())) {
                                    newAccounts.add(accountName)
                                }

                                val transferAccountIdx = sbs.txTransferAccountIdx
                                val transferAccountName = transferAccountIdx?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { null }
                                if (transferAccountName != null && !existingAccountNames.contains(transferAccountName.lowercase())) {
                                    newAccounts.add(transferAccountName)
                                }

                                val typeIdx = sbs.txTypeIdx
                                val rawType = typeIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                                val type = when {
                                    rawType.equals("TRANSFER", ignoreCase = true) -> TransactionType.TRANSFER
                                    rawType.equals("CC_SETTLEMENT", ignoreCase = true) || rawType.contains("SETTLEMENT", ignoreCase = true) -> TransactionType.CC_SETTLEMENT
                                    rawType.equals("EXPENSE", ignoreCase = true) -> TransactionType.EXPENSE
                                    rawType.equals("INCOME", ignoreCase = true) -> TransactionType.INCOME
                                    rawType.equals("REFUND", ignoreCase = true) -> TransactionType.REFUND
                                    rawType.equals("OPENING_BALANCE", ignoreCase = true) -> TransactionType.OPENING_BALANCE
                                    rawType.equals("RECONCILIATION", ignoreCase = true) -> TransactionType.RECONCILIATION
                                    transferAccountName != null -> TransactionType.TRANSFER
                                    amountMinor < 0L -> TransactionType.EXPENSE
                                    else -> TransactionType.INCOME
                                }

                                val isTransfer = type == TransactionType.TRANSFER || type == TransactionType.CC_SETTLEMENT
                                val catIdx = sbs.txCategoryIdx
                                val subCatIdx = sbs.txSubcategoryIdx
                                val categoryName = if (isTransfer) null else catIdx?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { null }
                                val subCategoryName = if (isTransfer) null else subCatIdx?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { null }
                                if (categoryName != null) {
                                    if (!existingCategoryNames.contains(categoryName.lowercase())) {
                                        newCategories.add(categoryName)
                                    }
                                    if (subCategoryName != null) {
                                        newCategories.add("$categoryName › $subCategoryName")
                                    }
                                }

                                val payeeIdx = sbs.txPayeeIdx
                                val payee = payeeIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""

                                val existingAcc = accountMap[accountName.lowercase()]
                                val fingerprint = if (existingAcc != null) {
                                    "${epochDay}|${existingAcc.id}|${type.name}|${Math.abs(amountMinor)}|${payee.lowercase()}"
                                } else {
                                    null
                                }

                                if (fingerprint != null && existingFingerprints.contains(fingerprint)) {
                                    duplicateCount++
                                    if (duplicateSamples.size < 20) {
                                        duplicateSamples.add(
                                            DuplicateTransactionSample(
                                                date = rawDate.ifBlank { "Unknown Date" },
                                                accountName = accountName.ifBlank { "Account" },
                                                payee = payee.ifBlank { "(No payee)" },
                                                amount = rawAmount.ifBlank { "0" }
                                            )
                                        )
                                    }
                                } else {
                                    validCount++
                                    if (fingerprint != null) {
                                        existingFingerprints.add(fingerprint)
                                    }
                                }
                            }
                        }
                    }
                    CsvSectionState.ACCOUNTS -> {
                        val nameIdx = colMap["name"] ?: colMap["account"] ?: colMap["accountname"] ?: 0
                        val name = tokens.getOrNull(nameIdx)?.trim() ?: ""
                        if (name.isNotEmpty()) {
                            if (!existingAccountNames.contains(name.lowercase())) {
                                newAccounts.add(name)
                            }
                            val balIdx = colMap["startingbalance"] ?: colMap["openingbalance"] ?: colMap["balance"]
                            val dateIdx = colMap["anchordate"] ?: colMap["openingdate"] ?: colMap["date"]
                            val rawBal = balIdx?.let { tokens.getOrNull(it)?.trim() }
                            val rawDate = dateIdx?.let { tokens.getOrNull(it)?.trim() }
                            if (!rawBal.isNullOrEmpty() && rawBal != "0" && rawBal != "0.00") {
                                val anchorStr = if (!rawDate.isNullOrEmpty()) " as of $rawDate" else ""
                                accountsWithOpeningBalance.add("$name: Starting $rawBal$anchorStr")
                            }
                        }
                    }
                    CsvSectionState.CATEGORIES -> {
                        val mainIdx = colMap["maincategory"] ?: colMap["category"] ?: colMap["name"] ?: 0
                        val subIdx = colMap["subcategory"] ?: colMap["subname"] ?: colMap["sub"]
                        val mainName = tokens.getOrNull(mainIdx)?.trim() ?: ""
                        val subName = subIdx?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { null }
                        if (mainName.isNotEmpty()) {
                            if (!existingCategoryNames.contains(mainName.lowercase())) {
                                newCategories.add(mainName)
                            }
                            if (subName != null) {
                                newCategories.add("$mainName â€º $subName")
                            }
                        }
                    }
                    CsvSectionState.TRANSACTIONS,
                    CsvSectionState.LEGACY -> {
                        val dateIdx = colMap["date"] ?: colMap["txdate"] ?: colMap["transactiondate"] ?: colMap["txndate"] ?: colMap["postingdate"] ?: colMap["valuedate"]
                        val amountIdx = colMap["amount"] ?: colMap["amountformatted"] ?: colMap["amountminor"] ?: colMap["txnamount"] ?: colMap["transactionamount"] ?: colMap["total"] ?: colMap["netamount"] ?: colMap["value"]
                        val accountIdx = colMap["account"] ?: colMap["accountname"] ?: colMap["fromaccount"] ?: colMap["sourceaccount"] ?: colMap["bank"] ?: colMap["wallet"]

                        if (dateIdx == null || amountIdx == null || accountIdx == null) {
                            val missingCols = mutableListOf<String>()
                            if (dateIdx == null) missingCols.add("Date")
                            if (amountIdx == null) missingCols.add("Amount")
                            if (accountIdx == null) missingCols.add("Account")
                            if (warnings.size < 5) warnings.add("Row $lineNum: Missing column(s) in header: ${missingCols.joinToString(", ")}.")
                        } else {
                            val rawDate = tokens.getOrNull(dateIdx) ?: ""
                            val epochDay = parseDateStringToEpochDay(rawDate)
                            val rawAmount = tokens.getOrNull(amountIdx) ?: ""
                            val amountMinor = parseAmountDecimalToMinor(rawAmount, 2)
                            val accountName = tokens.getOrNull(accountIdx)?.trim() ?: ""

                            if (epochDay == null) {
                                if (warnings.size < 5) warnings.add("Row $lineNum: Unrecognized date format '$rawDate'")
                            } else if (amountMinor == null) {
                                if (warnings.size < 5) warnings.add("Row $lineNum: Invalid amount '$rawAmount'")
                            } else if (accountName.isEmpty()) {
                                if (warnings.size < 5) warnings.add("Row $lineNum: Missing account name")
                            } else {
                                if (!existingAccountNames.contains(accountName.lowercase())) {
                                    newAccounts.add(accountName)
                                }

                                val transferAccountIdx = colMap["transfertoaccount"] ?: colMap["transferto"]
                                val transferAccountName = transferAccountIdx?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { null }
                                if (transferAccountName != null && !existingAccountNames.contains(transferAccountName.lowercase())) {
                                    newAccounts.add(transferAccountName)
                                }

                                val typeIdx = colMap["type"]
                                val rawType = typeIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                                val type = when {
                                    rawType.equals("TRANSFER", ignoreCase = true) -> TransactionType.TRANSFER
                                    rawType.equals("CC_SETTLEMENT", ignoreCase = true) || rawType.contains("SETTLEMENT", ignoreCase = true) -> TransactionType.CC_SETTLEMENT
                                    rawType.equals("EXPENSE", ignoreCase = true) -> TransactionType.EXPENSE
                                    rawType.equals("INCOME", ignoreCase = true) -> TransactionType.INCOME
                                    rawType.equals("REFUND", ignoreCase = true) -> TransactionType.REFUND
                                    rawType.equals("OPENING_BALANCE", ignoreCase = true) -> TransactionType.OPENING_BALANCE
                                    rawType.equals("RECONCILIATION", ignoreCase = true) -> TransactionType.RECONCILIATION
                                    transferAccountName != null -> TransactionType.TRANSFER
                                    amountMinor < 0L -> TransactionType.EXPENSE
                                    else -> TransactionType.INCOME
                                }

                                val isTransfer = type == TransactionType.TRANSFER || type == TransactionType.CC_SETTLEMENT
                                val catIdx = colMap["category"]
                                val categoryName = if (isTransfer) null else catIdx?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { null }
                                if (categoryName != null && !existingCategoryNames.contains(categoryName.lowercase())) {
                                    newCategories.add(categoryName)
                                }

                                val payeeIdx = colMap["payeeordescription"] ?: colMap["payee"] ?: colMap["description"]
                                val payee = payeeIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""

                                val existingAcc = accountMap[accountName.lowercase()]
                                val fingerprint = if (existingAcc != null) {
                                    "${epochDay}|${existingAcc.id}|${type.name}|${Math.abs(amountMinor)}|${payee.lowercase()}"
                                } else {
                                    null
                                }

                                if (fingerprint != null && existingFingerprints.contains(fingerprint)) {
                                    duplicateCount++
                                    if (duplicateSamples.size < 20) {
                                        duplicateSamples.add(
                                            DuplicateTransactionSample(
                                                date = rawDate.ifBlank { "Unknown Date" },
                                                accountName = accountName.ifBlank { "Account" },
                                                payee = payee.ifBlank { "(No payee)" },
                                                amount = rawAmount.ifBlank { "0" }
                                            )
                                        )
                                    }
                                } else {
                                    validCount++
                                    if (fingerprint != null) {
                                        existingFingerprints.add(fingerprint)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            line = reader.readLine()
        }

        return ImportCsvPreview(
            sourceUriString = sourceUriString,
            totalRowsRead = totalRows,
            validTransactionCount = validCount,
            duplicateCount = duplicateCount,
            newAccountsToCreate = newAccounts.toList(),
            newCategoriesToCreate = newCategories.toList(),
            warningMessages = warnings,
            accountsWithOpeningBalance = accountsWithOpeningBalance,
            duplicateSamples = duplicateSamples
        )
    }

    private suspend fun importCsvStream(
        inputStream: java.io.InputStream,
        skipDuplicates: Boolean
    ): ImportCsvResult = database.withTransaction {
        val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))
        var firstLine: String? = null
        while (true) {
            val raw = reader.readLine() ?: break
            val clean = raw.trim().removePrefix("\uFEFF").removePrefix("ï»¿")
            if (clean.isNotEmpty() && !clean.startsWith("#") && !clean.startsWith("//")) {
                firstLine = clean
                break
            }
        }
        if (firstLine == null) throw IllegalArgumentException("The selected CSV file is empty.")

        val accountDao = database.accountDao()
        val catTagDao = database.categoryAndTagDao()
        val txDao = database.transactionDao()

        val defaultCurrency = runCatching {
            userPreferences.observeUserPreferences().first().primaryCurrencyCode
        }.getOrNull()?.ifBlank { "INR" } ?: "INR"

        val accountNameToEntity = accountDao.getAllAccountsOnce()
            .associateBy { it.name.trim().lowercase() }
            .toMutableMap()

        val allCategories = catTagDao.getAllCategoriesOnce().toMutableList()
        val categoryNameToEntity = allCategories
            .associateBy { "${it.name.trim().lowercase()}|${it.type.name}" }
            .toMutableMap()

        val tagNameToEntity = catTagDao.getAllTagsOnce()
            .associateBy { it.name.trim().lowercase() }
            .toMutableMap()

        val rawTxs = txDao.getAllTransactionsRawOnce()
        val existingTxById = rawTxs.associateBy { it.id }

        val existingFingerprints = rawTxs.map { tx ->
            "${tx.ledgerDateEpochDay}|${tx.accountId}|${tx.type.name}|${tx.amountMinor}|${tx.payeeOrDescription.trim().lowercase()}"
        }.toMutableSet()

        val existingAnchorKeys = rawTxs
            .filter { it.type == TransactionType.OPENING_BALANCE }
            .map { "${it.accountId}|${it.ledgerDateEpochDay}" }
            .toMutableSet()

        suspend fun getOrCreateAccount(
            name: String,
            currency: String,
            groupOverride: AccountGroup? = null,
            limitMinor: Long? = null,
            billDay: Int? = null,
            anchorDateEpochDay: Long? = null
        ): AccountEntity {
            val key = name.trim().lowercase()
            accountNameToEntity[key]?.let { return it }

            val group = groupOverride ?: parseAccountGroupFromTokens(null, name)
            val defaultColor = when (group) {
                AccountGroup.CREDIT_CARD -> "#E11D48"
                AccountGroup.CASH -> "#D97706"
                AccountGroup.BANK_ACCOUNT -> "#059669"
                AccountGroup.LOAN -> "#BE123C"
                else -> "#475569"
            }
            val defaultIcon = when (group) {
                AccountGroup.CREDIT_CARD -> "credit_card"
                AccountGroup.CASH -> "account_balance_wallet"
                AccountGroup.BANK_ACCOUNT -> "account_balance"
                AccountGroup.LOAN -> "trending_down"
                else -> "payments"
            }

            val anchorMillis = anchorDateEpochDay?.let { it * 86400000L + 12 * 3600 * 1000L } ?: System.currentTimeMillis()
            val newEntity = AccountEntity(
                id = 0L,
                name = name.trim(),
                groupType = group,
                currencyCode = currency.ifBlank { "INR" },
                openingBalanceMinor = 0L,
                creditLimitMinor = limitMinor,
                settlementDayOfMonth = billDay,
                colorHex = defaultColor,
                iconName = defaultIcon,
                createdAtEpochMillis = anchorMillis
            )
            val newId = accountDao.insertNewAccountRaw(newEntity)
            val created = newEntity.copy(id = newId)
            accountNameToEntity[key] = created
            return created
        }

        suspend fun getOrCreateCategory(
            name: String?,
            subName: String?,
            type: CategoryType,
            iconOverride: String? = null,
            colorOverride: String? = null
        ): CategoryEntity? {
            val mainTrimmed = name?.trim()?.ifEmpty { null } ?: return null
            val subTrimmed = subName?.trim()?.ifEmpty { null }

            val mainKey = "${mainTrimmed.lowercase()}|${type.name}"
            var parent = categoryNameToEntity[mainKey]
            if (parent == null) {
                val suggestion = suggestIconAndColorForCategoryName(mainTrimmed, type)
                val newMain = CategoryEntity(
                    id = 0L,
                    name = mainTrimmed,
                    type = type,
                    parentId = null,
                    iconName = iconOverride ?: suggestion?.first ?: if (type == CategoryType.EXPENSE) "shopping_bag" else "payments",
                    colorHex = colorOverride ?: suggestion?.second ?: if (type == CategoryType.EXPENSE) "#E11D48" else "#059669"
                )
                val newId = catTagDao.insertNewCategoryRaw(newMain)
                parent = newMain.copy(id = newId)
                categoryNameToEntity[mainKey] = parent
                allCategories.add(parent)
            }

            if (subTrimmed == null) return parent

            val existingSub = allCategories.firstOrNull {
                it.parentId == parent.id && it.name.equals(subTrimmed, ignoreCase = true)
            }
            if (existingSub != null) return existingSub

            val subSuggestion = suggestIconAndColorForCategoryName(subTrimmed, type)
            val newSub = CategoryEntity(
                id = 0L,
                name = subTrimmed,
                type = type,
                parentId = parent.id,
                iconName = iconOverride ?: subSuggestion?.first ?: parent.iconName,
                colorHex = colorOverride ?: subSuggestion?.second ?: parent.colorHex
            )
            val subId = catTagDao.insertNewCategoryRaw(newSub)
            val createdSub = newSub.copy(id = subId)
            allCategories.add(createdSub)
            return createdSub
        }

        suspend fun getOrCreateTag(name: String): TagEntity {
            val trimmedName = name.trim().removePrefix("#").trim()
            if (trimmedName.isEmpty()) return TagEntity(id = 0L, name = "General")
            val key = trimmedName.lowercase()
            tagNameToEntity[key]?.let { return it }
            val existing = catTagDao.getTagByNameOnce(trimmedName)
            if (existing != null) {
                tagNameToEntity[key] = existing
                return existing
            }
            val newTag = TagEntity(id = 0L, name = trimmedName)
            val newId = catTagDao.insertTag(newTag)
            val created = newTag.copy(id = newId)
            tagNameToEntity[key] = created
            return created
        }

        var currentSection = CsvSectionState.LEGACY
        var colMap: Map<String, Int> = emptyMap()
        var sideBySideColMap: SideBySideColMap? = null

        val detectedFirst = detectSectionHeader(firstLine)
        if (detectedFirst != null) {
            currentSection = detectedFirst
        } else {
            val headerTokens = parseCsvLine(firstLine)
            if (isSideBySideHeader(headerTokens)) {
                currentSection = CsvSectionState.SIDE_BY_SIDE
                sideBySideColMap = parseSideBySideColMap(headerTokens)
            } else {
                colMap = headerTokens.mapIndexed { idx, name ->
                    name.trim().lowercase().replace("_", "").replace(" ", "") to idx
                }.toMap()
            }
        }

        var totalRows = 0
        var importedCount = 0
        var updatedCount = 0
        var skippedCount = 0
        var accountsConfiguredCount = 0
        val newAccountsCreated = mutableListOf<String>()
        val newCategoriesCreated = mutableListOf<String>()
        val errorMessages = mutableListOf<String>()

        var line = reader.readLine()
        var lineNum = 1
        while (line != null) {
            lineNum++
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.all { it == ',' || it.isWhitespace() }) {
                val sec = detectSectionHeader(trimmed)
                if (sec != null) {
                    currentSection = sec
                    colMap = emptyMap()
                    sideBySideColMap = null
                    line = reader.readLine()
                    continue
                }

                if (trimmed.startsWith("#") || trimmed.startsWith("//")) {
                    line = reader.readLine()
                    continue
                }

                if (colMap.isEmpty() && sideBySideColMap == null) {
                    val tokens = parseCsvLine(trimmed)
                    if (currentSection == CsvSectionState.SIDE_BY_SIDE || isSideBySideHeader(tokens)) {
                        currentSection = CsvSectionState.SIDE_BY_SIDE
                        sideBySideColMap = parseSideBySideColMap(tokens)
                    } else {
                        colMap = tokens.mapIndexed { idx, name ->
                            name.trim().lowercase().replace("_", "").replace(" ", "") to idx
                        }.toMap()
                    }
                    line = reader.readLine()
                    continue
                }

                totalRows++
                val tokens = parseCsvLine(trimmed)

                when (currentSection) {
                    CsvSectionState.SIDE_BY_SIDE -> {
                        val sbs = sideBySideColMap ?: parseSideBySideColMap(tokens).also { sideBySideColMap = it }
                        val accName = sbs.accNameIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                        if (accName.isNotEmpty()) {
                            val rawGroup = sbs.accGroupIdx?.let { tokens.getOrNull(it)?.trim() }
                            val group = parseAccountGroupFromTokens(rawGroup, accName)

                            val currency = sbs.accCurrencyIdx?.let { tokens.getOrNull(it)?.trim() }?.ifBlank { defaultCurrency }
                                ?: defaultCurrency
                            val spec = SupportedCurrencies.fromCode(currency)

                            val rawBal = sbs.accStartingBalIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                            val balMinor = parseAmountDecimalToMinor(rawBal, spec.fractionDigits) ?: 0L

                            val rawDate = sbs.accAnchorDateIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                            val anchorEpochDay = parseDateStringToEpochDay(rawDate) ?: LocalDate.now().toEpochDay()

                            val rawLimit = sbs.accCreditLimitIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                            val limitMinor = parseAmountDecimalToMinor(rawLimit, spec.fractionDigits)

                            val billDay = sbs.accSettlementDayIdx?.let { tokens.getOrNull(it)?.trim() }

                            val isNew = !accountNameToEntity.containsKey(accName.lowercase())
                            val acc = getOrCreateAccount(
                                name = accName,
                                currency = currency,
                                groupOverride = group,
                                limitMinor = limitMinor,
                                billDay = billDay?.toIntOrNull(),
                                anchorDateEpochDay = anchorEpochDay
                            )
                            if (isNew) {
                                newAccountsCreated.add(accName)
                            }

                            if (balMinor != 0L) {
                                if (acc.groupType == AccountGroup.CREDIT_CARD) {
                                    val updatedAcc = acc.copy(openingBalanceMinor = Math.abs(balMinor))
                                    accountDao.updateAccount(updatedAcc)
                                    accountNameToEntity[accName.lowercase()] = updatedAcc
                                    accountsConfiguredCount++
                                } else {
                                    val isOutflow = balMinor < 0L || acc.groupType.isLiabilityByDefault
                                    val anchorKey = "${acc.id}|$anchorEpochDay"
                                    val alreadyHasAnchor = anchorKey in existingAnchorKeys
                                    if (!alreadyHasAnchor) {
                                        existingAnchorKeys.add(anchorKey)
                                        val anchorTx = TransactionEntity(
                                            id = 0L,
                                            type = TransactionType.OPENING_BALANCE,
                                            timestampEpochMillis = anchorEpochDay * 86400000L + 12 * 3600 * 1000L,
                                            ledgerDateEpochDay = anchorEpochDay,
                                            amountMinor = Math.abs(balMinor),
                                            currencyCode = currency,
                                            accountId = acc.id,
                                            transferToAccountId = if (isOutflow) acc.id else null,
                                            payeeOrDescription = "Opening Balance",
                                            note = "Initial starting balance"
                                        )
                                        txDao.insertNewTransactionRaw(anchorTx)
                                        accountsConfiguredCount++
                                    }
                                }
                            }
                        }

                        val mainName = sbs.catMainIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                        val subName = sbs.catSubIdx?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { null }
                        if (mainName.isNotEmpty()) {
                            val rawType = sbs.catTypeIdx?.let { tokens.getOrNull(it)?.trim() } ?: "EXPENSE"
                            val catType = if (rawType.equals("INCOME", ignoreCase = true)) CategoryType.INCOME else CategoryType.EXPENSE
                            val beforeMain = categoryNameToEntity.containsKey("${mainName.lowercase()}|${catType.name}")
                            val createdCat = getOrCreateCategory(mainName, subName, catType, null, null)
                            if (createdCat != null) {
                                if (!beforeMain) {
                                    newCategoriesCreated.add(mainName)
                                }
                                if (subName != null) {
                                    newCategoriesCreated.add("$mainName › $subName")
                                }
                            }
                        }

                        val rawDate = sbs.txDateIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                        val rawAmount = sbs.txAmountIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                        val accountName = sbs.txAccountIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""

                        if (rawDate.isNotEmpty() || rawAmount.isNotEmpty() || accountName.isNotEmpty()) {
                            val epochDay = parseDateStringToEpochDay(rawDate)
                            val currency = sbs.txCurrencyIdx?.let { tokens.getOrNull(it)?.trim() }?.ifBlank { defaultCurrency } ?: defaultCurrency
                            val spec = SupportedCurrencies.fromCode(currency)
                            val amountMinor = parseAmountDecimalToMinor(rawAmount, spec.fractionDigits)

                            if (epochDay == null || amountMinor == null || accountName.isEmpty()) {
                                val reasons = mutableListOf<String>()
                                if (epochDay == null) reasons.add("Invalid date '$rawDate'")
                                if (amountMinor == null) reasons.add("Invalid amount '$rawAmount'")
                                if (accountName.isEmpty()) reasons.add("Missing account name")
                                if (errorMessages.size < 10) {
                                    errorMessages.add("Row $lineNum: ${reasons.joinToString(", ")}.")
                                }
                                skippedCount++
                            } else {
                                val isNewAcc = !accountNameToEntity.containsKey(accountName.lowercase())
                                val sourceAccount = getOrCreateAccount(accountName, currency)
                                if (isNewAcc) {
                                    newAccountsCreated.add(accountName)
                                }

                                val transferAccountIdx = sbs.txTransferAccountIdx
                                val transferAccountName = transferAccountIdx?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { null }
                                val rawType = sbs.txTypeIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                                val type = when {
                                    rawType.equals("TRANSFER", ignoreCase = true) -> TransactionType.TRANSFER
                                    rawType.equals("CC_SETTLEMENT", ignoreCase = true) || rawType.contains("SETTLEMENT", ignoreCase = true) -> TransactionType.CC_SETTLEMENT
                                    rawType.equals("EXPENSE", ignoreCase = true) -> TransactionType.EXPENSE
                                    rawType.equals("INCOME", ignoreCase = true) -> TransactionType.INCOME
                                    rawType.equals("REFUND", ignoreCase = true) -> TransactionType.REFUND
                                    rawType.equals("OPENING_BALANCE", ignoreCase = true) -> TransactionType.OPENING_BALANCE
                                    rawType.equals("RECONCILIATION", ignoreCase = true) -> TransactionType.RECONCILIATION
                                    transferAccountName != null -> TransactionType.TRANSFER
                                    amountMinor < 0L -> TransactionType.EXPENSE
                                    else -> TransactionType.INCOME
                                }

                                val transferAccount = if (transferAccountName != null && (type == TransactionType.TRANSFER || type == TransactionType.CC_SETTLEMENT)) {
                                    val isNewTransferAcc = !accountNameToEntity.containsKey(transferAccountName.lowercase())
                                    val createdTransferAcc = getOrCreateAccount(transferAccountName, currency)
                                    if (isNewTransferAcc) {
                                        newAccountsCreated.add(transferAccountName)
                                    }
                                    createdTransferAcc
                                } else {
                                    null
                                }

                                val isTransfer = type == TransactionType.TRANSFER || type == TransactionType.CC_SETTLEMENT
                                val categoryEntity = if (isTransfer) {
                                    null
                                } else {
                                    val categoryType = if (type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
                                    val rawCat = sbs.txCategoryIdx?.let { tokens.getOrNull(it)?.trim() }
                                    val rawSubcat = sbs.txSubcategoryIdx?.let { tokens.getOrNull(it)?.trim() }
                                    val created = getOrCreateCategory(rawCat, rawSubcat, categoryType)
                                    if (created != null && rawCat != null && !allCategories.any { it.name.equals(rawCat, ignoreCase = true) }) {
                                        newCategoriesCreated.add(rawCat)
                                    }
                                    created
                                }

                                val payee = sbs.txPayeeIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                                val note = sbs.txNoteIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                                val tagsStr = sbs.txTagsIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                                val tagList = tagsStr.split(";", ",").map { it.trim().removePrefix("#").trim() }.filter { it.isNotEmpty() }

                                val absAmount = Math.abs(amountMinor)
                                val fingerprint = "${epochDay}|${sourceAccount.id}|${type.name}|${absAmount}|${payee.lowercase()}"

                                if (skipDuplicates && existingFingerprints.contains(fingerprint)) {
                                    skippedCount++
                                } else {
                                    val newTx = TransactionEntity(
                                        id = 0L,
                                        type = type,
                                        timestampEpochMillis = epochDay * 86_400_000L + 12 * 3600 * 1000L,
                                        ledgerDateEpochDay = epochDay,
                                        amountMinor = absAmount,
                                        currencyCode = currency,
                                        accountId = sourceAccount.id,
                                        transferToAccountId = transferAccount?.id,
                                        transferFeeMinor = 0L,
                                        categoryId = categoryEntity?.id,
                                        isSplit = false,
                                        payeeOrDescription = payee,
                                        note = note
                                    )
                                    val insertedId = txDao.insertNewTransactionRaw(newTx)
                                    if (tagList.isNotEmpty()) {
                                        val refs = tagList.map { tagName ->
                                            val tag = getOrCreateTag(tagName)
                                            TransactionTagCrossRef(transactionId = insertedId, tagId = tag.id)
                                        }
                                        txDao.insertTagCrossRefs(refs)
                                    }
                                    existingFingerprints.add(fingerprint)
                                    importedCount++
                                }
                            }
                        }
                    }
                    CsvSectionState.ACCOUNTS -> {
                        val nameIdx = colMap["name"] ?: colMap["account"] ?: colMap["accountname"] ?: 0
                        val name = tokens.getOrNull(nameIdx)?.trim() ?: ""
                        if (name.isNotEmpty()) {
                            val rawGroup = colMap["group"]?.let { tokens.getOrNull(it)?.trim() }
                                ?: colMap["grouptype"]?.let { tokens.getOrNull(it)?.trim() }
                            val group = parseAccountGroupFromTokens(rawGroup, name)

                            val currency = colMap["currency"]?.let { tokens.getOrNull(it)?.trim() }?.ifBlank { defaultCurrency }
                                ?: defaultCurrency
                            val spec = SupportedCurrencies.fromCode(currency)

                            val rawBal = colMap["startingbalance"]?.let { tokens.getOrNull(it)?.trim() }
                                ?: colMap["openingbalance"]?.let { tokens.getOrNull(it)?.trim() }
                                ?: colMap["balance"]?.let { tokens.getOrNull(it)?.trim() }
                                ?: ""
                            val balMinor = parseAmountDecimalToMinor(rawBal, spec.fractionDigits) ?: 0L

                            val rawDate = colMap["anchordate"]?.let { tokens.getOrNull(it)?.trim() }
                                ?: colMap["openingdate"]?.let { tokens.getOrNull(it)?.trim() }
                                ?: colMap["date"]?.let { tokens.getOrNull(it)?.trim() }
                                ?: ""
                            val anchorEpochDay = parseDateStringToEpochDay(rawDate) ?: LocalDate.now().toEpochDay()

                            val rawLimit = colMap["creditlimit"]?.let { tokens.getOrNull(it)?.trim() }
                                ?: colMap["limit"]?.let { tokens.getOrNull(it)?.trim() }
                                ?: ""
                            val limitMinor = parseAmountDecimalToMinor(rawLimit, spec.fractionDigits)

                            val billDay = colMap["settlementday"]?.let { tokens.getOrNull(it)?.trim() }
                                ?: colMap["billday"]?.let { tokens.getOrNull(it)?.trim() }
                                ?: colMap["statementday"]?.let { tokens.getOrNull(it)?.trim() }

                            val isNew = !accountNameToEntity.containsKey(name.lowercase())
                            val acc = getOrCreateAccount(
                                name = name,
                                currency = currency,
                                groupOverride = group,
                                limitMinor = limitMinor,
                                billDay = billDay?.toIntOrNull(),
                                anchorDateEpochDay = anchorEpochDay
                            )
                            if (isNew) {
                                newAccountsCreated.add(name)
                            }

                            if (balMinor != 0L) {
                                if (acc.groupType == AccountGroup.CREDIT_CARD) {
                                    val updatedAcc = acc.copy(openingBalanceMinor = Math.abs(balMinor))
                                    accountDao.updateAccount(updatedAcc)
                                    accountNameToEntity[name.lowercase()] = updatedAcc
                                    accountsConfiguredCount++
                                } else {
                                    val isOutflow = balMinor < 0L || acc.groupType.isLiabilityByDefault
                                    val anchorKey = "${acc.id}|$anchorEpochDay"
                                    val alreadyHasAnchor = anchorKey in existingAnchorKeys
                                    if (!alreadyHasAnchor) {
                                        existingAnchorKeys.add(anchorKey)
                                        val anchorTx = TransactionEntity(
                                            id = 0L,
                                            type = TransactionType.OPENING_BALANCE,
                                            timestampEpochMillis = anchorEpochDay * 86400000L + 12 * 3600 * 1000L,
                                            ledgerDateEpochDay = anchorEpochDay,
                                            amountMinor = Math.abs(balMinor),
                                            currencyCode = currency,
                                            accountId = acc.id,
                                            transferToAccountId = if (isOutflow) acc.id else null,
                                            payeeOrDescription = "Opening Balance",
                                            note = "Initial starting balance"
                                        )
                                        txDao.insertNewTransactionRaw(anchorTx)
                                        accountsConfiguredCount++
                                    }
                                }
                            }
                        }
                    }
                    CsvSectionState.CATEGORIES -> {
                        val mainIdx = colMap["maincategory"] ?: colMap["category"] ?: colMap["name"] ?: 0
                        val subIdx = colMap["subcategory"] ?: colMap["subname"] ?: colMap["sub"]
                        val mainName = tokens.getOrNull(mainIdx)?.trim() ?: ""
                        val subName = subIdx?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { null }
                        val rawType = colMap["type"]?.let { tokens.getOrNull(it)?.trim() } ?: "EXPENSE"
                        val catType = if (rawType.equals("INCOME", ignoreCase = true)) CategoryType.INCOME else CategoryType.EXPENSE
                        val iconOverride = colMap["icon"]?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { null }
                        val colorOverride = colMap["color"]?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { null }

                        if (mainName.isNotEmpty()) {
                            val beforeMain = categoryNameToEntity.containsKey("${mainName.lowercase()}|${catType.name}")
                            val createdCat = getOrCreateCategory(mainName, subName, catType, iconOverride, colorOverride)
                            if (createdCat != null) {
                                if (!beforeMain) {
                                    newCategoriesCreated.add(mainName)
                                }
                                if (subName != null) {
                                    newCategoriesCreated.add("$mainName â€º $subName")
                                }
                            }
                        }
                    }
                    CsvSectionState.TRANSACTIONS,
                    CsvSectionState.LEGACY -> {
                        val dateIdx = colMap["date"] ?: colMap["txdate"] ?: colMap["transactiondate"] ?: colMap["txndate"] ?: colMap["postingdate"] ?: colMap["valuedate"]
                        val amountIdx = colMap["amount"] ?: colMap["amountformatted"] ?: colMap["amountminor"] ?: colMap["txnamount"] ?: colMap["transactionamount"] ?: colMap["total"] ?: colMap["netamount"] ?: colMap["value"]
                        val accountIdx = colMap["account"] ?: colMap["accountname"] ?: colMap["fromaccount"] ?: colMap["sourceaccount"] ?: colMap["bank"] ?: colMap["wallet"]

                        if (dateIdx == null || amountIdx == null || accountIdx == null) {
                            val missingCols = mutableListOf<String>()
                            if (dateIdx == null) missingCols.add("Date")
                            if (amountIdx == null) missingCols.add("Amount")
                            if (accountIdx == null) missingCols.add("Account")
                            if (errorMessages.size < 10) {
                                errorMessages.add("Row $lineNum: Missing column(s) in header: ${missingCols.joinToString(", ")}.")
                            }
                            skippedCount++
                            line = reader.readLine()
                            continue
                        }

                        val rawDate = tokens.getOrNull(dateIdx) ?: ""
                        val epochDay = parseDateStringToEpochDay(rawDate)
                        val currencyIdx = colMap["currency"]
                        val currency = currencyIdx?.let { tokens.getOrNull(it)?.trim() }?.ifBlank { defaultCurrency } ?: defaultCurrency
                        val spec = SupportedCurrencies.fromCode(currency)
                        val rawAmount = tokens.getOrNull(amountIdx) ?: ""
                        val amountMinor = parseAmountDecimalToMinor(rawAmount, spec.fractionDigits)
                        val accountName = tokens.getOrNull(accountIdx)?.trim() ?: ""

                        if (epochDay == null || amountMinor == null || accountName.isEmpty()) {
                            val reasons = mutableListOf<String>()
                            if (epochDay == null) reasons.add("Invalid date '$rawDate'")
                            if (amountMinor == null) reasons.add("Invalid amount '$rawAmount'")
                            if (accountName.isEmpty()) reasons.add("Missing account name")
                            if (errorMessages.size < 10) {
                                errorMessages.add("Row $lineNum: ${reasons.joinToString(", ")}.")
                            }
                            skippedCount++
                            line = reader.readLine()
                            continue
                        }

                        val isNewAcc = !accountNameToEntity.containsKey(accountName.lowercase())
                        val sourceAccount = getOrCreateAccount(accountName, currency)
                        if (isNewAcc) {
                            newAccountsCreated.add(accountName)
                        }

                        val transferAccountIdx = colMap["transfertoaccount"] ?: colMap["transferto"]
                        val transferAccountName = transferAccountIdx?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { null }
                        val rawType = colMap["type"]?.let { tokens.getOrNull(it)?.trim() } ?: ""
                        val type = when {
                            rawType.equals("TRANSFER", ignoreCase = true) -> TransactionType.TRANSFER
                            rawType.equals("CC_SETTLEMENT", ignoreCase = true) || rawType.contains("SETTLEMENT", ignoreCase = true) -> TransactionType.CC_SETTLEMENT
                            rawType.equals("EXPENSE", ignoreCase = true) -> TransactionType.EXPENSE
                            rawType.equals("INCOME", ignoreCase = true) -> TransactionType.INCOME
                            rawType.equals("REFUND", ignoreCase = true) -> TransactionType.REFUND
                            rawType.equals("OPENING_BALANCE", ignoreCase = true) -> TransactionType.OPENING_BALANCE
                            rawType.equals("RECONCILIATION", ignoreCase = true) -> TransactionType.RECONCILIATION
                            transferAccountName != null -> TransactionType.TRANSFER
                            amountMinor < 0L -> TransactionType.EXPENSE
                            else -> TransactionType.INCOME
                        }

                        val transferAccount = if (transferAccountName != null && (type == TransactionType.TRANSFER || type == TransactionType.CC_SETTLEMENT)) {
                            val isNewTransferAcc = !accountNameToEntity.containsKey(transferAccountName.lowercase())
                            val createdTransferAcc = getOrCreateAccount(transferAccountName, currency)
                            if (isNewTransferAcc) {
                                newAccountsCreated.add(transferAccountName)
                            }
                            createdTransferAcc
                        } else {
                            null
                        }

                        val transferFeeIdx = colMap["transferfee"] ?: colMap["transferfeeminor"]
                        val rawFee = transferFeeIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                        val feeMinor = parseAmountDecimalToMinor(rawFee, spec.fractionDigits) ?: 0L

                        val isTransfer = type == TransactionType.TRANSFER || type == TransactionType.CC_SETTLEMENT
                        val categoryEntity = if (isTransfer) {
                            null // Strictly suppress category creation for transfers!
                        } else {
                            val categoryType = if (type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
                            val rawCat = colMap["category"]?.let { tokens.getOrNull(it)?.trim() }
                            val rawSubcat = colMap["subcategory"]?.let { tokens.getOrNull(it)?.trim() }
                            val created = getOrCreateCategory(rawCat, rawSubcat, categoryType)
                            if (created != null && rawCat != null && !allCategories.any { it.name.equals(rawCat, ignoreCase = true) }) {
                                newCategoriesCreated.add(rawCat)
                            }
                            created
                        }

                        val payeeIdx = colMap["payeeordescription"] ?: colMap["payee"] ?: colMap["description"]
                        val payee = payeeIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                        val noteIdx = colMap["note"]
                        val note = noteIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                        val tagsIdx = colMap["tags"]
                        val tagsStr = tagsIdx?.let { tokens.getOrNull(it)?.trim() } ?: ""
                        val tagList = tagsStr.split(";", ",").map { it.trim().removePrefix("#").trim() }.filter { it.isNotEmpty() }

                        val absAmount = Math.abs(amountMinor)
                        val fingerprint = "${epochDay}|${sourceAccount.id}|${type.name}|${absAmount}|${payee.lowercase()}"

                        val idIdx = colMap["id"]
                        val rawId = idIdx?.let { tokens.getOrNull(it)?.trim() }?.toLongOrNull()

                        if (rawId != null && rawId > 0L && existingTxById.containsKey(rawId)) {
                            val existingTx = existingTxById[rawId]!!
                            val updatedTx = existingTx.copy(
                                type = type,
                                ledgerDateEpochDay = epochDay,
                                timestampEpochMillis = epochDay * 86_400_000L + 12 * 3600 * 1000L,
                                amountMinor = absAmount,
                                currencyCode = currency,
                                accountId = sourceAccount.id,
                                transferToAccountId = transferAccount?.id,
                                transferFeeMinor = Math.abs(feeMinor),
                                categoryId = categoryEntity?.id,
                                payeeOrDescription = payee,
                                note = note
                            )
                            txDao.updateTransactionRaw(updatedTx)

                            txDao.deleteTagCrossRefsForTransaction(rawId)
                            if (tagList.isNotEmpty()) {
                                val refs = tagList.map { tagName ->
                                    val tag = getOrCreateTag(tagName)
                                    TransactionTagCrossRef(transactionId = rawId, tagId = tag.id)
                                }
                                txDao.insertTagCrossRefs(refs)
                            }
                            updatedCount++
                        } else if (skipDuplicates && existingFingerprints.contains(fingerprint)) {
                            skippedCount++
                        } else {
                            val newTx = TransactionEntity(
                                id = 0L,
                                type = type,
                                timestampEpochMillis = epochDay * 86_400_000L + 12 * 3600 * 1000L,
                                ledgerDateEpochDay = epochDay,
                                amountMinor = absAmount,
                                currencyCode = currency,
                                accountId = sourceAccount.id,
                                transferToAccountId = transferAccount?.id,
                                transferFeeMinor = Math.abs(feeMinor),
                                categoryId = categoryEntity?.id,
                                isSplit = false,
                                payeeOrDescription = payee,
                                note = note
                            )
                            val insertedId = txDao.insertNewTransactionRaw(newTx)
                            if (tagList.isNotEmpty()) {
                                val refs = tagList.map { tagName ->
                                    val tag = getOrCreateTag(tagName)
                                    TransactionTagCrossRef(transactionId = insertedId, tagId = tag.id)
                                }
                                txDao.insertTagCrossRefs(refs)
                            }
                            existingFingerprints.add(fingerprint)
                            importedCount++
                        }
                    }
                }
            }
            line = reader.readLine()
        }

        ImportCsvResult(
            totalRowsRead = totalRows,
            importedCount = importedCount,
            updatedCount = updatedCount,
            skippedCount = skippedCount,
            newAccountsCreated = newAccountsCreated.distinct(),
            newCategoriesCreated = newCategoriesCreated.distinct(),
            errorMessages = errorMessages,
            accountsConfiguredCount = accountsConfiguredCount
        )
    }

    private fun formatDecimalAmountPlain(minor: Long, fractionDigits: Int): String {
        if (fractionDigits <= 0) return minor.toString()
        val isNeg = minor < 0L
        val absMinor = Math.abs(minor)
        val factor = 10.0.pow(fractionDigits.toDouble()).toLong()
        val whole = absMinor / factor
        val frac = absMinor % factor
        val fracStr = frac.toString().padStart(fractionDigits, '0')
        val res = "$whole.$fracStr"
        return if (isNeg) "-$res" else res
    }



    private data class FiscalRangeInfo(
        val startEpochDay: Long,
        val endEpochDay: Long,
        val title: String,
        val subtitle: String
    )

}

internal fun parseCsvLine(line: String): List<String> {
    val tokens = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val c = line[i]
        when {
            c == '"' -> {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            }
            c == ',' && !inQuotes -> {
                tokens.add(sb.toString())
                sb.setLength(0)
            }
            else -> {
                sb.append(c)
            }
        }
        i++
    }
    tokens.add(sb.toString())
    return tokens
}

internal enum class CsvSectionState {
    LEGACY,
    ACCOUNTS,
    CATEGORIES,
    TRANSACTIONS,
    SIDE_BY_SIDE
}

internal fun detectSectionHeader(line: String): CsvSectionState? {
    val trimmed = line.trim().uppercase()
    if (trimmed.isEmpty()) return null

    if (trimmed.startsWith("#") || trimmed.startsWith("//")) {
        // Any comment or hint with colons or commas is a comment/hint, not a Markdown section header
        if (trimmed.contains(":") || trimmed.contains(",")) {
            return null
        }
    }

    // Side-by-side banner detection: contains multiple section markers across columns
    val hasAccMarker = trimmed.contains("[ACCOUNT") || trimmed.contains("=== ACCOUNT") || trimmed.contains("## ACCOUNT")
    val hasCatMarker = trimmed.contains("[CATEGOR") || trimmed.contains("=== CATEGOR") || trimmed.contains("## CATEGOR")
    val hasTxMarker = trimmed.contains("[TRANSACTION") || trimmed.contains("=== TRANSACTION") || trimmed.contains("## TRANSACTION")
    val markerCount = (if (hasAccMarker) 1 else 0) + (if (hasCatMarker) 1 else 0) + (if (hasTxMarker) 1 else 0)
    if (markerCount >= 2) {
        return CsvSectionState.SIDE_BY_SIDE
    }

    // Banner without brackets/equals, e.g. "ACCOUNTS,,,,,,,CATEGORIES,,,,TRANSACTIONS,,,,,,,,,"
    if (!trimmed.contains("DATE") && !trimmed.contains("AMOUNT") && !trimmed.contains("BALANCE") && !trimmed.contains("CURRENCY")) {
        val hasAccountsWord = trimmed.contains("ACCOUNT")
        val hasCategoriesWord = trimmed.contains("CATEGOR")
        val hasTransactionsWord = trimmed.contains("TRANSACTION")
        val wordCount = (if (hasAccountsWord) 1 else 0) + (if (hasCategoriesWord) 1 else 0) + (if (hasTransactionsWord) 1 else 0)
        if (wordCount >= 2 && trimmed.contains(",")) {
            return CsvSectionState.SIDE_BY_SIDE
        }
    }

    // Single section header detection
    val tokens = parseCsvLine(trimmed)
    val firstToken = tokens.firstOrNull()?.trim() ?: ""
    val otherTokensNonEmpty = tokens.drop(1).any { it.isNotBlank() }

    // If there are other non-empty columns (like in a header row or data row), it cannot be a single section header
    if (otherTokensNonEmpty) {
        return null
    }

    val clean = firstToken
        .replace("=", "")
        .replace("[", "")
        .replace("]", "")
        .replace("#", "")
        .trim()

    return when {
        clean.startsWith("ACCOUNT") -> CsvSectionState.ACCOUNTS
        clean.startsWith("CATEGOR") -> CsvSectionState.CATEGORIES
        clean.startsWith("TRANSACTION") -> CsvSectionState.TRANSACTIONS
        else -> null
    }
}

internal data class SideBySideColMap(
    val accNameIdx: Int? = null,
    val accGroupIdx: Int? = null,
    val accCurrencyIdx: Int? = null,
    val accStartingBalIdx: Int? = null,
    val accAnchorDateIdx: Int? = null,
    val accCreditLimitIdx: Int? = null,
    val accSettlementDayIdx: Int? = null,

    val catMainIdx: Int? = null,
    val catSubIdx: Int? = null,
    val catTypeIdx: Int? = null,

    val txDateIdx: Int? = null,
    val txTypeIdx: Int? = null,
    val txAmountIdx: Int? = null,
    val txCurrencyIdx: Int? = null,
    val txAccountIdx: Int? = null,
    val txTransferAccountIdx: Int? = null,
    val txCategoryIdx: Int? = null,
    val txSubcategoryIdx: Int? = null,
    val txPayeeIdx: Int? = null,
    val txNoteIdx: Int? = null,
    val txTagsIdx: Int? = null
)

internal fun isSideBySideHeader(tokens: List<String>): Boolean {
    val normalized = tokens.map { it.trim().lowercase().replace("_", "").replace(" ", "") }
    val hasDate = normalized.any { it == "date" || it == "txdate" || it == "transactiondate" || it == "postingdate" }
    val hasAmount = normalized.any { it == "amount" || it == "txnamount" || it == "amountformatted" }

    // Side-by-side spreadsheets contain account balance/limit definitions or category definitions alongside transactions
    val hasAccountDefs = normalized.any {
        it == "startingbalance" || it == "openingbalance" || it == "creditlimit" || it == "settlementday"
    }
    val hasCategoryType = normalized.any { it == "categorytype" }
    val hasDuplicateCategory = normalized.count { it == "category" || it == "maincategory" || it == "categoryname" } >= 2
    val hasDuplicateAccount = normalized.count { it == "account" || it == "accountname" || it == "sourceaccount" } >= 2

    return hasDate && hasAmount && (hasAccountDefs || hasCategoryType || (hasDuplicateCategory && hasDuplicateAccount))
}

internal fun parseSideBySideColMap(tokens: List<String>): SideBySideColMap {
    val normalizedTokens = tokens.map { it.trim().lowercase().replace("_", "").replace(" ", "") }
    val dateIdx = normalizedTokens.indexOfFirst { token ->
        token == "date" || token == "txdate" || token == "transactiondate" || token == "postingdate"
    }.takeIf { it >= 0 }

    var accName: Int? = null
    var accGroup: Int? = null
    var accCurrency: Int? = null
    var accStartingBal: Int? = null
    var accAnchorDate: Int? = null
    var accCreditLimit: Int? = null
    var accSettlementDay: Int? = null

    var catMain: Int? = null
    var catSub: Int? = null
    var catType: Int? = null

    var txDate: Int? = dateIdx
    var txType: Int? = null
    var txAmount: Int? = null
    var txCurrency: Int? = null
    var txAccount: Int? = null
    var txTransferAccount: Int? = null
    var txCategory: Int? = null
    var txSubcategory: Int? = null
    var txPayee: Int? = null
    var txNote: Int? = null
    var txTags: Int? = null

    normalizedTokens.forEachIndexed { idx, col ->
        if (col.isBlank()) return@forEachIndexed

        val isBeforeTx = dateIdx == null || idx < dateIdx
        if (isBeforeTx) {
            when {
                col == "accountname" || col == "account" || (col == "name" && accName == null) -> accName = idx
                col == "accountgroup" || col == "group" || col == "grouptype" -> accGroup = idx
                col == "accountcurrency" || (col == "currency" && accCurrency == null) -> accCurrency = idx
                col == "startingbalance" || col == "openingbalance" || col == "balance" -> accStartingBal = idx
                col == "anchordate" || col == "openingdate" -> accAnchorDate = idx
                col == "creditlimit" || col == "limit" -> accCreditLimit = idx
                col == "settlementday" || col == "billday" || col == "statementday" -> accSettlementDay = idx

                col == "categorytype" || (col == "type" && catType == null) -> catType = idx
                col == "subcategory" || col == "subname" || col == "sub" -> catSub = idx
                col == "category" || col == "categoryname" || col == "maincategory" || (col == "name" && accName != null) -> catMain = idx
            }
        } else {
            when {
                idx == dateIdx -> txDate = idx
                col == "amount" || col == "txnamount" || col == "amountformatted" || col == "total" -> txAmount = idx
                col == "transfertoaccount" || col == "transferto" || col == "toaccount" || col == "destinationaccount" -> txTransferAccount = idx
                col == "account" || col == "accountname" || col == "sourceaccount" || col == "fromaccount" || col == "bank" || col == "wallet" -> txAccount = idx
                col == "type" || col == "txtype" || col == "transactiontype" -> txType = idx
                col == "currency" || col == "txcurrency" -> txCurrency = idx
                col == "subcategory" || col == "subname" || col == "subcat" -> txSubcategory = idx
                col == "category" || col == "categoryname" || col == "maincategory" -> txCategory = idx
                col == "payee" || col == "payeeordescription" || col == "description" || col == "merchant" -> txPayee = idx
                col == "note" || col == "notes" || col == "memo" -> txNote = idx
                col == "tags" || col == "tag" || col == "label" || col == "labels" -> txTags = idx
            }
        }
    }

    if (accName == null && (accStartingBal != null || accCreditLimit != null)) {
        accName = 0
    }

    return SideBySideColMap(
        accNameIdx = accName,
        accGroupIdx = accGroup,
        accCurrencyIdx = accCurrency,
        accStartingBalIdx = accStartingBal,
        accAnchorDateIdx = accAnchorDate,
        accCreditLimitIdx = accCreditLimit,
        accSettlementDayIdx = accSettlementDay,
        catMainIdx = catMain,
        catSubIdx = catSub,
        catTypeIdx = catType,
        txDateIdx = txDate,
        txTypeIdx = txType,
        txAmountIdx = txAmount,
        txCurrencyIdx = txCurrency,
        txAccountIdx = txAccount,
        txTransferAccountIdx = txTransferAccount,
        txCategoryIdx = txCategory,
        txSubcategoryIdx = txSubcategory,
        txPayeeIdx = txPayee,
        txNoteIdx = txNote,
        txTagsIdx = txTags
    )
}

internal fun generateSideBySideCsvTemplateContent(): String {
    val sb = StringBuilder()
    sb.append("[ACCOUNTS],,,,,,,,[CATEGORIES],,,,[TRANSACTIONS],,,,,,,,,,\n")
    sb.append("Account Name,Group,Account Currency,Starting Balance,Anchor Date,Credit Limit,Settlement Day,,Category,Subcategory,Category Type,,Date,Type,Amount,Currency,Account,Transfer To Account,Category,Subcategory,Payee,Note,Tags\n")
    sb.append("# e.g. Checking Account,# BANK_ACCOUNT | CREDIT_CARD | CASH | SAVINGS | LOAN,# ISO Code (USD/EUR/INR),# e.g. 5000.00,# YYYY-MM-DD,# e.g. 10000.00,# 1-31,,# Main Category Name,# Optional Subcategory,# EXPENSE | INCOME,,# YYYY-MM-DD,# EXPENSE | INCOME | TRANSFER,# e.g. 45.50,# ISO Code,# Source Account,# Destination if Transfer,# Category,# Subcategory,# Merchant / Payee,# Optional Note,# Semicolon-separated tags (e.g. groceries; food)\n")
    return sb.toString()
}

internal fun parseAccountGroupFromTokens(rawGroup: String?, accountName: String): AccountGroup {
    val clean = rawGroup?.trim()?.uppercase()?.replace(" ", "_") ?: ""
    if (clean.isNotEmpty()) {
        if (clean == "BANK") return AccountGroup.BANK_ACCOUNT
        if (clean == "WALLET") return AccountGroup.PREPAID_WALLET
        if (clean == "CC" || clean == "CREDIT" || clean == "CARD") return AccountGroup.CREDIT_CARD
        if (clean == "DEBIT") return AccountGroup.DEBIT_CARD
        if (clean == "OD" || clean == "OVERDRAFT") return AccountGroup.OVERDRAFT
        runCatching { return AccountGroup.valueOf(clean) }
    }
    val lower = accountName.lowercase()
    val ccRegex = Regex("""\b(cc|credit|credit\s*card)\b""")
    val odRegex = Regex("""\b(od|overdraft)\b""")
    return when {
        ccRegex.containsMatchIn(lower) -> AccountGroup.CREDIT_CARD
        lower.contains("debit") -> AccountGroup.DEBIT_CARD
        lower.contains("cash") || lower.contains("wallet") -> AccountGroup.CASH
        lower.contains("savings") -> AccountGroup.SAVINGS
        lower.contains("bank") || lower.contains("a/c") || lower.contains("checking") -> AccountGroup.BANK_ACCOUNT
        lower.contains("loan") || lower.contains("emi") || lower.contains("mortgage") || lower.contains("borrow") -> AccountGroup.LOAN
        odRegex.containsMatchIn(lower) -> AccountGroup.OVERDRAFT
        lower.contains("invest") || lower.contains("mutual") || lower.contains("stock") -> AccountGroup.INVESTMENT
        else -> AccountGroup.OTHER
    }
}

internal fun parseAmountDecimalToMinor(raw: String, fractionDigits: Int): Long? {
    val cleaned = raw.replace(",", "").replace("$", "").replace("₹", "").replace("€", "").replace("£", "").trim()
    if (cleaned.isEmpty()) return null
    return try {
        val bd = BigDecimal(cleaned)
        bd.movePointRight(fractionDigits).setScale(0, RoundingMode.HALF_UP).toLong()
    } catch (e: Exception) {
        null
    }
}

internal fun parseDateStringToEpochDay(raw: String): Long? {
    val cleaned = raw.trim()
    if (cleaned.isEmpty()) return null
    runCatching { return LocalDate.parse(cleaned, DateTimeFormatter.ISO_LOCAL_DATE).toEpochDay() }
    runCatching { return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("dd/MM/yyyy")).toEpochDay() }
    runCatching { return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("dd-MM-yyyy")).toEpochDay() }
    runCatching { return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("MM/dd/yyyy")).toEpochDay() }
    runCatching { return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("yyyy/MM/dd")).toEpochDay() }
    return null
}


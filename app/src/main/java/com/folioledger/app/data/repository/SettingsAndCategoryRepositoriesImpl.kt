// File: app/src/main/java/com/folioledger/app/data/repository/SettingsAndCategoryRepositoriesImpl.kt
package com.folioledger.app.data.repository

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.withTransaction
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
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.ImportCsvPreview
import com.folioledger.app.domain.model.ImportCsvResult
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow
import java.time.format.DateTimeFormatter
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.domain.repository.CategoryAndTagRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.components.suggestIconAndColorForCategoryName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
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
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private val Context.folioUserPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "folio_user_preferences"
)

class SettingsAndBackupRepositoryImpl(
    private val context: Context,
    private val database: FolioDatabase
) : SettingsAndBackupRepository {

    private object PreferenceKeys {
        val PRIMARY_CURRENCY_CODE = stringPreferencesKey("primary_currency_code")
        val NUMBER_GROUPING = stringPreferencesKey("number_grouping")
        val MONTH_START_DAY = intPreferencesKey("month_start_day")
        val WEEK_START_DAY_ISO = intPreferencesKey("week_start_day_iso")
        val SUBCATEGORIES_ENABLED = booleanPreferencesKey("subcategories_enabled")
        val CARRY_OVER_ENABLED = booleanPreferencesKey("carry_over_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
        val ACCENT_PALETTE = stringPreferencesKey("accent_palette")
        val BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
    }

    override fun observeUserPreferences(): Flow<UserPreferences> {
        return context.folioUserPreferencesDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { prefs -> mapPreferencesToDomain(prefs) }
    }

    override suspend fun updateUserPreferences(
        transform: (UserPreferences) -> UserPreferences
    ): Result<Unit> = runCatching {
        context.folioUserPreferencesDataStore.edit { mutablePrefs ->
            val current = mapPreferencesToDomain(mutablePrefs)
            val updated = transform(current)
            val validatedMonthStart = updated.monthStartDay.coerceIn(1, 31)
            val validatedWeekStart = updated.weekStartDayIso.coerceIn(1, 7)

            mutablePrefs[PreferenceKeys.PRIMARY_CURRENCY_CODE] = updated.primaryCurrencyCode
            mutablePrefs[PreferenceKeys.NUMBER_GROUPING] = updated.numberGrouping.name
            mutablePrefs[PreferenceKeys.MONTH_START_DAY] = validatedMonthStart
            mutablePrefs[PreferenceKeys.WEEK_START_DAY_ISO] = validatedWeekStart
            mutablePrefs[PreferenceKeys.SUBCATEGORIES_ENABLED] = updated.subcategoriesEnabled
            mutablePrefs[PreferenceKeys.CARRY_OVER_ENABLED] = updated.carryOverEnabled
            mutablePrefs[PreferenceKeys.THEME_MODE] = updated.themeMode
            mutablePrefs[PreferenceKeys.DYNAMIC_COLOR_ENABLED] = updated.dynamicColorEnabled
            mutablePrefs[PreferenceKeys.ACCENT_PALETTE] = updated.accentPalette
            mutablePrefs[PreferenceKeys.BIOMETRIC_LOCK_ENABLED] = updated.biometricLockEnabled
        }
        Unit
    }

    override suspend fun exportTransactionsToCsv(
        destinationUriString: String,
        startEpochDay: Long?,
        endEpochDay: Long?
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            openOutputStreamForLocation(destinationUriString).use { rawStream ->
                exportTransactionsToCsvStream(
                    outputStream = rawStream,
                    startEpochDay = startEpochDay,
                    endEpochDay = endEpochDay
                )
            }
        }
    }

    override suspend fun previewTransactionsCsv(
        sourceUriString: String
    ): Result<ImportCsvPreview> = withContext(Dispatchers.IO) {
        runCatching {
            // Cache incoming file locally to prevent Android Scoped Storage URI permission expiration
            val cachedFile = File(context.cacheDir, "pending_import_${System.currentTimeMillis()}.csv")
            openInputStreamForLocation(sourceUriString).use { input ->
                cachedFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            cachedFile.inputStream().use { cachedStream ->
                previewCsvStream(cachedStream, cachedFile.absolutePath)
            }
        }
    }

    override suspend fun importTransactionsFromCsv(
        sourceUriString: String,
        skipDuplicates: Boolean
    ): Result<ImportCsvResult> = withContext(Dispatchers.IO) {
        runCatching {
            openInputStreamForLocation(sourceUriString).use { rawStream ->
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
        endEpochDay: Long? = null
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
            afterStart && beforeEnd
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
        for (acc in allAccounts) {
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

    override suspend fun createPortableZipBackup(
        destinationUriString: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            openOutputStreamForLocation(destinationUriString).use { rawStream ->
                createBackupToStream(rawStream)
            }
            destinationUriString
        }
    }

    suspend fun createBackupToStream(outputStream: OutputStream): Unit = withContext(Dispatchers.IO) {
        val currentPrefs = observeUserPreferences().first()
        val accounts = database.accountDao().getAllAccountsOnce()
        val categories = database.categoryAndTagDao().getAllCategoriesOnce()
        val tags = database.categoryAndTagDao().getAllTagsOnce()
        val bookmarks = database.categoryAndTagDao().getAllBookmarksOnce()
        val installmentPlans = database.transactionDao().getAllInstallmentPlansOnce()
        val recurringRules = database.budgetAndRecurringDao().getAllRecurringRulesOnce()
        val transactions = database.transactionDao().getAllTransactionsRawOnce()
        val splits = database.transactionDao().getAllSplitsOnce()
        val tagCrossRefs = database.transactionDao().getAllTagCrossRefsOnce()
        val budgets = database.budgetAndRecurringDao().getAllBudgetsOnce()

        val rootJson = JSONObject().apply {
            put("backupFormatVersion", BACKUP_SCHEMA_VERSION)
            put("createdAtEpochMillis", System.currentTimeMillis())

            put(
                "userPreferences",
                JSONObject().apply {
                    put("primaryCurrencyCode", currentPrefs.primaryCurrencyCode)
                    put("numberGrouping", currentPrefs.numberGrouping.name)
                    put("monthStartDay", currentPrefs.monthStartDay)
                    put("weekStartDayIso", currentPrefs.weekStartDayIso)
                    put("subcategoriesEnabled", currentPrefs.subcategoriesEnabled)
                    put("carryOverEnabled", currentPrefs.carryOverEnabled)
                    put("themeMode", currentPrefs.themeMode)
                    put("dynamicColorEnabled", currentPrefs.dynamicColorEnabled)
                    put("accentPalette", currentPrefs.accentPalette)
                    put("biometricLockEnabled", currentPrefs.biometricLockEnabled)
                }
            )

            put(
                "accounts",
                JSONArray().apply {
                    for (acc in accounts) {
                        put(
                            JSONObject().apply {
                                put("id", acc.id)
                                put("name", acc.name)
                                put("groupType", acc.groupType.name)
                                put("currencyCode", acc.currencyCode)
                                put("openingBalanceMinor", acc.openingBalanceMinor)
                                putNullableLong("initialUnbilledMinor", acc.initialUnbilledMinor)
                                putNullableLong("creditLimitMinor", acc.creditLimitMinor)
                                putNullableInt("settlementDayOfMonth", acc.settlementDayOfMonth)
                                putNullableInt("paymentDueDayOfMonth", acc.paymentDueDayOfMonth)
                                putNullableLong("parentAccountId", acc.parentAccountId)
                                put("includeInNetWorth", acc.includeInNetWorth)
                                put("isArchived", acc.isArchived)
                                put("colorHex", acc.colorHex)
                                put("iconName", acc.iconName)
                                put("sortOrder", acc.sortOrder)
                                put("createdAtEpochMillis", acc.createdAtEpochMillis)
                            }
                        )
                    }
                }
            )

            put(
                "categories",
                JSONArray().apply {
                    for (cat in categories) {
                        put(
                            JSONObject().apply {
                                put("id", cat.id)
                                put("name", cat.name)
                                put("type", cat.type.name)
                                putNullableLong("parentId", cat.parentId)
                                put("iconName", cat.iconName)
                                put("colorHex", cat.colorHex)
                                put("sortOrder", cat.sortOrder)
                                put("isArchived", cat.isArchived)
                            }
                        )
                    }
                }
            )

            put(
                "tags",
                JSONArray().apply {
                    for (tag in tags) {
                        put(
                            JSONObject().apply {
                                put("id", tag.id)
                                put("name", tag.name)
                                put("colorHex", tag.colorHex)
                            }
                        )
                    }
                }
            )

            put(
                "bookmarks",
                JSONArray().apply {
                    for (bm in bookmarks) {
                        put(
                            JSONObject().apply {
                                put("id", bm.id)
                                put("title", bm.title)
                                put("type", bm.type.name)
                                put("amountMinor", bm.amountMinor)
                                put("accountId", bm.accountId)
                                putNullableLong("transferToAccountId", bm.transferToAccountId)
                                putNullableLong("categoryId", bm.categoryId)
                                put("payeeOrDescription", bm.payeeOrDescription)
                                put("note", bm.note)
                                put("tagsCsv", bm.tagsCsv)
                                put("sortOrder", bm.sortOrder)
                            }
                        )
                    }
                }
            )

            put(
                "installmentPlans",
                JSONArray().apply {
                    for (plan in installmentPlans) {
                        put(
                            JSONObject().apply {
                                put("id", plan.id)
                                put("title", plan.title)
                                put("accountId", plan.accountId)
                                putNullableLong("categoryId", plan.categoryId)
                                put("totalAmountMinor", plan.totalAmountMinor)
                                put("monthsCount", plan.monthsCount)
                                put("startDateEpochDay", plan.startDateEpochDay)
                                put("createdAtEpochMillis", plan.createdAtEpochMillis)
                            }
                        )
                    }
                }
            )

            put(
                "recurringRules",
                JSONArray().apply {
                    for (rule in recurringRules) {
                        put(
                            JSONObject().apply {
                                put("id", rule.id)
                                put("title", rule.title)
                                put("type", rule.type.name)
                                put("amountMinor", rule.amountMinor)
                                put("currencyCode", rule.currencyCode)
                                put("accountId", rule.accountId)
                                putNullableLong("transferToAccountId", rule.transferToAccountId)
                                putNullableLong("categoryId", rule.categoryId)
                                put("frequency", rule.frequency.name)
                                put("startDateEpochDay", rule.startDateEpochDay)
                                put("nextOccurrenceEpochDay", rule.nextOccurrenceEpochDay)
                                putNullableLong("endDateEpochDay", rule.endDateEpochDay)
                                put("payeeOrDescription", rule.payeeOrDescription)
                                put("note", rule.note)
                                put("isActive", rule.isActive)
                            }
                        )
                    }
                }
            )

            put(
                "transactions",
                JSONArray().apply {
                    for (tx in transactions) {
                        put(
                            JSONObject().apply {
                                put("id", tx.id)
                                put("type", tx.type.name)
                                put("timestampEpochMillis", tx.timestampEpochMillis)
                                put("ledgerDateEpochDay", tx.ledgerDateEpochDay)
                                put("amountMinor", tx.amountMinor)
                                put("currencyCode", tx.currencyCode)
                                put("accountId", tx.accountId)
                                putNullableLong("transferToAccountId", tx.transferToAccountId)
                                put("transferFeeMinor", tx.transferFeeMinor)
                                putNullableLong("categoryId", tx.categoryId)
                                put("isSplit", tx.isSplit)
                                put("payeeOrDescription", tx.payeeOrDescription)
                                put("note", tx.note)
                                putNullableLong("originalTransactionId", tx.originalTransactionId)
                                putNullableLong("installmentPlanId", tx.installmentPlanId)
                                putNullableInt("installmentIndex", tx.installmentIndex)
                                putNullableInt("installmentTotalCount", tx.installmentTotalCount)
                                putNullableLong("recurringRuleId", tx.recurringRuleId)
                            }
                        )
                    }
                }
            )

            put(
                "transactionSplits",
                JSONArray().apply {
                    for (split in splits) {
                        put(
                            JSONObject().apply {
                                put("id", split.id)
                                put("transactionId", split.transactionId)
                                put("categoryId", split.categoryId)
                                put("amountMinor", split.amountMinor)
                                put("note", split.note)
                            }
                        )
                    }
                }
            )

            put(
                "transactionTags",
                JSONArray().apply {
                    for (crossRef in tagCrossRefs) {
                        put(
                            JSONObject().apply {
                                put("transactionId", crossRef.transactionId)
                                put("tagId", crossRef.tagId)
                            }
                        )
                    }
                }
            )

            put(
                "budgets",
                JSONArray().apply {
                    for (budget in budgets) {
                        put(
                            JSONObject().apply {
                                put("id", budget.id)
                                putNullableLong("categoryId", budget.categoryId)
                                put("limitAmountMinor", budget.limitAmountMinor)
                                put("alertThresholdPercent", budget.alertThresholdPercent)
                                put("updatedAtEpochMillis", budget.updatedAtEpochMillis)
                            }
                        )
                    }
                }
            )
        }

        ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
            val entry = ZipEntry(BACKUP_JSON_ENTRY_NAME)
            zipOut.putNextEntry(entry)
            val bytes = rootJson.toString(2).toByteArray(StandardCharsets.UTF_8)
            zipOut.write(bytes)
            zipOut.closeEntry()
            zipOut.finish()
        }
        Unit
    }

    override suspend fun restoreFromZipBackup(
        sourceUriString: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            openInputStreamForLocation(sourceUriString).use { rawStream ->
                restoreBackupFromStream(rawStream)
            }
            Unit
        }
    }

    override suspend fun clearAllData(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            database.withTransaction {
                val txDao = database.transactionDao()
                val catTagDao = database.categoryAndTagDao()
                val budgetRecurringDao = database.budgetAndRecurringDao()
                val accountDao = database.accountDao()

                // Clear child tables first to respect foreign keys
                txDao.deleteAllTagCrossRefs()
                txDao.deleteAllSplits()
                txDao.deleteAllTransactions()
                txDao.deleteAllInstallmentPlans()
                budgetRecurringDao.deleteAllRecurringRules()
                budgetRecurringDao.deleteAllBudgets()
                catTagDao.deleteAllBookmarks()
                catTagDao.deleteAllTags()
                catTagDao.deleteAllCategories()
                accountDao.deleteAllAccounts()
            }
            DefaultCategorySeeder.seedIfEmpty(database.categoryAndTagDao())
            context.folioUserPreferencesDataStore.edit { mutablePrefs ->
                mutablePrefs.clear()
            }
            Unit
        }
    }

    suspend fun restoreBackupFromStream(inputStream: InputStream): Unit = withContext(Dispatchers.IO) {
        var backupJsonText: String? = null
        ZipInputStream(inputStream).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".json")) {
                    val buffer = ByteArrayOutputStream()
                    val data = ByteArray(4096)
                    var count = zipIn.read(data)
                    while (count != -1) {
                        buffer.write(data, 0, count)
                        count = zipIn.read(data)
                    }
                    backupJsonText = buffer.toString(StandardCharsets.UTF_8.name())
                    zipIn.closeEntry()
                    break
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }

        val jsonPayload = backupJsonText
            ?: throw IllegalArgumentException("Invalid backup archive: missing JSON ledger payload.")

        val root = JSONObject(jsonPayload)

        val accounts = parseAccounts(root.optJSONArray("accounts"))
        val categories = parseCategories(root.optJSONArray("categories"))
        val tags = parseTags(root.optJSONArray("tags"))
        val bookmarks = parseBookmarks(root.optJSONArray("bookmarks"))
        val installmentPlans = parseInstallmentPlans(root.optJSONArray("installmentPlans"))
        val recurringRules = parseRecurringRules(root.optJSONArray("recurringRules"))
        val transactions = parseTransactions(root.optJSONArray("transactions"))
        val splits = parseSplits(root.optJSONArray("transactionSplits"))
        val tagCrossRefs = parseTagCrossRefs(root.optJSONArray("transactionTags"))
        val budgets = parseBudgets(root.optJSONArray("budgets"))

        database.withTransaction {
            val txDao = database.transactionDao()
            val catTagDao = database.categoryAndTagDao()
            val budgetRecurringDao = database.budgetAndRecurringDao()
            val accountDao = database.accountDao()

            // Clear child tables first to respect foreign keys
            txDao.deleteAllTagCrossRefs()
            txDao.deleteAllSplits()
            txDao.deleteAllTransactions()
            txDao.deleteAllInstallmentPlans()
            budgetRecurringDao.deleteAllRecurringRules()
            budgetRecurringDao.deleteAllBudgets()
            catTagDao.deleteAllBookmarks()
            catTagDao.deleteAllTags()
            catTagDao.deleteAllCategories()
            accountDao.deleteAllAccounts()

            // Insert parent tables before child tables
            val parentAccounts = accounts.filter { it.parentAccountId == null }
            val childAccounts = accounts.filter { it.parentAccountId != null }
            accountDao.insertAccounts(parentAccounts)
            accountDao.insertAccounts(childAccounts)

            val mainCategories = categories.filter { it.parentId == null }
            val subCategories = categories.filter { it.parentId != null }
            catTagDao.insertCategories(mainCategories)
            catTagDao.insertCategories(subCategories)

            catTagDao.insertTags(tags)
            catTagDao.insertBookmarks(bookmarks)
            txDao.insertInstallmentPlansRaw(installmentPlans)
            budgetRecurringDao.insertRecurringRules(recurringRules)
            txDao.insertTransactionsRaw(transactions)
            txDao.insertSplits(splits)
            txDao.insertTagCrossRefs(tagCrossRefs)
            budgetRecurringDao.insertBudgets(budgets)
        }

        root.optJSONObject("userPreferences")?.let { prefsObj ->
            updateUserPreferences { current ->
                UserPreferences(
                    primaryCurrencyCode = prefsObj.optString(
                        "primaryCurrencyCode",
                        current.primaryCurrencyCode
                    ),
                    numberGrouping = runCatching {
                        NumberGroupingSystem.valueOf(
                            prefsObj.optString("numberGrouping", current.numberGrouping.name)
                        )
                    }.getOrDefault(current.numberGrouping),
                    monthStartDay = prefsObj.optInt("monthStartDay", current.monthStartDay)
                        .coerceIn(1, 31),
                    weekStartDayIso = prefsObj.optInt("weekStartDayIso", current.weekStartDayIso)
                        .coerceIn(1, 7),
                    subcategoriesEnabled = prefsObj.optBoolean(
                        "subcategoriesEnabled",
                        current.subcategoriesEnabled
                    ),
                    carryOverEnabled = prefsObj.optBoolean(
                        "carryOverEnabled",
                        current.carryOverEnabled
                    ),
                    themeMode = prefsObj.optString("themeMode", current.themeMode),
                    dynamicColorEnabled = prefsObj.optBoolean(
                        "dynamicColorEnabled",
                        current.dynamicColorEnabled
                    ),
                    accentPalette = prefsObj.optString("accentPalette", current.accentPalette),
                    biometricLockEnabled = prefsObj.optBoolean(
                        "biometricLockEnabled",
                        current.biometricLockEnabled
                    )
                )
            }
        }
        Unit
    }

    private fun mapPreferencesToDomain(prefs: Preferences): UserPreferences {
        val currencyCode = prefs[PreferenceKeys.PRIMARY_CURRENCY_CODE] ?: "INR"
        val defaultGrouping = SupportedCurrencies.fromCode(currencyCode).defaultGrouping
        val groupingName = prefs[PreferenceKeys.NUMBER_GROUPING] ?: defaultGrouping.name
        val grouping = runCatching { NumberGroupingSystem.valueOf(groupingName) }
            .getOrDefault(defaultGrouping)

        return UserPreferences(
            primaryCurrencyCode = currencyCode,
            numberGrouping = grouping,
            monthStartDay = (prefs[PreferenceKeys.MONTH_START_DAY] ?: 1).coerceIn(1, 31),
            weekStartDayIso = (prefs[PreferenceKeys.WEEK_START_DAY_ISO] ?: 1).coerceIn(1, 7),
            subcategoriesEnabled = prefs[PreferenceKeys.SUBCATEGORIES_ENABLED] ?: true,
            carryOverEnabled = prefs[PreferenceKeys.CARRY_OVER_ENABLED] ?: false,
            themeMode = prefs[PreferenceKeys.THEME_MODE] ?: "SYSTEM",
            dynamicColorEnabled = prefs[PreferenceKeys.DYNAMIC_COLOR_ENABLED] ?: false,
            accentPalette = prefs[PreferenceKeys.ACCENT_PALETTE] ?: "EMERALD",
            biometricLockEnabled = prefs[PreferenceKeys.BIOMETRIC_LOCK_ENABLED] ?: false
        )
    }

    private fun openOutputStreamForLocation(destinationUriString: String): OutputStream {
        if (destinationUriString.startsWith("content://") || destinationUriString.startsWith("file://")) {
            val uri = Uri.parse(destinationUriString)
            return context.contentResolver.openOutputStream(uri)
                ?: throw IOException("Unable to open OutputStream for URI: $destinationUriString")
        }

        val cleanFileName = destinationUriString
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .ifBlank { "Folio_Export_${System.currentTimeMillis()}.dat" }

        val isZip = cleanFileName.endsWith(".zip", ignoreCase = true)
        val mimeType = if (isZip) "application/zip" else "text/csv"

        // Always keep an internal copy in app-specific storage so 1-tap Restore always works
        val internalDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "FolioBackups").apply {
            mkdirs()
        }
        val internalFile = File(internalDir, cleanFileName)

        // Also write directly into the phone's public Downloads/Folio folder via MediaStore (API 29+)
        val downloadsStream: OutputStream? = runCatching {
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, cleanFileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(
                    android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    "${android.os.Environment.DIRECTORY_DOWNLOADS}/Folio"
                )
            }
            val uri = context.contentResolver.insert(
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            )
            uri?.let { context.contentResolver.openOutputStream(it) }
        }.getOrNull()

        val internalStream = FileOutputStream(internalFile)

        return if (downloadsStream != null) {
            object : OutputStream() {
                override fun write(b: Int) {
                    internalStream.write(b)
                    downloadsStream.write(b)
                }

                override fun write(b: ByteArray, off: Int, len: Int) {
                    internalStream.write(b, off, len)
                    downloadsStream.write(b, off, len)
                }

                override fun flush() {
                    internalStream.flush()
                    downloadsStream.flush()
                }

                override fun close() {
                    runCatching { internalStream.close() }
                    runCatching { downloadsStream.close() }
                }
            }
        } else {
            internalStream
        }
    }

    private fun openInputStreamForLocation(sourceUriString: String): InputStream {
        val localFile = File(sourceUriString)
        if (localFile.exists()) {
            return FileInputStream(localFile)
        }
        if (sourceUriString.startsWith("content://") || sourceUriString.startsWith("file://")) {
            val uri = Uri.parse(sourceUriString)
            return context.contentResolver.openInputStream(uri)
                ?: throw IOException("Unable to open InputStream for URI: $sourceUriString")
        }

        val internalDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "FolioBackups")
        if (sourceUriString == "LATEST_BACKUP" || !File(sourceUriString).exists()) {
            val latestZip = internalDir.listFiles { file ->
                file.isFile && file.name.endsWith(".zip", ignoreCase = true)
            }?.maxByOrNull { it.lastModified() }

            if (latestZip != null) {
                return FileInputStream(latestZip)
            }

            // Fallback: check public Downloads/Folio via MediaStore
            val projection = arrayOf(android.provider.MediaStore.MediaColumns._ID)
            val selection = "${android.provider.MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
            val selectionArgs = arrayOf("Folio_Backup%.zip")
            val sortOrder = "${android.provider.MediaStore.MediaColumns.DATE_MODIFIED} DESC"

            context.contentResolver.query(
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(0)
                    val contentUri = android.content.ContentUris.withAppendedId(
                        android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        id
                    )
                    context.contentResolver.openInputStream(contentUri)?.let { return it }
                }
            }

            throw IOException("No Folio backup (.zip) found in Downloads/Folio or app storage. Create a backup first.")
        }

        return FileInputStream(File(sourceUriString))
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

    private fun JSONObject.putNullableLong(key: String, value: Long?) {
        if (value == null) put(key, JSONObject.NULL) else put(key, value)
    }

    private fun JSONObject.putNullableInt(key: String, value: Int?) {
        if (value == null) put(key, JSONObject.NULL) else put(key, value)
    }

    private fun JSONObject.optNullableLong(key: String): Long? =
        if (isNull(key)) null else optLong(key)

    private fun JSONObject.optNullableInt(key: String): Int? =
        if (isNull(key)) null else optInt(key)

    private fun parseAccounts(array: JSONArray?): List<AccountEntity> {
        if (array == null) return emptyList()
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            AccountEntity(
                id = obj.getLong("id"),
                name = obj.getString("name"),
                groupType = runCatching {
                    AccountGroup.valueOf(obj.getString("groupType"))
                }.getOrDefault(AccountGroup.OTHER),
                currencyCode = obj.optString("currencyCode", "INR"),
                openingBalanceMinor = obj.optLong("openingBalanceMinor", 0L),
                initialUnbilledMinor = obj.optNullableLong("initialUnbilledMinor"),
                creditLimitMinor = obj.optNullableLong("creditLimitMinor"),
                settlementDayOfMonth = obj.optNullableInt("settlementDayOfMonth"),
                paymentDueDayOfMonth = obj.optNullableInt("paymentDueDayOfMonth"),
                parentAccountId = obj.optNullableLong("parentAccountId"),
                includeInNetWorth = obj.optBoolean("includeInNetWorth", true),
                isArchived = obj.optBoolean("isArchived", false),
                colorHex = obj.optString("colorHex", "#10B981"),
                iconName = obj.optString("iconName", "account_balance_wallet"),
                sortOrder = obj.optInt("sortOrder", 0),
                createdAtEpochMillis = obj.optLong("createdAtEpochMillis", System.currentTimeMillis())
            )
        }
    }

    private fun parseCategories(array: JSONArray?): List<CategoryEntity> {
        if (array == null) return emptyList()
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            CategoryEntity(
                id = obj.getLong("id"),
                name = obj.getString("name"),
                type = runCatching {
                    CategoryType.valueOf(obj.getString("type"))
                }.getOrDefault(CategoryType.EXPENSE),
                parentId = obj.optNullableLong("parentId"),
                iconName = obj.optString("iconName", "category"),
                colorHex = obj.optString("colorHex", "#10B981"),
                sortOrder = obj.optInt("sortOrder", 0),
                isArchived = obj.optBoolean("isArchived", false)
            )
        }
    }

    private fun parseTags(array: JSONArray?): List<TagEntity> {
        if (array == null) return emptyList()
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            TagEntity(
                id = obj.getLong("id"),
                name = obj.getString("name"),
                colorHex = obj.optString("colorHex", "#64748B")
            )
        }
    }

    private fun parseBookmarks(array: JSONArray?): List<BookmarkEntity> {
        if (array == null) return emptyList()
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            BookmarkEntity(
                id = obj.getLong("id"),
                title = obj.getString("title"),
                type = runCatching {
                    TransactionType.valueOf(obj.getString("type"))
                }.getOrDefault(TransactionType.EXPENSE),
                amountMinor = obj.getLong("amountMinor"),
                accountId = obj.getLong("accountId"),
                transferToAccountId = obj.optNullableLong("transferToAccountId"),
                categoryId = obj.optNullableLong("categoryId"),
                payeeOrDescription = obj.optString("payeeOrDescription", ""),
                note = obj.optString("note", ""),
                tagsCsv = obj.optString("tagsCsv", ""),
                sortOrder = obj.optInt("sortOrder", 0)
            )
        }
    }

    private fun parseInstallmentPlans(array: JSONArray?): List<InstallmentPlanEntity> {
        if (array == null) return emptyList()
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            InstallmentPlanEntity(
                id = obj.getLong("id"),
                title = obj.getString("title"),
                accountId = obj.getLong("accountId"),
                categoryId = obj.optNullableLong("categoryId"),
                totalAmountMinor = obj.getLong("totalAmountMinor"),
                monthsCount = obj.getInt("monthsCount"),
                startDateEpochDay = obj.getLong("startDateEpochDay"),
                createdAtEpochMillis = obj.optLong("createdAtEpochMillis", System.currentTimeMillis())
            )
        }
    }

    private fun parseRecurringRules(array: JSONArray?): List<RecurringRuleEntity> {
        if (array == null) return emptyList()
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            RecurringRuleEntity(
                id = obj.getLong("id"),
                title = obj.getString("title"),
                type = runCatching {
                    TransactionType.valueOf(obj.getString("type"))
                }.getOrDefault(TransactionType.EXPENSE),
                amountMinor = obj.getLong("amountMinor"),
                currencyCode = obj.optString("currencyCode", "INR"),
                accountId = obj.getLong("accountId"),
                transferToAccountId = obj.optNullableLong("transferToAccountId"),
                categoryId = obj.optNullableLong("categoryId"),
                frequency = runCatching {
                    RepeatFrequency.valueOf(obj.getString("frequency"))
                }.getOrDefault(RepeatFrequency.MONTHLY),
                startDateEpochDay = obj.getLong("startDateEpochDay"),
                nextOccurrenceEpochDay = obj.getLong("nextOccurrenceEpochDay"),
                endDateEpochDay = obj.optNullableLong("endDateEpochDay"),
                payeeOrDescription = obj.optString("payeeOrDescription", ""),
                note = obj.optString("note", ""),
                isActive = obj.optBoolean("isActive", true)
            )
        }
    }

    private fun parseTransactions(array: JSONArray?): List<TransactionEntity> {
        if (array == null) return emptyList()
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            TransactionEntity(
                id = obj.getLong("id"),
                type = runCatching {
                    TransactionType.valueOf(obj.getString("type"))
                }.getOrDefault(TransactionType.EXPENSE),
                timestampEpochMillis = obj.getLong("timestampEpochMillis"),
                ledgerDateEpochDay = obj.getLong("ledgerDateEpochDay"),
                amountMinor = obj.getLong("amountMinor"),
                currencyCode = obj.optString("currencyCode", "INR"),
                accountId = obj.getLong("accountId"),
                transferToAccountId = obj.optNullableLong("transferToAccountId"),
                transferFeeMinor = obj.optLong("transferFeeMinor", 0L),
                categoryId = obj.optNullableLong("categoryId"),
                isSplit = obj.optBoolean("isSplit", false),
                payeeOrDescription = obj.optString("payeeOrDescription", ""),
                note = obj.optString("note", ""),
                originalTransactionId = obj.optNullableLong("originalTransactionId"),
                installmentPlanId = obj.optNullableLong("installmentPlanId"),
                installmentIndex = obj.optNullableInt("installmentIndex"),
                installmentTotalCount = obj.optNullableInt("installmentTotalCount"),
                recurringRuleId = obj.optNullableLong("recurringRuleId")
            )
        }
    }

    private fun parseSplits(array: JSONArray?): List<TransactionSplitEntity> {
        if (array == null) return emptyList()
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            TransactionSplitEntity(
                id = obj.getLong("id"),
                transactionId = obj.getLong("transactionId"),
                categoryId = obj.getLong("categoryId"),
                amountMinor = obj.getLong("amountMinor"),
                note = obj.optString("note", "")
            )
        }
    }

    private fun parseTagCrossRefs(array: JSONArray?): List<TransactionTagCrossRef> {
        if (array == null) return emptyList()
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            TransactionTagCrossRef(
                transactionId = obj.getLong("transactionId"),
                tagId = obj.getLong("tagId")
            )
        }
    }

    private fun parseBudgets(array: JSONArray?): List<BudgetEntity> {
        if (array == null) return emptyList()
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            BudgetEntity(
                id = obj.getLong("id"),
                categoryId = obj.optNullableLong("categoryId"),
                limitAmountMinor = obj.getLong("limitAmountMinor"),
                alertThresholdPercent = obj.optInt("alertThresholdPercent", 80),
                updatedAtEpochMillis = obj.optLong("updatedAtEpochMillis", System.currentTimeMillis())
            )
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
        val existingTransactions = database.transactionDao().getAllTransactionsRawOnce()
        val accountMap = existingAccounts.associateBy { it.name.trim().lowercase() }

        val existingFingerprints = existingTransactions.map { tx ->
            "${tx.ledgerDateEpochDay}|${tx.accountId}|${tx.type.name}|${tx.amountMinor}|${tx.payeeOrDescription.trim().lowercase()}"
        }.toMutableSet()

        var currentSection = CsvSectionState.LEGACY
        var colMap: Map<String, Int> = emptyMap()

        val detectedFirst = detectSectionHeader(firstLine)
        if (detectedFirst != null) {
            currentSection = detectedFirst
        } else {
            val headerTokens = parseCsvLine(firstLine)
            colMap = headerTokens.mapIndexed { idx, name ->
                name.trim().lowercase().replace("_", "").replace(" ", "") to idx
            }.toMap()
        }

        var totalRows = 0
        var validCount = 0
        var duplicateCount = 0
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
                    line = reader.readLine()
                    continue
                }

                if (trimmed.startsWith("#") || trimmed.startsWith("//")) {
                    line = reader.readLine()
                    continue
                }

                if (colMap.isEmpty()) {
                    val tokens = parseCsvLine(trimmed)
                    colMap = tokens.mapIndexed { idx, name ->
                        name.trim().lowercase().replace("_", "").replace(" ", "") to idx
                    }.toMap()
                    line = reader.readLine()
                    continue
                }

                totalRows++
                val tokens = parseCsvLine(trimmed)

                when (currentSection) {
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
            accountsWithOpeningBalance = accountsWithOpeningBalance
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
            val trimmedName = name.trim()
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

        val detectedFirst = detectSectionHeader(firstLine)
        if (detectedFirst != null) {
            currentSection = detectedFirst
        } else {
            val headerTokens = parseCsvLine(firstLine)
            colMap = headerTokens.mapIndexed { idx, name ->
                name.trim().lowercase().replace("_", "").replace(" ", "") to idx
            }.toMap()
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
                    line = reader.readLine()
                    continue
                }

                if (trimmed.startsWith("#") || trimmed.startsWith("//")) {
                    line = reader.readLine()
                    continue
                }

                if (colMap.isEmpty()) {
                    val tokens = parseCsvLine(trimmed)
                    colMap = tokens.mapIndexed { idx, name ->
                        name.trim().lowercase().replace("_", "").replace(" ", "") to idx
                    }.toMap()
                    line = reader.readLine()
                    continue
                }

                totalRows++
                val tokens = parseCsvLine(trimmed)

                when (currentSection) {
                    CsvSectionState.ACCOUNTS -> {
                        val nameIdx = colMap["name"] ?: colMap["account"] ?: colMap["accountname"] ?: 0
                        val name = tokens.getOrNull(nameIdx)?.trim() ?: ""
                        if (name.isNotEmpty()) {
                            val rawGroup = colMap["group"]?.let { tokens.getOrNull(it)?.trim() }
                                ?: colMap["grouptype"]?.let { tokens.getOrNull(it)?.trim() }
                            val group = parseAccountGroupFromTokens(rawGroup, name)

                            val currency = colMap["currency"]?.let { tokens.getOrNull(it)?.trim() }?.ifBlank { "INR" }
                                ?: "INR"
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
                        val currency = currencyIdx?.let { tokens.getOrNull(it)?.trim() }?.ifBlank { "INR" } ?: "INR"
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
                        val tagList = tagsStr.split(";", ",").map { it.trim() }.filter { it.isNotEmpty() }

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



    companion object {
        private const val BACKUP_SCHEMA_VERSION = 1
        private const val BACKUP_JSON_ENTRY_NAME = "folio_backup_v1.json"
    }
}

class CategoryAndTagRepositoryImpl(
    private val categoryAndTagDao: CategoryAndTagDao
) : CategoryAndTagRepository {

    override fun observeCategoryTree(type: CategoryType): Flow<List<CategoryNode>> {
        return categoryAndTagDao.observeCategoriesByType(type)
            .onStart { DefaultCategorySeeder.seedIfEmpty(categoryAndTagDao) }
            .map { entities -> buildCategoryTree(entities) }
    }

    override fun observeAllCategoryTrees(): Flow<List<CategoryNode>> {
        return categoryAndTagDao.observeAllCategories()
            .onStart { DefaultCategorySeeder.seedIfEmpty(categoryAndTagDao) }
            .map { entities -> buildCategoryTree(entities) }
    }

    override fun observeAllTags(): Flow<List<String>> {
        return categoryAndTagDao.observeAllTags()
            .map { tagEntities -> tagEntities.map { it.name } }
    }

    override fun observeBookmarks(): Flow<List<TransactionBookmark>> {
        return categoryAndTagDao.observeAllBookmarks()
            .map { bookmarkEntities ->
                bookmarkEntities.map { entity ->
                    TransactionBookmark(
                        id = entity.id,
                        title = entity.title,
                        type = entity.type,
                        amount = MinorMoney(entity.amountMinor),
                        accountId = entity.accountId,
                        transferToAccountId = entity.transferToAccountId,
                        categoryId = entity.categoryId,
                        payeeOrDescription = entity.payeeOrDescription,
                        note = entity.note,
                        tags = if (entity.tagsCsv.isBlank()) {
                            emptyList()
                        } else {
                            entity.tagsCsv.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                        }
                    )
                }
            }
    }

    override suspend fun upsertCategory(category: CategoryNode): Result<Long> = runCatching {
        val trimmedName = category.name.trim()
        require(trimmedName.isNotEmpty()) { "Category name cannot be blank." }

        val existing = if (category.id != 0L) {
            categoryAndTagDao.getCategoryByIdOnce(category.id)
        } else {
            null
        }

        val entity = CategoryEntity(
            id = category.id,
            name = trimmedName,
            type = category.type,
            parentId = category.parentId,
            iconName = category.iconName.ifBlank { "category" },
            colorHex = category.colorHex.ifBlank { "#10B981" },
            sortOrder = existing?.sortOrder ?: 0,
            isArchived = existing?.isArchived ?: false
        )
        categoryAndTagDao.insertCategory(entity)
    }

    override suspend fun deleteCategory(
        categoryId: Long,
        reassignToCategoryId: Long?
    ): Result<Unit> = runCatching {
        if (reassignToCategoryId != null && reassignToCategoryId != categoryId) {
            categoryAndTagDao.reassignTransactionsCategory(categoryId, reassignToCategoryId)
            categoryAndTagDao.reassignSplitsCategory(categoryId, reassignToCategoryId)
        }
        categoryAndTagDao.deleteCategoryById(categoryId)
    }

    override suspend fun createTag(tagName: String): Result<Unit> = runCatching {
        val trimmed = tagName.trim()
        require(trimmed.isNotEmpty()) { "Tag name cannot be blank." }
        categoryAndTagDao.insertTag(TagEntity(name = trimmed))
    }

    override suspend fun renameTag(oldTagName: String, newTagName: String): Result<Unit> = runCatching {
        val oldTrimmed = oldTagName.trim()
        val newTrimmed = newTagName.trim()
        require(oldTrimmed.isNotEmpty() && newTrimmed.isNotEmpty()) {
            "Tag names cannot be blank."
        }
        categoryAndTagDao.renameTag(oldTrimmed, newTrimmed)
    }

    override suspend fun deleteTag(tagName: String): Result<Unit> = runCatching {
        val trimmed = tagName.trim()
        if (trimmed.isNotEmpty()) {
            categoryAndTagDao.deleteTagByName(trimmed)
        }
    }

    override suspend fun upsertBookmark(bookmark: TransactionBookmark): Result<Long> = runCatching {
        val trimmedTitle = bookmark.title.trim()
        require(trimmedTitle.isNotEmpty()) { "Bookmark title cannot be blank." }
        require(bookmark.amount.isPositive) { "Bookmark amount must be greater than zero." }

        val tagsCsv = bookmark.tags
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .joinToString(",")

        val entity = BookmarkEntity(
            id = bookmark.id,
            title = trimmedTitle,
            type = bookmark.type,
            amountMinor = bookmark.amount.amountMinor,
            accountId = bookmark.accountId,
            transferToAccountId = bookmark.transferToAccountId,
            categoryId = bookmark.categoryId,
            payeeOrDescription = bookmark.payeeOrDescription.trim(),
            note = bookmark.note.trim(),
            tagsCsv = tagsCsv
        )
        categoryAndTagDao.upsertBookmark(entity)
    }

    override suspend fun deleteBookmark(bookmarkId: Long): Result<Unit> = runCatching {
        categoryAndTagDao.deleteBookmarkById(bookmarkId)
    }

    private fun buildCategoryTree(entities: List<CategoryEntity>): List<CategoryNode> {
        val childrenByParentId = entities
            .filter { it.parentId != null }
            .groupBy { it.parentId }

        return entities
            .filter { it.parentId == null }
            .sortedWith(compareBy<CategoryEntity> { it.sortOrder }.thenBy { it.id })
            .map { mainCat ->
                val subcategories = childrenByParentId[mainCat.id]
                    .orEmpty()
                    .sortedWith(compareBy<CategoryEntity> { it.sortOrder }.thenBy { it.id })
                    .map { subCat ->
                        CategoryNode(
                            id = subCat.id,
                            name = subCat.name,
                            type = subCat.type,
                            parentId = subCat.parentId,
                            iconName = subCat.iconName,
                            colorHex = subCat.colorHex,
                            subcategories = emptyList()
                        )
                    }

                CategoryNode(
                    id = mainCat.id,
                    name = mainCat.name,
                    type = mainCat.type,
                    parentId = null,
                    iconName = mainCat.iconName,
                    colorHex = mainCat.colorHex,
                    subcategories = subcategories
                )
            }
    }
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
    TRANSACTIONS
}

internal fun detectSectionHeader(line: String): CsvSectionState? {
    val trimmed = line.trim().uppercase()
    if (trimmed.startsWith("# HINT") ||
        trimmed.startsWith("# FORMAT") ||
        trimmed.startsWith("# EXAMPLE") ||
        trimmed.startsWith("# NOTE") ||
        trimmed.startsWith("//")
    ) {
        return null
    }

    val clean = trimmed
        .replace("=", "")
        .replace("[", "")
        .replace("]", "")
        .replace("#", "")
        .replace(",", "")
        .trim()

    return when {
        clean.startsWith("ACCOUNT") -> CsvSectionState.ACCOUNTS
        clean.startsWith("CATEGOR") -> CsvSectionState.CATEGORIES
        clean.startsWith("TRANSACTION") -> CsvSectionState.TRANSACTIONS
        else -> null
    }
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


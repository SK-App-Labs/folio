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

    suspend fun exportTransactionsToCsvStream(
        outputStream: OutputStream,
        startEpochDay: Long? = null,
        endEpochDay: Long? = null
    ): Int = withContext(Dispatchers.IO) {
        val accountsById = database.accountDao()
            .getAllAccountsOnce()
            .associateBy { it.id }

        val categoriesById = database.categoryAndTagDao()
            .getAllCategoriesOnce()
            .associateBy { it.id }

        val allTransactions = database.transactionDao().getAllTransactionsWithDetailsOnce()
        val filteredTransactions = allTransactions.filter { txWithDetails ->
            val day = txWithDetails.transaction.ledgerDateEpochDay
            val afterStart = startEpochDay == null || day >= startEpochDay
            val beforeEnd = endEpochDay == null || day <= endEpochDay
            afterStart && beforeEnd
        }

        val writer = BufferedWriter(OutputStreamWriter(outputStream, StandardCharsets.UTF_8))
        writer.write(
            "ID,Date,Type,Account,TransferToAccount,Category,Subcategory,AmountFormatted,AmountMinor,TransferFeeMinor,Currency,PayeeOrDescription,Note,Tags,IsSplit,Installment"
        )
        writer.newLine()

        for (item in filteredTransactions) {
            val tx = item.transaction
            val dateIso = LocalDate.ofEpochDay(tx.ledgerDateEpochDay).toString()
            val accountName = accountsById[tx.accountId]?.name ?: ""
            val transferAccountName = tx.transferToAccountId?.let { accountsById[it]?.name } ?: ""

            val categoryEntity = tx.categoryId?.let { categoriesById[it] }
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
            val formattedAmount = MoneyFormatterAndCalculator.formatMinorMoney(
                money = MinorMoney(tx.amountMinor),
                currencySpec = currencySpec,
                includeSymbol = false
            )
            val tagsJoined = item.tags.joinToString(";") { it.name }
            val installmentLabel = if (
                tx.installmentIndex != null && tx.installmentTotalCount != null
            ) {
                "${tx.installmentIndex}/${tx.installmentTotalCount}"
            } else {
                ""
            }

            val rowColumns = listOf(
                tx.id.toString(),
                dateIso,
                tx.type.name,
                escapeCsvField(accountName),
                escapeCsvField(transferAccountName),
                escapeCsvField(mainCategoryName),
                escapeCsvField(subCategoryName),
                escapeCsvField(formattedAmount),
                tx.amountMinor.toString(),
                tx.transferFeeMinor.toString(),
                tx.currencyCode,
                escapeCsvField(tx.payeeOrDescription),
                escapeCsvField(tx.note),
                escapeCsvField(tagsJoined),
                tx.isSplit.toString(),
                escapeCsvField(installmentLabel)
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
        return if (
            destinationUriString.startsWith("content://") ||
            destinationUriString.startsWith("file://")
        ) {
            val uri = Uri.parse(destinationUriString)
            context.contentResolver.openOutputStream(uri)
                ?: throw IOException("Unable to open OutputStream for URI: $destinationUriString")
        } else {
            val file = File(destinationUriString)
            file.parentFile?.mkdirs()
            FileOutputStream(file)
        }
    }

    private fun openInputStreamForLocation(sourceUriString: String): InputStream {
        return if (
            sourceUriString.startsWith("content://") ||
            sourceUriString.startsWith("file://")
        ) {
            val uri = Uri.parse(sourceUriString)
            context.contentResolver.openInputStream(uri)
                ?: throw IOException("Unable to open InputStream for URI: $sourceUriString")
        } else {
            FileInputStream(File(sourceUriString))
        }
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
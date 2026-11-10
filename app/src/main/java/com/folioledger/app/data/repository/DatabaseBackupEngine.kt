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

class DatabaseBackupEngine(
    private val context: Context,
    private val database: FolioDatabase,
    private val userPreferences: UserPreferencesDataSource
) {

    companion object {
        const val BACKUP_SCHEMA_VERSION = 1
        const val BACKUP_JSON_ENTRY_NAME = "folio_backup_v1.json"
    }

    suspend fun createPortableZipBackup(
        destinationUriString: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            StorageAccessUtils.openOutputStreamForLocation(context, destinationUriString).use { rawStream ->
                createBackupToStream(rawStream)
            }
            userPreferences.updateUserPreferences { it.copy(lastBackupEpochMillis = System.currentTimeMillis()) }
            destinationUriString
        }
    }

    suspend fun createBackupToStream(outputStream: OutputStream): Unit = withContext(Dispatchers.IO) {
        val currentPrefs = userPreferences.observeUserPreferences().first()
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
                    put("secondaryCurrencies", serializeSecondaryCurrencies(currentPrefs.secondaryCurrencies))
                    put("numberGrouping", currentPrefs.numberGrouping.name)
                    put("monthStartDay", currentPrefs.monthStartDay)
                    put("weekStartDayIso", currentPrefs.weekStartDayIso)
                    put("subcategoriesEnabled", currentPrefs.subcategoriesEnabled)
                    put("carryOverEnabled", currentPrefs.carryOverEnabled)
                    put("themeMode", currentPrefs.themeMode)
                    put("dynamicColorEnabled", currentPrefs.dynamicColorEnabled)
                    put("accentPalette", currentPrefs.accentPalette)
                    put("biometricLockEnabled", currentPrefs.biometricLockEnabled)
                    put("isSupporter", currentPrefs.isSupporter)
                    putNullableString("supporterTier", currentPrefs.supporterTier)
                    put("cardMilestonesJson", currentPrefs.cardMilestonesJson)
                    put("iconPackTheme", currentPrefs.iconPackTheme)
                    put("monthlySavingsTargetMinor", currentPrefs.monthlySavingsTargetMinor)
                    put("savingsGoalsJson", currentPrefs.savingsGoalsJson)
                    put("remindersEnabled", currentPrefs.remindersEnabled)
                    put("ccReminderLeadDays", currentPrefs.ccReminderLeadDays)
                    put("billReminderLeadDays", currentPrefs.billReminderLeadDays)
                    put("reminderHourOfDay", currentPrefs.reminderHourOfDay)
                    put("reminderMinute", currentPrefs.reminderMinute)
                    put("hideAmountsOnLockScreen", currentPrefs.hideAmountsOnLockScreen)
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
                                put("isPinned", acc.isPinned)
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

    suspend fun restoreFromZipBackup(
        sourceUriString: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            createSafetyRollbackSnapshot()
            StorageAccessUtils.openInputStreamForLocation(context, sourceUriString).use { rawStream ->
                restoreBackupFromStream(rawStream)
            }
            Unit
        }
    }

    private suspend fun createSafetyRollbackSnapshot() {
        runCatching {
            val safetyFile = File(context.cacheDir, "pre_restore_safety_backup.zip")
            FileOutputStream(safetyFile).use { output ->
                createBackupToStream(output)
            }
        }
    }

    suspend fun clearAllData(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
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
            userPreferences.updateUserPreferences { current ->
                UserPreferences(
                    primaryCurrencyCode = prefsObj.optString(
                        "primaryCurrencyCode",
                        current.primaryCurrencyCode
                    ),
                    secondaryCurrencies = if (prefsObj.has("secondaryCurrencies")) {
                        parseSecondaryCurrencies(prefsObj.optString("secondaryCurrencies", "[]"))
                    } else {
                        current.secondaryCurrencies
                    },
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
                    ),
                    isSupporter = prefsObj.optBoolean(
                        "isSupporter",
                        current.isSupporter
                    ),
                    supporterTier = if (prefsObj.isNull("supporterTier")) current.supporterTier else prefsObj.optString("supporterTier").ifBlank { null },
                    lastBackupEpochMillis = current.lastBackupEpochMillis,
                    cardMilestonesJson = prefsObj.optString("cardMilestonesJson", current.cardMilestonesJson),
                    iconPackTheme = prefsObj.optString("iconPackTheme", current.iconPackTheme),
                    monthlySavingsTargetMinor = prefsObj.optLong("monthlySavingsTargetMinor", current.monthlySavingsTargetMinor),
                    savingsGoalsJson = prefsObj.optString("savingsGoalsJson", current.savingsGoalsJson),
                    hasSeenWelcomeSheet = true,
                    isDemoModeActive = false,
                    hasStashedRealLedger = false,
                    remindersEnabled = prefsObj.optBoolean("remindersEnabled", current.remindersEnabled),
                    ccReminderLeadDays = prefsObj.optInt("ccReminderLeadDays", current.ccReminderLeadDays),
                    billReminderLeadDays = prefsObj.optInt("billReminderLeadDays", current.billReminderLeadDays),
                    reminderHourOfDay = prefsObj.optInt("reminderHourOfDay", current.reminderHourOfDay),
                    reminderMinute = prefsObj.optInt("reminderMinute", current.reminderMinute),
                    hideAmountsOnLockScreen = prefsObj.optBoolean("hideAmountsOnLockScreen", current.hideAmountsOnLockScreen)
                )
            }
        }
        Unit
    }


    private fun JSONObject.putNullableString(key: String, value: String?) {
        if (value == null) put(key, JSONObject.NULL) else put(key, value)
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

    internal fun parseAccounts(array: JSONArray?): List<AccountEntity> {
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
                isPinned = obj.optBoolean("isPinned", false),
                createdAtEpochMillis = obj.optLong("createdAtEpochMillis", System.currentTimeMillis())
            )
        }
    }

    internal fun parseCategories(array: JSONArray?): List<CategoryEntity> {
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

    internal fun parseTags(array: JSONArray?): List<TagEntity> {
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

    internal fun parseBookmarks(array: JSONArray?): List<BookmarkEntity> {
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

    internal fun parseInstallmentPlans(array: JSONArray?): List<InstallmentPlanEntity> {
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

    internal fun parseRecurringRules(array: JSONArray?): List<RecurringRuleEntity> {
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
                    when (val raw = obj.getString("frequency")) {
                        "DAILY" -> RepeatFrequency.EVERY_DAY
                        "WEEKLY" -> RepeatFrequency.EVERY_WEEK
                        "BIWEEKLY" -> RepeatFrequency.EVERY_2_WEEKS
                        "MONTHLY" -> RepeatFrequency.EVERY_MONTH
                        "YEARLY" -> RepeatFrequency.ANNUALLY
                        else -> RepeatFrequency.valueOf(raw)
                    }
                }.getOrDefault(RepeatFrequency.EVERY_MONTH),
                startDateEpochDay = obj.getLong("startDateEpochDay"),
                nextOccurrenceEpochDay = obj.getLong("nextOccurrenceEpochDay"),
                endDateEpochDay = obj.optNullableLong("endDateEpochDay"),
                payeeOrDescription = obj.optString("payeeOrDescription", ""),
                note = obj.optString("note", ""),
                isActive = obj.optBoolean("isActive", true)
            )
        }
    }

    internal fun parseTransactions(array: JSONArray?): List<TransactionEntity> {
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

    internal fun parseSplits(array: JSONArray?): List<TransactionSplitEntity> {
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

    internal fun parseTagCrossRefs(array: JSONArray?): List<TransactionTagCrossRef> {
        if (array == null) return emptyList()
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            TransactionTagCrossRef(
                transactionId = obj.getLong("transactionId"),
                tagId = obj.getLong("tagId")
            )
        }
    }

    internal fun parseBudgets(array: JSONArray?): List<BudgetEntity> {
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





}

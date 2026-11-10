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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

internal val Context.folioUserPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "folio_user_preferences"
)

class UserPreferencesDataSource(private val context: Context) {

    private object PreferenceKeys {
        val PRIMARY_CURRENCY_CODE = stringPreferencesKey("primary_currency_code")
        val SECONDARY_CURRENCIES_JSON = stringPreferencesKey("secondary_currencies_json")
        val NUMBER_GROUPING = stringPreferencesKey("number_grouping")
        val MONTH_START_DAY = intPreferencesKey("month_start_day")
        val WEEK_START_DAY_ISO = intPreferencesKey("week_start_day_iso")
        val SUBCATEGORIES_ENABLED = booleanPreferencesKey("subcategories_enabled")
        val CARRY_OVER_ENABLED = booleanPreferencesKey("carry_over_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
        val ACCENT_PALETTE = stringPreferencesKey("accent_palette")
        val BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
        val IS_SUPPORTER = booleanPreferencesKey("is_supporter")
        val SUPPORTER_TIER = stringPreferencesKey("supporter_tier")
        val LAST_BACKUP_EPOCH_MILLIS = longPreferencesKey("last_backup_epoch_millis")
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val AUTO_BACKUP_RETENTION = stringPreferencesKey("auto_backup_retention")
        val CARD_MILESTONES_JSON = stringPreferencesKey("card_milestones_json")
        val ICON_PACK_THEME = stringPreferencesKey("icon_pack_theme")
        val MONTHLY_SAVINGS_TARGET_MINOR = longPreferencesKey("monthly_savings_target_minor")
        val SAVINGS_GOALS_JSON = stringPreferencesKey("savings_goals_json")
        val HAS_SEEN_WELCOME_SHEET = booleanPreferencesKey("has_seen_welcome_sheet")
        val IS_DEMO_MODE_ACTIVE = booleanPreferencesKey("is_demo_mode_active")
        val HAS_STASHED_REAL_LEDGER = booleanPreferencesKey("has_stashed_real_ledger")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val CC_REMINDER_LEAD_DAYS = intPreferencesKey("cc_reminder_lead_days")
        val BILL_REMINDER_LEAD_DAYS = intPreferencesKey("bill_reminder_lead_days")
        val REMINDER_HOUR_OF_DAY = intPreferencesKey("reminder_hour_of_day")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val HIDE_AMOUNTS_ON_LOCK_SCREEN = booleanPreferencesKey("hide_amounts_on_lock_screen")
    }

    fun observeUserPreferences(): Flow<UserPreferences> {
        return context.folioUserPreferencesDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { prefs -> mapPreferencesToDomain(prefs) }
            .distinctUntilChanged()
    }

    suspend fun updateUserPreferences(
        transform: (UserPreferences) -> UserPreferences
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
        context.folioUserPreferencesDataStore.edit { mutablePrefs ->
            val current = mapPreferencesToDomain(mutablePrefs)
            val updated = transform(current)
            val validatedMonthStart = updated.monthStartDay.coerceIn(1, 31)
            val validatedWeekStart = updated.weekStartDayIso.coerceIn(1, 7)

            mutablePrefs[PreferenceKeys.PRIMARY_CURRENCY_CODE] = updated.primaryCurrencyCode
            mutablePrefs[PreferenceKeys.SECONDARY_CURRENCIES_JSON] = serializeSecondaryCurrencies(updated.secondaryCurrencies)
            mutablePrefs[PreferenceKeys.NUMBER_GROUPING] = updated.numberGrouping.name
            mutablePrefs[PreferenceKeys.MONTH_START_DAY] = validatedMonthStart
            mutablePrefs[PreferenceKeys.WEEK_START_DAY_ISO] = validatedWeekStart
            mutablePrefs[PreferenceKeys.SUBCATEGORIES_ENABLED] = updated.subcategoriesEnabled
            mutablePrefs[PreferenceKeys.CARRY_OVER_ENABLED] = updated.carryOverEnabled
            mutablePrefs[PreferenceKeys.THEME_MODE] = updated.themeMode
            mutablePrefs[PreferenceKeys.DYNAMIC_COLOR_ENABLED] = updated.dynamicColorEnabled
            mutablePrefs[PreferenceKeys.ACCENT_PALETTE] = updated.accentPalette
            mutablePrefs[PreferenceKeys.BIOMETRIC_LOCK_ENABLED] = updated.biometricLockEnabled
            mutablePrefs[PreferenceKeys.IS_SUPPORTER] = updated.isSupporter
            if (updated.supporterTier != null) {
                mutablePrefs[PreferenceKeys.SUPPORTER_TIER] = updated.supporterTier
            } else {
                mutablePrefs.remove(PreferenceKeys.SUPPORTER_TIER)
            }
            if (updated.lastBackupEpochMillis != null) {
                mutablePrefs[PreferenceKeys.LAST_BACKUP_EPOCH_MILLIS] = updated.lastBackupEpochMillis
            } else {
                mutablePrefs.remove(PreferenceKeys.LAST_BACKUP_EPOCH_MILLIS)
            }
            mutablePrefs[PreferenceKeys.AUTO_BACKUP_ENABLED] = updated.autoBackupEnabled
            mutablePrefs[PreferenceKeys.AUTO_BACKUP_RETENTION] = updated.autoBackupRetention.name
            mutablePrefs[PreferenceKeys.CARD_MILESTONES_JSON] = updated.cardMilestonesJson
            mutablePrefs[PreferenceKeys.ICON_PACK_THEME] = updated.iconPackTheme
            mutablePrefs[PreferenceKeys.MONTHLY_SAVINGS_TARGET_MINOR] = updated.monthlySavingsTargetMinor
            mutablePrefs[PreferenceKeys.SAVINGS_GOALS_JSON] = updated.savingsGoalsJson
            mutablePrefs[PreferenceKeys.HAS_SEEN_WELCOME_SHEET] = updated.hasSeenWelcomeSheet
            mutablePrefs[PreferenceKeys.IS_DEMO_MODE_ACTIVE] = updated.isDemoModeActive
            mutablePrefs[PreferenceKeys.HAS_STASHED_REAL_LEDGER] = updated.hasStashedRealLedger
            mutablePrefs[PreferenceKeys.REMINDERS_ENABLED] = updated.remindersEnabled
            mutablePrefs[PreferenceKeys.CC_REMINDER_LEAD_DAYS] = updated.ccReminderLeadDays.coerceIn(0, 14)
            mutablePrefs[PreferenceKeys.BILL_REMINDER_LEAD_DAYS] = updated.billReminderLeadDays.coerceIn(0, 7)
            mutablePrefs[PreferenceKeys.REMINDER_HOUR_OF_DAY] = updated.reminderHourOfDay.coerceIn(0, 23)
            mutablePrefs[PreferenceKeys.REMINDER_MINUTE] = updated.reminderMinute.coerceIn(0, 59)
            mutablePrefs[PreferenceKeys.HIDE_AMOUNTS_ON_LOCK_SCREEN] = updated.hideAmountsOnLockScreen
        }
        Unit
        }
    }

    fun mapPreferencesToDomain(prefs: Preferences): UserPreferences {
        val currencyCode = prefs[PreferenceKeys.PRIMARY_CURRENCY_CODE] ?: "INR"
        val secondaryJson = prefs[PreferenceKeys.SECONDARY_CURRENCIES_JSON] ?: "[]"
        val secondaryCurrencies = parseSecondaryCurrencies(secondaryJson)
        val defaultGrouping = SupportedCurrencies.fromCode(currencyCode).defaultGrouping
        val groupingName = prefs[PreferenceKeys.NUMBER_GROUPING] ?: defaultGrouping.name
        val grouping = runCatching { NumberGroupingSystem.valueOf(groupingName) }
            .getOrDefault(defaultGrouping)

        return UserPreferences(
            primaryCurrencyCode = currencyCode,
            secondaryCurrencies = secondaryCurrencies,
            numberGrouping = grouping,
            monthStartDay = (prefs[PreferenceKeys.MONTH_START_DAY] ?: 1).coerceIn(1, 31),
            weekStartDayIso = (prefs[PreferenceKeys.WEEK_START_DAY_ISO] ?: 1).coerceIn(1, 7),
            subcategoriesEnabled = prefs[PreferenceKeys.SUBCATEGORIES_ENABLED] ?: true,
            carryOverEnabled = prefs[PreferenceKeys.CARRY_OVER_ENABLED] ?: false,
            themeMode = prefs[PreferenceKeys.THEME_MODE] ?: "SYSTEM",
            dynamicColorEnabled = prefs[PreferenceKeys.DYNAMIC_COLOR_ENABLED] ?: false,
            accentPalette = prefs[PreferenceKeys.ACCENT_PALETTE] ?: "EMERALD",
            biometricLockEnabled = prefs[PreferenceKeys.BIOMETRIC_LOCK_ENABLED] ?: false,
            isSupporter = prefs[PreferenceKeys.IS_SUPPORTER] ?: false,
            supporterTier = prefs[PreferenceKeys.SUPPORTER_TIER],
            lastBackupEpochMillis = prefs[PreferenceKeys.LAST_BACKUP_EPOCH_MILLIS],
            autoBackupEnabled = prefs[PreferenceKeys.AUTO_BACKUP_ENABLED] ?: true,
            autoBackupRetention = runCatching {
                AutoBackupRetention.valueOf(prefs[PreferenceKeys.AUTO_BACKUP_RETENTION] ?: AutoBackupRetention.THIRTY_DAYS.name)
            }.getOrDefault(AutoBackupRetention.THIRTY_DAYS),
            cardMilestonesJson = prefs[PreferenceKeys.CARD_MILESTONES_JSON] ?: "{}",
            iconPackTheme = prefs[PreferenceKeys.ICON_PACK_THEME] ?: "MATERIAL_FILLED",
            monthlySavingsTargetMinor = prefs[PreferenceKeys.MONTHLY_SAVINGS_TARGET_MINOR] ?: 0L,
            savingsGoalsJson = prefs[PreferenceKeys.SAVINGS_GOALS_JSON] ?: "{}",
            hasSeenWelcomeSheet = prefs[PreferenceKeys.HAS_SEEN_WELCOME_SHEET] ?: false,
            isDemoModeActive = prefs[PreferenceKeys.IS_DEMO_MODE_ACTIVE] ?: false,
            hasStashedRealLedger = prefs[PreferenceKeys.HAS_STASHED_REAL_LEDGER] ?: false,
            remindersEnabled = prefs[PreferenceKeys.REMINDERS_ENABLED] ?: false,
            ccReminderLeadDays = prefs[PreferenceKeys.CC_REMINDER_LEAD_DAYS] ?: 3,
            billReminderLeadDays = prefs[PreferenceKeys.BILL_REMINDER_LEAD_DAYS] ?: 1,
            reminderHourOfDay = prefs[PreferenceKeys.REMINDER_HOUR_OF_DAY] ?: 9,
            reminderMinute = prefs[PreferenceKeys.REMINDER_MINUTE] ?: 0,
            hideAmountsOnLockScreen = prefs[PreferenceKeys.HIDE_AMOUNTS_ON_LOCK_SCREEN] ?: false
        )
    }


}

internal fun parseSecondaryCurrencies(json: String): List<SecondaryCurrency> {
        return runCatching {
            val array = JSONArray(json)
            val list = mutableListOf<SecondaryCurrency>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val code = obj.optString("code").trim().uppercase()
                val rate = obj.optDouble("rate", 1.0)
                if (code.isNotEmpty() && rate > 0.0) {
                    list.add(SecondaryCurrency(code = code, exchangeRateToPrimary = rate))
                }
            }
            list.distinctBy { it.code }
        }.getOrDefault(emptyList())
    }


internal fun serializeSecondaryCurrencies(list: List<SecondaryCurrency>): String {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("code", item.code.trim().uppercase())
            obj.put("rate", item.exchangeRateToPrimary)
            array.put(obj)
        }
        return array.toString()
    }

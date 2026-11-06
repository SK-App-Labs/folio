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

class AutoBackupManager(
    private val context: Context,
    private val database: FolioDatabase,
    private val userPreferences: UserPreferencesDataSource,
    private val backupEngine: DatabaseBackupEngine
) {

    fun getLatestBackupFile(): File? {
        val internalDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "FolioBackups")
        return internalDir.listFiles { file ->
            file.isFile && file.name.endsWith(".zip", ignoreCase = true)
        }?.maxByOrNull { it.lastModified() }
    }

    suspend fun triggerSessionAutoBackupIfDirty(): Result<String?> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            val currentPrefs = userPreferences.observeUserPreferences().first()
            if (!currentPrefs.autoBackupEnabled) {
                return@runCatchingNonCancellation null
            }
            if (!SessionChangeTracker.canBackupNow()) {
                return@runCatchingNonCancellation null
            }

            val autoDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "FolioAutoBackups").apply {
                mkdirs()
            }
            val dateStr = LocalDate.now().toString()
            val timeStr = DateTimeFormatter.ofPattern("HHmmss").format(LocalTime.now())
            val fileName = "Folio_AutoBackup_${dateStr}_$timeStr.zip"
            val targetFile = File(autoDir, fileName)

            FileOutputStream(targetFile).use { output ->
                backupEngine.createBackupToStream(output)
            }

            // Mirror latest auto-backup to public Downloads/Folio folder for uninstall protection
            runCatching {
                StorageAccessUtils.openOutputStreamForLocation(context, "Folio_Latest_AutoBackup.zip").use { mirrorOut ->
                    FileInputStream(targetFile).use { it.copyTo(mirrorOut) }
                }
            }

            // Apply 30-Day Tiered Retention Pruning
            AutoBackupRetentionEngine.pruneDirectory(autoDir, currentPrefs.autoBackupRetention)

            // Update in-memory and DataStore recency indicator
            userPreferences.updateUserPreferences { it.copy(lastBackupEpochMillis = System.currentTimeMillis()) }
            SessionChangeTracker.recordBackupFinished()

            targetFile.absolutePath
        }
    }

    fun listAutoBackupFiles(): List<File> {
        val autoDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "FolioAutoBackups")
        if (!autoDir.exists() || !autoDir.isDirectory) return emptyList()
        return autoDir.listFiles { file ->
            file.isFile && file.name.startsWith("Folio_AutoBackup_") && file.name.endsWith(".zip", ignoreCase = true)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean): Result<Unit> =
        userPreferences.updateUserPreferences { it.copy(autoBackupEnabled = enabled) }

    suspend fun setAutoBackupRetention(retention: AutoBackupRetention): Result<Unit> =
        userPreferences.updateUserPreferences { it.copy(autoBackupRetention = retention) }

}

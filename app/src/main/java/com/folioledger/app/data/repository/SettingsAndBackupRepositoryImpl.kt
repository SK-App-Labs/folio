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

class SettingsAndBackupRepositoryImpl(
    private val context: Context,
    private val database: FolioDatabase
) : SettingsAndBackupRepository {

    private val userPreferencesDataSource = UserPreferencesDataSource(context)
    private val backupEngine = DatabaseBackupEngine(context, database, userPreferencesDataSource)
    private val autoBackupManager = AutoBackupManager(context, database, userPreferencesDataSource, backupEngine)
    private val csvEngine = CsvExportImportEngine(context, database, userPreferencesDataSource)
    private val pdfHelper = PdfStatementRepositoryHelper(context, database, userPreferencesDataSource)
    private val demoModeManager = com.folioledger.app.data.demo.DemoModeManager(context, database, userPreferencesDataSource, backupEngine)

    // User Preferences
    override fun observeUserPreferences(): Flow<UserPreferences> =
        userPreferencesDataSource.observeUserPreferences()

    override suspend fun updateUserPreferences(
        transform: (UserPreferences) -> UserPreferences
    ): Result<Unit> = userPreferencesDataSource.updateUserPreferences(transform)

    // CSV Import / Export
    override suspend fun exportTransactionsToCsv(
        destinationUriString: String,
        startEpochDay: Long?,
        endEpochDay: Long?,
        accountId: Long?
    ): Result<Int> = csvEngine.exportTransactionsToCsv(destinationUriString, startEpochDay, endEpochDay, accountId)

    override suspend fun exportCsvTemplate(destinationUriString: String): Result<File?> =
        csvEngine.exportCsvTemplate(destinationUriString)

    suspend fun exportTransactionsToCsvStream(
        outputStream: OutputStream,
        startEpochDay: Long? = null,
        endEpochDay: Long? = null,
        accountId: Long? = null
    ): Int = csvEngine.exportTransactionsToCsvStream(outputStream, startEpochDay, endEpochDay, accountId)

    override suspend fun previewTransactionsCsv(sourceUriString: String): Result<ImportCsvPreview> =
        csvEngine.previewTransactionsCsv(sourceUriString)

    override suspend fun importTransactionsFromCsv(
        sourceUriString: String,
        skipDuplicates: Boolean
    ): Result<ImportCsvResult> = csvEngine.importTransactionsFromCsv(sourceUriString, skipDuplicates)

    // Database Backup & Restore
    override suspend fun createPortableZipBackup(destinationUriString: String): Result<String> =
        backupEngine.createPortableZipBackup(destinationUriString)

    suspend fun createBackupToStream(outputStream: OutputStream): Unit =
        backupEngine.createBackupToStream(outputStream)

    override suspend fun restoreFromZipBackup(sourceUriString: String): Result<Unit> =
        backupEngine.restoreFromZipBackup(sourceUriString)

    suspend fun restoreBackupFromStream(inputStream: InputStream): Unit =
        backupEngine.restoreBackupFromStream(inputStream)

    override suspend fun clearAllData(): Result<Unit> =
        backupEngine.clearAllData()

    // Auto-Backup Retention
    override fun getLatestBackupFile(): File? =
        autoBackupManager.getLatestBackupFile()

    override fun getExportedFile(fileName: String): File? {
        val cleanFileName = fileName
            .substringAfterLast('/')
            .substringAfterLast('\\')
        val internalDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "FolioBackups")
        val file = File(internalDir, cleanFileName)
        return if (file.exists()) file else null
    }

    override suspend fun triggerSessionAutoBackupIfDirty(): Result<String?> =
        autoBackupManager.triggerSessionAutoBackupIfDirty()

    override fun listAutoBackupFiles(): List<File> =
        autoBackupManager.listAutoBackupFiles()

    override suspend fun setAutoBackupEnabled(enabled: Boolean): Result<Unit> =
        autoBackupManager.setAutoBackupEnabled(enabled)

    override suspend fun setAutoBackupRetention(retention: AutoBackupRetention): Result<Unit> =
        autoBackupManager.setAutoBackupRetention(retention)

    // PDF Statements
    override suspend fun generateAccountStatementPdf(
        accountId: Long,
        startEpochDay: Long,
        endEpochDay: Long,
        includeNotesAndTags: Boolean,
        includeRunningBalance: Boolean
    ): Result<PdfExportResult> = pdfHelper.generateAccountStatementPdf(
        accountId, startEpochDay, endEpochDay, includeNotesAndTags, includeRunningBalance
    )

    override suspend fun generateAnnualTaxStatementPdf(
        fiscalYearType: FiscalYearType,
        anchorYear: Int,
        customStartEpochDay: Long?,
        customEndEpochDay: Long?,
        selectedTaxTags: Set<String>
    ): Result<PdfExportResult> = pdfHelper.generateAnnualTaxStatementPdf(
        fiscalYearType, anchorYear, customStartEpochDay, customEndEpochDay, selectedTaxTags
    )

    // Demo Mode & Sandbox
    override suspend fun enterDemoMode(primaryCurrencyCode: String?): Result<Unit> =
        demoModeManager.enterDemoMode(primaryCurrencyCode)

    override suspend fun exitDemoMode(): Result<Unit> =
        demoModeManager.exitDemoMode()

    override fun isDemoStashAvailable(): Boolean =
        demoModeManager.isStashAvailable()
}

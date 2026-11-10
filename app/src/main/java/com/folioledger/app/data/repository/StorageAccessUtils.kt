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

internal object StorageAccessUtils {
    fun openOutputStreamForLocation(context: Context, destinationUriString: String): OutputStream {
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

    fun openInputStreamForLocation(context: Context, sourceUriString: String): InputStream {
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

}

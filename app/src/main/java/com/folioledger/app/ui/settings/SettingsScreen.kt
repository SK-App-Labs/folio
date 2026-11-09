// File: app/src/main/java/com/folioledger/app/ui/settings/SettingsScreen.kt
package com.folioledger.app.ui.settings

import com.folioledger.app.BuildConfig

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import com.folioledger.app.R
import com.folioledger.app.domain.model.AutoBackupRetention
import com.folioledger.app.domain.model.ImportCsvPreview
import com.folioledger.app.domain.model.ImportCsvResult
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupporterTier
import com.folioledger.app.ui.components.parseHexColorSafely
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material.icons.filled.Language
import com.folioledger.app.ui.settings.sheets.AppLanguageBottomSheet
import com.folioledger.app.ui.settings.components.AppearanceSecuritySettingsCard
import com.folioledger.app.ui.settings.components.CommunityHelpSettingsCard
import com.folioledger.app.ui.settings.components.DataBackupsSettingsCard
import com.folioledger.app.ui.settings.components.SettingsFooter
import android.Manifest
import android.os.Build
import com.folioledger.app.data.notification.DueRemindersScheduler
import com.folioledger.app.data.notification.FolioNotificationManager
import com.folioledger.app.ui.settings.components.RemindersSettingsCard
import com.folioledger.app.ui.settings.components.SettingsGroupCard
import com.folioledger.app.ui.settings.dialogs.BillLeadDaysDialog
import com.folioledger.app.ui.settings.dialogs.CcLeadDaysDialog
import com.folioledger.app.ui.settings.dialogs.ReminderTimePickerDialog
import com.folioledger.app.ui.settings.components.SettingsNavigationRow
import com.folioledger.app.ui.settings.components.SettingsSwitchRow
import com.folioledger.app.ui.settings.components.TaxonomySettingsCard
import com.folioledger.app.ui.settings.components.openFolioDownloadsFolder
import com.folioledger.app.ui.settings.components.shareBackupFile
import com.folioledger.app.ui.settings.dialogs.ClearAllDataConfirmationDialog
import com.folioledger.app.ui.settings.dialogs.FileExportSuccessDialog
import com.folioledger.app.ui.settings.dialogs.ImportCsvPreviewDialog
import com.folioledger.app.ui.settings.dialogs.ImportCsvResultDialog
import com.folioledger.app.ui.settings.dialogs.MonthStartDayPickerDialog
import com.folioledger.app.ui.settings.dialogs.RestoreConfirmationDialog
import com.folioledger.app.ui.settings.sheets.FolioLocaleManager
import com.folioledger.app.ui.settings.sheets.AppearanceBottomSheet
import com.folioledger.app.ui.settings.sheets.resolveCurrentLanguageDisplayName
import com.folioledger.app.ui.settings.sheets.AutoBackupBottomSheet
import com.folioledger.app.ui.settings.sheets.CategoryEditorDialog
import com.folioledger.app.ui.settings.sheets.CategoryManagerBottomSheet
import com.folioledger.app.ui.settings.sheets.FeedbackBottomSheet
import com.folioledger.app.ui.settings.sheets.PdfReportsBottomSheet
import com.folioledger.app.ui.settings.sheets.RecurringManagerBottomSheet
import com.folioledger.app.ui.settings.sheets.RegionalFormatBottomSheet
import com.folioledger.app.ui.settings.sheets.SecondaryCurrenciesBottomSheet
import com.folioledger.app.ui.settings.sheets.SpreadsheetsBottomSheet
import com.folioledger.app.ui.settings.sheets.TagsAndBookmarksBottomSheet
import com.folioledger.app.ui.settings.sheets.TransferAndManualBackupBottomSheet
import com.folioledger.app.ui.theme.FolioAccentPalette
import com.folioledger.app.ui.theme.FolioThemeMode
import com.folioledger.app.ui.iconpack.FolioIconPackTheme
import com.folioledger.app.ui.settings.sheets.IconPackBottomSheet
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.folioledger.app.ui.settings.sheets.formatBackupRelativeTime
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var isAppearanceSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isIconPackSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isLanguageSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isRegionalFormatSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isSecondaryCurrenciesSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isAutoBackupSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isTransferBackupSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isSpreadsheetsSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isTagsAndBookmarksSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showDemoModeConfirmDialog by rememberSaveable { mutableStateOf(false) }

    var showMonthStartDayDialog by rememberSaveable { mutableStateOf(false) }
    var showCcLeadDaysDialog by rememberSaveable { mutableStateOf(false) }
    var showBillLeadDaysDialog by rememberSaveable { mutableStateOf(false) }
    var showReminderTimeDialog by rememberSaveable { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setRemindersEnabled(true)
            DueRemindersScheduler.scheduleDailyReminders(
                context = context,
                hourOfDay = uiState.userPreferences.reminderHourOfDay,
                minute = uiState.userPreferences.reminderMinute
            )
        } else {
            viewModel.setRemindersEnabled(false)
            Toast.makeText(context, "Notification permission is required for reminders", Toast.LENGTH_SHORT).show()
        }
    }

    val csvDocumentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.loadCsvPreview(uri.toString())
        }
    }

    val zipDocumentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.requestRestoreConfirmation(uri.toString())
        }
    }
    var showClearAllDataDialog by rememberSaveable { mutableStateOf(false) }
    var showUserGuideSheet by rememberSaveable { mutableStateOf(false) }
    var showFeedbackSheet by rememberSaveable { mutableStateOf(false) }
    var showTipJarSheet by rememberSaveable { mutableStateOf(false) }

    val prefs = uiState.userPreferences

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (!message.isNullOrBlank()) {
            val fileToShare = uiState.lastCompletedFileForShare
            if (fileToShare != null) {
                val result = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = "Share",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    shareBackupFile(context, fileToShare)
                }
                viewModel.clearLastCompletedFileForShare()
            } else {
                snackbarHostState.showSnackbar(message)
            }
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding())
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = contentPadding.calculateTopPadding() + scaffoldPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding()
                )
        ) {
            // Header Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings & Data Management",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (uiState.isBusyWithFileOperation) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            val isNotBusy = !uiState.isBusyWithFileOperation
            val semanticColors = LocalFolioSemanticColors.current

            val currentTag = FolioLocaleManager.getCurrentLanguageTag(context)
            val currentLanguageDisplayName = resolveCurrentLanguageDisplayName(currentTag)

            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    TaxonomySettingsCard(
                        prefs = prefs,
                        uiState = uiState,
                        isNotBusy = isNotBusy,
                        onOpenCategories = { viewModel.setCategoryManagerOpen(true) },
                        onToggleSubcategories = viewModel::setSubcategoriesEnabled,
                        onOpenTagsAndBookmarks = { isTagsAndBookmarksSheetOpen = true },
                        onOpenRecurringManager = { viewModel.setRecurringManagerOpen(true) },
                        onOpenRegionalFormat = { isRegionalFormatSheetOpen = true },
                        onOpenSecondaryCurrencies = { isSecondaryCurrenciesSheetOpen = true },
                        onOpenMonthStartDayDialog = { showMonthStartDayDialog = true },
                        onToggleCarryOver = viewModel::setCarryOverEnabled
                    )
                }

                item {
                    AppearanceSecuritySettingsCard(
                        prefs = prefs,
                        isNotBusy = isNotBusy,
                        currentLanguageDisplayName = currentLanguageDisplayName,
                        onOpenAppearanceSheet = { isAppearanceSheetOpen = true },
                        onOpenIconPackSheet = { isIconPackSheetOpen = true },
                        onOpenLanguageSheet = { isLanguageSheetOpen = true },
                        onToggleBiometricLock = viewModel::setBiometricLockEnabled
                    )
                }

                item {
                    RemindersSettingsCard(
                        prefs = prefs,
                        isNotBusy = isNotBusy,
                        onToggleReminders = { enabled ->
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !FolioNotificationManager.hasNotificationPermission(context)
                                ) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setRemindersEnabled(true)
                                    DueRemindersScheduler.scheduleDailyReminders(
                                        context = context,
                                        hourOfDay = prefs.reminderHourOfDay,
                                        minute = prefs.reminderMinute
                                    )
                                }
                            } else {
                                viewModel.setRemindersEnabled(false)
                                DueRemindersScheduler.cancelDailyReminders(context)
                            }
                        },
                        onOpenCcLeadDaysDialog = { showCcLeadDaysDialog = true },
                        onOpenBillLeadDaysDialog = { showBillLeadDaysDialog = true },
                        onOpenReminderTimeDialog = { showReminderTimeDialog = true },
                        onToggleHideAmounts = viewModel::setHideAmountsOnLockScreen
                    )
                }

                item {
                    DataBackupsSettingsCard(
                        prefs = prefs,
                        isNotBusy = isNotBusy,
                        onOpenAutoBackupSheet = { isAutoBackupSheetOpen = true },
                        onOpenTransferBackupSheet = { isTransferBackupSheetOpen = true },
                        onOpenPdfReportsSheet = viewModel::openPdfReportsSheet,
                        onOpenSpreadsheetsSheet = { isSpreadsheetsSheetOpen = true },
                        onOpenClearAllDataDialog = { showClearAllDataDialog = true }
                    )
                }

                item {
                    CommunityHelpSettingsCard(
                        prefs = prefs,
                        isNotBusy = isNotBusy,
                        onOpenUserGuideSheet = { showUserGuideSheet = true },
                        onOpenFeedbackSheet = { showFeedbackSheet = true },
                        onRateApp = {
                            val pkgName = context.packageName
                            val marketIntent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("market://details?id=$pkgName")
                            ).apply {
                                addFlags(
                                    android.content.Intent.FLAG_ACTIVITY_NO_HISTORY or
                                        android.content.Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                                        android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                                )
                            }
                            try {
                                context.startActivity(marketIntent)
                            } catch (e: android.content.ActivityNotFoundException) {
                                val webIntent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://play.google.com/store/apps/details?id=$pkgName")
                                )
                                try {
                                    context.startActivity(webIntent)
                                } catch (_: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        context.getString(R.string.setting_rate_app_not_ready),
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        onOpenTipJarSheet = { showTipJarSheet = true },
                        onToggleDemoModeClick = { showDemoModeConfirmDialog = true }
                    )
                }

                item {
                    SettingsFooter(prefs = prefs)
                }
            }
        }
    }

    // 3 New Bottom Sheets
    if (isIconPackSheetOpen) {
        IconPackBottomSheet(
            currentThemeKey = prefs.iconPackTheme,
            onSelectTheme = { selected ->
                viewModel.setIconPackTheme(selected.key)
            },
            onDismiss = { isIconPackSheetOpen = false }
        )
    }

    if (isAppearanceSheetOpen) {
        AppearanceBottomSheet(
            prefs = prefs,
            onSelectThemeModeString = viewModel::setThemeMode,
            onSelectAccentPaletteString = viewModel::setAccentPalette,
            onToggleDynamicColor = viewModel::setDynamicColorEnabled,
            onDismiss = { isAppearanceSheetOpen = false }
        )
    }

    if (isLanguageSheetOpen) {
        AppLanguageBottomSheet(
            onDismissRequest = { isLanguageSheetOpen = false }
        )
    }

    if (isRegionalFormatSheetOpen) {
        RegionalFormatBottomSheet(
            prefs = prefs,
            onSelectCurrency = viewModel::setPrimaryCurrency,
            onSelectGroupingStyle = viewModel::setNumberGrouping,
            onDismiss = { isRegionalFormatSheetOpen = false }
        )
    }

    if (isSecondaryCurrenciesSheetOpen) {
        SecondaryCurrenciesBottomSheet(
            prefs = prefs,
            onAddOrUpdateSecondaryCurrency = viewModel::addOrUpdateSecondaryCurrency,
            onRemoveSecondaryCurrency = viewModel::removeSecondaryCurrency,
            onDismiss = { isSecondaryCurrenciesSheetOpen = false }
        )
    }

    LaunchedEffect(isAutoBackupSheetOpen, isTransferBackupSheetOpen) {
        if (isAutoBackupSheetOpen || isTransferBackupSheetOpen) {
            viewModel.refreshAutoBackupFiles()
        }
    }

    if (isAutoBackupSheetOpen) {
        AutoBackupBottomSheet(
            autoBackupEnabled = prefs.autoBackupEnabled,
            autoBackupRetention = prefs.autoBackupRetention,
            autoBackupFiles = uiState.autoBackupFiles,
            onToggleAutoBackup = viewModel::setAutoBackupEnabled,
            onSelectRetention = viewModel::setAutoBackupRetention,
            onRestoreAutoBackupFile = { file ->
                viewModel.requestRestoreConfirmation(file.absolutePath)
            },
            onShareBackupFile = { file ->
                shareBackupFile(context, file)
            },
            onDismiss = { isAutoBackupSheetOpen = false }
        )
    }

    if (isTransferBackupSheetOpen) {
        TransferAndManualBackupBottomSheet(
            lastBackupEpochMillis = prefs.lastBackupEpochMillis,
            onCreateBackup = {
                viewModel.createPortableZipBackup(
                    "Folio_Backup_${LocalDate.now()}_${System.currentTimeMillis() / 1000}.zip"
                )
            },
            onRestoreLatestBackup = {
                viewModel.requestRestoreConfirmation("LATEST_BACKUP")
            },
            onSelectBackupFile = {
                zipDocumentPicker.launch(arrayOf("application/zip", "application/x-zip-compressed", "*/*"))
            },
            onShareLatestBackup = {
                viewModel.getLatestBackupFile()?.let { file ->
                    shareBackupFile(context, file)
                }
            },
            onDismiss = { isTransferBackupSheetOpen = false }
        )
    }

    if (isSpreadsheetsSheetOpen) {
        SpreadsheetsBottomSheet(
            onExportCsv = {
                viewModel.exportAllTransactionsToCsv(
                    "Folio_Ledger_Export_${LocalDate.now()}.csv"
                )
            },
            onImportCsv = {
                csvDocumentPicker.launch(arrayOf("text/csv", "text/comma-separated-values", "application/vnd.ms-excel", "*/*"))
            },
            onDownloadTemplate = {
                viewModel.downloadCsvTemplate("Folio_Import_Template.csv")
            },
            onDismiss = { isSpreadsheetsSheetOpen = false }
        )
    }

    if (uiState.isPdfReportsSheetOpen) {
        PdfReportsBottomSheet(
            prefs = prefs,
            accounts = uiState.accounts,
            isGeneratingPdf = uiState.isGeneratingPdf,
            lastGeneratedPdfResult = uiState.lastGeneratedPdfResult,
            onGenerateAccountStatement = viewModel::generateAccountStatementPdf,
            onGenerateAnnualTaxStatement = viewModel::generateAnnualTaxStatementPdf,
            onClearLastResult = viewModel::clearLastGeneratedPdfResult,
            onDismiss = viewModel::closePdfReportsSheet
        )
    }

    // Dialogs & Secondary Sheets
    uiState.csvPreview?.let { preview ->
        ImportCsvPreviewDialog(
            preview = preview,
            onConfirmImport = { skipDuplicates ->
                viewModel.confirmCsvImport(skipDuplicates)
            },
            onDismiss = viewModel::dismissCsvPreview
        )
    }

    uiState.csvImportResult?.let { result ->
        ImportCsvResultDialog(
            result = result,
            onDismiss = viewModel::dismissCsvImportResult
        )
    }

    uiState.lastExportedFileSuccess?.let { exportSuccess ->
        FileExportSuccessDialog(
            model = exportSuccess,
            onShowInFiles = {
                openFolioDownloadsFolder(context)
                viewModel.clearLastExportedFileSuccess()
            },
            onShare = {
                exportSuccess.file?.let { file ->
                    shareBackupFile(context, file)
                    viewModel.clearLastExportedFileSuccess()
                } ?: Toast.makeText(context, "File sharing not available for this export", Toast.LENGTH_SHORT).show()
            },
            onDismiss = viewModel::clearLastExportedFileSuccess
        )
    }

    if (showClearAllDataDialog) {
        ClearAllDataConfirmationDialog(
            onConfirmClear = {
                showClearAllDataDialog = false
                viewModel.clearAllData()
            },
            onDismiss = { showClearAllDataDialog = false }
        )
    }

    if (showDemoModeConfirmDialog) {
        val isDemoActive = prefs.isDemoModeActive
        val hasStash = prefs.hasStashedRealLedger
        AlertDialog(
            onDismissRequest = { showDemoModeConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isDemoActive) {
                        if (hasStash) "Exit Demo Sandbox?" else "Reset Demo Data?"
                    } else {
                        "Launch Interactive Demo Mode?"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isDemoActive) {
                        if (hasStash) {
                            "Your real ledger will be restored from your private internal stash. Any transactions or edits created during this demo session will be discarded."
                        } else {
                            "This will wipe the sample demo data and reset your ledger so you can start tracking your personal finances."
                        }
                    } else {
                        "Folio will safely stash your current ledger in protected storage, pause auto-backups, and load a rich sample dataset with accounts, charts, credit card float tracking, and passbooks. You can restore your real ledger at any time."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDemoModeConfirmDialog = false
                        if (isDemoActive) {
                            viewModel.exitDemoMode()
                        } else {
                            viewModel.enterDemoMode()
                        }
                    }
                ) {
                    Text(
                        if (isDemoActive) {
                            if (hasStash) "Restore Real Ledger" else "Wipe & Start Fresh"
                        } else {
                            "Enter Demo Sandbox"
                        }
                    )
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDemoModeConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMonthStartDayDialog) {
        MonthStartDayPickerDialog(
            selectedDay = uiState.userPreferences.monthStartDay,
            onSelectDay = { day ->
                viewModel.setMonthStartDay(day)
                showMonthStartDayDialog = false
            },
            onDismiss = { showMonthStartDayDialog = false }
        )
    }

    if (showCcLeadDaysDialog) {
        CcLeadDaysDialog(
            selectedDays = prefs.ccReminderLeadDays,
            onSelectDays = viewModel::setCcReminderLeadDays,
            onDismiss = { showCcLeadDaysDialog = false }
        )
    }

    if (showBillLeadDaysDialog) {
        BillLeadDaysDialog(
            selectedDays = prefs.billReminderLeadDays,
            onSelectDays = viewModel::setBillReminderLeadDays,
            onDismiss = { showBillLeadDaysDialog = false }
        )
    }

    if (showReminderTimeDialog) {
        ReminderTimePickerDialog(
            selectedHour = prefs.reminderHourOfDay,
            selectedMinute = prefs.reminderMinute,
            onSelectTime = { hour, min ->
                viewModel.setReminderTime(hour, min)
                if (prefs.remindersEnabled) {
                    DueRemindersScheduler.scheduleDailyReminders(context, hour, min)
                }
            },
            onDismiss = { showReminderTimeDialog = false }
        )
    }

    if (uiState.isCategoryManagerOpen) {
        CategoryManagerBottomSheet(
            uiState = uiState,
            onSelectCategoryType = viewModel::selectCategoryManagerType,
            onAddMainCategory = {
                viewModel.openCreateMainCategoryDialog(uiState.selectedCategoryManagerType)
            },
            onAddSubcategory = viewModel::openCreateSubcategoryDialog,
            onEditCategory = viewModel::openEditCategoryDialog,
            onDeleteCategory = { catId -> viewModel.deleteCategory(catId, null) },
            onDismiss = { viewModel.setCategoryManagerOpen(false) }
        )
    }

    uiState.categoryEditorState?.let { categoryEditor ->
        CategoryEditorDialog(
            state = categoryEditor,
            onNameChange = viewModel::onCategoryNameInputChanged,
            onUpdateState = viewModel::updateCategoryEditorState,
            onSave = viewModel::saveCategoryFromDialog,
            onDelete = {
                if ((categoryEditor.isEditing) && (categoryEditor.categoryId > 0L)) {
                    viewModel.deleteCategory(categoryEditor.categoryId, null)
                }
            },
            onDismiss = viewModel::dismissCategoryEditorDialog
        )
    }

    if (isTagsAndBookmarksSheetOpen || uiState.isTagManagerOpen || uiState.isBookmarkManagerOpen) {
        TagsAndBookmarksBottomSheet(
            uiState = uiState,
            onCreateTag = viewModel::createTag,
            onRenameTag = viewModel::renameTag,
            onDeleteTag = viewModel::deleteTag,
            onDeleteBookmark = viewModel::deleteBookmark,
            onDismiss = {
                isTagsAndBookmarksSheetOpen = false
                viewModel.setTagManagerOpen(false)
                viewModel.setBookmarkManagerOpen(false)
            }
        )
    }

    if (uiState.isRecurringManagerOpen) {
        RecurringManagerBottomSheet(
            uiState = uiState,
            onToggleRecurringActive = viewModel::toggleRecurringRuleActive,
            onDeleteRecurringRule = viewModel::deleteRecurringRule,
            onUpdateRecurringRule = viewModel::upsertRecurringRule,
            onDeleteInstallmentPlan = viewModel::deleteInstallmentPlan,
            onDismiss = { viewModel.setRecurringManagerOpen(false) }
        )
    }

    if (uiState.pendingRestoreUriString != null) {
        RestoreConfirmationDialog(
            sourceUriString = uiState.pendingRestoreUriString,
            onConfirmRestore = viewModel::confirmRestoreFromZipBackup,
            onCancel = viewModel::cancelPendingRestore
        )
    }

    if (showUserGuideSheet) {
        UserGuideBottomSheet(
            onDismiss = { showUserGuideSheet = false }
        )
    }

    if (showFeedbackSheet) {
        FeedbackBottomSheet(
            onDismiss = { showFeedbackSheet = false }
        )
    }

    if (showTipJarSheet) {
        TipJarBottomSheet(
            currencySpec = uiState.currencySpec,
            isSupporter = prefs.isSupporter,
            supporterTier = prefs.supporterTier,
            onTipSelected = { _, tierTitle ->
                viewModel.unlockSupporterBadge(tierTitle)
            },
            onDismiss = { showTipJarSheet = false }
        )
    }
}


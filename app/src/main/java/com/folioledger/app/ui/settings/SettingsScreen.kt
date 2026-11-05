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
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sync
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
import android.content.Context
import android.content.Intent
import android.net.Uri
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

    var showMonthStartDayDialog by rememberSaveable { mutableStateOf(false) }

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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                // Group 1: LEDGER & TAXONOMY
                item {
                    SettingsGroupCard(title = stringResource(R.string.settings_group_taxonomy)) {
                        SettingsNavigationRow(
                            icon = Icons.Default.Category,
                            title = stringResource(R.string.setting_categories),
                            valueText = "${uiState.expenseCategories.size + uiState.incomeCategories.size} active",
                            enabled = isNotBusy,
                            onClick = { viewModel.setCategoryManagerOpen(true) }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsSwitchRow(
                            icon = Icons.Default.Category,
                            title = "Enable Subcategories",
                            checked = prefs.subcategoriesEnabled,
                            enabled = isNotBusy,
                            onCheckedChange = { viewModel.setSubcategoriesEnabled(it) }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsNavigationRow(
                            icon = Icons.AutoMirrored.Filled.Label,
                            title = stringResource(R.string.setting_tags_bookmarks),
                            valueText = "${uiState.tags.size} tags • ${uiState.bookmarks.size} saved",
                            enabled = isNotBusy,
                            onClick = { isTagsAndBookmarksSheetOpen = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsNavigationRow(
                            icon = Icons.Default.Autorenew,
                            title = "Recurring & Subscriptions",
                            valueText = "${uiState.activeRecurringCount} active • ${uiState.formattedMonthlyBurden}/mo",
                            enabled = isNotBusy,
                            onClick = { viewModel.setRecurringManagerOpen(true) }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsNavigationRow(
                            icon = Icons.Default.Payments,
                            title = stringResource(R.string.setting_currency_format),
                            valueText = "${prefs.primaryCurrencyCode} • ${if (prefs.numberGrouping == NumberGroupingSystem.INDIAN) "12,34,567" else "1,234,567"}",
                            enabled = isNotBusy,
                            onClick = { isRegionalFormatSheetOpen = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsNavigationRow(
                            icon = Icons.Default.Public,
                            title = stringResource(R.string.setting_secondary_currencies),
                            valueText = when {
                                prefs.secondaryCurrencies.isEmpty() -> "None configured"
                                prefs.secondaryCurrencies.size == 1 -> {
                                    val sec = prefs.secondaryCurrencies.first()
                                    "${sec.code} (1 = ${sec.exchangeRateToPrimary})"
                                }
                                else -> "${prefs.secondaryCurrencies.size} active (${prefs.secondaryCurrencies.joinToString(", ") { it.code }})"
                            },
                            enabled = isNotBusy,
                            onClick = { isSecondaryCurrenciesSheetOpen = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsNavigationRow(
                            icon = Icons.Default.CalendarMonth,
                            title = "Financial Month Start Day",
                            valueText = if (prefs.monthStartDay == 1) "1st of month" else "Day ${prefs.monthStartDay}",
                            enabled = isNotBusy,
                            onClick = { showMonthStartDayDialog = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsSwitchRow(
                            icon = Icons.Default.Sync,
                            title = stringResource(R.string.ledger_banner_carry_over),
                            checked = prefs.carryOverEnabled,
                            enabled = isNotBusy,
                            onCheckedChange = { viewModel.setCarryOverEnabled(it) }
                        )
                    }
                }

                // Group 2: APPEARANCE & SECURITY
                item {
                    SettingsGroupCard(title = stringResource(R.string.settings_group_appearance)) {
                        val themeModeObj = FolioThemeMode.fromKey(prefs.themeMode)
                        val accentPaletteObj = FolioAccentPalette.fromKey(prefs.accentPalette)
                        SettingsNavigationRow(
                            icon = Icons.Default.Palette,
                            title = stringResource(R.string.setting_appearance_theme),
                            valueText = themeModeObj.displayName,
                            valueColorDot = accentPaletteObj.previewColor,
                            enabled = isNotBusy,
                            onClick = { isAppearanceSheetOpen = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        val iconPackThemeObj = FolioIconPackTheme.fromKey(prefs.iconPackTheme)
                        SettingsNavigationRow(
                            icon = Icons.Default.Category,
                            title = "Icon Style",
                            valueText = iconPackThemeObj.displayName,
                            enabled = isNotBusy,
                            onClick = { isIconPackSheetOpen = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        val currentTag = FolioLocaleManager.getCurrentLanguageTag(context)
                        SettingsNavigationRow(
                            icon = Icons.Default.Language,
                            title = stringResource(R.string.setting_app_language),
                            valueText = resolveCurrentLanguageDisplayName(currentTag),
                            enabled = isNotBusy,
                            onClick = { isLanguageSheetOpen = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsSwitchRow(
                            icon = Icons.Default.Fingerprint,
                            title = stringResource(R.string.setting_app_lock),
                            checked = prefs.biometricLockEnabled,
                            enabled = isNotBusy,
                            onCheckedChange = { viewModel.setBiometricLockEnabled(it) }
                        )
                    }
                }

                // Group 3: DATA & BACKUPS
                item {
                    SettingsGroupCard(title = stringResource(R.string.settings_group_data)) {
                        // 1. Automated Rolling Backups
                        val autoBackupValue = if (prefs.autoBackupEnabled) {
                            when (prefs.autoBackupRetention) {
                                AutoBackupRetention.TEN_SESSIONS -> "10 Sessions"
                                AutoBackupRetention.THIRTY_DAYS -> "30 Days (Tiered)"
                                AutoBackupRetention.NINETY_DAYS -> "90 Days"
                            }
                        } else "Disabled"

                        SettingsNavigationRow(
                            icon = Icons.Default.History,
                            title = stringResource(R.string.setting_auto_backup),
                            valueText = autoBackupValue,
                            valueColorDot = if (prefs.autoBackupEnabled) semanticColors.incomeEmerald else semanticColors.warningAmber,
                            enabled = isNotBusy,
                            onClick = { isAutoBackupSheetOpen = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // 2. Transfer & Manual Backup
                        val backupStatusText = formatBackupRelativeTime(prefs.lastBackupEpochMillis)
                        SettingsNavigationRow(
                            icon = Icons.Default.Backup,
                            title = stringResource(R.string.setting_phone_migration),
                            valueText = if (prefs.lastBackupEpochMillis != null) backupStatusText else "Downloads/Folio",
                            valueColorDot = if (prefs.lastBackupEpochMillis != null) semanticColors.incomeEmerald else null,
                            enabled = isNotBusy,
                            onClick = { isTransferBackupSheetOpen = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // 3. PDF Statements & Tax Reports
                        SettingsNavigationRow(
                            icon = Icons.Default.PictureAsPdf,
                            title = stringResource(R.string.setting_pdf_statements),
                            valueText = "Account & Tax Reports",
                            enabled = isNotBusy,
                            onClick = { viewModel.openPdfReportsSheet() }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // 4. Spreadsheets & CSV
                        SettingsNavigationRow(
                            icon = Icons.Default.TableChart,
                            title = stringResource(R.string.setting_spreadsheets),
                            valueText = "Export / Import",
                            enabled = isNotBusy,
                            onClick = { isSpreadsheetsSheetOpen = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // 4. Reset All Ledger Data (Danger Zone)
                        SettingsNavigationRow(
                            icon = Icons.Default.DeleteForever,
                            title = stringResource(R.string.setting_danger_reset),
                            valueText = null,
                            titleColor = MaterialTheme.colorScheme.error,
                            iconTint = MaterialTheme.colorScheme.error,
                            enabled = isNotBusy,
                            onClick = { showClearAllDataDialog = true }
                        )
                    }
                }

                // Group 4: HELP & COMMUNITY
                item {
                    SettingsGroupCard(title = stringResource(R.string.settings_group_community)) {
                        SettingsNavigationRow(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = stringResource(R.string.setting_user_guide),
                            valueText = null,
                            enabled = isNotBusy,
                            onClick = { showUserGuideSheet = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsNavigationRow(
                            icon = Icons.Default.Feedback,
                            title = "Send Feedback & Feature Ideas",
                            valueText = null,
                            enabled = isNotBusy,
                            onClick = { showFeedbackSheet = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        val activeSupporterTier = SupporterTier.fromSkuOrTitle(prefs.supporterTier)
                            ?: if (prefs.isSupporter) SupporterTier.GENEROUS_SUPPORTER else null

                        SettingsNavigationRow(
                            icon = Icons.Default.LocalCafe,
                            title = stringResource(R.string.setting_tip_jar),
                            valueText = activeSupporterTier?.badgeLabel ?: (if (prefs.isSupporter) "★ Unlocked" else null),
                            valueColorDot = activeSupporterTier?.let { parseHexColorSafely(it.accentHex) } ?: (if (prefs.isSupporter) semanticColors.warningAmber else null),
                            enabled = isNotBusy,
                            onClick = { showTipJarSheet = true }
                        )
                    }
                }

                // Minimalist Footer
                item {
                    val activeSupporterTier = SupporterTier.fromSkuOrTitle(prefs.supporterTier)
                        ?: if (prefs.isSupporter) SupporterTier.GENEROUS_SUPPORTER else null

                    val footerText = activeSupporterTier?.let {
                        "Folio Ledger v${BuildConfig.VERSION_NAME} • ${it.footerEdition}"
                    } ?: if (prefs.isSupporter) {
                        "Folio Ledger v${BuildConfig.VERSION_NAME} • ★ Supporter Edition"
                    } else {
                        "Folio Ledger v${BuildConfig.VERSION_NAME} • 100% Offline SQLite"
                    }

                    val footerColor = activeSupporterTier?.let { parseHexColorSafely(it.accentHex) }
                        ?: if (prefs.isSupporter) semanticColors.warningAmber else MaterialTheme.colorScheme.onSurfaceVariant

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 24.dp)
                    ) {
                        Text(
                            text = footerText,
                            style = MaterialTheme.typography.labelSmall,
                            color = footerColor,
                            fontWeight = if (prefs.isSupporter) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.heightIn(min = 2.dp))
                        Text(
                            text = "Zero Cloud • Zero Ads • Local Device Privacy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
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

    if (showClearAllDataDialog) {
        ClearAllDataConfirmationDialog(
            onConfirmClear = {
                showClearAllDataDialog = false
                viewModel.clearAllData()
            },
            onDismiss = { showClearAllDataDialog = false }
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

@Composable
private fun MonthStartDayPickerDialog(
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Financial Month Start Day (1–31)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Choose the day of the month your financial cycle begins (1–31). Months with fewer days automatically clamp to the last day of that month.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..31).forEach { day ->
                        FilterChip(
                            selected = selectedDay == day,
                            onClick = { onSelectDay(day) },
                            label = {
                                Text(
                                    text = day.toString(),
                                    style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                                    fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun shareBackupFile(context: Context, file: File) {
    runCatching {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (file.name.endsWith(".zip", ignoreCase = true)) "application/zip" else "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Folio Backup"))
    }
}

@Composable
private fun RestoreConfirmationDialog(
    sourceUriString: String?,
    onConfirmRestore: () -> Unit,
    onCancel: () -> Unit
) {
    val isLocalLatest = sourceUriString == null || sourceUriString == "LATEST_BACKUP"
    val sourceDescription = if (isLocalLatest) {
        "the latest backup from Downloads/Folio"
    } else {
        "the selected backup file"
    }

    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = LocalFolioSemanticColors.current.warningAmber
            )
        },
        title = {
            Text(
                text = "Restore from Portable ZIP Backup?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Restoring $sourceDescription will replace your current local accounts, transactions, categories, and budgets.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "A safety rollback snapshot will be automatically saved before restoring.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmRestore,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalFolioSemanticColors.current.expenseCoral,
                    contentColor = Color.White
                )
            ) {
                Text("Replace & Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ClearAllDataConfirmationDialog(
    onConfirmClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = LocalFolioSemanticColors.current.expenseCoral
            )
        },
        title = {
            Text(
                text = "Reset & Clear All Ledger Data?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "This action will permanently delete all your accounts, transactions, custom categories, tags, budgets, and settings. This action cannot be undone unless you have a backup. Are you sure you want to proceed?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmClear,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalFolioSemanticColors.current.expenseCoral,
                    contentColor = Color.White
                )
            ) {
                Text("Clear All Data")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SettingsGroupCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 16.dp)
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    valueText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleColor: Color? = null,
    iconTint: Color? = null,
    valueColorDot: Color? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconTint ?: MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor ?: MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (valueColorDot != null) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color = valueColorDot, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        if (!valueText.isNullOrEmpty()) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun ImportCsvPreviewDialog(
    preview: ImportCsvPreview,
    onConfirmImport: (skipDuplicates: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var skipDuplicates by rememberSaveable { mutableStateOf(true) }
    var showDuplicateDetails by rememberSaveable { mutableStateOf(false) }

    val totalToImport = if (skipDuplicates) {
        preview.validTransactionCount
    } else {
        preview.validTransactionCount + preview.duplicateCount
    }

    val hasMetadataChanges = preview.accountsWithOpeningBalance.isNotEmpty() ||
        preview.newAccountsToCreate.isNotEmpty() ||
        preview.newCategoriesToCreate.isNotEmpty()

    val canConfirm = totalToImport > 0 || hasMetadataChanges

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Import CSV Preview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Folio analyzed your spreadsheet and found:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "• Total Rows: ${preview.totalRowsRead}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "• Valid New Transactions: ${preview.validTransactionCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (preview.duplicateCount > 0) {
                            Text(
                                text = "• Duplicates Detected: ${preview.duplicateCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (preview.accountsWithOpeningBalance.isNotEmpty()) {
                            Text(
                                text = "• Opening Balance Anchors (${preview.accountsWithOpeningBalance.size}): ${preview.accountsWithOpeningBalance.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (preview.newAccountsToCreate.isNotEmpty()) {
                            Text(
                                text = "• New Accounts to create (${preview.newAccountsToCreate.size}): ${preview.newAccountsToCreate.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (preview.newCategoriesToCreate.isNotEmpty()) {
                            Text(
                                text = "• New Categories to create (${preview.newCategoriesToCreate.size}): ${preview.newCategoriesToCreate.take(5).joinToString(", ")}${if (preview.newCategoriesToCreate.size > 5) "..." else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Duplicate Handling Section
                if (preview.duplicateCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Duplicate Handling",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "${preview.duplicateCount} transaction${if (preview.duplicateCount > 1) "s" else ""} match existing records in your ledger.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Option 1: Exclude Duplicates (Recommended)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (skipDuplicates) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                },
                                border = BorderStroke(
                                    width = if (skipDuplicates) 1.5.dp else 1.dp,
                                    color = if (skipDuplicates) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { skipDuplicates = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = skipDuplicates,
                                        onClick = { skipDuplicates = true },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Exclude Duplicates",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "Recommended",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Skips the ${preview.duplicateCount} duplicate${if (preview.duplicateCount > 1) "s" else ""} to avoid double-counting overlapping statements.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "→ Will import ${preview.validTransactionCount} new transaction${if (preview.validTransactionCount != 1) "s" else ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Option 2: Include All (Allow Duplicates)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!skipDuplicates) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                },
                                border = BorderStroke(
                                    width = if (!skipDuplicates) 1.5.dp else 1.dp,
                                    color = if (!skipDuplicates) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { skipDuplicates = false }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = !skipDuplicates,
                                        onClick = { skipDuplicates = false },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "Include All (Allow Duplicates)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Imports every row. Use this if your statement includes legitimate identical purchases on the same day.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "→ Will import all ${preview.validTransactionCount + preview.duplicateCount} transactions",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }

                            // Expandable Duplicate Inspection
                            if (preview.duplicateSamples.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDuplicateDetails = !showDuplicateDetails }
                                        .padding(top = 4.dp, bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (showDuplicateDetails) "Hide duplicate details" else "Inspect detected duplicates (${preview.duplicateCount})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        imageVector = if (showDuplicateDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (showDuplicateDetails) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            preview.duplicateSamples.forEachIndexed { idx, sample ->
                                                if (idx > 0) {
                                                    HorizontalDivider(
                                                        thickness = 0.5.dp,
                                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = sample.payee,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "${sample.date} • ${sample.accountName}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Text(
                                                        text = sample.amount,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                            if (preview.duplicateCount > preview.duplicateSamples.size) {
                                                Text(
                                                    text = "+ ${preview.duplicateCount - preview.duplicateSamples.size} more duplicates not shown",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontStyle = FontStyle.Italic,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (preview.validTransactionCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "No duplicates detected. All ${preview.validTransactionCount} transactions are new.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (preview.warningMessages.isNotEmpty()) {
                    Text(
                        text = "Warnings (${preview.warningMessages.size}):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    preview.warningMessages.take(3).forEach { warning ->
                        Text(
                            text = "⚠ $warning",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmImport(skipDuplicates) },
                enabled = canConfirm
            ) {
                val buttonLabel = when {
                    totalToImport > 0 -> "Import $totalToImport Records"
                    hasMetadataChanges -> "Import Metadata"
                    else -> "Confirm & Import"
                }
                Text(buttonLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ImportCsvResultDialog(
    result: ImportCsvResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Import Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CSV import completed successfully!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "• Imported: ${result.importedCount} transactions")
                        if (result.accountsConfiguredCount > 0) {
                            Text(
                                text = "• Accounts Configured: ${result.accountsConfiguredCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (result.updatedCount > 0) {
                            Text(text = "• Updated: ${result.updatedCount} existing transactions")
                        }
                        if (result.skippedCount > 0) {
                            Text(text = "• Skipped: ${result.skippedCount} duplicates/invalid rows")
                        }
                        if (result.newAccountsCreated.isNotEmpty()) {
                            Text(
                                text = "• Created Accounts: ${result.newAccountsCreated.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (result.newCategoriesCreated.isNotEmpty()) {
                            Text(
                                text = "• Created Categories: ${result.newCategoriesCreated.size} new categories",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (result.errorMessages.isNotEmpty()) {
                    Text(
                        text = "Errors / Skipped rows (${result.errorMessages.size}):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    result.errorMessages.take(5).forEach { err ->
                        Text(
                            text = "• $err",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

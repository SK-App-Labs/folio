// File: app/src/main/java/com/folioledger/app/ui/settings/SettingsScreen.kt
package com.folioledger.app.ui.settings

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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Sync
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
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.ui.settings.sheets.AppearanceBottomSheet
import com.folioledger.app.ui.settings.sheets.CategoryEditorDialog
import com.folioledger.app.ui.settings.sheets.CategoryManagerBottomSheet
import com.folioledger.app.ui.settings.sheets.DataAndBackupBottomSheet
import com.folioledger.app.ui.settings.sheets.RegionalFormatBottomSheet
import com.folioledger.app.ui.settings.sheets.TagsAndBookmarksBottomSheet
import com.folioledger.app.ui.theme.FolioAccentPalette
import com.folioledger.app.ui.theme.FolioThemeMode
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var isAppearanceSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isRegionalFormatSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isDataBackupSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isTagsAndBookmarksSheetOpen by rememberSaveable { mutableStateOf(false) }

    var showMonthStartDayDialog by rememberSaveable { mutableStateOf(false) }
    var showClearAllDataDialog by rememberSaveable { mutableStateOf(false) }
    var showUserGuideSheet by rememberSaveable { mutableStateOf(false) }
    var showTipJarSheet by rememberSaveable { mutableStateOf(false) }
    var isSupporterUnlocked by rememberSaveable { mutableStateOf(false) }

    val prefs = uiState.userPreferences

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
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
                    bottom = contentPadding.calculateBottomPadding() + scaffoldPadding.calculateBottomPadding()
                )
        ) {
            // Header Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Settings & Data Management",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Customize themes, regional accounting rules, categories, backups & app security",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    bottom = 48.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Group 1: LEDGER & TAXONOMY
                item {
                    SettingsGroupCard(title = "LEDGER & TAXONOMY") {
                        SettingsNavigationRow(
                            icon = Icons.Default.Category,
                            title = "Categories & Subcategories",
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
                            title = "Tags & Bookmarks",
                            valueText = "${uiState.tags.size} tags • ${uiState.bookmarks.size} saved",
                            enabled = isNotBusy,
                            onClick = { isTagsAndBookmarksSheetOpen = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsNavigationRow(
                            icon = Icons.Default.Payments,
                            title = "Currency & Number Format",
                            valueText = "${prefs.primaryCurrencyCode} • ${if (prefs.numberGrouping == NumberGroupingSystem.INDIAN) "12,34,567" else "1,234,567"}",
                            enabled = isNotBusy,
                            onClick = { isRegionalFormatSheetOpen = true }
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
                            title = "Carry-Over Monthly Balance",
                            checked = prefs.carryOverEnabled,
                            enabled = isNotBusy,
                            onCheckedChange = { viewModel.setCarryOverEnabled(it) }
                        )
                    }
                }

                // Group 2: APPEARANCE & SECURITY
                item {
                    SettingsGroupCard(title = "APPEARANCE & SECURITY") {
                        val themeModeObj = FolioThemeMode.fromKey(prefs.themeMode)
                        val accentPaletteObj = FolioAccentPalette.fromKey(prefs.accentPalette)
                        SettingsNavigationRow(
                            icon = Icons.Default.Palette,
                            title = "Theme & Accent Palette",
                            valueText = themeModeObj.displayName,
                            valueColorDot = accentPaletteObj.previewColor,
                            enabled = isNotBusy,
                            onClick = { isAppearanceSheetOpen = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsSwitchRow(
                            icon = Icons.Default.Fingerprint,
                            title = "Biometric App Lock",
                            checked = prefs.biometricLockEnabled,
                            enabled = isNotBusy,
                            onCheckedChange = { viewModel.setBiometricLockEnabled(it) }
                        )
                    }
                }

                // Group 3: DATA, BACKUPS & HELP
                item {
                    SettingsGroupCard(title = "DATA, BACKUPS & HELP") {
                        SettingsNavigationRow(
                            icon = Icons.Default.Backup,
                            title = "Backups, CSV Export & Reset",
                            valueText = "Downloads/Folio",
                            enabled = isNotBusy,
                            onClick = { isDataBackupSheetOpen = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsNavigationRow(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = "Folio User Guide & FAQs",
                            valueText = "Guide",
                            enabled = isNotBusy,
                            onClick = { showUserGuideSheet = true }
                        )
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsNavigationRow(
                            icon = Icons.Default.LocalCafe,
                            title = "Support Folio (Tip Jar) ☕",
                            valueText = "Tip Jar",
                            enabled = isNotBusy,
                            onClick = { showTipJarSheet = true }
                        )
                    }
                }

                // Minimalist Footer
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 24.dp)
                    ) {
                        Text(
                            text = "Folio Ledger v1.0.1 • 100% Offline SQLite",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    if (isAppearanceSheetOpen) {
        AppearanceBottomSheet(
            prefs = prefs,
            onSelectThemeModeString = viewModel::setThemeMode,
            onSelectAccentPaletteString = viewModel::setAccentPalette,
            onToggleDynamicColor = viewModel::setDynamicColorEnabled,
            onDismiss = { isAppearanceSheetOpen = false }
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

    if (isDataBackupSheetOpen) {
        DataAndBackupBottomSheet(
            onCreateBackup = {
                viewModel.createPortableZipBackup(
                    "Folio_Backup_${LocalDate.now()}_${System.currentTimeMillis() / 1000}.zip"
                )
            },
            onRestoreBackup = {
                viewModel.requestRestoreConfirmation("LATEST_BACKUP")
            },
            onExportCsv = {
                viewModel.exportAllTransactionsToCsv(
                    "Folio_Ledger_Export_${LocalDate.now()}.csv"
                )
            },
            onRequestClearAllData = {
                showClearAllDataDialog = true
            },
            onDismiss = { isDataBackupSheetOpen = false }
        )
    }

    // Dialogs & Secondary Sheets
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

    if (uiState.pendingRestoreUriString != null) {
        RestoreConfirmationDialog(
            onConfirmRestore = viewModel::confirmRestoreFromZipBackup,
            onCancel = viewModel::cancelPendingRestore
        )
    }

    if (showUserGuideSheet) {
        UserGuideBottomSheet(
            onDismiss = { showUserGuideSheet = false }
        )
    }

    if (showTipJarSheet) {
        TipJarBottomSheet(
            currencySpec = uiState.currencySpec,
            isSupporter = isSupporterUnlocked,
            onTipSelected = { _, tierTitle ->
                isSupporterUnlocked = true
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "Thank you for your $tierTitle support! ★ Supporter badge unlocked."
                    )
                }
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

@Composable
private fun RestoreConfirmationDialog(
    onConfirmRestore: () -> Unit,
    onCancel: () -> Unit
) {
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
            Text(
                text = "Restoring the latest backup from Downloads/Folio will replace your current local accounts, transactions, categories, and budgets. Are you sure you want to proceed?",
                style = MaterialTheme.typography.bodyMedium
            )
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
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
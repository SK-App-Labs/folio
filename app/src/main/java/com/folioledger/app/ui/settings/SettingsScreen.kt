// File: app/src/main/java/com/folioledger/app/ui/settings/SettingsScreen.kt
package com.folioledger.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.fromTransactionType
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.theme.FolioAccentPalette
import com.folioledger.app.ui.theme.FolioThemeMode
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.LocalDate

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCurrencyPickerDialog by rememberSaveable { mutableStateOf(false) }
    var showMonthStartDayDialog by rememberSaveable { mutableStateOf(false) }

    val createZipBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            viewModel.createPortableZipBackup(uri.toString())
        }
    }

    val openZipRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.requestRestoreConfirmation(uri.toString())
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportAllTransactionsToCsv(uri.toString())
        }
    }

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
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + scaffoldPadding.calculateTopPadding() + 12.dp,
                bottom = contentPadding.calculateBottomPadding() + scaffoldPadding.calculateBottomPadding() + 48.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column {
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

            // 1. Appearance & Theme Card
            item {
                AppearanceAndThemeSectionCard(
                    themeMode = uiState.userPreferences.themeMode,
                    accentPalette = uiState.userPreferences.accentPalette,
                    dynamicColorEnabled = uiState.userPreferences.dynamicColorEnabled,
                    onThemeModeChange = viewModel::setThemeMode,
                    onAccentPaletteChange = viewModel::setAccentPalette,
                    onDynamicColorChange = viewModel::setDynamicColorEnabled
                )
            }

            // 2. Ledger & Regional Rules Card
            item {
                LedgerAndRegionalRulesSectionCard(
                    currencySpec = uiState.currencySpec,
                    numberGrouping = uiState.userPreferences.numberGrouping,
                    monthStartDay = uiState.userPreferences.monthStartDay,
                    weekStartDayIso = uiState.userPreferences.weekStartDayIso,
                    subcategoriesEnabled = uiState.userPreferences.subcategoriesEnabled,
                    carryOverEnabled = uiState.userPreferences.carryOverEnabled,
                    onOpenCurrencyPicker = { showCurrencyPickerDialog = true },
                    onGroupingChange = viewModel::setNumberGrouping,
                    onOpenMonthStartDayPicker = { showMonthStartDayDialog = true },
                    onWeekStartDayChange = viewModel::setWeekStartDayIso,
                    onSubcategoriesEnabledChange = viewModel::setSubcategoriesEnabled,
                    onCarryOverEnabledChange = viewModel::setCarryOverEnabled
                )
            }

            // 3. Categories, Tags & Bookmarks Card
            item {
                CategoriesTagsBookmarksSectionCard(
                    expenseCount = uiState.totalExpenseCategoryCount,
                    incomeCount = uiState.totalIncomeCategoryCount,
                    tagCount = uiState.tags.size,
                    bookmarkCount = uiState.bookmarks.size,
                    onOpenCategoryManager = {
                        viewModel.setCategoryManagerOpen(true, CategoryType.EXPENSE)
                    },
                    onOpenTagManager = { viewModel.setTagManagerOpen(true) },
                    onOpenBookmarkManager = { viewModel.setBookmarkManagerOpen(true) }
                )
            }

            // 4. Data Backup, Restore & Export Card
            item {
                BackupRestoreExportSectionCard(
                    isBusy = uiState.isBusyWithFileOperation,
                    statusLabel = uiState.fileOperationStatusLabel,
                    onCreateZipBackup = {
                        val todayStr = LocalDate.now().toString()
                        createZipBackupLauncher.launch("Folio_Backup_$todayStr.zip")
                    },
                    onRestoreZipBackup = {
                        openZipRestoreLauncher.launch(
                            arrayOf("application/zip", "application/octet-stream")
                        )
                    },
                    onExportCsv = {
                        val todayStr = LocalDate.now().toString()
                        exportCsvLauncher.launch("Folio_Ledger_Export_$todayStr.csv")
                    }
                )
            }

            // 5. Security & Privacy Card
            item {
                SecurityAndPrivacySectionCard(
                    biometricLockEnabled = uiState.userPreferences.biometricLockEnabled,
                    onBiometricLockChange = viewModel::setBiometricLockEnabled
                )
            }
        }
    }

    // Dialogs & Bottom Sheets
    if (showCurrencyPickerDialog) {
        CurrencyPickerDialog(
            selectedCode = uiState.currencySpec.code,
            onSelectCurrency = { code ->
                viewModel.setPrimaryCurrency(code)
                showCurrencyPickerDialog = false
            },
            onDismiss = { showCurrencyPickerDialog = false }
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

    val categoryEditor = uiState.categoryEditorState
    if (categoryEditor != null) {
        CategoryEditorDialog(
            state = categoryEditor,
            onUpdateState = viewModel::updateCategoryEditorState,
            onSave = viewModel::saveCategoryFromDialog,
            onDelete = {
                if (categoryEditor.isEditing && categoryEditor.categoryId > 0L) {
                    viewModel.deleteCategory(categoryEditor.categoryId, null)
                }
            },
            onDismiss = viewModel::dismissCategoryEditorDialog
        )
    }

    if (uiState.isTagManagerOpen) {
        TagManagerDialog(
            tags = uiState.tags,
            onCreateTag = viewModel::createTag,
            onRenameTag = viewModel::renameTag,
            onDeleteTag = viewModel::deleteTag,
            onDismiss = { viewModel.setTagManagerOpen(false) }
        )
    }

    if (uiState.isBookmarkManagerOpen) {
        BookmarkManagerDialog(
            bookmarks = uiState.bookmarks,
            currencySpec = uiState.currencySpec,
            grouping = uiState.userPreferences.numberGrouping,
            onDeleteBookmark = viewModel::deleteBookmark,
            onDismiss = { viewModel.setBookmarkManagerOpen(false) }
        )
    }

    if (uiState.pendingRestoreUriString != null) {
        RestoreConfirmationDialog(
            onConfirmRestore = viewModel::confirmRestoreFromZipBackup,
            onCancel = viewModel::cancelPendingRestore
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppearanceAndThemeSectionCard(
    themeMode: String,
    accentPalette: String,
    dynamicColorEnabled: Boolean,
    onThemeModeChange: (String) -> Unit,
    onAccentPaletteChange: (String) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Appearance & Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Theme Mode",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FolioThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = themeMode.equals(mode.key, ignoreCase = true),
                        onClick = { onThemeModeChange(mode.key) },
                        label = { Text(mode.displayName) }
                    )
                }
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Accent Palette (6 Presets)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FolioAccentPalette.entries.forEach { palette ->
                    val isSelected = accentPalette.equals(palette.key, ignoreCase = true)
                    val swatchColor = parseHexColorSafely(palette.seedHex, palette.previewColor)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            swatchColor.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) swatchColor else semanticColors.cardStrokeSubtle
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onAccentPaletteChange(palette.key) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(swatchColor)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = palette.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Material You Dynamic Color (Android 12+)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Adapts primary surfaces to your device wallpaper",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = dynamicColorEnabled,
                    onCheckedChange = onDynamicColorChange
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LedgerAndRegionalRulesSectionCard(
    currencySpec: CurrencySpec,
    numberGrouping: NumberGroupingSystem,
    monthStartDay: Int,
    weekStartDayIso: Int,
    subcategoriesEnabled: Boolean,
    carryOverEnabled: Boolean,
    onOpenCurrencyPicker: () -> Unit,
    onGroupingChange: (NumberGroupingSystem) -> Unit,
    onOpenMonthStartDayPicker: () -> Unit,
    onWeekStartDayChange: (Int) -> Unit,
    onSubcategoriesEnabledChange: (Boolean) -> Unit,
    onCarryOverEnabledChange: (Boolean) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ledger & Regional Rules",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Primary Currency Selector Row
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenCurrencyPicker)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Column {
                        Text(
                            text = "Primary Ledger Currency",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${currencySpec.displayName} (${currencySpec.code}) · ${currencySpec.fractionDigits} decimals",
                            style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${currencySpec.symbol} ${currencySpec.code}",
                                style = tabularMoneyStyle(MaterialTheme.typography.labelLarge),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Number Grouping System
            Text(
                text = "Number Digit Grouping",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = numberGrouping == NumberGroupingSystem.INDIAN,
                    onClick = { onGroupingChange(NumberGroupingSystem.INDIAN) },
                    label = {
                        Text(
                            text = "Indian (12,34,567.89)",
                            style = tabularMoneyStyle(MaterialTheme.typography.labelMedium)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = numberGrouping == NumberGroupingSystem.INTERNATIONAL,
                    onClick = { onGroupingChange(NumberGroupingSystem.INTERNATIONAL) },
                    label = {
                        Text(
                            text = "Intl (1,234,567.89)",
                            style = tabularMoneyStyle(MaterialTheme.typography.labelMedium)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Financial Month Start Day
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenMonthStartDayPicker)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Financial Month Start Day",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Aligns monthly summaries & budgets with your salary cycle (1–28)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Day $monthStartDay",
                            style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Week Start Day
            Text(
                text = "Week Start Day",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val weekOptions = listOf(1 to "Monday", 7 to "Sunday", 6 to "Saturday")
                weekOptions.forEach { (iso, label) ->
                    FilterChip(
                        selected = weekStartDayIso == iso,
                        onClick = { onWeekStartDayChange(iso) },
                        label = { Text(label) }
                    )
                }
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            // 2-Level Subcategories Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable 2-Level Subcategories",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Allows nested subcategories under main Expense & Income categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = subcategoriesEnabled,
                    onCheckedChange = onSubcategoriesEnabledChange
                )
            }

            // Period Carry-Over Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Carry-Over Balance Across Months",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Rolls cumulative net surplus/deficit into the next financial period",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = carryOverEnabled,
                    onCheckedChange = onCarryOverEnabledChange
                )
            }
        }
    }
}

@Composable
private fun CategoriesTagsBookmarksSectionCard(
    expenseCount: Int,
    incomeCount: Int,
    tagCount: Int,
    bookmarkCount: Int,
    onOpenCategoryManager: () -> Unit,
    onOpenTagManager: () -> Unit,
    onOpenBookmarkManager: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Categories, #Tags & Bookmarks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            SettingsNavigationRow(
                icon = Icons.Filled.Category,
                title = "2-Level Category & Subcategory Manager",
                subtitle = "$expenseCount Expense items · $incomeCount Income items",
                onClick = onOpenCategoryManager
            )

            SettingsNavigationRow(
                icon = Icons.Filled.Label,
                title = "#Tag Manager",
                subtitle = "$tagCount custom cross-category tags configured",
                onClick = onOpenTagManager
            )

            SettingsNavigationRow(
                icon = Icons.Filled.Star,
                title = "Saved Bookmarks (★) Templates",
                subtitle = "$bookmarkCount quick-entry transaction templates",
                onClick = onOpenBookmarkManager
            )
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BackupRestoreExportSectionCard(
    isBusy: Boolean,
    statusLabel: String?,
    onCreateZipBackup: () -> Unit,
    onRestoreZipBackup: () -> Unit,
    onExportCsv: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Backup,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Portable Backup, Restore & CSV Export",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Uses Android Storage Access Framework — zero storage permissions required",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = isBusy) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel ?: "Processing file...",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            Button(
                onClick = onCreateZipBackup,
                enabled = !isBusy,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Backup,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create Portable ZIP Backup",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    onClick = onRestoreZipBackup,
                    enabled = !isBusy,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Restore ZIP",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onExportCsv,
                    enabled = !isBusy,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Export CSV",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityAndPrivacySectionCard(
    biometricLockEnabled: Boolean,
    onBiometricLockChange: (Boolean) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Security & Offline Privacy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Biometric / Device Credential App Lock",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Require fingerprint, face unlock, or device PIN when opening Folio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = biometricLockEnabled,
                    onCheckedChange = onBiometricLockChange
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = semanticColors.incomeEmeraldContainer.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, semanticColors.incomeEmerald.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = semanticColors.incomeEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "100% Offline-First Architecture: All financial records stay strictly inside your local device database with zero cloud servers, zero telemetry, and zero SMS reading.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrencyPickerDialog(
    selectedCode: String,
    onSelectCurrency: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Primary Currency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SupportedCurrencies.ALL, key = { it.code }) { spec ->
                    val isSelected = spec.code == selectedCode
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectCurrency(spec.code) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "${spec.displayName} (${spec.code})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${spec.fractionDigits} decimal digits · ${spec.defaultGrouping.name} grouping",
                                    style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = spec.symbol,
                                style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
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
                text = "Financial Month Start Day (1–28)",
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
                    text = "Choose the day of the month your financial cycle begins (e.g., Day 1 for calendar month, or Day 25 for salary cycle).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..28).forEach { day ->
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CategoryManagerBottomSheet(
    uiState: SettingsUiState,
    onSelectCategoryType: (CategoryType) -> Unit,
    onAddMainCategory: () -> Unit,
    onAddSubcategory: (CategoryNode) -> Unit,
    onEditCategory: (CategoryNode, String?) -> Unit,
    onDeleteCategory: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    val categories = uiState.activeManagedCategories

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = "Category & Subcategory Manager",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Add, edit, or organize 2-level categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onAddMainCategory,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Main Category", style = MaterialTheme.typography.labelMedium)
                }
            }

            PrimaryTabRow(
                selectedTabIndex = if (uiState.selectedCategoryManagerType == CategoryType.EXPENSE) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = uiState.selectedCategoryManagerType == CategoryType.EXPENSE,
                    onClick = { onSelectCategoryType(CategoryType.EXPENSE) },
                    text = {
                        Text(
                            text = "Expenses (${uiState.expenseCategories.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = uiState.selectedCategoryManagerType == CategoryType.INCOME,
                    onClick = { onSelectCategoryType(CategoryType.INCOME) },
                    text = {
                        Text(
                            text = "Income (${uiState.incomeCategories.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories, key = { it.id }) { parent ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CategoryIconBadge(
                                    iconName = parent.iconName,
                                    colorHex = parent.colorHex,
                                    size = 38.dp,
                                    iconSize = 19.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = parent.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${parent.subcategories.size} subcategories",
                                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                TextButton(
                                    onClick = { onAddSubcategory(parent) },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Sub", style = MaterialTheme.typography.labelSmall)
                                }

                                IconButton(
                                    onClick = { onEditCategory(parent, null) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit ${parent.name}",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteCategory(parent.id) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete ${parent.name}",
                                        tint = semanticColors.expenseCoral,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (parent.subcategories.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    parent.subcategories.forEach { sub ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainer,
                                            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { onEditCategory(sub, parent.name) }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = sub.name,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Filled.Edit,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryEditorDialog(
    state: CategoryEditorDialogState,
    onUpdateState: ((CategoryEditorDialogState) -> CategoryEditorDialogState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val swatches = remember {
        listOf(
            "#059669", "#10B981", "#0D9488", "#2563EB",
            "#4F46E5", "#7C3AED", "#D97706", "#F97316",
            "#E11D48", "#334155"
        )
    }
    val icons = remember {
        listOf(
            "restaurant", "fastfood", "local_cafe", "shopping_cart",
            "checkroom", "home", "bolt", "wifi",
            "phone_android", "directions_car", "local_gas_station", "directions_bus",
            "flight", "local_hospital", "fitness_center", "spa",
            "movie", "school", "child_care", "pets",
            "build", "payments", "work", "trending_up",
            "savings", "card_giftcard", "receipt_long", "security"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIconBadge(
                    iconName = state.iconName,
                    colorHex = state.colorHex,
                    size = 36.dp,
                    iconSize = 18.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = when {
                            state.isEditing -> "Edit Category"
                            state.parentId != null -> "New Subcategory"
                            else -> "New Main Category"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!state.parentCategoryName.isNullOrBlank()) {
                        Text(
                            text = "Under ${state.parentCategoryName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (!state.validationError.isNullOrBlank()) {
                    Text(
                        text = state.validationError,
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalFolioSemanticColors.current.expenseCoral
                    )
                }

                OutlinedTextField(
                    value = state.name,
                    onValueChange = { v -> onUpdateState { it.copy(name = v) } },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Badge Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    swatches.forEach { hex ->
                        val color = parseHexColorSafely(hex)
                        val selected = state.colorHex.equals(hex, ignoreCase = true)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onUpdateState { it.copy(colorHex = hex) } }
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    icons.forEach { iconKey ->
                        val selected = state.iconName == iconKey
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            border = BorderStroke(
                                1.dp,
                                if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    LocalFolioSemanticColors.current.cardStrokeSubtle
                                }
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onUpdateState { it.copy(iconName = iconKey) } }
                                .padding(6.dp)
                        ) {
                            CategoryIconBadge(
                                iconName = iconKey,
                                colorHex = state.colorHex,
                                size = 28.dp,
                                iconSize = 15.dp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (state.isEditing && state.categoryId > 0L) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = LocalFolioSemanticColors.current.expenseCoral
                        )
                    ) {
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun TagManagerDialog(
    tags: List<String>,
    onCreateTag: (String) -> Unit,
    onRenameTag: (String, String) -> Unit,
    onDeleteTag: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newTagInput by remember { mutableStateOf("") }
    var tagBeingRenamed by remember { mutableStateOf<String?>(null) }
    var renameInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "#Tag Manager (${tags.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newTagInput,
                        onValueChange = { newTagInput = it },
                        label = { Text("New #tag name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newTagInput.isNotBlank()) {
                                onCreateTag(newTagInput)
                                newTagInput = ""
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }

                if (tagBeingRenamed != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            OutlinedTextField(
                                value = renameInput,
                                onValueChange = { renameInput = it },
                                label = { Text("Rename #$tagBeingRenamed") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    val old = tagBeingRenamed
                                    if (old != null && renameInput.isNotBlank()) {
                                        onRenameTag(old, renameInput)
                                    }
                                    tagBeingRenamed = null
                                }
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }

                HorizontalDivider(color = LocalFolioSemanticColors.current.cardStrokeSubtle)

                if (tags.isEmpty()) {
                    Text(
                        text = "No tags created yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(tags, key = { it }) { tag ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            tagBeingRenamed = tag
                                            renameInput = tag
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Rename #$tag",
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteTag(tag) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete #$tag",
                                            tint = LocalFolioSemanticColors.current.expenseCoral,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun BookmarkManagerDialog(
    bookmarks: List<TransactionBookmark>,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onDeleteBookmark: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Saved Bookmarks (${bookmarks.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (bookmarks.isEmpty()) {
                Text(
                    text = "No bookmarks saved yet. Open the + New Transaction sheet and tap ★ to save any form as a reusable template.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(bookmarks, key = { it.id }) { bm ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bm.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (bm.payeeOrDescription.isNotBlank()) {
                                        Text(
                                            text = bm.payeeOrDescription,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                MoneyText(
                                    money = bm.amount,
                                    currencySpec = currencySpec,
                                    grouping = grouping,
                                    colorMode = MoneyColorMode.fromTransactionType(bm.type),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { onDeleteBookmark(bm.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete Bookmark",
                                        tint = LocalFolioSemanticColors.current.expenseCoral,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
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
                text = "Restoring this backup will replace your current local accounts, transactions, categories, and budgets with the contents of the selected ZIP archive. Are you sure you want to proceed?",
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
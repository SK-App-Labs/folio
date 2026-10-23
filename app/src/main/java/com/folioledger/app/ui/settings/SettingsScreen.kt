// File: app/src/main/java/com/folioledger/app/ui/settings/SettingsScreen.kt
package com.folioledger.app.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.components.ALL_CATEGORY_ICON_KEYS
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
            // Header Bar (Matches Accounts & Net Worth header typography & padding)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                            onClick = { viewModel.setCategoryManagerOpen(true) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsSwitchRow(
                            icon = Icons.Default.Category,
                            title = "Enable Subcategories",
                            checked = prefs.subcategoriesEnabled,
                            onCheckedChange = { viewModel.setSubcategoriesEnabled(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsNavigationRow(
                            icon = Icons.AutoMirrored.Filled.Label,
                            title = "Tags & Bookmarks",
                            valueText = "${uiState.tags.size} tags • ${uiState.bookmarks.size} saved",
                            onClick = { isTagsAndBookmarksSheetOpen = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsNavigationRow(
                            icon = Icons.Default.Payments,
                            title = "Currency & Number Format",
                            valueText = "${prefs.primaryCurrencyCode} • ${if (prefs.numberGrouping == NumberGroupingSystem.INDIAN) "12,34,567" else "1,234,567"}",
                            onClick = { isRegionalFormatSheetOpen = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsNavigationRow(
                            icon = Icons.Default.CalendarMonth,
                            title = "Financial Month Start Day",
                            valueText = if (prefs.monthStartDay == 1) "1st of month" else "Day ${prefs.monthStartDay}",
                            onClick = { showMonthStartDayDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsSwitchRow(
                            icon = Icons.Default.Sync,
                            title = "Carry-Over Monthly Balance",
                            checked = prefs.carryOverEnabled,
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
                            onClick = { isAppearanceSheetOpen = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsSwitchRow(
                            icon = Icons.Default.Fingerprint,
                            title = "Biometric App Lock",
                            checked = prefs.biometricLockEnabled,
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
                            onClick = { isDataBackupSheetOpen = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsNavigationRow(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = "Folio User Guide & FAQs",
                            valueText = "Guide",
                            onClick = { showUserGuideSheet = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsNavigationRow(
                            icon = Icons.Default.LocalCafe,
                            title = "Support Folio (Tip Jar) ☕",
                            valueText = "Tip Jar",
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
                        Spacer(modifier = Modifier.height(2.dp))
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
                viewModel.requestRestoreConfirmation("LATEST_BACKUP")
            },
            onDismiss = { isDataBackupSheetOpen = false }
        )
    }

    // Dialogs & Secondary Sheets
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
private fun CategoryEditorDialog(
    state: CategoryEditorDialogState,
    onNameChange: (String) -> Unit,
    onUpdateState: ((CategoryEditorDialogState) -> CategoryEditorDialogState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val swatches = remember {
        listOf(
            "#059669", "#10B981", "#0D9488", "#0284C7",
            "#2563EB", "#4F46E5", "#7C3AED", "#EC4899",
            "#E11D48", "#F97316", "#EA580C", "#D97706",
            "#334155"
        )
    }
    val icons = remember { ALL_CATEGORY_ICON_KEYS }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIconBadge(
                    iconName = state.iconName,
                    colorHex = state.colorHex,
                    size = 38.dp,
                    iconSize = 20.dp
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
                    .heightIn(max = 440.dp)
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
                    onValueChange = onNameChange,
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g., Filter Coffee, Petrol, Swiggy, SIP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(visible = state.isSmartIconMatched) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "✨ Auto-matched icon & color from category name",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "Badge Color (${swatches.size} Swatches)",
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
                    text = "Select Icon (${icons.size} Icons)",
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
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) {
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
                                iconSize = 16.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagsAndBookmarksBottomSheet(
    uiState: SettingsUiState,
    onCreateTag: (String) -> Unit,
    onRenameTag: (String, String) -> Unit,
    onDeleteTag: (String) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    var newTagInput by remember { mutableStateOf("") }
    var tagBeingRenamed by remember { mutableStateOf<String?>(null) }
    var renameInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(bottom = 24.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = "Tags & Favorite Bookmarks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage cross-category #tags & saved transaction templates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tab Row
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "#Tags (${uiState.tags.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Bookmarks ★ (${uiState.bookmarks.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Tag Manager Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Create New Tag Input Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newTagInput,
                                onValueChange = { newTagInput = it },
                                label = { Text("New #tag name") },
                                placeholder = { Text("e.g. vacation, tax, project") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (newTagInput.isNotBlank()) {
                                        onCreateTag(newTagInput.trim())
                                        newTagInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                enabled = newTagInput.isNotBlank()
                            ) {
                                Text("Add")
                            }
                        }

                        // Rename Tag Section
                        if (tagBeingRenamed != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = renameInput,
                                        onValueChange = { renameInput = it },
                                        label = { Text("Rename #$tagBeingRenamed") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = {
                                            val old = tagBeingRenamed
                                            if (old != null && renameInput.isNotBlank()) {
                                                onRenameTag(old, renameInput.trim())
                                            }
                                            tagBeingRenamed = null
                                        }
                                    ) {
                                        Text("Save", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = semanticColors.cardStrokeSubtle)

                        if (uiState.tags.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No tags created yet. Type a name above to create your first #tag.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(uiState.tags, key = { it }) { tag ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                        ),
                                        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = "#$tag",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    tagBeingRenamed = tag
                                                    renameInput = tag
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Rename #$tag",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeleteTag(tag) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete #$tag",
                                                    tint = semanticColors.expenseCoral,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Bookmark Templates Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (uiState.bookmarks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No favorite bookmarks saved yet.\nTap ★ in the + New Transaction sheet to save any form as a reusable template.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(uiState.bookmarks, key = { it.id }) { bm ->
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                        ),
                                        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = bm.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (bm.payeeOrDescription.isNotBlank()) {
                                                    Text(
                                                        text = bm.payeeOrDescription,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            MoneyText(
                                                money = bm.amount,
                                                currencySpec = uiState.currencySpec,
                                                grouping = uiState.userPreferences.numberGrouping,
                                                colorMode = MoneyColorMode.fromTransactionType(bm.type),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            IconButton(
                                                onClick = { onDeleteBookmark(bm.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Bookmark",
                                                    tint = semanticColors.expenseCoral,
                                                    modifier = Modifier.size(16.dp)
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

// ============================================================================
// TYPE ALIASES & REUSABLE LAYOUT COMPONENTS
// ============================================================================

typealias ThemeMode = FolioThemeMode
typealias AccentPalette = FolioAccentPalette
typealias NumberGroupingStyle = NumberGroupingSystem

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
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp, top = 16.dp)
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
            tonalElevation = 1.dp,
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
    valueColorDot: Color? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
    subtitle: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
            onCheckedChange = onCheckedChange
        )
    }
}

// ============================================================================
// 2. FOCUSED MODAL BOTTOM SHEETS
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceBottomSheet(
    prefs: UserPreferences,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onSelectAccentPalette: (AccentPalette) -> Unit,
    onToggleDynamicColor: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentTheme = FolioThemeMode.fromKey(prefs.themeMode)
    val currentAccent = FolioAccentPalette.fromKey(prefs.accentPalette)
    val semanticColors = LocalFolioSemanticColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp, top = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Appearance & Theme",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Live Theme & Accent Preview Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "LIVE THEME PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = currentAccent.previewColor.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = currentAccent.displayName,
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                fontWeight = FontWeight.Bold,
                                color = currentAccent.previewColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💼 Monthly Salary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "+₹45,000.00",
                            style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                            fontWeight = FontWeight.Bold,
                            color = semanticColors.incomeEmerald
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "☕ Filter Coffee",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "-₹150.00",
                            style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                            fontWeight = FontWeight.Bold,
                            color = semanticColors.expenseCoral
                        )
                    }
                }
            }

            // Theme Mode Selection Cards
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "THEME MODE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FolioThemeMode.entries.forEach { mode ->
                        val isSelected = currentTheme == mode
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                }
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else semanticColors.cardStrokeSubtle
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectThemeMode(mode) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (mode) {
                                                FolioThemeMode.SYSTEM -> MaterialTheme.colorScheme.primary
                                                FolioThemeMode.LIGHT -> Color(0xFFFFFFFF)
                                                FolioThemeMode.SEPIA -> Color(0xFFF7F3E9)
                                                FolioThemeMode.SLATE_GRAY -> Color(0xFF202124)
                                                FolioThemeMode.DARK -> Color(0xFF0F172A)
                                                FolioThemeMode.AMOLED_BLACK -> Color(0xFF000000)
                                            }
                                        )
                                        .border(1.dp, semanticColors.cardStrokeSubtle, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = mode.displayName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // Accent Palette Selection
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ACCENT COLOR PALETTE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = currentAccent.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = currentAccent.previewColor
                    )
                }

                val row1Palettes = remember { FolioAccentPalette.entries.take(6) }
                val row2Palettes = remember { FolioAccentPalette.entries.drop(6) }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Row 1 (Exactly 6 swatches)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row1Palettes.forEach { palette ->
                            val isSelected = currentAccent == palette
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(palette.previewColor)
                                    .clickable { onSelectAccentPalette(palette) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = palette.displayName,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Row 2 (Exactly 6 swatches)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row2Palettes.forEach { palette ->
                            val isSelected = currentAccent == palette
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(palette.previewColor)
                                    .clickable { onSelectAccentPalette(palette) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = palette.displayName,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // Dynamic Color Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dynamic Color (Material You)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Adapt system colors from wallpaper on supported devices",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = prefs.dynamicColorEnabled,
                    onCheckedChange = onToggleDynamicColor
                )
            }
        }
    }
}

// String-based overload for AppearanceBottomSheet for seamless compatibility
@JvmName("AppearanceBottomSheetString")
@Composable
fun AppearanceBottomSheet(
    prefs: UserPreferences,
    onSelectThemeModeString: (String) -> Unit,
    onSelectAccentPaletteString: (String) -> Unit,
    onToggleDynamicColor: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AppearanceBottomSheet(
        prefs = prefs,
        onSelectThemeMode = { onSelectThemeModeString(it.key) },
        onSelectAccentPalette = { onSelectAccentPaletteString(it.key) },
        onToggleDynamicColor = onToggleDynamicColor,
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionalFormatBottomSheet(
    prefs: UserPreferences,
    onSelectCurrency: (String) -> Unit,
    onSelectGroupingStyle: (NumberGroupingStyle) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activeCurrencySpec = SupportedCurrencies.fromCode(prefs.primaryCurrencyCode)
    val semanticColors = LocalFolioSemanticColors.current

    var searchCurrencyQuery by rememberSaveable { mutableStateOf("") }

    // Currencies list: INR, USD, EUR, GBP, JPY, AED, SGD, AUD, CAD, CHF, CNY, SAR
    val supportedCodes = listOf("INR", "USD", "EUR", "GBP", "JPY", "AED", "SGD", "AUD", "CAD", "CHF", "CNY", "SAR")
    val allCurrencies = remember {
        supportedCodes.map { code ->
            SupportedCurrencies.getByCode(code) ?: CurrencySpec(
                code = code,
                symbol = code,
                displayName = when (code) {
                    "SAR" -> "Saudi Riyal"
                    else -> code
                },
                fractionDigits = 2,
                defaultGrouping = NumberGroupingSystem.INTERNATIONAL
            )
        }
    }

    val popularCurrencies = remember(allCurrencies) {
        allCurrencies.filter { it.code in listOf("INR", "USD", "EUR", "GBP", "AED", "SAR") }
    }

    val filteredCurrencies = remember(allCurrencies, searchCurrencyQuery) {
        if (searchCurrencyQuery.isBlank()) {
            allCurrencies
        } else {
            allCurrencies.filter { currency ->
                currency.code.contains(searchCurrencyQuery, ignoreCase = true) ||
                currency.displayName.contains(searchCurrencyQuery, ignoreCase = true) ||
                currency.symbol.contains(searchCurrencyQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Regional & Formatting",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Live Formatted Preview Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LIVE NUMBER FORMAT PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = MoneyFormatterAndCalculator.formatMinorMoney(
                            money = MinorMoney(123456789L),
                            currencySpec = activeCurrencySpec,
                            grouping = prefs.numberGrouping,
                            includeSymbol = true
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${activeCurrencySpec.displayName} (${if (prefs.numberGrouping == NumberGroupingSystem.INDIAN) "Indian" else "Standard"} Grouping)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Number Grouping System Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "NUMBER GROUPING STYLE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isIndian = prefs.numberGrouping == NumberGroupingSystem.INDIAN
                    val isStandard = prefs.numberGrouping == NumberGroupingSystem.INTERNATIONAL

                    FilterChip(
                        selected = isIndian,
                        onClick = { onSelectGroupingStyle(NumberGroupingSystem.INDIAN) },
                        label = { Text("Indian (12,34,567.89)") },
                        leadingIcon = if (isIndian) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = isStandard,
                        onClick = { onSelectGroupingStyle(NumberGroupingSystem.INTERNATIONAL) },
                        label = { Text("Standard (1,234,567.89)") },
                        leadingIcon = if (isStandard) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider()

            // Primary Currency Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "PRIMARY LEDGER CURRENCY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Search Currency Field
                OutlinedTextField(
                    value = searchCurrencyQuery,
                    onValueChange = { searchCurrencyQuery = it },
                    placeholder = { Text("Search currency") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchCurrencyQuery.isNotBlank()) {
                            IconButton(onClick = { searchCurrencyQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick / Popular Currencies Chips
                if (popularCurrencies.isNotEmpty() && searchCurrencyQuery.isBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "⚡ POPULAR CURRENCIES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(popularCurrencies, key = { it.code }) { currency ->
                                val isSelected = prefs.primaryCurrencyCode.equals(currency.code, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectCurrency(currency.code) },
                                    label = { Text("${currency.code} (${currency.symbol})") },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }

                // Currency Selectable Cards List
                if (filteredCurrencies.isEmpty()) {
                    Text(
                        text = "No currency found matching \"$searchCurrencyQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredCurrencies.forEach { currency ->
                            val isSelected = prefs.primaryCurrencyCode.equals(currency.code, ignoreCase = true)
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    }
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else semanticColors.cardStrokeSubtle
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectCurrency(currency.code) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = currency.symbol,
                                                    style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "${currency.displayName} (${currency.code})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataAndBackupBottomSheet(
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onExportCsv: () -> Unit,
    onRequestClearAllData: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "Data Management & Backup",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "All backup files are automatically saved to Downloads/Folio",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Create Portable ZIP Backup
            Card(
                onClick = {
                    onCreateBackup()
                    onDismiss()
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Create Portable ZIP Backup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Encrypted archive of database, settings, and preferences",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Restore Latest ZIP Backup
            Card(
                onClick = {
                    onRestoreBackup()
                    onDismiss()
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Restore Latest ZIP Backup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Restore database from Downloads/Folio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Export Full Ledger to CSV
            Card(
                onClick = {
                    onExportCsv()
                    onDismiss()
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Export Full Ledger to CSV",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Spreadsheet compatible file with all transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Danger Zone — Reset / Clear All Data
            OutlinedButton(
                onClick = {
                    onRequestClearAllData()
                    onDismiss()
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Danger Zone — Reset / Clear All Data",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val subcategoriesEnabled = uiState.userPreferences.subcategoriesEnabled

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var expandedCategoryIds by rememberSaveable { mutableStateOf(setOf<Long>()) }

    // Filter categories dynamically based on searchQuery
    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) {
            categories
        } else {
            categories.mapNotNull { parent ->
                val parentMatches = parent.name.contains(searchQuery, ignoreCase = true)
                val matchingSubs = parent.subcategories.filter { sub ->
                    sub.name.contains(searchQuery, ignoreCase = true)
                }
                if (parentMatches || matchingSubs.isNotEmpty()) {
                    parent.copy(
                        subcategories = if (parentMatches) parent.subcategories else matchingSubs
                    )
                } else null
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(bottom = 24.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (subcategoriesEnabled) "Category & Subcategory Manager" else "Category Manager",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (subcategoriesEnabled) "Add, edit, or organize 2-level categories" else "Add or edit main categories",
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

            // Real-Time Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search categories or subcategories...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // Primary Tab Row
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

            if (filteredCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No categories found for \"$searchQuery\"" else "No categories found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredCategories, key = { it.id }) { parent ->
                        var showMenu by remember { mutableStateOf(false) }
                        val isSearching = searchQuery.isNotBlank()
                        val isExpanded = isSearching || expandedCategoryIds.contains(parent.id)

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Accordion Parent Row — NO .clickable on the container row!
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
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = parent.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (subcategoriesEnabled) {
                                            Text(
                                                text = "${parent.subcategories.size} subcategories",
                                                style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Parent Overflow Menu (⋮)
                                    Box {
                                        IconButton(onClick = { showMenu = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Options for ${parent.name}",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false }
                                        ) {
                                            if (subcategoriesEnabled) {
                                                DropdownMenuItem(
                                                    text = { Text("Add Subcategory") },
                                                    leadingIcon = {
                                                        Icon(Icons.Default.Add, contentDescription = null)
                                                    },
                                                    onClick = {
                                                        showMenu = false
                                                        onAddSubcategory(parent)
                                                    }
                                                )
                                            }
                                            DropdownMenuItem(
                                                text = { Text("Edit Main Category") },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Edit, contentDescription = null)
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    onEditCategory(parent, null)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete Category", color = semanticColors.expenseCoral) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = semanticColors.expenseCoral)
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    onDeleteCategory(parent.id)
                                                }
                                            )
                                        }
                                    }

                                    // Dedicated Chevron IconButton to toggle expansion!
                                    if (subcategoriesEnabled) {
                                        IconButton(
                                            onClick = {
                                                if (!isSearching) {
                                                    expandedCategoryIds = if (isExpanded) {
                                                        expandedCategoryIds - parent.id
                                                    } else {
                                                        expandedCategoryIds + parent.id
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = if (isExpanded) "Collapse ${parent.name}" else "Expand ${parent.name}",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Expanded Subcategory Tree List
                                if (subcategoriesEnabled && isExpanded) {
                                    AnimatedVisibility(visible = isExpanded) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 10.dp)
                                        ) {
                                            HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                                            Spacer(modifier = Modifier.height(8.dp))

                                            if (parent.subcategories.isEmpty()) {
                                                Text(
                                                    text = "No subcategories created yet under ${parent.name}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                                                )
                                            } else {
                                                parent.subcategories.forEach { sub ->
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(start = 20.dp, top = 4.dp, bottom = 4.dp, end = 4.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                                    ) {
                                                        CategoryIconBadge(
                                                            iconName = sub.iconName,
                                                            colorHex = sub.colorHex,
                                                            size = 32.dp,
                                                            iconSize = 16.dp
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text = sub.name,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.SemiBold,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        IconButton(
                                                            onClick = { onEditCategory(sub, parent.name) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Edit,
                                                                contentDescription = "Edit ${sub.name}",
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                        }
                                                        IconButton(
                                                            onClick = { onDeleteCategory(sub.id) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Delete ${sub.name}",
                                                                tint = semanticColors.expenseCoral,
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            TextButton(
                                                onClick = { onAddSubcategory(parent) },
                                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Add Subcategory under ${parent.name}",
                                                    style = MaterialTheme.typography.labelMedium
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




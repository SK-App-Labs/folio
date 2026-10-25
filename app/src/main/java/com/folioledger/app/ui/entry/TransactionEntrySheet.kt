// File: app/src/main/java/com/folioledger/app/ui/entry/TransactionEntrySheet.kt
package com.folioledger.app.ui.entry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.components.suggestIconAndColorForCategoryName
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.fromTransactionType
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEntrySheet(
    viewModel: TransactionEntryViewModel,
    onDismiss: () -> Unit,
    onTransactionSaved: () -> Unit = onDismiss,
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        TransactionEntryFormContent(
            uiState = uiState,
            onModeSelected = viewModel::setMode,
            onStepDateByDays = viewModel::stepLedgerDateByDays,
            onDateSelected = viewModel::setLedgerDateEpochDay,
            onSelectSourceAccount = viewModel::selectSourceAccount,
            onSelectDestinationAccount = viewModel::selectTransferDestinationAccount,
            onTransferFeeChange = viewModel::updateTransferFeeInput,
            onSelectCategory = viewModel::selectCategory,
            onCreateCategory = viewModel::createAndSelectCategory,
            onToggleSplit = viewModel::toggleSplitMode,
            onAddSplitLine = viewModel::addSplitLine,
            onUpdateSplitAmount = viewModel::updateSplitLineAmount,
            onUpdateSplitCategory = viewModel::updateSplitLineCategory,
            onAssignRemainingToSplit = viewModel::assignRemainingToSplitLine,
            onRemoveSplitLine = viewModel::removeSplitLine,
            onToggleInstallment = viewModel::toggleInstallmentEnabled,
            onSelectInstallmentMonths = viewModel::setInstallmentMonthsCount,
            onPayeeChange = viewModel::updatePayeeOrDescription,
            onNoteChange = viewModel::updateNote,
            onToggleTag = viewModel::toggleTagSelection,
            onCreateNewTag = viewModel::createAndSelectTag,
            onApplyBookmark = viewModel::applyBookmark,
            onSaveBookmark = viewModel::saveCurrentAsBookmark,
            onDeleteBookmark = viewModel::deleteBookmark,
            onSetKeypadVisible = viewModel::setKeypadVisible,
            onKeypadKeyPress = viewModel::onKeypadKeyPress,
            onSaveClick = { viewModel.saveTransaction(onSuccess = onTransactionSaved) },
            onCloseClick = onDismiss
        )
    }
}

@Composable
fun TransactionEntryFormContent(
    uiState: TransactionEntryUiState,
    onModeSelected: (TransactionType) -> Unit,
    onStepDateByDays: (Long) -> Unit,
    onDateSelected: (Long) -> Unit,
    onSelectSourceAccount: (Long) -> Unit,
    onSelectDestinationAccount: (Long) -> Unit,
    onTransferFeeChange: (String) -> Unit,
    onSelectCategory: (CategoryNode, String?) -> Unit,
    onCreateCategory: (name: String, parentId: Long?, parentName: String?, iconName: String, colorHex: String, splitLineLocalId: Long?, onSuccess: ((CategoryNode) -> Unit)?) -> Unit = { _, _, _, _, _, _, _ -> },
    onToggleSplit: () -> Unit,
    onAddSplitLine: () -> Unit,
    onUpdateSplitAmount: (Long, String) -> Unit,
    onUpdateSplitCategory: (Long, CategoryNode) -> Unit,
    onAssignRemainingToSplit: (Long) -> Unit,
    onRemoveSplitLine: (Long) -> Unit,
    onToggleInstallment: () -> Unit,
    onSelectInstallmentMonths: (Int) -> Unit,
    onPayeeChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onToggleTag: (String) -> Unit,
    onCreateNewTag: (String) -> Unit,
    onApplyBookmark: (TransactionBookmark) -> Unit,
    onSaveBookmark: (String) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onSetKeypadVisible: (Boolean) -> Unit,
    onKeypadKeyPress: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val focusManager = LocalFocusManager.current

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    var showSourceAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showDestAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showCategorySheet by rememberSaveable { mutableStateOf(false) }
    var splitLineIdForCategoryPicker by rememberSaveable { mutableStateOf<Long?>(null) }
    var showBookmarksDialog by rememberSaveable { mutableStateOf(false) }
    var showNewTagDialog by rememberSaveable { mutableStateOf(false) }

    val formattedDate = remember(uiState.ledgerDateEpochDay) {
        val date = LocalDate.ofEpochDay(uiState.ledgerDateEpochDay)
        val today = LocalDate.now()
        val pattern = DateTimeFormatter.ofPattern("dd MMM yyyy (EEE)", Locale.US)
        when (date) {
            today -> "Today, ${date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.US))}"
            today.minusDays(1) -> "Yesterday, ${date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.US))}"
            else -> date.format(pattern)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Top Header: Segmented Mode Tabs + Bookmarks ★ + Close
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close Entry Sheet"
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
            ) {
                val modes = listOf(
                    TransactionType.EXPENSE to "Expense",
                    TransactionType.INCOME to "Income",
                    TransactionType.TRANSFER to "Transfer",
                    TransactionType.REFUND to "Refund"
                )
                modes.forEach { (type, label) ->
                    val selected = uiState.mode == type
                    val activeAccent = when (type) {
                        TransactionType.INCOME, TransactionType.REFUND -> semanticColors.incomeEmerald
                        TransactionType.EXPENSE -> semanticColors.expenseCoral
                        TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> semanticColors.transferBlue
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) {
                            activeAccent.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        border = BorderStroke(
                            1.dp,
                            if (selected) activeAccent else semanticColors.cardStrokeSubtle
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onModeSelected(type) }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) activeAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = { showBookmarksDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Bookmarks Templates",
                    tint = semanticColors.warningAmber
                )
            }
        }

        // Amount & Live Calculator Expression Display Banner (Tapping toggles keypad visibility)
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    focusManager.clearFocus()
                    onSetKeypadVisible(!uiState.isKeypadVisible)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    val modePrefix = if (uiState.editingTransactionId > 0L) "EDITING · " else ""
                    Text(
                        text = modePrefix + when (uiState.mode) {
                            TransactionType.EXPENSE -> "EXPENSE AMOUNT"
                            TransactionType.INCOME -> "INCOME AMOUNT"
                            TransactionType.TRANSFER -> "TRANSFER AMOUNT"
                            TransactionType.REFUND -> "REFUND AMOUNT"
                            TransactionType.CC_SETTLEMENT -> "SETTLEMENT AMOUNT"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.expressionHasOperator || uiState.expressionBuffer != "0") {
                        Text(
                            text = "${uiState.currencySpec.symbol} ${uiState.expressionBuffer}",
                            style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoneyText(
                        money = uiState.evaluatedAmount,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping,
                        colorMode = MoneyColorMode.fromTransactionType(uiState.mode),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Calculate,
                        contentDescription = "Toggle Calculator Keypad",
                        tint = if (uiState.isKeypadVisible) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (!uiState.errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = semanticColors.expenseCoralContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.labelMedium,
                    color = semanticColors.onExpenseCoralContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Scrollable Middle Form Area
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Date & Quick Feature Toggles Row (Split / Inst.)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { onStepDateByDays(-1L) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Day"
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showDatePickerDialog = true }
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formattedDate,
                                style = tabularMoneyStyle(MaterialTheme.typography.labelLarge),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }

                        IconButton(
                            onClick = { onStepDateByDays(1L) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Day"
                            )
                        }
                    }
                }

                if (uiState.mode == TransactionType.EXPENSE || uiState.mode == TransactionType.INCOME) {
                    FilterChip(
                        selected = uiState.isSplit,
                        onClick = onToggleSplit,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Layers,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Split") }
                    )
                }

                if (uiState.isCreditCardExpense) {
                    FilterChip(
                        selected = uiState.isInstallmentEnabled,
                        onClick = onToggleInstallment,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Inst.") }
                    )
                }
            }

            // 2-Column Account & Category (or From Account & To Account) Selector Cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left Selector: Account / From Account
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showSourceAccountDialog = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        CategoryIconBadge(
                            iconName = uiState.selectedAccount?.iconName ?: "account_balance_wallet",
                            colorHex = uiState.selectedAccount?.colorHex ?: "#059669",
                            size = 34.dp,
                            iconSize = 18.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.mode == TransactionType.TRANSFER) "From Account" else "Account",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = uiState.selectedAccount?.name ?: "Select Account",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Right Selector: Destination Account (if Transfer) OR Category/Subcategory
                if (uiState.mode == TransactionType.TRANSFER) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showDestAccountDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            CategoryIconBadge(
                                iconName = uiState.transferToAccount?.iconName ?: "swap_horiz",
                                colorHex = uiState.transferToAccount?.colorHex ?: "#2563EB",
                                size = 34.dp,
                                iconSize = 18.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "To Account",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = uiState.transferToAccount?.name ?: "Select Destination",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else if (!uiState.isSplit) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                splitLineIdForCategoryPicker = null
                                showCategorySheet = true
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            CategoryIconBadge(
                                iconName = uiState.selectedCategoryIconName ?: "category",
                                colorHex = uiState.selectedCategoryColorHex ?: "#059669",
                                size = 34.dp,
                                iconSize = 18.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.selectedParentCategoryName ?: "Category",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = uiState.selectedCategoryName ?: "Select Category",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Optional Transfer Fee Input
            AnimatedVisibility(visible = uiState.mode == TransactionType.TRANSFER) {
                OutlinedTextField(
                    value = uiState.transferFeeInput,
                    onValueChange = onTransferFeeChange,
                    label = { Text("Optional Transfer Fee (${uiState.currencySpec.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) onSetKeypadVisible(false) }
                )
            }

            // Dynamic Split Transaction Editor Section
            AnimatedVisibility(visible = uiState.isSplit) {
                SplitTransactionEditorCard(
                    uiState = uiState,
                    onAddSplitLine = onAddSplitLine,
                    onPickSplitCategory = { localId ->
                        splitLineIdForCategoryPicker = localId
                        showCategorySheet = true
                    },
                    onUpdateSplitAmount = onUpdateSplitAmount,
                    onAssignRemainingToSplit = onAssignRemainingToSplit,
                    onRemoveSplitLine = onRemoveSplitLine
                )
            }

            // Dynamic Credit Card Installment Plan (Inst.) Section
            AnimatedVisibility(visible = uiState.isInstallmentEnabled && uiState.isCreditCardExpense) {
                InstallmentPlanCard(
                    uiState = uiState,
                    onSelectMonths = onSelectInstallmentMonths
                )
            }

            // Payee / Description & Note Inputs
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.payeeOrDescription,
                    onValueChange = onPayeeChange,
                    label = { Text("Payee / Description") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { if (it.isFocused) onSetKeypadVisible(false) }
                )

                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = onNoteChange,
                    label = { Text("Note / Memo") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { if (it.isFocused) onSetKeypadVisible(false) }
                )
            }

            // Tags Row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                uiState.availableTags.forEach { tag ->
                    val selected = tag in uiState.selectedTags
                    FilterChip(
                        selected = selected,
                        onClick = { onToggleTag(tag) },
                        label = { Text("#$tag", style = MaterialTheme.typography.labelSmall) }
                    )
                }

                FilterChip(
                    selected = false,
                    onClick = { showNewTagDialog = true },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Label,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    label = { Text("+ New Tag", style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        HorizontalDivider(color = semanticColors.cardStrokeSubtle)

        // Built-in Fast Calculator Numeric Keypad with Top Minimize / Hide Strip
        AnimatedVisibility(visible = uiState.isKeypadVisible) {
            CalculatorNumericKeypad(
                isSaving = uiState.isSaving,
                isEditingExisting = uiState.editingTransactionId > 0L,
                expressionBuffer = uiState.expressionBuffer,
                currencySymbol = uiState.currencySpec.symbol,
                expressionHasOperator = uiState.expressionHasOperator,
                onKeyPress = onKeypadKeyPress,
                onHideKeypadClick = { onSetKeypadVisible(false) },
                onSaveClick = onSaveClick
            )
        }

        if (!uiState.isKeypadVisible) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        focusManager.clearFocus()
                        onSetKeypadVisible(true)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Show Keypad")
                }

                Button(
                    onClick = onSaveClick,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (uiState.editingTransactionId > 0L) "Update" else "Save Transaction")
                }
            }
        }
    }

    // Dialogs & Sub-Sheets
    if (showDatePickerDialog) {
        val initialMillis = remember(uiState.ledgerDateEpochDay) {
            LocalDate.ofEpochDay(uiState.ledgerDateEpochDay)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val selectedEpochDay = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .toEpochDay()
                            onDateSelected(selectedEpochDay)
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showSourceAccountDialog) {
        AccountPickerDialog(
            title = if (uiState.mode == TransactionType.TRANSFER) "Select Source Account" else "Select Account",
            accounts = uiState.activeAccounts,
            selectedAccountId = uiState.selectedAccount?.id,
            grouping = uiState.numberGrouping,
            onAccountSelected = { acc ->
                onSelectSourceAccount(acc.id)
                showSourceAccountDialog = false
            },
            onDismiss = { showSourceAccountDialog = false }
        )
    }

    if (showDestAccountDialog) {
        AccountPickerDialog(
            title = "Select Destination Account",
            accounts = uiState.activeAccounts.filter { it.id != uiState.selectedAccount?.id },
            selectedAccountId = uiState.transferToAccount?.id,
            grouping = uiState.numberGrouping,
            onAccountSelected = { acc ->
                onSelectDestinationAccount(acc.id)
                showDestAccountDialog = false
            },
            onDismiss = { showDestAccountDialog = false }
        )
    }

    if (showCategorySheet) {
        CategoryPickerBottomSheet(
            categories = uiState.activeCategoryTree,
            selectedCategoryId = uiState.selectedCategoryId,
            subcategoriesEnabled = uiState.subcategoriesEnabled,
            categoryType = if (uiState.mode == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE,
            onCategorySelected = { category, parentName ->
                val targetSplitId = splitLineIdForCategoryPicker
                if (targetSplitId != null) {
                    onUpdateSplitCategory(targetSplitId, category)
                } else {
                    onSelectCategory(category, parentName)
                }
                splitLineIdForCategoryPicker = null
                showCategorySheet = false
            },
            onCreateNewCategory = { name, parentId, parentName, iconName, colorHex, onCreated ->
                onCreateCategory(name, parentId, parentName, iconName, colorHex, splitLineIdForCategoryPicker) { createdNode ->
                    onCreated(createdNode)
                }
            },
            onDismiss = {
                splitLineIdForCategoryPicker = null
                showCategorySheet = false
            }
        )
    }

    if (showBookmarksDialog) {
        BookmarksTemplateDialog(
            bookmarks = uiState.bookmarks,
            currencySpec = uiState.currencySpec,
            grouping = uiState.numberGrouping,
            onSelectBookmark = { bm ->
                onApplyBookmark(bm)
                showBookmarksDialog = false
            },
            onSaveCurrentAsBookmark = { title ->
                onSaveBookmark(title)
            },
            onDeleteBookmark = onDeleteBookmark,
            onDismiss = { showBookmarksDialog = false }
        )
    }

    if (showNewTagDialog) {
        var newTagText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewTagDialog = false },
            title = { Text("Create Tag") },
            text = {
                OutlinedTextField(
                    value = newTagText,
                    onValueChange = { newTagText = it },
                    label = { Text("Tag name (e.g. vacation, tax)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCreateNewTag(newTagText)
                        showNewTagDialog = false
                    }
                ) {
                    Text("Add Tag")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTagDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SplitTransactionEditorCard(
    uiState: TransactionEntryUiState,
    onAddSplitLine: () -> Unit,
    onPickSplitCategory: (Long) -> Unit,
    onUpdateSplitAmount: (Long, String) -> Unit,
    onAssignRemainingToSplit: (Long) -> Unit,
    onRemoveSplitLine: (Long) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val validation = uiState.splitValidation

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (validation.isValid) {
                semanticColors.incomeEmerald
            } else {
                semanticColors.warningAmber
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Split Transaction Breakdown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (validation.isValid) "Balanced · Remaining: " else "Remaining Unallocated: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = validation.remainingUnallocated,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping,
                            colorMode = if (validation.remainingUnallocated.isZero) {
                                MoneyColorMode.INCOME
                            } else {
                                MoneyColorMode.EXPENSE
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onAddSplitLine,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Split", style = MaterialTheme.typography.labelSmall)
                }
            }

            uiState.splitDrafts.forEach { draft ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPickSplitCategory(draft.localId) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            CategoryIconBadge(
                                iconName = draft.categoryIconName,
                                colorHex = draft.categoryColorHex,
                                size = 26.dp,
                                iconSize = 14.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = draft.categoryName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    OutlinedTextField(
                        value = draft.amountInput,
                        onValueChange = { onUpdateSplitAmount(draft.localId, it) },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
                    )

                    if (!validation.remainingUnallocated.isZero) {
                        TextButton(
                            onClick = { onAssignRemainingToSplit(draft.localId) },
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text("Fill", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    IconButton(
                        onClick = { onRemoveSplitLine(draft.localId) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Remove Split Line",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstallmentPlanCard(
    uiState: TransactionEntryUiState,
    onSelectMonths: (Int) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val monthPresets = listOf(3, 6, 9, 12, 18, 24)
    val schedule = uiState.installmentSchedulePreview

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "Credit Card Installment Plan (Inst.)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                monthPresets.forEach { m ->
                    FilterChip(
                        selected = uiState.installmentMonthsCount == m,
                        onClick = { onSelectMonths(m) },
                        label = { Text("${m}M") }
                    )
                }
            }

            if (schedule.isNotEmpty()) {
                val first = schedule.first()
                val second = schedule.getOrNull(1) ?: first
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = "Month 1 (includes rounding remainder)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            MoneyText(
                                money = first.amount,
                                currencySpec = uiState.currencySpec,
                                grouping = uiState.numberGrouping,
                                colorMode = MoneyColorMode.EXPENSE,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Months 2..${schedule.size} (${schedule.size - 1}x)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            MoneyText(
                                money = second.amount,
                                currencySpec = uiState.currencySpec,
                                grouping = uiState.numberGrouping,
                                colorMode = MoneyColorMode.EXPENSE,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorNumericKeypad(
    isSaving: Boolean,
    isEditingExisting: Boolean,
    expressionBuffer: String,
    currencySymbol: String,
    expressionHasOperator: Boolean,
    onKeyPress: (String) -> Unit,
    onHideKeypadClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val rows = listOf(
        listOf("1", "2", "3", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("7", "8", "9", "-"),
        listOf(".", "0", "00", "+")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Compact Top Toolbar Strip above keypad keys with prominent "Hide Keypad ⌄" button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (expressionHasOperator || expressionBuffer != "0") {
                        "Expr: $currencySymbol $expressionBuffer"
                    } else {
                        "Calculator Keypad"
                    },
                    style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onHideKeypadClick)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardHide,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hide Keypad",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Minimize Keypad",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        rows.forEach { rowKeys ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowKeys.forEach { key ->
                    val isOperator = key in listOf("+", "-", "×", "÷")
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOperator) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onKeyPress(key) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = key,
                                style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                                fontWeight = FontWeight.Bold,
                                color = if (isOperator) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action Row: C | ⌫ | = | Save
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .weight(0.8f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onKeyPress("C") }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "C",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = semanticColors.expenseCoral
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .weight(0.8f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onKeyPress("⌫") }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (expressionHasOperator) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                modifier = Modifier
                    .weight(0.8f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onKeyPress("=") }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "=",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (expressionHasOperator) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Button(
                onClick = onSaveClick,
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .weight(1.6f)
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isEditingExisting) "Update" else "Save",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AccountPickerDialog(
    title: String,
    accounts: List<AccountSummary>,
    selectedAccountId: Long?,
    grouping: com.folioledger.app.domain.model.NumberGroupingSystem,
    onAccountSelected: (AccountSummary) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(accounts, key = { it.id }) { account ->
                    val isSelected = account.id == selectedAccountId
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
                            .clickable { onAccountSelected(account) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            CategoryIconBadge(
                                iconName = account.iconName,
                                colorHex = account.colorHex,
                                size = 34.dp,
                                iconSize = 18.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = account.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = account.groupType.name.replace('_', ' '),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            MoneyText(
                                money = account.currentBalance,
                                currencySpec = SupportedCurrencies.fromCode(account.currencyCode),
                                grouping = grouping,
                                colorMode = MoneyColorMode.AUTO_SIGN,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerBottomSheet(
    categories: List<CategoryNode>,
    selectedCategoryId: Long?,
    subcategoriesEnabled: Boolean = true,
    categoryType: CategoryType = CategoryType.EXPENSE,
    onCategorySelected: (CategoryNode, String?) -> Unit,
    onCreateNewCategory: (name: String, parentId: Long?, parentName: String?, iconName: String, colorHex: String, onCreated: (CategoryNode) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var expandedCategoryIds by rememberSaveable { mutableStateOf(setOf<Long>()) }

    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var createParentId by rememberSaveable { mutableStateOf<Long?>(null) }
    var createParentName by rememberSaveable { mutableStateOf<String?>(null) }
    var initialNewCategoryName by rememberSaveable { mutableStateOf("") }

    // Top 6 Frequent categories
    val frequentCategories = remember(categories) {
        val flatList = mutableListOf<Pair<CategoryNode, String?>>()
        categories.take(4).forEach { parent ->
            flatList.add(parent to null)
            parent.subcategories.take(2).forEach { sub ->
                flatList.add(sub to parent.name)
            }
        }
        flatList.take(6)
    }

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
                .fillMaxHeight(0.88f)
                .padding(bottom = 24.dp)
        ) {
            // Header Row: Title & "+ New Category" Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (subcategoriesEnabled) "Select Category & Subcategory" else "Select Category",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (categoryType == CategoryType.EXPENSE) "Expense Categories" else "Income Categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = {
                        createParentId = null
                        createParentName = null
                        initialNewCategoryName = searchQuery
                        showCreateDialog = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Category", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Real-Time Search Field
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

            // ⚡ Frequent Categories Quick-Access Row
            if (frequentCategories.isNotEmpty() && searchQuery.isBlank()) {
                Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                    Text(
                        text = "⚡ FREQUENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(frequentCategories, key = { "${it.first.id}_${it.second}" }) { (node, parentName) ->
                            val isSelected = node.id == selectedCategoryId
                            FilterChip(
                                selected = isSelected,
                                onClick = { onCategorySelected(node, parentName) },
                                leadingIcon = {
                                    CategoryIconBadge(
                                        iconName = node.iconName,
                                        colorHex = node.colorHex,
                                        size = 20.dp,
                                        iconSize = 12.dp
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (parentName != null) "$parentName › ${node.name}" else node.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Fallback Create Card if Search Query has no matches
            if (filteredCategories.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "No categories found matching \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            createParentId = null
                            createParentName = null
                            initialNewCategoryName = searchQuery
                            showCreateDialog = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create \"$searchQuery\" as New Category")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredCategories, key = { it.id }) { parent ->
                        val isSearching = searchQuery.isNotBlank()
                        val isExpanded = isSearching || expandedCategoryIds.contains(parent.id)
                        val isParentSelected = parent.id == selectedCategoryId

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isParentSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                }
                            ),
                            border = BorderStroke(
                                width = if (isParentSelected) 2.dp else 1.dp,
                                color = if (isParentSelected) MaterialTheme.colorScheme.primary else semanticColors.cardStrokeSubtle
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Parent Category Header Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onCategorySelected(parent, null) }
                                        .padding(4.dp)
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

                                    if (isParentSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

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
                                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Expanded Subcategory List
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
                                                    val isSubSelected = sub.id == selectedCategoryId
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(start = 20.dp, top = 2.dp, bottom = 2.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(
                                                                if (isSubSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                                            )
                                                            .clickable { onCategorySelected(sub, parent.name) }
                                                            .padding(horizontal = 10.dp, vertical = 8.dp)
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
                                                            fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                            color = if (isSubSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        if (isSubSelected) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Selected",
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            TextButton(
                                                onClick = {
                                                    createParentId = parent.id
                                                    createParentName = parent.name
                                                    initialNewCategoryName = ""
                                                    showCreateDialog = true
                                                },
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

    if (showCreateDialog) {
        InlineCategoryCreateDialog(
            initialName = initialNewCategoryName,
            parentId = createParentId,
            parentName = createParentName,
            categoryType = categoryType,
            onCreateCategory = { name, parentId, parentName, iconName, colorHex ->
                onCreateNewCategory(name, parentId, parentName, iconName, colorHex) { createdNode ->
                    val targetExpandId = parentId ?: createdNode.id
                    expandedCategoryIds = expandedCategoryIds + targetExpandId
                }
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
private fun InlineCategoryCreateDialog(
    initialName: String,
    parentId: Long?,
    parentName: String?,
    categoryType: CategoryType,
    onCreateCategory: (name: String, parentId: Long?, parentName: String?, iconName: String, colorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    var nameInput by remember { mutableStateOf(initialName) }
    var selectedColorHex by remember {
        mutableStateOf(if (categoryType == CategoryType.EXPENSE) "#E11D48" else "#059669")
    }
    var selectedIconName by remember {
        mutableStateOf(if (categoryType == CategoryType.EXPENSE) "shopping_cart" else "payments")
    }

    val swatches = remember {
        listOf(
            "#059669", "#10B981", "#0D9488", "#0284C7",
            "#2563EB", "#4F46E5", "#7C3AED", "#EC4899",
            "#E11D48", "#F97316", "#EA580C", "#D97706",
            "#334155"
        )
    }

    LaunchedEffect(nameInput) {
        if (nameInput.isNotBlank()) {
            val suggestion = suggestIconAndColorForCategoryName(nameInput, categoryType)
            if (suggestion != null) {
                selectedIconName = suggestion.first
                selectedColorHex = suggestion.second
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIconBadge(
                    iconName = selectedIconName,
                    colorHex = selectedColorHex,
                    size = 38.dp,
                    iconSize = 20.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (parentId != null) "New Subcategory" else "New Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!parentName.isNullOrBlank()) {
                        Text(
                            text = "Under $parentName",
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
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g. Filter Coffee, Swiggy, Fuel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Color Badge",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    swatches.forEach { hex ->
                        val color = parseHexColorSafely(hex)
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColorHex = hex }
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        onCreateCategory(
                            nameInput.trim(),
                            parentId,
                            parentName,
                            selectedIconName,
                            selectedColorHex
                        )
                    }
                },
                enabled = nameInput.isNotBlank()
            ) {
                Text("Create & Select")
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
private fun BookmarksTemplateDialog(
    bookmarks: List<TransactionBookmark>,
    currencySpec: com.folioledger.app.domain.model.CurrencySpec,
    grouping: com.folioledger.app.domain.model.NumberGroupingSystem,
    onSelectBookmark: (TransactionBookmark) -> Unit,
    onSaveCurrentAsBookmark: (String) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var newBookmarkTitle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Favorite Bookmarks (★)",
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
                        value = newBookmarkTitle,
                        onValueChange = { newBookmarkTitle = it },
                        label = { Text("Save current form as template") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newBookmarkTitle.isNotBlank()) {
                                onSaveCurrentAsBookmark(newBookmarkTitle)
                                newBookmarkTitle = ""
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }

                HorizontalDivider(color = LocalFolioSemanticColors.current.cardStrokeSubtle)

                if (bookmarks.isEmpty()) {
                    Text(
                        text = "No saved bookmark templates yet. Fill the form and save frequent transactions here for 1-tap entry.",
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectBookmark(bm) }
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
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (bm.payeeOrDescription.isNotBlank()) {
                                            Text(
                                                text = bm.payeeOrDescription,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                            modifier = Modifier.size(16.dp)
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
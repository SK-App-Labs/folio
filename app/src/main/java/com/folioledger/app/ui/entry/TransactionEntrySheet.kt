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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Star
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
        CategoryTwoLevelPickerDialog(
            categories = uiState.activeCategoryTree,
            selectedCategoryId = uiState.selectedCategoryId,
            subcategoriesEnabled = uiState.subcategoriesEnabled,
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
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "-"),
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

@Composable
private fun CategoryTwoLevelPickerDialog(
    categories: List<CategoryNode>,
    selectedCategoryId: Long?,
    subcategoriesEnabled: Boolean = true,
    onCategorySelected: (CategoryNode, String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (subcategoriesEnabled) "Select Category & Subcategory" else "Select Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories, key = { it.id }) { parent ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, LocalFolioSemanticColors.current.cardStrokeSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
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
                                    size = 34.dp,
                                    iconSize = 18.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = parent.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (parent.id == selectedCategoryId) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            if (subcategoriesEnabled && parent.subcategories.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    parent.subcategories.forEach { sub ->
                                        FilterChip(
                                            selected = sub.id == selectedCategoryId,
                                            onClick = { onCategorySelected(sub, parent.name) },
                                            label = {
                                                Text(
                                                    text = sub.name,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
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
package com.folioledger.app.ui.entry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.fromTransactionType
import com.folioledger.app.ui.entry.components.AccountPickerDialog
import com.folioledger.app.ui.entry.components.BookmarksTemplateDialog
import com.folioledger.app.ui.entry.components.CalculatorNumericKeypad
import com.folioledger.app.ui.entry.components.CategoryPickerBottomSheet
import com.folioledger.app.ui.entry.components.DockedAccountPicker
import com.folioledger.app.ui.entry.components.DockedCategoryPicker
import com.folioledger.app.ui.entry.components.InstallmentPlanCard
import com.folioledger.app.ui.entry.components.SplitTransactionEditorCard
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class LowerDockContent { KEYPAD, ACCOUNT_GRID, CATEGORY_TREE, HIDDEN }

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
        dragHandle = null,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        TransactionEntryFormContent(
            uiState = uiState,
            onModeSelected = viewModel::setMode,
            onStepDateByDays = viewModel::stepLedgerDateByDays,
            onDateSelected = viewModel::setLedgerDateEpochDay,
            onTimeSelected = viewModel::setLedgerTime,
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
            onAmountInputChange = viewModel::updateAmountInput,
            onKeypadKeyPress = viewModel::onKeypadKeyPress,
            onSaveClick = { viewModel.saveTransaction(onSuccess = onTransactionSaved) },
            onSaveAndContinueClick = {
                viewModel.saveTransaction {
                    // Save succeeds -> Keep sheet open and reset form for next rapid entry
                    viewModel.setKeypadVisible(true)
                }
            },
            onCloseClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        )
    }
}

@Composable
fun TransactionEntryFormContent(
    uiState: TransactionEntryUiState,
    onModeSelected: (TransactionType) -> Unit,
    onStepDateByDays: (Long) -> Unit,
    onDateSelected: (Long) -> Unit,
    onTimeSelected: (Int, Int) -> Unit = { _, _ -> },
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
    onAmountInputChange: (String) -> Unit = {},
    onKeypadKeyPress: (String) -> Unit,
    onSaveClick: () -> Unit,
    onSaveAndContinueClick: () -> Unit = onSaveClick,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current
    val amountFocusRequester = remember { FocusRequester() }

    var lowerDockContent by remember { mutableStateOf(LowerDockContent.HIDDEN) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        if (lowerDockContent == LowerDockContent.HIDDEN) {
            amountFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    var showTimePickerDialog by rememberSaveable { mutableStateOf(false) }
    var showSourceAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showDestAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showCategorySheet by rememberSaveable { mutableStateOf(false) }
    var splitLineIdForCategoryPicker by rememberSaveable { mutableStateOf<Long?>(null) }
    var showBookmarksDialog by rememberSaveable { mutableStateOf(false) }
    var showNewTagDialog by rememberSaveable { mutableStateOf(false) }

    val formattedDate = remember(uiState.ledgerDateEpochDay) {
        val date = LocalDate.ofEpochDay(uiState.ledgerDateEpochDay)
        val today = LocalDate.now()
        val pattern = DateTimeFormatter.ofPattern("dd/MM/yy (EEE)", Locale.US)
        when (date) {
            today -> "Today, ${date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.US))}"
            today.minusDays(1) -> "Yesterday, ${date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.US))}"
            else -> date.format(pattern)
        }
    }

    val formattedTime = remember(uiState.timestampEpochMillis) {
        val time = Instant.ofEpochMilli(uiState.timestampEpochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        // UPPER HALF: Form Display, Requirements & Anchored Save Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Top Header: Segmented Mode Tabs + Bookmarks ★ + Close
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
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
                            TransactionType.OPENING_BALANCE, TransactionType.RECONCILIATION -> MaterialTheme.colorScheme.primary
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

            // Unified Date, Time & Split Feature Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
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
                        // Left: Date Stepper
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = { onStepDateByDays(-1L) },
                                modifier = Modifier.size(30.dp)
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
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formattedDate,
                                    style = tabularMoneyStyle(MaterialTheme.typography.labelLarge),
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }

                            IconButton(
                                onClick = { onStepDateByDays(1L) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next Day"
                                )
                            }
                        }

                        // Right: Time Picker Button Chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showTimePickerDialog = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccessTime,
                                    contentDescription = "Select Time",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formattedTime,
                                    style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
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

            Spacer(modifier = Modifier.height(6.dp))

            // Amount & Live Calculator Expression Display Banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        if (lowerDockContent == LowerDockContent.KEYPAD) {
                            lowerDockContent = LowerDockContent.HIDDEN
                        }
                        amountFocusRequester.requestFocus()
                        keyboardController?.show()
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val modePrefix = if (uiState.editingTransactionId > 0L) "EDITING · " else ""
                        Text(
                            text = modePrefix + when (uiState.mode) {
                                TransactionType.EXPENSE -> "EXPENSE AMOUNT"
                                TransactionType.INCOME -> "INCOME AMOUNT"
                                TransactionType.TRANSFER -> "TRANSFER AMOUNT"
                                TransactionType.REFUND -> "REFUND AMOUNT"
                                TransactionType.CC_SETTLEMENT -> "SETTLEMENT AMOUNT"
                                TransactionType.OPENING_BALANCE -> "OPENING BALANCE"
                                TransactionType.RECONCILIATION -> "RECONCILIATION AMOUNT"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.expressionHasOperator || (lowerDockContent == LowerDockContent.KEYPAD && uiState.expressionBuffer != "0")) {
                            Text(
                                text = "${uiState.currencySpec.symbol} ${uiState.expressionBuffer}",
                                style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (lowerDockContent == LowerDockContent.KEYPAD) {
                            MoneyText(
                                money = uiState.evaluatedAmount,
                                currencySpec = uiState.currencySpec,
                                grouping = uiState.numberGrouping,
                                colorMode = MoneyColorMode.fromTransactionType(uiState.mode),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            val amountColor = when (uiState.mode) {
                                TransactionType.EXPENSE -> semanticColors.expenseCoral
                                TransactionType.INCOME, TransactionType.REFUND -> semanticColors.incomeEmerald
                                TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> semanticColors.transferBlue
                                TransactionType.OPENING_BALANCE, TransactionType.RECONCILIATION -> MaterialTheme.colorScheme.primary
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = uiState.currencySpec.symbol,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = amountColor.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                BasicTextField(
                                    value = if (uiState.expressionBuffer == "0") "" else uiState.expressionBuffer,
                                    onValueChange = onAmountInputChange,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            lowerDockContent = LowerDockContent.ACCOUNT_GRID
                                        }
                                    ),
                                    singleLine = true,
                                    textStyle = tabularMoneyStyle(MaterialTheme.typography.headlineMedium).copy(
                                        color = amountColor,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .focusRequester(amountFocusRequester)
                                        .widthIn(min = 40.dp, max = 220.dp),
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.CenterEnd) {
                                            if (uiState.expressionBuffer == "0" || uiState.expressionBuffer.isEmpty()) {
                                                Text(
                                                    text = "0",
                                                    style = tabularMoneyStyle(MaterialTheme.typography.headlineMedium),
                                                    fontWeight = FontWeight.Bold,
                                                    color = amountColor.copy(alpha = 0.35f),
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (lowerDockContent == LowerDockContent.KEYPAD) {
                                    lowerDockContent = LowerDockContent.HIDDEN
                                    amountFocusRequester.requestFocus()
                                    keyboardController?.show()
                                } else {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    lowerDockContent = LowerDockContent.KEYPAD
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Calculate,
                                contentDescription = "Toggle Calculator Keypad",
                                tint = if (lowerDockContent == LowerDockContent.KEYPAD) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        }
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
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // 2-Column Account & Category Cards
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
                            .clickable {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                lowerDockContent = LowerDockContent.ACCOUNT_GRID
                            }
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

                    // Right Selector: Destination Account (if Transfer) OR Category
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
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    splitLineIdForCategoryPicker = null
                                    lowerDockContent = LowerDockContent.CATEGORY_TREE
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
                            .onFocusChanged { if (it.isFocused) lowerDockContent = LowerDockContent.HIDDEN }
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

                // Dynamic Credit Card Installment Plan Section
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
                            .onFocusChanged { if (it.isFocused) lowerDockContent = LowerDockContent.HIDDEN }
                    )

                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = onNoteChange,
                        label = { Text("Note / Memo") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { if (it.isFocused) lowerDockContent = LowerDockContent.HIDDEN }
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

            // Always-Visible Save Action Bar Anchored directly at Bottom of Upper Half
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSaveClick()
                    },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                FilledTonalButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSaveAndContinueClick()
                    },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        HorizontalDivider(color = semanticColors.cardStrokeSubtle)

        // LOWER HALF: Dynamic Dock Container (Keypad / 3-Col Accounts / Split Categories)
        AnimatedVisibility(visible = lowerDockContent != LowerDockContent.HIDDEN) {
            when (lowerDockContent) {
                LowerDockContent.KEYPAD -> {
                    CalculatorNumericKeypad(
                        isSaving = uiState.isSaving,
                        isEditingExisting = uiState.editingTransactionId > 0L,
                        expressionBuffer = uiState.expressionBuffer,
                        currencySymbol = uiState.currencySpec.symbol,
                        expressionHasOperator = uiState.expressionHasOperator,
                        onKeyPress = onKeypadKeyPress,
                        onHideKeypadClick = { lowerDockContent = LowerDockContent.HIDDEN },
                        onNextStepClick = {
                            focusManager.clearFocus()
                            if (uiState.expressionHasOperator) {
                                onKeypadKeyPress("=")
                            }
                            lowerDockContent = LowerDockContent.ACCOUNT_GRID
                        }
                    )
                }

                LowerDockContent.ACCOUNT_GRID -> {
                    DockedAccountPicker(
                        title = if (uiState.mode == TransactionType.TRANSFER) "Select Source Account" else "Select Account",
                        accounts = uiState.activeAccounts,
                        selectedAccountId = uiState.selectedAccount?.id,
                        onAccountSelected = { acc ->
                            onSelectSourceAccount(acc.id)
                            if (uiState.mode != TransactionType.TRANSFER && !uiState.isSplit) {
                                lowerDockContent = LowerDockContent.CATEGORY_TREE
                            } else {
                                lowerDockContent = LowerDockContent.HIDDEN
                            }
                        },
                        onCloseClick = { lowerDockContent = LowerDockContent.HIDDEN }
                    )
                }

                LowerDockContent.CATEGORY_TREE -> {
                    DockedCategoryPicker(
                        categories = uiState.activeCategoryTree,
                        selectedCategoryId = uiState.selectedCategoryId,
                        categoryType = if (uiState.mode == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE,
                        onCategorySelected = { category, parentName ->
                            onSelectCategory(category, parentName)
                            lowerDockContent = LowerDockContent.HIDDEN
                        },
                        onCreateNewCategoryClick = { showCategorySheet = true },
                        onCloseClick = { lowerDockContent = LowerDockContent.HIDDEN }
                    )
                }

                LowerDockContent.HIDDEN -> {}
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

    if (showTimePickerDialog) {
        val initialZone = ZoneId.systemDefault()
        val initialLocalTime = remember(uiState.timestampEpochMillis) {
            Instant.ofEpochMilli(uiState.timestampEpochMillis)
                .atZone(initialZone)
                .toLocalTime()
        }
        val androidContext = LocalContext.current

        DisposableEffect(Unit) {
            val dialog = android.app.TimePickerDialog(
                androidContext,
                { _, hourOfDay, minute ->
                    onTimeSelected(hourOfDay, minute)
                    showTimePickerDialog = false
                },
                initialLocalTime.hour,
                initialLocalTime.minute,
                false // 12-hour AM/PM format
            )
            dialog.setOnDismissListener { showTimePickerDialog = false }
            dialog.show()

            onDispose {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
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

package com.folioledger.app.ui.entry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import com.folioledger.app.data.notification.FolioNotificationManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.ui.entry.components.BookmarksTemplateDialog
import com.folioledger.app.ui.entry.components.CalculatorNumericKeypad
import com.folioledger.app.ui.entry.components.CategoryPickerBottomSheet
import com.folioledger.app.ui.entry.components.CreateTagDialog
import com.folioledger.app.ui.entry.components.DockedAccountPicker
import com.folioledger.app.ui.entry.components.DockedCategoryPicker
import com.folioledger.app.ui.entry.components.EntryAccountAndCategoryCards
import com.folioledger.app.ui.entry.components.EntryAmountDisplayCard
import com.folioledger.app.ui.entry.components.EntryDateAndTimeBar
import com.folioledger.app.ui.entry.components.EntryDatePickerDialog
import com.folioledger.app.ui.entry.components.EntryModeSelectorHeader
import com.folioledger.app.ui.entry.components.EntryRecurringBanner
import com.folioledger.app.ui.entry.components.EntryTimePickerLauncher
import com.folioledger.app.ui.entry.components.InstallmentPlanCard
import com.folioledger.app.ui.entry.components.RecurrenceFrequencyPickerBottomSheet
import com.folioledger.app.ui.entry.components.SplitTransactionEditorCard
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class LowerDockContent { KEYPAD, ACCOUNT_GRID, DEST_ACCOUNT_GRID, CATEGORY_TREE, HIDDEN }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEntrySheet(
    viewModel: TransactionEntryViewModel,
    onDismiss: () -> Unit,
    onTransactionSaved: () -> Unit = onDismiss,
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

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
            onToggleRecurring = viewModel::toggleRecurringMode,
            onSelectRecurringFrequency = viewModel::setRecurringFrequency,
            onDisableRecurring = viewModel::disableRecurringMode,
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
            onSaveClick = {
                val linkedRule = uiState.linkedRecurringRuleId
                viewModel.saveTransaction(onSuccess = {
                    if (linkedRule != null) {
                        FolioNotificationManager.cancelBillNotification(context, linkedRule)
                    }
                    onTransactionSaved()
                })
            },
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
    onToggleRecurring: () -> Unit = {},
    onSelectRecurringFrequency: (RepeatFrequency) -> Unit = {},
    onDisableRecurring: () -> Unit = {},
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

    var lowerDockContent by remember { mutableStateOf(LowerDockContent.KEYPAD) }

    LaunchedEffect(Unit) {
        if (lowerDockContent == LowerDockContent.HIDDEN) {
            amountFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    var showTimePickerDialog by rememberSaveable { mutableStateOf(false) }
    var showRecurrencePicker by rememberSaveable { mutableStateOf(false) }
    var showCategorySheet by rememberSaveable { mutableStateOf(false) }
    var splitLineIdForCategoryPicker by rememberSaveable { mutableStateOf<Long?>(null) }
    var showBookmarksDialog by rememberSaveable { mutableStateOf(false) }
    var showNewTagDialog by rememberSaveable { mutableStateOf(false) }

    var localAmountInput by remember(uiState.editingTransactionId) {
        mutableStateOf(if (uiState.expressionBuffer == "0") "" else uiState.expressionBuffer)
    }
    LaunchedEffect(uiState.expressionBuffer) {
        val expected = if (uiState.expressionBuffer == "0") "" else uiState.expressionBuffer
        if (localAmountInput != expected) {
            localAmountInput = expected
        }
    }

    var localTransferFee by remember(uiState.editingTransactionId) {
        mutableStateOf(uiState.transferFeeInput)
    }
    LaunchedEffect(uiState.transferFeeInput) {
        if (localTransferFee != uiState.transferFeeInput) {
            localTransferFee = uiState.transferFeeInput
        }
    }

    var localPayee by remember(uiState.editingTransactionId) {
        mutableStateOf(uiState.payeeOrDescription)
    }
    LaunchedEffect(uiState.payeeOrDescription) {
        if (localPayee != uiState.payeeOrDescription) {
            localPayee = uiState.payeeOrDescription
        }
    }

    var localNote by remember(uiState.editingTransactionId) {
        mutableStateOf(uiState.note)
    }
    LaunchedEffect(uiState.note) {
        if (localNote != uiState.note) {
            localNote = uiState.note
        }
    }

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
            EntryModeSelectorHeader(
                currentMode = uiState.mode,
                onModeSelected = onModeSelected,
                onOpenBookmarks = { showBookmarksDialog = true },
                onCloseClick = onCloseClick
            )

            // Unified Date, Time & Feature Actions Bar
            EntryDateAndTimeBar(
                formattedDate = formattedDate,
                formattedTime = formattedTime,
                mode = uiState.mode,
                isSplit = uiState.isSplit,
                isRecurring = uiState.isRecurring,
                isInstallmentEnabled = uiState.isInstallmentEnabled,
                isCreditCardExpense = uiState.isCreditCardExpense,
                onStepDateByDays = onStepDateByDays,
                onOpenDatePicker = { showDatePickerDialog = true },
                onOpenTimePicker = { showTimePickerDialog = true },
                onToggleSplit = onToggleSplit,
                onToggleRecurring = {
                    if (uiState.isRecurring) {
                        showRecurrencePicker = true
                    } else {
                        onToggleRecurring()
                        showRecurrencePicker = true
                    }
                },
                onToggleInstallment = onToggleInstallment
            )

            if (uiState.isRecurring) {
                EntryRecurringBanner(
                    recurringFrequency = uiState.recurringFrequency,
                    ledgerDateEpochDay = uiState.ledgerDateEpochDay,
                    onEditRecurrence = { showRecurrencePicker = true },
                    onDisableRecurring = onDisableRecurring
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Amount & Live Calculator Expression Display Banner
            EntryAmountDisplayCard(
                isKeypadActive = lowerDockContent == LowerDockContent.KEYPAD,
                isEditingExisting = uiState.editingTransactionId > 0L,
                mode = uiState.mode,
                evaluatedAmount = uiState.evaluatedAmount,
                expressionBuffer = uiState.expressionBuffer,
                expressionHasOperator = uiState.expressionHasOperator,
                currencySpec = uiState.currencySpec,
                numberGrouping = uiState.numberGrouping,
                amountInput = localAmountInput,
                amountFocusRequester = amountFocusRequester,
                onAmountInputChange = { newInput ->
                    localAmountInput = newInput
                    onAmountInputChange(newInput)
                },
                onCardClick = {
                    if (lowerDockContent == LowerDockContent.KEYPAD) {
                        lowerDockContent = LowerDockContent.HIDDEN
                    }
                    amountFocusRequester.requestFocus()
                    keyboardController?.show()
                },
                onToggleKeypadClick = {
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
                onNextImeAction = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    lowerDockContent = LowerDockContent.ACCOUNT_GRID
                }
            )

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
                EntryAccountAndCategoryCards(
                    mode = uiState.mode,
                    isSplit = uiState.isSplit,
                    selectedAccount = uiState.selectedAccount,
                    transferToAccount = uiState.transferToAccount,
                    selectedCategoryName = uiState.selectedCategoryName,
                    selectedParentCategoryName = uiState.selectedParentCategoryName,
                    selectedCategoryIconName = uiState.selectedCategoryIconName,
                    selectedCategoryColorHex = uiState.selectedCategoryColorHex,
                    onPickSourceAccount = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        lowerDockContent = LowerDockContent.ACCOUNT_GRID
                    },
                    onPickDestinationAccount = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        lowerDockContent = LowerDockContent.DEST_ACCOUNT_GRID
                    },
                    onPickCategory = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        splitLineIdForCategoryPicker = null
                        lowerDockContent = LowerDockContent.CATEGORY_TREE
                    }
                )

                // Optional Transfer Fee Input
                AnimatedVisibility(visible = uiState.mode == TransactionType.TRANSFER) {
                    OutlinedTextField(
                        value = localTransferFee,
                        onValueChange = { newInput ->
                            localTransferFee = newInput
                            onTransferFeeChange(newInput)
                        },
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
                        value = localPayee,
                        onValueChange = { newInput ->
                            localPayee = newInput
                            onPayeeChange(newInput)
                        },
                        label = { Text("Payee / Description") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { if (it.isFocused) lowerDockContent = LowerDockContent.HIDDEN }
                    )

                    OutlinedTextField(
                        value = localNote,
                        onValueChange = { newInput ->
                            localNote = newInput
                            onNoteChange(newInput)
                        },
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
                            if (uiState.mode == TransactionType.TRANSFER) {
                                lowerDockContent = LowerDockContent.DEST_ACCOUNT_GRID
                            } else if (!uiState.isSplit) {
                                lowerDockContent = LowerDockContent.CATEGORY_TREE
                            } else {
                                lowerDockContent = LowerDockContent.HIDDEN
                            }
                        },
                        onCloseClick = { lowerDockContent = LowerDockContent.HIDDEN }
                    )
                }

                LowerDockContent.DEST_ACCOUNT_GRID -> {
                    DockedAccountPicker(
                        title = "Select Destination Account",
                        accounts = uiState.activeAccounts.filter { it.id != uiState.selectedAccount?.id },
                        selectedAccountId = uiState.transferToAccount?.id,
                        onAccountSelected = { acc ->
                            onSelectDestinationAccount(acc.id)
                            lowerDockContent = LowerDockContent.HIDDEN
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
        EntryDatePickerDialog(
            initialEpochDay = uiState.ledgerDateEpochDay,
            onDateSelected = onDateSelected,
            onDismiss = { showDatePickerDialog = false }
        )
    }

    if (showTimePickerDialog) {
        EntryTimePickerLauncher(
            initialEpochMillis = uiState.timestampEpochMillis,
            onTimeSelected = onTimeSelected,
            onDismiss = { showTimePickerDialog = false }
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
        CreateTagDialog(
            onTagCreated = onCreateNewTag,
            onDismiss = { showNewTagDialog = false }
        )
    }

    if (showRecurrencePicker) {
        RecurrenceFrequencyPickerBottomSheet(
            selectedFrequency = uiState.recurringFrequency,
            isRecurringActive = uiState.isRecurring,
            baseDate = LocalDate.ofEpochDay(uiState.ledgerDateEpochDay),
            onSelectFrequency = { freq ->
                onSelectRecurringFrequency(freq)
            },
            onDisableRecurring = {
                onDisableRecurring()
            },
            onDismiss = { showRecurrencePicker = false }
        )
    }
}

package com.folioledger.app.ui.accounts.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import com.folioledger.app.ui.theme.tabularMoneyStyle
import com.folioledger.app.ui.accounts.parseInputToMinorOrZero
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.ui.accounts.AccountEditorDialogState
import com.folioledger.app.ui.accounts.AccountsViewModel
import com.folioledger.app.ui.accounts.CreditCardCycleCalculator
import com.folioledger.app.ui.accounts.components.formatAccountGroupTitle
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun AccountEditorBottomSheet(
    state: AccountEditorDialogState,
    referenceDateEpochDay: Long,
    parentBankCandidates: List<AccountSummary>,
    isSaving: Boolean,
    onUpdateState: ((AccountEditorDialogState) -> AccountEditorDialogState) -> Unit,
    onSave: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current

    var showPaymentDueDayPicker by rememberSaveable { mutableStateOf(false) }
    var isForeignCurrencyExpanded by rememberSaveable { mutableStateOf(state.currencyCode != "INR") }
    var isAppearanceCustomizerExpanded by rememberSaveable { mutableStateOf(false) }

    var selectedSegmentIndex by rememberSaveable {
        mutableIntStateOf(
            when (state.groupType) {
                AccountGroup.CASH, AccountGroup.BANK_ACCOUNT, AccountGroup.PREPAID_WALLET -> 0
                AccountGroup.DEBIT_CARD, AccountGroup.CREDIT_CARD -> 1
                else -> 2
            }
        )
    }

    val swatchPresets = remember {
        listOf("#059669", "#2563EB", "#4F46E5", "#D97706", "#E11D48", "#0D9488", "#7C3AED", "#334155")
    }
    val iconPresets = remember {
        listOf(
            "account_balance",
            "account_balance_wallet",
            "credit_card",
            "savings",
            "trending_up",
            "payments",
            "security",
            "work"
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(
                        iconName = state.iconName,
                        colorHex = state.colorHex,
                        size = 40.dp,
                        iconSize = 20.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (state.isEditing) "Edit Account" else "New Account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close"
                    )
                }
            }

            if (!state.validationError.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = semanticColors.expenseCoralContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.validationError,
                        style = MaterialTheme.typography.labelMedium,
                        color = semanticColors.onExpenseCoralContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // 1. Account Name
            OutlinedTextField(
                value = state.name,
                onValueChange = { newName -> onUpdateState { it.copy(name = newName) } },
                label = { Text("Account Name") },
                placeholder = { Text("e.g., HDFC Regalia CC, ICICI Salary A/c") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 2. Opening Balance / Credit Limit (Moved directly below Account Name!)
            if (state.groupType == AccountGroup.CREDIT_CARD) {
                OutlinedTextField(
                    value = state.creditLimitInput,
                    onValueChange = { v -> onUpdateState { it.copy(creditLimitInput = v) } },
                    label = { Text("Credit Limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (state.isEditing) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (state.groupType.isLiabilityByDefault) "Current Outstanding Balance" else "Current Live Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${state.currencyCode} ${state.openingBalanceInput}",
                            style = tabularMoneyStyle(MaterialTheme.typography.titleLarge),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "To adjust or reconcile this balance against your bank statement, use the Reconcile tool in the Passbook.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = state.openingBalanceInput,
                    onValueChange = { v -> onUpdateState { it.copy(openingBalanceInput = v) } },
                    label = {
                        Text(
                            if (state.groupType.isLiabilityByDefault) {
                                "Starting Outstanding Balance"
                            } else {
                                "Starting Balance"
                            }
                        )
                    },
                    supportingText = {
                        Text(
                            if (state.groupType.isLiabilityByDefault) {
                                "Initial debt as of the starting date"
                            } else {
                                "Initial starting balance as of the starting date"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                var showDatePickerDialog by remember { mutableStateOf(false) }
                val formattedAnchorDate = remember(state.openingDateEpochDay) {
                    LocalDate.ofEpochDay(state.openingDateEpochDay)
                        .format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US))
                }

                OutlinedCard(
                    onClick = { showDatePickerDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Starting Balance Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formattedAnchorDate,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = "Select starting date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (showDatePickerDialog) {
                    val initialMillis = remember(state.openingDateEpochDay) {
                        LocalDate.ofEpochDay(state.openingDateEpochDay)
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
                                        onUpdateState { it.copy(openingDateEpochDay = selectedEpochDay) }
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
            }

            // 3. Segmented Account Group Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Account Group",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                PrimaryTabRow(
                    selectedTabIndex = selectedSegmentIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedSegmentIndex == 0,
                        onClick = { selectedSegmentIndex = 0 },
                        text = { Text("Liquid", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedSegmentIndex == 1,
                        onClick = { selectedSegmentIndex = 1 },
                        text = { Text("Cards", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedSegmentIndex == 2,
                        onClick = { selectedSegmentIndex = 2 },
                        text = { Text("Savings & Debt", fontWeight = FontWeight.Bold) }
                    )
                }

                val activeGroups = when (selectedSegmentIndex) {
                    0 -> listOf(AccountGroup.CASH, AccountGroup.BANK_ACCOUNT, AccountGroup.PREPAID_WALLET)
                    1 -> listOf(AccountGroup.DEBIT_CARD, AccountGroup.CREDIT_CARD)
                    else -> listOf(
                        AccountGroup.SAVINGS,
                        AccountGroup.INVESTMENT,
                        AccountGroup.OVERDRAFT,
                        AccountGroup.LOAN,
                        AccountGroup.OTHER
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    activeGroups.forEach { group ->
                        FilterChip(
                            selected = state.groupType == group,
                            onClick = {
                                onUpdateState { current ->
                                    val updatedAnchorEpochDay = if (group == AccountGroup.CREDIT_CARD) {
                                        CreditCardCycleCalculator.computeCycleWindow(
                                            referenceDateEpochDay = referenceDateEpochDay,
                                            settlementDayOfMonth = current.settlementDayOfMonth,
                                            paymentDueDayOfMonth = current.paymentDueDayOfMonth
                                        ).lastStatementEnd.toEpochDay()
                                    } else if (current.groupType == AccountGroup.CREDIT_CARD) {
                                        LocalDate.now().toEpochDay()
                                    } else {
                                        current.openingDateEpochDay
                                    }

                                    current.copy(
                                        groupType = group,
                                        openingDateEpochDay = updatedAnchorEpochDay,
                                        iconName = AccountsViewModel.defaultIconForGroup(group),
                                        colorHex = AccountsViewModel.defaultColorForGroup(group),
                                        parentAccountId = if (group == AccountGroup.DEBIT_CARD) {
                                            current.parentAccountId ?: parentBankCandidates.firstOrNull()?.id
                                        } else {
                                            null
                                        }
                                    )
                                }
                            },
                            label = { Text(formatAccountGroupTitle(group)) }
                        )
                    }
                }
            }

            // 4. Group-Specific Fields
            when (state.groupType) {
                AccountGroup.DEBIT_CARD -> {
                    Text(
                        text = "Linked Parent Bank Account",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (parentBankCandidates.isEmpty()) {
                        Text(
                            text = "No Bank or Savings account found. Create a Bank Account first to link a Debit Card.",
                            style = MaterialTheme.typography.bodySmall,
                            color = semanticColors.expenseCoral
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            parentBankCandidates.forEach { candidate ->
                                FilterChip(
                                    selected = state.parentAccountId == candidate.id,
                                    onClick = {
                                        onUpdateState { it.copy(parentAccountId = candidate.id) }
                                    },
                                    label = { Text(candidate.name) }
                                )
                            }
                        }
                    }
                }

                AccountGroup.CREDIT_CARD -> {
                    val liveCycleWindow = remember(
                        referenceDateEpochDay,
                        state.settlementDayOfMonth,
                        state.paymentDueDayOfMonth
                    ) {
                        CreditCardCycleCalculator.computeCycleWindow(
                            referenceDateEpochDay = referenceDateEpochDay,
                            settlementDayOfMonth = state.settlementDayOfMonth,
                            paymentDueDayOfMonth = state.paymentDueDayOfMonth
                        )
                    }

                    val statementAnchorDate = remember(state.openingDateEpochDay, liveCycleWindow.lastStatementEnd) {
                        if (state.openingDateEpochDay > 0L) {
                            LocalDate.ofEpochDay(state.openingDateEpochDay)
                        } else {
                            liveCycleWindow.lastStatementEnd
                        }
                    }

                    var showStatementDatePickerDialog by remember { mutableStateOf(false) }

                    val closedCycleText = remember(liveCycleWindow.lastStatementStart, liveCycleWindow.lastStatementEnd) {
                        val fmt = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
                        "${liveCycleWindow.lastStatementStart.format(fmt)} – ${liveCycleWindow.lastStatementEnd.format(fmt)}"
                    }
                    val activeCycleText = remember(liveCycleWindow.activeCycleStart, liveCycleWindow.activeCycleEnd) {
                        val fmt = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
                        "${liveCycleWindow.activeCycleStart.format(fmt)} – ${liveCycleWindow.activeCycleEnd.format(fmt)}"
                    }

                    // 1. BILLING SCHEDULE CARDS (Statement Date & Payment Due Date)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Statement Date Card (Unified Calendar DatePicker)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showStatementDatePickerDialog = true }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Statement Date",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Day ${state.settlementDayOfMonth}",
                                    style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Payment Due Day Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showPaymentDueDayPicker = true }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Payment Due Day",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Day ${state.paymentDueDayOfMonth}",
                                    style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Statement Anchor Date Picker
                    if (showStatementDatePickerDialog) {
                        val initialMillis = remember(statementAnchorDate) {
                            statementAnchorDate
                                .atStartOfDay(ZoneOffset.UTC)
                                .toInstant()
                                .toEpochMilli()
                        }
                        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
                        DatePickerDialog(
                            onDismissRequest = { showStatementDatePickerDialog = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val selectedMillis = datePickerState.selectedDateMillis
                                        if (selectedMillis != null) {
                                            val selectedDate = Instant.ofEpochMilli(selectedMillis)
                                                .atZone(ZoneOffset.UTC)
                                                .toLocalDate()
                                            onUpdateState {
                                                it.copy(
                                                    openingDateEpochDay = selectedDate.toEpochDay(),
                                                    settlementDayOfMonth = selectedDate.dayOfMonth
                                                )
                                            }
                                        }
                                        showStatementDatePickerDialog = false
                                    }
                                ) {
                                    Text("Confirm")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showStatementDatePickerDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    // 2. COMPACT BILLING CYCLE DATE RANGE PREVIEW
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Closed Cycle (Current Due)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = closedCycleText,
                                    style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Active Cycle (Unbilled)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = activeCycleText,
                                    style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (state.settlementDayOfMonth == state.paymentDueDayOfMonth) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = semanticColors.warningAmberContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = semanticColors.warningAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Statement closing day and payment due day are set to the same day.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = semanticColors.warningAmber
                                )
                            }
                        }
                    }

                    // 3. SIDE-BY-SIDE STARTING DEBT FIELDS
                    if (!state.isEditing) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = state.statementPayableInput,
                            onValueChange = { v -> onUpdateState { it.copy(statementPayableInput = v) } },
                            label = { Text("Current Due") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = state.unbilledAmountInput,
                            onValueChange = { v -> onUpdateState { it.copy(unbilledAmountInput = v) } },
                            label = { Text("Unbilled Spend") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Live Ledger Balances",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Billed Statement Due",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${state.currencyCode} ${state.statementPayableInput}",
                                            style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Active Unbilled Spend",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${state.currencyCode} ${state.unbilledAmountInput}",
                                            style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Text(
                                    text = "Statement Due and Unbilled Spend are calculated dynamically from your transactions. To adjust balances, use Reconcile in the Passbook.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 4. COMPACT SUMMARY CARD
                    val activeSpec = remember(state.currencyCode) {
                        SupportedCurrencies.fromCode(state.currencyCode)
                    }
                    val payableMinor = remember(state.statementPayableInput, activeSpec.fractionDigits) {
                        parseInputToMinorOrZero(state.statementPayableInput, activeSpec.fractionDigits) ?: MinorMoney.ZERO
                    }
                    val unbilledMinor = remember(state.unbilledAmountInput, activeSpec.fractionDigits) {
                        parseInputToMinorOrZero(state.unbilledAmountInput, activeSpec.fractionDigits) ?: MinorMoney.ZERO
                    }
                    val totalDebtMinor = remember(payableMinor, unbilledMinor) {
                        payableMinor + unbilledMinor
                    }
                    val limitMinor = remember(state.creditLimitInput, activeSpec.fractionDigits) {
                        parseInputToMinorOrZero(state.creditLimitInput, activeSpec.fractionDigits) ?: MinorMoney.ZERO
                    }
                    val availableMinor = remember(limitMinor, totalDebtMinor) {
                        if (limitMinor > totalDebtMinor) limitMinor - totalDebtMinor else MinorMoney.ZERO
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Total Outstanding Debt",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = -totalDebtMinor,
                                    currencySpec = activeSpec,
                                    colorMode = if (totalDebtMinor.isPositive) MoneyColorMode.EXPENSE else MoneyColorMode.NEUTRAL,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Available Credit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = availableMinor,
                                    currencySpec = activeSpec,
                                    colorMode = MoneyColorMode.INCOME,
                                    style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 4. LIVE BILLING CYCLE PREVIEW
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Live Billing Cycle Preview",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "• Current Active Billing Cycle: ${liveCycleWindow.activeCycleRangeFullText} (New expenses in this window count as Unbilled Spend).",
                                style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "• Last Closed Statement: ${liveCycleWindow.lastStatementRangeFullText} (Payable by ${liveCycleWindow.lastStatementDueFullText}).",
                                style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                else -> {}
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            // 5. Collapsible Foreign Currency Selector
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isForeignCurrencyExpanded = !isForeignCurrencyExpanded }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Currency: ${state.currencyCode}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = if (isForeignCurrencyExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isForeignCurrencyExpanded) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SupportedCurrencies.ALL.forEach { spec ->
                                    FilterChip(
                                        selected = state.currencyCode == spec.code,
                                        onClick = { onUpdateState { it.copy(currencyCode = spec.code) } },
                                        label = { Text("${spec.symbol} ${spec.code}") }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Collapsible Appearance Customizer (Badge Color & Icon)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isAppearanceCustomizerExpanded = !isAppearanceCustomizerExpanded }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Customize Icon & Badge Color",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = if (isAppearanceCustomizerExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isAppearanceCustomizerExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                swatchPresets.forEach { hex ->
                                    val swatch = parseHexColorSafely(hex)
                                    val selected = state.colorHex.equals(hex, ignoreCase = true)
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(swatch)
                                            .clickable { onUpdateState { it.copy(colorHex = hex) } }
                                    ) {
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                iconPresets.forEach { iconKey ->
                                    val isSelected = state.iconName == iconKey
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainer
                                        },
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                semanticColors.cardStrokeSubtle
                                            }
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onUpdateState { it.copy(iconName = iconKey) } }
                                            .padding(8.dp)
                                    ) {
                                        CategoryIconBadge(
                                            iconName = iconKey,
                                            colorHex = state.colorHex,
                                            size = 32.dp,
                                            iconSize = 18.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Net Worth Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Include in Net Worth Overview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Counts this account towards total Assets or Liabilities",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.includeInNetWorth,
                    onCheckedChange = { checked ->
                        onUpdateState { it.copy(includeInNetWorth = checked) }
                    }
                )
            }

            // 8. Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isEditing) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = semanticColors.expenseCoral
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onToggleArchive,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (state.isArchived) {
                                Icons.Filled.Unarchive
                            } else {
                                Icons.Filled.Archive
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (state.isArchived) "Unarchive" else "Archive")
                    }
                }

                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isEditing) "Save Changes" else "Create Account")
                }
            }
        }
    }

    if (showPaymentDueDayPicker) {
        DayOfMonthPickerDialog(
            title = "Select Payment Due Day (1–31)",
            subtitle = "The day of the month your billed credit card statement is due for payment.",
            selectedDay = state.paymentDueDayOfMonth,
            onSelectDay = { day ->
                onUpdateState { it.copy(paymentDueDayOfMonth = day.coerceIn(1, 31)) }
                showPaymentDueDayPicker = false
            },
            onDismiss = { showPaymentDueDayPicker = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayOfMonthPickerDialog(
    title: String,
    subtitle: String,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
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
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = subtitle,
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
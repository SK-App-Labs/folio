package com.folioledger.app.ui.accounts.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.SecondaryCurrency
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
import com.folioledger.app.ui.accounts.components.AccountAppearanceCustomizerCard
import com.folioledger.app.ui.accounts.components.AccountCurrencyPickerCard
import com.folioledger.app.ui.accounts.components.AccountTypeQuickPickTile
import com.folioledger.app.ui.accounts.components.CreditCardBillingCycleSection
import com.folioledger.app.ui.accounts.dialogs.DayOfMonthPickerDialog
import com.folioledger.app.ui.theme.tabularMoneyStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun AccountEditorBottomSheet(
    state: AccountEditorDialogState,
    referenceDateEpochDay: Long,
    parentBankCandidates: List<AccountSummary>,
    primaryCurrencyCode: String = "INR",
    secondaryCurrencies: List<SecondaryCurrency> = emptyList(),
    isSaving: Boolean,
    onUpdateState: ((AccountEditorDialogState) -> AccountEditorDialogState) -> Unit,
    onSave: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current

    var isForeignCurrencyExpanded by rememberSaveable { mutableStateOf(state.currencyCode != "INR") }
    var isAppearanceCustomizerExpanded by rememberSaveable { mutableStateOf(false) }

    var localName by rememberSaveable(state.accountId) { mutableStateOf(state.name) }
    LaunchedEffect(state.name) {
        if (state.name != localName) {
            localName = state.name
        }
    }

    var localCreditLimit by rememberSaveable(state.accountId) { mutableStateOf(state.creditLimitInput) }
    LaunchedEffect(state.creditLimitInput) {
        if (state.creditLimitInput != localCreditLimit) {
            localCreditLimit = state.creditLimitInput
        }
    }


    var localOpeningBalance by rememberSaveable(state.accountId) { mutableStateOf(state.openingBalanceInput) }
    LaunchedEffect(state.openingBalanceInput) {
        if (state.openingBalanceInput != localOpeningBalance) {
            localOpeningBalance = state.openingBalanceInput
        }
    }

    val top4Groups = remember {
        listOf(
            AccountGroup.BANK_ACCOUNT,
            AccountGroup.CREDIT_CARD,
            AccountGroup.CASH,
            AccountGroup.DEBIT_CARD
        )
    }
    val secondaryGroups = remember {
        listOf(
            AccountGroup.SAVINGS,
            AccountGroup.PREPAID_WALLET,
            AccountGroup.INVESTMENT,
            AccountGroup.OVERDRAFT,
            AccountGroup.LOAN,
            AccountGroup.OTHER
        )
    }
    var isMoreTypesExpanded by rememberSaveable {
        mutableStateOf(secondaryGroups.contains(state.groupType))
    }
    val haptic = LocalHapticFeedback.current

    val handleGroupSelection: (AccountGroup) -> Unit = { group ->
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
                value = localName,
                onValueChange = { newName ->
                    localName = newName
                    onUpdateState { it.copy(name = newName) }
                },
                label = { Text("Account Name") },
                placeholder = { Text("e.g., HDFC Regalia CC, ICICI Salary A/c") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 2. Account Group Selector (Option A - Smart Quick-Pick 2×2 Grid + More Types Expander)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Account Type",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (top4Groups.contains(state.groupType)) "1-Tap Quick Pick" else "Custom Type",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Top 4 Quick-Pick 2x2 Grid
                top4Groups.chunked(2).forEach { rowPair ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowPair.forEach { group ->
                            AccountTypeQuickPickTile(
                                group = group,
                                isSelected = state.groupType == group,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    handleGroupSelection(group)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Collapsible More Types Expander
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isMoreTypesExpanded = !isMoreTypesExpanded
                        }
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isMoreTypesExpanded) {
                                "Show Less Types"
                            } else {
                                "More Types (Savings, Loan, Investment...)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isMoreTypesExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = isMoreTypesExpanded) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        secondaryGroups.chunked(2).forEach { rowPair ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowPair.forEach { group ->
                                    AccountTypeQuickPickTile(
                                        group = group,
                                        isSelected = state.groupType == group,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            handleGroupSelection(group)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowPair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // 3. Adaptive Account Details (Credit Card, Debit Card, or Standard Balance)
            when (state.groupType) {
                AccountGroup.CREDIT_CARD -> {
                    // Credit Limit Field
                    OutlinedTextField(
                        value = localCreditLimit,
                        onValueChange = { v ->
                            localCreditLimit = v
                            onUpdateState { it.copy(creditLimitInput = v) }
                        },
                        label = { Text("Credit Limit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    CreditCardBillingCycleSection(
                        state = state,
                        referenceDateEpochDay = referenceDateEpochDay,
                        onUpdateState = onUpdateState
                    )
                }

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

                else -> {
                    if (state.isEditing) {
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
                            value = localOpeningBalance,
                            onValueChange = { v ->
                                localOpeningBalance = v
                                onUpdateState { it.copy(openingBalanceInput = v) }
                            },
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
                }
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            // 5. Active Currency Selector
            AccountCurrencyPickerCard(
                currencyCode = state.currencyCode,
                primaryCurrencyCode = primaryCurrencyCode,
                secondaryCurrencies = secondaryCurrencies,
                isExpanded = isForeignCurrencyExpanded,
                onToggleExpanded = { isForeignCurrencyExpanded = !isForeignCurrencyExpanded },
                onSelectCurrency = { code -> onUpdateState { it.copy(currencyCode = code) } }
            )

            // 6. Collapsible Appearance Customizer (Badge Color & Icon)
            AccountAppearanceCustomizerCard(
                colorHex = state.colorHex,
                iconName = state.iconName,
                isExpanded = isAppearanceCustomizerExpanded,
                onToggleExpanded = { isAppearanceCustomizerExpanded = !isAppearanceCustomizerExpanded },
                onSelectColor = { hex -> onUpdateState { it.copy(colorHex = hex) } },
                onSelectIcon = { iconKey -> onUpdateState { it.copy(iconName = iconKey) } }
            )

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

            // Pin to Top (Primary Account)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pin Account to Top",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Prioritize at the top of your accounts list and the quick transaction bar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.isPinned,
                    onCheckedChange = { checked ->
                        onUpdateState { it.copy(isPinned = checked) }
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

}

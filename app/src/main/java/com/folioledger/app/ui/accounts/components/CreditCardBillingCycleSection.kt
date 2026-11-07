package com.folioledger.app.ui.accounts.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.util.CreditCardCycleCalculator
import com.folioledger.app.ui.accounts.AccountEditorDialogState
import com.folioledger.app.ui.accounts.dialogs.DayOfMonthPickerDialog
import com.folioledger.app.ui.accounts.parseInputToMinorOrZero
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.theme.tabularMoneyStyle
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardBillingCycleSection(
    state: AccountEditorDialogState,
    referenceDateEpochDay: Long,
    onUpdateState: ((AccountEditorDialogState) -> AccountEditorDialogState) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    var showPaymentDueDayPicker by rememberSaveable { mutableStateOf(false) }

    var localStatementPayable by rememberSaveable(state.accountId) { mutableStateOf(state.statementPayableInput) }
    LaunchedEffect(state.statementPayableInput) {
        if (state.statementPayableInput != localStatementPayable) {
            localStatementPayable = state.statementPayableInput
        }
    }

    var localUnbilledAmount by rememberSaveable(state.accountId) { mutableStateOf(state.unbilledAmountInput) }
    LaunchedEffect(state.unbilledAmountInput) {
        if (state.unbilledAmountInput != localUnbilledAmount) {
            localUnbilledAmount = state.unbilledAmountInput
        }
    }

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

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
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
                    value = localStatementPayable,
                    onValueChange = { v ->
                        localStatementPayable = v
                        onUpdateState { it.copy(statementPayableInput = v) }
                    },
                    label = { Text("Current Due") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = localUnbilledAmount,
                    onValueChange = { v ->
                        localUnbilledAmount = v
                        onUpdateState { it.copy(unbilledAmountInput = v) }
                    },
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

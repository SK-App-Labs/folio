package com.folioledger.app.ui.accounts.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.ui.accounts.CreditCardSettlementSheetState
import com.folioledger.app.ui.accounts.SettlementAmountPreset
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun CreditCardSettlementBottomSheet(
    state: CreditCardSettlementSheetState,
    assetAccounts: List<AccountSummary>,
    grouping: NumberGroupingSystem,
    isSaving: Boolean,
    onSelectPreset: (SettlementAmountPreset) -> Unit,
    onUpdateState: ((CreditCardSettlementSheetState) -> CreditCardSettlementSheetState) -> Unit,
    onConfirmSettlement: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cardSpec = remember(state.creditCardAccount.currencyCode) {
        SupportedCurrencies.getByCode(state.creditCardAccount.currencyCode)
            ?: SupportedCurrencies.fromCode(state.creditCardAccount.currencyCode)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(
                        iconName = "credit_card",
                        colorHex = state.creditCardAccount.colorHex,
                        size = 40.dp,
                        iconSize = 20.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Settle Credit Card Bill",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.creditCardAccount.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                }
            }

            if (!state.validationError.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LocalFolioSemanticColors.current.expenseCoralContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.validationError,
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalFolioSemanticColors.current.onExpenseCoralContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, LocalFolioSemanticColors.current.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Statement Payable",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = state.creditCardAccount.creditCardStatementPayable ?: MinorMoney.ZERO,
                            currencySpec = cardSpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Outstanding Debt",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = state.creditCardAccount.currentBalance.abs(),
                            currencySpec = cardSpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.NEUTRAL,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "Settlement Amount Option",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = state.preset == SettlementAmountPreset.STATEMENT_PAYABLE,
                    onClick = { onSelectPreset(SettlementAmountPreset.STATEMENT_PAYABLE) },
                    label = { Text("Statement Due") }
                )
                FilterChip(
                    selected = state.preset == SettlementAmountPreset.FULL_OUTSTANDING,
                    onClick = { onSelectPreset(SettlementAmountPreset.FULL_OUTSTANDING) },
                    label = { Text("Full Debt") }
                )
                FilterChip(
                    selected = state.preset == SettlementAmountPreset.CUSTOM,
                    onClick = { onSelectPreset(SettlementAmountPreset.CUSTOM) },
                    label = { Text("Custom") }
                )
            }

            OutlinedTextField(
                value = state.amountInput,
                onValueChange = { v ->
                    onUpdateState {
                        it.copy(preset = SettlementAmountPreset.CUSTOM, amountInput = v)
                    }
                },
                label = { Text("Settlement Amount (${cardSpec.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            var showDatePickerDialog by remember { mutableStateOf(false) }
            val formattedDate = remember(state.ledgerDateEpochDay) {
                LocalDate.ofEpochDay(state.ledgerDateEpochDay)
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
                            text = "Settlement Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Select settlement date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showDatePickerDialog) {
                val initialMillis = remember(state.ledgerDateEpochDay) {
                    LocalDate.ofEpochDay(state.ledgerDateEpochDay)
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
                                    onUpdateState { it.copy(ledgerDateEpochDay = selectedEpochDay) }
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

            val eligibleAssets = remember(assetAccounts, state.creditCardAccount.currencyCode) {
                assetAccounts.filter { it.currencyCode == state.creditCardAccount.currencyCode }
            }

            Text(
                text = "Pay From Asset Account",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            if (eligibleAssets.isEmpty()) {
                Text(
                    text = "No active Bank or Cash account available in ${cardSpec.code} (${cardSpec.symbol}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalFolioSemanticColors.current.expenseCoral
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    eligibleAssets.forEach { asset ->
                        val isSelected = state.selectedSourceAccountId == asset.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onUpdateState { it.copy(selectedSourceAccountId = asset.id) }
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(asset.name)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    MoneyText(
                                        money = asset.currentBalance,
                                        currencySpec = SupportedCurrencies.getByCode(asset.currencyCode)
                                            ?: SupportedCurrencies.fromCode(asset.currencyCode),
                                        grouping = grouping,
                                        colorMode = MoneyColorMode.AUTO_SIGN,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.note,
                onValueChange = { v -> onUpdateState { it.copy(note = v) } },
                label = { Text("Settlement Note") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onConfirmSettlement,
                    enabled = !isSaving && eligibleAssets.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record Settlement")
                }
            }
        }
    }
}

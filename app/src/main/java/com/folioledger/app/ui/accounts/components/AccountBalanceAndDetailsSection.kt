package com.folioledger.app.ui.accounts.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.ui.accounts.AccountEditorDialogState
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountBalanceAndDetailsSection(
    state: AccountEditorDialogState,
    referenceDateEpochDay: Long,
    parentBankCandidates: List<AccountSummary>,
    onUpdateState: ((AccountEditorDialogState) -> AccountEditorDialogState) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

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

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        when (state.groupType) {
            AccountGroup.CREDIT_CARD -> {
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
    }
}

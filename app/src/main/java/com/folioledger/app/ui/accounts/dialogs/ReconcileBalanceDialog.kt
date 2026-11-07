package com.folioledger.app.ui.accounts.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.ui.accounts.ReconcileBalanceDialogState
import com.folioledger.app.ui.accounts.formatMinorPlainForInput
import com.folioledger.app.ui.accounts.parseInputToMinorOrZero
import com.folioledger.app.ui.theme.tabularMoneyStyle
import kotlin.math.abs

@Composable
fun ReconcileBalanceDialog(
    state: ReconcileBalanceDialogState,
    onUpdateTargetBalance: (String) -> Unit,
    onUpdateNote: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var localTargetBalance by remember(state.accountId) { mutableStateOf(state.targetBalanceInput) }
    LaunchedEffect(state.targetBalanceInput) {
        if (state.targetBalanceInput != localTargetBalance) {
            localTargetBalance = state.targetBalanceInput
        }
    }
    var localNote by remember(state.accountId) { mutableStateOf(state.note) }
    LaunchedEffect(state.note) {
        if (state.note != localNote) {
            localNote = state.note
        }
    }

    val isLiability = state.isLiability
    val recordedMinor = if (isLiability) state.recordedBalance.abs().amountMinor else state.recordedBalance.amountMinor
    val spec = SupportedCurrencies.fromCode(state.currencyCode)
    val parsedTarget = parseInputToMinorOrZero(localTargetBalance, spec.fractionDigits)
    val diffMinor = parsedTarget?.let {
        if (isLiability) {
            it.amountMinor - recordedMinor
        } else {
            it.amountMinor - state.recordedBalance.amountMinor
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reconcile ${state.accountName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isLiability) {
                        "Match Folio's outstanding balance against your official credit card or loan statement."
                    } else {
                        "Match Folio's balance against your official bank statement or physical cash."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (isLiability) "Current Recorded Outstanding Debt" else "Current Recorded Balance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val displayRecorded = if (isLiability) state.recordedBalance.abs() else state.recordedBalance
                        Text(
                            text = "${state.currencyCode} ${formatMinorPlainForInput(displayRecorded, spec.fractionDigits)}",
                            style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedTextField(
                    value = localTargetBalance,
                    onValueChange = {
                        localTargetBalance = it
                        onUpdateTargetBalance(it)
                    },
                    label = { Text(if (isLiability) "Actual Statement Outstanding Debt" else "Actual Statement Balance") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = state.validationError != null,
                    supportingText = state.validationError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                if (diffMinor != null) {
                    val adjustmentColor = when {
                        diffMinor == 0L -> MaterialTheme.colorScheme.onSurfaceVariant
                        isLiability -> if (diffMinor > 0L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        else -> if (diffMinor > 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    }
                    val diffSign = if (diffMinor > 0L) "+" else ""
                    val diffText = if (diffMinor == 0L) {
                        "✓ Balances match. No adjustment needed."
                    } else if (isLiability) {
                        val formattedDiff = formatMinorPlainForInput(MinorMoney(abs(diffMinor)), spec.fractionDigits)
                        if (diffMinor > 0L) {
                            "Adjustment: +${state.currencyCode} $formattedDiff (Increases debt)"
                        } else {
                            "Adjustment: -${state.currencyCode} $formattedDiff (Decreases debt)"
                        }
                    } else {
                        val formattedDiff = formatMinorPlainForInput(MinorMoney(abs(diffMinor)), spec.fractionDigits)
                        "Adjustment: $diffSign${state.currencyCode} $formattedDiff (${if (diffMinor > 0L) "Inflow adjustment" else "Outflow adjustment"})"
                    }

                    Text(
                        text = diffText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = adjustmentColor
                    )
                }

                OutlinedTextField(
                    value = localNote,
                    onValueChange = {
                        localNote = it
                        onUpdateNote(it)
                    },
                    label = { Text("Note (Optional)") },
                    placeholder = { Text("e.g., Monthly bank reconciliation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfirm()
                },
                enabled = parsedTarget != null
            ) {
                Text(if (diffMinor == 0L) "Confirm Match" else "Apply Adjustment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

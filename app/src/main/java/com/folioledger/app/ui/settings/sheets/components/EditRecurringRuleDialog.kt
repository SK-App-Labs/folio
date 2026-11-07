package com.folioledger.app.ui.settings.sheets.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.RecurringCommitmentItem
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.domain.util.RecurringScheduleCalculator
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

private fun formatMinorPlainForInput(money: MinorMoney, fractionDigits: Int): String {
    val divisor = BigDecimal.TEN.pow(fractionDigits)
    return BigDecimal.valueOf(money.amountMinor)
        .divide(divisor, fractionDigits, RoundingMode.HALF_UP)
        .toPlainString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecurringRuleDialog(
    rule: RecurringCommitmentItem,
    currencySpec: CurrencySpec,
    onDismiss: () -> Unit,
    onSave: (RecurringCommitmentItem) -> Unit
) {
    var title by remember { mutableStateOf(rule.title) }
    val initialAmount = remember(rule.amount, currencySpec.fractionDigits) {
        formatMinorPlainForInput(rule.amount, currencySpec.fractionDigits)
    }
    var amountInput by remember(initialAmount) { mutableStateOf(initialAmount) }
    var selectedFrequency by remember { mutableStateOf(rule.frequency) }
    var frequencyExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Recurring Rule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (rule.type == TransactionType.TRANSFER && rule.transferToAccountName != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Transfer: ${rule.accountName} → ${rule.transferToAccountName}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Payee") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount (${currencySpec.symbol})") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = frequencyExpanded,
                    onExpandedChange = { frequencyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedFrequency.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeat Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = frequencyExpanded,
                        onDismissRequest = { frequencyExpanded = false }
                    ) {
                        RepeatFrequency.entries.forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.displayName) },
                                onClick = {
                                    selectedFrequency = freq
                                    frequencyExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val evaluatedResult = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
                        expression = amountInput,
                        fractionDigits = currencySpec.fractionDigits
                    )
                    val evaluatedMinor = evaluatedResult.getOrNull()?.amountMinor ?: 0L
                    if (evaluatedMinor > 0L && title.isNotBlank()) {
                        val nextDate = RecurringScheduleCalculator.computeNextOccurrence(
                            currentDate = LocalDate.now(),
                            frequency = selectedFrequency
                        )
                        onSave(
                            rule.copy(
                                title = title.trim(),
                                amount = MinorMoney(evaluatedMinor),
                                frequency = selectedFrequency,
                                nextOccurrenceEpochDay = nextDate.toEpochDay()
                            )
                        )
                    }
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

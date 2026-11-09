package com.folioledger.app.ui.settings.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle

@Composable
fun MonthStartDayPickerDialog(
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Financial Month Start Day (1–31)",
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
                    text = "Choose the day of the month your financial cycle begins (1–31). Months with fewer days automatically clamp to the last day of that month.",
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

@Composable
fun RestoreConfirmationDialog(
    sourceUriString: String?,
    onConfirmRestore: () -> Unit,
    onCancel: () -> Unit
) {
    val isLocalLatest = sourceUriString == null || sourceUriString == "LATEST_BACKUP"
    val sourceDescription = if (isLocalLatest) {
        "the latest backup from Downloads/Folio"
    } else {
        "the selected backup file"
    }

    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = LocalFolioSemanticColors.current.warningAmber
            )
        },
        title = {
            Text(
                text = "Restore from Portable ZIP Backup?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Restoring $sourceDescription will replace your current local accounts, transactions, categories, and budgets.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "A safety rollback snapshot will be automatically saved before restoring.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmRestore,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalFolioSemanticColors.current.expenseCoral,
                    contentColor = Color.White
                )
            ) {
                Text("Replace & Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ClearAllDataConfirmationDialog(
    onConfirmClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = LocalFolioSemanticColors.current.expenseCoral
            )
        },
        title = {
            Text(
                text = "Reset & Clear All Ledger Data?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "This action will permanently delete all your accounts, transactions, custom categories, tags, budgets, and settings. This action cannot be undone unless you have a backup. Are you sure you want to proceed?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmClear,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalFolioSemanticColors.current.expenseCoral,
                    contentColor = Color.White
                )
            ) {
                Text("Clear All Data")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun CcLeadDaysDialog(
    selectedDays: Int,
    onSelectDays: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        0 to "On Due Date",
        1 to "1 Day Before",
        2 to "2 Days Before",
        3 to "3 Days Before (Recommended)",
        5 to "5 Days Before"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Credit Card Due Notice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Choose how many days before your credit card payment due date Folio should notify you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { (days, label) ->
                        FilterChip(
                            selected = selectedDays == days,
                            onClick = {
                                onSelectDays(days)
                                onDismiss()
                            },
                            label = { Text(label) }
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

@Composable
fun BillLeadDaysDialog(
    selectedDays: Int,
    onSelectDays: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        0 to "On Due Date",
        1 to "1 Day Before (Recommended)",
        2 to "2 Days Before",
        3 to "3 Days Before"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Upcoming Bill Notice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Choose how many days before a recurring bill or subscription Folio should notify you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { (days, label) ->
                        FilterChip(
                            selected = selectedDays == days,
                            onClick = {
                                onSelectDays(days)
                                onDismiss()
                            },
                            label = { Text(label) }
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

@Composable
fun ReminderTimePickerDialog(
    selectedHour: Int,
    selectedMinute: Int,
    onSelectTime: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        Pair(8, 0) to "08:00 AM",
        Pair(9, 0) to "09:00 AM (Default)",
        Pair(10, 0) to "10:00 AM",
        Pair(12, 0) to "12:00 PM",
        Pair(18, 0) to "06:00 PM",
        Pair(20, 0) to "08:00 PM"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Preferred Reminder Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Select when Folio should evaluate and post your daily bill and credit card due reminders.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { (time, label) ->
                        val isSelected = selectedHour == time.first && selectedMinute == time.second
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onSelectTime(time.first, time.second)
                                onDismiss()
                            },
                            label = { Text(label) }
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

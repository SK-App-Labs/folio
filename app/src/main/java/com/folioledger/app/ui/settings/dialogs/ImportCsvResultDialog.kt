package com.folioledger.app.ui.settings.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.ImportCsvResult

/**
 * Summary dialog displayed following a CSV transaction import batch,
 * reporting imported, updated, skipped records, and any row error warnings.
 */
@Composable
fun ImportCsvResultDialog(
    result: ImportCsvResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Import Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CSV import completed successfully!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "• Imported: ${result.importedCount} transactions")
                        if (result.accountsConfiguredCount > 0) {
                            Text(
                                text = "• Accounts Configured: ${result.accountsConfiguredCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (result.updatedCount > 0) {
                            Text(text = "• Updated: ${result.updatedCount} existing transactions")
                        }
                        if (result.skippedCount > 0) {
                            Text(text = "• Skipped: ${result.skippedCount} duplicates/invalid rows")
                        }
                        if (result.newAccountsCreated.isNotEmpty()) {
                            Text(
                                text = "• Created Accounts: ${result.newAccountsCreated.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (result.newCategoriesCreated.isNotEmpty()) {
                            Text(
                                text = "• Created Categories: ${result.newCategoriesCreated.size} new categories",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (result.errorMessages.isNotEmpty()) {
                    Text(
                        text = "Errors / Skipped rows (${result.errorMessages.size}):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    result.errorMessages.take(5).forEach { err ->
                        Text(
                            text = "• $err",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

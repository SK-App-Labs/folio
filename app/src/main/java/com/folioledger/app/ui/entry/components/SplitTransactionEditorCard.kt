package com.folioledger.app.ui.entry.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.entry.TransactionEntryUiState
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

@Composable
fun SplitTransactionEditorCard(
    uiState: TransactionEntryUiState,
    onAddSplitLine: () -> Unit,
    onPickSplitCategory: (Long) -> Unit,
    onUpdateSplitAmount: (Long, String) -> Unit,
    onAssignRemainingToSplit: (Long) -> Unit,
    onRemoveSplitLine: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val validation = uiState.splitValidation

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (validation.isValid) {
                semanticColors.incomeEmerald
            } else {
                semanticColors.warningAmber
            }
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Split Transaction Breakdown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (validation.isValid) "Balanced · Remaining: " else "Remaining Unallocated: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = validation.remainingUnallocated,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping,
                            colorMode = if (validation.remainingUnallocated.isZero) {
                                MoneyColorMode.INCOME
                            } else {
                                MoneyColorMode.EXPENSE
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onAddSplitLine,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Split", style = MaterialTheme.typography.labelSmall)
                }
            }

            uiState.splitDrafts.forEach { draft ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPickSplitCategory(draft.localId) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            CategoryIconBadge(
                                iconName = draft.categoryIconName,
                                colorHex = draft.categoryColorHex,
                                size = 26.dp,
                                iconSize = 14.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = draft.categoryName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    OutlinedTextField(
                        value = draft.amountInput,
                        onValueChange = { onUpdateSplitAmount(draft.localId, it) },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
                    )

                    if (!validation.remainingUnallocated.isZero) {
                        TextButton(
                            onClick = { onAssignRemainingToSplit(draft.localId) },
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text("Fill", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    IconButton(
                        onClick = { onRemoveSplitLine(draft.localId) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Remove Split Line",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

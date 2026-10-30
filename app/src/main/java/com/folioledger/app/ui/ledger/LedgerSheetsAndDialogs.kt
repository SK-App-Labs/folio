package com.folioledger.app.ui.ledger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.fromTransactionType
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionDetailActionSheet(
    transaction: TransactionItem,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val txCurrency = remember(transaction.currencyCode, currencySpec) {
        SupportedCurrencies.getByCode(transaction.currencyCode) ?: currencySpec
    }

    val categoryTitle = remember(transaction.parentCategoryName, transaction.categoryName, transaction.type) {
        when {
            !transaction.parentCategoryName.isNullOrBlank() && !transaction.categoryName.isNullOrBlank() ->
                "${transaction.parentCategoryName} › ${transaction.categoryName}"
            !transaction.categoryName.isNullOrBlank() ->
                transaction.categoryName
            else ->
                transaction.type.name.replace('_', ' ')
        }
    }

    val dateFormatted = remember(transaction.ledgerDateEpochDay) {
        LocalDate.ofEpochDay(transaction.ledgerDateEpochDay)
            .format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale.US))
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
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(
                        iconName = transaction.categoryIconName,
                        colorHex = transaction.categoryColorHex,
                        size = 44.dp,
                        iconSize = 22.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = categoryTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                MoneyText(
                    money = transaction.amount,
                    currencySpec = txCurrency,
                    grouping = grouping,
                    colorMode = MoneyColorMode.fromTransactionType(transaction.type),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (
                        (transaction.type == TransactionType.TRANSFER || transaction.type == TransactionType.CC_SETTLEMENT) &&
                        transaction.transferToAccountName != null
                    ) {
                        "${transaction.accountName} → ${transaction.transferToAccountName}"
                    } else {
                        transaction.accountName
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (transaction.installmentIndex != null && transaction.installmentTotalCount != null) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Installment",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = "${transaction.installmentIndex} of ${transaction.installmentTotalCount}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            if (transaction.payeeOrDescription.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Payee / Description",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = transaction.payeeOrDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (transaction.note.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (transaction.isSplit && transaction.splits.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Split Lines (${transaction.splits.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        transaction.splits.forEach { split ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = split.categoryName.ifBlank { "Split Item" },
                                    style = MaterialTheme.typography.bodySmall
                                )
                                MoneyText(
                                    money = split.amount,
                                    currencySpec = txCurrency,
                                    grouping = grouping,
                                    colorMode = MoneyColorMode.NEUTRAL,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            if (transaction.tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    transaction.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { showDeleteConfirmation = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = semanticColors.expenseCoral
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete")
                }

                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Transaction")
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReinforcedSearchFilterBottomSheet(
    uiState: LedgerHomeUiState,
    onQueryChange: (String) -> Unit,
    onToggleType: (TransactionType) -> Unit,
    onToggleAccount: (Long) -> Unit,
    onToggleCategory: (Long) -> Unit,
    onToggleTag: (String) -> Unit,
    onUpdateAmountBounds: (String, String) -> Unit,
    onFilterAllPeriodsChange: (Boolean) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filter = uiState.searchFilter

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
                Text(
                    text = "Reinforced Search & Multi-Filter",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    TextButton(onClick = onClearAll) {
                        Text("Reset All")
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            }

            OutlinedTextField(
                value = filter.queryText,
                onValueChange = onQueryChange,
                label = { Text("Keyword (Payee, Description, Note)") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onDismiss() }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Scope Filter to Selected Month (${uiState.activePeriodRange.title})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Follows < Prev / Next > month navigation automatically",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = !uiState.filterAllPeriods,
                    onCheckedChange = { scopeToMonth -> onFilterAllPeriodsChange(!scopeToMonth) }
                )
            }

            Text(
                text = "Transaction Types",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TransactionType.entries.forEach { type ->
                    FilterChip(
                        selected = type in filter.types,
                        onClick = { onToggleType(type) },
                        label = { Text(type.name.replace('_', ' ')) }
                    )
                }
            }

            if (uiState.availableAccounts.isNotEmpty()) {
                Text(
                    text = "Accounts",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.availableAccounts.forEach { account ->
                        FilterChip(
                            selected = account.id in filter.accountIds,
                            onClick = { onToggleAccount(account.id) },
                            label = { Text(account.name) }
                        )
                    }
                }
            }

            if (uiState.availableCategories.isNotEmpty()) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.availableCategories.forEach { cat ->
                        FilterChip(
                            selected = cat.id in filter.categoryIds,
                            onClick = { onToggleCategory(cat.id) },
                            label = { Text(cat.name) }
                        )
                    }
                }
            }

            if (uiState.availableTags.isNotEmpty()) {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.availableTags.forEach { tag ->
                        FilterChip(
                            selected = tag in filter.tags,
                            onClick = { onToggleTag(tag) },
                            label = { Text("#$tag") }
                        )
                    }
                }
            }

            Text(
                text = "Amount Range (${uiState.currencySpec.symbol})",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.minAmountFilterInput,
                    onValueChange = { minVal ->
                        onUpdateAmountBounds(minVal, uiState.maxAmountFilterInput)
                    },
                    label = { Text("Min Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.maxAmountFilterInput,
                    onValueChange = { maxVal ->
                        onUpdateAmountBounds(uiState.minAmountFilterInput, maxVal)
                    },
                    label = { Text("Max Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show Matching Results (${uiState.filteredTransactions.size})")
            }
        }
    }
}

@Composable
internal fun QuickBookmarksLauncherDialog(
    bookmarks: List<TransactionBookmark>,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onSelectBookmark: (TransactionBookmark) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = LocalFolioSemanticColors.current.warningAmber
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quick Bookmarks (★)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            if (bookmarks.isEmpty()) {
                Text(
                    text = "No saved bookmark templates yet. Open the + New Transaction composer and tap ★ to save frequent templates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(bookmarks, key = { it.id }) { bm ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectBookmark(bm) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bm.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (bm.payeeOrDescription.isNotBlank()) {
                                        Text(
                                            text = bm.payeeOrDescription,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                MoneyText(
                                    money = bm.amount,
                                    currencySpec = currencySpec,
                                    grouping = grouping,
                                    colorMode = MoneyColorMode.fromTransactionType(bm.type),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

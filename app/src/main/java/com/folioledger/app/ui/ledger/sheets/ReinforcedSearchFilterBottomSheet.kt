package com.folioledger.app.ui.ledger.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.ledger.LedgerHomeUiState
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

enum class LedgerFilterSheetTab {
    EXPENSE,
    INCOME,
    ACCOUNTS
}

@Composable
private fun FilterTabPill(
    title: String,
    badgeCount: Int,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) selectedColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.dp,
            if (selected) selectedColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = selectedColor
                ) {
                    Text(
                        text = badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReinforcedSearchFilterBottomSheet(
    uiState: LedgerHomeUiState,
    onQueryChange: (String) -> Unit,
    onToggleType: (TransactionType) -> Unit,
    onToggleAccount: (Long) -> Unit,
    onToggleCategory: (Long) -> Unit,
    onToggleTag: (String) -> Unit,
    onToggleTransfers: () -> Unit,
    onUpdateAmountBounds: (String, String) -> Unit,
    onFilterAllPeriodsChange: (Boolean) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    val filter = uiState.searchFilter
    var selectedFilterTab by rememberSaveable { mutableStateOf(LedgerFilterSheetTab.EXPENSE) }

    var localQuery by rememberSaveable { mutableStateOf(filter.queryText) }
    LaunchedEffect(filter.queryText) {
        if (filter.queryText != localQuery) {
            localQuery = filter.queryText
        }
    }

    var localMinAmount by rememberSaveable { mutableStateOf(uiState.minAmountFilterInput) }
    LaunchedEffect(uiState.minAmountFilterInput) {
        if (uiState.minAmountFilterInput != localMinAmount) {
            localMinAmount = uiState.minAmountFilterInput
        }
    }

    var localMaxAmount by rememberSaveable { mutableStateOf(uiState.maxAmountFilterInput) }
    LaunchedEffect(uiState.maxAmountFilterInput) {
        if (uiState.maxAmountFilterInput != localMaxAmount) {
            localMaxAmount = uiState.maxAmountFilterInput
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Section (Fixed & non-scrollable: immediately triggers pull-to-dismiss gesture on drag down)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Search & Multi-Filter",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onClearAll) {
                            Text("Reset All")
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                        }
                    }
                }

                OutlinedTextField(
                    value = localQuery,
                    onValueChange = {
                        localQuery = it
                        onQueryChange(it)
                    },
                    label = { Text("Keyword (Payee, Description, Note)") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (localQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                localQuery = ""
                                onQueryChange("")
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear search query"
                                )
                            }
                        }
                    },
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
                            text = "Only in ${uiState.activePeriodRange.title}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Turn off to search across all dates",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = !uiState.filterAllPeriods,
                        onCheckedChange = { onlyThisMonth -> onFilterAllPeriodsChange(!onlyThisMonth) }
                    )
                }

                // 3-Tab Filter Switcher
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterTabPill(
                        title = "Expense",
                        badgeCount = uiState.expenseFilterCount,
                        selected = selectedFilterTab == LedgerFilterSheetTab.EXPENSE,
                        selectedColor = semanticColors.expenseCoral,
                        onClick = { selectedFilterTab = LedgerFilterSheetTab.EXPENSE },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabPill(
                        title = "Income",
                        badgeCount = uiState.incomeFilterCount,
                        selected = selectedFilterTab == LedgerFilterSheetTab.INCOME,
                        selectedColor = semanticColors.incomeEmerald,
                        onClick = { selectedFilterTab = LedgerFilterSheetTab.INCOME },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabPill(
                        title = "Accounts & Range",
                        badgeCount = uiState.accountFilterCount,
                        selected = selectedFilterTab == LedgerFilterSheetTab.ACCOUNTS,
                        selectedColor = semanticColors.transferBlue,
                        onClick = { selectedFilterTab = LedgerFilterSheetTab.ACCOUNTS },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            // Middle Section (Scrollable tab body keyed by active tab)
            key(selectedFilterTab) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    when (selectedFilterTab) {
                        LedgerFilterSheetTab.EXPENSE -> {
                            Text(
                                text = "Transaction Type",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = TransactionType.EXPENSE in filter.types,
                                    onClick = { onToggleType(TransactionType.EXPENSE) },
                                    label = { Text("Expense") }
                                )
                                FilterChip(
                                    selected = TransactionType.REFUND in filter.types,
                                    onClick = { onToggleType(TransactionType.REFUND) },
                                    label = { Text("Refund") }
                                )
                            }

                            if (uiState.expenseCategories.isNotEmpty()) {
                                Text(
                                    text = "Expense Categories (${uiState.expenseCategories.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    uiState.expenseCategories.forEach { cat ->
                                        FilterChip(
                                            selected = cat.id in filter.categoryIds,
                                            onClick = { onToggleCategory(cat.id) },
                                            leadingIcon = {
                                                CategoryIconBadge(
                                                    iconName = cat.iconName,
                                                    colorHex = cat.colorHex,
                                                    size = 18.dp,
                                                    iconSize = 12.dp
                                                )
                                            },
                                            label = { Text(cat.name) }
                                        )
                                    }
                                }
                            }

                            if (uiState.availableTags.isNotEmpty()) {
                                Text(
                                    text = "Spending Tags",
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
                        }

                        LedgerFilterSheetTab.INCOME -> {
                            Text(
                                text = "Transaction Type",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = TransactionType.INCOME in filter.types,
                                    onClick = { onToggleType(TransactionType.INCOME) },
                                    label = { Text("Income") }
                                )
                            }

                            if (uiState.incomeCategories.isNotEmpty()) {
                                Text(
                                    text = "Income Categories (${uiState.incomeCategories.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    uiState.incomeCategories.forEach { cat ->
                                        FilterChip(
                                            selected = cat.id in filter.categoryIds,
                                            onClick = { onToggleCategory(cat.id) },
                                            leadingIcon = {
                                                CategoryIconBadge(
                                                    iconName = cat.iconName,
                                                    colorHex = cat.colorHex,
                                                    size = 18.dp,
                                                    iconSize = 12.dp
                                                )
                                            },
                                            label = { Text(cat.name) }
                                        )
                                    }
                                }
                            }
                        }

                        LedgerFilterSheetTab.ACCOUNTS -> {
                            if (uiState.availableAccounts.isNotEmpty()) {
                                Text(
                                    text = "Accounts (${uiState.availableAccounts.size})",
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
                                            leadingIcon = {
                                                CategoryIconBadge(
                                                    iconName = account.iconName,
                                                    colorHex = account.colorHex,
                                                    size = 18.dp,
                                                    iconSize = 12.dp
                                                )
                                            },
                                            label = { Text(account.name) }
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Transfers & Settlements",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            val isTransferSelected = TransactionType.TRANSFER in filter.types || TransactionType.CC_SETTLEMENT in filter.types
                            FilterChip(
                                selected = isTransferSelected,
                                onClick = onToggleTransfers,
                                label = { Text("⇄ Internal Transfers & Settlements") }
                            )

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
                                    value = localMinAmount,
                                    onValueChange = { minVal ->
                                        localMinAmount = minVal
                                        onUpdateAmountBounds(minVal, localMaxAmount)
                                    },
                                    label = { Text("Min Amount") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = localMaxAmount,
                                    onValueChange = { maxVal ->
                                        localMaxAmount = maxVal
                                        onUpdateAmountBounds(localMinAmount, maxVal)
                                    },
                                    label = { Text("Max Amount") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            // Pinned Bottom Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (uiState.filteredTransactions.isNotEmpty()) {
                            "Apply Filters (${uiState.filteredTransactions.size} Results)"
                        } else if (uiState.isFilterActive) {
                            "Apply Filters (0 Results)"
                        } else {
                            "Close Filters"
                        }
                    )
                }
            }
        }
    }
}

package com.folioledger.app.ui.ledger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionSearchFilter
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.theme.tabularMoneyStyle

@Composable
internal fun SelectedAccountScopeBanner(
    account: AccountSummary,
    grouping: NumberGroupingSystem,
    onClearScopedAccount: () -> Unit
) {
    val accountSpec = remember(account.currencyCode) {
        SupportedCurrencies.fromCode(account.currencyCode)
    }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                CategoryIconBadge(
                    iconName = account.iconName,
                    colorHex = account.colorHex,
                    size = 30.dp,
                    iconSize = 16.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Viewing Account: ${account.name}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Balance: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = account.currentBalance,
                            currencySpec = accountSpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.AUTO_SIGN,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onClearScopedAccount)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Show All Accounts",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear Account Scope",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun SearchAndActiveFilterBanner(
    queryText: String,
    activeFilterCount: Int,
    isFilterActive: Boolean,
    periodTitle: String,
    filterAllPeriods: Boolean,
    filter: TransactionSearchFilter = TransactionSearchFilter(),
    accounts: List<AccountSummary> = emptyList(),
    categories: List<CategoryNode> = emptyList(),
    currencySpec: CurrencySpec = SupportedCurrencies.INR,
    grouping: NumberGroupingSystem = NumberGroupingSystem.INDIAN,
    onQueryChange: (String) -> Unit,
    onOpenFilterSheet: () -> Unit,
    onClearAllFilters: () -> Unit,
    onRemoveCategoryFilter: (Long) -> Unit = {},
    onRemoveAccountFilter: (Long) -> Unit = {},
    onRemoveTypeFilter: (TransactionType) -> Unit = {},
    onRemoveTagFilter: (String) -> Unit = {},
    onRemoveTransfersFilter: () -> Unit = {},
    onRemoveAmountBounds: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = queryText,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            text = "Search",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (queryText.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear Search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                FilledTonalButton(
                    onClick = onOpenFilterSheet,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Filters",
                        modifier = Modifier.size(18.dp)
                    )
                    if (activeFilterCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$activeFilterCount",
                            style = tabularMoneyStyle(MaterialTheme.typography.labelLarge),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isFilterActive) {
                    TextButton(
                        onClick = onClearAllFilters,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Reset")
                    }
                }
            }

            val hasActiveCriteria = filter.categoryIds.isNotEmpty() ||
                    filter.accountIds.isNotEmpty() ||
                    filter.types.isNotEmpty() ||
                    filter.tags.isNotEmpty() ||
                    filter.minAmount != null ||
                    filter.maxAmount != null

            if (isFilterActive && hasActiveCriteria) {
                Spacer(modifier = Modifier.size(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Types
                    if (TransactionType.EXPENSE in filter.types) {
                        item(key = "type_exp") {
                            ActiveFilterChipPill(
                                label = "Expense",
                                onRemove = { onRemoveTypeFilter(TransactionType.EXPENSE) }
                            )
                        }
                    }
                    if (TransactionType.REFUND in filter.types) {
                        item(key = "type_ref") {
                            ActiveFilterChipPill(
                                label = "Refund",
                                onRemove = { onRemoveTypeFilter(TransactionType.REFUND) }
                            )
                        }
                    }
                    if (TransactionType.INCOME in filter.types) {
                        item(key = "type_inc") {
                            ActiveFilterChipPill(
                                label = "Income",
                                onRemove = { onRemoveTypeFilter(TransactionType.INCOME) }
                            )
                        }
                    }
                    if (TransactionType.TRANSFER in filter.types || TransactionType.CC_SETTLEMENT in filter.types) {
                        item(key = "type_trans") {
                            ActiveFilterChipPill(
                                label = "⇄ Transfers",
                                onRemove = onRemoveTransfersFilter
                            )
                        }
                    }

                    // Categories
                    items(filter.categoryIds.toList(), key = { "cat_$it" }) { catId ->
                        val cat = categories.firstOrNull { it.id == catId }
                            ?: categories.flatMap { it.subcategories }.firstOrNull { it.id == catId }
                        ActiveFilterChipPill(
                            label = cat?.name ?: "Category #$catId",
                            iconName = cat?.iconName,
                            colorHex = cat?.colorHex,
                            onRemove = { onRemoveCategoryFilter(catId) }
                        )
                    }

                    // Accounts
                    items(filter.accountIds.toList(), key = { "acc_$it" }) { accId ->
                        val acc = accounts.firstOrNull { it.id == accId }
                        ActiveFilterChipPill(
                            label = acc?.name ?: "Account #$accId",
                            iconName = acc?.iconName,
                            colorHex = acc?.colorHex,
                            onRemove = { onRemoveAccountFilter(accId) }
                        )
                    }

                    // Tags
                    items(filter.tags.toList(), key = { "tag_$it" }) { tag ->
                        ActiveFilterChipPill(
                            label = "#$tag",
                            onRemove = { onRemoveTagFilter(tag) }
                        )
                    }

                    // Amount Range
                    if (filter.minAmount != null || filter.maxAmount != null) {
                        val label = when {
                            filter.minAmount != null && filter.maxAmount != null ->
                                "${MoneyFormatterAndCalculator.formatMinorMoney(filter.minAmount, currencySpec, grouping)} - ${MoneyFormatterAndCalculator.formatMinorMoney(filter.maxAmount, currencySpec, grouping)}"
                            filter.minAmount != null ->
                                "≥ ${MoneyFormatterAndCalculator.formatMinorMoney(filter.minAmount, currencySpec, grouping)}"
                            filter.maxAmount != null ->
                                "≤ ${MoneyFormatterAndCalculator.formatMinorMoney(filter.maxAmount, currencySpec, grouping)}"
                            else -> ""
                        }
                        item(key = "amount_bound") {
                            ActiveFilterChipPill(
                                label = label,
                                onRemove = onRemoveAmountBounds
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveFilterChipPill(
    label: String,
    iconName: String? = null,
    colorHex: String? = null,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 2.dp, top = 2.dp, bottom = 2.dp)
        ) {
            if (iconName != null && colorHex != null) {
                CategoryIconBadge(
                    iconName = iconName,
                    colorHex = colorHex,
                    size = 18.dp,
                    iconSize = 11.dp
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(2.dp))
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(22.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove $label filter",
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

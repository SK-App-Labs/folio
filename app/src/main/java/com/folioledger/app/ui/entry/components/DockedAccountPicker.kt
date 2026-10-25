package com.folioledger.app.ui.entry.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

enum class AccountPickerViewMode { GRID, SPLIT_PANES }

@Composable
fun DockedAccountPicker(
    title: String = "Select Account",
    accounts: List<AccountSummary>,
    selectedAccountId: Long?,
    onAccountSelected: (AccountSummary) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    var viewMode by rememberSaveable { mutableStateOf(AccountPickerViewMode.GRID) }

    val stopParentScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // Consume unconsumed vertical scroll so it NEVER passes up to parent ModalBottomSheet!
                return Offset(0f, available.y)
            }
        }
    }

    val availableGroups = remember(accounts) {
        accounts.map { it.groupType }.distinct()
    }

    var activeGroup by remember(accounts, selectedAccountId) {
        val selectedAcc = accounts.firstOrNull { it.id == selectedAccountId }
        mutableStateOf(selectedAcc?.groupType ?: availableGroups.firstOrNull() ?: AccountGroup.CASH)
    }

    val accountsInActiveGroup = remember(accounts, activeGroup) {
        accounts.filter { it.groupType == activeGroup }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .nestedScroll(stopParentScrollConnection)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Dock Header: Title, View Switcher & Close Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Change View Toggle Button
                TextButton(
                    onClick = {
                        viewMode = if (viewMode == AccountPickerViewMode.GRID) {
                            AccountPickerViewMode.SPLIT_PANES
                        } else {
                            AccountPickerViewMode.GRID
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (viewMode == AccountPickerViewMode.GRID) Icons.Default.ViewStream else Icons.Default.GridView,
                        contentDescription = "Change View",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (viewMode == AccountPickerViewMode.GRID) "Groups View" else "Grid View",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Dock",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (viewMode == AccountPickerViewMode.GRID) {
            // MODE A: 3-Column Compact Account Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(accounts, key = { it.id }) { account ->
                    val isSelected = account.id == selectedAccountId
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else semanticColors.cardStrokeSubtle
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onAccountSelected(account) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            CategoryIconBadge(
                                iconName = account.iconName,
                                colorHex = account.colorHex,
                                size = 28.dp,
                                iconSize = 15.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        } else {
            // MODE B: 2-Pane Split Group View (Left: Account Groups | Right: Accounts in Group)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Left Pane: Account Groups
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .padding(end = 4.dp)
                ) {
                    items(availableGroups, key = { it.name }) { group ->
                        val isGroupActive = group == activeGroup
                        val countInGroup = accounts.count { it.groupType == group }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isGroupActive) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isGroupActive) MaterialTheme.colorScheme.primary else semanticColors.cardStrokeSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { activeGroup = group }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                CategoryIconBadge(
                                    iconName = getAccountGroupIconName(group),
                                    colorHex = if (group.isLiabilityByDefault) "#E11D48" else "#2563EB",
                                    size = 26.dp,
                                    iconSize = 14.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = getAccountGroupDisplayName(group),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isGroupActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isGroupActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Text(
                                        text = "$countInGroup",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Pane: Accounts in Active Selected Group
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 4.dp)
                ) {
                    if (accountsInActiveGroup.isEmpty()) {
                        item {
                            Text(
                                text = "No accounts under this group",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        items(accountsInActiveGroup, key = { it.id }) { account ->
                            val isSelected = account.id == selectedAccountId
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.secondary else semanticColors.cardStrokeSubtle
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onAccountSelected(account) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    CategoryIconBadge(
                                        iconName = account.iconName,
                                        colorHex = account.colorHex,
                                        size = 24.dp,
                                        iconSize = 13.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = account.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getAccountGroupDisplayName(group: AccountGroup): String = when (group) {
    AccountGroup.CASH -> "Cash"
    AccountGroup.BANK_ACCOUNT -> "Bank Accounts"
    AccountGroup.DEBIT_CARD -> "Debit Cards"
    AccountGroup.CREDIT_CARD -> "Credit Cards"
    AccountGroup.SAVINGS -> "Savings"
    AccountGroup.PREPAID_WALLET -> "Wallets"
    AccountGroup.INVESTMENT -> "Investments"
    AccountGroup.OVERDRAFT -> "Overdraft"
    AccountGroup.LOAN -> "Loans & Mortgages"
    AccountGroup.OTHER -> "Other"
}

private fun getAccountGroupIconName(group: AccountGroup): String = when (group) {
    AccountGroup.CASH -> "payments"
    AccountGroup.BANK_ACCOUNT -> "account_balance"
    AccountGroup.DEBIT_CARD -> "credit_card"
    AccountGroup.CREDIT_CARD -> "credit_card"
    AccountGroup.SAVINGS -> "savings"
    AccountGroup.PREPAID_WALLET -> "account_balance_wallet"
    AccountGroup.INVESTMENT -> "show_chart"
    AccountGroup.OVERDRAFT -> "account_balance"
    AccountGroup.LOAN -> "account_balance"
    AccountGroup.OTHER -> "account_balance_wallet"
}

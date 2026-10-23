package com.folioledger.app.ui.accounts.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle

@Composable
internal fun AccountGroupSectionCard(
    group: AccountGroup,
    accounts: List<AccountSummary>,
    allAccountsById: Map<Long, AccountSummary>,
    referenceDateEpochDay: Long,
    groupSubtotal: MinorMoney,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onAddAccountInGroup: () -> Unit,
    onEditAccount: (AccountSummary) -> Unit,
    onSettleCreditCard: (AccountSummary) -> Unit,
    onAccountClick: (AccountSummary) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleCollapse)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isCollapsed) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
                        contentDescription = if (isCollapsed) "Expand Group" else "Collapse Group",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatAccountGroupTitle(group),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = accounts.size.toString(),
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (group == AccountGroup.DEBIT_CARD) {
                        Text(
                            text = "Period Spend: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    MoneyText(
                        money = groupSubtotal,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = if (group == AccountGroup.DEBIT_CARD) {
                            MoneyColorMode.EXPENSE
                        } else {
                            MoneyColorMode.AUTO_SIGN
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onAddAccountInGroup,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add to ${formatAccountGroupTitle(group)}",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !isCollapsed,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    accounts.forEachIndexed { index, account ->
                        HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                        when (account.groupType) {
                            AccountGroup.CREDIT_CARD -> {
                                CreditCardAccountRow(
                                    account = account,
                                    referenceDateEpochDay = referenceDateEpochDay,
                                    defaultCurrencySpec = currencySpec,
                                    grouping = grouping,
                                    onSettleClick = { onSettleCreditCard(account) },
                                    onEditClick = { onEditAccount(account) },
                                    onRowClick = { onAccountClick(account) }
                                )
                            }

                            AccountGroup.DEBIT_CARD -> {
                                val linkedParent = account.parentAccountId?.let { allAccountsById[it] }
                                DebitCardAccountRow(
                                    account = account,
                                    parentAccount = linkedParent,
                                    defaultCurrencySpec = currencySpec,
                                    grouping = grouping,
                                    onEditClick = { onEditAccount(account) },
                                    onRowClick = { onAccountClick(account) }
                                )
                            }

                            else -> {
                                StandardAccountRow(
                                    account = account,
                                    defaultCurrencySpec = currencySpec,
                                    grouping = grouping,
                                    onEditClick = { onEditAccount(account) },
                                    onRowClick = { onAccountClick(account) }
                                )
                            }
                        }
                        if (index == accounts.lastIndex) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

internal fun formatAccountGroupTitle(group: AccountGroup): String = when (group) {
    AccountGroup.CASH -> "Cash Wallets"
    AccountGroup.BANK_ACCOUNT -> "Bank Accounts"
    AccountGroup.DEBIT_CARD -> "Debit Cards"
    AccountGroup.CREDIT_CARD -> "Credit Cards"
    AccountGroup.SAVINGS -> "Savings & Deposits"
    AccountGroup.PREPAID_WALLET -> "Prepaid Wallets"
    AccountGroup.INVESTMENT -> "Investments"
    AccountGroup.OVERDRAFT -> "Overdraft Accounts"
    AccountGroup.LOAN -> "Loans & Mortgages"
    AccountGroup.OTHER -> "Other Accounts"
}

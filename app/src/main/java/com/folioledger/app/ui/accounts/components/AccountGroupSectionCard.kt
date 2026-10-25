package com.folioledger.app.ui.accounts.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onEditAccount: (AccountSummary) -> Unit,
    onSettleCreditCard: (AccountSummary) -> Unit,
    onAccountClick: (AccountSummary) -> Unit,
    onAddAccountInGroup: (() -> Unit)? = null
) {
    val semanticColors = LocalFolioSemanticColors.current

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header Row (outside card surface, whole row tap to expand/collapse)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleCollapse)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formatAccountGroupTitle(group).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
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
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (group == AccountGroup.CREDIT_CARD) {
                    val groupPayableMinor = accounts.fold(0L) { sum, acc ->
                        sum + (acc.creditCardStatementPayable?.amountMinor ?: 0L)
                    }
                    val groupUnbilledMinor = accounts.fold(0L) { sum, acc ->
                        val totalDebt = acc.currentBalance.abs().amountMinor
                        val statement = acc.creditCardStatementPayable?.amountMinor ?: 0L
                        val unbilled = acc.creditCardUnbilledAmount?.amountMinor
                            ?: if (totalDebt > statement) totalDebt - statement else 0L
                        sum + unbilled
                    }

                    Box(
                        modifier = Modifier.width(78.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        MoneyText(
                            money = MinorMoney(groupPayableMinor),
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = if (groupPayableMinor > 0L) {
                                MoneyColorMode.EXPENSE
                            } else {
                                MoneyColorMode.INCOME
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier.width(72.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        MoneyText(
                            money = MinorMoney(groupUnbilledMinor),
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorOverride = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    MoneyText(
                        money = groupSubtotal,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = if (group == AccountGroup.DEBIT_CARD) {
                            if (groupSubtotal.isPositive) MoneyColorMode.EXPENSE else MoneyColorMode.NEUTRAL
                        } else {
                            MoneyColorMode.AUTO_SIGN
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Account List Surface Card
        AnimatedVisibility(
            visible = !isCollapsed,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (group == AccountGroup.CREDIT_CARD && accounts.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 6.dp)
                        ) {
                            Text(
                                text = "CARD DETAILS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier.width(78.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "DUE NOW",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.End
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier.width(72.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "UNBILLED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                        HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                    }

                    accounts.forEachIndexed { index, account ->
                        if (index > 0) {
                            HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                        }
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
                    }
                }
            }
        }
    }
}

internal fun formatAccountGroupTitle(group: AccountGroup): String = when (group) {
    AccountGroup.CASH -> "Cash"
    AccountGroup.BANK_ACCOUNT -> "Bank Accounts"
    AccountGroup.DEBIT_CARD -> "Debit Cards"
    AccountGroup.CREDIT_CARD -> "Credit Cards"
    AccountGroup.SAVINGS -> "Savings"
    AccountGroup.PREPAID_WALLET -> "Prepaid Wallets"
    AccountGroup.INVESTMENT -> "Investments"
    AccountGroup.OVERDRAFT -> "Overdraft"
    AccountGroup.LOAN -> "Loans"
    AccountGroup.OTHER -> "Other Accounts"
}
package com.folioledger.app.ui.accounts.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.ui.accounts.AccountPassbookDayGroup
import com.folioledger.app.ui.accounts.AccountPassbookEntryUiModel
import com.folioledger.app.ui.accounts.AccountPassbookUiModel
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.theme.tabularMoneyStyle
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

@Composable
fun PassbookTwoTierSummaryCard(
    passbook: AccountPassbookUiModel,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                PassbookSummaryMetricCell(
                    label = "Opening Bal",
                    amount = passbook.periodOpeningBalance,
                    colorMode = MoneyColorMode.AUTO_SIGN,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    color = semanticColors.cardStrokeSubtle,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                )

                PassbookSummaryMetricCell(
                    label = "Net Change",
                    amount = passbook.periodNetFlow,
                    colorMode = MoneyColorMode.AUTO_SIGN,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    showPlusSign = true,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    color = semanticColors.cardStrokeSubtle,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                )

                PassbookSummaryMetricCell(
                    label = "Closing Bal",
                    amount = passbook.periodClosingBalance,
                    colorMode = MoneyColorMode.AUTO_SIGN,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Inflows: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = passbook.periodInflows,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.INCOME,
                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                        fontWeight = FontWeight.Bold,
                        showPlusSignForPositive = true
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Outflows: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = passbook.periodOutflows,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.EXPENSE,
                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${passbook.totalPeriodTransactionCount} entries",
                    style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PassbookSummaryMetricCell(
    label: String,
    amount: MinorMoney,
    colorMode: MoneyColorMode,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    modifier: Modifier = Modifier,
    showPlusSign: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        MoneyText(
            money = amount,
            currencySpec = currencySpec,
            grouping = grouping,
            colorMode = colorMode,
            style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            showPlusSignForPositive = showPlusSign
        )
    }
}

@Composable
fun PassbookDayGroupHeaderRow(
    dayGroup: AccountPassbookDayGroup,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = dayGroup.dayOfMonthText,
                style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (dayGroup.isToday) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            ) {
                Text(
                    text = if (dayGroup.isToday) "TODAY · ${dayGroup.dayOfWeekText}" else dayGroup.dayOfWeekText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (dayGroup.isToday) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = dayGroup.monthYearText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MoneyText(
                money = dayGroup.dayEndRunningBalance,
                currencySpec = currencySpec,
                grouping = grouping,
                colorMode = MoneyColorMode.NEUTRAL,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PassbookTransactionEntryRowCard(
    entry: AccountPassbookEntryUiModel,
    inspectedAccountId: Long,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val transaction = entry.transaction

    val titleText = when {
        transaction.type == TransactionType.TRANSFER ||
                transaction.type == TransactionType.CC_SETTLEMENT -> {
            if (entry.isInflow && transaction.transferToAccountId == inspectedAccountId) {
                "Transfer In ← ${transaction.accountName}"
            } else {
                "Transfer Out → ${transaction.transferToAccountName ?: "Account"}"
            }
        }
        transaction.type == TransactionType.OPENING_BALANCE -> "Opening Balance"
        transaction.type == TransactionType.RECONCILIATION -> "Balance Reconciliation"
        transaction.isSplit -> "Split (${transaction.splits.size} categories)"
        !transaction.parentCategoryName.isNullOrBlank() -> {
            "${transaction.parentCategoryName} › ${transaction.categoryName ?: ""}"
        }
        else -> transaction.categoryName ?: "Uncategorized"
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            CategoryIconBadge(
                iconName = transaction.categoryIconName ?: when (transaction.type) {
                    TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> "swap_horiz"
                    TransactionType.REFUND -> "autorenew"
                    TransactionType.OPENING_BALANCE -> "account_balance_wallet"
                    TransactionType.RECONCILIATION -> "tune"
                    else -> "category"
                },
                colorHex = transaction.categoryColorHex ?: when (transaction.type) {
                    TransactionType.OPENING_BALANCE, TransactionType.RECONCILIATION -> "#0284C7"
                    else -> if (entry.isInflow) "#059669" else "#E11D48"
                },
                size = 38.dp,
                iconSize = 19.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val subline = when (transaction.type) {
                    TransactionType.OPENING_BALANCE -> transaction.note.ifBlank { "Initial starting balance" }
                    TransactionType.RECONCILIATION -> transaction.note.ifBlank { "Manual balance adjustment" }
                    else -> transaction.payeeOrDescription.ifBlank { transaction.note }
                }
                if (subline.isNotBlank()) {
                    Text(
                        text = subline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!entry.viaLinkedDebitCardName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Swiped via ${entry.viaLinkedDebitCardName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                MoneyText(
                    money = entry.signedDelta,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = when (transaction.type) {
                        TransactionType.OPENING_BALANCE, TransactionType.RECONCILIATION -> MoneyColorMode.NEUTRAL
                        else -> if (entry.isInflow) MoneyColorMode.INCOME else MoneyColorMode.EXPENSE
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    showPlusSignForPositive = true
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bal: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = entry.runningBalanceAfter,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.NEUTRAL,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

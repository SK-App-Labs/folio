package com.folioledger.app.ui.ledger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.fromTransactionType
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle

@Composable
internal fun DailyDateGroupHeaderRow(
    group: DailyLedgerGroup,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    amountsAlpha: Float = 1f
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = group.dayOfMonthText,
                    style = tabularMoneyStyle(MaterialTheme.typography.titleLarge),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (group.isToday) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Text(
                        text = if (group.isToday) "TODAY · ${group.dayOfWeekText}" else group.dayOfWeekText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (group.isToday) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = group.monthYearText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (amountsAlpha > 0f) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.graphicsLayer { alpha = amountsAlpha }
                ) {
                    if (group.dailyIncome.isPositive) {
                        MoneyText(
                            money = group.dailyIncome,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.INCOME,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            showPlusSignForPositive = true
                        )
                    }
                    if (group.dailyExpense.isPositive) {
                        MoneyText(
                            money = group.dailyExpense,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DailyGroupedTransactionsCard(
    transactions: List<TransactionItem>,
    scopedAccountId: Long? = null,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onTransactionClick: (TransactionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            transactions.forEachIndexed { index, tx ->
                if (index > 0) {
                    HorizontalDivider(
                        color = semanticColors.cardStrokeSubtle,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                LedgerTransactionRow(
                    transaction = tx,
                    scopedAccountId = scopedAccountId,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    onClick = { onTransactionClick(tx) }
                )
            }
        }
    }
}

@Composable
internal fun LedgerTransactionRow(
    transaction: TransactionItem,
    scopedAccountId: Long? = null,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val txCurrency = remember(transaction.currencyCode, currencySpec) {
        SupportedCurrencies.getByCode(transaction.currencyCode) ?: currencySpec
    }

    val resolvedColorMode = when {
        scopedAccountId != null &&
                (transaction.type == TransactionType.TRANSFER || transaction.type == TransactionType.CC_SETTLEMENT) -> {
            if (transaction.transferToAccountId == scopedAccountId) {
                MoneyColorMode.INCOME
            } else {
                MoneyColorMode.EXPENSE
            }
        }
        else -> MoneyColorMode.fromTransactionType(transaction.type)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        CategoryIconBadge(
            iconName = transaction.categoryIconName ?: when (transaction.type) {
                TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> "swap_horiz"
                TransactionType.REFUND -> "autorenew"
                else -> "category"
            },
            colorHex = transaction.categoryColorHex ?: when (transaction.type) {
                TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> "#2563EB"
                TransactionType.INCOME, TransactionType.REFUND -> "#059669"
                TransactionType.EXPENSE -> "#E11D48"
            },
            size = 40.dp,
            iconSize = 20.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            val titleText = when {
                transaction.type == TransactionType.TRANSFER ||
                        transaction.type == TransactionType.CC_SETTLEMENT -> {
                    "${transaction.accountName} → ${transaction.transferToAccountName ?: "Account"}"
                }
                transaction.isSplit -> {
                    "Split (${transaction.splits.size} categories)"
                }
                !transaction.parentCategoryName.isNullOrBlank() -> {
                    "${transaction.parentCategoryName} › ${transaction.categoryName ?: ""}"
                }
                else -> transaction.categoryName ?: "Uncategorized"
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (transaction.installmentIndex != null && transaction.installmentTotalCount != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Inst. ${transaction.installmentIndex}/${transaction.installmentTotalCount}",
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            val secondaryDescription = transaction.payeeOrDescription.ifBlank { transaction.note }
            if (secondaryDescription.isNotBlank()) {
                Text(
                    text = secondaryDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = transaction.accountName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (transaction.tags.isNotEmpty()) {
                    Text(
                        text = transaction.tags.joinToString(" ") { "#$it" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        MoneyText(
            money = transaction.amount,
            currencySpec = txCurrency,
            grouping = grouping,
            colorMode = resolvedColorMode,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun LedgerTransactionItemCard(
    transaction: TransactionItem,
    scopedAccountId: Long? = null,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onClick: () -> Unit
) {
    DailyGroupedTransactionsCard(
        transactions = listOf(transaction),
        scopedAccountId = scopedAccountId,
        currencySpec = currencySpec,
        grouping = grouping,
        onTransactionClick = { onClick() }
    )
}

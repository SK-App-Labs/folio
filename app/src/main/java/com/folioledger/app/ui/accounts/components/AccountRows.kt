package com.folioledger.app.ui.accounts.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.ui.accounts.CreditCardCycleCalculator
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.util.Locale

@Composable
internal fun StandardAccountRow(
    account: AccountSummary,
    defaultCurrencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onEditClick: () -> Unit,
    onRowClick: () -> Unit
) {
    val accountCurrency = remember(account.currencyCode, defaultCurrencySpec) {
        SupportedCurrencies.getByCode(account.currencyCode) ?: defaultCurrencySpec
    }

    val isForeignCurrency = account.currencyCode != defaultCurrencySpec.code
    val isExcluded = !account.includeInNetWorth
    val subtext = when {
        isForeignCurrency && isExcluded -> "${account.currencyCode} · Excluded from Net Worth"
        isForeignCurrency -> account.currencyCode
        isExcluded -> "Excluded from Net Worth"
        else -> null
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onRowClick,
                onLongClick = onEditClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        CategoryIconBadge(
            iconName = account.iconName,
            colorHex = account.colorHex,
            size = 42.dp,
            iconSize = 21.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (account.isArchived) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = "ARCHIVED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            if (subtext != null) {
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        MoneyText(
            money = account.currentBalance,
            currencySpec = accountCurrency,
            grouping = grouping,
            colorMode = MoneyColorMode.AUTO_SIGN,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun DebitCardAccountRow(
    account: AccountSummary,
    parentAccount: AccountSummary?,
    defaultCurrencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onEditClick: () -> Unit,
    onRowClick: () -> Unit
) {
    val accountCurrency = remember(account.currencyCode, defaultCurrencySpec) {
        SupportedCurrencies.getByCode(account.currencyCode) ?: defaultCurrencySpec
    }
    val periodSpend = account.debitCardPeriodSpend ?: MinorMoney.ZERO

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onRowClick,
                onLongClick = onEditClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        CategoryIconBadge(
            iconName = account.iconName,
            colorHex = account.colorHex,
            size = 42.dp,
            iconSize = 21.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (parentAccount != null) {
                    "Linked Bank: ${parentAccount.name}"
                } else {
                    "Linked to Parent Bank Account"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            if (parentAccount != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Available Bank Bal: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = parentAccount.currentBalance,
                        currencySpec = accountCurrency,
                        grouping = grouping,
                        colorMode = MoneyColorMode.AUTO_SIGN,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "This Month Spend",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MoneyText(
                money = periodSpend,
                currencySpec = accountCurrency,
                grouping = grouping,
                colorMode = MoneyColorMode.EXPENSE,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
internal fun CreditCardAccountRow(
    account: AccountSummary,
    referenceDateEpochDay: Long,
    defaultCurrencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onSettleClick: () -> Unit,
    onEditClick: () -> Unit,
    onRowClick: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val accountCurrency = remember(account.currencyCode, defaultCurrencySpec) {
        SupportedCurrencies.getByCode(account.currencyCode) ?: defaultCurrencySpec
    }

    val statementDay = (account.settlementDayOfMonth ?: 18).coerceIn(1, 31)
    val dueDay = (account.paymentDueDayOfMonth ?: 5).coerceIn(1, 31)
    val cycleWindow = remember(referenceDateEpochDay, statementDay, dueDay) {
        CreditCardCycleCalculator.computeCycleWindow(
            referenceDateEpochDay = referenceDateEpochDay,
            settlementDayOfMonth = statementDay,
            paymentDueDayOfMonth = dueDay
        )
    }

    val totalOutstandingDebt = account.currentBalance.abs()
    val statementPayable = account.creditCardStatementPayable ?: MinorMoney.ZERO
    val unbilledCycleAmount = account.creditCardUnbilledAmount
        ?: if (totalOutstandingDebt > statementPayable) {
            totalOutstandingDebt - statementPayable
        } else {
            MinorMoney.ZERO
        }

    val creditLimit = account.creditLimit
    val utilizationFraction = remember(totalOutstandingDebt, creditLimit) {
        if (creditLimit != null && creditLimit.amountMinor > 0L) {
            (totalOutstandingDebt.amountMinor.toFloat() / creditLimit.amountMinor.toFloat())
                .coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    val utilizationPercent = remember(utilizationFraction) {
        utilizationFraction * 100f
    }

    val utilizationColor = when {
        utilizationPercent >= 75f -> semanticColors.expenseCoral
        utilizationPercent >= 35f -> semanticColors.warningAmber
        else -> semanticColors.incomeEmerald
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onRowClick,
                onLongClick = onEditClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CategoryIconBadge(
                iconName = account.iconName,
                colorHex = account.colorHex,
                size = 42.dp,
                iconSize = 21.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Statement Day $statementDay · Due by ${cycleWindow.lastStatementDueShortText}",
                    style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalButton(
                onClick = onSettleClick,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Payments,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pay Bill",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Balance Payable",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = cycleWindow.lastStatementRangeShortText,
                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    MoneyText(
                        money = statementPayable,
                        currencySpec = accountCurrency,
                        grouping = grouping,
                        colorMode = if (statementPayable.isPositive) {
                            MoneyColorMode.EXPENSE
                        } else {
                            MoneyColorMode.INCOME
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Current Unbilled",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Active Cycle",
                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    MoneyText(
                        money = unbilledCycleAmount,
                        currencySpec = accountCurrency,
                        grouping = grouping,
                        colorMode = if (unbilledCycleAmount.isPositive) {
                            MoneyColorMode.EXPENSE
                        } else {
                            MoneyColorMode.NEUTRAL
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (creditLimit != null && creditLimit.isPositive) {
            Spacer(modifier = Modifier.height(10.dp))

            val availableCredit = (creditLimit - totalOutstandingDebt).let {
                if (it.isNegative) MinorMoney.ZERO else it
            }
            val pctText = remember(utilizationPercent) {
                String.format(Locale.US, "%.1f%% used", utilizationPercent)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Credit Utilization · $pctText",
                    style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                    fontWeight = FontWeight.SemiBold,
                    color = utilizationColor
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Avail: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = availableCredit,
                        currencySpec = accountCurrency,
                        grouping = grouping,
                        colorMode = MoneyColorMode.NEUTRAL,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = " / ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = creditLimit,
                        currencySpec = accountCurrency,
                        grouping = grouping,
                        colorMode = MoneyColorMode.NEUTRAL,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { utilizationFraction },
                color = utilizationColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

package com.folioledger.app.ui.accounts.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
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
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        CategoryIconBadge(
            iconName = account.iconName,
            colorHex = account.colorHex,
            size = 40.dp,
            iconSize = 20.dp
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
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        CategoryIconBadge(
            iconName = account.iconName,
            colorHex = account.colorHex,
            size = 40.dp,
            iconSize = 20.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (parentAccount != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${parentAccount.name} · Bal: ",
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
            } else {
                Text(
                    text = "Parent Bank Account",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        MoneyText(
            money = periodSpend,
            currencySpec = accountCurrency,
            grouping = grouping,
            colorMode = if (periodSpend.isPositive) {
                MoneyColorMode.EXPENSE
            } else {
                MoneyColorMode.NEUTRAL
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onRowClick,
                onLongClick = onEditClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        CategoryIconBadge(
            iconName = account.iconName,
            colorHex = account.colorHex,
            size = 40.dp,
            iconSize = 20.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Due ${cycleWindow.lastStatementDueShortText}",
                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.width(78.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                MoneyText(
                    money = statementPayable,
                    currencySpec = accountCurrency,
                    grouping = grouping,
                    colorMode = if (statementPayable.isPositive) {
                        MoneyColorMode.EXPENSE
                    } else {
                        MoneyColorMode.INCOME
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier.width(72.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                MoneyText(
                    money = unbilledCycleAmount,
                    currencySpec = accountCurrency,
                    grouping = grouping,
                    colorOverride = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
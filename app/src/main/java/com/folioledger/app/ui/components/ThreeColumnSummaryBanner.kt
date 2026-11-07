// File: app/src/main/java/com/folioledger/app/ui/components/ThreeColumnSummaryBanner.kt
package com.folioledger.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.R
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NetWorthOverview
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.PeriodLedgerSummary
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

/**
 * Reusable three-column summary metric card used across Ledger headers,
 * Accounts overview, and period cashflow displays.
 */
@Composable
fun ThreeColumnSummaryBanner(
    firstLabel: String,
    firstAmount: MinorMoney,
    secondLabel: String,
    secondAmount: MinorMoney,
    thirdLabel: String,
    thirdAmount: MinorMoney,
    modifier: Modifier = Modifier,
    currencySpec: CurrencySpec = SupportedCurrencies.INR,
    grouping: NumberGroupingSystem = currencySpec.defaultGrouping,
    firstColorMode: MoneyColorMode = MoneyColorMode.INCOME,
    secondColorMode: MoneyColorMode = MoneyColorMode.EXPENSE,
    thirdColorMode: MoneyColorMode = MoneyColorMode.AUTO_SIGN,
    carryOverAmount: MinorMoney? = null,
    onColumnClick: ((columnIndex: Int) -> Unit)? = null,
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(vertical = 12.dp)
            ) {
                SummaryColumnCell(
                    label = firstLabel,
                    amount = firstAmount,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = firstColorMode,
                    onClick = if (onColumnClick != null) ({ onColumnClick(0) }) else null,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    color = semanticColors.cardStrokeSubtle,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                )

                SummaryColumnCell(
                    label = secondLabel,
                    amount = secondAmount,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = secondColorMode,
                    onClick = if (onColumnClick != null) ({ onColumnClick(1) }) else null,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    color = semanticColors.cardStrokeSubtle,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                )

                SummaryColumnCell(
                    label = thirdLabel,
                    amount = thirdAmount,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = thirdColorMode,
                    showPlusSignForPositive = false,
                    onClick = if (onColumnClick != null) ({ onColumnClick(2) }) else null,
                    modifier = Modifier.weight(1f)
                )
            }

            if ((carryOverAmount != null) && (!carryOverAmount.isZero)) {
                HorizontalDivider(
                    color = semanticColors.cardStrokeSubtle,
                    thickness = 1.dp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ledger_banner_carry_over),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = carryOverAmount,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.AUTO_SIGN,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        showPlusSignForPositive = true
                    )
                }
            }
        }
    }
}

@Composable
fun ThreeColumnSummaryBanner(
    periodSummary: PeriodLedgerSummary,
    modifier: Modifier = Modifier,
    currencySpec: CurrencySpec = SupportedCurrencies.INR,
    grouping: NumberGroupingSystem = currencySpec.defaultGrouping,
    showCarryOver: Boolean = false,
    onColumnClick: ((columnIndex: Int) -> Unit)? = null,
) {
    ThreeColumnSummaryBanner(
        firstLabel = stringResource(R.string.ledger_banner_income),
        firstAmount = periodSummary.totalIncome,
        secondLabel = stringResource(R.string.ledger_banner_expense),
        secondAmount = periodSummary.totalExpense,
        thirdLabel = stringResource(R.string.ledger_banner_net),
        thirdAmount = periodSummary.netCashflow,
        currencySpec = currencySpec,
        grouping = grouping,
        firstColorMode = MoneyColorMode.INCOME,
        secondColorMode = MoneyColorMode.EXPENSE,
        thirdColorMode = MoneyColorMode.AUTO_SIGN,
        carryOverAmount = if (showCarryOver) periodSummary.carryOverFromPreviousPeriod else null,
        onColumnClick = onColumnClick,
        modifier = modifier
    )
}

@Composable
fun ThreeColumnSummaryBanner(
    netWorthOverview: NetWorthOverview,
    modifier: Modifier = Modifier,
    currencySpec: CurrencySpec = SupportedCurrencies.INR,
    grouping: NumberGroupingSystem = currencySpec.defaultGrouping,
    onColumnClick: ((columnIndex: Int) -> Unit)? = null,
) {
    ThreeColumnSummaryBanner(
        firstLabel = stringResource(R.string.accounts_banner_assets),
        firstAmount = netWorthOverview.totalAssets,
        secondLabel = stringResource(R.string.accounts_banner_liabilities),
        secondAmount = netWorthOverview.totalLiabilities,
        thirdLabel = stringResource(R.string.accounts_banner_net_worth),
        thirdAmount = netWorthOverview.netWorth,
        currencySpec = currencySpec,
        grouping = grouping,
        firstColorMode = MoneyColorMode.INCOME,
        secondColorMode = MoneyColorMode.EXPENSE,
        thirdColorMode = MoneyColorMode.AUTO_SIGN,
        carryOverAmount = null,
        onColumnClick = onColumnClick,
        modifier = modifier
    )
}

@Composable
private fun SummaryColumnCell(
    label: String,
    amount: MinorMoney,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    colorMode: MoneyColorMode,
    modifier: Modifier = Modifier,
    showPlusSignForPositive: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = if (currencySpec.symbol.length > 2) 4.dp else 6.dp)
    ) {
        Text(
            text = label,
            style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        MoneyText(
            money = amount,
            currencySpec = currencySpec,
            grouping = grouping,
            colorMode = colorMode,
            style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            showPlusSignForPositive = showPlusSignForPositive
        )
    }
}

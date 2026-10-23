package com.folioledger.app.ui.accounts.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.ui.accounts.AccountPassbookDayGroup
import com.folioledger.app.ui.accounts.AccountPassbookEntryUiModel
import com.folioledger.app.ui.accounts.AccountPassbookUiModel
import com.folioledger.app.ui.accounts.CreditCardCycleCalculator
import com.folioledger.app.ui.accounts.components.formatAccountGroupTitle
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.PeriodSelectorHeader
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountPassbookBottomSheet(
    passbook: AccountPassbookUiModel,
    referenceDateEpochDay: Long,
    defaultCurrencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onResetToCurrentMonth: () -> Unit,
    onEditAccountClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onSettleBillClick: () -> Unit,
    onViewInLedgerClick: () -> Unit,
    onTransactionRowClick: (TransactionItem) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    val account = passbook.account
    val accountCurrency = remember(account.currencyCode, defaultCurrencySpec) {
        SupportedCurrencies.getByCode(account.currencyCode) ?: defaultCurrencySpec
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Passbook Account Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    CategoryIconBadge(
                        iconName = account.iconName,
                        colorHex = account.colorHex,
                        size = 46.dp,
                        iconSize = 24.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = formatAccountGroupTitle(account.groupType),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (passbook.parentBankAccount != null) {
                                Text(
                                    text = "Linked to ${passbook.parentBankAccount.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else if (passbook.linkedDebitCards.isNotEmpty()) {
                                Text(
                                    text = "+${passbook.linkedDebitCards.size} Debit Card(s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (account.groupType == AccountGroup.DEBIT_CARD) {
                            "Bank Live Balance"
                        } else {
                            "Live Balance"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = passbook.currentLiveBalance,
                        currencySpec = accountCurrency,
                        grouping = grouping,
                        colorMode = MoneyColorMode.AUTO_SIGN,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onEditAccountClick) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit ${account.name}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Passbook"
                    )
                }
            }

            // Monthly Statement Period Switcher (< Prev | Month Year | Next >)
            PeriodSelectorHeader(
                periodTitle = passbook.periodTitle,
                periodSubtitle = passbook.periodSubtitle,
                onPreviousPeriod = onPreviousPeriod,
                onNextPeriod = onNextPeriod,
                onPeriodTitleClick = onResetToCurrentMonth,
                trailingContent = {
                    AssistChip(
                        onClick = onViewInLedgerClick,
                        label = {
                            Text(
                                text = "View in Ledger",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            LazyColumn(
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 12.dp,
                    bottom = 36.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Quick Actions Row (Primary Transaction Actions Only)
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onAddTransactionClick,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add Transaction",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (account.groupType == AccountGroup.CREDIT_CARD) {
                            FilledTonalButton(
                                onClick = onSettleBillClick,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Payments,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Settle Bill",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 2-Tier Passbook Statement Summary Card (Opening -> Inflows -> Outflows -> Closing)
                item {
                    PassbookTwoTierSummaryCard(
                        passbook = passbook,
                        currencySpec = accountCurrency,
                        grouping = grouping
                    )
                }

                // Credit Card Statement & Cycle Breakdown Card (if Credit Card)
                if (account.groupType == AccountGroup.CREDIT_CARD) {
                    item {
                        CreditCardStatementBreakdownCard(
                            account = account,
                            referenceDateEpochDay = referenceDateEpochDay,
                            currencySpec = accountCurrency,
                            grouping = grouping
                        )
                    }
                }

                // Passbook Statement Entries Header
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${passbook.periodTitle} Statement (${passbook.totalPeriodTransactionCount} entries)",
                            style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Amount & Running Bal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (passbook.dayGroups.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Activity in ${passbook.periodTitle}",
                            subtitle = "Opening Balance (${passbook.periodSubtitle}) carries forward unchanged to Closing Balance.",
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            actionLabel = "+ Add Transaction",
                            onActionClick = onAddTransactionClick
                        )
                    }
                } else {
                    passbook.dayGroups.forEach { dayGroup ->
                        item(key = "pb_day_${dayGroup.epochDay}") {
                            PassbookDayGroupHeaderRow(
                                dayGroup = dayGroup,
                                currencySpec = accountCurrency,
                                grouping = grouping
                            )
                        }
                        items(
                            items = dayGroup.entries,
                            key = { "pb_entry_${it.transaction.id}_${it.runningBalanceAfter.amountMinor}" }
                        ) { entry ->
                            PassbookTransactionEntryRowCard(
                                entry = entry,
                                inspectedAccountId = account.id,
                                currencySpec = accountCurrency,
                                grouping = grouping,
                                onClick = { onTransactionRowClick(entry.transaction) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PassbookTwoTierSummaryCard(
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
                    .padding(vertical = 12.dp, horizontal = 6.dp)
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
                    label = "+ Inflows",
                    amount = passbook.periodInflows,
                    colorMode = MoneyColorMode.INCOME,
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
                    label = "- Outflows",
                    amount = passbook.periodOutflows,
                    colorMode = MoneyColorMode.EXPENSE,
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
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${passbook.periodTitle} Net Change: ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = passbook.periodNetFlow,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.AUTO_SIGN,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        showPlusSignForPositive = true
                    )
                }

                if (passbook.linkedDebitCards.isNotEmpty()) {
                    Text(
                        text = "Includes ${passbook.linkedDebitCards.size} linked Debit Card(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PassbookSummaryMetricCell(
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
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
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
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            showPlusSignForPositive = showPlusSign
        )
    }
}

@Composable
private fun PassbookDayGroupHeaderRow(
    dayGroup: AccountPassbookDayGroup,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
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
            Text(
                text = "Day End Bal:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
private fun PassbookTransactionEntryRowCard(
    entry: AccountPassbookEntryUiModel,
    inspectedAccountId: Long,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onClick: () -> Unit
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
        modifier = Modifier
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
                    else -> "category"
                },
                colorHex = transaction.categoryColorHex ?: when {
                    entry.isInflow -> "#059669"
                    else -> "#E11D48"
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

                val subline = transaction.payeeOrDescription.ifBlank { transaction.note }
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
                    colorMode = if (entry.isInflow) {
                        MoneyColorMode.INCOME
                    } else {
                        MoneyColorMode.EXPENSE
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

@Composable
private fun CreditCardStatementBreakdownCard(
    account: AccountSummary,
    referenceDateEpochDay: Long,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    val semanticColors = LocalFolioSemanticColors.current
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
    val unbilledSpend = account.creditCardUnbilledAmount
        ?: if (totalOutstandingDebt > statementPayable) {
            totalOutstandingDebt - statementPayable
        } else {
            MinorMoney.ZERO
        }
    val creditLimit = account.creditLimit ?: MinorMoney.ZERO
    val availableCredit = if (creditLimit > totalOutstandingDebt) {
        creditLimit - totalOutstandingDebt
    } else {
        MinorMoney.ZERO
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Statement & Cycle Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Last Closed Statement (Payable Now)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Statement Period: ${cycleWindow.lastStatementRangeShortText} • Due by ${cycleWindow.lastStatementDueShortText}",
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    MoneyText(
                        money = statementPayable,
                        currencySpec = currencySpec,
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
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Active Unbilled Cycle Spend",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Active Unbilled Cycle: ${cycleWindow.activeCycleRangeShortText} (Purchases after last statement date)",
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    MoneyText(
                        money = unbilledSpend,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.NEUTRAL,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            ) {
                Column {
                    Text(
                        text = "Total Outstanding Debt",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = account.currentBalance,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.AUTO_SIGN,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (creditLimit.isPositive) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Available Credit Limit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MoneyText(
                                money = availableCredit,
                                currencySpec = currencySpec,
                                grouping = grouping,
                                colorMode = MoneyColorMode.INCOME,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " / ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            MoneyText(
                                money = creditLimit,
                                currencySpec = currencySpec,
                                grouping = grouping,
                                colorMode = MoneyColorMode.NEUTRAL,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

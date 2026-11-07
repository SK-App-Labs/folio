package com.folioledger.app.ui.accounts.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.ui.accounts.AccountPassbookUiModel
import com.folioledger.app.ui.accounts.ReconcileBalanceDialogState
import com.folioledger.app.ui.accounts.components.CreditCardPassbookHeroCard
import com.folioledger.app.ui.accounts.components.InvestmentPassbookHeroCard
import com.folioledger.app.ui.accounts.components.PassbookDayGroupHeaderRow
import com.folioledger.app.ui.accounts.components.PassbookQuickActionsRow
import com.folioledger.app.ui.accounts.components.PassbookTopAccountHeader
import com.folioledger.app.ui.accounts.components.PassbookTransactionEntryRowCard
import com.folioledger.app.ui.accounts.components.PassbookTwoTierSummaryCard
import com.folioledger.app.ui.components.EmptyStateCard
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
    onReconcileClick: () -> Unit = {},
    onTogglePin: () -> Unit = {},
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            // Top Passbook Account Header
            PassbookTopAccountHeader(
                account = account,
                passbook = passbook,
                accountCurrency = accountCurrency,
                grouping = grouping,
                onTogglePin = onTogglePin,
                onReconcileClick = onReconcileClick,
                onEditAccountClick = onEditAccountClick,
                onDismiss = onDismiss
            )

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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (account.groupType == AccountGroup.CREDIT_CARD) {
                    // Unified Credit Card Statement & Debt Hub Card
                    item(contentType = "passbook_hero") {
                        CreditCardPassbookHeroCard(
                            account = account,
                            passbook = passbook,
                            referenceDateEpochDay = referenceDateEpochDay,
                            currencySpec = accountCurrency,
                            grouping = grouping
                        )
                    }

                    // Dual Primary Actions Hub (Settle Bill + Add Expense)
                    item(contentType = "passbook_actions") {
                        PassbookQuickActionsRow(
                            account = account,
                            onSettleBillClick = onSettleBillClick,
                            onAddTransactionClick = onAddTransactionClick,
                            onReconcileClick = onReconcileClick,
                            onEditAccountClick = onEditAccountClick
                        )
                    }
                } else if (account.groupType == AccountGroup.INVESTMENT) {
                    // Investment Portfolio Valuation Hero Card
                    item(contentType = "investment_hero") {
                        InvestmentPassbookHeroCard(
                            account = account,
                            passbook = passbook,
                            currencySpec = accountCurrency,
                            grouping = grouping
                        )
                    }

                    // Quick Actions Row for Investment Accounts (Add Transaction, Update Value, Edit)
                    item {
                        PassbookQuickActionsRow(
                            account = account,
                            onSettleBillClick = onSettleBillClick,
                            onAddTransactionClick = onAddTransactionClick,
                            onReconcileClick = onReconcileClick,
                            onEditAccountClick = onEditAccountClick
                        )
                    }

                    // 2-Tier Passbook Statement Summary Card
                    item(contentType = "passbook_summary") {
                        PassbookTwoTierSummaryCard(
                            passbook = passbook,
                            currencySpec = accountCurrency,
                            grouping = grouping
                        )
                    }
                } else {
                    // Quick Actions Row for Standard Accounts
                    item {
                        PassbookQuickActionsRow(
                            account = account,
                            onSettleBillClick = onSettleBillClick,
                            onAddTransactionClick = onAddTransactionClick,
                            onReconcileClick = onReconcileClick,
                            onEditAccountClick = onEditAccountClick
                        )
                    }

                    // 2-Tier Passbook Statement Summary Card (Opening -> Inflows -> Outflows -> Closing)
                    item(contentType = "passbook_summary") {
                        PassbookTwoTierSummaryCard(
                            passbook = passbook,
                            currencySpec = accountCurrency,
                            grouping = grouping
                        )
                    }
                }

                // Passbook Statement Entries Header
                item(contentType = "passbook_header") {
                    Text(
                        text = "${passbook.periodTitle} Statement · ${passbook.totalPeriodTransactionCount} entries",
                        style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
                        item(key = "pb_day_${dayGroup.epochDay}", contentType = "passbook_day_header") {
                            PassbookDayGroupHeaderRow(
                                dayGroup = dayGroup,
                                currencySpec = accountCurrency,
                                grouping = grouping
                            )
                        }
                        items(
                            items = dayGroup.entries,
                            key = { "pb_entry_${it.transaction.id}_${it.runningBalanceAfter.amountMinor}" },
                            contentType = { "passbook_entry" }
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

@Deprecated(
    message = "Moved to com.folioledger.app.ui.accounts.dialogs.ReconcileBalanceDialog",
    replaceWith = ReplaceWith(
        "ReconcileBalanceDialog(state, onUpdateTargetBalance, onUpdateNote, onConfirm, onDismiss)",
        "com.folioledger.app.ui.accounts.dialogs.ReconcileBalanceDialog"
    )
)
@Composable
fun ReconcileBalanceDialog(
    state: ReconcileBalanceDialogState,
    onUpdateTargetBalance: (String) -> Unit,
    onUpdateNote: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) = com.folioledger.app.ui.accounts.dialogs.ReconcileBalanceDialog(
    state = state,
    onUpdateTargetBalance = onUpdateTargetBalance,
    onUpdateNote = onUpdateNote,
    onConfirm = onConfirm,
    onDismiss = onDismiss
)

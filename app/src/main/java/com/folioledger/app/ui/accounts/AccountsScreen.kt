// File: app/src/main/java/com/folioledger/app/ui/accounts/AccountsScreen.kt
package com.folioledger.app.ui.accounts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.PeriodSelectorHeader
import com.folioledger.app.ui.components.ThreeColumnSummaryBanner
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.util.Locale

@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onAccountClick: ((AccountSummary) -> Unit)? = null,
    onAddTransactionForAccount: (AccountSummary) -> Unit = {},
    onEditTransaction: (TransactionItem) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }

    AccountsScreenContent(
        uiState = uiState,
        contentPadding = contentPadding,
        snackbarHostState = snackbarHostState,
        onToggleGroupCollapsed = viewModel::toggleGroupCollapsed,
        onToggleShowArchived = viewModel::toggleShowArchivedAccounts,
        onOpenCreateAccount = viewModel::openCreateAccountDialog,
        onOpenEditAccount = viewModel::openEditAccountDialog,
        onOpenSettlementSheet = viewModel::openSettlementSheet,
        onAccountClick = { account ->
            viewModel.selectAccountForInspection(account.id)
            onAccountClick?.invoke(account)
        }
    )

    val passbook = uiState.accountPassbook
    if (passbook != null) {
        AccountPassbookBottomSheet(
            passbook = passbook,
            referenceDateEpochDay = uiState.referenceDateEpochDay,
            defaultCurrencySpec = uiState.currencySpec,
            grouping = uiState.numberGrouping,
            onPreviousPeriod = { viewModel.stepPassbookMonth(-1) },
            onNextPeriod = { viewModel.stepPassbookMonth(1) },
            onResetToCurrentMonth = viewModel::resetPassbookToCurrentMonth,
            onEditAccountClick = {
                viewModel.dismissInspectedAccountSheet()
                viewModel.openEditAccountDialog(passbook.account)
            },
            onAddTransactionClick = {
                viewModel.dismissInspectedAccountSheet()
                onAddTransactionForAccount(passbook.account)
            },
            onSettleBillClick = {
                viewModel.dismissInspectedAccountSheet()
                viewModel.openSettlementSheet(passbook.account)
            },
            onTransactionRowClick = { tx ->
                viewModel.dismissInspectedAccountSheet()
                onEditTransaction(tx)
            },
            onDismiss = viewModel::dismissInspectedAccountSheet
        )
    }

    val editorState = uiState.editorDialogState
    if (editorState != null) {
        AccountEditorBottomSheet(
            state = editorState,
            referenceDateEpochDay = uiState.referenceDateEpochDay,
            parentBankCandidates = uiState.parentBankCandidates,
            isSaving = uiState.isSaving,
            onUpdateState = viewModel::updateEditorDialogState,
            onSave = viewModel::saveAccountFromDialog,
            onToggleArchive = {
                if (editorState.isEditing && editorState.accountId > 0L) {
                    viewModel.setAccountArchived(editorState.accountId, !editorState.isArchived)
                }
            },
            onDismiss = viewModel::dismissAccountEditorDialog
        )
    }

    val settlementState = uiState.settlementSheetState
    if (settlementState != null) {
        CreditCardSettlementBottomSheet(
            state = settlementState,
            assetAccounts = uiState.assetAccountsForSettlement,
            grouping = uiState.numberGrouping,
            isSaving = uiState.isSaving,
            onSelectPreset = viewModel::selectSettlementPreset,
            onUpdateState = viewModel::updateSettlementSheetState,
            onConfirmSettlement = viewModel::submitCreditCardSettlement,
            onDismiss = viewModel::dismissSettlementSheet
        )
    }
}

@Composable
fun AccountsScreenContent(
    uiState: AccountsUiState,
    contentPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    onToggleGroupCollapsed: (AccountGroup) -> Unit,
    onToggleShowArchived: () -> Unit,
    onOpenCreateAccount: (AccountGroup) -> Unit,
    onOpenEditAccount: (AccountSummary) -> Unit,
    onOpenSettlementSheet: (AccountSummary) -> Unit,
    onAccountClick: (AccountSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val allById = remember(uiState.allAccounts) {
        uiState.allAccounts.associateBy { it.id }
    }
    val orderedGroups = remember(uiState.netWorthOverview.accountsByGroup) {
        AccountGroup.entries.filter { group ->
            !uiState.netWorthOverview.accountsByGroup[group].isNullOrEmpty()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { scaffoldPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + scaffoldPadding.calculateTopPadding() + 12.dp,
                bottom = contentPadding.calculateBottomPadding() + scaffoldPadding.calculateBottomPadding() + 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Accounts & Net Worth",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap any account to open its monthly Passbook & Running Balance Statement",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { onOpenCreateAccount(AccountGroup.BANK_ACCOUNT) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Account",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Account",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            item {
                ThreeColumnSummaryBanner(
                    netWorthOverview = uiState.netWorthOverview,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.numberGrouping
                )
            }

            if (orderedGroups.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Active Accounts Yet",
                        subtitle = "Add your Bank Accounts, Cash Wallet, Credit Cards, and linked Debit Cards to track your live Net Worth.",
                        icon = Icons.Filled.AccountBalanceWallet,
                        actionLabel = "Create First Account",
                        onActionClick = { onOpenCreateAccount(AccountGroup.BANK_ACCOUNT) }
                    )
                }
            } else {
                orderedGroups.forEach { group ->
                    val groupAccounts = uiState.netWorthOverview.accountsByGroup[group].orEmpty()
                    val isCollapsed = group in uiState.collapsedGroups
                    val groupTotalMinor = groupAccounts.fold(0L) { sum, acc ->
                        if (acc.groupType == AccountGroup.DEBIT_CARD) {
                            sum + (acc.debitCardPeriodSpend?.amountMinor ?: 0L)
                        } else {
                            sum + acc.currentBalance.amountMinor
                        }
                    }

                    item(key = "group_header_${group.name}") {
                        AccountGroupSectionCard(
                            group = group,
                            accounts = groupAccounts,
                            allAccountsById = allById,
                            referenceDateEpochDay = uiState.referenceDateEpochDay,
                            groupSubtotal = MinorMoney(groupTotalMinor),
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping,
                            isCollapsed = isCollapsed,
                            onToggleCollapse = { onToggleGroupCollapsed(group) },
                            onAddAccountInGroup = { onOpenCreateAccount(group) },
                            onEditAccount = onOpenEditAccount,
                            onSettleCreditCard = onOpenSettlementSheet,
                            onAccountClick = onAccountClick
                        )
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "Include Archived Accounts",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = uiState.showArchivedAccounts,
                        onCheckedChange = { onToggleShowArchived() }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountGroupSectionCard(
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

@Composable
private fun StandardAccountRow(
    account: AccountSummary,
    defaultCurrencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onEditClick: () -> Unit,
    onRowClick: () -> Unit
) {
    val accountCurrency = remember(account.currencyCode, defaultCurrencySpec) {
        SupportedCurrencies.getByCode(account.currencyCode) ?: defaultCurrencySpec
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRowClick)
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
            Text(
                text = if (account.includeInNetWorth) {
                    "${account.currencyCode} · Included in Net Worth · Tap for passbook"
                } else {
                    "${account.currencyCode} · Excluded from Net Worth · Tap for passbook"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        MoneyText(
            money = account.currentBalance,
            currencySpec = accountCurrency,
            grouping = grouping,
            colorMode = MoneyColorMode.AUTO_SIGN,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(
            onClick = onEditClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit ${account.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun DebitCardAccountRow(
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
            .clickable(onClick = onRowClick)
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

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(
            onClick = onEditClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit ${account.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun CreditCardAccountRow(
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
            .clickable(onClick = onRowClick)
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

            Spacer(modifier = Modifier.width(2.dp))

            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit ${account.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountPassbookBottomSheet(
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
                onPeriodTitleClick = onResetToCurrentMonth
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
                // Quick Actions Row
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onAddTransactionClick,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
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
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
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

                        OutlinedButton(
                            onClick = onEditAccountClick,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit Account",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
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
            // Tier 1: Opening Balance | + Inflows | - Outflows | Closing Balance
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

            // Tier 2: Period Net Cashflow & Linked Debit Card Rollup Note
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

            // 1. Last Closed Statement Cycle & Balance Payable
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

            // 2. Active Unbilled Cycle Dates & Unbilled Spend
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

            // 3. Total Outstanding Debt & Available Credit Limit
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountEditorBottomSheet(
    state: AccountEditorDialogState,
    referenceDateEpochDay: Long,
    parentBankCandidates: List<AccountSummary>,
    isSaving: Boolean,
    onUpdateState: ((AccountEditorDialogState) -> AccountEditorDialogState) -> Unit,
    onSave: () -> Unit,
    onToggleArchive: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current

    var showStatementDayPicker by rememberSaveable { mutableStateOf(false) }
    var showPaymentDueDayPicker by rememberSaveable { mutableStateOf(false) }

    val swatchPresets = remember {
        listOf("#059669", "#2563EB", "#4F46E5", "#D97706", "#E11D48", "#0D9488", "#7C3AED", "#334155")
    }
    val iconPresets = remember {
        listOf(
            "account_balance",
            "account_balance_wallet",
            "credit_card",
            "savings",
            "trending_up",
            "payments",
            "security",
            "work"
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(
                        iconName = state.iconName,
                        colorHex = state.colorHex,
                        size = 40.dp,
                        iconSize = 20.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (state.isEditing) "Edit Account" else "New Account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close"
                    )
                }
            }

            if (!state.validationError.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = semanticColors.expenseCoralContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.validationError,
                        style = MaterialTheme.typography.labelMedium,
                        color = semanticColors.onExpenseCoralContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            OutlinedTextField(
                value = state.name,
                onValueChange = { newName -> onUpdateState { it.copy(name = newName) } },
                label = { Text("Account Name") },
                placeholder = { Text("e.g., HDFC Regalia CC, ICICI Salary A/c") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Account Group",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccountGroup.entries.forEach { group ->
                    FilterChip(
                        selected = state.groupType == group,
                        onClick = {
                            onUpdateState { current ->
                                current.copy(
                                    groupType = group,
                                    iconName = AccountsViewModel.defaultIconForGroup(group),
                                    colorHex = AccountsViewModel.defaultColorForGroup(group),
                                    parentAccountId = if (group == AccountGroup.DEBIT_CARD) {
                                        current.parentAccountId ?: parentBankCandidates.firstOrNull()?.id
                                    } else {
                                        null
                                    }
                                )
                            }
                        },
                        label = { Text(formatAccountGroupTitle(group)) }
                    )
                }
            }

            Text(
                text = "Currency",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SupportedCurrencies.ALL.forEach { spec ->
                    FilterChip(
                        selected = state.currencyCode == spec.code,
                        onClick = { onUpdateState { it.copy(currencyCode = spec.code) } },
                        label = { Text("${spec.symbol} ${spec.code}") }
                    )
                }
            }

            when (state.groupType) {
                AccountGroup.DEBIT_CARD -> {
                    Text(
                        text = "Linked Parent Bank Account",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (parentBankCandidates.isEmpty()) {
                        Text(
                            text = "No Bank or Savings account found. Create a Bank Account first to link a Debit Card.",
                            style = MaterialTheme.typography.bodySmall,
                            color = semanticColors.expenseCoral
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            parentBankCandidates.forEach { candidate ->
                                FilterChip(
                                    selected = state.parentAccountId == candidate.id,
                                    onClick = {
                                        onUpdateState { it.copy(parentAccountId = candidate.id) }
                                    },
                                    label = { Text(candidate.name) }
                                )
                            }
                        }
                    }
                }

                AccountGroup.CREDIT_CARD -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = state.openingBalanceInput,
                            onValueChange = { v -> onUpdateState { it.copy(openingBalanceInput = v) } },
                            label = { Text("Total Debt Owed") },
                            supportingText = { Text("Billed + Unbilled debt") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.creditLimitInput,
                            onValueChange = { v -> onUpdateState { it.copy(creditLimitInput = v) } },
                            label = { Text("Credit Limit") },
                            supportingText = { Text("Total card limit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = state.statementPayableInput,
                        onValueChange = { v -> onUpdateState { it.copy(statementPayableInput = v) } },
                        label = { Text("Current Billed Statement Due (Payable Now)") },
                        supportingText = { Text("Unpaid bill from your last closed statement") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.unbilledAmountInput,
                        onValueChange = { v -> onUpdateState { it.copy(unbilledAmountInput = v) } },
                        label = { Text("Current Unbilled Spend (Since Last Statement)") },
                        supportingText = { Text("Purchases made after your last statement closed") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Statement Day (1..31) & Payment Due Day (1..31) Selector Cards
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showStatementDayPicker = true }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Statement Day (1–31)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Day ${state.settlementDayOfMonth}",
                                    style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showPaymentDueDayPicker = true }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Payment Due Day (1–31)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Day ${state.paymentDueDayOfMonth}",
                                    style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    val liveCycleWindow = remember(
                        referenceDateEpochDay,
                        state.settlementDayOfMonth,
                        state.paymentDueDayOfMonth
                    ) {
                        CreditCardCycleCalculator.computeCycleWindow(
                            referenceDateEpochDay = referenceDateEpochDay,
                            settlementDayOfMonth = state.settlementDayOfMonth,
                            paymentDueDayOfMonth = state.paymentDueDayOfMonth
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Live Billing Cycle Preview",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "• Current Active Billing Cycle: ${liveCycleWindow.activeCycleRangeFullText} (New expenses in this window count as Unbilled Spend).",
                                style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "• Last Closed Statement: ${liveCycleWindow.lastStatementRangeFullText} (Payable by ${liveCycleWindow.lastStatementDueFullText}).",
                                style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                else -> {
                    OutlinedTextField(
                        value = state.openingBalanceInput,
                        onValueChange = { v -> onUpdateState { it.copy(openingBalanceInput = v) } },
                        label = {
                            Text(
                                if (state.groupType.isLiabilityByDefault) {
                                    "Current Liability / Loan Balance"
                                } else {
                                    "Current / Opening Balance"
                                }
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Text(
                text = "Badge Color & Icon",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                swatchPresets.forEach { hex ->
                    val swatch = parseHexColorSafely(hex)
                    val selected = state.colorHex.equals(hex, ignoreCase = true)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(swatch)
                            .clickable { onUpdateState { it.copy(colorHex = hex) } }
                    ) {
                        if (selected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                iconPresets.forEach { iconKey ->
                    val isSelected = state.iconName == iconKey
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                semanticColors.cardStrokeSubtle
                            }
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onUpdateState { it.copy(iconName = iconKey) } }
                            .padding(8.dp)
                    ) {
                        CategoryIconBadge(
                            iconName = iconKey,
                            colorHex = state.colorHex,
                            size = 32.dp,
                            iconSize = 18.dp
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Include in Net Worth Overview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Counts this account towards total Assets or Liabilities",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.includeInNetWorth,
                    onCheckedChange = { checked ->
                        onUpdateState { it.copy(includeInNetWorth = checked) }
                    }
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isEditing) {
                    OutlinedButton(
                        onClick = onToggleArchive,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (state.isArchived) {
                                Icons.Filled.Unarchive
                            } else {
                                Icons.Filled.Archive
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (state.isArchived) "Unarchive" else "Archive")
                    }
                }

                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isEditing) "Save Changes" else "Create Account")
                }
            }
        }
    }

    if (showStatementDayPicker) {
        DayOfMonthPickerDialog(
            title = "Select Statement Closing Day (1–31)",
            subtitle = "Months with fewer than 31 days automatically clamp to the last day of that month.",
            selectedDay = state.settlementDayOfMonth,
            onSelectDay = { day ->
                onUpdateState { it.copy(settlementDayOfMonth = day.coerceIn(1, 31)) }
                showStatementDayPicker = false
            },
            onDismiss = { showStatementDayPicker = false }
        )
    }

    if (showPaymentDueDayPicker) {
        DayOfMonthPickerDialog(
            title = "Select Payment Due Day (1–31)",
            subtitle = "The day of the month your billed credit card statement is due for payment.",
            selectedDay = state.paymentDueDayOfMonth,
            onSelectDay = { day ->
                onUpdateState { it.copy(paymentDueDayOfMonth = day.coerceIn(1, 31)) }
                showPaymentDueDayPicker = false
            },
            onDismiss = { showPaymentDueDayPicker = false }
        )
    }
}

@Composable
private fun DayOfMonthPickerDialog(
    title: String,
    subtitle: String,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..31).forEach { day ->
                        FilterChip(
                            selected = selectedDay == day,
                            onClick = { onSelectDay(day) },
                            label = {
                                Text(
                                    text = day.toString(),
                                    style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                                    fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreditCardSettlementBottomSheet(
    state: CreditCardSettlementSheetState,
    assetAccounts: List<AccountSummary>,
    grouping: NumberGroupingSystem,
    isSaving: Boolean,
    onSelectPreset: (SettlementAmountPreset) -> Unit,
    onUpdateState: ((CreditCardSettlementSheetState) -> CreditCardSettlementSheetState) -> Unit,
    onConfirmSettlement: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cardSpec = remember(state.creditCardAccount.currencyCode) {
        SupportedCurrencies.fromCode(state.creditCardAccount.currencyCode)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(
                        iconName = Icons.Filled.CreditCard.name,
                        colorHex = state.creditCardAccount.colorHex,
                        size = 40.dp,
                        iconSize = 20.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Settle Credit Card Bill",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.creditCardAccount.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                }
            }

            if (!state.validationError.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LocalFolioSemanticColors.current.expenseCoralContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.validationError,
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalFolioSemanticColors.current.onExpenseCoralContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, LocalFolioSemanticColors.current.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Statement Payable",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = state.creditCardAccount.creditCardStatementPayable ?: MinorMoney.ZERO,
                            currencySpec = cardSpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Outstanding Debt",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = state.creditCardAccount.currentBalance.abs(),
                            currencySpec = cardSpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.NEUTRAL,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "Settlement Amount Option",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = state.preset == SettlementAmountPreset.STATEMENT_PAYABLE,
                    onClick = { onSelectPreset(SettlementAmountPreset.STATEMENT_PAYABLE) },
                    label = { Text("Statement Due") }
                )
                FilterChip(
                    selected = state.preset == SettlementAmountPreset.FULL_OUTSTANDING,
                    onClick = { onSelectPreset(SettlementAmountPreset.FULL_OUTSTANDING) },
                    label = { Text("Full Debt") }
                )
                FilterChip(
                    selected = state.preset == SettlementAmountPreset.CUSTOM,
                    onClick = { onSelectPreset(SettlementAmountPreset.CUSTOM) },
                    label = { Text("Custom") }
                )
            }

            OutlinedTextField(
                value = state.amountInput,
                onValueChange = { v ->
                    onUpdateState {
                        it.copy(preset = SettlementAmountPreset.CUSTOM, amountInput = v)
                    }
                },
                label = { Text("Settlement Amount (${cardSpec.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Pay From Asset Account",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            if (assetAccounts.isEmpty()) {
                Text(
                    text = "No active Bank or Cash account available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalFolioSemanticColors.current.expenseCoral
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    assetAccounts.forEach { asset ->
                        val isSelected = state.selectedSourceAccountId == asset.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onUpdateState { it.copy(selectedSourceAccountId = asset.id) }
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(asset.name)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    MoneyText(
                                        money = asset.currentBalance,
                                        currencySpec = SupportedCurrencies.fromCode(asset.currencyCode),
                                        grouping = grouping,
                                        colorMode = MoneyColorMode.AUTO_SIGN,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.note,
                onValueChange = { v -> onUpdateState { it.copy(note = v) } },
                label = { Text("Settlement Note") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onConfirmSettlement,
                    enabled = !isSaving && assetAccounts.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record Settlement")
                }
            }
        }
    }
}

fun formatAccountGroupTitle(group: AccountGroup): String = when (group) {
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
// File: app/src/main/java/com/folioledger/app/ui/ledger/LedgerHomeScreen.kt
package com.folioledger.app.ui.ledger

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.PeriodSelectorHeader
import com.folioledger.app.ui.components.ThreeColumnSummaryBanner
import com.folioledger.app.ui.components.fromTransactionType
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LedgerHomeScreen(
    viewModel: LedgerHomeViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onOpenNewTransactionForDate: (epochDay: Long) -> Unit = {},
    onEditTransaction: (TransactionItem) -> Unit = {},
    onSelectBookmarkTemplate: (TransactionBookmark) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showBookmarksQuickDialog by rememberSaveable { mutableStateOf(false) }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportActivePeriodToCsv(uri.toString())
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = contentPadding.calculateTopPadding() + scaffoldPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + scaffoldPadding.calculateBottomPadding()
                )
        ) {
            PeriodSelectorHeader(
                periodTitle = uiState.activePeriodRange.title,
                periodSubtitle = uiState.activePeriodRange.subtitle,
                onPreviousPeriod = viewModel::navigateToPreviousPeriod,
                onNextPeriod = viewModel::navigateToNextPeriod,
                onPeriodTitleClick = viewModel::jumpToToday,
                onBookmarksClick = { showBookmarksQuickDialog = true },
                onFilterClick = { viewModel.setFilterSheetOpen(true) },
                activeFilterCount = uiState.activeFilterCount,
                onSearchClick = viewModel::toggleSearchBar
            )

            // Prominent Scoped Account Filter Banner when an account is selected
            val scopedAccount = uiState.scopedAccount
            AnimatedVisibility(
                visible = scopedAccount != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                if (scopedAccount != null) {
                    SelectedAccountScopeBanner(
                        account = scopedAccount,
                        grouping = uiState.numberGrouping,
                        onClearScopedAccount = { viewModel.setScopedAccount(null) }
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.isSearchBarVisible || uiState.isFilterActive,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                SearchAndActiveFilterBanner(
                    queryText = uiState.searchFilter.queryText,
                    activeFilterCount = uiState.activeFilterCount,
                    isFilterActive = uiState.isFilterActive,
                    periodTitle = uiState.activePeriodRange.title,
                    filterAllPeriods = uiState.filterAllPeriods,
                    onQueryChange = viewModel::updateSearchQuery,
                    onOpenFilterSheet = { viewModel.setFilterSheetOpen(true) },
                    onClearAllFilters = viewModel::clearAllFilters
                )
            }

            if (!uiState.isFilterActive) {
                PrimaryScrollableTabRow(
                    selectedTabIndex = uiState.selectedSubTab.ordinal,
                    edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {}
                ) {
                    LedgerSubTab.entries.forEach { tab ->
                        val isSelected = uiState.selectedSubTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.selectSubTab(tab) },
                            text = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = LocalFolioSemanticColors.current.cardStrokeSubtle)

            if (uiState.isFilterActive) {
                FilteredSearchResultsContent(
                    uiState = uiState,
                    onTransactionClick = viewModel::inspectTransaction,
                    onToggleFilterAllPeriods = viewModel::setFilterAllPeriods,
                    onClearFilters = viewModel::clearAllFilters
                )
            } else {
                when (uiState.selectedSubTab) {
                    LedgerSubTab.DAILY -> {
                        DailyLedgerSubTabContent(
                            uiState = uiState,
                            onTransactionClick = viewModel::inspectTransaction,
                            onAddTransactionClick = {
                                onOpenNewTransactionForDate(uiState.referenceDateEpochDay)
                            }
                        )
                    }

                    LedgerSubTab.CALENDAR -> {
                        CalendarLedgerSubTabContent(
                            uiState = uiState,
                            onSelectDay = viewModel::selectCalendarDay,
                            onAddOnDate = onOpenNewTransactionForDate,
                            onTransactionClick = viewModel::inspectTransaction
                        )
                    }

                    LedgerSubTab.WEEKLY -> {
                        WeeklyLedgerSubTabContent(
                            uiState = uiState,
                            onToggleWeek = viewModel::toggleWeekExpanded,
                            onTransactionClick = viewModel::inspectTransaction
                        )
                    }

                    LedgerSubTab.MONTHLY -> {
                        MonthlyLedgerSubTabContent(
                            uiState = uiState,
                            onToggleMonth = viewModel::toggleYearlyMonthExpanded
                        )
                    }

                    LedgerSubTab.SUMMARY -> {
                        SummaryLedgerSubTabContent(
                            uiState = uiState,
                            onExportCsvClick = {
                                val sanitizedTitle = uiState.activePeriodRange.title.replace(' ', '_')
                                csvExportLauncher.launch("Folio_Ledger_$sanitizedTitle.csv")
                            }
                        )
                    }
                }
            }
        }
    }

    val inspected = uiState.inspectedTransaction
    if (inspected != null) {
        TransactionDetailActionSheet(
            transaction = inspected,
            currencySpec = uiState.currencySpec,
            grouping = uiState.numberGrouping,
            onEdit = {
                viewModel.inspectTransaction(null)
                onEditTransaction(inspected)
            },
            onDelete = {
                viewModel.deleteTransaction(inspected.id)
            },
            onDismiss = { viewModel.inspectTransaction(null) }
        )
    }

    if (uiState.isFilterSheetOpen) {
        ReinforcedSearchFilterBottomSheet(
            uiState = uiState,
            onQueryChange = viewModel::updateSearchQuery,
            onToggleType = viewModel::toggleFilterTransactionType,
            onToggleAccount = viewModel::toggleFilterAccountId,
            onToggleCategory = viewModel::toggleFilterCategoryId,
            onToggleTag = viewModel::toggleFilterTag,
            onUpdateAmountBounds = viewModel::updateFilterAmountBounds,
            onFilterAllPeriodsChange = viewModel::setFilterAllPeriods,
            onClearAll = viewModel::clearAllFilters,
            onDismiss = { viewModel.setFilterSheetOpen(false) }
        )
    }

    if (showBookmarksQuickDialog) {
        QuickBookmarksLauncherDialog(
            bookmarks = uiState.bookmarks,
            currencySpec = uiState.currencySpec,
            grouping = uiState.numberGrouping,
            onSelectBookmark = { bm ->
                showBookmarksQuickDialog = false
                onSelectBookmarkTemplate(bm)
            },
            onDismiss = { showBookmarksQuickDialog = false }
        )
    }
}

@Composable
private fun SelectedAccountScopeBanner(
    account: AccountSummary,
    grouping: NumberGroupingSystem,
    onClearScopedAccount: () -> Unit
) {
    val accountSpec = remember(account.currencyCode) {
        SupportedCurrencies.fromCode(account.currencyCode)
    }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                CategoryIconBadge(
                    iconName = account.iconName,
                    colorHex = account.colorHex,
                    size = 30.dp,
                    iconSize = 16.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Viewing Account: ${account.name}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Balance: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = account.currentBalance,
                            currencySpec = accountSpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.AUTO_SIGN,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onClearScopedAccount)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Show All Accounts",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear Account Scope",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchAndActiveFilterBanner(
    queryText: String,
    activeFilterCount: Int,
    isFilterActive: Boolean,
    periodTitle: String,
    filterAllPeriods: Boolean,
    onQueryChange: (String) -> Unit,
    onOpenFilterSheet: () -> Unit,
    onClearAllFilters: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = queryText,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            if (filterAllPeriods) {
                                "Search across all dates..."
                            } else {
                                "Search in $periodTitle..."
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (queryText.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear Search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                FilledTonalButton(
                    onClick = onOpenFilterSheet,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (activeFilterCount > 0) "Filters ($activeFilterCount)" else "Filters",
                        style = tabularMoneyStyle(MaterialTheme.typography.labelLarge)
                    )
                }

                if (isFilterActive) {
                    TextButton(onClick = onClearAllFilters) {
                        Text("Reset")
                    }
                }
            }
        }
    }
}

@Composable
private fun FilteredSearchResultsContent(
    uiState: LedgerHomeUiState,
    onTransactionClick: (TransactionItem) -> Unit,
    onToggleFilterAllPeriods: (Boolean) -> Unit,
    onClearFilters: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ThreeColumnSummaryBanner(
                firstLabel = "Filtered Income",
                firstAmount = uiState.filteredIncomeTotal,
                secondLabel = "Filtered Expenses",
                secondAmount = uiState.filteredExpenseTotal,
                thirdLabel = "Filtered Net",
                thirdAmount = uiState.filteredNetTotal,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping
            )
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${uiState.filteredTransactions.size} matches",
                        style = tabularMoneyStyle(MaterialTheme.typography.labelLarge),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FilterChip(
                        selected = !uiState.filterAllPeriods,
                        onClick = { onToggleFilterAllPeriods(!uiState.filterAllPeriods) },
                        label = {
                            Text(
                                text = if (uiState.filterAllPeriods) {
                                    "All Dates"
                                } else {
                                    "In ${uiState.activePeriodRange.title}"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }

                TextButton(onClick = onClearFilters) {
                    Text("Exit Filter")
                }
            }
        }

        if (uiState.filteredDailyGroups.isEmpty()) {
            item {
                EmptyStateCard(
                    title = if (uiState.filterAllPeriods) {
                        "No Matching Transactions"
                    } else {
                        "No Matches in ${uiState.activePeriodRange.title}"
                    },
                    subtitle = if (uiState.filterAllPeriods) {
                        "No ledger entries matched your search query and multi-filter criteria."
                    } else {
                        "Use < Prev / Next > above to check other months, or tap 'In ${uiState.activePeriodRange.title}' to search across all dates."
                    },
                    icon = Icons.Filled.Search,
                    actionLabel = "Clear All Filters",
                    onActionClick = onClearFilters
                )
            }
        } else {
            uiState.filteredDailyGroups.forEach { group ->
                item(key = "filtered_day_${group.epochDay}") {
                    DailyDateGroupHeaderRow(
                        group = group,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )
                }
                items(group.transactions, key = { "filtered_tx_${it.id}" }) { tx ->
                    LedgerTransactionItemCard(
                        transaction = tx,
                        scopedAccountId = uiState.scopedAccountId,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping,
                        onClick = { onTransactionClick(tx) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyLedgerSubTabContent(
    uiState: LedgerHomeUiState,
    onTransactionClick: (TransactionItem) -> Unit,
    onAddTransactionClick: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ThreeColumnSummaryBanner(
                periodSummary = uiState.periodSummary,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                showCarryOver = uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null
            )
        }

        if (uiState.dailyGroups.isEmpty()) {
            item {
                val scopedName = uiState.scopedAccount?.name
                EmptyStateCard(
                    title = if (scopedName != null) {
                        "No Transactions for $scopedName in ${uiState.activePeriodRange.title}"
                    } else {
                        "No Transactions in ${uiState.activePeriodRange.title}"
                    },
                    subtitle = "Record an expense, income, or transfer to populate your daily chronological ledger.",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    actionLabel = "+ Add Transaction",
                    onActionClick = onAddTransactionClick
                )
            }
        } else {
            uiState.dailyGroups.forEach { group ->
                item(key = "day_header_${group.epochDay}") {
                    DailyDateGroupHeaderRow(
                        group = group,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )
                }
                items(group.transactions, key = { it.id }) { transaction ->
                    LedgerTransactionItemCard(
                        transaction = transaction,
                        scopedAccountId = uiState.scopedAccountId,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyDateGroupHeaderRow(
    group: DailyLedgerGroup,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp)
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

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

@Composable
private fun LedgerTransactionItemCard(
    transaction: TransactionItem,
    scopedAccountId: Long? = null,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onClick: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
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
}

@Composable
private fun CalendarLedgerSubTabContent(
    uiState: LedgerHomeUiState,
    onSelectDay: (Long) -> Unit,
    onAddOnDate: (Long) -> Unit,
    onTransactionClick: (TransactionItem) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val selectedDateFormatted = remember(uiState.selectedCalendarDayEpochDay) {
        LocalDate.ofEpochDay(uiState.selectedCalendarDayEpochDay)
            .format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale.US))
    }

    val weeksOfCells = remember(uiState.calendarCells) {
        uiState.calendarCells.chunked(7)
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ThreeColumnSummaryBanner(
                periodSummary = uiState.periodSummary,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                showCarryOver = uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        uiState.calendarWeekdayHeaders.forEach { header ->
                            Text(
                                text = header,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    weeksOfCells.forEach { weekRow ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            weekRow.forEach { cell ->
                                val isSelected = cell.epochDay != null &&
                                        cell.epochDay == uiState.selectedCalendarDayEpochDay
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        !cell.isInActivePeriod -> Color.Transparent
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                                        cell.isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                                        else -> MaterialTheme.colorScheme.surfaceContainer
                                    },
                                    border = if (isSelected) {
                                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                    } else {
                                        null
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(62.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .then(
                                            if (cell.epochDay != null) {
                                                Modifier.clickable { onSelectDay(cell.epochDay) }
                                            } else {
                                                Modifier
                                            }
                                        )
                                ) {
                                    if (cell.isInActivePeriod && cell.dayNumber != null) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                        ) {
                                            Text(
                                                text = cell.dayNumber.toString(),
                                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                                fontWeight = if (isSelected || cell.isToday) {
                                                    FontWeight.Bold
                                                } else {
                                                    FontWeight.Medium
                                                },
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                if (cell.incomeTotal.isPositive) {
                                                    MoneyText(
                                                        money = cell.incomeTotal,
                                                        currencySpec = uiState.currencySpec,
                                                        grouping = uiState.numberGrouping,
                                                        includeSymbol = false,
                                                        colorMode = MoneyColorMode.INCOME,
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                                if (cell.expenseTotal.isPositive) {
                                                    MoneyText(
                                                        money = cell.expenseTotal,
                                                        currencySpec = uiState.currencySpec,
                                                        grouping = uiState.numberGrouping,
                                                        includeSymbol = false,
                                                        colorMode = MoneyColorMode.EXPENSE,
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedDateFormatted,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "${uiState.selectedCalendarDayTransactions.size} txns",
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (uiState.selectedCalendarDayIncome.isPositive) {
                                MoneyText(
                                    money = uiState.selectedCalendarDayIncome,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.INCOME,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    showPlusSignForPositive = true
                                )
                            }
                            if (uiState.selectedCalendarDayExpense.isPositive) {
                                MoneyText(
                                    money = uiState.selectedCalendarDayExpense,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    FilledTonalButton(
                        onClick = { onAddOnDate(uiState.selectedCalendarDayEpochDay) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add on this date",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (uiState.selectedCalendarDayTransactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No transactions recorded on this date. Tap \"+ Add on this date\" above to log an entry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(
                items = uiState.selectedCalendarDayTransactions,
                key = { "cal_tx_${it.id}" }
            ) { tx ->
                LedgerTransactionItemCard(
                    transaction = tx,
                    scopedAccountId = uiState.scopedAccountId,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.numberGrouping,
                    onClick = { onTransactionClick(tx) }
                )
            }
        }
    }
}

@Composable
private fun WeeklyLedgerSubTabContent(
    uiState: LedgerHomeUiState,
    onToggleWeek: (Int) -> Unit,
    onTransactionClick: (TransactionItem) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ThreeColumnSummaryBanner(
                periodSummary = uiState.periodSummary,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                showCarryOver = uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null
            )
        }

        items(uiState.weeklyBuckets, key = { it.weekIndex }) { bucket ->
            val isExpanded = bucket.weekIndex in uiState.expandedWeekIndices

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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleWeek(bucket.weekIndex) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isExpanded) {
                                    Icons.Filled.ExpandLess
                                } else {
                                    Icons.Filled.ExpandMore
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${bucket.label} · ${bucket.dateRangeSubtitle}",
                                    style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${bucket.transactions.size} transactions",
                                    style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MoneyText(
                                    money = bucket.incomeTotal,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.INCOME,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    showPlusSignForPositive = true
                                )
                                MoneyText(
                                    money = bucket.expenseTotal,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            MoneyText(
                                money = bucket.netTotal,
                                currencySpec = uiState.currencySpec,
                                grouping = uiState.numberGrouping,
                                colorMode = MoneyColorMode.AUTO_SIGN,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                showPlusSignForPositive = true
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isExpanded && bucket.transactions.isNotEmpty(),
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                        ) {
                            HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                            bucket.transactions.forEach { tx ->
                                LedgerTransactionItemCard(
                                    transaction = tx,
                                    scopedAccountId = uiState.scopedAccountId,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    onClick = { onTransactionClick(tx) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyLedgerSubTabContent(
    uiState: LedgerHomeUiState,
    onToggleMonth: (Int) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ThreeColumnSummaryBanner(
                periodSummary = uiState.periodSummary,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                showCarryOver = uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null
            )
        }

        item {
            Text(
                text = "${uiState.activePeriodRange.anchorYear} Financial Year (12 Months)",
                style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(uiState.yearlyMonthItems.reversed(), key = { it.monthIndex }) { monthItem ->
            val isExpanded = monthItem.monthIndex in uiState.expandedYearlyMonths

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (monthItem.isCurrentActiveMonth) {
                        MaterialTheme.colorScheme.surfaceContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    }
                ),
                border = BorderStroke(
                    width = if (monthItem.isCurrentActiveMonth) 1.5.dp else 1.dp,
                    color = if (monthItem.isCurrentActiveMonth) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        semanticColors.cardStrokeSubtle
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleMonth(monthItem.monthIndex) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isExpanded) {
                                    Icons.Filled.ExpandLess
                                } else {
                                    Icons.Filled.ExpandMore
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = monthItem.periodRange.title,
                                    style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${monthItem.periodRange.subtitle} · ${monthItem.transactionCount} txns",
                                    style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MoneyText(
                                    money = monthItem.incomeTotal,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.INCOME,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                MoneyText(
                                    money = monthItem.expenseTotal,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            MoneyText(
                                money = monthItem.netTotal,
                                currencySpec = uiState.currencySpec,
                                grouping = uiState.numberGrouping,
                                colorMode = MoneyColorMode.AUTO_SIGN,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                showPlusSignForPositive = true
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 14.dp, bottom = 12.dp)
                        ) {
                            HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                            monthItem.weeklyBuckets.forEach { week ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${week.label} (${week.dateRangeSubtitle})",
                                        style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        MoneyText(
                                            money = week.incomeTotal,
                                            currencySpec = uiState.currencySpec,
                                            grouping = uiState.numberGrouping,
                                            colorMode = MoneyColorMode.INCOME,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        MoneyText(
                                            money = week.expenseTotal,
                                            currencySpec = uiState.currencySpec,
                                            grouping = uiState.numberGrouping,
                                            colorMode = MoneyColorMode.EXPENSE,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        MoneyText(
                                            money = week.netTotal,
                                            currencySpec = uiState.currencySpec,
                                            grouping = uiState.numberGrouping,
                                            colorMode = MoneyColorMode.AUTO_SIGN,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryLedgerSubTabContent(
    uiState: LedgerHomeUiState,
    onExportCsvClick: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val summary = uiState.periodSummary
    val closingBalanceWithCarryOver = summary.netCashflow + summary.carryOverFromPreviousPeriod
    val overallBudget = uiState.budgetProgressList.firstOrNull { it.categoryId == null }
        ?: uiState.budgetProgressList.firstOrNull()

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ThreeColumnSummaryBanner(
                periodSummary = summary,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                showCarryOver = uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null
            )
        }

        item {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${uiState.activePeriodRange.title} Cashflow Digest",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        val savingsRateText = remember(uiState.savingsRatePercent) {
                            String.format(Locale.US, "Savings Rate: %.1f%%", uiState.savingsRatePercent)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (uiState.savingsRatePercent >= 0f) {
                                semanticColors.incomeEmeraldContainer
                            } else {
                                semanticColors.expenseCoralContainer
                            }
                        ) {
                            Text(
                                text = savingsRateText,
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.savingsRatePercent >= 0f) {
                                    semanticColors.onIncomeEmeraldContainer
                                } else {
                                    semanticColors.onExpenseCoralContainer
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = semanticColors.cardStrokeSubtle)

                    SummaryMetricRow(
                        label = "Total Period Income",
                        amount = summary.totalIncome,
                        colorMode = MoneyColorMode.INCOME,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )
                    SummaryMetricRow(
                        label = "Total Period Expenses",
                        amount = summary.totalExpense,
                        colorMode = MoneyColorMode.EXPENSE,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )
                    SummaryMetricRow(
                        label = "Net Period Cashflow",
                        amount = summary.netCashflow,
                        colorMode = MoneyColorMode.AUTO_SIGN,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )

                    if (uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null) {
                        HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                        SummaryMetricRow(
                            label = "Carry-Over from Previous Period",
                            amount = summary.carryOverFromPreviousPeriod,
                            colorMode = MoneyColorMode.AUTO_SIGN,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping
                        )
                        SummaryMetricRow(
                            label = "Closing Cumulative Balance",
                            amount = closingBalanceWithCarryOver,
                            colorMode = MoneyColorMode.AUTO_SIGN,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping
                        )
                    }
                }
            }
        }

        if (overallBudget != null && uiState.scopedAccountId == null) {
            item {
                val fraction = (overallBudget.usagePercentage / 100f).coerceIn(0f, 1f)
                val barColor = when {
                    overallBudget.usagePercentage >= 100f -> semanticColors.expenseCoral
                    overallBudget.usagePercentage >= overallBudget.alertThresholdPercent -> semanticColors.warningAmber
                    else -> semanticColors.incomeEmerald
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
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Budget Status (${overallBudget.categoryName})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f%% used", overallBudget.usagePercentage),
                                style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                                fontWeight = FontWeight.Bold,
                                color = barColor
                            )
                        }

                        LinearProgressIndicator(
                            progress = { fraction },
                            color = barColor,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row {
                                Text(
                                    text = "Spent: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = overallBudget.spentAmount,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row {
                                Text(
                                    text = "Remaining: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = overallBudget.remainingAmount,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.AUTO_SIGN,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.topExpenseCategories.isNotEmpty() && uiState.scopedAccountId == null) {
            item {
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
                        Text(
                            text = "Top Expense Categories",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.topExpenseCategories.forEach { stat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CategoryIconBadge(
                                    iconName = stat.iconName,
                                    colorHex = stat.colorHex,
                                    size = 32.dp,
                                    iconSize = 16.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stat.categoryName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = String.format(
                                            Locale.US,
                                            "%.1f%% · %d txns",
                                            stat.percentageOfTotal,
                                            stat.transactionCount
                                        ),
                                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                MoneyText(
                                    money = stat.netAmount,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onExportCsvClick,
                enabled = !uiState.isExportingCsv,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isExportingCsv) {
                        "Exporting CSV..."
                    } else {
                        "Export ${uiState.activePeriodRange.title} Transactions to CSV"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SummaryMetricRow(
    label: String,
    amount: MinorMoney,
    colorMode: MoneyColorMode,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        MoneyText(
            money = amount,
            currencySpec = currencySpec,
            grouping = grouping,
            colorMode = colorMode,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            showPlusSignForPositive = colorMode == MoneyColorMode.AUTO_SIGN
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TransactionDetailActionSheet(
    transaction: TransactionItem,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    val dateFormatted = remember(transaction.ledgerDateEpochDay) {
        LocalDate.ofEpochDay(transaction.ledgerDateEpochDay)
            .format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale.US))
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
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(
                        iconName = transaction.categoryIconName,
                        colorHex = transaction.categoryColorHex,
                        size = 44.dp,
                        iconSize = 22.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = transaction.categoryName
                                ?: transaction.type.name.replace('_', ' '),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                MoneyText(
                    money = transaction.amount,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = MoneyColorMode.fromTransactionType(transaction.type),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (transaction.transferToAccountName != null) {
                        "${transaction.accountName} → ${transaction.transferToAccountName}"
                    } else {
                        transaction.accountName
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (transaction.payeeOrDescription.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Payee / Description",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = transaction.payeeOrDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (transaction.note.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (transaction.isSplit && transaction.splits.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Split Lines (${transaction.splits.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        transaction.splits.forEach { split ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = split.categoryName.ifBlank { "Split Item" },
                                    style = MaterialTheme.typography.bodySmall
                                )
                                MoneyText(
                                    money = split.amount,
                                    currencySpec = currencySpec,
                                    grouping = grouping,
                                    colorMode = MoneyColorMode.NEUTRAL,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            if (transaction.tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    transaction.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = semanticColors.expenseCoral
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete")
                }

                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Transaction")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ReinforcedSearchFilterBottomSheet(
    uiState: LedgerHomeUiState,
    onQueryChange: (String) -> Unit,
    onToggleType: (TransactionType) -> Unit,
    onToggleAccount: (Long) -> Unit,
    onToggleCategory: (Long) -> Unit,
    onToggleTag: (String) -> Unit,
    onUpdateAmountBounds: (String, String) -> Unit,
    onFilterAllPeriodsChange: (Boolean) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filter = uiState.searchFilter

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
                Text(
                    text = "Reinforced Search & Multi-Filter",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    TextButton(onClick = onClearAll) {
                        Text("Reset All")
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            }

            OutlinedTextField(
                value = filter.queryText,
                onValueChange = onQueryChange,
                label = { Text("Keyword (Payee, Description, Note)") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Scope Filter to Selected Month (${uiState.activePeriodRange.title})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Follows < Prev / Next > month navigation automatically",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = !uiState.filterAllPeriods,
                    onCheckedChange = { scopeToMonth -> onFilterAllPeriodsChange(!scopeToMonth) }
                )
            }

            Text(
                text = "Transaction Types",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TransactionType.entries.forEach { type ->
                    FilterChip(
                        selected = type in filter.types,
                        onClick = { onToggleType(type) },
                        label = { Text(type.name.replace('_', ' ')) }
                    )
                }
            }

            if (uiState.availableAccounts.isNotEmpty()) {
                Text(
                    text = "Accounts",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.availableAccounts.forEach { account ->
                        FilterChip(
                            selected = account.id in filter.accountIds,
                            onClick = { onToggleAccount(account.id) },
                            label = { Text(account.name) }
                        )
                    }
                }
            }

            if (uiState.availableCategories.isNotEmpty()) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.availableCategories.forEach { cat ->
                        FilterChip(
                            selected = cat.id in filter.categoryIds,
                            onClick = { onToggleCategory(cat.id) },
                            label = { Text(cat.name) }
                        )
                    }
                }
            }

            if (uiState.availableTags.isNotEmpty()) {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.availableTags.forEach { tag ->
                        FilterChip(
                            selected = tag in filter.tags,
                            onClick = { onToggleTag(tag) },
                            label = { Text("#$tag") }
                        )
                    }
                }
            }

            Text(
                text = "Amount Range (${uiState.currencySpec.symbol})",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.minAmountFilterInput,
                    onValueChange = { minVal ->
                        onUpdateAmountBounds(minVal, uiState.maxAmountFilterInput)
                    },
                    label = { Text("Min Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.maxAmountFilterInput,
                    onValueChange = { maxVal ->
                        onUpdateAmountBounds(uiState.minAmountFilterInput, maxVal)
                    },
                    label = { Text("Max Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show Matching Results (${uiState.filteredTransactions.size})")
            }
        }
    }
}

@Composable
private fun QuickBookmarksLauncherDialog(
    bookmarks: List<TransactionBookmark>,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onSelectBookmark: (TransactionBookmark) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = LocalFolioSemanticColors.current.warningAmber
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quick Bookmarks (★)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            if (bookmarks.isEmpty()) {
                Text(
                    text = "No saved bookmark templates yet. Open the + New Transaction composer and tap ★ to save frequent templates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(bookmarks, key = { it.id }) { bm ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectBookmark(bm) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bm.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (bm.payeeOrDescription.isNotBlank()) {
                                        Text(
                                            text = bm.payeeOrDescription,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                MoneyText(
                                    money = bm.amount,
                                    currencySpec = currencySpec,
                                    grouping = grouping,
                                    colorMode = MoneyColorMode.fromTransactionType(bm.type),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
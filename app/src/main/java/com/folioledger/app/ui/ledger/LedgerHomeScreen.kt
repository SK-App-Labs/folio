package com.folioledger.app.ui.ledger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.ui.components.PeriodSelectorHeader
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

@Composable
fun LedgerHomeScreen(
    viewModel: LedgerHomeViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onOpenNewTransactionForDate: (epochDay: Long) -> Unit = {},
    onEditTransaction: (TransactionItem) -> Unit = {},
    onSelectBookmarkTemplate: (TransactionBookmark) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showBookmarksQuickDialog by rememberSaveable { mutableStateOf(false) }

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
                                viewModel.exportActivePeriodToCsv("Folio_$sanitizedTitle.csv")
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

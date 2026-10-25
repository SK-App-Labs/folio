package com.folioledger.app.ui.ledger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.ui.components.PeriodSelectorHeader
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import java.time.LocalDate
import kotlinx.coroutines.launch

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

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = uiState.selectedSubTab.ordinal) { LedgerSubTab.entries.size }

    // Sync ViewModel's selectedSubTab ONLY when user swipe gesture settles
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { settledPage ->
            val targetTab = LedgerSubTab.entries[settledPage]
            if (uiState.selectedSubTab != targetTab) {
                viewModel.selectSubTab(targetTab)
            }
        }
    }

    // Sync pagerState when ViewModel's selectedSubTab changes externally (e.g. Tab click)
    LaunchedEffect(uiState.selectedSubTab) {
        if (pagerState.currentPage != uiState.selectedSubTab.ordinal && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(uiState.selectedSubTab.ordinal)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = contentPadding.calculateTopPadding() + scaffoldPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + scaffoldPadding.calculateBottomPadding()
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val isMonthlyTab = uiState.selectedSubTab == LedgerSubTab.MONTHLY
                val headerTitle = if (isMonthlyTab) {
                    "Year ${uiState.activePeriodRange.anchorYear}"
                } else {
                    uiState.activePeriodRange.title
                }

                PeriodSelectorHeader(
                    periodTitle = headerTitle,
                    periodSubtitle = null,
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
                        selectedTabIndex = pagerState.currentPage,
                        edgePadding = 12.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        divider = {}
                    ) {
                        LedgerSubTab.entries.forEach { tab ->
                            val isSelected = pagerState.currentPage == tab.ordinal
                            Tab(
                                selected = isSelected,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(tab.ordinal)
                                    }
                                },
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
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 1,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (LedgerSubTab.entries[page]) {
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

            // Floating "Jump to Today ⤶" Quick Action Badge
            val todayEpochDay = remember { LocalDate.now().toEpochDay() }
            val isNotToday = uiState.referenceDateEpochDay != todayEpochDay
            AnimatedVisibility(
                visible = isNotToday && !uiState.isFilterActive,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.jumpToToday() },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = "Jump to Today",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Jump to Today ⤶",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
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

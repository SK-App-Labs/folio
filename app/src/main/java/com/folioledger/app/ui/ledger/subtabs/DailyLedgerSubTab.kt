package com.folioledger.app.ui.ledger.subtabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.ThreeColumnSummaryBanner
import com.folioledger.app.ui.ledger.*
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun DailyLedgerSubTabContent(
    uiState: LedgerHomeUiState,
    onTransactionClick: (TransactionItem) -> Unit,
    onAddTransactionClick: () -> Unit
) {
    val lazyListState = rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(uiState.scrollToTopSignal) {
        if (uiState.scrollToTopSignal > 0) {
            lazyListState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = "daily_summary_banner") {
            ThreeColumnSummaryBanner(
                periodSummary = uiState.periodSummary,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                showCarryOver = uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null
            )
        }

        if (uiState.dailyGroups.isEmpty()) {
            item(key = "daily_empty_state") {
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
                val headerKey = "day_header_${group.epochDay}"
                stickyHeader(key = headerKey, contentType = "day_group_header") {
                    DailyDateGroupHeaderRow(
                        group = group,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )
                }
                item(key = "day_card_${group.epochDay}", contentType = "day_group_card") {
                    DailyGroupedTransactionsCard(
                        transactions = group.transactions,
                        scopedAccountId = uiState.scopedAccountId,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping,
                        onTransactionClick = onTransactionClick
                    )
                }
            }
        }
    }
}

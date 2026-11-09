package com.folioledger.app.ui.stats

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CardMilestoneConfig
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.util.CardMilestoneCalculator
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.SixMonthSmoothTrendAreaChart
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.stats.*
import com.folioledger.app.ui.stats.components.*
import com.folioledger.app.ui.stats.dialogs.*
import com.folioledger.app.ui.stats.sheets.*
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin


private val shortDateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
private val fullDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardsCockpitSubTabContent(
    uiState: StatsAndBudgetsUiState,
    onAddTransactionForAccount: ((AccountSummary) -> Unit)? = null,
    onSaveCardMilestone: ((accountId: Long, title: String, startDateEpochDay: Long, targetMinor: Long, feeMinor: Long?, autoRenew: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val cockpit = uiState.creditCockpitState
    val semanticColors = LocalFolioSemanticColors.current

    var selectedMilestoneCard by remember { mutableStateOf<CreditCardAnalyticsItem?>(null) }
    var isPaydownSimulatorOpen by remember { mutableStateOf(false) }
    var isFloatRankingOpen by remember { mutableStateOf(false) }

    if (cockpit.activeCards.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(24.dp)
        ) {
            EmptyStateCard(
                icon = Icons.Filled.CreditCard,
                title = "No Active Credit Cards",
                subtitle = "Add a credit card account from the Accounts tab to track credit health, billing cycle timelines, free-credit float days, and annual fee waiver milestones."
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // SECTION 1: HERO UTILIZATION GAUGE & CASH SAFETY HUB
        item(key = "hero_gauge_card") {
            CreditHeroArcGaugeCard(
                cockpit = cockpit,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                onOpenPaydownSimulator = { isPaydownSimulatorOpen = true }
            )
        }

        // SECTION 2: SMART SWIPE ADVISOR BANNER (ACTIONABLE)
        cockpit.smartSwipeRecommendation?.let { recommendation ->
            item(key = "smart_swipe_advisor") {
                SmartSwipeAdvisorBanner(
                    recommendation = recommendation,
                    onUseCard = { onAddTransactionForAccount?.invoke(recommendation.account) },
                    onOpenFloatRanking = { isFloatRankingOpen = true }
                )
            }
        }

        // SECTION 3: UNIFIED CARDS PORTFOLIO (ELIMINATES DUPLICATION & NESTED BOXES)
        if (cockpit.cardAnalyticsItems.isNotEmpty()) {
            item(key = "unified_cards_portfolio") {
                UnifiedCardsPortfolioSection(
                    cards = cockpit.cardAnalyticsItems,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.numberGrouping,
                    onOpenMilestoneEditor = { cardItem -> selectedMilestoneCard = cardItem }
                )
            }
        }

        // SECTION 4: SPEND MODE RATIO (CREDIT VS LIQUID)
        item(key = "spend_mode_ratio_card") {
            SpendModeRatioCard(
                ratio = cockpit.spendRatio,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                periodTitle = uiState.activePeriodRange.title
            )
        }

        // SECTION 5: 6-MONTH HISTORICAL CREDIT SPEND TREND
        if (cockpit.sixMonthCreditTrendBars.isNotEmpty()) {
            item(key = "six_month_credit_trend_card") {
                CreditSpendTrendCard(
                    bars = cockpit.sixMonthCreditTrendBars,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.numberGrouping
                )
            }
        }
    }

    // MODAL BOTTOM SHEETS & DIALOGS
    selectedMilestoneCard?.let { card ->
        CardMilestoneEditorBottomSheet(
            card = card,
            currencySpec = uiState.currencySpec,
            grouping = uiState.numberGrouping,
            onDismiss = { selectedMilestoneCard = null },
            onSave = { title, startDateEpoch, targetMinor, feeMinor, autoRenew ->
                onSaveCardMilestone?.invoke(
                    card.account.id,
                    title,
                    startDateEpoch,
                    targetMinor,
                    feeMinor,
                    autoRenew
                )
                selectedMilestoneCard = null
            }
        )
    }

    if (isPaydownSimulatorOpen) {
        CreditPaydownSimulatorBottomSheet(
            cockpit = cockpit,
            currencySpec = uiState.currencySpec,
            grouping = uiState.numberGrouping,
            onDismiss = { isPaydownSimulatorOpen = false }
        )
    }

    if (isFloatRankingOpen) {
        AllCardsFloatRankingDialog(
            cards = cockpit.cardAnalyticsItems,
            onDismiss = { isFloatRankingOpen = false },
            onSelectCard = { account ->
                isFloatRankingOpen = false
                onAddTransactionForAccount?.invoke(account)
            }
        )
    }
}

// File: app/src/main/java/com/folioledger/app/domain/util/CreditCardCockpitCalculator.kt
package com.folioledger.app.domain.util

import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CreditCardAnalyticsItem
import com.folioledger.app.domain.model.CreditCardCockpitUiState
import com.folioledger.app.domain.model.CreditCardHealthUiModel
import com.folioledger.app.domain.model.CreditCardTimelineItem
import com.folioledger.app.domain.model.CreditSpendRatio
import com.folioledger.app.domain.model.CreditUtilizationStatus
import com.folioledger.app.domain.model.LiquidCashCoverageState
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.SmartSwipeRecommendation
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object CreditCardCockpitCalculator {

    fun computeCreditCardHealthList(
        allAccounts: List<AccountSummary>
    ): List<CreditCardHealthUiModel> {
        val creditCards = allAccounts.filter { !it.isArchived && it.groupType == AccountGroup.CREDIT_CARD }
        return creditCards.map { card ->
            val limit = card.creditLimit ?: MinorMoney.ZERO
            val debt = card.currentBalance.abs()
            val payable = card.creditCardStatementPayable ?: MinorMoney.ZERO
            val unbilled = card.creditCardUnbilledAmount ?: MinorMoney.ZERO

            val available = if (limit > debt) limit - debt else MinorMoney.ZERO
            val utilization = if (limit.isPositive) {
                (debt.amountMinor.toFloat() / limit.amountMinor.toFloat() * 100f).coerceIn(0f, 100f)
            } else {
                0f
            }

            val status = when {
                utilization > 80f -> CreditUtilizationStatus.WARNING
                utilization > 30f -> CreditUtilizationStatus.MODERATE
                else -> CreditUtilizationStatus.SAFE
            }

            CreditCardHealthUiModel(
                account = card,
                statementPayable = payable,
                unbilledSpend = unbilled,
                totalDebt = debt,
                creditLimit = limit,
                availableCredit = available,
                utilizationPercent = utilization,
                status = status
            )
        }
    }

    fun computeCreditCockpitState(
        allAccounts: List<AccountSummary>,
        allTxs: List<TransactionItem>,
        periodTransactions: List<TransactionItem>,
        trendBars: List<MonthlyTrendBar>,
        referenceDateEpochDay: Long,
        milestonesJson: String? = null
    ): CreditCardCockpitUiState {
        val today = LocalDate.ofEpochDay(referenceDateEpochDay)
        val creditCards = allAccounts.filter { !it.isArchived && it.groupType == AccountGroup.CREDIT_CARD }
        val creditCardIds = creditCards.map { it.id }.toSet()

        if (creditCards.isEmpty()) {
            return CreditCardCockpitUiState(activeCards = emptyList())
        }

        // 1. Total Limits & Balances
        var totalLimitMinor = 0L
        var totalDebtMinor = 0L
        for (card in creditCards) {
            totalLimitMinor += card.creditLimit?.amountMinor ?: 0L
            totalDebtMinor += card.currentBalance.abs().amountMinor
        }
        val totalLimit = MinorMoney(totalLimitMinor)
        val totalCcDebt = MinorMoney(totalDebtMinor)
        val totalAvailable = if (totalLimit > totalCcDebt) totalLimit - totalCcDebt else MinorMoney.ZERO
        val aggregateUtilization = if (totalLimit.isPositive) {
            (totalCcDebt.amountMinor.toFloat() / totalLimit.amountMinor.toFloat() * 100f).coerceIn(0f, 100f)
        } else {
            0f
        }
        val aggregateStatus = when {
            aggregateUtilization > 50f -> CreditUtilizationStatus.WARNING
            aggregateUtilization > 30f -> CreditUtilizationStatus.MODERATE
            else -> CreditUtilizationStatus.SAFE
        }

        // 2. Liquid Cash Coverage
        val liquidAccounts = allAccounts.filter {
            !it.isArchived && (
                it.groupType == AccountGroup.BANK_ACCOUNT ||
                it.groupType == AccountGroup.CASH ||
                it.groupType == AccountGroup.SAVINGS ||
                it.groupType == AccountGroup.PREPAID_WALLET
            )
        }
        var totalLiquidMinor = 0L
        for (acc in liquidAccounts) {
            if (acc.currentBalance.isPositive) {
                totalLiquidMinor += acc.currentBalance.amountMinor
            }
        }
        val totalLiquidCash = MinorMoney(totalLiquidMinor)
        val netLiquidMinor = totalLiquidMinor - totalDebtMinor
        val netLiquidCash = MinorMoney(netLiquidMinor)
        val coverageRatio = if (totalDebtMinor > 0L) {
            totalLiquidMinor.toFloat() / totalDebtMinor.toFloat()
        } else {
            999f
        }
        val liquidCoverage = LiquidCashCoverageState(
            totalLiquidCash = totalLiquidCash,
            totalCcDebt = totalCcDebt,
            netLiquidCash = netLiquidCash,
            coverageRatio = coverageRatio,
            isCashBacked = totalLiquidMinor >= totalDebtMinor
        )

        // 3. Cycle Window, Timeline & Smart Swipe
        val currentYearStartEpoch = LocalDate.of(today.year, 1, 1).toEpochDay()
        val timelineItems = mutableListOf<CreditCardTimelineItem>()
        val cardAnalyticsItems = mutableListOf<CreditCardAnalyticsItem>()
        var bestSmartSwipe: SmartSwipeRecommendation? = null
        var maxFloatDays = -1L
        var singleCardExceeding30: CreditCardAnalyticsItem? = null
        val milestoneMap = CardMilestoneCalculator.parseMilestones(milestonesJson)

        for (card in creditCards) {
            val stmtDay = (card.settlementDayOfMonth ?: 1).coerceIn(1, 31)
            val dueDay = (card.paymentDueDayOfMonth ?: 20).coerceIn(1, 31)
            val cycle = CreditCardCycleCalculator.computeCycleWindow(
                referenceDateEpochDay = referenceDateEpochDay,
                settlementDayOfMonth = stmtDay,
                paymentDueDayOfMonth = dueDay
            )

            val cardDebt = card.currentBalance.abs()
            val cardLimit = card.creditLimit ?: MinorMoney.ZERO
            val cardAvailable = if (cardLimit > cardDebt) cardLimit - cardDebt else MinorMoney.ZERO
            val cardPayable = card.creditCardStatementPayable ?: MinorMoney.ZERO
            val cardUnbilled = card.creditCardUnbilledAmount ?: MinorMoney.ZERO

            val cardUtil = if (cardLimit.isPositive) {
                (cardDebt.amountMinor.toFloat() / cardLimit.amountMinor.toFloat() * 100f).coerceIn(0f, 100f)
            } else {
                0f
            }
            val cardStatus = when {
                cardUtil > 50f -> CreditUtilizationStatus.WARNING
                cardUtil > 30f -> CreditUtilizationStatus.MODERATE
                else -> CreditUtilizationStatus.SAFE
            }

            // Float days if swiped today (prioritize cards with available credit)
            val floatDays = ChronoUnit.DAYS.between(today, cycle.activeCycleNextDueDate).coerceAtLeast(0L)
            val hasAvailableHeadroom = cardAvailable.isPositive
            if (floatDays > maxFloatDays && hasAvailableHeadroom) {
                maxFloatDays = floatDays
                bestSmartSwipe = SmartSwipeRecommendation(
                    account = card,
                    floatDaysRemaining = floatDays,
                    statementCloseDate = cycle.activeCycleEnd,
                    nextDueDate = cycle.activeCycleNextDueDate
                )
            }

            // Timeline item
            if (cardPayable.isPositive) {
                val daysRem = ChronoUnit.DAYS.between(today, cycle.lastStatementDueDate)
                timelineItems.add(
                    CreditCardTimelineItem(
                        account = card,
                        isBilled = true,
                        amountDue = cardPayable,
                        daysRemaining = daysRem,
                        targetDate = cycle.lastStatementDueDate,
                        isUrgent = daysRem in 0..3,
                        isOverdue = daysRem < 0,
                        floatDays = floatDays
                    )
                )
            } else {
                val daysRem = ChronoUnit.DAYS.between(today, cycle.activeCycleEnd)
                timelineItems.add(
                    CreditCardTimelineItem(
                        account = card,
                        isBilled = false,
                        amountDue = cardUnbilled,
                        daysRemaining = daysRem,
                        targetDate = cycle.activeCycleEnd,
                        isUrgent = false,
                        isOverdue = false,
                        floatDays = floatDays
                    )
                )
            }

            // Milestone calculation (supports recurring 1-year auto-renewal)
            val milestoneConfig = milestoneMap[card.id]
            val milestoneInfo = if (milestoneConfig != null) {
                val (cycleStart, _) = CardMilestoneCalculator.computeCurrentAnniversaryWindow(
                    startDateEpochDay = milestoneConfig.cycleStartDateEpochDay,
                    referenceDateEpochDay = today.toEpochDay(),
                    isAutoRenewing = milestoneConfig.isAutoRenewing
                )
                val cycleStartEpoch = cycleStart.toEpochDay()
                val todayEpoch = today.toEpochDay()

                var qualifyingMinor = 0L
                for (tx in allTxs) {
                    if (tx.accountId == card.id && tx.type == TransactionType.EXPENSE && tx.ledgerDateEpochDay in cycleStartEpoch..todayEpoch) {
                        qualifyingMinor += Math.abs(tx.amount.amountMinor)
                    }
                }
                CardMilestoneCalculator.evaluateMilestone(
                    config = milestoneConfig,
                    qualifyingSpendMinor = qualifyingMinor,
                    referenceDate = today
                )
            } else null

            // Card YTD Spend & Top Category
            var ytdMinor = 0L
            val catSpendMap = mutableMapOf<String, Long>()
            for (tx in allTxs) {
                if (tx.accountId == card.id && tx.type == TransactionType.EXPENSE && tx.ledgerDateEpochDay >= currentYearStartEpoch) {
                    val amt = Math.abs(tx.amount.amountMinor)
                    ytdMinor += amt
                    val catName = tx.categoryName ?: "General"
                    catSpendMap[catName] = (catSpendMap[catName] ?: 0L) + amt
                }
            }
            val ytdSpend = MinorMoney(ytdMinor)
            val topCatEntry = catSpendMap.maxByOrNull { it.value }
            val topCategoryName = topCatEntry?.key
            val topCategoryPercent = if (ytdMinor > 0L && topCatEntry != null) {
                (topCatEntry.value.toFloat() / ytdMinor.toFloat() * 100f).coerceIn(0f, 100f)
            } else {
                0f
            }

            val isBilled = cardPayable.isPositive
            val targetDueDateOrCut = if (isBilled) cycle.lastStatementDueDate else cycle.activeCycleEnd
            val daysRem = ChronoUnit.DAYS.between(today, targetDueDateOrCut)
            val isOverdue = isBilled && daysRem < 0
            val isUrgent = isBilled && daysRem in 0..3

            val analyticsItem = CreditCardAnalyticsItem(
                account = card,
                currentDebt = cardDebt,
                creditLimit = cardLimit,
                availableCredit = cardAvailable,
                utilizationPercent = cardUtil,
                status = cardStatus,
                billedAmount = cardPayable,
                unbilledAmount = cardUnbilled,
                ytdSpend = ytdSpend,
                topCategoryName = topCategoryName,
                topCategoryPercent = topCategoryPercent,
                floatDaysRemaining = floatDays,
                isBilled = isBilled,
                daysRemaining = daysRem,
                targetDate = targetDueDateOrCut,
                isUrgent = isUrgent,
                isOverdue = isOverdue,
                milestoneInfo = milestoneInfo
            )
            cardAnalyticsItems.add(analyticsItem)

            if (aggregateUtilization <= 30f && cardUtil > 30f && (singleCardExceeding30 == null || cardUtil > singleCardExceeding30.utilizationPercent)) {
                singleCardExceeding30 = analyticsItem
            }
        }

        // Fallback for smart swipe if all cards were maxed out
        if (bestSmartSwipe == null && cardAnalyticsItems.isNotEmpty()) {
            val bestItem = cardAnalyticsItems.maxByOrNull { it.floatDaysRemaining }
            if (bestItem != null) {
                val stmtDay = (bestItem.account.settlementDayOfMonth ?: 1).coerceIn(1, 31)
                val dueDay = (bestItem.account.paymentDueDayOfMonth ?: 20).coerceIn(1, 31)
                val cycle = CreditCardCycleCalculator.computeCycleWindow(referenceDateEpochDay, stmtDay, dueDay)
                bestSmartSwipe = SmartSwipeRecommendation(
                    account = bestItem.account,
                    floatDaysRemaining = bestItem.floatDaysRemaining,
                    statementCloseDate = cycle.activeCycleEnd,
                    nextDueDate = cycle.activeCycleNextDueDate
                )
            }
        }

        // Sort timeline: billed first (sorted by daysRemaining ascending), then unbilled (sorted by daysRemaining ascending)
        val sortedTimeline = timelineItems.sortedWith(
            compareBy<CreditCardTimelineItem> { !it.isBilled }
                .thenBy { it.daysRemaining }
        )

        // 4. Spend Mode Ratio (Pure EXPENSE only, excluding transfers and cc settlements)
        var creditSpendMinor = 0L
        var liquidSpendMinor = 0L
        for (tx in periodTransactions) {
            if (tx.type == TransactionType.EXPENSE) {
                val amt = Math.abs(tx.amount.amountMinor)
                if (tx.accountId in creditCardIds) {
                    creditSpendMinor += amt
                } else {
                    liquidSpendMinor += amt
                }
            }
        }
        val totalExpenseMinor = creditSpendMinor + liquidSpendMinor
        val creditPct = if (totalExpenseMinor > 0L) (creditSpendMinor.toFloat() / totalExpenseMinor.toFloat() * 100f) else 0f
        val liquidPct = if (totalExpenseMinor > 0L) (liquidSpendMinor.toFloat() / totalExpenseMinor.toFloat() * 100f) else 0f
        val spendRatio = CreditSpendRatio(
            creditSpend = MinorMoney(creditSpendMinor),
            liquidSpend = MinorMoney(liquidSpendMinor),
            creditPercentage = creditPct,
            liquidPercentage = liquidPct
        )

        // 5. 6-Month Historical Credit Spend Trend
        val creditTrendBars = trendBars.map { bar ->
            var barSpendMinor = 0L
            for (tx in allTxs) {
                if (tx.ledgerDateEpochDay in bar.startEpochDay..bar.endEpochDay &&
                    tx.type == TransactionType.EXPENSE &&
                    tx.accountId in creditCardIds
                ) {
                    barSpendMinor += Math.abs(tx.amount.amountMinor)
                }
            }
            MonthlyTrendBar(
                periodLabel = bar.periodLabel,
                startEpochDay = bar.startEpochDay,
                endEpochDay = bar.endEpochDay,
                totalIncome = MinorMoney.ZERO,
                totalExpense = MinorMoney(barSpendMinor),
                netSavings = MinorMoney(-barSpendMinor)
            )
        }

        return CreditCardCockpitUiState(
            totalCreditLimit = totalLimit,
            totalCcDebt = totalCcDebt,
            totalAvailableCredit = totalAvailable,
            aggregateUtilizationPercent = aggregateUtilization,
            aggregateStatus = aggregateStatus,
            singleCardExceeding30 = singleCardExceeding30,
            liquidCoverage = liquidCoverage,
            smartSwipeRecommendation = bestSmartSwipe,
            timelineItems = sortedTimeline,
            cardAnalyticsItems = cardAnalyticsItems,
            spendRatio = spendRatio,
            sixMonthCreditTrendBars = creditTrendBars,
            activeCards = creditCards
        )
    }
}

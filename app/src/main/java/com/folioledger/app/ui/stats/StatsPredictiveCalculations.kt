package com.folioledger.app.ui.stats

import com.folioledger.app.domain.model.AccountShortfallAlert
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.EmergencyRunwayState
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.NetWorthGrowthTrendItem
import com.folioledger.app.domain.model.NetWorthVelocityState
import com.folioledger.app.domain.model.PayeeDrillDownState
import com.folioledger.app.domain.model.RecurringCommitmentItem
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.SafeToSpendState
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.TagProjectDrillDownState
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.util.MerchantAndTagAnalyticsCalculator
import com.folioledger.app.domain.util.NetWorthPredictiveCalculator
import com.folioledger.app.domain.util.SafeToSpendCalculator

object StatsPredictiveCalculations {

    fun computeNetWorthVelocity(
        netWorthTrend: List<NetWorthGrowthTrendItem>
    ): NetWorthVelocityState = NetWorthPredictiveCalculator.computeNetWorthVelocity(netWorthTrend)

    fun computeEmergencyRunway(
        allAccounts: List<AccountSummary>,
        trendBars: List<MonthlyTrendBar>,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency>,
        includesInvestments: Boolean
    ): EmergencyRunwayState = NetWorthPredictiveCalculator.computeEmergencyRunway(
        allAccounts = allAccounts,
        trendBars = trendBars,
        primaryCurrencyCode = primaryCurrencyCode,
        secondaryCurrencies = secondaryCurrencies,
        includesInvestments = includesInvestments
    )

    fun computePayeeDrillDownState(
        payeeName: String,
        allTxs: List<TransactionItem>,
        trendBars: List<MonthlyTrendBar>,
        startEpochDay: Long,
        endEpochDay: Long,
        isAllTime: Boolean,
        primaryCurrencyCode: String
    ): PayeeDrillDownState = MerchantAndTagAnalyticsCalculator.computePayeeDrillDownState(
        payeeName = payeeName,
        allTxs = allTxs,
        trendBars = trendBars,
        startEpochDay = startEpochDay,
        endEpochDay = endEpochDay,
        isAllTime = isAllTime,
        primaryCurrencyCode = primaryCurrencyCode
    )

    fun computeTagProjectDrillDownState(
        tagName: String,
        allTxs: List<TransactionItem>,
        trendBars: List<MonthlyTrendBar>,
        startEpochDay: Long,
        endEpochDay: Long,
        isAllTime: Boolean,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): TagProjectDrillDownState = MerchantAndTagAnalyticsCalculator.computeTagProjectDrillDownState(
        tagName = tagName,
        allTxs = allTxs,
        trendBars = trendBars,
        startEpochDay = startEpochDay,
        endEpochDay = endEpochDay,
        isAllTime = isAllTime,
        primaryCurrencyCode = primaryCurrencyCode,
        secondaryCurrencies = secondaryCurrencies
    )

    fun projectRecurringOccurrencesInHorizon(
        startDay: Long,
        frequency: RepeatFrequency,
        todayEpochDay: Long,
        horizonEpochDay: Long
    ): List<Long> = SafeToSpendCalculator.projectRecurringOccurrencesInHorizon(
        startDay = startDay,
        frequency = frequency,
        todayEpochDay = todayEpochDay,
        horizonEpochDay = horizonEpochDay
    )

    fun computeSafeToSpend(
        allAccounts: List<AccountSummary>,
        recurringRules: List<RecurringCommitmentItem>,
        futureTransactions: List<TransactionItem>,
        todayEpochDay: Long,
        periodStartEpochDay: Long,
        periodEndEpochDay: Long,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): SafeToSpendState = SafeToSpendCalculator.computeSafeToSpend(
        allAccounts = allAccounts,
        recurringRules = recurringRules,
        futureTransactions = futureTransactions,
        todayEpochDay = todayEpochDay,
        periodStartEpochDay = periodStartEpochDay,
        periodEndEpochDay = periodEndEpochDay,
        primaryCurrencyCode = primaryCurrencyCode,
        secondaryCurrencies = secondaryCurrencies
    )

    fun computeShortfallAlerts(
        allAccounts: List<AccountSummary>,
        recurringRules: List<RecurringCommitmentItem>,
        futureTransactions: List<TransactionItem>,
        todayEpochDay: Long,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): List<AccountShortfallAlert> = SafeToSpendCalculator.computeShortfallAlerts(
        allAccounts = allAccounts,
        recurringRules = recurringRules,
        futureTransactions = futureTransactions,
        todayEpochDay = todayEpochDay,
        primaryCurrencyCode = primaryCurrencyCode,
        secondaryCurrencies = secondaryCurrencies
    )
}

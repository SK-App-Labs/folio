// File: app/src/main/java/com/folioledger/app/domain/util/NetWorthPredictiveCalculator.kt
package com.folioledger.app.domain.util

import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.EmergencyRunwayState
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.NetWorthGrowthTrendItem
import com.folioledger.app.domain.model.NetWorthVelocityState
import com.folioledger.app.domain.model.RunwayHealthTier
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.StatsPeriodGranularity
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object NetWorthPredictiveCalculator {

    fun computeNetWorthTrend(
        allAccounts: List<AccountSummary>,
        allTransactions: List<TransactionItem>,
        referenceDateEpochDay: Long,
        periodGranularity: StatsPeriodGranularity
    ): List<NetWorthGrowthTrendItem> {
        val refLocalDate = LocalDate.ofEpochDay(referenceDateEpochDay)
        val shortMonthFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)

        val targetYearMonths: List<YearMonth> = if (periodGranularity == StatsPeriodGranularity.ANNUALLY) {
            (1..12).map { month -> YearMonth.of(refLocalDate.year, month) }
        } else {
            (5 downTo 0).map { offset -> YearMonth.from(refLocalDate.minusMonths(offset.toLong())) }
        }
        val trendItems = ArrayList<NetWorthGrowthTrendItem>(targetYearMonths.size)

        val activeAccounts = allAccounts.filter { !it.isArchived && it.includeInNetWorth && it.groupType != AccountGroup.DEBIT_CARD }

        for (targetYearMonth in targetYearMonths) {
            val endOfMonthEpochDay = targetYearMonth.atEndOfMonth().toEpochDay()

            var monthAssetsMinor = 0L
            var monthLiabilitiesMinor = 0L

            for (acc in activeAccounts) {
                var currentMinor = acc.currentBalance.amountMinor

                val futureTxs = allTransactions.filter { tx ->
                    tx.ledgerDateEpochDay > endOfMonthEpochDay &&
                            (tx.accountId == acc.id || tx.transferToAccountId == acc.id)
                }

                for (tx in futureTxs) {
                    val amount = Math.abs(tx.amount.amountMinor)
                    val fee = Math.abs(tx.transferFee.amountMinor)

                    if (tx.accountId == acc.id) {
                        when (tx.type) {
                            TransactionType.INCOME, TransactionType.REFUND -> currentMinor -= amount
                            TransactionType.EXPENSE -> currentMinor += (amount + fee)
                            TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> currentMinor += (amount + fee)
                            TransactionType.OPENING_BALANCE,
                            TransactionType.RECONCILIATION -> {
                                val isOutflow = tx.transferToAccountId != null
                                if (isOutflow) {
                                    currentMinor += amount
                                } else {
                                    currentMinor -= amount
                                }
                            }
                        }
                    } else if (tx.transferToAccountId == acc.id) {
                        currentMinor -= amount
                    }
                }

                if (currentMinor >= 0L) {
                    monthAssetsMinor += currentMinor
                } else {
                    monthLiabilitiesMinor += Math.abs(currentMinor)
                }
            }

            val assets = MinorMoney(monthAssetsMinor)
            val liabilities = MinorMoney(monthLiabilitiesMinor)
            val netWorth = MinorMoney(monthAssetsMinor - monthLiabilitiesMinor)

            trendItems.add(
                NetWorthGrowthTrendItem(
                    epochDay = endOfMonthEpochDay,
                    monthYearText = targetYearMonth.format(shortMonthFormatter),
                    assets = assets,
                    liabilities = liabilities,
                    netWorth = netWorth
                )
            )
        }

        return trendItems
    }

    fun computeNetWorthVelocity(
        netWorthTrend: List<NetWorthGrowthTrendItem>
    ): NetWorthVelocityState {
        if (netWorthTrend.isEmpty()) {
            return NetWorthVelocityState()
        }
        val currentItem = netWorthTrend.last()
        val currentNW = currentItem.netWorth
        val previousItem = if (netWorthTrend.size >= 2) netWorthTrend[netWorthTrend.size - 2] else currentItem
        val previousNW = previousItem.netWorth
        val absChange = currentNW - previousNW
        val isPositive = absChange.amountMinor >= 0L
        val pct = if (previousNW.amountMinor != 0L) {
            (absChange.amountMinor.toFloat() / Math.abs(previousNW.amountMinor).toFloat()) * 100f
        } else {
            if (absChange.amountMinor > 0L) 100f else if (absChange.amountMinor < 0L) -100f else 0f
        }
        return NetWorthVelocityState(
            currentNetWorth = currentNW,
            previousMonthNetWorth = previousNW,
            absoluteChange = absChange,
            percentageChange = pct,
            isPositive = isPositive
        )
    }

    fun computeEmergencyRunway(
        allAccounts: List<AccountSummary>,
        trendBars: List<MonthlyTrendBar>,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency>,
        includesInvestments: Boolean
    ): EmergencyRunwayState {
        val primarySpec = SupportedCurrencies.fromCode(primaryCurrencyCode)
        val secondaryMap = secondaryCurrencies.associateBy { it.code.trim().uppercase() }

        var totalLiquidMinor = 0L
        var totalInvestmentsMinor = 0L

        for (acc in allAccounts) {
            if (acc.isArchived || acc.currentBalance.amountMinor <= 0L) continue

            val isLiquid = acc.groupType == AccountGroup.BANK_ACCOUNT ||
                    acc.groupType == AccountGroup.CASH ||
                    acc.groupType == AccountGroup.SAVINGS ||
                    acc.groupType == AccountGroup.PREPAID_WALLET
            val isInvestment = acc.groupType == AccountGroup.INVESTMENT

            if (!isLiquid && !isInvestment) continue

            val rawBalanceMinor = acc.currentBalance.amountMinor
            val accountCurrency = acc.currencyCode.trim().uppercase()
            val convertedBalanceMinor = if (accountCurrency == primarySpec.code.uppercase()) {
                rawBalanceMinor
            } else {
                val secondary = secondaryMap[accountCurrency]
                if (secondary != null && secondary.exchangeRateToPrimary > 0.0) {
                    val accSpec = SupportedCurrencies.fromCode(accountCurrency)
                    MoneyFormatterAndCalculator.convertMinorMoney(
                        sourceMinor = rawBalanceMinor,
                        sourceFractionDigits = accSpec.fractionDigits,
                        targetFractionDigits = primarySpec.fractionDigits,
                        rateToTarget = secondary.exchangeRateToPrimary
                    )
                } else {
                    rawBalanceMinor
                }
            }

            if (isLiquid) {
                totalLiquidMinor = Math.addExact(totalLiquidMinor, convertedBalanceMinor)
            } else if (isInvestment) {
                totalInvestmentsMinor = Math.addExact(totalInvestmentsMinor, convertedBalanceMinor)
            }
        }

        val positiveExpenseMonths = trendBars.map { it.totalExpense.amountMinor }.filter { it > 0L }
        val averageBurnMinor = if (positiveExpenseMonths.isNotEmpty()) {
            positiveExpenseMonths.average().toLong()
        } else if (trendBars.isNotEmpty()) {
            trendBars.map { it.totalExpense.amountMinor }.average().toLong()
        } else {
            0L
        }

        val applicableFundsMinor = if (includesInvestments) {
            Math.addExact(totalLiquidMinor, totalInvestmentsMinor)
        } else {
            totalLiquidMinor
        }

        val runwayMonths = if (averageBurnMinor > 0L) {
            (applicableFundsMinor.toFloat() / averageBurnMinor.toFloat()).coerceIn(0f, 99.9f)
        } else {
            if (applicableFundsMinor > 0L) 99.9f else 0f
        }

        val healthTier = when {
            runwayMonths >= 6.0f -> RunwayHealthTier.RESILIENT
            runwayMonths >= 3.0f -> RunwayHealthTier.ADEQUATE
            else -> RunwayHealthTier.VULNERABLE
        }

        return EmergencyRunwayState(
            totalLiquidFunds = MinorMoney(totalLiquidMinor),
            totalInvestmentFunds = MinorMoney(totalInvestmentsMinor),
            averageMonthlyBurnMinor = averageBurnMinor,
            runwayMonths = runwayMonths,
            healthTier = healthTier,
            targetMonths = 6.0f,
            includesInvestments = includesInvestments
        )
    }
}

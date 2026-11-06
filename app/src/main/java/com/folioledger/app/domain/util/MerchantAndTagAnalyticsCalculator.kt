// File: app/src/main/java/com/folioledger/app/domain/util/MerchantAndTagAnalyticsCalculator.kt
package com.folioledger.app.domain.util

import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.PayeeDrillDownState
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TagProjectDrillDownState
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType

object MerchantAndTagAnalyticsCalculator {

    fun computePayeeDrillDownState(
        payeeName: String,
        allTxs: List<TransactionItem>,
        trendBars: List<MonthlyTrendBar>,
        startEpochDay: Long,
        endEpochDay: Long,
        isAllTime: Boolean,
        primaryCurrencyCode: String
    ): PayeeDrillDownState {
        val trimmedTarget = payeeName.trim().lowercase()

        val allMatchingTxs = allTxs.filter { tx ->
            val raw = tx.payeeOrDescription.trim().ifEmpty { tx.note.trim() }.lowercase()
            raw == trimmedTarget && (tx.type == TransactionType.EXPENSE || tx.type == TransactionType.REFUND)
        }

        val periodMatchingTxs = if (isAllTime) {
            allMatchingTxs
        } else {
            allMatchingTxs.filter { it.ledgerDateEpochDay in startEpochDay..endEpochDay }
        }

        var totalPeriodMinor = 0L
        var periodVisitCount = 0
        for (tx in periodMatchingTxs) {
            if (tx.type == TransactionType.EXPENSE) {
                totalPeriodMinor += Math.abs(tx.amount.amountMinor) + Math.abs(tx.transferFee.amountMinor)
                periodVisitCount++
            } else if (tx.type == TransactionType.REFUND) {
                totalPeriodMinor -= Math.abs(tx.amount.amountMinor)
            }
        }
        totalPeriodMinor = Math.max(0L, totalPeriodMinor)

        var totalAllTimeMinor = 0L
        var allTimeVisitCount = 0
        for (tx in allMatchingTxs) {
            if (tx.type == TransactionType.EXPENSE) {
                totalAllTimeMinor += Math.abs(tx.amount.amountMinor) + Math.abs(tx.transferFee.amountMinor)
                allTimeVisitCount++
            } else if (tx.type == TransactionType.REFUND) {
                totalAllTimeMinor -= Math.abs(tx.amount.amountMinor)
            }
        }
        totalAllTimeMinor = Math.max(0L, totalAllTimeMinor)

        val averageTicketMinor = if (periodVisitCount > 0) {
            totalPeriodMinor / periodVisitCount
        } else if (allTimeVisitCount > 0) {
            totalAllTimeMinor / allTimeVisitCount
        } else {
            0L
        }

        // Category distribution for this merchant
        val catAmountMap = mutableMapOf<String, Long>()
        val catCountMap = mutableMapOf<String, Int>()
        val catColorMap = mutableMapOf<String, String>()
        val catIconMap = mutableMapOf<String, String>()

        val activeForCategories = if (periodMatchingTxs.isNotEmpty()) periodMatchingTxs else allMatchingTxs
        for (tx in activeForCategories) {
            val catName = tx.categoryName ?: "General"
            val amt = Math.abs(tx.amount.amountMinor)
            catAmountMap[catName] = (catAmountMap[catName] ?: 0L) + amt
            catCountMap[catName] = (catCountMap[catName] ?: 0) + 1
            if (tx.categoryColorHex != null) catColorMap[catName] = tx.categoryColorHex
            if (tx.categoryIconName != null) catIconMap[catName] = tx.categoryIconName
        }

        val grandCategoryMinor = catAmountMap.values.sum().coerceAtLeast(1L)
        val catDistribution = catAmountMap.map { (catName, minorSum) ->
            CategorySpendStat(
                categoryId = 0L,
                categoryName = catName,
                colorHex = catColorMap[catName] ?: "#8B5CF6",
                iconName = catIconMap[catName] ?: "category",
                netAmount = MinorMoney(minorSum),
                percentageOfTotal = (minorSum.toFloat() / grandCategoryMinor.toFloat() * 100f).coerceIn(0f, 100f),
                transactionCount = catCountMap[catName] ?: 1
            )
        }.sortedByDescending { it.netAmount }

        // 6-month monthly spend bars for this merchant
        val payeeMonthlyBars = trendBars.map { bar ->
            var barExpenseMinor = 0L
            for (tx in allMatchingTxs) {
                if (tx.ledgerDateEpochDay in bar.startEpochDay..bar.endEpochDay) {
                    if (tx.type == TransactionType.EXPENSE) {
                        barExpenseMinor += Math.abs(tx.amount.amountMinor) + Math.abs(tx.transferFee.amountMinor)
                    } else if (tx.type == TransactionType.REFUND) {
                        barExpenseMinor -= Math.abs(tx.amount.amountMinor)
                    }
                }
            }
            val safeMinor = Math.max(0L, barExpenseMinor)
            MonthlyTrendBar(
                periodLabel = bar.periodLabel,
                startEpochDay = bar.startEpochDay,
                endEpochDay = bar.endEpochDay,
                totalIncome = MinorMoney.ZERO,
                totalExpense = MinorMoney(safeMinor),
                netSavings = MinorMoney(-safeMinor)
            )
        }

        val sortedTransactions = activeForCategories.sortedWith(
            compareByDescending<TransactionItem> { it.ledgerDateEpochDay }
                .thenByDescending { it.timestampEpochMillis }
        )

        return PayeeDrillDownState(
            payeeName = payeeName,
            totalPeriodSpend = MinorMoney(totalPeriodMinor),
            totalAllTimeSpend = MinorMoney(totalAllTimeMinor),
            periodVisitCount = periodVisitCount,
            allTimeVisitCount = allTimeVisitCount,
            averageTicketSize = MinorMoney(averageTicketMinor),
            categoryDistribution = catDistribution,
            monthlyTrendBars = payeeMonthlyBars,
            transactions = sortedTransactions
        )
    }

    fun computeTagProjectDrillDownState(
        tagName: String,
        allTxs: List<TransactionItem>,
        trendBars: List<MonthlyTrendBar>,
        startEpochDay: Long,
        endEpochDay: Long,
        isAllTime: Boolean,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): TagProjectDrillDownState {
        val cleanTargetTag = tagName.removePrefix("#").trim().lowercase()

        val allMatchingTxs = allTxs.filter { tx ->
            tx.tags.any { it.removePrefix("#").trim().lowercase() == cleanTargetTag }
        }

        val periodMatchingTxs = if (isAllTime) {
            allMatchingTxs
        } else {
            val inPeriod = allMatchingTxs.filter { it.ledgerDateEpochDay in startEpochDay..endEpochDay }
            if (inPeriod.isNotEmpty()) inPeriod else allMatchingTxs
        }

        val primarySpec = SupportedCurrencies.fromCode(primaryCurrencyCode)
        val secondaryMap = secondaryCurrencies.associateBy { it.code.trim().uppercase() }

        fun convertToPrimary(rawAmountMinor: Long, currencyCode: String): Long {
            val accCurr = currencyCode.trim().uppercase()
            if (accCurr == primarySpec.code.uppercase()) return rawAmountMinor
            val secondary = secondaryMap[accCurr]
            return if (secondary != null && secondary.exchangeRateToPrimary > 0.0) {
                val accSpec = SupportedCurrencies.fromCode(accCurr)
                MoneyFormatterAndCalculator.convertMinorMoney(
                    sourceMinor = rawAmountMinor,
                    sourceFractionDigits = accSpec.fractionDigits,
                    targetFractionDigits = primarySpec.fractionDigits,
                    rateToTarget = secondary.exchangeRateToPrimary
                )
            } else {
                rawAmountMinor
            }
        }

        val isReimbursableKeyword = cleanTargetTag.contains("reimburs") ||
                cleanTargetTag.contains("claim") ||
                cleanTargetTag.contains("split") ||
                cleanTargetTag.contains("office") ||
                cleanTargetTag.contains("work") ||
                cleanTargetTag.contains("bill") ||
                cleanTargetTag.contains("owed") ||
                cleanTargetTag.contains("due")

        val hasInflowTransactions = allMatchingTxs.any {
            it.type == TransactionType.REFUND || it.type == TransactionType.INCOME
        }

        val isReimbursableProject = isReimbursableKeyword || hasInflowTransactions

        var totalSpentMinor = 0L
        var totalReimbursedMinor = 0L
        var minEpochDay: Long? = null
        var maxEpochDay: Long? = null

        val activeTxs = if (periodMatchingTxs.isNotEmpty()) periodMatchingTxs else allMatchingTxs

        for (tx in activeTxs) {
            val convertedAmount = convertToPrimary(Math.abs(tx.amount.amountMinor), tx.currencyCode)
            val convertedFee = convertToPrimary(Math.abs(tx.transferFee.amountMinor), tx.currencyCode)

            when (tx.type) {
                TransactionType.EXPENSE -> {
                    totalSpentMinor += convertedAmount + convertedFee
                    val d = tx.ledgerDateEpochDay
                    minEpochDay = if (minEpochDay == null) d else minOf(minEpochDay, d)
                    maxEpochDay = if (maxEpochDay == null) d else maxOf(maxEpochDay, d)
                }
                TransactionType.REFUND, TransactionType.INCOME -> {
                    totalReimbursedMinor += convertedAmount
                    val d = tx.ledgerDateEpochDay
                    minEpochDay = if (minEpochDay == null) d else minOf(minEpochDay, d)
                    maxEpochDay = if (maxEpochDay == null) d else maxOf(maxEpochDay, d)
                }
                TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> {
                    if (convertedFee > 0L) {
                        totalSpentMinor += convertedFee
                    }
                }
                TransactionType.OPENING_BALANCE, TransactionType.RECONCILIATION -> {}
            }
        }

        val netPendingOwedMinor = Math.max(0L, totalSpentMinor - totalReimbursedMinor)
        val isFullySettled = totalSpentMinor > 0L && netPendingOwedMinor == 0L

        val dateSpanDays = if (minEpochDay != null && maxEpochDay != null) {
            (maxEpochDay - minEpochDay + 1).toInt().coerceAtLeast(1)
        } else {
            1
        }

        val dailyAverageSpendMinor = if (dateSpanDays > 0) {
            totalSpentMinor / dateSpanDays
        } else {
            totalSpentMinor
        }

        // Category distribution for this project
        val catAmountMap = mutableMapOf<String, Long>()
        val catCountMap = mutableMapOf<String, Int>()
        val catColorMap = mutableMapOf<String, String>()
        val catIconMap = mutableMapOf<String, String>()

        for (tx in activeTxs) {
            if (tx.type == TransactionType.EXPENSE) {
                val catName = tx.categoryName ?: "General"
                val amt = convertToPrimary(Math.abs(tx.amount.amountMinor), tx.currencyCode)
                catAmountMap[catName] = (catAmountMap[catName] ?: 0L) + amt
                catCountMap[catName] = (catCountMap[catName] ?: 0) + 1
                if (tx.categoryColorHex != null) catColorMap[catName] = tx.categoryColorHex
                if (tx.categoryIconName != null) catIconMap[catName] = tx.categoryIconName
            }
        }

        val grandCategoryMinor = catAmountMap.values.sum().coerceAtLeast(1L)
        val catDistribution = catAmountMap.map { (catName, minorSum) ->
            CategorySpendStat(
                categoryId = 0L,
                categoryName = catName,
                colorHex = catColorMap[catName] ?: "#8B5CF6",
                iconName = catIconMap[catName] ?: "category",
                netAmount = MinorMoney(minorSum),
                percentageOfTotal = (minorSum.toFloat() / grandCategoryMinor.toFloat() * 100f).coerceIn(0f, 100f),
                transactionCount = catCountMap[catName] ?: 1
            )
        }.sortedByDescending { it.netAmount }

        // 6-month monthly spend bars for this tag
        val tagMonthlyBars = trendBars.map { bar ->
            var barExpenseMinor = 0L
            for (tx in allMatchingTxs) {
                if (tx.ledgerDateEpochDay in bar.startEpochDay..bar.endEpochDay) {
                    val amt = convertToPrimary(Math.abs(tx.amount.amountMinor), tx.currencyCode)
                    if (tx.type == TransactionType.EXPENSE) {
                        val fee = convertToPrimary(Math.abs(tx.transferFee.amountMinor), tx.currencyCode)
                        barExpenseMinor += amt + fee
                    } else if (tx.type == TransactionType.REFUND) {
                        barExpenseMinor -= amt
                    }
                }
            }
            val safeMinor = Math.max(0L, barExpenseMinor)
            MonthlyTrendBar(
                periodLabel = bar.periodLabel,
                startEpochDay = bar.startEpochDay,
                endEpochDay = bar.endEpochDay,
                totalIncome = MinorMoney.ZERO,
                totalExpense = MinorMoney(safeMinor),
                netSavings = MinorMoney(-safeMinor)
            )
        }

        val sortedTransactions = activeTxs.sortedWith(
            compareByDescending<TransactionItem> { it.ledgerDateEpochDay }
                .thenByDescending { it.timestampEpochMillis }
        )

        return TagProjectDrillDownState(
            tagName = tagName,
            isReimbursableProject = isReimbursableProject,
            totalSpent = MinorMoney(totalSpentMinor),
            totalReimbursed = MinorMoney(totalReimbursedMinor),
            netPendingOwed = MinorMoney(netPendingOwedMinor),
            isFullySettled = isFullySettled,
            dateSpanDays = dateSpanDays,
            minEpochDay = minEpochDay,
            maxEpochDay = maxEpochDay,
            dailyAverageSpend = MinorMoney(dailyAverageSpendMinor),
            categoryDistribution = catDistribution,
            monthlyTrendBars = tagMonthlyBars,
            transactions = sortedTransactions
        )
    }
}

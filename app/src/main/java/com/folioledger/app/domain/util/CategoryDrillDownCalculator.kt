package com.folioledger.app.domain.util

import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import kotlin.math.abs

object CategoryDrillDownCalculator {

    fun attachComparativeDeltas(
        currentList: List<CategorySpendStat>,
        priorList: List<CategorySpendStat>
    ): List<CategorySpendStat> {
        val priorMap = priorList.associateBy { it.categoryId }
        return currentList.map { current ->
            val prior = priorMap[current.categoryId]
            val priorAmount = prior?.netAmount
            val changePct = if (priorAmount != null && priorAmount.amountMinor > 0L) {
                ((current.netAmount.amountMinor - priorAmount.amountMinor).toFloat() / priorAmount.amountMinor.toFloat()) * 100f
            } else if (priorAmount != null && priorAmount.amountMinor == 0L && current.netAmount.amountMinor > 0L) {
                100f
            } else {
                null
            }
            current.copy(
                priorPeriodAmount = priorAmount,
                changePercentage = changePct
            )
        }
    }

    fun collectCategoryAndSubcategoryIds(
        tree: List<CategoryNode>,
        targetCategoryId: Long
    ): Set<Long> {
        for ((id, _, _, _, _, _, subcategories) in tree) {
            if (id == targetCategoryId) {
                return buildSet {
                    add(id)
                    subcategories.forEach { add(it.id) }
                }
            }
            if (subcategories.any { it.id == targetCategoryId }) {
                return setOf(targetCategoryId)
            }
        }
        return setOf(targetCategoryId)
    }

    fun findCategoryNode(tree: List<CategoryNode>, categoryId: Long?): CategoryNode? {
        if (categoryId == null) return null
        for (parent in tree) {
            if (parent.id == categoryId) return parent
            val matchingSub = parent.subcategories.firstOrNull { it.id == categoryId }
            if (matchingSub != null) return matchingSub
        }
        return null
    }

    /**
     * Filters a category tree by search query string:
     * - Returns full tree untouched when query is blank.
     * - Retains full parent and all subcategories if parent name matches.
     * - Retains parent with matching subcategories if only subcategories match.
     * - Filters out nodes where neither parent nor any subcategory matches.
     */
    fun filterCategoryTree(
        categories: List<CategoryNode>,
        searchQuery: String
    ): List<CategoryNode> {
        val trimmed = searchQuery.trim()
        if (trimmed.isEmpty()) return categories

        return categories.mapNotNull { parent ->
            val parentMatches = parent.name.contains(trimmed, ignoreCase = true)
            val matchingSubs = parent.subcategories.filter { sub ->
                sub.name.contains(trimmed, ignoreCase = true)
            }
            if (parentMatches || matchingSubs.isNotEmpty()) {
                parent.copy(
                    subcategories = if (parentMatches) parent.subcategories else matchingSubs
                )
            } else null
        }
    }

    fun filterMatchingTransactions(
        transactions: List<TransactionItem>,
        categoryType: CategoryType,
        categoryName: String,
        matchingCategoryIds: Set<Long>
    ): List<TransactionItem> {
        return transactions.filter { tx ->
            val typeMatches = when (categoryType) {
                CategoryType.EXPENSE -> tx.type == TransactionType.EXPENSE || tx.type == TransactionType.REFUND
                CategoryType.INCOME -> tx.type == TransactionType.INCOME
            }
            if (!typeMatches) return@filter false

            val directMatch = tx.categoryId != null && tx.categoryId in matchingCategoryIds
            val splitMatch = tx.isSplit && tx.splits.any { it.categoryId in matchingCategoryIds }
            val nameMatch = tx.categoryName != null && tx.categoryName.equals(categoryName, ignoreCase = true)
            val parentNameMatch = tx.parentCategoryName != null && tx.parentCategoryName.equals(categoryName, ignoreCase = true)

            directMatch || splitMatch || nameMatch || parentNameMatch
        }.sortedWith(
            compareByDescending<TransactionItem> { it.ledgerDateEpochDay }
                .thenByDescending { it.timestampEpochMillis }
        )
    }

    fun computeCategoryHistoricalTrend(
        trendBars: List<MonthlyTrendBar>,
        allTransactions: List<TransactionItem>,
        categoryType: CategoryType,
        categoryName: String,
        matchingCategoryIds: Set<Long>
    ): List<MonthlyTrendBar> {
        return trendBars.map { bar ->
            var cycleExpenseMinor = 0L
            var cycleIncomeMinor = 0L

            for (tx in allTransactions) {
                if (tx.ledgerDateEpochDay !in bar.startEpochDay..bar.endEpochDay) continue

                val directMatch = tx.categoryId != null && tx.categoryId in matchingCategoryIds
                val splitMatch = tx.isSplit && tx.splits.any { it.categoryId in matchingCategoryIds }
                val nameMatch = tx.categoryName != null && tx.categoryName.equals(categoryName, ignoreCase = true)
                val parentNameMatch = tx.parentCategoryName != null && tx.parentCategoryName.equals(categoryName, ignoreCase = true)

                if (!directMatch && !splitMatch && !nameMatch && !parentNameMatch) continue

                val rawAmount = abs(tx.amount.amountMinor)
                if (categoryType == CategoryType.EXPENSE) {
                    when (tx.type) {
                        TransactionType.EXPENSE -> cycleExpenseMinor += rawAmount
                        TransactionType.REFUND -> cycleExpenseMinor -= rawAmount
                        else -> {}
                    }
                } else {
                    if (tx.type == TransactionType.INCOME) {
                        cycleIncomeMinor += rawAmount
                    }
                }
            }

            MonthlyTrendBar(
                periodLabel = bar.periodLabel,
                startEpochDay = bar.startEpochDay,
                endEpochDay = bar.endEpochDay,
                totalIncome = MinorMoney(cycleIncomeMinor.coerceAtLeast(0L)),
                totalExpense = MinorMoney(cycleExpenseMinor.coerceAtLeast(0L)),
                netSavings = MinorMoney(cycleIncomeMinor - cycleExpenseMinor)
            )
        }
    }
}

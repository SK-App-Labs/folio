package com.folioledger.app.domain.util

import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CategoryDrillDownCalculatorTest {

    @Test
    fun `attachComparativeDeltas calculates correct percentage changes`() {
        val current = listOf(
            CategorySpendStat(categoryId = 1L, categoryName = "Groceries", colorHex = "#000", iconName = "cart", netAmount = MinorMoney(15000L), percentageOfTotal = 50f, transactionCount = 5),
            CategorySpendStat(categoryId = 2L, categoryName = "Dining", colorHex = "#000", iconName = "food", netAmount = MinorMoney(8000L), percentageOfTotal = 30f, transactionCount = 3),
            CategorySpendStat(categoryId = 3L, categoryName = "New Sub", colorHex = "#000", iconName = "sub", netAmount = MinorMoney(2000L), percentageOfTotal = 10f, transactionCount = 1)
        )
        val prior = listOf(
            CategorySpendStat(categoryId = 1L, categoryName = "Groceries", colorHex = "#000", iconName = "cart", netAmount = MinorMoney(10000L), percentageOfTotal = 40f, transactionCount = 4),
            CategorySpendStat(categoryId = 2L, categoryName = "Dining", colorHex = "#000", iconName = "food", netAmount = MinorMoney(10000L), percentageOfTotal = 40f, transactionCount = 4),
            CategorySpendStat(categoryId = 3L, categoryName = "New Sub", colorHex = "#000", iconName = "sub", netAmount = MinorMoney.ZERO, percentageOfTotal = 0f, transactionCount = 0)
        )

        val result = CategoryDrillDownCalculator.attachComparativeDeltas(current, prior)

        // Groceries: 15000 vs 10000 -> +50%
        val groceries = result.first { it.categoryId == 1L }
        assertEquals(50f, groceries.changePercentage!!, 0.01f)
        assertEquals(MinorMoney(10000L), groceries.priorPeriodAmount)

        // Dining: 8000 vs 10000 -> -20%
        val dining = result.first { it.categoryId == 2L }
        assertEquals(-20f, dining.changePercentage!!, 0.01f)
        assertEquals(MinorMoney(10000L), dining.priorPeriodAmount)

        // New Sub: 2000 vs 0 -> 100%
        val newSub = result.first { it.categoryId == 3L }
        assertEquals(100f, newSub.changePercentage!!, 0.01f)
    }

    @Test
    fun `collectCategoryAndSubcategoryIds returns parent and all children`() {
        val sub1 = CategoryNode(id = 11L, name = "Organic", type = CategoryType.EXPENSE, parentId = 1L, iconName = "eco", colorHex = "#000")
        val sub2 = CategoryNode(id = 12L, name = "Snacks", type = CategoryType.EXPENSE, parentId = 1L, iconName = "fastfood", colorHex = "#000")
        val parent = CategoryNode(id = 1L, name = "Groceries", type = CategoryType.EXPENSE, parentId = null, iconName = "shopping_cart", colorHex = "#000", subcategories = listOf(sub1, sub2))

        val tree = listOf(parent)

        // Target parent: should return {1, 11, 12}
        val parentIds = CategoryDrillDownCalculator.collectCategoryAndSubcategoryIds(tree, targetCategoryId = 1L)
        assertEquals(setOf(1L, 11L, 12L), parentIds)

        // Target child: should return {11}
        val childIds = CategoryDrillDownCalculator.collectCategoryAndSubcategoryIds(tree, targetCategoryId = 11L)
        assertEquals(setOf(11L), childIds)

        // Unknown: fallback to {999}
        val unknownIds = CategoryDrillDownCalculator.collectCategoryAndSubcategoryIds(tree, targetCategoryId = 999L)
        assertEquals(setOf(999L), unknownIds)
    }

    @Test
    fun `findCategoryNode locates parent and child nodes`() {
        val sub1 = CategoryNode(id = 21L, name = "Sub", type = CategoryType.EXPENSE, parentId = 2L, iconName = "icon", colorHex = "#000")
        val parent = CategoryNode(id = 2L, name = "Parent", type = CategoryType.EXPENSE, parentId = null, iconName = "icon", colorHex = "#000", subcategories = listOf(sub1))
        val tree = listOf(parent)

        assertNull(CategoryDrillDownCalculator.findCategoryNode(tree, null))
        assertNull(CategoryDrillDownCalculator.findCategoryNode(tree, 999L))

        val foundParent = CategoryDrillDownCalculator.findCategoryNode(tree, 2L)
        assertNotNull(foundParent)
        assertEquals("Parent", foundParent!!.name)

        val foundChild = CategoryDrillDownCalculator.findCategoryNode(tree, 21L)
        assertNotNull(foundChild)
        assertEquals("Sub", foundChild!!.name)
    }

    @Test
    fun `filterMatchingTransactions matches category, splits, and names`() {
        val tx1 = createDummyTransaction(id = 1L, categoryId = 10L, type = TransactionType.EXPENSE, amount = MinorMoney(500L), ledgerDateEpochDay = 100L)
        val tx2 = createDummyTransaction(id = 2L, categoryId = 99L, type = TransactionType.EXPENSE, amount = MinorMoney(300L), categoryName = "Shopping", ledgerDateEpochDay = 101L)
        val tx3 = createDummyTransaction(id = 3L, categoryId = 10L, type = TransactionType.INCOME, amount = MinorMoney(1000L), ledgerDateEpochDay = 102L)

        val matchingIds = setOf(10L)

        val expenseMatches = CategoryDrillDownCalculator.filterMatchingTransactions(
            transactions = listOf(tx1, tx2, tx3),
            categoryType = CategoryType.EXPENSE,
            categoryName = "Shopping",
            matchingCategoryIds = matchingIds
        )

        // tx1 matches categoryId 10L (expense)
        // tx2 matches categoryName "Shopping" (expense)
        // tx3 is INCOME, so ignored
        assertEquals(2, expenseMatches.size)
        // Ordered descending by ledgerDateEpochDay
        assertEquals(2L, expenseMatches[0].id)
        assertEquals(1L, expenseMatches[1].id)
    }

    @Test
    fun `computeCategoryHistoricalTrend calculates period expense and refund netting`() {
        val bars = listOf(
            MonthlyTrendBar(periodLabel = "Oct", startEpochDay = 100L, endEpochDay = 130L, totalIncome = MinorMoney.ZERO, totalExpense = MinorMoney.ZERO, netSavings = MinorMoney.ZERO),
            MonthlyTrendBar(periodLabel = "Nov", startEpochDay = 131L, endEpochDay = 160L, totalIncome = MinorMoney.ZERO, totalExpense = MinorMoney.ZERO, netSavings = MinorMoney.ZERO)
        )

        val txs = listOf(
            createDummyTransaction(id = 1L, categoryId = 10L, type = TransactionType.EXPENSE, amount = MinorMoney(5000L), ledgerDateEpochDay = 110L),
            createDummyTransaction(id = 2L, categoryId = 10L, type = TransactionType.REFUND, amount = MinorMoney(1000L), ledgerDateEpochDay = 120L),
            createDummyTransaction(id = 3L, categoryId = 10L, type = TransactionType.EXPENSE, amount = MinorMoney(3000L), ledgerDateEpochDay = 140L)
        )

        val result = CategoryDrillDownCalculator.computeCategoryHistoricalTrend(
            trendBars = bars,
            allTransactions = txs,
            categoryType = CategoryType.EXPENSE,
            categoryName = "Tech",
            matchingCategoryIds = setOf(10L)
        )

        assertEquals(2, result.size)
        // Oct: 5000 expense - 1000 refund = 4000 net expense
        assertEquals(MinorMoney(4000L), result[0].totalExpense)
        // Nov: 3000 expense
        assertEquals(MinorMoney(3000L), result[1].totalExpense)
    }

    @Test
    fun `filterCategoryTree with blank query returns all categories unchanged`() {
        val sub = CategoryNode(id = 11L, name = "Organic", type = CategoryType.EXPENSE, parentId = 1L, iconName = "eco", colorHex = "#000")
        val parent = CategoryNode(id = 1L, name = "Groceries", type = CategoryType.EXPENSE, parentId = null, iconName = "cart", colorHex = "#000", subcategories = listOf(sub))
        val tree = listOf(parent)

        assertEquals(tree, CategoryDrillDownCalculator.filterCategoryTree(tree, ""))
        assertEquals(tree, CategoryDrillDownCalculator.filterCategoryTree(tree, "   "))
    }

    @Test
    fun `filterCategoryTree matching parent name includes parent and all subcategories`() {
        val sub1 = CategoryNode(id = 11L, name = "Organic", type = CategoryType.EXPENSE, parentId = 1L, iconName = "eco", colorHex = "#000")
        val sub2 = CategoryNode(id = 12L, name = "Snacks", type = CategoryType.EXPENSE, parentId = 1L, iconName = "fastfood", colorHex = "#000")
        val parent1 = CategoryNode(id = 1L, name = "Groceries", type = CategoryType.EXPENSE, parentId = null, iconName = "cart", colorHex = "#000", subcategories = listOf(sub1, sub2))
        val parent2 = CategoryNode(id = 2L, name = "Utilities", type = CategoryType.EXPENSE, parentId = null, iconName = "bolt", colorHex = "#000")
        val tree = listOf(parent1, parent2)

        val filtered = CategoryDrillDownCalculator.filterCategoryTree(tree, "grocer")
        assertEquals(1, filtered.size)
        assertEquals("Groceries", filtered[0].name)
        assertEquals(2, filtered[0].subcategories.size)
    }

    @Test
    fun `filterCategoryTree matching subcategory only includes parent with matching subcategory`() {
        val sub1 = CategoryNode(id = 11L, name = "Organic Veggies", type = CategoryType.EXPENSE, parentId = 1L, iconName = "eco", colorHex = "#000")
        val sub2 = CategoryNode(id = 12L, name = "Snacks", type = CategoryType.EXPENSE, parentId = 1L, iconName = "fastfood", colorHex = "#000")
        val parent = CategoryNode(id = 1L, name = "Groceries", type = CategoryType.EXPENSE, parentId = null, iconName = "cart", colorHex = "#000", subcategories = listOf(sub1, sub2))
        val tree = listOf(parent)

        val filtered = CategoryDrillDownCalculator.filterCategoryTree(tree, "veggie")
        assertEquals(1, filtered.size)
        assertEquals(1, filtered[0].subcategories.size)
        assertEquals("Organic Veggies", filtered[0].subcategories[0].name)
    }

    @Test
    fun `filterCategoryTree with no match returns empty list`() {
        val sub = CategoryNode(id = 11L, name = "Organic", type = CategoryType.EXPENSE, parentId = 1L, iconName = "eco", colorHex = "#000")
        val parent = CategoryNode(id = 1L, name = "Groceries", type = CategoryType.EXPENSE, parentId = null, iconName = "cart", colorHex = "#000", subcategories = listOf(sub))
        val tree = listOf(parent)

        val filtered = CategoryDrillDownCalculator.filterCategoryTree(tree, "NonExistentXYZ")
        assertEquals(0, filtered.size)
    }

    @Test
    fun `computeSubcategoryBreakdown aggregates subcategories and calculates distribution correctly`() {
        val tx1 = createDummyTransaction(id = 1L, categoryId = 10L, type = TransactionType.EXPENSE, amount = MinorMoney(6000L), categoryName = "Fruits", ledgerDateEpochDay = 100L)
        val tx2 = createDummyTransaction(id = 2L, categoryId = 10L, type = TransactionType.EXPENSE, amount = MinorMoney(4000L), categoryName = "Vegetables", ledgerDateEpochDay = 101L)

        val breakdown = CategoryDrillDownCalculator.computeSubcategoryBreakdown(
            transactions = listOf(tx1, tx2),
            parentNetAmount = MinorMoney(10000L),
            parentCategoryName = "Groceries"
        )

        assertEquals(2, breakdown.size)
        assertEquals("Fruits", breakdown[0].name)
        assertEquals(MinorMoney(6000L), breakdown[0].totalAmount)
        assertEquals(60f, breakdown[0].percentageOfParent, 0.01f)
        assertEquals(1, breakdown[0].transactionCount)

        assertEquals("Vegetables", breakdown[1].name)
        assertEquals(MinorMoney(4000L), breakdown[1].totalAmount)
        assertEquals(40f, breakdown[1].percentageOfParent, 0.01f)
        assertEquals(1, breakdown[1].transactionCount)
    }

    private fun createDummyTransaction(
        id: Long,
        categoryId: Long?,
        type: TransactionType,
        amount: MinorMoney,
        categoryName: String? = null,
        ledgerDateEpochDay: Long
    ): TransactionItem {
        return TransactionItem(
            id = id,
            accountId = 1L,
            accountName = "Checking",
            currencyCode = "INR",
            categoryId = categoryId,
            categoryName = categoryName,
            categoryIconName = "icon",
            categoryColorHex = "#000",
            parentCategoryName = null,
            amount = amount,
            type = type,
            payeeOrDescription = "Store",
            note = "",
            ledgerDateEpochDay = ledgerDateEpochDay,
            timestampEpochMillis = ledgerDateEpochDay * 86_400_000L
        )
    }
}

// File: app/src/test/java/com/folioledger/app/ui/stats/PayeeIntelligenceCalculationsTest.kt
package com.folioledger.app.ui.stats

import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.SpendRankingItem
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PayeeIntelligenceCalculationsTest {

    private val refEpochDay = 20757L // Approx Oct 2026

    private fun createTestTransaction(
        id: Long,
        payee: String,
        amountMinor: Long,
        type: TransactionType = TransactionType.EXPENSE,
        epochDay: Long = refEpochDay,
        categoryName: String = "Food & Dining",
        categoryColorHex: String = "#F59E0B",
        categoryIconName: String = "restaurant"
    ) = TransactionItem(
        id = id,
        accountId = 1L,
        accountName = "Bank Account",
        amount = MinorMoney(amountMinor),
        currencyCode = "INR",
        type = type,
        ledgerDateEpochDay = epochDay,
        timestampEpochMillis = epochDay * 86_400_000L,
        categoryId = 10L,
        categoryName = categoryName,
        categoryColorHex = categoryColorHex,
        categoryIconName = categoryIconName,
        note = "Test receipt",
        payeeOrDescription = payee,
        tags = emptyList()
    )

    @Test
    fun testPayeeSorting_bySpendVsByFrequency() {
        val apple = SpendRankingItem(
            label = "Apple Store",
            totalAmount = MinorMoney(100_000_00L),
            transactionCount = 2,
            percentageOfTotal = 60f
        )
        val starbucks = SpendRankingItem(
            label = "Starbucks",
            totalAmount = MinorMoney(12_000_00L),
            transactionCount = 24,
            percentageOfTotal = 7.2f
        )
        val swiggy = SpendRankingItem(
            label = "Swiggy",
            totalAmount = MinorMoney(20_000_00L),
            transactionCount = 18,
            percentageOfTotal = 12f
        )

        val rawPayees = listOf(apple, starbucks, swiggy)

        // Sort By Spend (Default)
        val sortedBySpend = rawPayees.sortedWith(
            compareByDescending<SpendRankingItem> { it.totalAmount }
                .thenByDescending { it.transactionCount }
        )
        assertEquals("Apple Store", sortedBySpend[0].label)
        assertEquals("Swiggy", sortedBySpend[1].label)
        assertEquals("Starbucks", sortedBySpend[2].label)

        // Sort By Frequency (Visits)
        val sortedByVisits = rawPayees.sortedWith(
            compareByDescending<SpendRankingItem> { it.transactionCount }
                .thenByDescending { it.totalAmount }
        )
        assertEquals("Starbucks", sortedByVisits[0].label)
        assertEquals("Swiggy", sortedByVisits[1].label)
        assertEquals("Apple Store", sortedByVisits[2].label)
    }

    @Test
    fun testPayeeDrillDownState_metricsCalculation() {
        val txs = listOf(
            createTestTransaction(1, "Starbucks", 400_00L),
            createTestTransaction(2, "Starbucks", 600_00L),
            createTestTransaction(3, "Starbucks", 500_00L),
            createTestTransaction(4, "Starbucks", 200_00L, type = TransactionType.REFUND), // 200 refund
            createTestTransaction(5, "Other Merchant", 1_000_00L) // Unrelated
        )

        val trendBars = listOf(
            MonthlyTrendBar("Oct 2026", refEpochDay - 15, refEpochDay + 15, MinorMoney.ZERO, MinorMoney.ZERO, MinorMoney.ZERO)
        )

        val drillDown = StatsPredictiveCalculations.computePayeeDrillDownState(
            payeeName = "Starbucks",
            allTxs = txs,
            trendBars = trendBars,
            startEpochDay = refEpochDay - 30,
            endEpochDay = refEpochDay + 30,
            isAllTime = false,
            primaryCurrencyCode = "INR"
        )

        // Total net spend: (400 + 600 + 500) - 200 = 1,300
        assertEquals("Starbucks", drillDown.payeeName)
        assertEquals(1300_00L, drillDown.totalPeriodSpend.amountMinor)
        assertEquals(1300_00L, drillDown.totalAllTimeSpend.amountMinor)
        assertEquals(3, drillDown.periodVisitCount)
        assertEquals(3, drillDown.allTimeVisitCount)
        // Average ticket: 1,300 / 3 = 433.33 (433_33)
        assertEquals(433_33L, drillDown.averageTicketSize.amountMinor)
        assertEquals(4, drillDown.transactions.size) // 3 expenses + 1 refund
    }

    @Test
    fun testPayeeDrillDownState_categoryDistribution() {
        val txs = listOf(
            createTestTransaction(1, "Amazon", 6_000_00L, categoryName = "Electronics", categoryColorHex = "#3B82F6"),
            createTestTransaction(2, "Amazon", 4_000_00L, categoryName = "Books", categoryColorHex = "#10B981")
        )

        val drillDown = StatsPredictiveCalculations.computePayeeDrillDownState(
            payeeName = "Amazon",
            allTxs = txs,
            trendBars = emptyList(),
            startEpochDay = 0,
            endEpochDay = 30000,
            isAllTime = true,
            primaryCurrencyCode = "INR"
        )

        assertEquals(2, drillDown.categoryDistribution.size)
        val firstCategory = drillDown.categoryDistribution[0]
        assertEquals("Electronics", firstCategory.categoryName)
        assertEquals(6_000_00L, firstCategory.netAmount.amountMinor)
        assertEquals(60.0f, firstCategory.percentageOfTotal, 0.01f)

        val secondCategory = drillDown.categoryDistribution[1]
        assertEquals("Books", secondCategory.categoryName)
        assertEquals(4_000_00L, secondCategory.netAmount.amountMinor)
        assertEquals(40.0f, secondCategory.percentageOfTotal, 0.01f)
    }

    @Test
    fun testPayeeDrillDownState_monthlySpendTrendBars() {
        val month1Start = 100L
        val month1End = 130L
        val month2Start = 131L
        val month2End = 160L

        val txs = listOf(
            createTestTransaction(1, "Netflix", 649_00L, epochDay = 115L),
            createTestTransaction(2, "Netflix", 649_00L, epochDay = 145L),
            createTestTransaction(3, "Spotify", 199_00L, epochDay = 145L) // Ignored
        )

        val trendBars = listOf(
            MonthlyTrendBar("Month 1", month1Start, month1End, MinorMoney.ZERO, MinorMoney(50_000_00L), MinorMoney.ZERO),
            MonthlyTrendBar("Month 2", month2Start, month2End, MinorMoney.ZERO, MinorMoney(60_000_00L), MinorMoney.ZERO)
        )

        val drillDown = StatsPredictiveCalculations.computePayeeDrillDownState(
            payeeName = "Netflix",
            allTxs = txs,
            trendBars = trendBars,
            startEpochDay = month1Start,
            endEpochDay = month2End,
            isAllTime = true,
            primaryCurrencyCode = "INR"
        )

        assertEquals(2, drillDown.monthlyTrendBars.size)
        assertEquals(649_00L, drillDown.monthlyTrendBars[0].totalExpense.amountMinor)
        assertEquals(649_00L, drillDown.monthlyTrendBars[1].totalExpense.amountMinor)
    }
}

// File: app/src/test/java/com/folioledger/app/ui/stats/TagProjectIntelligenceCalculationsTest.kt
package com.folioledger.app.ui.stats

import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TagProjectIntelligenceCalculationsTest {

    private val refEpochDay = 20757L // Approx Oct 2026

    private fun createTestTransaction(
        id: Long,
        amountMinor: Long,
        tags: List<String>,
        type: TransactionType = TransactionType.EXPENSE,
        epochDay: Long = refEpochDay,
        categoryName: String = "Food & Dining",
        categoryColorHex: String = "#F59E0B",
        categoryIconName: String = "restaurant",
        payee: String = "Test Merchant",
        currencyCode: String = "INR"
    ) = TransactionItem(
        id = id,
        accountId = 1L,
        accountName = "Bank Account",
        amount = MinorMoney(amountMinor),
        currencyCode = currencyCode,
        type = type,
        ledgerDateEpochDay = epochDay,
        timestampEpochMillis = epochDay * 86_400_000L,
        categoryId = 10L,
        categoryName = categoryName,
        categoryColorHex = categoryColorHex,
        categoryIconName = categoryIconName,
        note = "Project receipt",
        payeeOrDescription = payee,
        tags = tags
    )

    @Test
    fun testEventTripTag_durationDailyBurnAndCategoryBreakdown() {
        val txs = listOf(
            createTestTransaction(
                id = 1,
                amountMinor = 25_000_00L,
                tags = listOf("EuropeTrip", "Flights"),
                epochDay = refEpochDay,
                categoryName = "Transportation",
                categoryColorHex = "#3B82F6",
                categoryIconName = "flight"
            ),
            createTestTransaction(
                id = 2,
                amountMinor = 15_000_00L,
                tags = listOf("#EuropeTrip"),
                epochDay = refEpochDay + 2,
                categoryName = "Lodging",
                categoryColorHex = "#8B5CF6",
                categoryIconName = "hotel"
            ),
            createTestTransaction(
                id = 3,
                amountMinor = 5_000_00L,
                tags = listOf("europetrip"),
                epochDay = refEpochDay + 4,
                categoryName = "Food & Dining",
                categoryColorHex = "#F59E0B",
                categoryIconName = "restaurant"
            ),
            createTestTransaction(
                id = 4,
                amountMinor = 10_000_00L,
                tags = listOf("UnrelatedTag"),
                epochDay = refEpochDay
            )
        )

        val trendBars = listOf(
            MonthlyTrendBar("Oct 2026", refEpochDay - 10, refEpochDay + 10, MinorMoney.ZERO, MinorMoney.ZERO, MinorMoney.ZERO)
        )

        val drillDown = StatsPredictiveCalculations.computeTagProjectDrillDownState(
            tagName = "EuropeTrip",
            allTxs = txs,
            trendBars = trendBars,
            startEpochDay = refEpochDay - 10,
            endEpochDay = refEpochDay + 10,
            isAllTime = false,
            primaryCurrencyCode = "INR"
        )

        assertFalse("Standard trip tag should not be classified as reimbursable", drillDown.isReimbursableProject)
        assertEquals(MinorMoney(45_000_00L), drillDown.totalSpent)
        assertEquals(MinorMoney.ZERO, drillDown.totalReimbursed)
        assertEquals(5, drillDown.dateSpanDays) // refEpochDay to refEpochDay + 4 => 5 days span
        assertEquals(MinorMoney(9_000_00L), drillDown.dailyAverageSpend) // 45,000 / 5 = 9,000 / day
        assertEquals(3, drillDown.transactions.size)

        // Category breakdown verification
        assertEquals(3, drillDown.categoryDistribution.size)
        assertEquals("Transportation", drillDown.categoryDistribution[0].categoryName)
        assertEquals(MinorMoney(25_000_00L), drillDown.categoryDistribution[0].netAmount)
        assertEquals("Lodging", drillDown.categoryDistribution[1].categoryName)
        assertEquals(MinorMoney(15_000_00L), drillDown.categoryDistribution[1].netAmount)
        assertEquals("Food & Dining", drillDown.categoryDistribution[2].categoryName)
        assertEquals(MinorMoney(5_000_00L), drillDown.categoryDistribution[2].netAmount)
    }

    @Test
    fun testReimbursableTag_outlayReimbursementNetting() {
        val txs = listOf(
            createTestTransaction(
                id = 1,
                amountMinor = 2_500_00L,
                tags = listOf("Reimbursable"),
                type = TransactionType.EXPENSE
            ),
            createTestTransaction(
                id = 2,
                amountMinor = 1_500_00L,
                tags = listOf("#reimbursable"),
                type = TransactionType.EXPENSE
            ),
            createTestTransaction(
                id = 3,
                amountMinor = 3_000_00L,
                tags = listOf("reimbursable"),
                type = TransactionType.REFUND // Employer reimbursed ₹3,000
            )
        )

        val drillDown = StatsPredictiveCalculations.computeTagProjectDrillDownState(
            tagName = "Reimbursable",
            allTxs = txs,
            trendBars = emptyList(),
            startEpochDay = refEpochDay - 10,
            endEpochDay = refEpochDay + 10,
            isAllTime = true,
            primaryCurrencyCode = "INR"
        )

        assertTrue("Tag containing 'reimbursable' or refund should be classified as reimbursable project", drillDown.isReimbursableProject)
        assertEquals(MinorMoney(4_000_00L), drillDown.totalSpent)
        assertEquals(MinorMoney(3_000_00L), drillDown.totalReimbursed)
        assertEquals(MinorMoney(1_000_00L), drillDown.netPendingOwed) // 4,000 - 3,000 = 1,000 still owed
        assertFalse("Should not be fully settled while dues remain", drillDown.isFullySettled)
    }

    @Test
    fun testReimbursableTag_fullySettledState() {
        val txs = listOf(
            createTestTransaction(
                id = 1,
                amountMinor = 5_000_00L,
                tags = listOf("OfficeLunch"),
                type = TransactionType.EXPENSE
            ),
            createTestTransaction(
                id = 2,
                amountMinor = 5_000_00L,
                tags = listOf("OfficeLunch"),
                type = TransactionType.INCOME // Full reimbursement received
            )
        )

        val drillDown = StatsPredictiveCalculations.computeTagProjectDrillDownState(
            tagName = "#OfficeLunch",
            allTxs = txs,
            trendBars = emptyList(),
            startEpochDay = refEpochDay - 10,
            endEpochDay = refEpochDay + 10,
            isAllTime = true,
            primaryCurrencyCode = "INR"
        )

        assertTrue(drillDown.isReimbursableProject)
        assertEquals(MinorMoney(5_000_00L), drillDown.totalSpent)
        assertEquals(MinorMoney(5_000_00L), drillDown.totalReimbursed)
        assertEquals(MinorMoney.ZERO, drillDown.netPendingOwed)
        assertTrue("Project should be marked fully settled when all spent funds are reimbursed", drillDown.isFullySettled)
    }

    @Test
    fun testMultiCurrencyTagProject_conversion() {
        val secondary = listOf(
            SecondaryCurrency(code = "USD", exchangeRateToPrimary = 85.0)
        )

        val txs = listOf(
            createTestTransaction(
                id = 1,
                amountMinor = 100_00L, // $100 USD
                currencyCode = "USD",
                tags = listOf("Conference"),
                type = TransactionType.EXPENSE
            )
        )

        val drillDown = StatsPredictiveCalculations.computeTagProjectDrillDownState(
            tagName = "Conference",
            allTxs = txs,
            trendBars = emptyList(),
            startEpochDay = refEpochDay - 10,
            endEpochDay = refEpochDay + 10,
            isAllTime = true,
            primaryCurrencyCode = "INR",
            secondaryCurrencies = secondary
        )

        // $100 * 85 = ₹8,500.00
        assertEquals(MinorMoney(8_500_00L), drillDown.totalSpent)
    }
}

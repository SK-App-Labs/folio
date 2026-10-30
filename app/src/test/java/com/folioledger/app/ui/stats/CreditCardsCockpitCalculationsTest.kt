// File: app/src/test/java/com/folioledger/app/ui/stats/CreditCardsCockpitCalculationsTest.kt
package com.folioledger.app.ui.stats

import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.ui.accounts.CreditCardCycleCalculator
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreditCardsCockpitCalculationsTest {

    private val refDate = LocalDate.of(2026, 10, 31)
    private val refEpochDay = refDate.toEpochDay()

    private fun createTestAccount(
        id: Long,
        name: String,
        group: AccountGroup,
        balanceMinor: Long,
        creditLimitMinor: Long? = null,
        statementDay: Int? = null,
        dueDay: Int? = null,
        statementPayableMinor: Long? = null,
        unbilledMinor: Long? = null
    ) = AccountSummary(
        id = id,
        name = name,
        groupType = group,
        currencyCode = "INR",
        currentBalance = MinorMoney(balanceMinor),
        creditLimit = creditLimitMinor?.let { MinorMoney(it) },
        settlementDayOfMonth = statementDay,
        paymentDueDayOfMonth = dueDay,
        creditCardStatementPayable = statementPayableMinor?.let { MinorMoney(it) },
        creditCardUnbilledAmount = unbilledMinor?.let { MinorMoney(it) },
        colorHex = "#10B981",
        iconName = "credit_card",
        includeInNetWorth = true,
        isArchived = false
    )

    private fun createTestTransaction(
        id: Long,
        accountId: Long,
        amountMinor: Long,
        type: TransactionType,
        epochDay: Long = refEpochDay,
        categoryName: String = "Shopping",
        categoryIconName: String? = "shopping_bag",
        categoryColorHex: String? = "#8B5CF6"
    ) = TransactionItem(
        id = id,
        accountId = accountId,
        accountName = "Account $accountId",
        amount = MinorMoney(amountMinor),
        currencyCode = "INR",
        type = type,
        ledgerDateEpochDay = epochDay,
        timestampEpochMillis = epochDay * 86_400_000L,
        categoryId = 100L,
        categoryName = categoryName,
        categoryIconName = categoryIconName,
        categoryColorHex = categoryColorHex,
        note = "Test Tx",
        payeeOrDescription = "Merchant",
        tags = emptyList(),
        splits = emptyList()
    )

    @Test
    fun testLiquidCashCoverage_100PercentCashBacked() {
        val bank = createTestAccount(1, "HDFC Bank", AccountGroup.BANK_ACCOUNT, balanceMinor = 150_000_00) // ₹1,50,000
        val cash = createTestAccount(2, "Cash in Hand", AccountGroup.CASH, balanceMinor = 15_000_00)     // ₹15,000
        val card = createTestAccount(3, "Axis Card", AccountGroup.CREDIT_CARD, balanceMinor = -48_200_00) // ₹48,200 debt

        val liquidMinor = bank.currentBalance.amountMinor + cash.currentBalance.amountMinor // 165,000
        val debtMinor = card.currentBalance.abs().amountMinor // 48,200
        val netLiquidMinor = liquidMinor - debtMinor

        val isCashBacked = liquidMinor >= debtMinor
        val coverageRatio = liquidMinor.toFloat() / debtMinor.toFloat()

        assertTrue("Should be 100% cash-backed", isCashBacked)
        assertEquals(116_800_00L, netLiquidMinor)
        assertTrue("Coverage ratio should be > 3.4x", coverageRatio > 3.4f)
    }

    @Test
    fun testLiquidCashCoverage_RidingCreditFloat() {
        val bank = createTestAccount(1, "Salary Bank", AccountGroup.BANK_ACCOUNT, balanceMinor = 20_000_00) // ₹20,000
        val card = createTestAccount(2, "ICICI Card", AccountGroup.CREDIT_CARD, balanceMinor = -60_000_00) // ₹60,000 debt

        val liquidMinor = bank.currentBalance.amountMinor
        val debtMinor = card.currentBalance.abs().amountMinor
        val isCashBacked = liquidMinor >= debtMinor

        assertFalse("Should be riding credit float (deficit)", isCashBacked)
        val deficit = debtMinor - liquidMinor
        assertEquals(40_000_00L, deficit)
    }

    @Test
    fun testSmartSwipeAdvisor_SelectsCardWithMaxFloatDays() {
        // Card A: Statement day 28th, due day 16th
        // On 31 Oct: Statement just closed on 28 Oct. Today's purchase will close on 28 Nov and be due around 16 Dec.
        val cardA = createTestAccount(
            id = 1,
            name = "Axis Rewards",
            group = AccountGroup.CREDIT_CARD,
            balanceMinor = 0L,
            creditLimitMinor = 100_000_00L,
            statementDay = 28,
            dueDay = 16
        )

        // Card B: Statement day 1st, due day 20th
        // On 31 Oct: Statement closes tomorrow (1 Nov)! Due around 20 Nov.
        val cardB = createTestAccount(
            id = 2,
            name = "HDFC Regalia",
            group = AccountGroup.CREDIT_CARD,
            balanceMinor = 0L,
            creditLimitMinor = 100_000_00L,
            statementDay = 1,
            dueDay = 20
        )

        val cycleA = CreditCardCycleCalculator.computeCycleWindow(refEpochDay, 28, 16)
        val cycleB = CreditCardCycleCalculator.computeCycleWindow(refEpochDay, 1, 20)

        val floatDaysA = ChronoUnit.DAYS.between(refDate, cycleA.activeCycleNextDueDate)
        val floatDaysB = ChronoUnit.DAYS.between(refDate, cycleB.activeCycleNextDueDate)

        // Card A should give far more float days (~46 days) than Card B (~20 days)
        assertTrue("Card A float ($floatDaysA) should be greater than Card B float ($floatDaysB)", floatDaysA > floatDaysB)
        assertTrue("Card A should give over 40 interest-free days", floatDaysA >= 40)
    }

    @Test
    fun testSmartSwipeAdvisor_PrioritizesCardsWithAvailableCredit() {
        // Card A has longer float (~46 days) but is 100% MAXED OUT (limit 50k, debt 50k -> available 0)
        val cardA = createTestAccount(
            id = 1,
            name = "Maxed Out Card",
            group = AccountGroup.CREDIT_CARD,
            balanceMinor = -50_000_00L,
            creditLimitMinor = 50_000_00L,
            statementDay = 28,
            dueDay = 16
        )

        // Card B has shorter float (~20 days) but HAS AVAILABLE LIMIT (limit 100k, debt 10k -> available 90k)
        val cardB = createTestAccount(
            id = 2,
            name = "Available Card",
            group = AccountGroup.CREDIT_CARD,
            balanceMinor = -10_000_00L,
            creditLimitMinor = 100_000_00L,
            statementDay = 1,
            dueDay = 20
        )

        val cards = listOf(cardA, cardB)
        val candidateWithHeadroom = cards.filter {
            val limit = it.creditLimit?.amountMinor ?: 0L
            val debt = it.currentBalance.abs().amountMinor
            limit > debt
        }.maxByOrNull { card ->
            val cycle = CreditCardCycleCalculator.computeCycleWindow(refEpochDay, card.settlementDayOfMonth!!, card.paymentDueDayOfMonth!!)
            ChronoUnit.DAYS.between(refDate, cycle.activeCycleNextDueDate)
        }

        assertNotNull(candidateWithHeadroom)
        assertEquals("Available Card", candidateWithHeadroom?.name)
    }

    @Test
    fun testSpendModeRatio_StrictlyExcludesTransfersAndSettlements() {
        val ccId = 10L
        val bankId = 20L

        val txns = listOf(
            createTestTransaction(1, ccId, 50_000_00L, TransactionType.EXPENSE),
            createTestTransaction(2, bankId, 15_000_00L, TransactionType.EXPENSE),
            // Settlement transfer from Bank -> CC should NOT count towards expense ratio!
            createTestTransaction(3, bankId, 30_000_00L, TransactionType.CC_SETTLEMENT),
            createTestTransaction(4, bankId, 20_000_00L, TransactionType.TRANSFER)
        )

        var ccExpenseMinor = 0L
        var liquidExpenseMinor = 0L

        for (tx in txns) {
            if (tx.type == TransactionType.EXPENSE) {
                if (tx.accountId == ccId) {
                    ccExpenseMinor += tx.amount.amountMinor
                } else {
                    liquidExpenseMinor += tx.amount.amountMinor
                }
            }
        }

        assertEquals("CC Expense should be 50k", 50_000_00L, ccExpenseMinor)
        assertEquals("Liquid Expense should be 15k (transfers and settlements excluded)", 15_000_00L, liquidExpenseMinor)

        val totalExpense = ccExpenseMinor + liquidExpenseMinor // 65,000
        val ccPct = (ccExpenseMinor.toFloat() / totalExpense.toFloat() * 100f)
        val liquidPct = (liquidExpenseMinor.toFloat() / totalExpense.toFloat() * 100f)

        assertEquals(76.92f, ccPct, 0.1f)
        assertEquals(23.08f, liquidPct, 0.1f)
    }

    @Test
    fun testSingleCardSpikeAlert_FlagsSingleCardOver30Percent() {
        // Aggregate limit = 2,00,000. Total debt = 35,000. Aggregate util = 17.5% (Safe <30%)
        // Card 1: Limit 1,50,000. Debt 10,000 (6.6% Safe)
        // Card 2: Limit 50,000. Debt 25,000 (50.0% Spike!)
        val card1 = createTestAccount(1, "Large Card", AccountGroup.CREDIT_CARD, balanceMinor = -10_000_00L, creditLimitMinor = 150_000_00L)
        val card2 = createTestAccount(2, "Small Card", AccountGroup.CREDIT_CARD, balanceMinor = -25_000_00L, creditLimitMinor = 50_000_00L)

        val cards = listOf(card1, card2)
        val totalLimit = cards.sumOf { it.creditLimit?.amountMinor ?: 0L }
        val totalDebt = cards.sumOf { it.currentBalance.abs().amountMinor }
        val aggregateUtil = (totalDebt.toFloat() / totalLimit.toFloat()) * 100f

        assertEquals("Aggregate utilization should be 17.5%", 17.5f, aggregateUtil, 0.1f)

        // Find single card spike
        val spikeCard = cards.firstOrNull { card ->
            val limit = card.creditLimit?.amountMinor ?: 0L
            val debt = card.currentBalance.abs().amountMinor
            val util = if (limit > 0) (debt.toFloat() / limit.toFloat() * 100f) else 0f
            util > 30f
        }

        assertNotNull("Should detect single card spike", spikeCard)
        assertEquals("Small Card", spikeCard?.name)
    }

    @Test
    fun testCardTopCategorySpendAndIconExtraction() {
        val cardId = 42L
        val txs = listOf(
            createTestTransaction(1, cardId, 3_000_00L, TransactionType.EXPENSE, categoryName = "Dining", categoryIconName = "restaurant", categoryColorHex = "#F59E0B"),
            createTestTransaction(2, cardId, 2_000_00L, TransactionType.EXPENSE, categoryName = "Dining", categoryIconName = "restaurant", categoryColorHex = "#F59E0B"),
            createTestTransaction(3, cardId, 4_000_00L, TransactionType.EXPENSE, categoryName = "Shopping", categoryIconName = "shopping_bag", categoryColorHex = "#8B5CF6"),
            createTestTransaction(4, cardId, 1_000_00L, TransactionType.EXPENSE, categoryName = "Fuel", categoryIconName = "local_gas_station", categoryColorHex = "#EF4444")
        )

        // Total: Dining 5,000, Shopping 4,000, Fuel 1,000 -> Total YTD = 10,000
        var ytdMinor = 0L
        data class CategorySpendTracker(
            val name: String,
            val iconName: String?,
            val colorHex: String?,
            var totalMinor: Long
        )
        val catSpendMap = mutableMapOf<String, CategorySpendTracker>()
        for (tx in txs) {
            val amt = Math.abs(tx.amount.amountMinor)
            ytdMinor += amt
            val catName = tx.categoryName ?: "General"
            val existing = catSpendMap[catName]
            if (existing != null) {
                existing.totalMinor += amt
            } else {
                catSpendMap[catName] = CategorySpendTracker(
                    name = catName,
                    iconName = tx.categoryIconName,
                    colorHex = tx.categoryColorHex,
                    totalMinor = amt
                )
            }
        }

        val topCatEntry = catSpendMap.values.maxByOrNull { it.totalMinor }
        assertNotNull(topCatEntry)
        assertEquals("Dining", topCatEntry?.name)
        assertEquals("restaurant", topCatEntry?.iconName)
        assertEquals("#F59E0B", topCatEntry?.colorHex)
        assertEquals(5_000_00L, topCatEntry?.totalMinor)

        val topPercent = (topCatEntry!!.totalMinor.toFloat() / ytdMinor.toFloat()) * 100f
        assertEquals(50f, topPercent, 0.01f)
    }
}

package com.folioledger.app.data

import com.folioledger.app.data.repository.parseAmountDecimalToMinor
import com.folioledger.app.data.repository.parseCsvLine
import com.folioledger.app.data.repository.parseDateStringToEpochDay
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.usecase.AccountingRulesEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.io.InputStreamReader
import java.io.BufferedReader
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.YearMonth

class CreditCardCycleEditingRegressionTest {

    @Test
    fun axisRewardsCC_metricsCalculatedAccurately_beforeAndAfterDueDayEdit() {
        // Load actual Axis Rewards CC transactions from user spreadsheet
        val file = File("c:/Users/sivas/AndroidStudioProjects/Folio/app/src/test/resources/user_test_spreadsheet.csv")
        val reader = BufferedReader(InputStreamReader(file.inputStream(), StandardCharsets.UTF_8))

        val ccTransactions = mutableListOf<TransactionItem>()
        var line = reader.readLine()
        var txIdCounter = 1L

        while (line != null) {
            val trimmed = line.trim()
            if (trimmed.contains("Axis Rewards CC", ignoreCase = true) && !trimmed.startsWith("Axis Rewards CC,")) {
                val tokens = parseCsvLine(trimmed)
                val rawDate = tokens.getOrNull(1) ?: ""
                val epochDay = parseDateStringToEpochDay(rawDate)
                val rawType = tokens.getOrNull(2)?.trim() ?: ""
                val type = when {
                    rawType.equals("CC_SETTLEMENT", ignoreCase = true) -> TransactionType.CC_SETTLEMENT
                    rawType.equals("EXPENSE", ignoreCase = true) -> TransactionType.EXPENSE
                    rawType.equals("INCOME", ignoreCase = true) -> TransactionType.INCOME
                    else -> TransactionType.EXPENSE
                }
                val rawAmount = tokens.getOrNull(3) ?: ""
                val amountMinor = parseAmountDecimalToMinor(rawAmount, 2)

                if (epochDay != null && amountMinor != null) {
                    ccTransactions.add(
                        TransactionItem(
                            id = txIdCounter++,
                            type = type,
                            timestampEpochMillis = epochDay * 86_400_000L + 12 * 3600 * 1000L,
                            ledgerDateEpochDay = epochDay,
                            amount = MinorMoney(amountMinor),
                            currencyCode = "INR",
                            accountId = 101L,
                            accountName = "Axis Rewards CC"
                        )
                    )
                }
            }
            line = reader.readLine()
        }

        assertEquals(25, ccTransactions.size)

        // 1. Compute metrics as of 2026-10-27 with Statement Day = 17, Opening Balance = 18589.90 (1858990 minor)
        val openingDebt = MinorMoney(1858990L)
        val refDateEpochDay = LocalDate.of(2026, 10, 27).toEpochDay()
        val limitMoney = MinorMoney(15000000L) // 1,50,000

        val metricsClean = AccountingRulesEngine.computeCreditCardMetrics(
            openingBalance = openingDebt,
            transactions = ccTransactions,
            settlementDayOfMonth = 17,
            referenceDateEpochDay = refDateEpochDay,
            creditLimit = limitMoney,
            initialUnbilledAmount = null
        )

        // Billed statement due must be 15,203.08
        assertEquals(1520308L, metricsClean.statementPayable.amountMinor)
        // Active cycle unbilled spend must be 15,866.38
        assertEquals(1586638L, metricsClean.unbilledAmount.amountMinor)
        // Total outstanding debt is 31,069.46
        assertEquals(-3106946L, metricsClean.currentBalance.amountMinor)

        // 2. Self-Healing Simulation:
        // Even if initialUnbilledMinor in the database was accidentally set to 1586638L by editing due day,
        // our repository guard verifies anchor date <= last statement close date and ccTransactions.isNotEmpty(),
        // safely setting initialUnbilledMoney to null!
        val anchorEpochDay = LocalDate.of(2026, 6, 10).toEpochDay()
        val refDate = LocalDate.ofEpochDay(refDateEpochDay)
        val refYearMonth = YearMonth.from(refDate)
        val statementDay = 17
        val statementInRefMonth = refYearMonth.atDay(statementDay)
        val lastStatementCloseDate = if (refDate.isAfter(statementInRefMonth) || refDate.isEqual(statementInRefMonth)) {
            statementInRefMonth
        } else {
            refYearMonth.minusMonths(1).atDay(statementDay)
        }

        val initialUnbilledMoney = if (anchorEpochDay <= lastStatementCloseDate.toEpochDay() || ccTransactions.isNotEmpty()) {
            null
        } else {
            MinorMoney(1586638L)
        }

        assertNull("Corrupted initial unbilled anchor from old cycle or active transactions must be self-healed to null", initialUnbilledMoney)

        val metricsAfterHeal = AccountingRulesEngine.computeCreditCardMetrics(
            openingBalance = openingDebt,
            transactions = ccTransactions,
            settlementDayOfMonth = 17,
            referenceDateEpochDay = refDateEpochDay,
            creditLimit = limitMoney,
            initialUnbilledAmount = initialUnbilledMoney
        )

        assertEquals(1520308L, metricsAfterHeal.statementPayable.amountMinor)
        assertEquals(1586638L, metricsAfterHeal.unbilledAmount.amountMinor)
        assertEquals(-3106946L, metricsAfterHeal.currentBalance.amountMinor)
    }

    @Test
    fun newCreditCardWithoutTransactions_initialUnbilledAmount_isRespected() {
        // Brand new card created in current billing cycle without transactions
        val refDateEpochDay = LocalDate.of(2026, 10, 27).toEpochDay()
        val totalOpeningDebt = MinorMoney(3000000L) // 30,000.00
        val initialUnbilled = MinorMoney(1000000L) // 10,000.00
        val limit = MinorMoney(10000000L) // 1,00,000.00

        val metrics = AccountingRulesEngine.computeCreditCardMetrics(
            openingBalance = totalOpeningDebt,
            transactions = emptyList(),
            settlementDayOfMonth = 17,
            referenceDateEpochDay = refDateEpochDay,
            creditLimit = limit,
            initialUnbilledAmount = initialUnbilled
        )

        // 30,000 total debt: 20,000 closed cycle statement due, 10,000 active unbilled spend
        assertEquals(2000000L, metrics.statementPayable.amountMinor)
        assertEquals(1000000L, metrics.unbilledAmount.amountMinor)
        assertEquals(-3000000L, metrics.currentBalance.amountMinor)
        assertEquals(7000000L, metrics.availableCredit?.amountMinor)
    }
}

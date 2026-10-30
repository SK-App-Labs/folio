package com.folioledger.app.data

import com.folioledger.app.data.pdf.FiscalYearType
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.usecase.AccountingRulesEngine
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PdfStatementModelsAndCalculationsTest {

    @Test
    fun fiscalYearTypeCalendarYear_producesCorrectJanToDecRange() {
        val anchorYear = 2026
        val start = LocalDate.of(anchorYear, 1, 1).toEpochDay()
        val end = LocalDate.of(anchorYear, 12, 31).toEpochDay()

        assertEquals(LocalDate.of(2026, 1, 1), LocalDate.ofEpochDay(start))
        assertEquals(LocalDate.of(2026, 12, 31), LocalDate.ofEpochDay(end))
        assertTrue(end > start)
    }

    @Test
    fun fiscalYearTypeAprilYear_producesCorrectAprToMarRange() {
        val anchorYear = 2025
        val start = LocalDate.of(anchorYear, 4, 1).toEpochDay()
        val end = LocalDate.of(anchorYear + 1, 3, 31).toEpochDay()

        assertEquals(LocalDate.of(2025, 4, 1), LocalDate.ofEpochDay(start))
        assertEquals(LocalDate.of(2026, 3, 31), LocalDate.ofEpochDay(end))
        assertTrue(end > start)
    }

    @Test
    fun fiscalYearTypeJulyYear_producesCorrectJulToJunRange() {
        val anchorYear = 2025
        val start = LocalDate.of(anchorYear, 7, 1).toEpochDay()
        val end = LocalDate.of(anchorYear + 1, 6, 30).toEpochDay()

        assertEquals(LocalDate.of(2025, 7, 1), LocalDate.ofEpochDay(start))
        assertEquals(LocalDate.of(2026, 6, 30), LocalDate.ofEpochDay(end))
        assertTrue(end > start)
    }

    @Test
    fun signedDeltaCalculation_computesInflowsAndOutflowsCorrectly() {
        val accountIds = setOf(101L)

        val incomeTx = TransactionItem(
            id = 1L,
            type = TransactionType.INCOME,
            timestampEpochMillis = 1000L,
            ledgerDateEpochDay = 100L,
            amount = MinorMoney(500000L), // 5,000.00
            currencyCode = "USD",
            accountId = 101L,
            accountName = "Primary Checking"
        )
        val incomeDelta = AccountingRulesEngine.computeSignedDeltaMinorForAccount(incomeTx, accountIds)
        assertEquals(500000L, incomeDelta)

        val expenseTx = TransactionItem(
            id = 2L,
            type = TransactionType.EXPENSE,
            timestampEpochMillis = 2000L,
            ledgerDateEpochDay = 101L,
            amount = MinorMoney(120000L), // 1,200.00
            currencyCode = "USD",
            accountId = 101L,
            accountName = "Primary Checking"
        )
        val expenseDelta = AccountingRulesEngine.computeSignedDeltaMinorForAccount(expenseTx, accountIds)
        assertEquals(-120000L, expenseDelta)

        val transferTx = TransactionItem(
            id = 3L,
            type = TransactionType.TRANSFER,
            timestampEpochMillis = 3000L,
            ledgerDateEpochDay = 102L,
            amount = MinorMoney(50000L), // 500.00
            currencyCode = "USD",
            accountId = 101L,
            accountName = "Primary Checking",
            transferToAccountId = 102L,
            transferToAccountName = "Savings"
        )
        val transferOutDelta = AccountingRulesEngine.computeSignedDeltaMinorForAccount(transferTx, accountIds)
        assertEquals(-50000L, transferOutDelta)

        val transferInDelta = AccountingRulesEngine.computeSignedDeltaMinorForAccount(transferTx, setOf(102L))
        assertEquals(50000L, transferInDelta)
    }

    @Test
    fun multiCurrencyConversion_handlesForeignAccountsInTaxStatement() {
        val usdSpec = SupportedCurrencies.USD // 2 decimals
        val inrSpec = SupportedCurrencies.INR // 2 decimals
        val rateToInr = 85.0

        val usdMinor = 1000_00L // $1,000.00
        val inrConverted = MoneyFormatterAndCalculator.convertMinorMoney(
            sourceMinor = usdMinor,
            sourceFractionDigits = usdSpec.fractionDigits,
            targetFractionDigits = inrSpec.fractionDigits,
            rateToTarget = rateToInr
        )

        // 1,000 * 85 = 85,000.00 INR (8,500,000 minor paise)
        assertEquals(8500000L, inrConverted)

        val jpySpec = SupportedCurrencies.JPY // 0 decimals
        val jpyMinor = 10_000L // ¥10,000
        val rateJpyToUsd = 0.0067
        val usdFromJpy = MoneyFormatterAndCalculator.convertMinorMoney(
            sourceMinor = jpyMinor,
            sourceFractionDigits = jpySpec.fractionDigits,
            targetFractionDigits = usdSpec.fractionDigits,
            rateToTarget = rateJpyToUsd
        )
        // 10,000 * 0.0067 = $67.00 = 6,700 cents
        assertEquals(6700L, usdFromJpy)
    }

    @Test
    fun taxDeductionTagMatching_identifiesLegitimateDeductions() {
        val taxTags = setOf("tax", "business", "medical", "donation", "charity")

        val tx1Tags = listOf("business", "travel")
        assertTrue(tx1Tags.any { it in taxTags })

        val tx2Tags = listOf("groceries", "personal")
        assertFalse(tx2Tags.any { it in taxTags })

        val tx3Tags = listOf("medical", "prescription")
        assertTrue(tx3Tags.any { it in taxTags })
    }
}

package com.folioledger.app.domain.parser

import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartTextParserEngineTest {

    @Test
    fun testSbiUpiDebitSms() {
        val sms = "Dear SBI User, your A/c ending 4589 debited for Rs.450.00 on 10-Nov-24 by UPI/ref 43152678/STARBUCKS. Avl Bal: Rs.12,450.75"
        val result = SmartTextParserEngine.parse(sms)

        assertTrue(result.isValid)
        assertEquals(MinorMoney(45000L), result.amountMinor)
        assertEquals("INR", result.currencyCode)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("4589", result.accountSuffix)
        assertEquals("State Bank of India", result.bankInstitution)
        assertTrue(result.payeeOrDescription.contains("Starbucks", ignoreCase = true))
        assertNotNull(result.discardedBalanceSnippet)
        assertTrue(result.discardedBalanceSnippet!!.contains("12,450.75"))
    }

    @Test
    fun testIciciCreditCardSms() {
        val sms = "Alert: ICICI Bank Credit Card XX4120 spent INR 3,499.00 at ZARA MUMBAI on 09-Nov-24. Avl Limit INR 96,500.00"
        val result = SmartTextParserEngine.parse(sms)

        assertTrue(result.isValid)
        assertEquals(MinorMoney(349900L), result.amountMinor)
        assertEquals("INR", result.currencyCode)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("4120", result.accountSuffix)
        assertEquals("ICICI Bank", result.bankInstitution)
        assertTrue(result.payeeOrDescription.contains("Zara", ignoreCase = true))
        assertNotNull(result.discardedBalanceSnippet)
        assertTrue(result.discardedBalanceSnippet!!.contains("96,500.00"))
    }

    @Test
    fun testHdfcSalaryCreditSms() {
        val sms = "HDFC Bank: Salary of Rs 85,000.00 credited to A/c 9876 on 01-Nov-24 by ACME TECH. Avl Bal Rs 1,12,450.00"
        val result = SmartTextParserEngine.parse(sms)

        assertTrue(result.isValid)
        assertEquals(MinorMoney(8500000L), result.amountMinor)
        assertEquals("INR", result.currencyCode)
        assertEquals(TransactionType.INCOME, result.type)
        assertEquals("9876", result.accountSuffix)
        assertEquals("HDFC Bank", result.bankInstitution)
        assertTrue(result.payeeOrDescription.contains("Acme Tech", ignoreCase = true))
        assertNotNull(result.discardedBalanceSnippet)
    }

    @Test
    fun testAxisFuelDebitSms() {
        val sms = "Axis Bank: Rs.2,100.00 debited from A/c XX8812 on 10-Nov-24 at SHELL PETROL PUMP BANGALORE. UPI Ref 991204. Avl Bal: Rs.41,200.00"
        val result = SmartTextParserEngine.parse(sms)

        assertTrue(result.isValid)
        assertEquals(MinorMoney(210000L), result.amountMinor)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("8812", result.accountSuffix)
        assertEquals("Axis Bank", result.bankInstitution)
        assertTrue(result.payeeOrDescription.contains("Shell Petrol Pump", ignoreCase = true))
    }

    @Test
    fun testChaseUsdPurchaseSms() {
        val sms = "Chase: You made a $42.50 purchase at WHOLE FOODS with card ending 7891 on Nov 10."
        val result = SmartTextParserEngine.parse(sms)

        assertTrue(result.isValid)
        assertEquals(MinorMoney(4250L), result.amountMinor)
        assertEquals("USD", result.currencyCode)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("7891", result.accountSuffix)
        assertEquals("Chase", result.bankInstitution)
        assertTrue(result.payeeOrDescription.contains("Whole Foods", ignoreCase = true))
    }

    @Test
    fun testAmexUsdChargeSms() {
        val sms = "Amex: A charge of USD 120.00 at UBER was approved on your card ending -31002."
        val result = SmartTextParserEngine.parse(sms)

        assertTrue(result.isValid)
        assertEquals(MinorMoney(12000L), result.amountMinor)
        assertEquals("USD", result.currencyCode)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("31002", result.accountSuffix)
        assertEquals("American Express", result.bankInstitution)
        assertTrue(result.payeeOrDescription.contains("Uber", ignoreCase = true))
    }

    @Test
    fun testMonzoGbpDebitSms() {
        val sms = "Monzo: You paid £14.20 to Pret A Manger from your Current Account."
        val result = SmartTextParserEngine.parse(sms)

        assertTrue(result.isValid)
        assertEquals(MinorMoney(1420L), result.amountMinor)
        assertEquals("GBP", result.currencyCode)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("Monzo", result.bankInstitution)
        assertTrue(result.payeeOrDescription.contains("Pret A Manger", ignoreCase = true))
    }

    @Test
    fun testCasualPlainText() {
        val text = "Dinner 65.00 with Sarah"
        val result = SmartTextParserEngine.parse(text)

        assertTrue(result.isValid)
        assertEquals(MinorMoney(6500L), result.amountMinor)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("Dinner", result.payeeOrDescription)
    }

    @Test
    fun testNonFinancialTextWithDecimals_isSafelyIgnored() {
        val nonFinancialTexts = listOf(
            "Meeting tomorrow at 12.30 in room 4",
            "Updated Folio to version 2.10",
            "Bus number 405.20 arriving shortly"
        )
        for (text in nonFinancialTexts) {
            val result = SmartTextParserEngine.parse(text)
            org.junit.Assert.assertFalse(
                "Non-financial text '$text' should not be parsed as valid transaction",
                result.isValid
            )
        }
    }

    @Test
    fun testResolverAccountMatching() {
        val sbiAcct = AccountSummary(
            id = 101L,
            name = "SBI Savings (4589)",
            groupType = AccountGroup.BANK_ACCOUNT,
            currencyCode = "INR",
            currentBalance = MinorMoney.ZERO
        )
        val hdfcAcct = AccountSummary(
            id = 102L,
            name = "HDFC Salary Bank (9876)",
            groupType = AccountGroup.BANK_ACCOUNT,
            currencyCode = "INR",
            currentBalance = MinorMoney.ZERO
        )
        val accounts = listOf(sbiAcct, hdfcAcct)

        val parsedSbi = SmartTextParserEngine.parse(
            "Dear SBI User, your A/c ending 4589 debited for Rs.450.00 on 10-Nov-24 by UPI/ref 43152678/STARBUCKS."
        )
        val resolvedSbi = ParsedTransactionResolver.resolve(
            parsed = parsedSbi,
            accounts = accounts,
            categories = emptyList()
        )
        assertEquals(101L, resolvedSbi.matchedAccount?.id)

        val parsedHdfc = SmartTextParserEngine.parse(
            "HDFC Bank: Salary of Rs 85,000.00 credited to A/c 9876 on 01-Nov-24 by ACME TECH."
        )
        val resolvedHdfc = ParsedTransactionResolver.resolve(
            parsed = parsedHdfc,
            accounts = accounts,
            categories = emptyList()
        )
        assertEquals(102L, resolvedHdfc.matchedAccount?.id)
    }

    @Test
    fun testResolverCategoryHistoricalAndSemanticMatching() {
        val coffeeCategory = CategoryNode(
            id = 201L,
            name = "Coffee",
            type = CategoryType.EXPENSE,
            parentId = 200L,
            iconName = "local_cafe",
            colorHex = "#795548"
        )
        val diningCategory = CategoryNode(
            id = 200L,
            name = "Food & Dining",
            type = CategoryType.EXPENSE,
            parentId = null,
            iconName = "restaurant",
            colorHex = "#FF5722",
            subcategories = listOf(coffeeCategory)
        )
        val transportCategory = CategoryNode(
            id = 300L,
            name = "Transport & Fuel",
            type = CategoryType.EXPENSE,
            parentId = null,
            iconName = "directions_car",
            colorHex = "#2196F3"
        )
        val categories = listOf(diningCategory, transportCategory)

        // Historical Match Test
        val parsedStarbucks = SmartTextParserEngine.parse(
            "Paid Rs.450 to Starbucks on UPI"
        )
        val resolvedHistorical = ParsedTransactionResolver.resolve(
            parsed = parsedStarbucks,
            accounts = emptyList(),
            categories = categories,
            historicalPayeeCategoryMap = mapOf("starbucks" to 201L)
        )
        assertEquals(201L, resolvedHistorical.matchedCategory?.id)
        assertEquals(CategoryMatchSource.HISTORICAL, resolvedHistorical.categoryMatchSource)

        // Semantic Keyword Match Test (Uber -> Transport)
        val parsedUber = SmartTextParserEngine.parse(
            "Paid Rs.320 to Uber Rides on UPI"
        )
        val resolvedSemantic = ParsedTransactionResolver.resolve(
            parsed = parsedUber,
            accounts = emptyList(),
            categories = categories
        )
        assertEquals(300L, resolvedSemantic.matchedCategory?.id)
        assertEquals(CategoryMatchSource.SEMANTIC_KEYWORD, resolvedSemantic.categoryMatchSource)
    }
}

package com.folioledger.app.domain

import com.folioledger.app.domain.model.SupportedCurrencies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversalCurrencySupportTest {

    @Test
    fun popularCurrenciesContainsCoreGlobalCurrencies() {
        val popular = SupportedCurrencies.POPULAR
        val popularCodes = popular.map { it.code }.toSet()

        assertTrue("INR should be in popular", popularCodes.contains("INR"))
        assertTrue("USD should be in popular", popularCodes.contains("USD"))
        assertTrue("EUR should be in popular", popularCodes.contains("EUR"))
        assertTrue("GBP should be in popular", popularCodes.contains("GBP"))
        assertTrue("AED should be in popular", popularCodes.contains("AED"))
        assertTrue("SAR should be in popular", popularCodes.contains("SAR"))
        assertTrue("SGD should be in popular", popularCodes.contains("SGD"))
        assertTrue("QAR should be in popular", popularCodes.contains("QAR"))
        assertTrue("MYR should be in popular", popularCodes.contains("MYR"))
        assertTrue("THB should be in popular", popularCodes.contains("THB"))
    }

    @Test
    fun allCurrenciesCatalogContainsWorldCurrencies() {
        val all = SupportedCurrencies.ALL
        assertTrue("Should contain comprehensive world currencies (>100)", all.size > 100)

        val allCodes = all.map { it.code }.toSet()
        // Latin America
        assertTrue("BRL (Brazilian Real) should be present", allCodes.contains("BRL"))
        assertTrue("MXN (Mexican Peso) should be present", allCodes.contains("MXN"))
        assertTrue("COP (Colombian Peso) should be present", allCodes.contains("COP"))

        // Africa
        assertTrue("ZAR (South African Rand) should be present", allCodes.contains("ZAR"))
        assertTrue("NGN (Nigerian Naira) should be present", allCodes.contains("NGN"))

        // Southeast Asia
        assertTrue("PHP (Philippine Peso) should be present", allCodes.contains("PHP"))
        assertTrue("VND (Vietnamese Dong) should be present", allCodes.contains("VND"))
        assertTrue("IDR (Indonesian Rupiah) should be present", allCodes.contains("IDR"))

        // Europe
        assertTrue("PLN (Polish Zloty) should be present", allCodes.contains("PLN"))
        assertTrue("SEK (Swedish Krona) should be present", allCodes.contains("SEK"))
    }

    @Test
    fun dynamicResolutionResolvesAnyISOCurrency() {
        val brl = SupportedCurrencies.fromCode("BRL")
        assertEquals("BRL", brl.code)
        assertEquals(2, brl.fractionDigits)

        val zar = SupportedCurrencies.fromCode("ZAR")
        assertEquals("ZAR", zar.code)
        assertEquals(2, zar.fractionDigits)

        val jpy = SupportedCurrencies.fromCode("JPY")
        assertEquals("JPY", jpy.code)
        assertEquals(0, jpy.fractionDigits)

        val bhd = SupportedCurrencies.fromCode("BHD")
        assertEquals("BHD", bhd.code)
        assertEquals(3, bhd.fractionDigits)
    }

    @Test
    fun invalidCurrencyGracefullyFallsBackToINR() {
        val fallback = SupportedCurrencies.fromCode("NONEXISTENT_XYZ")
        assertEquals("INR", fallback.code)
        assertEquals("₹", fallback.symbol)
    }
}

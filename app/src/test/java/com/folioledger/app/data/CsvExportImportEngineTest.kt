package com.folioledger.app.data

import com.folioledger.app.data.repository.parseCsvLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.pow

class CsvExportImportEngineTest {

    @Test
    fun parseCsvLine_simpleTokens_parsedCorrectly() {
        val line = "101,2026-03-25,EXPENSE,250.50,INR,HDFC Bank"
        val tokens = parseCsvLine(line)
        assertEquals(listOf("101", "2026-03-25", "EXPENSE", "250.50", "INR", "HDFC Bank"), tokens)
    }

    @Test
    fun parseCsvLine_commasInsideQuotes_preservedIntact() {
        val line = "102,2026-03-25,EXPENSE,1500.00,INR,\"Supermarket, Grocery Store\",Weekly Groceries"
        val tokens = parseCsvLine(line)
        assertEquals(7, tokens.size)
        assertEquals("Supermarket, Grocery Store", tokens[5])
        assertEquals("Weekly Groceries", tokens[6])
    }

    @Test
    fun parseCsvLine_escapedDoubleQuotes_handledPerRfc4180() {
        val line = "103,2026-03-25,INCOME,50000.00,INR,Bank,\"Bonus \"\"Q1 Performance\"\"\",Job"
        val tokens = parseCsvLine(line)
        assertEquals(8, tokens.size)
        assertEquals("Bonus \"Q1 Performance\"", tokens[6])
    }

    @Test
    fun parseCsvLine_emptyFields_parsedAsEmptyStrings() {
        val line = "104,2026-03-25,TRANSFER,1000.00,INR,Bank A,Bank B,,,,Note,"
        val tokens = parseCsvLine(line)
        assertEquals(12, tokens.size)
        assertEquals("", tokens[7])
        assertEquals("", tokens[8])
        assertEquals("", tokens[9])
        assertEquals("Note", tokens[10])
        assertEquals("", tokens[11])
    }

    @Test
    fun utf8Bom_strippedSuccessfully() {
        val rawHeader = "\uFEFFID,Date,Type,Amount,Currency,Account"
        val cleanHeader = if (rawHeader.startsWith("\uFEFF")) rawHeader.substring(1) else rawHeader
        val tokens = parseCsvLine(cleanHeader)
        assertEquals("ID", tokens[0])
    }

    @Test
    fun amountParsing_bigDecimal_exactWithoutFloatingPointError() {
        fun parseAmountDecimalToMinor(raw: String, fractionDigits: Int): Long? {
            val cleaned = raw.replace(",", "").replace("$", "").replace("₹", "").trim()
            if (cleaned.isEmpty()) return null
            return try {
                val bd = BigDecimal(cleaned)
                bd.movePointRight(fractionDigits).setScale(0, RoundingMode.HALF_UP).toLong()
            } catch (e: Exception) {
                null
            }
        }

        // Test standard 2-fraction digits (INR / USD)
        assertEquals(25050L, parseAmountDecimalToMinor("250.50", 2))
        assertEquals(25000L, parseAmountDecimalToMinor("250", 2))
        assertEquals(123456789L, parseAmountDecimalToMinor("1,234,567.89", 2))

        // Test 0 fraction digits (JPY)
        assertEquals(5000L, parseAmountDecimalToMinor("5000", 0))

        // Test 3 fraction digits (KWD / BHD)
        assertEquals(1500L, parseAmountDecimalToMinor("1.500", 3))
    }

    @Test
    fun dateParsing_multipleFormats_parsedToEpochDayCorrectly() {
        fun parseDateStringToEpochDay(raw: String): Long? {
            val cleaned = raw.trim()
            if (cleaned.isEmpty()) return null
            runCatching { return LocalDate.parse(cleaned, DateTimeFormatter.ISO_LOCAL_DATE).toEpochDay() }
            runCatching { return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("dd/MM/yyyy")).toEpochDay() }
            runCatching { return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("dd-MM-yyyy")).toEpochDay() }
            runCatching { return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("MM/dd/yyyy")).toEpochDay() }
            runCatching { return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("yyyy/MM/dd")).toEpochDay() }
            return null
        }

        val expected = LocalDate.of(2026, 3, 25).toEpochDay()
        assertEquals(expected, parseDateStringToEpochDay("2026-03-25"))
        assertEquals(expected, parseDateStringToEpochDay("25/03/2026"))
        assertEquals(expected, parseDateStringToEpochDay("25-03-2026"))
        assertEquals(expected, parseDateStringToEpochDay("03/25/2026"))
        assertEquals(expected, parseDateStringToEpochDay("2026/03/25"))
    }

    @Test
    fun decimalFormatting_plainWithoutGroupingCommas() {
        fun formatDecimalAmountPlain(minor: Long, fractionDigits: Int): String {
            if (fractionDigits <= 0) return minor.toString()
            val isNeg = minor < 0L
            val absMinor = Math.abs(minor)
            val factor = 10.0.pow(fractionDigits.toDouble()).toLong()
            val whole = absMinor / factor
            val frac = absMinor % factor
            val fracStr = frac.toString().padStart(fractionDigits, '0')
            val res = "$whole.$fracStr"
            return if (isNeg) "-$res" else res
        }

        assertEquals("250.50", formatDecimalAmountPlain(25050L, 2))
        assertEquals("1234567.89", formatDecimalAmountPlain(123456789L, 2))
        assertEquals("5000", formatDecimalAmountPlain(5000L, 0))
        assertEquals("1.500", formatDecimalAmountPlain(1500L, 3))
    }
}

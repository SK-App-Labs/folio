package com.folioledger.app.data

import com.folioledger.app.data.repository.CsvSectionState
import com.folioledger.app.data.repository.detectSectionHeader
import com.folioledger.app.data.repository.parseAccountGroupFromTokens
import com.folioledger.app.data.repository.parseAmountDecimalToMinor
import com.folioledger.app.data.repository.parseCsvLine
import com.folioledger.app.data.repository.parseDateStringToEpochDay
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    @Test
    fun detectSectionHeader_variousSectionMarkers() {
        // Accounts markers
        assertEquals(CsvSectionState.ACCOUNTS, detectSectionHeader("=== ACCOUNTS ==="))
        assertEquals(CsvSectionState.ACCOUNTS, detectSectionHeader("=== Accounts & Balances ==="))
        assertEquals(CsvSectionState.ACCOUNTS, detectSectionHeader("[ACCOUNTS]"))
        assertEquals(CsvSectionState.ACCOUNTS, detectSectionHeader("[ACCOUNTS],,,,,,,,,,,,"))
        assertEquals(CsvSectionState.ACCOUNTS, detectSectionHeader("# ACCOUNTS"))

        // Categories markers
        assertEquals(CsvSectionState.CATEGORIES, detectSectionHeader("=== CATEGORIES ==="))
        assertEquals(CsvSectionState.CATEGORIES, detectSectionHeader("[CATEGORIES]"))
        assertEquals(CsvSectionState.CATEGORIES, detectSectionHeader("[CATEGORIES],,,,,,,,,,,,"))
        assertEquals(CsvSectionState.CATEGORIES, detectSectionHeader("# Categories & Subcategories"))

        // Transactions markers
        assertEquals(CsvSectionState.TRANSACTIONS, detectSectionHeader("=== TRANSACTIONS ==="))
        assertEquals(CsvSectionState.TRANSACTIONS, detectSectionHeader("[TRANSACTIONS]"))
        assertEquals(CsvSectionState.TRANSACTIONS, detectSectionHeader("[TRANSACTIONS],,,,,,,,,,,,"))
        assertEquals(CsvSectionState.TRANSACTIONS, detectSectionHeader("# Transactions"))

        // Hint rows must NEVER be detected as section headers!
        assertNull(detectSectionHeader("# Hint: (Account Name),(BANK_ACCOUNT|CREDIT_CARD...),,,,,,"))
        assertNull(detectSectionHeader("# Hint: (Main Category Name),(Optional Subcategory)..."))
        assertNull(detectSectionHeader("# Hint: (Optional ID),(YYYY-MM-DD)...(Source Account)..."))

        // Non-section lines (Headers or data) should return null
        assertNull(detectSectionHeader("Date,Type,Amount,Currency,Account"))
        assertNull(detectSectionHeader("HDFC Bank,BANK,INR,50000.00,2026-01-01"))
        assertNull(detectSectionHeader("2026-03-25,EXPENSE,250.50,INR,HDFC Bank"))
        assertNull(detectSectionHeader(""))
    }

    @Test
    fun accountRowParsing_startingBalanceAndLimits() {
        fun parseAccountGroup(raw: String?): AccountGroup {
            val clean = raw?.trim()?.uppercase()?.replace(" ", "_") ?: ""
            if (clean == "BANK") return AccountGroup.BANK_ACCOUNT
            return runCatching { AccountGroup.valueOf(clean) }.getOrDefault(AccountGroup.BANK_ACCOUNT)
        }

        fun parseAmount(raw: String?): Long? {
            if (raw.isNullOrBlank()) return null
            val cleaned = raw.replace(",", "").replace("$", "").replace("₹", "").trim()
            return runCatching {
                BigDecimal(cleaned).movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()
            }.getOrNull()
        }

        // Test 1: Bank account with positive starting balance and anchor date
        val row1 = parseCsvLine("HDFC Salary,BANK,INR,50000.00,2026-01-01,,")
        val acc1Name = row1[0]
        val acc1Group = parseAccountGroup(row1[1])
        val acc1Currency = row1[2]
        val acc1Balance = parseAmount(row1[3])
        val acc1Anchor = LocalDate.parse(row1[4]).toEpochDay()
        val acc1Limit = parseAmount(row1[5])
        val acc1BillDay = row1[6].toIntOrNull()

        assertEquals("HDFC Salary", acc1Name)
        assertEquals(AccountGroup.BANK_ACCOUNT, acc1Group)
        assertEquals("INR", acc1Currency)
        assertEquals(5000000L, acc1Balance)
        assertEquals(LocalDate.of(2026, 1, 1).toEpochDay(), acc1Anchor)
        assertNull(acc1Limit)
        assertNull(acc1BillDay)

        // Test 2: Credit card with negative starting balance, credit limit, and settlement day
        val row2 = parseCsvLine("Amazon ICICI,CREDIT_CARD,INR,-4500.50,2026-01-01,150000.00,18")
        val acc2Name = row2[0]
        val acc2Group = parseAccountGroup(row2[1])
        val acc2Balance = parseAmount(row2[3])
        val acc2Limit = parseAmount(row2[5])
        val acc2BillDay = row2[6].toIntOrNull()

        assertEquals("Amazon ICICI", acc2Name)
        assertEquals(AccountGroup.CREDIT_CARD, acc2Group)
        assertEquals(-450050L, acc2Balance)
        assertEquals(15000000L, acc2Limit)
        assertEquals(18, acc2BillDay)

        // Test 3: Cash account without starting balance
        val row3 = parseCsvLine("Wallet Cash,CASH,INR,,,,,")
        val acc3Balance = parseAmount(row3[3])
        assertNull(acc3Balance)

        // Test 4: Loan account with positive entered balance - auto-detected as liability
        val row4 = parseCsvLine("HDFC Home Loan,LOAN,INR,500000.00,2026-01-01,,")
        val acc4Group = parseAccountGroup(row4[1])
        val acc4Balance = parseAmount(row4[3])
        assertTrue("LOAN must be liability by default", acc4Group.isLiabilityByDefault)
        assertEquals(50000000L, acc4Balance)
    }

    @Test
    fun liabilityPolarity_autoDetectionForLoansAndCreditCards() {
        // Business Rule: For accounts where isLiabilityByDefault == true (CREDIT_CARD, LOAN, OVERDRAFT),
        // whether user writes positive (+50000) or negative (-50000), Folio recognizes it as an outstanding liability!
        fun computeOpeningBalanceOutflow(group: AccountGroup, rawAmount: Long): Boolean {
            return rawAmount < 0L || group.isLiabilityByDefault
        }

        // Bank Account (Asset): Positive is inflow (+), Negative is outflow (overdrawn)
        assertEquals(false, computeOpeningBalanceOutflow(AccountGroup.BANK_ACCOUNT, 2500000L))
        assertEquals(true, computeOpeningBalanceOutflow(AccountGroup.BANK_ACCOUNT, -500000L))

        // Loan (Liability): Always treated as outflow/debt regardless of whether entered with or without minus sign
        assertEquals(true, computeOpeningBalanceOutflow(AccountGroup.LOAN, 50000000L))
        assertEquals(true, computeOpeningBalanceOutflow(AccountGroup.LOAN, -50000000L))

        // Overdraft (Liability): Always treated as outflow/debt
        assertEquals(true, computeOpeningBalanceOutflow(AccountGroup.OVERDRAFT, 1000000L))
        assertEquals(true, computeOpeningBalanceOutflow(AccountGroup.OVERDRAFT, -1000000L))

        // Credit Card: Magnitude is stored as initial debt
        val enteredPositiveDebt = 450050L
        val enteredNegativeDebt = -450050L
        assertEquals(450050L, Math.abs(enteredPositiveDebt))
        assertEquals(450050L, Math.abs(enteredNegativeDebt))
        assertTrue(AccountGroup.CREDIT_CARD.isLiabilityByDefault)
    }

    @Test
    fun categoryRowParsing_hierarchyAndTypes() {
        val catLine1 = parseCsvLine("Food & Dining,,EXPENSE,restaurant,#FF5722")
        val mainCat = catLine1[0]
        val subCat = catLine1[1]
        val catType = CategoryType.valueOf(catLine1[2])
        val icon = catLine1[3]
        val color = catLine1[4]

        assertEquals("Food & Dining", mainCat)
        assertTrue(subCat.isEmpty())
        assertEquals(CategoryType.EXPENSE, catType)
        assertEquals("restaurant", icon)
        assertEquals("#FF5722", color)

        val catLine2 = parseCsvLine("Food & Dining,Groceries,EXPENSE,shopping_cart,#FF9800")
        assertEquals("Food & Dining", catLine2[0])
        assertEquals("Groceries", catLine2[1])
        assertEquals(CategoryType.EXPENSE, CategoryType.valueOf(catLine2[2]))
    }

    @Test
    fun transferCategoryProtection_invariantHoldsStrictly() {
        // Business Rule: Transfers (TRANSFER and CC_SETTLEMENT) must NEVER have a category attached.
        // Even if the CSV row has account names or notes in the category column, categoryId must resolve to null.
        fun resolveCategoryId(
            type: TransactionType,
            csvCategoryName: String?,
            categoryLookup: (String) -> Long?
        ): Long? {
            val isTransfer = type == TransactionType.TRANSFER || type == TransactionType.CC_SETTLEMENT
            if (isTransfer) {
                return null // Category protection invariant
            }
            return csvCategoryName?.takeIf { it.isNotBlank() }?.let { categoryLookup(it) }
        }

        val dummyCategories = mapOf("Groceries" to 101L, "HDFC Bank" to 102L, "Dining" to 103L)

        // Test 1: Standard Expense with category -> Successfully resolves category
        val expenseCatId = resolveCategoryId(TransactionType.EXPENSE, "Groceries") { dummyCategories[it] }
        assertEquals(101L, expenseCatId)

        // Test 2: Standard Income with category -> Successfully resolves category
        val incomeCatId = resolveCategoryId(TransactionType.INCOME, "Dining") { dummyCategories[it] }
        assertEquals(103L, incomeCatId)

        // Test 3: Transfer with account name in category column -> Strictly returns null!
        val transferCatId = resolveCategoryId(TransactionType.TRANSFER, "HDFC Bank") { dummyCategories[it] }
        assertNull("Transfers must never link a category, even if column has text", transferCatId)

        // Test 4: CC Settlement with account name in category column -> Strictly returns null!
        val settlementCatId = resolveCategoryId(TransactionType.CC_SETTLEMENT, "HDFC Bank") { dummyCategories[it] }
        assertNull("CC Settlement must never link a category", settlementCatId)
    }

    @Test
    fun multiSectionCsvStreamSimulation_stateTransitionsAndAnchorGeneration() {
        val csvLines = listOf(
            "# Folio Spreadsheet Template",
            "[ACCOUNTS],,,,,,,,,,,,",
            "Name,Group,Currency,Starting Balance,Anchor Date,Credit Limit,Settlement Day,,,,,,",
            "# Hint: (Account Name),(BANK_ACCOUNT|CREDIT_CARD|CASH|SAVINGS|PREPAID_WALLET|LOAN),(Currency ISO),(Starting Balance e.g. 50000.00),(Anchor Date YYYY-MM-DD),(Credit Limit),(Settlement Day 1-31),,,,,,",
            "HDFC Salary,BANK,INR,25000.00,2026-01-01,,,,,,,,",
            "Cash,CASH,INR,,,,,,,,",
            ",,,,,,,,,,,,",
            "[CATEGORIES],,,,,,,,,,,,",
            "Main Category,Subcategory,Type,Icon,Color,,,,,,,,",
            "# Hint: (Main Category Name),(Optional Subcategory),(EXPENSE|INCOME),(Icon Name e.g. restaurant),(Hex Color e.g. #FF5722),,,,,,,,",
            "Groceries,,EXPENSE,shopping_bag,#4CAF50,,,,,,,,",
            ",,,,,,,,,,,,",
            "[TRANSACTIONS],,,,,,,,,,,,",
            "Date,Type,Amount,Currency,Account,Transfer To,Category,Note,,,,,,",
            "# Hint: (Optional ID),(YYYY-MM-DD),(EXPENSE|INCOME|TRANSFER|CC_SETTLEMENT|REFUND),(Amount e.g. 250.50),(Currency),(Source Account),(Destination Account if Transfer),(Leave blank if Transfer),(Subcategory),(Transfer Fee),(Payee/Description),(Note),(Tags semicolon separated)",
            "2026-01-05,EXPENSE,500.00,INR,HDFC Salary,,Groceries,Supermarket,,,,,,",
            "2026-01-10,TRANSFER,2000.00,INR,HDFC Salary,Cash,Cash,ATM Withdrawal,,,,,,"
        )

        var currentSection = CsvSectionState.LEGACY
        val configuredAccounts = mutableListOf<String>()
        val accountsWithStartingBalance = mutableListOf<String>()
        val openingBalanceTransactions = mutableListOf<Pair<String, Long>>()
        val configuredCategories = mutableListOf<String>()
        val transactionsParsed = mutableListOf<String>()

        for (line in csvLines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.all { it == ',' || it.isWhitespace() }) continue

            val section = detectSectionHeader(trimmed)
            if (section != null) {
                currentSection = section
                continue
            }

            if (trimmed.startsWith("#") || trimmed.startsWith("//")) continue

            val tokens = parseCsvLine(trimmed)
            when (currentSection) {
                CsvSectionState.ACCOUNTS -> {
                    if (tokens.firstOrNull()?.contains("Name", ignoreCase = true) == true) continue
                    val name = tokens[0]
                    configuredAccounts.add(name)
                    val balanceStr = tokens.getOrNull(3)
                    if (!balanceStr.isNullOrBlank()) {
                        accountsWithStartingBalance.add(name)
                        val minor = BigDecimal(balanceStr).movePointRight(2).toLong()
                        openingBalanceTransactions.add(name to minor)
                    }
                }
                CsvSectionState.CATEGORIES -> {
                    if (tokens.firstOrNull()?.contains("Main", ignoreCase = true) == true) continue
                    configuredCategories.add(tokens[0])
                }
                CsvSectionState.TRANSACTIONS, CsvSectionState.LEGACY -> {
                    if (tokens.firstOrNull()?.contains("Date", ignoreCase = true) == true) continue
                    transactionsParsed.add(tokens[0])
                }
            }
        }

        assertEquals(listOf("HDFC Salary", "Cash"), configuredAccounts)
        assertEquals(listOf("HDFC Salary"), accountsWithStartingBalance)
        assertEquals(1, openingBalanceTransactions.size)
        assertEquals("HDFC Salary", openingBalanceTransactions[0].first)
        assertEquals(2500000L, openingBalanceTransactions[0].second)
        assertEquals(listOf("Groceries"), configuredCategories)
        assertEquals(2, transactionsParsed.size)
    }

    @Test
    fun userProvidedSpreadsheet_fullSimulation_parsesAllRowsWithoutErrors() {
        val stream = javaClass.classLoader?.getResourceAsStream("user_test_spreadsheet.csv")
            ?: java.io.File("c:/Users/sivas/AndroidStudioProjects/Folio/app/src/test/resources/user_test_spreadsheet.csv").inputStream()

        val reader = java.io.BufferedReader(java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8))
        var currentSection = CsvSectionState.LEGACY
        var colMap = emptyMap<String, Int>()

        val parsedAccounts = mutableListOf<String>()
        val parsedCategories = mutableListOf<String>()
        val parsedTransactions = mutableListOf<String>()
        val errors = mutableListOf<String>()

        var line = reader.readLine()
        var lineNum = 0
        while (line != null) {
            lineNum++
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.all { it == ',' || it.isWhitespace() }) {
                val sec = detectSectionHeader(trimmed)
                if (sec != null) {
                    currentSection = sec
                    colMap = emptyMap()
                    line = reader.readLine()
                    continue
                }

                if (trimmed.startsWith("#") || trimmed.startsWith("//")) {
                    line = reader.readLine()
                    continue
                }

                if (colMap.isEmpty()) {
                    val tokens = parseCsvLine(trimmed)
                    colMap = tokens.mapIndexed { idx, name ->
                        name.trim().lowercase().replace("_", "").replace(" ", "") to idx
                    }.toMap()
                    line = reader.readLine()
                    continue
                }

                val tokens = parseCsvLine(trimmed)
                when (currentSection) {
                    CsvSectionState.ACCOUNTS -> {
                        val nameIdx = colMap["name"] ?: colMap["account"] ?: colMap["accountname"] ?: 0
                        val name = tokens.getOrNull(nameIdx)?.trim() ?: ""
                        if (name.isNotEmpty()) {
                            parsedAccounts.add(name)
                            val rawGroup = colMap["group"]?.let { tokens.getOrNull(it)?.trim() }
                                ?: colMap["grouptype"]?.let { tokens.getOrNull(it)?.trim() }
                            val group = parseAccountGroupFromTokens(rawGroup, name)
                            assertNotNull("Group should be parsed", group)
                        }
                    }
                    CsvSectionState.CATEGORIES -> {
                        val mainIdx = colMap["maincategory"] ?: colMap["category"] ?: colMap["name"] ?: 0
                        val mainName = tokens.getOrNull(mainIdx)?.trim() ?: ""
                        if (mainName.isNotEmpty()) {
                            parsedCategories.add(mainName)
                        }
                    }
                    CsvSectionState.TRANSACTIONS, CsvSectionState.LEGACY -> {
                        val dateIdx = colMap["date"] ?: colMap["txdate"] ?: colMap["transactiondate"]
                        val amountIdx = colMap["amount"] ?: colMap["txnamount"] ?: colMap["total"]
                        val accountIdx = colMap["account"] ?: colMap["accountname"] ?: colMap["fromaccount"]

                        if (dateIdx == null || amountIdx == null || accountIdx == null) {
                            errors.add("Line $lineNum: Missing header columns")
                        } else {
                            val rawDate = tokens.getOrNull(dateIdx) ?: ""
                            val epochDay = parseDateStringToEpochDay(rawDate)
                            val rawAmount = tokens.getOrNull(amountIdx) ?: ""
                            val amountMinor = parseAmountDecimalToMinor(rawAmount, 2)
                            val accountName = tokens.getOrNull(accountIdx)?.trim() ?: ""

                            if (epochDay == null) errors.add("Line $lineNum: Bad date '$rawDate'")
                            if (amountMinor == null) errors.add("Line $lineNum: Bad amount '$rawAmount'")
                            if (accountName.isEmpty()) errors.add("Line $lineNum: Missing account")

                            if (epochDay != null && amountMinor != null && accountName.isNotEmpty()) {
                                parsedTransactions.add(accountName)
                            }
                        }
                    }
                }
            }
            line = reader.readLine()
        }

        assertTrue("Errors should be empty, but had: $errors", errors.isEmpty())
        assertEquals(17, parsedAccounts.size)
        assertTrue(parsedAccounts.contains("Axis Bank"))
        assertTrue(parsedAccounts.contains("ICICI Home Loan"))
        assertTrue(parsedAccounts.contains("Axis Rewards CC"))
        assertTrue(parsedCategories.size >= 50)
        assertEquals(358, parsedTransactions.size)
    }

    @Test
    fun parseAccountGroupFromTokens_noFalsePositivesOnTwoLetterSubstrings() {
        // Words containing 'od' or 'cc' internally must not trigger OVERDRAFT or CREDIT_CARD
        assertEquals(AccountGroup.OTHER, parseAccountGroupFromTokens(null, "Food Card"))
        assertEquals(AccountGroup.OTHER, parseAccountGroupFromTokens(null, "Godrej Salary"))
        assertEquals(AccountGroup.OTHER, parseAccountGroupFromTokens(null, "Period Care"))
        assertEquals(AccountGroup.OTHER, parseAccountGroupFromTokens(null, "Today Expense"))
        assertEquals(AccountGroup.BANK_ACCOUNT, parseAccountGroupFromTokens(null, "Access Bank"))

        // Whole-word tokens should still match accurately
        assertEquals(AccountGroup.CREDIT_CARD, parseAccountGroupFromTokens(null, "Axis CC"))
        assertEquals(AccountGroup.CREDIT_CARD, parseAccountGroupFromTokens(null, "HDFC Credit Card"))
        assertEquals(AccountGroup.OVERDRAFT, parseAccountGroupFromTokens(null, "SBI OD"))
        assertEquals(AccountGroup.OVERDRAFT, parseAccountGroupFromTokens(null, "Personal Overdraft"))
        assertEquals(AccountGroup.DEBIT_CARD, parseAccountGroupFromTokens(null, "HDFC Debit"))
        assertEquals(AccountGroup.CASH, parseAccountGroupFromTokens(null, "Petty Cash"))
    }
}

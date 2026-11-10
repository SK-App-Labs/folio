package com.folioledger.app.domain.parser

import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.TransactionType
import java.time.LocalDate
import java.time.Month
import java.util.Locale

/**
 * 100% on-device regular expression and token intelligence engine for extracting
 * financial transaction entities from raw SMS, payment notification, or clipboard text.
 *
 * Designed with zero external dependencies, zero network calls, and deterministic accuracy.
 */
object SmartTextParserEngine {

    // Common bank noise markers: balance statements, available credit limits, OTP disclaimers.
    private val BALANCE_NOISE_REGEX = Regex(
        "(?i)(?:avl(?:\\.|ailable)?\\s*(?:bal(?:ance)?|lmt|limit)|total\\s*bal(?:ance)?|bal(?:\\.|ance)?\\s*[:\\s\\-]|clear\\s*bal|remaining\\s*limit|credit\\s*limit)\\s*[:\\s\\-]*([A-Za-z$₹£€]+\\.?\\s*[0-9,]+(?:\\.[0-9]{1,2})?)",
        RegexOption.IGNORE_CASE
    )

    // Common keywords indicating financial context when parsing casual decimal numbers without currency
    private val FINANCIAL_CONTEXT_KEYWORDS = listOf(
        "spent", "paid", "lunch", "dinner", "coffee", "breakfast", "uber", "ola", "lyft",
        "groceries", "grocery", "bill", "ticket", "store", "bought", "purchase", "cost",
        "food", "recharge", "fee", "tax", "rent", "petrol", "fuel", "gas", "fare", "tip",
        "order", "swiggy", "zomato", "amazon", "flipkart", "subscription"
    )

    // Primary action verbs associated with financial debits/credits
    private val VERB_AMOUNT_REGEX = Regex(
        "(?i)(?:debited(?:\\s+for|\\s+with|\\s+by)?|spent|paid|purchase(?:\\s+of)?|charge(?:\\s+of)?|charged|sent|transfer(?:red)?(?:\\s+of)?|credited(?:\\s+with|\\s+for)?|received|refund(?:\\s+of)?|salary(?:\\s+of)?)\\s*(?:(?:INR|Rs\\.?|₹|\\$|USD|€|EUR|£|GBP)\\s*)?([0-9,]+(?:\\.[0-9]{1,2})?)",
        RegexOption.IGNORE_CASE
    )

    // Multi-currency symbols and codes
    private val CURRENCY_AMOUNT_REGEX = Regex(
        "(?i)(?:Rs\\.?|INR|₹|\\$|USD|€|EUR|£|GBP|AED|CAD|AUD|SGD)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
        RegexOption.IGNORE_CASE
    )

    // Number with standard currency decimals (casual fallback)
    private val CASUAL_AMOUNT_REGEX = Regex(
        "\\b([0-9]{1,3}(?:,[0-9]{3})*\\.[0-9]{2}|[0-9]+\\.[0-9]{2})\\b"
    )

    // Card or Account suffix hints
    private val ACCOUNT_SUFFIX_REGEX = Regex(
        "(?i)(?:a/c|acct|account|card)\\s*(?:no\\.?|ending(?:\\s+in|\\s+with)?|xx|\\*+|\\.{3,})?[\\s\\-]*([0-9]{3,5})\\b|\\b(?:ending|ending with|ending in)[\\s\\-]*([0-9]{3,5})\\b|(?:\\*+|x+|\\.{3,}|-)\\s*([0-9]{3,5})\\b",
        RegexOption.IGNORE_CASE
    )

    // Known financial institutions
    private val KNOWN_BANKS = listOf(
        "HDFC" to "HDFC Bank",
        "ICICI" to "ICICI Bank",
        "SBI" to "State Bank of India",
        "STATE BANK" to "State Bank of India",
        "AXIS" to "Axis Bank",
        "KOTAK" to "Kotak Mahindra Bank",
        "INDUSIND" to "IndusInd Bank",
        "PUNJAB NATIONAL" to "PNB",
        "PNB" to "PNB",
        "BANK OF BARODA" to "Bank of Baroda",
        "CANARA" to "Canara Bank",
        "FEDERAL BANK" to "Federal Bank",
        "IDFC" to "IDFC FIRST Bank",
        "YES BANK" to "Yes Bank",
        "CHASE" to "Chase",
        "JPMORGAN" to "Chase",
        "AMEX" to "American Express",
        "AMERICAN EXPRESS" to "American Express",
        "CITI" to "Citibank",
        "CITIBANK" to "Citibank",
        "WELLS FARGO" to "Wells Fargo",
        "BANK OF AMERICA" to "Bank of America",
        "BOA" to "Bank of America",
        "CAPITAL ONE" to "Capital One",
        "DISCOVER" to "Discover",
        "BARCLAYS" to "Barclays",
        "HSBC" to "HSBC",
        "MONZO" to "Monzo",
        "REVOLUT" to "Revolut",
        "LLOYDS" to "Lloyds Bank",
        "NATWEST" to "NatWest",
        "SANTANDER" to "Santander"
    )

    // UPI Merchant extraction: e.g. UPI/ref 43152678/STARBUCKS or UPI Ref 12345 to Vendor
    private val UPI_PAYEE_REGEX = Regex(
        "(?i)UPI(?:/ref|\\s*ref)?\\s*[:\\s/]*[0-9A-Za-z]+\\s*/\\s*([A-Za-z0-9\\s&'\\.\\-_]+?)(?=\\s*(?:\\.|on|via|ref|avl|bal|\\n|$))",
        RegexOption.IGNORE_CASE
    )

    // Common preposition anchors: e.g. "at STARBUCKS on", "to Swiggy", "by ACME TECH", "towards Electricity"
    private val PREPOSITION_PAYEE_REGEX = Regex(
        "(?i)\\b(?:to|at|towards|by)\\s+([A-Za-z0-9&'\\.\\-_]+(?:\\s+[A-Za-z0-9&'\\.\\-_]+)*?)(?=\\s+(?:on|via|ref|upi|avl|bal|using|with|from|card|acct|a/c|ending|\\.|\\n|$))",
        RegexOption.IGNORE_CASE
    )

    // Date Pattern 1: e.g. 10-Nov-24, 09/11/2026, 15-11-2024
    private val DATE_PATTERN_1 = Regex(
        "\\b(\\d{1,2})[-/]([A-Za-z]{3}|\\d{1,2})[-/](\\d{2,4})\\b"
    )

    // Date Pattern 2: e.g. Nov 10, Nov 10, 2026
    private val DATE_PATTERN_2 = Regex(
        "(?i)\\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(\\d{1,2})(?:st|nd|rd|th)?(?:,?\\s*(\\d{2,4}))?\\b",
        RegexOption.IGNORE_CASE
    )

    /**
     * Parses raw financial text into an extracted [ParsedTransactionResult].
     */
    fun parse(rawText: String): ParsedTransactionResult {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) {
            return ParsedTransactionResult(rawText = rawText)
        }

        // Step 1: Detect and isolate balance noise so we don't accidentally treat account balance as spend
        var discardedBalanceSnippet: String? = null
        var textWithoutBalance = trimmed
        val balanceMatch = BALANCE_NOISE_REGEX.find(trimmed)
        if (balanceMatch != null) {
            discardedBalanceSnippet = balanceMatch.value.trim()
            // Replace the balance region with spaces so indices remain stable
            textWithoutBalance = trimmed.substring(0, balanceMatch.range.first) +
                    " ".repeat(balanceMatch.range.last - balanceMatch.range.first + 1) +
                    trimmed.substring(balanceMatch.range.last + 1)
        }

        // Step 2: Extract Amount & Currency
        val (amountMinor, rawAmountString, currencyCode) = extractAmountAndCurrency(textWithoutBalance)

        // Step 3: Classify Direction / TransactionType
        val (type, typeTriggerWord) = classifyDirection(trimmed)

        // Step 4: Extract Account & Bank hints
        val accountSuffix = extractAccountSuffix(trimmed)
        val bankInstitution = extractBankInstitution(trimmed)

        // Step 5: Extract Payee / Merchant
        val (payeeOrDescription, rawVendorSnippet) = extractPayee(trimmed)

        // Step 6: Extract Date
        val dateEpochDay = extractDateEpochDay(trimmed)

        // Step 7: Calculate Confidence Score
        var confidence = 0f
        if (amountMinor != null && amountMinor.isPositive) confidence += 0.40f
        if (payeeOrDescription.isNotBlank()) confidence += 0.25f
        if (accountSuffix != null || bankInstitution != null) confidence += 0.20f
        if (dateEpochDay != null) confidence += 0.10f
        if (typeTriggerWord != null) confidence += 0.05f

        return ParsedTransactionResult(
            rawText = rawText,
            amountMinor = amountMinor,
            rawAmountString = rawAmountString,
            currencyCode = currencyCode,
            type = type,
            typeTriggerWord = typeTriggerWord,
            payeeOrDescription = payeeOrDescription,
            rawVendorSnippet = rawVendorSnippet,
            accountSuffix = accountSuffix,
            bankInstitution = bankInstitution,
            dateEpochDay = dateEpochDay,
            discardedBalanceSnippet = discardedBalanceSnippet,
            confidence = confidence.coerceIn(0f, 1f)
        )
    }

    private fun extractAmountAndCurrency(text: String): Triple<MinorMoney?, String?, String?> {
        // Priority 1: Verb-tethered amount (e.g. "debited for Rs.450.00" or "spent INR 3,499.00")
        val verbMatch = VERB_AMOUNT_REGEX.find(text)
        if (verbMatch != null && verbMatch.groupValues.size >= 2) {
            val amountStr = verbMatch.groupValues[1]
            val minor = parseMinorMoney(amountStr)
            if (minor != null) {
                val fullMatchedSnippet = verbMatch.value
                val currency = detectCurrencyCode(fullMatchedSnippet)
                return Triple(minor, formatRawAmountString(currency, amountStr), currency)
            }
        }

        // Priority 2: Currency-prefixed amount (e.g. "Rs. 1,250", "$42.50", "€25.00")
        val currencyMatch = CURRENCY_AMOUNT_REGEX.find(text)
        if (currencyMatch != null && currencyMatch.groupValues.size >= 2) {
            val amountStr = currencyMatch.groupValues[1]
            val minor = parseMinorMoney(amountStr)
            if (minor != null) {
                val currency = detectCurrencyCode(currencyMatch.value)
                return Triple(minor, formatRawAmountString(currency, amountStr), currency)
            }
        }

        // Priority 3: Standalone 2-decimal number (casual plain text e.g. "Dinner 65.00 with Sarah")
        val casualMatch = CASUAL_AMOUNT_REGEX.find(text)
        if (casualMatch != null && casualMatch.groupValues.size >= 2) {
            val amountStr = casualMatch.groupValues[1]
            // Require at least one financial context keyword so non-financial text like "Meeting at 12.30" is ignored
            if (hasFinancialContext(text)) {
                val minor = parseMinorMoney(amountStr)
                if (minor != null) {
                    return Triple(minor, amountStr, null)
                }
            }
        }

        return Triple(null, null, null)
    }

    private fun hasFinancialContext(text: String): Boolean {
        val lower = text.lowercase(Locale.US)
        return FINANCIAL_CONTEXT_KEYWORDS.any { lower.contains(it) }
    }

    private fun detectCurrencyCode(snippet: String): String {
        val upper = snippet.uppercase(Locale.US)
        return when {
            upper.contains("RS") || upper.contains("INR") || upper.contains("₹") -> "INR"
            upper.contains("USD") || upper.contains("$") -> "USD"
            upper.contains("EUR") || upper.contains("€") -> "EUR"
            upper.contains("GBP") || upper.contains("£") -> "GBP"
            upper.contains("AED") -> "AED"
            upper.contains("CAD") -> "CAD"
            upper.contains("AUD") -> "AUD"
            upper.contains("SGD") -> "SGD"
            else -> "INR"
        }
    }

    private fun formatRawAmountString(currency: String?, amountStr: String): String {
        val sym = when (currency) {
            "INR" -> "₹"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            else -> ""
        }
        return if (sym.isNotEmpty()) "$sym$amountStr" else amountStr
    }

    private fun parseMinorMoney(amountString: String): MinorMoney? {
        return try {
            val clean = amountString.replace(",", "").trim()
            if (clean.isEmpty()) return null
            if (clean.contains(".")) {
                val parts = clean.split(".")
                val major = parts[0].toLongOrNull() ?: 0L
                val minorStr = parts.getOrNull(1)?.padEnd(2, '0')?.take(2) ?: "00"
                val minor = minorStr.toLongOrNull() ?: 0L
                MinorMoney(major * 100L + minor)
            } else {
                val major = clean.toLongOrNull() ?: return null
                MinorMoney(major * 100L)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun classifyDirection(text: String): Pair<TransactionType, String?> {
        val lower = text.lowercase(Locale.US)

        // Income patterns
        val incomeKeywords = listOf(
            "credited to", "credited with", "credited", "refund of", "refund",
            "cashback", "salary of", "salary", "received from", "received", "deposited"
        )
        for (kw in incomeKeywords) {
            if (lower.contains(kw)) {
                return Pair(TransactionType.INCOME, kw)
            }
        }

        // Transfer patterns
        val transferKeywords = listOf(
            "transferred to self", "transfer to savings", "atm withdrawal", "atm wdl", "transferred between"
        )
        for (kw in transferKeywords) {
            if (lower.contains(kw)) {
                return Pair(TransactionType.TRANSFER, kw)
            }
        }

        // Expense patterns
        val expenseKeywords = listOf(
            "debited for", "debited from", "debited", "spent inr", "spent",
            "paid to", "paid", "purchase at", "purchase of", "purchase",
            "charged to", "charge of", "charged", "sent to", "sent", "used at"
        )
        for (kw in expenseKeywords) {
            if (lower.contains(kw)) {
                return Pair(TransactionType.EXPENSE, kw)
            }
        }

        return Pair(TransactionType.EXPENSE, null)
    }

    private fun extractAccountSuffix(text: String): String? {
        val match = ACCOUNT_SUFFIX_REGEX.find(text) ?: return null
        for (i in 1 until match.groupValues.size) {
            val candidate = match.groupValues[i]
            if (candidate.isNotBlank() && candidate.all { it.isDigit() }) {
                return candidate
            }
        }
        return null
    }

    private fun extractBankInstitution(text: String): String? {
        val upper = text.uppercase(Locale.US)
        for ((keyword, canonicalName) in KNOWN_BANKS) {
            if (upper.contains(keyword)) {
                return canonicalName
            }
        }
        return null
    }

    private fun extractPayee(text: String): Pair<String, String?> {
        // Priority 1: UPI Ref Vendor (e.g. "UPI/ref 43152678/STARBUCKS")
        val upiMatch = UPI_PAYEE_REGEX.find(text)
        if (upiMatch != null && upiMatch.groupValues.size >= 2) {
            val candidate = sanitizePayeeString(upiMatch.groupValues[1])
            if (isValidPayee(candidate)) {
                return Pair(candidate, upiMatch.value)
            }
        }

        // Priority 2: Preposition Vendor Anchor (e.g. "at ZARA MUMBAI", "to Pret A Manger", "by ACME TECH")
        val prepMatches = PREPOSITION_PAYEE_REGEX.findAll(text)
        for (m in prepMatches) {
            if (m.groupValues.size >= 2) {
                val candidate = sanitizePayeeString(m.groupValues[1])
                if (isValidPayee(candidate)) {
                    return Pair(candidate, m.value)
                }
            }
        }

        // Priority 3: Plain text starting words (e.g. "Dinner 65.00 with Sarah at Bella Italia")
        if (text.startsWith("Dinner", ignoreCase = true) || text.startsWith("Lunch", ignoreCase = true) ||
            text.startsWith("Coffee", ignoreCase = true) || text.startsWith("Uber", ignoreCase = true)
        ) {
            val words = text.split(" ")
            val firstWord = words.firstOrNull() ?: ""
            return Pair(cleanTitleCase(firstWord), null)
        }

        return Pair("", null)
    }

    private fun sanitizePayeeString(raw: String): String {
        var clean = raw.trim()
            .replace(Regex("(?i)\\b(?:pvt\\.?\\s*ltd\\.?|limited|inc\\.?|corp\\.?|llc\\.?)\\b"), "")
            .replace(Regex("(?i)\\b(?:bangalore|mumbai|delhi|hyderabad|chennai|pune|gurgaon|noida|kolkata)\\b"), "")
            .replace(Regex("[^a-zA-Z0-9\\s&'\\-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        return cleanTitleCase(clean)
    }

    private fun isValidPayee(candidate: String): Boolean {
        if (candidate.length < 2) return false
        val lower = candidate.lowercase(Locale.US)
        // Discard false positives like "your a c", "account", "credit card", "bank"
        val falsePositives = listOf("your a c", "your ac", "account", "card", "bank", "credit card", "debit card", "upi")
        return falsePositives.none { lower == it || lower.startsWith(it) }
    }

    private fun cleanTitleCase(text: String): String {
        return text.split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                if (word.length <= 1) word.uppercase(Locale.US)
                else word.substring(0, 1).uppercase(Locale.US) + word.substring(1).lowercase(Locale.US)
            }
    }

    private fun extractDateEpochDay(text: String): Long? {
        val now = LocalDate.now()

        // Match Pattern 1: e.g. 10-Nov-24, 09/11/2026, 15-11-2024
        val m1 = DATE_PATTERN_1.find(text)
        if (m1 != null && m1.groupValues.size >= 4) {
            val day = m1.groupValues[1].toIntOrNull() ?: return null
            val monthRaw = m1.groupValues[2]
            val yearRaw = m1.groupValues[3]

            val month = parseMonth(monthRaw) ?: return null
            val year = parseYear(yearRaw, now.year)

            return try {
                LocalDate.of(year, month, day).toEpochDay()
            } catch (_: Exception) {
                null
            }
        }

        // Match Pattern 2: e.g. Nov 10, Nov 10, 2026
        val m2 = DATE_PATTERN_2.find(text)
        if (m2 != null && m2.groupValues.size >= 3) {
            val monthStr = m2.groupValues[1]
            val day = m2.groupValues[2].toIntOrNull() ?: return null
            val yearRaw = m2.groupValues.getOrNull(3)?.ifBlank { null }

            val month = parseMonth(monthStr) ?: return null
            val year = if (yearRaw != null) parseYear(yearRaw, now.year) else now.year

            return try {
                LocalDate.of(year, month, day).toEpochDay()
            } catch (_: Exception) {
                null
            }
        }

        return null
    }

    private fun parseMonth(raw: String): Month? {
        val num = raw.toIntOrNull()
        if (num != null && num in 1..12) {
            return Month.of(num)
        }
        val prefix = raw.take(3).uppercase(Locale.US)
        return when (prefix) {
            "JAN" -> Month.JANUARY
            "FEB" -> Month.FEBRUARY
            "MAR" -> Month.MARCH
            "APR" -> Month.APRIL
            "MAY" -> Month.MAY
            "JUN" -> Month.JUNE
            "JUL" -> Month.JULY
            "AUG" -> Month.AUGUST
            "SEP" -> Month.SEPTEMBER
            "OCT" -> Month.OCTOBER
            "NOV" -> Month.NOVEMBER
            "DEC" -> Month.DECEMBER
            else -> null
        }
    }

    private fun parseYear(raw: String, fallbackYear: Int): Int {
        val num = raw.toIntOrNull() ?: return fallbackYear
        return when {
            num in 0..99 -> 2000 + num
            num in 1900..2100 -> num
            else -> fallbackYear
        }
    }
}

package com.folioledger.app.domain.parser

import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CategoryNode
import java.util.Locale

/**
 * Resolves raw parser extraction results against user's actual database entities:
 * - Maps account hints (card suffix / bank name) to existing accounts in Folio.
 * - Predicts category via historical payee association or semantic keyword dictionary.
 */
object ParsedTransactionResolver {

    private val SEMANTIC_CATEGORY_KEYWORDS: Map<String, List<String>> = mapOf(
        "coffee" to listOf("starbucks", "cafe", "coffee", "tea", "roasters", "blue tokai", "costa", "pret"),
        "food" to listOf("swiggy", "zomato", "restaurant", "pizza", "burger", "mcdonald", "diner", "bakery", "subway", "dominos", "kfc", "dining", "bistro", "taco"),
        "groceries" to listOf("groceries", "grocery", "supermarket", "mart", "whole foods", "zepto", "blinkit", "instamart", "bigbasket", "walmart", "costco", "trader joe", "safeway", "target", "provisions"),
        "fuel" to listOf("petrol", "fuel", "gas", "shell", "diesel", "indian oil", "bharat petroleum", "hpcl", "chevron", "exxon", "bp"),
        "transport" to listOf("uber", "ola", "lyft", "fastag", "toll", "metro", "irctc", "train", "railway", "flight", "airline", "indigo", "air india", "cab", "taxi", "parking"),
        "shopping" to listOf("amazon", "flipkart", "zara", "h&m", "myntra", "clothing", "apparel", "shoes", "nike", "adidas", "mall", "uniqlo", "retail", "fashion"),
        "utilities" to listOf("electricity", "bescom", "water", "wifi", "broadband", "airtel", "jio", "vodafone", "recharge", "bill", "utility", "gas bill", "power", "tneb"),
        "entertainment" to listOf("netflix", "spotify", "prime", "hotstar", "movie", "cinema", "pvr", "inox", "theatre", "youtube", "gaming", "steam", "playstation"),
        "health" to listOf("pharmacy", "apollo", "chemist", "medicine", "medical", "hospital", "clinic", "doctor", "lab", "diagnostics", "dental"),
        "salary" to listOf("salary", "payroll", "wages", "employer", "stipend", "bonus", "dividend", "interest credited")
    )

    fun resolve(
        parsed: ParsedTransactionResult,
        accounts: List<AccountSummary>,
        categories: List<CategoryNode>,
        historicalPayeeCategoryMap: Map<String, Long> = emptyMap()
    ): ResolvedTransactionFormValues {
        val matchedAccount = resolveAccount(parsed, accounts)
        val (matchedCat, parentName, matchSource) = resolveCategory(
            parsed = parsed,
            categories = categories,
            historicalPayeeCategoryMap = historicalPayeeCategoryMap
        )

        return ResolvedTransactionFormValues(
            parsedResult = parsed,
            matchedAccount = matchedAccount,
            matchedCategory = matchedCat,
            matchedParentCategoryName = parentName,
            categoryMatchSource = matchSource
        )
    }

    private fun resolveAccount(
        parsed: ParsedTransactionResult,
        accounts: List<AccountSummary>
    ): AccountSummary? {
        if (accounts.isEmpty()) return null

        val suffix = parsed.accountSuffix
        if (!suffix.isNullOrBlank()) {
            val byName = accounts.firstOrNull { acct ->
                acct.name.contains(suffix)
            }
            if (byName != null) return byName
        }

        // Match 3: Bank institution keyword
        val bank = parsed.bankInstitution
        if (!bank.isNullOrBlank()) {
            val bankLower = bank.lowercase(Locale.US)
            val byBank = accounts.firstOrNull { acct ->
                val nameLower = acct.name.lowercase(Locale.US)
                nameLower.contains(bankLower) || bankLower.contains(nameLower)
            }
            if (byBank != null) return byBank
        }

        // Fallback: First account
        return accounts.firstOrNull()
    }

    private fun resolveCategory(
        parsed: ParsedTransactionResult,
        categories: List<CategoryNode>,
        historicalPayeeCategoryMap: Map<String, Long>
    ): Triple<CategoryNode?, String?, CategoryMatchSource> {
        val payeeLower = parsed.payeeOrDescription.lowercase(Locale.US).trim()
        val textLower = parsed.rawText.lowercase(Locale.US)

        // Tier 1: Historical Payee Association
        if (payeeLower.isNotBlank() && historicalPayeeCategoryMap.isNotEmpty()) {
            val historicalCatId = historicalPayeeCategoryMap[payeeLower]
            if (historicalCatId != null) {
                val found = findCategoryById(categories, historicalCatId)
                if (found != null) {
                    return Triple(found.first, found.second, CategoryMatchSource.HISTORICAL)
                }
            }
        }

        // Tier 2: Semantic Keyword Matching
        for ((groupKey, keywords) in SEMANTIC_CATEGORY_KEYWORDS) {
            val matchesKeyword = keywords.any { kw ->
                payeeLower.contains(kw) || textLower.contains(kw)
            }
            if (matchesKeyword) {
                val matchedNode = findCategoryForSemanticGroup(categories, groupKey)
                if (matchedNode != null) {
                    return Triple(matchedNode.first, matchedNode.second, CategoryMatchSource.SEMANTIC_KEYWORD)
                }
            }
        }

        return Triple(null, null, CategoryMatchSource.NONE)
    }

    private fun findCategoryById(
        nodes: List<CategoryNode>,
        targetId: Long,
        parentName: String? = null
    ): Pair<CategoryNode, String?>? {
        for (node in nodes) {
            if (node.id == targetId) {
                return Pair(node, parentName)
            }
            if (node.subcategories.isNotEmpty()) {
                val foundInChild = findCategoryById(node.subcategories, targetId, node.name)
                if (foundInChild != null) return foundInChild
            }
        }
        return null
    }

    private fun findCategoryForSemanticGroup(
        nodes: List<CategoryNode>,
        groupKey: String,
        parentName: String? = null
    ): Pair<CategoryNode, String?>? {
        val keyLower = groupKey.lowercase(Locale.US)

        // First look for subcategory matching the group
        for (node in nodes) {
            for (sub in node.subcategories) {
                if (sub.name.lowercase(Locale.US).contains(keyLower)) {
                    return Pair(sub, node.name)
                }
            }
        }

        // Second look for top-level category matching the group
        for (node in nodes) {
            if (node.name.lowercase(Locale.US).contains(keyLower)) {
                return Pair(node, parentName)
            }
        }

        // Special aliases
        val alias = when (keyLower) {
            "coffee" -> "food"
            "fuel" -> "transport"
            "groceries" -> "food"
            else -> null
        }
        if (alias != null) {
            return findCategoryForSemanticGroup(nodes, alias, parentName)
        }

        return null
    }
}

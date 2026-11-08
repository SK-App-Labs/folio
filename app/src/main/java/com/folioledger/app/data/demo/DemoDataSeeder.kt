// File: app/src/main/java/com/folioledger/app/data/demo/DemoDataSeeder.kt
package com.folioledger.app.data.demo

import com.folioledger.app.data.local.FolioDatabase
import com.folioledger.app.data.local.entity.AccountEntity
import com.folioledger.app.data.local.entity.BookmarkEntity
import com.folioledger.app.data.local.entity.BudgetEntity
import com.folioledger.app.data.local.entity.RecurringRuleEntity
import com.folioledger.app.data.local.entity.TransactionEntity
import com.folioledger.app.data.local.entity.TransactionSplitEntity
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.json.JSONArray
import org.json.JSONObject

object DemoDataSeeder {

    fun scaleMinor(baseUsdMinor: Long, currencyCode: String): Long {
        return when (currencyCode.uppercase()) {
            "INR" -> baseUsdMinor * 20L
            "JPY", "KRW" -> baseUsdMinor * 100L
            "PHP", "THB", "MXN", "ZAR" -> baseUsdMinor * 15L
            else -> baseUsdMinor
        }
    }

    suspend fun seed(database: FolioDatabase, primaryCurrencyCode: String): String {
        val today = LocalDate.now()
        val accountDao = database.accountDao()
        val categoryDao = database.categoryAndTagDao()
        val transactionDao = database.transactionDao()
        val budgetDao = database.budgetAndRecurringDao()

        // 1. Resolve Category IDs dynamically
        val categories = categoryDao.getAllCategoriesOnce()
        val salaryCat = categories.firstOrNull { it.name.contains("Salary", ignoreCase = true) }?.id ?: 201L
        val foodCat = categories.firstOrNull { it.name.contains("Food", ignoreCase = true) }?.id ?: 1L
        val groceriesCat = categories.firstOrNull { it.name.contains("Groceries", ignoreCase = true) }?.id ?: 2L
        val shoppingCat = categories.firstOrNull { it.name.contains("Shopping", ignoreCase = true) }?.id ?: 3L
        val rentCat = categories.firstOrNull { it.name.contains("Housing", ignoreCase = true) || it.name.contains("Rent", ignoreCase = true) }?.id ?: 4L
        val transportCat = categories.firstOrNull { it.name.contains("Transportation", ignoreCase = true) }?.id ?: 5L
        val fuelCat = categories.firstOrNull { it.name.contains("Vehicle", ignoreCase = true) || it.name.contains("Fuel", ignoreCase = true) }?.id ?: 6L
        val utilitiesCat = categories.firstOrNull { it.name.contains("Utilities", ignoreCase = true) }?.id ?: 7L
        val entertainmentCat = categories.firstOrNull { it.name.contains("Entertainment", ignoreCase = true) }?.id ?: 8L
        val healthCat = categories.firstOrNull { it.name.contains("Health", ignoreCase = true) }?.id ?: 9L

        // 2. Create Accounts
        val cashAcc = AccountEntity(
            name = "Cash Wallet",
            groupType = AccountGroup.CASH,
            currencyCode = primaryCurrencyCode,
            openingBalanceMinor = scaleMinor(350_00L, primaryCurrencyCode),
            colorHex = "#10B981",
            iconName = "payments",
            sortOrder = 1
        )
        val salaryBankAcc = AccountEntity(
            name = "Primary Salary Bank",
            groupType = AccountGroup.BANK_ACCOUNT,
            currencyCode = primaryCurrencyCode,
            openingBalanceMinor = scaleMinor(4850_00L, primaryCurrencyCode),
            colorHex = "#3B82F6",
            iconName = "account_balance",
            sortOrder = 2
        )
        val billCheckingAcc = AccountEntity(
            name = "Bill Pay Checking",
            groupType = AccountGroup.BANK_ACCOUNT,
            currencyCode = primaryCurrencyCode,
            openingBalanceMinor = scaleMinor(1200_00L, primaryCurrencyCode),
            colorHex = "#6366F1",
            iconName = "account_balance",
            sortOrder = 3
        )
        val emergencyVaultAcc = AccountEntity(
            name = "Emergency Fund Vault",
            groupType = AccountGroup.SAVINGS,
            currencyCode = primaryCurrencyCode,
            openingBalanceMinor = scaleMinor(15000_00L, primaryCurrencyCode),
            colorHex = "#059669",
            iconName = "savings",
            sortOrder = 4
        )
        val card1Acc = AccountEntity(
            name = "Sapphire Rewards Card",
            groupType = AccountGroup.CREDIT_CARD,
            currencyCode = primaryCurrencyCode,
            openingBalanceMinor = 0L,
            creditLimitMinor = scaleMinor(6000_00L, primaryCurrencyCode),
            settlementDayOfMonth = today.minusDays(3).dayOfMonth.coerceIn(1, 28),
            paymentDueDayOfMonth = today.plusDays(12).dayOfMonth.coerceIn(1, 28),
            colorHex = "#8B5CF6",
            iconName = "credit_card",
            sortOrder = 5
        )
        val card2Acc = AccountEntity(
            name = "Infinity Cashback Card",
            groupType = AccountGroup.CREDIT_CARD,
            currencyCode = primaryCurrencyCode,
            openingBalanceMinor = 0L,
            creditLimitMinor = scaleMinor(8000_00L, primaryCurrencyCode),
            settlementDayOfMonth = today.plusDays(25).dayOfMonth.coerceIn(1, 28),
            paymentDueDayOfMonth = today.plusDays(45).dayOfMonth.coerceIn(1, 28),
            colorHex = "#EC4899",
            iconName = "credit_card",
            sortOrder = 6
        )
        val investmentAcc = AccountEntity(
            name = "Index Fund Portfolio",
            groupType = AccountGroup.INVESTMENT,
            currencyCode = primaryCurrencyCode,
            openingBalanceMinor = scaleMinor(28000_00L, primaryCurrencyCode),
            colorHex = "#F59E0B",
            iconName = "trending_up",
            sortOrder = 7
        )
        val loanAcc = AccountEntity(
            name = "Home Mortgage Loan",
            groupType = AccountGroup.LOAN,
            currencyCode = primaryCurrencyCode,
            openingBalanceMinor = -scaleMinor(48000_00L, primaryCurrencyCode),
            colorHex = "#EF4444",
            iconName = "home",
            sortOrder = 8
        )

        val cashId = accountDao.insertNewAccountRaw(cashAcc)
        val salaryBankId = accountDao.insertNewAccountRaw(salaryBankAcc)
        val billCheckingId = accountDao.insertNewAccountRaw(billCheckingAcc)
        val emergencyVaultId = accountDao.insertNewAccountRaw(emergencyVaultAcc)
        val card1Id = accountDao.insertNewAccountRaw(card1Acc)
        val card2Id = accountDao.insertNewAccountRaw(card2Acc)
        val investmentId = accountDao.insertNewAccountRaw(investmentAcc)
        val loanId = accountDao.insertNewAccountRaw(loanAcc)

        // 3. Budgets
        budgetDao.insertBudgets(
            listOf(
                BudgetEntity(
                    categoryId = null,
                    limitAmountMinor = scaleMinor(3200_00L, primaryCurrencyCode),
                    alertThresholdPercent = 80
                ),
                BudgetEntity(
                    categoryId = foodCat,
                    limitAmountMinor = scaleMinor(500_00L, primaryCurrencyCode),
                    alertThresholdPercent = 80
                ),
                BudgetEntity(
                    categoryId = groceriesCat,
                    limitAmountMinor = scaleMinor(600_00L, primaryCurrencyCode),
                    alertThresholdPercent = 80
                ),
                BudgetEntity(
                    categoryId = shoppingCat,
                    limitAmountMinor = scaleMinor(350_00L, primaryCurrencyCode),
                    alertThresholdPercent = 80
                )
            )
        )

        // 4. Recurring Commitments
        budgetDao.insertRecurringRules(
            listOf(
                RecurringRuleEntity(
                    title = "Apartment Rent & Maintenance",
                    type = TransactionType.EXPENSE,
                    amountMinor = scaleMinor(1250_00L, primaryCurrencyCode),
                    currencyCode = primaryCurrencyCode,
                    accountId = billCheckingId,
                    categoryId = rentCat,
                    frequency = RepeatFrequency.EVERY_MONTH,
                    startDateEpochDay = today.minusMonths(3).toEpochDay(),
                    nextOccurrenceEpochDay = today.plusDays(5).toEpochDay(),
                    payeeOrDescription = "Skyline Realty Group",
                    note = "Monthly apartment lease commitment"
                ),
                RecurringRuleEntity(
                    title = "Gigabit Fiber Internet",
                    type = TransactionType.EXPENSE,
                    amountMinor = scaleMinor(65_00L, primaryCurrencyCode),
                    currencyCode = primaryCurrencyCode,
                    accountId = salaryBankId,
                    categoryId = utilitiesCat,
                    frequency = RepeatFrequency.EVERY_MONTH,
                    startDateEpochDay = today.minusMonths(3).toEpochDay(),
                    nextOccurrenceEpochDay = today.plusDays(8).toEpochDay(),
                    payeeOrDescription = "Metro Fiber Internet",
                    note = "Home broadband connection"
                ),
                RecurringRuleEntity(
                    title = "All-in-One Cloud & Media Bundle",
                    type = TransactionType.EXPENSE,
                    amountMinor = scaleMinor(28_00L, primaryCurrencyCode),
                    currencyCode = primaryCurrencyCode,
                    accountId = card2Id,
                    categoryId = entertainmentCat,
                    frequency = RepeatFrequency.EVERY_MONTH,
                    startDateEpochDay = today.minusMonths(3).toEpochDay(),
                    nextOccurrenceEpochDay = today.plusDays(11).toEpochDay(),
                    payeeOrDescription = "Digital Media Bundle",
                    note = "Music, movies & cloud storage"
                )
            )
        )

        // 5. Bookmarks
        categoryDao.insertBookmarks(
            listOf(
                BookmarkEntity(
                    title = "Artisan Espresso",
                    type = TransactionType.EXPENSE,
                    amountMinor = scaleMinor(4_50L, primaryCurrencyCode),
                    accountId = cashId,
                    categoryId = foodCat,
                    payeeOrDescription = "Blue Bottle Cafe",
                    sortOrder = 1
                ),
                BookmarkEntity(
                    title = "Metro Transit Fare",
                    type = TransactionType.EXPENSE,
                    amountMinor = scaleMinor(2_75L, primaryCurrencyCode),
                    accountId = cashId,
                    categoryId = transportCat,
                    payeeOrDescription = "City Transit Authority",
                    sortOrder = 2
                ),
                BookmarkEntity(
                    title = "Supermarket Essentials",
                    type = TransactionType.EXPENSE,
                    amountMinor = scaleMinor(95_00L, primaryCurrencyCode),
                    accountId = card1Id,
                    categoryId = groceriesCat,
                    payeeOrDescription = "Whole Foods Market",
                    sortOrder = 3
                )
            )
        )

        // Helper for inserting transactions with timestamps
        fun timeMillis(dayOffset: Long, hour: Int, minute: Int): Pair<Long, Long> {
            val date = today.plusDays(dayOffset)
            val dt = date.atTime(LocalTime.of(hour, minute))
            val millis = dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            return Pair(date.toEpochDay(), millis)
        }

        suspend fun logTx(
            dayOffset: Long,
            hour: Int,
            minute: Int,
            type: TransactionType,
            amountMinor: Long,
            accId: Long,
            catId: Long?,
            payee: String,
            note: String = "",
            transferToId: Long? = null,
            splits: List<TransactionSplitEntity> = emptyList(),
            tags: List<String> = emptyList()
        ) {
            val (epochDay, epochMillis) = timeMillis(dayOffset, hour, minute)
            val entity = TransactionEntity(
                type = type,
                timestampEpochMillis = epochMillis,
                ledgerDateEpochDay = epochDay,
                amountMinor = amountMinor,
                currencyCode = primaryCurrencyCode,
                accountId = accId,
                transferToAccountId = transferToId,
                categoryId = catId,
                isSplit = splits.isNotEmpty(),
                payeeOrDescription = payee,
                note = note
            )
            transactionDao.upsertTransactionWithDetails(entity, splits, tags)
        }

        // =====================================================================
        // 6. HISTORICAL TRANSACTIONS (~45 transactions across 90 days)
        // =====================================================================

        // --- Month -2 (Approx 65 to 80 days ago) ---
        logTx(-78, 9, 30, TransactionType.INCOME, scaleMinor(4200_00L, primaryCurrencyCode), salaryBankId, salaryCat, "Acme Corp Tech", "Monthly Salary Paycheck")
        logTx(-76, 11, 0, TransactionType.TRANSFER, scaleMinor(600_00L, primaryCurrencyCode), salaryBankId, null, "Emergency Vault Allocation", transferToId = emergencyVaultId)
        logTx(-75, 14, 0, TransactionType.TRANSFER, scaleMinor(1400_00L, primaryCurrencyCode), salaryBankId, null, "Bill Pay Reserve Transfer", transferToId = billCheckingId)
        logTx(-74, 10, 0, TransactionType.EXPENSE, scaleMinor(1250_00L, primaryCurrencyCode), billCheckingId, rentCat, "Skyline Realty Group", "Apartment Rent Payment")
        logTx(-72, 18, 45, TransactionType.EXPENSE, scaleMinor(145_00L, primaryCurrencyCode), card1Id, groceriesCat, "Whole Foods Market", "Bi-weekly pantry haul")
        logTx(-70, 16, 20, TransactionType.EXPENSE, scaleMinor(48_00L, primaryCurrencyCode), card1Id, fuelCat, "Shell Express", "Vehicle Fuel")
        logTx(-68, 20, 15, TransactionType.EXPENSE, scaleMinor(86_00L, primaryCurrencyCode), card1Id, foodCat, "Trattoria Romana", "Dinner with colleagues")
        logTx(-65, 12, 0, TransactionType.TRANSFER, scaleMinor(500_00L, primaryCurrencyCode), salaryBankId, null, "Monthly Index SIP", transferToId = investmentId)

        // --- Month -1 (Approx 32 to 55 days ago) ---
        logTx(-48, 9, 30, TransactionType.INCOME, scaleMinor(4200_00L, primaryCurrencyCode), salaryBankId, salaryCat, "Acme Corp Tech", "Monthly Salary Paycheck")
        logTx(-46, 11, 0, TransactionType.TRANSFER, scaleMinor(1400_00L, primaryCurrencyCode), salaryBankId, null, "Bill Pay Reserve Transfer", transferToId = billCheckingId)
        logTx(-45, 10, 0, TransactionType.EXPENSE, scaleMinor(1250_00L, primaryCurrencyCode), billCheckingId, rentCat, "Skyline Realty Group", "Apartment Rent Payment")
        logTx(-42, 15, 30, TransactionType.CC_SETTLEMENT, scaleMinor(680_00L, primaryCurrencyCode), salaryBankId, null, "Sapphire Statement Settlement", transferToId = card1Id)
        logTx(-40, 17, 30, TransactionType.EXPENSE, scaleMinor(112_00L, primaryCurrencyCode), card2Id, groceriesCat, "Trader Joe's", "Weekly groceries")
        logTx(-38, 14, 15, TransactionType.EXPENSE, scaleMinor(34_00L, primaryCurrencyCode), card2Id, healthCat, "Walgreens Pharmacy", "Vitamins & Wellness")
        logTx(-35, 11, 45, TransactionType.EXPENSE, scaleMinor(62_00L, primaryCurrencyCode), card2Id, fuelCat, "Chevron Station", "Road trip fuel", tags = listOf("vacation"))
        logTx(-34, 19, 30, TransactionType.EXPENSE, scaleMinor(94_00L, primaryCurrencyCode), card2Id, foodCat, "Oceanview Bistro", "Vacation Dinner", tags = listOf("vacation"))
        logTx(-32, 16, 0, TransactionType.CC_SETTLEMENT, scaleMinor(410_00L, primaryCurrencyCode), salaryBankId, null, "Infinity Statement Settlement", transferToId = card2Id)

        // --- Current Month (Last 25 days up to Today) ---
        logTx(-22, 9, 30, TransactionType.INCOME, scaleMinor(4200_00L, primaryCurrencyCode), salaryBankId, salaryCat, "Acme Corp Tech", "Monthly Salary Paycheck")
        logTx(-20, 11, 0, TransactionType.TRANSFER, scaleMinor(500_00L, primaryCurrencyCode), salaryBankId, null, "Emergency Vault Allocation", transferToId = emergencyVaultId)
        logTx(-19, 14, 0, TransactionType.TRANSFER, scaleMinor(1400_00L, primaryCurrencyCode), salaryBankId, null, "Bill Pay Reserve Transfer", transferToId = billCheckingId)
        logTx(-18, 10, 0, TransactionType.EXPENSE, scaleMinor(1250_00L, primaryCurrencyCode), billCheckingId, rentCat, "Skyline Realty Group", "Apartment Rent Payment")
        logTx(-16, 12, 15, TransactionType.EXPENSE, scaleMinor(65_00L, primaryCurrencyCode), billCheckingId, utilitiesCat, "Metro Fiber Internet", "Monthly broadband")
        logTx(-14, 15, 30, TransactionType.EXPENSE, scaleMinor(84_00L, primaryCurrencyCode), billCheckingId, utilitiesCat, "City Power & Water", "Utility bill")
        logTx(-12, 13, 0, TransactionType.EXPENSE, scaleMinor(78_00L, primaryCurrencyCode), card1Id, foodCat, "Sushiya Japanese", "Team lunch celebration")

        // Split transaction: Target Superstore haul
        val targetSplit1 = TransactionSplitEntity(transactionId = 0L, categoryId = groceriesCat, amountMinor = scaleMinor(110_00L, primaryCurrencyCode), note = "Weekly pantry essentials")
        val targetSplit2 = TransactionSplitEntity(transactionId = 0L, categoryId = utilitiesCat, amountMinor = scaleMinor(45_00L, primaryCurrencyCode), note = "Cleaning supplies & filters")
        val targetSplit3 = TransactionSplitEntity(transactionId = 0L, categoryId = healthCat, amountMinor = scaleMinor(30_00L, primaryCurrencyCode), note = "First aid & personal care")
        logTx(
            dayOffset = -10,
            hour = 17,
            minute = 45,
            type = TransactionType.EXPENSE,
            amountMinor = scaleMinor(185_00L, primaryCurrencyCode),
            accId = card1Id,
            catId = null,
            payee = "Target Superstore",
            note = "Multi-category shopping haul",
            splits = listOf(targetSplit1, targetSplit2, targetSplit3)
        )

        // Work expenses tagged #reimbursable
        logTx(-8, 9, 15, TransactionType.EXPENSE, scaleMinor(18_50L, primaryCurrencyCode), card2Id, foodCat, "Blue Bottle Coffee", "Client discovery chat", tags = listOf("reimbursable"))
        logTx(-7, 13, 0, TransactionType.EXPENSE, scaleMinor(54_00L, primaryCurrencyCode), card2Id, foodCat, "Parkside Grill", "Client business lunch", tags = listOf("reimbursable"))

        // Recent days dining & shopping (brings Food & Dining spending to ~85% of budget)
        logTx(-6, 20, 0, TransactionType.EXPENSE, scaleMinor(145_00L, primaryCurrencyCode), card1Id, foodCat, "L'Ami Bistro", "Anniversary dinner celebration")
        logTx(-5, 14, 30, TransactionType.EXPENSE, scaleMinor(42_00L, primaryCurrencyCode), card1Id, foodCat, "Green Fork Deli", "Weekend brunch")
        logTx(-4, 18, 0, TransactionType.EXPENSE, scaleMinor(135_00L, primaryCurrencyCode), card1Id, shoppingCat, "Nordstrom Rack", "Seasonal apparel")
        logTx(-3, 11, 20, TransactionType.EXPENSE, scaleMinor(65_00L, primaryCurrencyCode), cashId, groceriesCat, "Fresh Greens Market", "Organic veggies & dairy")
        logTx(-2, 19, 45, TransactionType.EXPENSE, scaleMinor(38_00L, primaryCurrencyCode), card1Id, foodCat, "Napoli Pizzeria", "Friday night pizza delivery")
        logTx(-1, 15, 10, TransactionType.EXPENSE, scaleMinor(46_00L, primaryCurrencyCode), card2Id, fuelCat, "Chevron Station", "Commuter vehicle fuel")

        // Day 0: Today!
        logTx(0, 8, 30, TransactionType.EXPENSE, scaleMinor(4_50L, primaryCurrencyCode), cashId, foodCat, "Blue Bottle Cafe", "Morning cortado")
        logTx(0, 11, 15, TransactionType.EXPENSE, scaleMinor(22_00L, primaryCurrencyCode), card2Id, healthCat, "CVS Health", "Daily supplements")
        logTx(0, 13, 30, TransactionType.EXPENSE, scaleMinor(16_50L, primaryCurrencyCode), cashId, foodCat, "Chipotle Mexican Grill", "Burrito bowl lunch")

        // Generate card milestones JSON for Sapphire card
        val cardMilestonesJson = JSONObject().apply {
            val milestoneArray = JSONArray().apply {
                put(
                    JSONObject().apply {
                        put("title", "Annual Fee Waiver Milestone")
                        put("targetAmountMinor", scaleMinor(25000_00L, primaryCurrencyCode))
                        put("spentAmountMinor", scaleMinor(18200_00L, primaryCurrencyCode))
                        put("endDateEpochDay", today.plusMonths(4).toEpochDay())
                    }
                )
            }
            put(card1Id.toString(), milestoneArray)
        }.toString()

        return cardMilestonesJson
    }
}

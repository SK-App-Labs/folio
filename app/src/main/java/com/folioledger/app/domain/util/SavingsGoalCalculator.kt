// File: app/src/main/java/com/folioledger/app/domain/util/SavingsGoalCalculator.kt
package com.folioledger.app.domain.util

import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSavingsGoal
import com.folioledger.app.domain.model.AccountSavingsGoalConfig
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlySavingsProgress
import com.folioledger.app.domain.model.PeriodLedgerSummary
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import org.json.JSONObject
import java.time.LocalDate

object SavingsGoalCalculator {

    /**
     * Parses the persistent savings goals JSON map: `{ "accountId": { ...config } }`.
     */
    fun parseGoals(json: String?): Map<Long, AccountSavingsGoalConfig> {
        if (json.isNullOrBlank() || json.trim() == "{}") return emptyMap()
        return runCatching {
            val root = JSONObject(json)
            val map = mutableMapOf<Long, AccountSavingsGoalConfig>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val accountId = key.toLongOrNull() ?: continue
                val obj = root.getJSONObject(key)
                map[accountId] = AccountSavingsGoalConfig(
                    accountId = accountId,
                    goalTitle = obj.optString("goalTitle", "Savings Goal"),
                    targetAmountMinor = obj.optLong("targetAmountMinor", 500_000_00L),
                    targetDateEpochDay = if (obj.has("targetDateEpochDay") && !obj.isNull("targetDateEpochDay")) {
                        obj.getLong("targetDateEpochDay")
                    } else null
                )
            }
            map
        }.getOrDefault(emptyMap())
    }

    /**
     * Upserts a goal config for [config.accountId] into the existing JSON string.
     */
    fun upsertGoal(
        currentJson: String?,
        config: AccountSavingsGoalConfig
    ): String = runCatching {
        val root = if (currentJson.isNullOrBlank() || currentJson.trim() == "{}") JSONObject() else JSONObject(currentJson)
        val obj = JSONObject().apply {
            put("accountId", config.accountId)
            put("goalTitle", config.goalTitle)
            put("targetAmountMinor", config.targetAmountMinor)
            put("targetDateEpochDay", config.targetDateEpochDay ?: JSONObject.NULL)
        }
        root.put(config.accountId.toString(), obj)
        root.toString()
    }.getOrDefault(currentJson ?: "{}")

    /**
     * Removes the goal config for [accountId] from the existing JSON string.
     */
    fun removeGoal(currentJson: String?, accountId: Long): String = runCatching {
        if (currentJson.isNullOrBlank() || currentJson.trim() == "{}") return "{}"
        val root = JSONObject(currentJson)
        root.remove(accountId.toString())
        root.toString()
    }.getOrDefault(currentJson ?: "{}")

    /**
     * Computes account-backed savings goals combining configs with live ledger account balances.
     */
    fun computeAccountSavingsGoals(
        allAccounts: List<AccountSummary>,
        goalsJson: String?
    ): List<AccountSavingsGoal> = computeAccountSavingsGoals(allAccounts, parseGoals(goalsJson))

    fun computeAccountSavingsGoals(
        allAccounts: List<AccountSummary>,
        configs: Map<Long, AccountSavingsGoalConfig>
    ): List<AccountSavingsGoal> {
        if (configs.isEmpty()) return emptyList()

        val accountMap = allAccounts.associateBy { it.id }
        return configs.values.mapNotNull { config ->
            val account = accountMap[config.accountId] ?: return@mapNotNull null
            val currentBalance = account.currentBalance
            val targetAmount = MinorMoney(config.targetAmountMinor)
            val currentMinor = currentBalance.amountMinor
            val targetMinor = config.targetAmountMinor

            val progressPct = if (targetMinor > 0L) {
                ((currentMinor.toFloat() / targetMinor.toFloat()) * 100f).coerceAtLeast(0f)
            } else 0f

            val remaining = if (currentMinor < targetMinor) {
                MinorMoney(targetMinor - currentMinor)
            } else {
                MinorMoney.ZERO
            }

            AccountSavingsGoal(
                accountId = account.id,
                accountName = account.name,
                accountGroup = account.groupType,
                colorHex = account.colorHex,
                iconName = account.iconName,
                goalTitle = config.goalTitle,
                targetAmount = targetAmount,
                currentBalance = currentBalance,
                progressPercentage = progressPct,
                remainingAmount = remaining,
                targetDateEpochDay = config.targetDateEpochDay,
                isAchieved = currentMinor >= targetMinor
            )
        }.sortedWith(
            compareBy<AccountSavingsGoal> { it.isAchieved }
                .thenByDescending { it.progressPercentage }
        )
    }

    /**
     * Computes the "Pay Yourself First" monthly savings progress by tracking net wealth transfers
     * from operational accounts into SAVINGS and INVESTMENT accounts.
     */
    fun computeMonthlySavingsProgress(
        allAccounts: List<AccountSummary>,
        periodTransactions: List<TransactionItem>,
        periodSummary: PeriodLedgerSummary,
        monthlySavingsTargetMinor: Long
    ): MonthlySavingsProgress {
        val accountMap = allAccounts.associateBy { it.id }
        val wealthGroups = setOf(AccountGroup.SAVINGS, AccountGroup.INVESTMENT)

        var savingsDepositsMinor = 0L
        var savingsWithdrawalsMinor = 0L

        for (tx in periodTransactions) {
            if (tx.type == TransactionType.TRANSFER) {
                val source = accountMap[tx.accountId]
                val dest = tx.transferToAccountId?.let { accountMap[it] }

                val sourceIsWealth = source?.groupType in wealthGroups
                val destIsWealth = dest?.groupType in wealthGroups

                if (!sourceIsWealth && destIsWealth) {
                    // Deposit into wealth accumulation account
                    savingsDepositsMinor = Math.addExact(savingsDepositsMinor, tx.amount.amountMinor)
                } else if (sourceIsWealth && !destIsWealth) {
                    // Withdrawal from wealth accumulation account
                    savingsWithdrawalsMinor = Math.addExact(savingsWithdrawalsMinor, tx.amount.amountMinor)
                }
            }
        }

        val netSavedMinor = (savingsDepositsMinor - savingsWithdrawalsMinor).coerceAtLeast(0L)
        val savedMoney = MinorMoney(netSavedMinor)
        val targetMoney = MinorMoney(monthlySavingsTargetMinor)

        val progressPercentage = if (monthlySavingsTargetMinor > 0L) {
            ((netSavedMinor.toFloat() / monthlySavingsTargetMinor.toFloat()) * 100f).coerceAtLeast(0f)
        } else 0f

        val remainingMoney = if (netSavedMinor < monthlySavingsTargetMinor) {
            MinorMoney(monthlySavingsTargetMinor - netSavedMinor)
        } else {
            MinorMoney.ZERO
        }

        // Unallocated Cashflow Surplus = Income - Expenses - Net Savings Transferred
        val netCashflowMinor = periodSummary.netCashflow.amountMinor
        val unallocatedMinor = (netCashflowMinor - netSavedMinor).coerceAtLeast(0L)
        val unallocatedSurplus = MinorMoney(unallocatedMinor)

        return MonthlySavingsProgress(
            targetAmount = targetMoney,
            savedAmount = savedMoney,
            remainingAmount = remainingMoney,
            progressPercentage = progressPercentage,
            unallocatedSurplus = unallocatedSurplus,
            isConfigured = monthlySavingsTargetMinor > 0L
        )
    }
}

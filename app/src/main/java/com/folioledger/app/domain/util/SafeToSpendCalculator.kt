// File: app/src/main/java/com/folioledger/app/domain/util/SafeToSpendCalculator.kt
package com.folioledger.app.domain.util

import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountShortfallAlert
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.RecurringCommitmentItem
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.SafeToSpendState
import com.folioledger.app.domain.model.SafeToSpendStatus
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType

object SafeToSpendCalculator {

    fun projectRecurringOccurrencesInHorizon(
        startDay: Long,
        frequency: RepeatFrequency,
        todayEpochDay: Long,
        horizonEpochDay: Long
    ): List<Long> {
        return RecurringScheduleCalculator.projectOccurrencesInHorizon(
            startDay = startDay,
            frequency = frequency,
            todayEpochDay = todayEpochDay,
            horizonEpochDay = horizonEpochDay
        )
    }

    fun computeSafeToSpend(
        allAccounts: List<AccountSummary>,
        recurringRules: List<RecurringCommitmentItem>,
        futureTransactions: List<TransactionItem>,
        todayEpochDay: Long,
        periodStartEpochDay: Long,
        periodEndEpochDay: Long,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): SafeToSpendState {
        val primarySpec = SupportedCurrencies.fromCode(primaryCurrencyCode)
        val secondaryMap = secondaryCurrencies.associateBy { it.code.trim().uppercase() }

        fun convertToPrimary(rawAmountMinor: Long, currencyCode: String): Long {
            val accCurr = currencyCode.trim().uppercase()
            if (accCurr == primarySpec.code.uppercase()) return rawAmountMinor
            val secondary = secondaryMap[accCurr]
            return if (secondary != null && secondary.exchangeRateToPrimary > 0.0) {
                val accSpec = SupportedCurrencies.fromCode(accCurr)
                MoneyFormatterAndCalculator.convertMinorMoney(
                    sourceMinor = rawAmountMinor,
                    sourceFractionDigits = accSpec.fractionDigits,
                    targetFractionDigits = primarySpec.fractionDigits,
                    rateToTarget = secondary.exchangeRateToPrimary
                )
            } else {
                rawAmountMinor
            }
        }

        // 1. Total Liquid Cash & Bank Balances
        var totalLiquidMinor = 0L
        for (acc in allAccounts) {
            if (acc.isArchived || acc.currentBalance.amountMinor <= 0L) continue
            val isLiquid = acc.groupType == AccountGroup.BANK_ACCOUNT ||
                    acc.groupType == AccountGroup.CASH ||
                    acc.groupType == AccountGroup.SAVINGS ||
                    acc.groupType == AccountGroup.PREPAID_WALLET
            if (isLiquid) {
                totalLiquidMinor += convertToPrimary(acc.currentBalance.amountMinor, acc.currencyCode)
            }
        }

        // 2. Total Credit Card Debt (Billed + Unbilled)
        var totalCcDebtMinor = 0L
        for (acc in allAccounts) {
            if (acc.isArchived || acc.groupType != AccountGroup.CREDIT_CARD) continue
            // Only negative balance indicates outstanding debt owed (negative in Folio double-entry ledger).
            // A positive balance indicates an overpayment or store credit, which is not debt.
            if (acc.currentBalance.amountMinor < 0L) {
                val debtMinor = Math.abs(acc.currentBalance.amountMinor)
                totalCcDebtMinor += convertToPrimary(debtMinor, acc.currencyCode)
            }
        }

        // 3. Upcoming 14-Day Commitments
        val horizonEpochDay = todayEpochDay + 14L
        var upcomingCommitmentsMinor = 0L

        // a. Recurring Rules
        for (rule in recurringRules) {
            if (!rule.isActive) continue
            if (rule.type != TransactionType.EXPENSE && rule.type != TransactionType.TRANSFER) continue

            val occurrences = projectRecurringOccurrencesInHorizon(
                startDay = rule.nextOccurrenceEpochDay,
                frequency = rule.frequency,
                todayEpochDay = todayEpochDay,
                horizonEpochDay = horizonEpochDay
            )

            val singleAmt = convertToPrimary(rule.amount.amountMinor, rule.currencyCode)
            upcomingCommitmentsMinor += (singleAmt * occurrences.size)
        }

        // b. Future Scheduled Transactions
        for (tx in futureTransactions) {
            if (tx.ledgerDateEpochDay in (todayEpochDay + 1L)..horizonEpochDay) {
                if (tx.type == TransactionType.EXPENSE) {
                    val amt = convertToPrimary(Math.abs(tx.amount.amountMinor) + Math.abs(tx.transferFee.amountMinor), tx.currencyCode)
                    upcomingCommitmentsMinor += amt
                }
            }
        }

        val trueSafeToSpendMinor = totalLiquidMinor - totalCcDebtMinor - upcomingCommitmentsMinor
        val isPositive = trueSafeToSpendMinor >= 0L
        val isCurrentPeriod = todayEpochDay in periodStartEpochDay..periodEndEpochDay

        val status = when {
            trueSafeToSpendMinor > 0L -> SafeToSpendStatus.SAFE
            totalLiquidMinor == 0L && totalCcDebtMinor == 0L && upcomingCommitmentsMinor == 0L -> SafeToSpendStatus.ZERO_RESERVES
            upcomingCommitmentsMinor == 0L && totalCcDebtMinor > totalLiquidMinor -> SafeToSpendStatus.CARD_DEBT_EXCEEDS_CASH
            totalCcDebtMinor == 0L && upcomingCommitmentsMinor > totalLiquidMinor -> SafeToSpendStatus.BILLS_EXCEED_CASH
            totalLiquidMinor >= totalCcDebtMinor && upcomingCommitmentsMinor > (totalLiquidMinor - totalCcDebtMinor) -> SafeToSpendStatus.BILLS_EXCEED_REMAINING_CASH
            totalCcDebtMinor > totalLiquidMinor && upcomingCommitmentsMinor > 0L -> SafeToSpendStatus.COMBINED_OBLIGATIONS_EXCEED_CASH
            trueSafeToSpendMinor == 0L -> SafeToSpendStatus.SAFE
            else -> if (totalCcDebtMinor > 0L) SafeToSpendStatus.CARD_DEBT_EXCEEDS_CASH else SafeToSpendStatus.BILLS_EXCEED_CASH
        }

        return SafeToSpendState(
            totalLiquidBankAndCash = MinorMoney(totalLiquidMinor),
            totalCreditCardDebt = MinorMoney(totalCcDebtMinor),
            upcoming14DayCommitments = MinorMoney(upcomingCommitmentsMinor),
            trueSafeToSpend = MinorMoney(trueSafeToSpendMinor),
            isPositive = isPositive,
            isCurrentPeriod = isCurrentPeriod,
            status = status
        )
    }

    fun computeShortfallAlerts(
        allAccounts: List<AccountSummary>,
        recurringRules: List<RecurringCommitmentItem>,
        futureTransactions: List<TransactionItem>,
        todayEpochDay: Long,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): List<AccountShortfallAlert> {
        val primarySpec = SupportedCurrencies.fromCode(primaryCurrencyCode)
        val secondaryMap = secondaryCurrencies.associateBy { it.code.trim().uppercase() }

        fun convertToPrimary(rawAmountMinor: Long, currencyCode: String): Long {
            val accCurr = currencyCode.trim().uppercase()
            if (accCurr == primarySpec.code.uppercase()) return rawAmountMinor
            val secondary = secondaryMap[accCurr]
            return if (secondary != null && secondary.exchangeRateToPrimary > 0.0) {
                val accSpec = SupportedCurrencies.fromCode(accCurr)
                MoneyFormatterAndCalculator.convertMinorMoney(
                    sourceMinor = rawAmountMinor,
                    sourceFractionDigits = accSpec.fractionDigits,
                    targetFractionDigits = primarySpec.fractionDigits,
                    rateToTarget = secondary.exchangeRateToPrimary
                )
            } else {
                rawAmountMinor
            }
        }

        val horizonEpochDay = todayEpochDay + 14L
        val debitsByAccount = mutableMapOf<Long, Long>()
        val earliestDebitByAccount = mutableMapOf<Long, Long>()

        // 1. Recurring Rules
        for (rule in recurringRules) {
            if (!rule.isActive) continue
            if (rule.type != TransactionType.EXPENSE && rule.type != TransactionType.TRANSFER) continue

            val occurrences = projectRecurringOccurrencesInHorizon(
                startDay = rule.nextOccurrenceEpochDay,
                frequency = rule.frequency,
                todayEpochDay = todayEpochDay,
                horizonEpochDay = horizonEpochDay
            )

            val singleAmt = convertToPrimary(rule.amount.amountMinor, rule.currencyCode)
            for (occDay in occurrences) {
                debitsByAccount[rule.accountId] = (debitsByAccount[rule.accountId] ?: 0L) + singleAmt
                val curr = earliestDebitByAccount[rule.accountId]
                earliestDebitByAccount[rule.accountId] = if (curr == null) occDay else minOf(curr, occDay)
            }
        }

        // 2. Future Scheduled Transactions
        for (tx in futureTransactions) {
            if (tx.ledgerDateEpochDay in (todayEpochDay + 1L)..horizonEpochDay) {
                if (tx.type == TransactionType.EXPENSE) {
                    val amt = convertToPrimary(Math.abs(tx.amount.amountMinor) + Math.abs(tx.transferFee.amountMinor), tx.currencyCode)
                    debitsByAccount[tx.accountId] = (debitsByAccount[tx.accountId] ?: 0L) + amt
                    val curr = earliestDebitByAccount[tx.accountId]
                    earliestDebitByAccount[tx.accountId] = if (curr == null) tx.ledgerDateEpochDay else minOf(curr, tx.ledgerDateEpochDay)
                }
            }
        }

        // 3. Match against live liquid account balances
        val alerts = mutableListOf<AccountShortfallAlert>()
        for (acc in allAccounts) {
            if (acc.isArchived) continue
            val isLiquidBank = acc.groupType == AccountGroup.BANK_ACCOUNT ||
                    acc.groupType == AccountGroup.SAVINGS ||
                    acc.groupType == AccountGroup.PREPAID_WALLET
            if (!isLiquidBank) continue

            val scheduledDebitsMinor = debitsByAccount[acc.id] ?: 0L
            if (scheduledDebitsMinor <= 0L) continue

            val liveBalanceMinor = convertToPrimary(acc.currentBalance.amountMinor, acc.currencyCode)
            if (liveBalanceMinor < scheduledDebitsMinor) {
                val shortfallMinor = scheduledDebitsMinor - liveBalanceMinor
                val earliestDay = earliestDebitByAccount[acc.id] ?: todayEpochDay
                val daysUntil = maxOf(0L, earliestDay - todayEpochDay)

                alerts.add(
                    AccountShortfallAlert(
                        accountId = acc.id,
                        accountName = acc.name,
                        currentBalance = acc.currentBalance,
                        upcomingDebits = MinorMoney(scheduledDebitsMinor),
                        shortfallAmount = MinorMoney(shortfallMinor),
                        daysUntilShortfall = daysUntil
                    )
                )
            }
        }

        return alerts.sortedBy { it.daysUntilShortfall }
    }
}

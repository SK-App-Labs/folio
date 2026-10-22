// File: app/src/main/java/com/folioledger/app/domain/usecase/AccountingRulesEngine.kt
package com.folioledger.app.domain.usecase

import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.SplitLineItem
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

data class CreditCardMetrics(
    val currentBalance: MinorMoney,
    val outstandingBalance: MinorMoney,
    val statementPayable: MinorMoney,
    val unbilledAmount: MinorMoney,
    val availableCredit: MinorMoney?,
    val utilizationPercent: Float,
    val lastStatementCloseEpochDay: Long
)

class AccountingRulesEngine {

    fun validateSplitTransaction(
        totalAmount: MinorMoney,
        splits: List<SplitLineItem>
    ): Result<Unit> = Companion.validateSplitTransaction(totalAmount, splits)

    fun generateInstallmentSchedule(
        totalAmount: MinorMoney,
        monthsCount: Int,
        startDateEpochDay: Long
    ): List<Pair<Long, MinorMoney>> =
        Companion.generateInstallmentSchedule(totalAmount, monthsCount, startDateEpochDay)

    fun computeFinancialMonthRange(
        referenceDateEpochDay: Long,
        monthStartDay: Int
    ): Pair<Long, Long> =
        Companion.computeFinancialMonthRange(referenceDateEpochDay, monthStartDay)

    fun computeCreditCardMetrics(
        openingBalance: MinorMoney,
        transactions: List<TransactionItem>,
        settlementDayOfMonth: Int,
        referenceDateEpochDay: Long,
        creditLimit: MinorMoney?
    ): CreditCardMetrics = Companion.computeCreditCardMetrics(
        openingBalance = openingBalance,
        transactions = transactions,
        settlementDayOfMonth = settlementDayOfMonth,
        referenceDateEpochDay = referenceDateEpochDay,
        creditLimit = creditLimit
    )

    companion object {

        fun validateSplitTransaction(
            totalAmount: MinorMoney,
            splits: List<SplitLineItem>
        ): Result<Unit> {
            if (!totalAmount.isPositive) {
                return Result.failure(
                    IllegalArgumentException("Parent split transaction total must be greater than zero.")
                )
            }
            if (splits.size < 2) {
                return Result.failure(
                    IllegalArgumentException("A split transaction requires at least 2 split line items.")
                )
            }

            return try {
                var runningSum = MinorMoney.ZERO
                for ((index, item) in splits.withIndex()) {
                    if (!item.amount.isPositive) {
                        return Result.failure(
                            IllegalArgumentException(
                                "Split line item #${index + 1} must have a positive amount."
                            )
                        )
                    }
                    runningSum += item.amount
                }

                if (runningSum != totalAmount) {
                    Result.failure(
                        IllegalArgumentException(
                            "Split sum (${runningSum.amountMinor}) does not match parent total (${totalAmount.amountMinor})."
                        )
                    )
                } else {
                    Result.success(Unit)
                }
            } catch (e: ArithmeticException) {
                Result.failure(
                    ArithmeticException("Overflow while summing split line items.")
                )
            }
        }

        fun generateInstallmentSchedule(
            totalAmount: MinorMoney,
            monthsCount: Int,
            startDateEpochDay: Long
        ): List<Pair<Long, MinorMoney>> {
            require(totalAmount.isPositive) {
                "Installment totalAmount must be strictly positive."
            }
            require(monthsCount >= 1) {
                "Installment monthsCount must be at least 1."
            }

            val baseAmountMinor: Long = totalAmount.amountMinor / monthsCount.toLong()
            val remainderMinor: Long = totalAmount.amountMinor % monthsCount.toLong()
            val firstInstallmentMinor: Long = Math.addExact(baseAmountMinor, remainderMinor)
            val startDate: LocalDate = LocalDate.ofEpochDay(startDateEpochDay)

            return List(monthsCount) { index ->
                val installmentDateEpochDay = startDate.plusMonths(index.toLong()).toEpochDay()
                val installmentMoney = if (index == 0) {
                    MinorMoney(firstInstallmentMinor)
                } else {
                    MinorMoney(baseAmountMinor)
                }
                installmentDateEpochDay to installmentMoney
            }
        }

        fun computeFinancialMonthRange(
            referenceDateEpochDay: Long,
            monthStartDay: Int
        ): Pair<Long, Long> {
            require(monthStartDay in 1..28) {
                "monthStartDay must be between 1 and 28 inclusive, but was $monthStartDay."
            }

            val referenceDate = LocalDate.ofEpochDay(referenceDateEpochDay)
            val cycleStartDate = if (referenceDate.dayOfMonth >= monthStartDay) {
                referenceDate.withDayOfMonth(monthStartDay)
            } else {
                referenceDate.minusMonths(1).withDayOfMonth(monthStartDay)
            }

            val cycleEndDate = cycleStartDate.plusMonths(1).minusDays(1)
            return cycleStartDate.toEpochDay() to cycleEndDate.toEpochDay()
        }

        fun computeCreditCardMetrics(
            openingBalance: MinorMoney,
            transactions: List<TransactionItem>,
            settlementDayOfMonth: Int,
            referenceDateEpochDay: Long,
            creditLimit: MinorMoney?,
            initialUnbilledAmount: MinorMoney? = null
        ): CreditCardMetrics {
            require(settlementDayOfMonth in 1..28) {
                "settlementDayOfMonth must be between 1 and 28 inclusive, but was $settlementDayOfMonth."
            }

            val referenceDate = LocalDate.ofEpochDay(referenceDateEpochDay)
            val lastStatementCloseDate = if (referenceDate.dayOfMonth >= settlementDayOfMonth) {
                referenceDate.withDayOfMonth(settlementDayOfMonth)
            } else {
                referenceDate.minusMonths(1).withDayOfMonth(settlementDayOfMonth)
            }
            val lastStatementCloseEpochDay = lastStatementCloseDate.toEpochDay()

            // Deduplicate transactions by non-zero id so transfer/settlement legs are never counted twice.
            val uniqueTransactions = buildList {
                val seenIds = HashSet<Long>()
                for (tx in transactions) {
                    if (tx.ledgerDateEpochDay <= referenceDateEpochDay) {
                        if (tx.id == 0L || seenIds.add(tx.id)) {
                            add(tx)
                        }
                    }
                }
            }

            // Opening debt is normalized to positive debt minor units.
            // If initialUnbilledAmount is specified, split initial opening debt into closed cycle debt vs active cycle unbilled spend.
            val initTotalDebtMinor = openingBalance.abs().amountMinor
            val initUnbilledMinor = (initialUnbilledAmount?.abs()?.amountMinor ?: 0L)
                .coerceAtMost(initTotalDebtMinor)

            var closedCycleNetDebtMinor: Long = Math.subtractExact(initTotalDebtMinor, initUnbilledMinor)
            var activeCycleSpendMinor: Long = initUnbilledMinor
            var activeCycleCreditMinor = 0L

            for (tx in uniqueTransactions) {
                val isClosedCycle = tx.ledgerDateEpochDay <= lastStatementCloseEpochDay
                val txMagnitude = tx.amount.abs().amountMinor
                val feeMagnitude = tx.transferFee.abs().amountMinor

                when (tx.type) {
                    TransactionType.EXPENSE -> {
                        val charge = Math.addExact(txMagnitude, feeMagnitude)
                        if (isClosedCycle) {
                            closedCycleNetDebtMinor = Math.addExact(closedCycleNetDebtMinor, charge)
                        } else {
                            activeCycleSpendMinor = Math.addExact(activeCycleSpendMinor, charge)
                        }
                    }
                    TransactionType.TRANSFER -> {
                        // A transfer originating from a credit card (cash advance) adds to debt;
                        // a transfer into the credit card acts like a payment reduction.
                        val charge = Math.addExact(txMagnitude, feeMagnitude)
                        if (isClosedCycle) {
                            closedCycleNetDebtMinor = Math.addExact(closedCycleNetDebtMinor, charge)
                        } else {
                            activeCycleSpendMinor = Math.addExact(activeCycleSpendMinor, charge)
                        }
                    }
                    TransactionType.CC_SETTLEMENT,
                    TransactionType.REFUND,
                    TransactionType.INCOME -> {
                        if (isClosedCycle) {
                            closedCycleNetDebtMinor = Math.subtractExact(closedCycleNetDebtMinor, txMagnitude)
                        } else {
                            activeCycleCreditMinor = Math.addExact(activeCycleCreditMinor, txMagnitude)
                        }
                    }
                }
            }

            // Apply any post-cutoff settlements/refunds to the closed statement balance first,
            // and apply any surplus credit to active unbilled spend so nothing is double-counted.
            val rawStatementAfterCredits = Math.subtractExact(closedCycleNetDebtMinor, activeCycleCreditMinor)
            val statementPayableMinor = rawStatementAfterCredits.coerceAtLeast(0L)

            val surplusCreditAfterStatement = if (rawStatementAfterCredits < 0L) {
                Math.negateExact(rawStatementAfterCredits)
            } else {
                0L
            }

            val unbilledAmountMinor = Math.subtractExact(
                activeCycleSpendMinor,
                surplusCreditAfterStatement
            ).coerceAtLeast(0L)

            val netDebtSignedMinor = Math.addExact(
                closedCycleNetDebtMinor,
                Math.subtractExact(activeCycleSpendMinor, activeCycleCreditMinor)
            )

            val outstandingBalanceMinor = netDebtSignedMinor.coerceAtLeast(0L)
            val currentBalanceSignedMinor = Math.negateExact(netDebtSignedMinor)

            val availableCredit: MinorMoney? = creditLimit?.let { limit ->
                val remaining = Math.subtractExact(limit.amountMinor, outstandingBalanceMinor)
                    .coerceAtLeast(0L)
                MinorMoney(remaining)
            }

            val utilizationPercent: Float = if (
                creditLimit != null &&
                creditLimit.amountMinor > 0L &&
                outstandingBalanceMinor > 0L
            ) {
                BigDecimal.valueOf(outstandingBalanceMinor)
                    .multiply(BigDecimal.valueOf(100L))
                    .divide(
                        BigDecimal.valueOf(creditLimit.amountMinor),
                        2,
                        RoundingMode.HALF_UP
                    )
                    .toFloat()
            } else {
                0f
            }

            return CreditCardMetrics(
                currentBalance = MinorMoney(currentBalanceSignedMinor),
                outstandingBalance = MinorMoney(outstandingBalanceMinor),
                statementPayable = MinorMoney(statementPayableMinor),
                unbilledAmount = MinorMoney(unbilledAmountMinor),
                availableCredit = availableCredit,
                utilizationPercent = utilizationPercent,
                lastStatementCloseEpochDay = lastStatementCloseEpochDay
            )
        }
    }
}
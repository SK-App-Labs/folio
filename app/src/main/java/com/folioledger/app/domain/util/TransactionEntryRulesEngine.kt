package com.folioledger.app.domain.util

import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.SplitLineItem
import com.folioledger.app.ui.entry.InstallmentPreviewItem
import com.folioledger.app.ui.entry.SplitValidationSummary
import java.time.LocalDate

object TransactionEntryRulesEngine {

    fun validateSplitTransaction(
        parentTotal: MinorMoney,
        splits: List<SplitLineItem>
    ): SplitValidationSummary {
        if (!parentTotal.isPositive) {
            return SplitValidationSummary(
                isValid = false,
                allocatedTotal = MinorMoney.ZERO,
                remainingUnallocated = parentTotal,
                errorMessage = "Enter a positive transaction total before splitting."
            )
        }
        if (splits.size < 2) {
            val allocated = splits.fold(MinorMoney.ZERO) { acc, item -> acc + item.amount }
            return SplitValidationSummary(
                isValid = false,
                allocatedTotal = allocated,
                remainingUnallocated = parentTotal - allocated,
                errorMessage = "A split transaction requires at least 2 split rows."
            )
        }

        var allocatedMinor = 0L
        splits.forEach { line ->
            if (line.categoryId <= 0L) {
                return SplitValidationSummary(
                    isValid = false,
                    allocatedTotal = MinorMoney(allocatedMinor),
                    remainingUnallocated = MinorMoney(parentTotal.amountMinor - allocatedMinor),
                    errorMessage = "Every split line must have a category selected."
                )
            }
            if (!line.amount.isPositive) {
                return SplitValidationSummary(
                    isValid = false,
                    allocatedTotal = MinorMoney(allocatedMinor),
                    remainingUnallocated = MinorMoney(parentTotal.amountMinor - allocatedMinor),
                    errorMessage = "Every split line amount must be strictly greater than zero."
                )
            }
            allocatedMinor = Math.addExact(allocatedMinor, line.amount.amountMinor)
        }

        val allocated = MinorMoney(allocatedMinor)
        val remaining = parentTotal - allocated
        return if (remaining.isZero) {
            SplitValidationSummary(
                isValid = true,
                allocatedTotal = allocated,
                remainingUnallocated = MinorMoney.ZERO,
                errorMessage = null
            )
        } else {
            SplitValidationSummary(
                isValid = false,
                allocatedTotal = allocated,
                remainingUnallocated = remaining,
                errorMessage = if (remaining.isPositive) {
                    "Split lines are under-allocated."
                } else {
                    "Split lines exceed the transaction total."
                }
            )
        }
    }

    fun generateInstallmentSchedule(
        totalAmount: MinorMoney,
        monthsCount: Int,
        startDateEpochDay: Long
    ): List<InstallmentPreviewItem> {
        val safeMonths = monthsCount.coerceIn(2, 60)
        if (!totalAmount.isPositive) return emptyList()

        val baseMinor = totalAmount.amountMinor / safeMonths
        val remainderMinor = totalAmount.amountMinor % safeMonths
        val startLocalDate = LocalDate.ofEpochDay(startDateEpochDay)

        return (1..safeMonths).map { index ->
            val installmentMinor = if (index == 1) {
                baseMinor + remainderMinor
            } else {
                baseMinor
            }
            val installmentDate = startLocalDate.plusMonths((index - 1).toLong())
            InstallmentPreviewItem(
                installmentIndex = index,
                totalInstallments = safeMonths,
                dueDateEpochDay = installmentDate.toEpochDay(),
                amount = MinorMoney(installmentMinor)
            )
        }
    }
}

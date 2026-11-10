// File: app/src/main/java/com/folioledger/app/domain/util/DueRemindersEvaluator.kt
package com.folioledger.app.domain.util

import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.BillDueReminderItem
import com.folioledger.app.domain.model.CardDueReminderItem
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.RecurringCommitmentItem
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object DueRemindersEvaluator {

    fun evaluateCreditCardReminders(
        accounts: List<AccountSummary>,
        todayEpochDay: Long,
        leadDaysNotice: Int = 3
    ): List<CardDueReminderItem> {
        val today = LocalDate.ofEpochDay(todayEpochDay)
        val validLeadDays = leadDaysNotice.coerceIn(0, 14).toLong()
        val results = mutableListOf<CardDueReminderItem>()

        val creditCards = accounts.filter { !it.isArchived && it.groupType == AccountGroup.CREDIT_CARD }
        for (card in creditCards) {
            val stmtDay = (card.settlementDayOfMonth ?: 1).coerceIn(1, 31)
            val dueDay = (card.paymentDueDayOfMonth ?: 20).coerceIn(1, 31)

            val cycle = CreditCardCycleCalculator.computeCycleWindow(
                referenceDateEpochDay = todayEpochDay,
                settlementDayOfMonth = stmtDay,
                paymentDueDayOfMonth = dueDay
            )

            // Calculate amount due now:
            // When creditCardStatementPayable is available, it reflects the closed cycle statement due on lastStatementDueDate.
            // If statementPayable is zero or non-positive, that statement has been settled; do not alarm the user for active cycle spend.
            val statementPayable = card.creditCardStatementPayable
            val amountDueNow = when {
                statementPayable != null -> {
                    if (statementPayable.isPositive) statementPayable else MinorMoney.ZERO
                }
                card.currentBalance.isNegative -> {
                    // Fallback only if no statement calculation exists and the card is in debt (negative balance)
                    card.currentBalance.abs()
                }
                else -> MinorMoney.ZERO
            }

            // If nothing is due, skip
            if (!amountDueNow.isPositive) continue

            // Evaluate target due date
            val targetDueDate = cycle.lastStatementDueDate
            val daysRemaining = ChronoUnit.DAYS.between(today, targetDueDate)

            val isDueSoon = daysRemaining in 0L..validLeadDays
            val isRecentlyOverdue = daysRemaining < 0L && daysRemaining >= -3L

            if (isDueSoon || isRecentlyOverdue) {
                results.add(
                    CardDueReminderItem(
                        accountId = card.id,
                        accountName = card.name,
                        currencyCode = card.currencyCode,
                        amountDue = amountDueNow,
                        dueDate = targetDueDate,
                        daysRemaining = daysRemaining,
                        isOverdue = daysRemaining < 0L
                    )
                )
            }
        }

        return results.sortedWith(
            compareBy<CardDueReminderItem> { !it.isOverdue }
                .thenBy { it.daysRemaining }
        )
    }

    fun evaluateBillReminders(
        recurringRules: List<RecurringCommitmentItem>,
        todayEpochDay: Long,
        leadDaysNotice: Int = 1
    ): List<BillDueReminderItem> {
        val today = LocalDate.ofEpochDay(todayEpochDay)
        val validLeadDays = leadDaysNotice.coerceIn(0, 7).toLong()
        val results = mutableListOf<BillDueReminderItem>()

        val activeRules = recurringRules.filter { it.isActive && it.amount.isPositive }
        for (rule in activeRules) {
            val dueDate = LocalDate.ofEpochDay(rule.nextOccurrenceEpochDay)
            val daysRemaining = ChronoUnit.DAYS.between(today, dueDate)

            if (daysRemaining in 0L..validLeadDays) {
                results.add(
                    BillDueReminderItem(
                        ruleId = rule.id,
                        title = rule.title,
                        payeeOrDescription = rule.payeeOrDescription.ifEmpty { rule.title },
                        currencyCode = rule.currencyCode,
                        amount = rule.amount,
                        dueDate = dueDate,
                        daysRemaining = daysRemaining,
                        accountName = rule.accountName
                    )
                )
            }
        }

        return results.sortedBy { it.daysRemaining }
    }
}

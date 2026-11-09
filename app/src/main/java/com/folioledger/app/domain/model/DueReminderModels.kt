// File: app/src/main/java/com/folioledger/app/domain/model/DueReminderModels.kt
package com.folioledger.app.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class CardDueReminderItem(
    val accountId: Long,
    val accountName: String,
    val currencyCode: String,
    val amountDue: MinorMoney,
    val dueDate: LocalDate,
    val daysRemaining: Long,
    val isOverdue: Boolean
)

@Immutable
data class BillDueReminderItem(
    val ruleId: Long,
    val title: String,
    val payeeOrDescription: String,
    val currencyCode: String,
    val amount: MinorMoney,
    val dueDate: LocalDate,
    val daysRemaining: Long,
    val accountName: String
)

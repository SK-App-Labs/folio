// File: app/src/main/java/com/folioledger/app/data/notification/FolioNotificationManager.kt
package com.folioledger.app.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.folioledger.app.MainActivity
import com.folioledger.app.R
import com.folioledger.app.domain.model.BillDueReminderItem
import com.folioledger.app.domain.model.CardDueReminderItem
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import java.time.format.DateTimeFormatter
import java.util.Locale

class FolioNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_CREDIT_CARDS = "folio_channel_credit_cards"
        const val CHANNEL_BILLS = "folio_channel_recurring_bills"

        const val ACTION_SETTLE_CARD = "com.folioledger.app.ACTION_SETTLE_CARD"
        const val ACTION_LOG_BILL = "com.folioledger.app.ACTION_LOG_BILL"

        const val EXTRA_NOTIFICATION_ACTION = "notification_action"
        const val EXTRA_ACCOUNT_ID = "account_id"
        const val EXTRA_RECURRING_RULE_ID = "recurring_rule_id"

        private const val NOTIFICATION_ID_CC_BASE = 70_000
        private const val NOTIFICATION_ID_BILL_BASE = 80_000

        private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return

                val ccChannel = NotificationChannel(
                    CHANNEL_CREDIT_CARDS,
                    "Credit Card Payment Dues",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts for credit card statement dues and payment reminders"
                    enableLights(true)
                    enableVibration(true)
                }

                val billsChannel = NotificationChannel(
                    CHANNEL_BILLS,
                    "Upcoming Bills & Subscriptions",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Reminders for scheduled bills and recurring subscriptions"
                }

                notificationManager.createNotificationChannel(ccChannel)
                notificationManager.createNotificationChannel(billsChannel)
            }
        }

        fun hasNotificationPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }
    }

    fun postDueReminders(
        cardReminders: List<CardDueReminderItem>,
        billReminders: List<BillDueReminderItem>,
        hideAmounts: Boolean,
        primaryCurrencyCode: String,
        grouping: NumberGroupingSystem
    ) {
        if (!hasNotificationPermission(context)) return

        val managerCompat = NotificationManagerCompat.from(context)

        // 1. Post Credit Card Due Reminders
        for (alert in cardReminders) {
            val notificationId = NOTIFICATION_ID_CC_BASE + (alert.accountId % 9999).toInt()
            val spec = SupportedCurrencies.fromCode(alert.currencyCode)
            val formattedAmount = MoneyFormatterAndCalculator.formatMinorMoney(
                alert.amountDue,
                spec,
                grouping
            )

            val title = when {
                alert.isOverdue -> "Overdue: ${alert.accountName} Bill"
                alert.daysRemaining == 0L -> "Due Today: ${alert.accountName} Bill"
                alert.daysRemaining == 1L -> "${alert.accountName} Due Tomorrow"
                else -> "${alert.accountName} Due in ${alert.daysRemaining} Days"
            }

            val body = if (hideAmounts) {
                "Due on ${alert.dueDate.format(dateFormatter)}. Settle now to preserve your grace period."
            } else {
                "Statement balance of $formattedAmount is due on ${alert.dueDate.format(dateFormatter)}."
            }

            // Settle Intent
            val settleIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                action = ACTION_SETTLE_CARD
                putExtra(EXTRA_NOTIFICATION_ACTION, ACTION_SETTLE_CARD)
                putExtra(EXTRA_ACCOUNT_ID, alert.accountId)
            }
            val settlePendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                settleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_CREDIT_CARDS)
                .setSmallIcon(R.drawable.ic_notification_reminder)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(settlePendingIntent)
                .addAction(
                    R.drawable.ic_shortcut_transfer,
                    "Settle Now",
                    settlePendingIntent
                )
                .build()

            managerCompat.notify(notificationId, notification)
        }

        // 2. Post Recurring Bill Reminders
        for (bill in billReminders) {
            val notificationId = NOTIFICATION_ID_BILL_BASE + (bill.ruleId % 9999).toInt()
            val spec = SupportedCurrencies.fromCode(bill.currencyCode)
            val formattedAmount = MoneyFormatterAndCalculator.formatMinorMoney(
                bill.amount,
                spec,
                grouping
            )

            val title = when {
                bill.isOverdue -> "Overdue Bill: ${bill.title}"
                bill.daysRemaining == 0L -> "Upcoming Bill: ${bill.title} (Today)"
                bill.daysRemaining == 1L -> "Upcoming Bill: ${bill.title} (Tomorrow)"
                else -> "Upcoming Bill: ${bill.title} (in ${bill.daysRemaining} days)"
            }

            val body = if (hideAmounts) {
                "Due from ${bill.accountName} on ${bill.dueDate.format(dateFormatter)}."
            } else {
                "$formattedAmount due from ${bill.accountName} on ${bill.dueDate.format(dateFormatter)}."
            }

            val billIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                action = ACTION_LOG_BILL
                putExtra(EXTRA_NOTIFICATION_ACTION, ACTION_LOG_BILL)
                putExtra(EXTRA_RECURRING_RULE_ID, bill.ruleId)
            }
            val billPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                billIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_BILLS)
                .setSmallIcon(R.drawable.ic_notification_reminder)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(billPendingIntent)
                .addAction(
                    R.drawable.ic_shortcut_expense,
                    "Log Payment",
                    billPendingIntent
                )
                .build()

            managerCompat.notify(notificationId, notification)
        }
    }
}

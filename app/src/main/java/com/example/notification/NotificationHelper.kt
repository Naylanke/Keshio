package com.example.notification

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
import com.example.MainActivity
import com.example.R
import com.example.data.model.TransactionType
import com.example.util.DateTimeUtils

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_BUDGET_ALERTS = "keshio_budget_alerts"
        const val CHANNEL_TRANSACTIONS = "keshio_transactions"
        const val CHANNEL_DAILY_SUMMARY = "keshio_daily_summary"

        const val NOTIFICATION_ID_APPROACHING = 1001
        const val NOTIFICATION_ID_EXCEEDED = 1002
        const val NOTIFICATION_ID_TRANSACTION = 1003
        const val NOTIFICATION_ID_SUMMARY = 1004

        @Volatile
        private var INSTANCE: NotificationHelper? = null

        fun getInstance(context: Context): NotificationHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = NotificationHelper(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            // 1. Budget Alerts Channel
            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                "Budget & Spending Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when approaching or exceeding your daily budget limits"
                enableVibration(true)
                setShowBadge(true)
            }

            // 2. Transaction Detections Channel
            val txChannel = NotificationChannel(
                CHANNEL_TRANSACTIONS,
                "Financial Transaction Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when new financial transactions are detected"
                enableVibration(false)
                setShowBadge(true)
            }

            // 3. Daily Summary Channel
            val summaryChannel = NotificationChannel(
                CHANNEL_DAILY_SUMMARY,
                "End-of-Day Budget Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily spending recap and remaining budget overview"
                enableVibration(false)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(
                listOf(budgetChannel, txChannel, summaryChannel)
            )
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    private fun getContentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Approaching Limit Notification (~80% spent)
     * «🟠 Keshio
     * You're getting close to today's spending target.
     * KSh 200 remaining.»
     */
    fun showApproachingLimitNotification(
        remainingAmount: Double,
        dailyTarget: Double,
        currencySymbol: String = "KSh"
    ) {
        if (!hasNotificationPermission()) return

        val formattedRemaining = DateTimeUtils.formatCurrency(remainingAmount, currencySymbol)
        val title = "🟠 Keshio"
        val message = "You're getting close to today's spending target.\n$formattedRemaining remaining."

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText("You're getting close to today's spending target. $formattedRemaining remaining.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(getContentIntent())
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_APPROACHING, notification)
    }

    /**
     * Target Exceeded Notification (100%+ spent)
     * «🔴 Keshio
     * Today's spending target has been exceeded by KSh 120.»
     */
    fun showTargetExceededNotification(
        exceededAmount: Double,
        currencySymbol: String = "KSh"
    ) {
        if (!hasNotificationPermission()) return

        val formattedExceeded = DateTimeUtils.formatCurrency(exceededAmount, currencySymbol)
        val title = "🔴 Keshio"
        val message = "Today's spending target has been exceeded by $formattedExceeded."

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(getContentIntent())
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_EXCEEDED, notification)
    }

    /**
     * Transaction Detected Notification
     * «KSh 500 spent
     * Today's spending: KSh 850 / KSh 1,000
     * KSh 150 remaining.»
     */
    fun showTransactionNotification(
        amount: Double,
        type: TransactionType,
        party: String,
        todaySpent: Double,
        dailyTarget: Double,
        remainingAmount: Double,
        currencySymbol: String = "KSh",
        isDetailed: Boolean = false
    ) {
        if (!hasNotificationPermission()) return

        val formattedAmount = DateTimeUtils.formatCurrency(amount, currencySymbol)
        val formattedSpent = DateTimeUtils.formatCurrency(todaySpent, currencySymbol)
        val formattedTarget = DateTimeUtils.formatCurrency(dailyTarget, currencySymbol)
        val formattedRemaining = DateTimeUtils.formatCurrency(remainingAmount, currencySymbol)

        val title: String
        val message: String

        if (isDetailed) {
            val actionWord = if (type == TransactionType.INCOME) "received from $party" else "spent ($party)"
            title = "$formattedAmount $actionWord"
            message = "Today's spending: $formattedSpent / $formattedTarget\n$formattedRemaining remaining."
        } else {
            title = "✅ New transaction detected"
            message = "Keshio updated your spending summary."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_TRANSACTIONS)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(title)
            .setContentText(if (isDetailed) "Today: $formattedSpent / $formattedTarget • $formattedRemaining remaining" else "Tap to view spending summary in Keshio")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(getContentIntent())
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TRANSACTION, notification)
    }

    /**
     * End-of-Day Summary Notification
     * «Keshio — Today's Summary
     * Received: KSh 3,000
     * Spent: KSh 850
     * Target: KSh 1,000
     * Remaining: KSh 150»
     * Or if exceeded:
     * «You exceeded today's target by KSh 180.»
     */
    fun showEndOfDaySummaryNotification(
        todayReceived: Double,
        todaySpent: Double,
        dailyTarget: Double,
        currencySymbol: String = "KSh"
    ) {
        if (!hasNotificationPermission()) return

        val formattedReceived = DateTimeUtils.formatCurrency(todayReceived, currencySymbol)
        val formattedSpent = DateTimeUtils.formatCurrency(todaySpent, currencySymbol)
        val formattedTarget = DateTimeUtils.formatCurrency(dailyTarget, currencySymbol)

        val title = "Keshio — Today's Summary"
        val message = if (todaySpent > dailyTarget) {
            val exceeded = todaySpent - dailyTarget
            val formattedExceeded = DateTimeUtils.formatCurrency(exceeded, currencySymbol)
            "Received: $formattedReceived • Spent: $formattedSpent\nTarget: $formattedTarget\nYou exceeded today's target by $formattedExceeded."
        } else {
            val remaining = (dailyTarget - todaySpent).coerceAtLeast(0.0)
            val formattedRemaining = DateTimeUtils.formatCurrency(remaining, currencySymbol)
            "Received: $formattedReceived • Spent: $formattedSpent\nTarget: $formattedTarget • Remaining: $formattedRemaining"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_SUMMARY)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("Spent: $formattedSpent | Target: $formattedTarget")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(getContentIntent())
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SUMMARY, notification)
    }
}

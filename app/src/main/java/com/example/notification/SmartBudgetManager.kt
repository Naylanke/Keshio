package com.example.notification

import android.content.Context
import com.example.data.local.TransactionEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.model.TransactionType
import com.example.data.repository.KeshioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SmartBudgetManager(
    private val context: Context,
    private val repository: KeshioRepository
) {
    private val notificationHelper = NotificationHelper.getInstance(context)

    companion object {
        @Volatile
        private var INSTANCE: SmartBudgetManager? = null

        fun getInstance(context: Context, repository: KeshioRepository): SmartBudgetManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SmartBudgetManager(context.applicationContext, repository)
                INSTANCE = instance
                instance
            }
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    /**
     * Evaluates budget and sends smart alerts when a new transaction is recorded or modified.
     */
    suspend fun onTransactionRecorded(
        transaction: TransactionEntity,
        isAutomaticDetection: Boolean = true
    ) = withContext(Dispatchers.IO) {
        val settings = repository.userSettings.first() ?: UserSettingsEntity()
        val allTransactions = repository.allTransactions.first()

        // Calculate today's metrics
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis

        var todayReceived = 0.0
        var todaySpent = 0.0

        allTransactions.filter { it.timestamp >= startOfToday }.forEach { tx ->
            if (tx.type == TransactionType.INCOME.name) {
                todayReceived += tx.amount
            } else {
                todaySpent += tx.amount
            }
        }

        val dailyTarget = settings.dailyTarget
        val dailyRemaining = (dailyTarget - todaySpent).coerceAtLeast(0.0)
        val currency = settings.currencySymbol
        val todayStr = getTodayDateString()

        // 1. Transaction Notification (if enabled and requested)
        if (settings.transactionNotificationsEnabled && isAutomaticDetection) {
            val txType = try {
                TransactionType.valueOf(transaction.type)
            } catch (_: Exception) {
                TransactionType.EXPENSE
            }
            notificationHelper.showTransactionNotification(
                amount = transaction.amount,
                type = txType,
                party = transaction.title,
                todaySpent = todaySpent,
                dailyTarget = dailyTarget,
                remainingAmount = dailyRemaining,
                currencySymbol = currency
            )
        }

        // 2. Budget Warnings (if enabled)
        if (settings.budgetWarningsEnabled && dailyTarget > 0) {
            val percentageUsed = (todaySpent / dailyTarget) * 100

            // Exceeded threshold: >= 100%
            if (todaySpent >= dailyTarget) {
                if (settings.lastExceededAlertDate != todayStr) {
                    val exceededAmount = todaySpent - dailyTarget
                    notificationHelper.showTargetExceededNotification(
                        exceededAmount = exceededAmount,
                        currencySymbol = currency
                    )
                    // Update cooldown date so we don't spam for subsequent transactions today
                    repository.updateLastExceededAlertDate(todayStr)
                }
            }
            // Approaching threshold: 70% to 99% (e.g. ~80%)
            else if (percentageUsed >= 70.0) {
                if (settings.lastApproachingAlertDate != todayStr) {
                    notificationHelper.showApproachingLimitNotification(
                        remainingAmount = dailyRemaining,
                        dailyTarget = dailyTarget,
                        currencySymbol = currency
                    )
                    // Update cooldown date
                    repository.updateLastApproachingAlertDate(todayStr)
                }
            }
        }
    }

    /**
     * Triggers End-of-Day Summary notification.
     */
    suspend fun triggerDailySummary() = withContext(Dispatchers.IO) {
        val settings = repository.userSettings.first() ?: UserSettingsEntity()
        if (!settings.endOfDaySummaryEnabled) return@withContext

        val allTransactions = repository.allTransactions.first()
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis

        var todayReceived = 0.0
        var todaySpent = 0.0

        allTransactions.filter { it.timestamp >= startOfToday }.forEach { tx ->
            if (tx.type == TransactionType.INCOME.name) {
                todayReceived += tx.amount
            } else {
                todaySpent += tx.amount
            }
        }

        val todayStr = getTodayDateString()
        notificationHelper.showEndOfDaySummaryNotification(
            todayReceived = todayReceived,
            todaySpent = todaySpent,
            dailyTarget = settings.dailyTarget,
            currencySymbol = settings.currencySymbol
        )
        repository.updateLastSummaryDate(todayStr)
    }

    /**
     * Diagnostic / Developer testing methods to preview notifications directly
     */
    suspend fun testApproachingNotification() = withContext(Dispatchers.IO) {
        val settings = repository.userSettings.first() ?: UserSettingsEntity()
        val target = settings.dailyTarget
        val remaining = (target * 0.20).coerceAtLeast(200.0)
        notificationHelper.showApproachingLimitNotification(
            remainingAmount = remaining,
            dailyTarget = target,
            currencySymbol = settings.currencySymbol
        )
    }

    suspend fun testExceededNotification() = withContext(Dispatchers.IO) {
        val settings = repository.userSettings.first() ?: UserSettingsEntity()
        notificationHelper.showTargetExceededNotification(
            exceededAmount = 120.0,
            currencySymbol = settings.currencySymbol
        )
    }

    suspend fun testTransactionNotification() = withContext(Dispatchers.IO) {
        val settings = repository.userSettings.first() ?: UserSettingsEntity()
        notificationHelper.showTransactionNotification(
            amount = 500.0,
            type = TransactionType.EXPENSE,
            party = "Naivas Supermarket",
            todaySpent = 850.0,
            dailyTarget = settings.dailyTarget,
            remainingAmount = (settings.dailyTarget - 850.0).coerceAtLeast(0.0),
            currencySymbol = settings.currencySymbol
        )
    }

    suspend fun testSummaryNotification() = withContext(Dispatchers.IO) {
        val settings = repository.userSettings.first() ?: UserSettingsEntity()
        notificationHelper.showEndOfDaySummaryNotification(
            todayReceived = 3000.0,
            todaySpent = 850.0,
            dailyTarget = settings.dailyTarget,
            currencySymbol = settings.currencySymbol
        )
    }
}

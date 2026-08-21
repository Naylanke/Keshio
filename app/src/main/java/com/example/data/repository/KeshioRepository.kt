package com.example.data.repository

import com.example.data.local.SavingsGoalDao
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.local.UserSettingsDao
import com.example.data.local.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

class KeshioRepository(
    private val transactionDao: TransactionDao,
    private val userSettingsDao: UserSettingsDao,
    private val savingsGoalDao: SavingsGoalDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val userSettings: Flow<UserSettingsEntity?> = userSettingsDao.getUserSettings()
    val allGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals()
    val activeGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.getActiveGoals()

    suspend fun insertGoal(goal: SavingsGoalEntity): Long {
        return savingsGoalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.deleteGoal(goal)
    }

    suspend fun addMoneyToGoal(goalId: Long, amount: Double) {
        // Retrieve active goals and update
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }

    suspend fun updateDailyTarget(target: Double) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(current.copy(dailyTarget = target))
    }

    suspend fun updateMonthlyTarget(target: Double) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(current.copy(monthlyTarget = target))
    }

    suspend fun updateCurrency(symbol: String) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(current.copy(currencySymbol = symbol))
    }

    suspend fun updateThemeMode(theme: String) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(current.copy(themeMode = theme))
    }

    suspend fun getTransactionByReferenceId(referenceId: String): TransactionEntity? {
        return transactionDao.getTransactionByReferenceId(referenceId)
    }

    suspend fun getTransactionByFingerprint(fingerprint: String): TransactionEntity? {
        return transactionDao.getTransactionByFingerprint(fingerprint)
    }

    suspend fun updateSmsTracking(enabled: Boolean) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(current.copy(smsTrackingEnabled = enabled))
    }

    suspend fun updateBudgetWarnings(enabled: Boolean) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(current.copy(budgetWarningsEnabled = enabled))
    }

    suspend fun updateTransactionNotifications(enabled: Boolean) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(current.copy(transactionNotificationsEnabled = enabled))
    }

    suspend fun updateEndOfDaySummary(enabled: Boolean) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(current.copy(endOfDaySummaryEnabled = enabled))
    }

    suspend fun updateLastApproachingAlertDate(dateStr: String) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(current.copy(lastApproachingAlertDate = dateStr))
    }

    suspend fun updateLastExceededAlertDate(dateStr: String) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(current.copy(lastExceededAlertDate = dateStr))
    }

    suspend fun updateLastSummaryDate(dateStr: String) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(current.copy(lastSummaryDate = dateStr))
    }

    suspend fun initializeDefaultDataIfNeeded() {
        var settings = userSettingsDao.getUserSettingsDirect()
        if (settings == null) {
            settings = UserSettingsEntity(
                id = 1,
                dailyTarget = 1500.0,
                monthlyTarget = 45000.0,
                currencySymbol = "KSh",
                themeMode = "SYSTEM",
                sampleDataInitialized = true,
                smsTrackingEnabled = false,
                testModeEnabled = false,
                budgetWarningsEnabled = true,
                transactionNotificationsEnabled = true,
                endOfDaySummaryEnabled = true,
                detailedNotificationsEnabled = false,
                isAppLockEnabled = false,
                appLockType = "NONE",
                pinHash = "",
                appLockTiming = "FOREGROUND",
                onboardingCompleted = false
            )
            userSettingsDao.insertOrUpdateSettings(settings)
        }
    }

    suspend fun updateDetailedNotifications(enabled: Boolean) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(current.copy(detailedNotificationsEnabled = enabled))
    }

    suspend fun updateAppLock(
        enabled: Boolean,
        lockType: String,
        pinHash: String = "",
        timing: String = "FOREGROUND"
    ) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(
            current.copy(
                isAppLockEnabled = enabled,
                appLockType = lockType,
                pinHash = if (pinHash.isNotBlank()) pinHash else current.pinHash,
                appLockTiming = timing
            )
        )
    }

    suspend fun updateOnboardingCompleted(completed: Boolean) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(current.copy(onboardingCompleted = completed))
    }

    suspend fun clearAllTransactionsOnly() {
        transactionDao.deleteAllTransactions()
    }

    suspend fun deleteAllData() {
        transactionDao.deleteAllTransactions()
        savingsGoalDao.deleteAllGoals()
        val freshSettings = UserSettingsEntity(
            id = 1,
            dailyTarget = 1500.0,
            monthlyTarget = 45000.0,
            currencySymbol = "KSh",
            themeMode = "SYSTEM",
            sampleDataInitialized = true,
            smsTrackingEnabled = false,
            testModeEnabled = false,
            budgetWarningsEnabled = true,
            transactionNotificationsEnabled = true,
            endOfDaySummaryEnabled = true
        )
        userSettingsDao.insertOrUpdateSettings(freshSettings)
    }
}

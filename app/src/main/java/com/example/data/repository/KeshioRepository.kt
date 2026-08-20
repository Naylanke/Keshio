package com.example.data.repository

import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.local.UserSettingsDao
import com.example.data.local.UserSettingsEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class KeshioRepository(
    private val transactionDao: TransactionDao,
    private val userSettingsDao: UserSettingsDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val userSettings: Flow<UserSettingsEntity?> = userSettingsDao.getUserSettings()

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

    suspend fun updateTestMode(enabled: Boolean) {
        val current = userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
        userSettingsDao.insertOrUpdateSettings(current.copy(testModeEnabled = enabled))
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
                sampleDataInitialized = false,
                smsTrackingEnabled = false,
                testModeEnabled = false,
                budgetWarningsEnabled = true,
                transactionNotificationsEnabled = true,
                endOfDaySummaryEnabled = true
            )
            userSettingsDao.insertOrUpdateSettings(settings)
        }

        val count = transactionDao.getTransactionCount()
        if (count == 0 && !settings.sampleDataInitialized) {
            seedSampleTransactions()
            userSettingsDao.insertOrUpdateSettings(settings.copy(sampleDataInitialized = true))
        }
    }

    suspend fun restoreSampleData() {
        transactionDao.deleteAllTransactions()
        seedSampleTransactions()
    }

    private suspend fun seedSampleTransactions() {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // Today timestamps
        calendar.timeInMillis = now
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 30)
        val todayMorning = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 11)
        calendar.set(Calendar.MINUTE, 15)
        val todayMidday = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 13)
        calendar.set(Calendar.MINUTE, 20)
        val todayLunch = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 15)
        calendar.set(Calendar.MINUTE, 45)
        val todayAfternoon = calendar.timeInMillis

        // Yesterday timestamps
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        val yesterdayMorning = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 14)
        calendar.set(Calendar.MINUTE, 30)
        val yesterdayAfternoon = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 17)
        calendar.set(Calendar.MINUTE, 10)
        val yesterdayEvening = calendar.timeInMillis

        // 2 days ago
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -2)
        calendar.set(Calendar.HOUR_OF_DAY, 10)
        calendar.set(Calendar.MINUTE, 0)
        val day2Morning = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 19)
        calendar.set(Calendar.MINUTE, 30)
        val day2Dinner = calendar.timeInMillis

        // 4 days ago
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -4)
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 15)
        val day4Noon = calendar.timeInMillis

        // 6 days ago
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        val day6Salary = calendar.timeInMillis

        val sampleList = listOf(
            TransactionEntity(
                title = "Java House Espresso & Croissant",
                amount = 480.00,
                type = TransactionType.EXPENSE.name,
                category = "Food & Dining",
                note = "Ref: QA12BC34DA • Java House CBD",
                timestamp = todayMorning,
                referenceId = "QA12BC34DA",
                source = "MANUAL"
            ),
            TransactionEntity(
                title = "Client Design Milestone Payout",
                amount = 15000.00,
                type = TransactionType.INCOME.name,
                category = "Freelance & Business",
                note = "Ref: QA12BC34DB • Mobile UI Prototype",
                timestamp = todayMidday,
                referenceId = "QA12BC34DB",
                source = "MANUAL"
            ),
            TransactionEntity(
                title = "Super Metro Commute",
                amount = 120.00,
                type = TransactionType.EXPENSE.name,
                category = "Transport",
                note = "Daily CBD transit fare",
                timestamp = todayLunch,
                source = "MANUAL"
            ),
            TransactionEntity(
                title = "Quickmart Supermarket",
                amount = 1240.00,
                type = TransactionType.EXPENSE.name,
                category = "Groceries",
                note = "Ref: QA12BC34DC • Fresh groceries & pantry",
                timestamp = todayAfternoon,
                referenceId = "QA12BC34DC",
                source = "MANUAL"
            ),
            TransactionEntity(
                title = "Supermarket Weekly Supplies",
                amount = 3650.00,
                type = TransactionType.EXPENSE.name,
                category = "Groceries",
                note = "Naivas Supermarket pantry refill",
                timestamp = yesterdayMorning,
                source = "MANUAL"
            ),
            TransactionEntity(
                title = "Safaricom Airtime & Data",
                amount = 500.00,
                type = TransactionType.EXPENSE.name,
                category = "Bills & Utilities",
                note = "Monthly data bundle top-up",
                timestamp = yesterdayAfternoon,
                source = "MANUAL"
            ),
            TransactionEntity(
                title = "KPLC Prepaid Electricity",
                amount = 2500.00,
                type = TransactionType.EXPENSE.name,
                category = "Bills & Utilities",
                note = "Ref: QA12BC34DD • Tokens purchase",
                timestamp = yesterdayEvening,
                referenceId = "QA12BC34DD",
                source = "MANUAL"
            ),
            TransactionEntity(
                title = "Consulting Retainer Transfer",
                amount = 25000.00,
                type = TransactionType.INCOME.name,
                category = "Salary & Wages",
                note = "Direct M-Pesa business transfer",
                timestamp = day2Morning,
                source = "MANUAL"
            ),
            TransactionEntity(
                title = "Artcaffe Dinner with Friends",
                amount = 2850.00,
                type = TransactionType.EXPENSE.name,
                category = "Food & Dining",
                note = "Weekend gathering",
                timestamp = day2Dinner,
                source = "MANUAL"
            ),
            TransactionEntity(
                title = "Goodlife Pharmacy Wellness",
                amount = 1450.00,
                type = TransactionType.EXPENSE.name,
                category = "Health & Medical",
                note = "Vitamins & supplements",
                timestamp = day4Noon,
                source = "MANUAL"
            ),
            TransactionEntity(
                title = "Monthly Salary Advance",
                amount = 85000.00,
                type = TransactionType.INCOME.name,
                category = "Salary & Wages",
                note = "Primary earnings deposit",
                timestamp = day6Salary,
                source = "MANUAL"
            )
        )

        transactionDao.insertTransactions(sampleList)
    }
}

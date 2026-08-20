package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val dailyTarget: Double = 1500.0,
    val monthlyTarget: Double = 45000.0,
    val currencySymbol: String = "KSh",
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val sampleDataInitialized: Boolean = false,
    val smsTrackingEnabled: Boolean = false,
    val testModeEnabled: Boolean = false,
    
    // Phase 3: Smart Budget Alerts & Notifications Settings
    val budgetWarningsEnabled: Boolean = true, // Approaching (80%) and Exceeded (100%) alerts
    val transactionNotificationsEnabled: Boolean = true, // Real-time notification when transaction is detected
    val endOfDaySummaryEnabled: Boolean = true, // Optional end of day budget summary
    val notificationCooldownMinutes: Int = 30, // Minimum delay between alerts (plus 1/day limit for exceeded)
    val lastApproachingAlertDate: String = "", // e.g. "2026-08-20"
    val lastExceededAlertDate: String = "", // e.g. "2026-08-20"
    val lastSummaryDate: String = "" // e.g. "2026-08-20"
)



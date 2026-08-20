package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.KeshioUiState
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusGreen
import com.example.util.DateTimeUtils

@Composable
fun SettingsScreen(
    uiState: KeshioUiState,
    onUpdateDailyTarget: (Double) -> Unit,
    onUpdateMonthlyTarget: (Double) -> Unit,
    onUpdateCurrency: (String) -> Unit,
    onUpdateThemeMode: (String) -> Unit,
    onUpdateSmsTracking: (Boolean) -> Unit,
    onUpdateTestMode: (Boolean) -> Unit,
    onUpdateBudgetWarnings: (Boolean) -> Unit,
    onUpdateTransactionNotifications: (Boolean) -> Unit,
    onUpdateEndOfDaySummary: (Boolean) -> Unit,
    onOpenSmsSimulator: () -> Unit,
    onRestoreSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    onDeleteAllKeshioData: () -> Unit = onClearAllData,
    onUpdateDetailedNotifications: (Boolean) -> Unit = {},
    onUpdateAppLock: (enabled: Boolean, lockType: String, pin: String, timing: String) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currency = uiState.userSettings.currencySymbol

    var showDailyTargetDialog by remember { mutableStateOf(false) }
    var showMonthlyTargetDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showDeleteAllKeshioDialog by remember { mutableStateOf(false) }
    var showRestoreSampleDialog by remember { mutableStateOf(false) }
    var showAppLockDialog by remember { mutableStateOf(false) }
    var showSetPinDialog by remember { mutableStateOf(false) }

    // Dialog for Daily Spending Target
    if (showDailyTargetDialog) {
        var tempDaily by remember { mutableStateOf(uiState.dailyTarget.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showDailyTargetDialog = false },
            title = { Text("Set Daily Spending Target") },
            text = {
                Column {
                    Text(
                        "Set your maximum desired spending limit for each day.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempDaily,
                        onValueChange = { tempDaily = it },
                        label = { Text("Daily Target ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_daily_target")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = tempDaily.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            onUpdateDailyTarget(parsed)
                        }
                        showDailyTargetDialog = false
                    },
                    modifier = Modifier.testTag("save_daily_target_btn")
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDailyTargetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog for Monthly Spending Target
    if (showMonthlyTargetDialog) {
        var tempMonthly by remember { mutableStateOf(uiState.monthlyTarget.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showMonthlyTargetDialog = false },
            title = { Text("Set Monthly Spending Target") },
            text = {
                Column {
                    Text(
                        "Set your monthly overall budget target.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempMonthly,
                        onValueChange = { tempMonthly = it },
                        label = { Text("Monthly Target ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_monthly_target")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = tempMonthly.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            onUpdateMonthlyTarget(parsed)
                        }
                        showMonthlyTargetDialog = false
                    },
                    modifier = Modifier.testTag("save_monthly_target_btn")
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMonthlyTargetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Currency Picker Dialog
    if (showCurrencyDialog) {
        val currencies = listOf(
            "$" to "US Dollar ($)",
            "KSh" to "Kenyan Shilling (KSh)",
            "€" to "Euro (€)",
            "£" to "British Pound (£)",
            "₹" to "Indian Rupee (₹)",
            "¥" to "Japanese Yen (¥)",
            "₦" to "Nigerian Naira (₦)",
            "₵" to "Ghanaian Cedi (₵)",
            "R" to "South African Rand (R)",
            "C$" to "Canadian Dollar (C$)"
        )

        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                Column {
                    currencies.forEach { (symbol, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onUpdateCurrency(symbol)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = symbol,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(50.dp)
                            )
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Delete All Keshio Data Confirmation Dialog
    if (showDeleteAllKeshioDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllKeshioDialog = false },
            title = { Text("Delete All Keshio Data?") },
            text = {
                Column {
                    Text(
                        "This will permanently delete your transactions, budgets, insights, goals and settings from this device.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Keshio will return to a clean initial state. Real SMS messages on your phone will NOT be deleted.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAllKeshioData()
                        showDeleteAllKeshioDialog = false
                    },
                    modifier = Modifier.testTag("confirm_delete_all_keshio_data_btn")
                ) {
                    Text("Delete All Data", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllKeshioDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Restore Sample Data Confirmation Dialog
    if (showRestoreSampleDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreSampleDialog = false },
            title = { Text("Restore Sample Transactions?") },
            text = { Text("This will replace current transactions with realistic sample financial data for testing.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRestoreSampleData()
                        showRestoreSampleDialog = false
                    },
                    modifier = Modifier.testTag("confirm_restore_sample_btn")
                ) {
                    Text("Restore", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreSampleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // App Lock Security Configuration Dialog
    if (showAppLockDialog) {
        val isBiometricSupported = remember(context) { com.example.security.SecurityUtils.canAuthenticateWithBiometrics(context) }
        var tempEnabled by remember { mutableStateOf(uiState.userSettings.isAppLockEnabled) }
        var tempType by remember { mutableStateOf(if (uiState.userSettings.appLockType == "NONE") "PIN" else uiState.userSettings.appLockType) }
        var tempTiming by remember { mutableStateOf(uiState.userSettings.appLockTiming) }

        AlertDialog(
            onDismissRequest = { showAppLockDialog = false },
            title = { Text("App Lock Security Settings") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable App Lock", fontWeight = FontWeight.Bold)
                        Switch(
                            checked = tempEnabled,
                            onCheckedChange = { tempEnabled = it },
                            modifier = Modifier.testTag("app_lock_enable_switch")
                        )
                    }

                    if (tempEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Lock Method:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = tempType == "PIN",
                                onClick = { tempType = "PIN" },
                                modifier = Modifier.testTag("app_lock_type_pin")
                            )
                            Text("PIN Code", modifier = Modifier.clickable { tempType = "PIN" })
                        }

                        if (isBiometricSupported) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = tempType == "BIOMETRIC",
                                    onClick = { tempType = "BIOMETRIC" },
                                    modifier = Modifier.testTag("app_lock_type_biometric")
                                )
                                Text("Biometrics / Fingerprint", modifier = Modifier.clickable { tempType = "BIOMETRIC" })
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Lock Timing:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))

                        val timings = listOf(
                            "IMMEDIATE" to "Immediately",
                            "FOREGROUND" to "When leaving foreground (Default)",
                            "ONE_MIN" to "After 1 minute in background",
                            "FIVE_MIN" to "After 5 minutes in background"
                        )

                        timings.forEach { (key, label) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = tempTiming == key,
                                    onClick = { tempTiming = key },
                                    modifier = Modifier.testTag("app_lock_timing_$key")
                                )
                                Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.clickable { tempTiming = key })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempEnabled && tempType == "PIN" && uiState.userSettings.pinHash.isBlank()) {
                            showAppLockDialog = false
                            showSetPinDialog = true
                        } else {
                            onUpdateAppLock(tempEnabled, if (tempEnabled) tempType else "NONE", "", tempTiming)
                            showAppLockDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_app_lock_settings_btn")
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppLockDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Set PIN Dialog
    if (showSetPinDialog) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showSetPinDialog = false },
            title = { Text("Set 4-Digit Secret PIN") },
            text = {
                Column {
                    Text("Enter a 4 to 6 digit secret PIN to protect Keshio.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 6) newPin = it.filter { c -> c.isDigit() } },
                        label = { Text("New PIN") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_pin_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 6) confirmPin = it.filter { c -> c.isDigit() } },
                        label = { Text("Confirm PIN") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_pin_input")
                    )

                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(pinError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPin.length < 4) {
                            pinError = "PIN must be at least 4 digits"
                        } else if (newPin != confirmPin) {
                            pinError = "PINs do not match"
                        } else {
                            onUpdateAppLock(true, "PIN", newPin, uiState.userSettings.appLockTiming)
                            showSetPinDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_set_pin_btn")
                ) {
                    Text("Set PIN", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Manage budgets, preferences & privacy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Security & App Lock Section
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(title = "Security & App Protection")
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingItem(
                        icon = Icons.Default.Lock,
                        title = "App Lock Protection",
                        subtitle = if (uiState.userSettings.isAppLockEnabled) "Enabled (${uiState.userSettings.appLockType})" else "Disabled",
                        onClick = { showAppLockDialog = true },
                        testTag = "setting_app_lock"
                    )

                    if (uiState.userSettings.isAppLockEnabled && uiState.userSettings.appLockType == "PIN") {
                        SettingDivider()
                        SettingItem(
                            icon = Icons.Default.Key,
                            title = "Change Secret PIN",
                            subtitle = "Update your 4-digit access code",
                            onClick = { showSetPinDialog = true },
                            testTag = "setting_change_pin"
                        )
                    }
                }
            }
        }

        // Notifications & Alerts Section
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(title = "Notifications & Privacy")
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingToggleItem(
                        title = "Budget Warnings",
                        subtitle = "Alerts when approaching or exceeding daily target",
                        checked = uiState.userSettings.budgetWarningsEnabled,
                        onCheckedChange = { onUpdateBudgetWarnings(it) }
                    )
                    SettingDivider()
                    SettingToggleItem(
                        title = "Transaction Notifications",
                        subtitle = "Alerts when a new financial transaction is detected",
                        checked = uiState.userSettings.transactionNotificationsEnabled,
                        onCheckedChange = { onUpdateTransactionNotifications(it) }
                    )
                    SettingDivider()
                    SettingToggleItem(
                        title = "Hide Financial Info on Lock Screen",
                        subtitle = "Mask transaction amounts and merchant names in notifications",
                        checked = !uiState.userSettings.detailedNotificationsEnabled,
                        onCheckedChange = { onUpdateDetailedNotifications(!it) }
                    )
                    SettingDivider()
                    SettingToggleItem(
                        title = "End-of-Day Summary",
                        subtitle = "Recap of today's spending and remaining budget",
                        checked = uiState.userSettings.endOfDaySummaryEnabled,
                        onCheckedChange = { onUpdateEndOfDaySummary(it) }
                    )
                }
            }
        }

        // Automatic SMS Tracking Section
        item {
            SectionHeader(title = "Automatic Financial SMS Detection")
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                com.example.ui.components.SmsTrackingCard(
                    isTrackingEnabled = uiState.userSettings.smsTrackingEnabled,
                    onTrackingToggled = { onUpdateSmsTracking(it) },
                    onOpenTestSimulator = onOpenSmsSimulator
                )
            }
        }

        // Budget Settings Section
        item {
            SectionHeader(title = "Budgeting Targets")
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingItem(
                        icon = Icons.Default.Paid,
                        title = "Daily Spending Target",
                        subtitle = "Current: ${DateTimeUtils.formatCurrency(uiState.dailyTarget, currency)}",
                        onClick = { showDailyTargetDialog = true },
                        testTag = "setting_daily_target"
                    )

                    SettingDivider()

                    SettingItem(
                        icon = Icons.Default.CalendarMonth,
                        title = "Monthly Spending Target",
                        subtitle = "Current: ${DateTimeUtils.formatCurrency(uiState.monthlyTarget, currency)}",
                        onClick = { showMonthlyTargetDialog = true },
                        testTag = "setting_monthly_target"
                    )

                    SettingDivider()

                    SettingItem(
                        icon = Icons.Default.AttachMoney,
                        title = "Currency Symbol",
                        subtitle = "Current: $currency",
                        onClick = { showCurrencyDialog = true },
                        testTag = "setting_currency"
                    )
                }
            }
        }

        // Appearance Theme Section
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(title = "Appearance & Theme")
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val currentTheme = uiState.userSettings.themeMode

                    ThemeOptionItem(
                        title = "System Default",
                        subtitle = "Follow Android system theme settings",
                        icon = Icons.Default.SettingsBrightness,
                        isSelected = currentTheme == "SYSTEM",
                        onClick = { onUpdateThemeMode("SYSTEM") }
                    )

                    SettingDivider()

                    ThemeOptionItem(
                        title = "Light Mode",
                        subtitle = "Clean light background",
                        icon = Icons.Default.LightMode,
                        isSelected = currentTheme == "LIGHT",
                        onClick = { onUpdateThemeMode("LIGHT") }
                    )

                    SettingDivider()

                    ThemeOptionItem(
                        title = "Dark Mode",
                        subtitle = "Deep dark slate canvas",
                        icon = Icons.Default.DarkMode,
                        isSelected = currentTheme == "DARK",
                        onClick = { onUpdateThemeMode("DARK") }
                    )
                }
            }
        }

        // Privacy & Local Data Section
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(title = "Privacy Center & Local Data Policy")
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    // Header Status
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(StatusGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Privacy",
                                tint = StatusGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Privacy-First Architecture",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "SMS Monitoring: ${if (uiState.userSettings.smsTrackingEnabled) "Active" else "Paused"}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (uiState.userSettings.smsTrackingEnabled) StatusGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // What Keshio Stores
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "What Keshio stores (Local SQLite):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Parsed transaction amounts, dates, type & party names\n• Daily and monthly budget target limits\n• Savings goals & custom progress logs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "What Keshio NEVER accesses or stores:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = StatusGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Contacts, phone calls, camera, microphone or location\n• Non-financial SMS messages, bank PINs or passwords\n• No server sync — 100% of your data stays on this phone",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingItem(
                        icon = Icons.Default.Refresh,
                        title = "Restore Sample Transactions",
                        subtitle = "Populate realistic sample spending and income records",
                        onClick = { showRestoreSampleDialog = true },
                        testTag = "setting_restore_sample"
                    )

                    SettingDivider()

                    SettingItem(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear Transactions Only",
                        subtitle = "Erase transaction records while keeping budgets & goals",
                        onClick = { showClearDataDialog = true },
                        iconTint = MaterialTheme.colorScheme.error,
                        testTag = "setting_clear_all_data"
                    )

                    SettingDivider()

                    SettingItem(
                        icon = Icons.Default.DeleteSweep,
                        title = "Delete All Keshio Data",
                        subtitle = "Wipe all transactions, goals, budgets & reset app completely",
                        onClick = { showDeleteAllKeshioDialog = true },
                        iconTint = MaterialTheme.colorScheme.error,
                        testTag = "setting_delete_all_keshio_data"
                    )
                }
            }
        }

        // About Keshio
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(title = "About")
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Keshio",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Keshio",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "“Know where your money goes.”",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Version 1.0 (Foundation Edition)\nPrivacy-first architecture built with Jetpack Compose & local Room persistence.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ThemeOptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun SettingToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    )
}

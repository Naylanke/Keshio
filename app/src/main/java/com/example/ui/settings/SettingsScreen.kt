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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
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
    onOpenSmsSimulator: () -> Unit,
    onRestoreSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = uiState.userSettings.currencySymbol

    var showDailyTargetDialog by remember { mutableStateOf(false) }
    var showMonthlyTargetDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showRestoreSampleDialog by remember { mutableStateOf(false) }

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

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Transactions?") },
            text = { Text("This will permanently delete all transaction records from your device. Budget settings will be preserved.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllData()
                        showClearDataDialog = false
                    },
                    modifier = Modifier.testTag("confirm_clear_all_data_btn")
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
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

        // Privacy & Offline Storage Section
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(title = "Privacy & Local Data")
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
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
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
                                text = "Zero Cloud Sync Guarantee",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "All transactions and budget limits are stored 100% locally on your device in a secure SQLite database.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                        title = "Clear All Transactions",
                        subtitle = "Erase all transaction records permanently",
                        onClick = { showClearDataDialog = true },
                        iconTint = MaterialTheme.colorScheme.error,
                        testTag = "setting_clear_all_data"
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
private fun SettingDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    )
}

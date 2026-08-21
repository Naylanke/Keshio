package com.example.ui.onboarding

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.SecurityUtils
import com.example.security.SmsPermissionUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun OnboardingScreen(
    onCompleteOnboarding: (dailyTarget: Double, monthlyTarget: Double, appLockType: String, pin: String, smsEnabled: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentStep by remember { mutableStateOf(1) }

    // State for configuration
    var dailyTargetInput by remember { mutableStateOf("1500") }
    var monthlyTargetInput by remember { mutableStateOf("45000") }
    var appLockChoice by remember { mutableStateOf("NONE") } // "NONE", "PIN", "BIOMETRIC"
    var pinInput by remember { mutableStateOf("") }
    var isSmsGranted by remember { mutableStateOf(false) }
    var permissionErrorMessage by remember { mutableStateOf<String?>(null) }
    var isPermanentlyDenied by remember { mutableStateOf(false) }

    val isBiometricSupported = remember(context) { SecurityUtils.canAuthenticateWithBiometrics(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("KeshioSmsPermission", "Onboarding RECEIVE_SMS permission result: $isGranted")

        if (isGranted) {
            isSmsGranted = true
            permissionErrorMessage = null
            isPermanentlyDenied = false
        } else {
            val permanentlyDenied = !SmsPermissionUtils.shouldShowReceiveSmsRationale(context)
            isPermanentlyDenied = permanentlyDenied
            isSmsGranted = false
            permissionErrorMessage = if (permanentlyDenied) {
                "RECEIVE_SMS permission was disabled. Grant access in Settings (or App Info > Allow restricted settings) to enable auto-tracking."
            } else {
                "RECEIVE_SMS permission was not granted. Keshio continues working normally with manual transactions."
            }
        }
    }

    val hasSmsPermission = SmsPermissionUtils.hasReceiveSmsPermission(context)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val hasPermissionNow = SmsPermissionUtils.hasReceiveSmsPermission(context)
                Log.d("KeshioSmsTracking", "Onboarding ON_RESUME permission check: $hasPermissionNow")
                if (hasPermissionNow) {
                    isSmsGranted = true
                    permissionErrorMessage = null
                    isPermanentlyDenied = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(hasSmsPermission) {
        if (hasSmsPermission) {
            isSmsGranted = true
            permissionErrorMessage = null
            isPermanentlyDenied = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Progress Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (step in 1..4) {
                    Box(
                        modifier = Modifier
                            .size(if (step == currentStep) 24.dp else 10.dp, 10.dp)
                            .clip(CircleShape)
                            .background(
                                if (step == currentStep) MaterialTheme.colorScheme.primary
                                else if (step < currentStep) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (currentStep) {
                1 -> {
                    // Step 1: Welcome
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Keshio Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Welcome to Keshio",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Know where your money goes.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Keshio helps you automatically track your mobile financial transactions 100% locally on your phone. No cloud account, no advertisements, no subscriptions.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    Button(
                        onClick = { currentStep = 2 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_next_step_1"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }

                2 -> {
                    // Step 2: Budget Targets
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = "Budget Setup",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Set Your Targets",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Customize your daily spending limit and optional monthly budget.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = dailyTargetInput,
                        onValueChange = { dailyTargetInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Daily Target (KSh)") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_daily_target_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = monthlyTargetInput,
                        onValueChange = { monthlyTargetInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Monthly Budget (KSh)") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_monthly_target_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentStep = 1 },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Back")
                        }

                        Button(
                            onClick = { currentStep = 3 },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("onboarding_next_step_2"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Next", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                3 -> {
                    // Step 3: Security & App Lock
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "App Lock Setup",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Protect Keshio",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your financial information is private. Choose how you'd like to protect the app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Option: No Lock
                    Card(
                        onClick = { appLockChoice = "NONE" },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (appLockChoice == "NONE") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (appLockChoice == "NONE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("onboarding_lock_option_none")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = appLockChoice == "NONE", onClick = { appLockChoice = "NONE" })
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("No App Lock", fontWeight = FontWeight.Bold)
                                Text("Quick access without verification", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // Option: PIN
                    Card(
                        onClick = { appLockChoice = "PIN" },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (appLockChoice == "PIN") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (appLockChoice == "PIN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("onboarding_lock_option_pin")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = appLockChoice == "PIN", onClick = { appLockChoice = "PIN" })
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("PIN Protection", fontWeight = FontWeight.Bold)
                                Text("Protect app with a 4-digit secret PIN", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    if (appLockChoice == "PIN") {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 6) pinInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Set 4-Digit PIN") },
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_pin_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    // Option: Biometric
                    if (isBiometricSupported) {
                        Card(
                            onClick = { appLockChoice = "BIOMETRIC" },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (appLockChoice == "BIOMETRIC") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (appLockChoice == "BIOMETRIC") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("onboarding_lock_option_biometric")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = appLockChoice == "BIOMETRIC", onClick = { appLockChoice = "BIOMETRIC" })
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Biometrics / Fingerprint", fontWeight = FontWeight.Bold)
                                    Text("Use Android system fingerprint or face unlock", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentStep = 2 },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Back")
                        }

                        Button(
                            onClick = { currentStep = 4 },
                            enabled = appLockChoice != "PIN" || pinInput.length >= 4,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("onboarding_next_step_3"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Next", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                4 -> {
                    // Step 4: SMS Tracking Permission
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "SMS Tracking",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Automatic SMS Tracking",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Keshio parses incoming financial SMS messages (like M-Pesa & bank alerts) to automatically log transactions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSmsGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (isSmsGranted) Icons.Default.CheckCircle else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isSmsGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isSmsGranted) "SMS Permission Granted" else "Grant SMS Access",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "100% On-Device Processing. Non-financial texts are completely ignored.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (!isSmsGranted) {
                                Spacer(modifier = Modifier.height(16.dp))
                                if (isPermanentlyDenied) {
                                    Button(
                                        onClick = {
                                            SmsPermissionUtils.openAppSettings(context)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.testTag("onboarding_open_settings_btn")
                                    ) {
                                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Open Settings")
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            if (!SmsPermissionUtils.isTelephonySupported(context)) {
                                                permissionErrorMessage = "SMS features are unavailable on this device. You can proceed with manual entries."
                                            } else {
                                                SmsPermissionUtils.safeLaunchSinglePermissionRequest(
                                                    launcher = permissionLauncher,
                                                    permission = Manifest.permission.RECEIVE_SMS,
                                                    context = context,
                                                    onError = { error ->
                                                        permissionErrorMessage = error
                                                    }
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("onboarding_grant_sms_btn")
                                    ) {
                                        Text("Allow SMS Access")
                                    }
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = permissionErrorMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = permissionErrorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentStep = 3 },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Back")
                        }

                        Button(
                            onClick = {
                                val daily = dailyTargetInput.toDoubleOrNull() ?: 1500.0
                                val monthly = monthlyTargetInput.toDoubleOrNull() ?: 45000.0
                                onCompleteOnboarding(daily, monthly, appLockChoice, pinInput, isSmsGranted)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("onboarding_finish_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Start Keshio", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

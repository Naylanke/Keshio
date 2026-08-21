package com.example.ui.components

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.security.SmsPermissionUtils
import com.example.sms.SmsHistoryImporter
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IncomeGreen
import kotlinx.coroutines.launch

@Composable
fun SmsTrackingCard(
    isTrackingEnabled: Boolean,
    onTrackingToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionDeniedMessage by remember { mutableStateOf<String?>(null) }
    var isPermanentlyDenied by remember { mutableStateOf(false) }
    var showRationaleDialog by remember { mutableStateOf(false) }

    var isCheckingOrRequesting by remember { mutableStateOf(false) }
    var isImportingHistory by remember { mutableStateOf(false) }
    var importResultMessage by remember { mutableStateOf<String?>(null) }

    var hasReceiveSms by remember { mutableStateOf(SmsPermissionUtils.hasReceiveSmsPermission(context)) }
    var hasReadSms by remember { mutableStateOf(SmsPermissionUtils.hasReadSmsPermission(context)) }

    // Re-check permissions automatically when user resumes activity (e.g. returning from Android Settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val receivePermissionNow = SmsPermissionUtils.hasReceiveSmsPermission(context)
                val readPermissionNow = SmsPermissionUtils.hasReadSmsPermission(context)
                hasReceiveSms = receivePermissionNow
                hasReadSms = readPermissionNow
                Log.d("KeshioSmsTracking", "Activity ON_RESUME permission re-check: RECEIVE_SMS=$receivePermissionNow, READ_SMS=$readPermissionNow")

                if (receivePermissionNow) {
                    permissionDeniedMessage = null
                    isPermanentlyDenied = false
                    isCheckingOrRequesting = false
                    if (!isTrackingEnabled) {
                        Log.d("KeshioSmsTracking", "Permission granted on resume. Enabling automatic SMS detection.")
                        onTrackingToggled(true)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Separate launcher for RECEIVE_SMS (Automatic detection of NEW SMS)
    val receiveSmsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isCheckingOrRequesting = false
        Log.d("KeshioSmsTracking", "RECEIVE_SMS permission callback result: $isGranted")
        hasReceiveSms = SmsPermissionUtils.hasReceiveSmsPermission(context)
        if (isGranted) {
            permissionDeniedMessage = null
            isPermanentlyDenied = false
            onTrackingToggled(true)
            Log.d("KeshioSmsTracking", "RECEIVE_SMS permission granted! Automatic detection enabled.")
        } else {
            val permanentlyDenied = !SmsPermissionUtils.shouldShowReceiveSmsRationale(context)
            isPermanentlyDenied = permanentlyDenied
            permissionDeniedMessage = if (permanentlyDenied) {
                "RECEIVE_SMS permission was disabled. Tap 'Open App Settings' below to allow SMS access for automatic tracking."
            } else {
                "RECEIVE_SMS permission was denied. Keshio will continue with manual transaction tracking."
            }
            onTrackingToggled(false)
            Log.w("KeshioSmsTracking", "RECEIVE_SMS permission denied. permanentlyDenied=$permanentlyDenied")
        }
    }

    // Separate launcher for READ_SMS (Scanning EXISTING SMS history)
    val readSmsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("KeshioSmsTracking", "READ_SMS permission callback result: $isGranted")
        hasReadSms = SmsPermissionUtils.hasReadSmsPermission(context)
        if (isGranted) {
            isImportingHistory = true
            scope.launch {
                try {
                    val result = SmsHistoryImporter.importSmsHistory(context)
                    importResultMessage = if (result.errorMessage != null) {
                        result.errorMessage
                    } else {
                        "Import finished: ${result.importedCount} new transactions added (${result.duplicateCount} duplicates skipped)."
                    }
                } catch (e: Exception) {
                    Log.e("KeshioSmsTracking", "Error during SMS history import", e)
                    importResultMessage = "Error importing SMS history: ${e.message}"
                } finally {
                    isImportingHistory = false
                }
            }
        } else {
            importResultMessage = "READ_SMS permission is required to scan past inbox messages."
        }
    }

    fun requestReceiveSms() {
        Log.d("KeshioSmsTracking", "requestReceiveSms initiated...")
        isCheckingOrRequesting = true
        permissionDeniedMessage = null

        try {
            if (!SmsPermissionUtils.isTelephonySupported(context)) {
                isCheckingOrRequesting = false
                permissionDeniedMessage = "SMS features are unavailable on this device."
                Log.w("KeshioSmsTracking", "Telephony hardware not supported on device.")
                return
            }

            if (SmsPermissionUtils.hasReceiveSmsPermission(context)) {
                isCheckingOrRequesting = false
                hasReceiveSms = true
                onTrackingToggled(true)
                Log.d("KeshioSmsTracking", "RECEIVE_SMS already granted. Tracking enabled.")
                return
            }

            if (SmsPermissionUtils.shouldShowReceiveSmsRationale(context)) {
                isCheckingOrRequesting = false
                showRationaleDialog = true
                Log.d("KeshioSmsTracking", "Showing rationale dialog before permission request.")
                return
            }

            Log.d("KeshioSmsTracking", "Launching RECEIVE_SMS permission prompt...")
            SmsPermissionUtils.safeLaunchSinglePermissionRequest(
                launcher = receiveSmsLauncher,
                permission = Manifest.permission.RECEIVE_SMS,
                context = context,
                onError = { error ->
                    isCheckingOrRequesting = false
                    permissionDeniedMessage = error
                    onTrackingToggled(false)
                    Log.e("KeshioSmsTracking", "Error launching RECEIVE_SMS request: $error")
                }
            )
        } catch (e: Exception) {
            isCheckingOrRequesting = false
            permissionDeniedMessage = "Unable to request SMS permission: ${e.localizedMessage}"
            Log.e("KeshioSmsTracking", "Unexpected exception in requestReceiveSms", e)
        }
    }

    fun startSmsHistoryImport() {
        Log.d("KeshioSmsTracking", "startSmsHistoryImport initiated...")
        if (SmsPermissionUtils.hasReadSmsPermission(context)) {
            hasReadSms = true
            isImportingHistory = true
            scope.launch {
                try {
                    val result = SmsHistoryImporter.importSmsHistory(context)
                    importResultMessage = if (result.errorMessage != null) {
                        result.errorMessage
                    } else {
                        "Import finished: ${result.importedCount} new transactions added (${result.duplicateCount} duplicates skipped)."
                    }
                } catch (e: Exception) {
                    Log.e("KeshioSmsTracking", "Error during SMS history import", e)
                    importResultMessage = "Error importing SMS history: ${e.message}"
                } finally {
                    isImportingHistory = false
                }
            }
        } else {
            if (SmsPermissionUtils.shouldShowReadSmsRationale(context)) {
                importResultMessage = "READ_SMS permission is required to scan past inbox messages."
            }
            SmsPermissionUtils.safeLaunchSinglePermissionRequest(
                launcher = readSmsLauncher,
                permission = Manifest.permission.READ_SMS,
                context = context,
                onError = { error -> importResultMessage = error }
            )
        }
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = {
                showRationaleDialog = false
                isCheckingOrRequesting = false
            },
            title = {
                Text(
                    text = "Why does Keshio need SMS access?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Keshio parses incoming financial transaction messages (like M-Pesa & bank alerts) to automatically track spending 100% locally on your phone. Non-financial messages are completely ignored.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationaleDialog = false
                        isCheckingOrRequesting = true
                        SmsPermissionUtils.safeLaunchSinglePermissionRequest(
                            launcher = receiveSmsLauncher,
                            permission = Manifest.permission.RECEIVE_SMS,
                            context = context,
                            onError = { error ->
                                isCheckingOrRequesting = false
                                permissionDeniedMessage = error
                                onTrackingToggled(false)
                            }
                        )
                    },
                    modifier = Modifier.testTag("continue_sms_permission_btn")
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationaleDialog = false
                        isCheckingOrRequesting = false
                    }
                ) {
                    Text("Not Now")
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sms_tracking_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "SMS Tracking",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Automatically track your transactions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "M-Pesa & Financial SMS",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (hasReceiveSms && isTrackingEnabled) {
                    Switch(
                        checked = isTrackingEnabled,
                        onCheckedChange = { onTrackingToggled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldPrimary
                        ),
                        modifier = Modifier.testTag("sms_tracking_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Explanatory body text
            Text(
                text = "Keshio can detect supported incoming financial transaction messages and automatically record them into your budget.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Privacy badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Private",
                    tint = IncomeGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "100% On-Device & Private. Messages are never uploaded to any server.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!hasReceiveSms || !isTrackingEnabled) {
                Spacer(modifier = Modifier.height(16.dp))

                if (isPermanentlyDenied) {
                    Button(
                        onClick = {
                            Log.d("KeshioSmsTracking", "Opening App Settings due to permanent denial...")
                            SmsPermissionUtils.openAppSettings(context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("open_app_settings_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open App Settings",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Button(
                        onClick = { requestReceiveSms() },
                        enabled = !isCheckingOrRequesting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("enable_sms_detection_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary
                        )
                    ) {
                        if (isCheckingOrRequesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Checking Permission...",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable Automatic SMS Detection",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(IncomeGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Active & monitoring incoming M-Pesa transactions",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = IncomeGreen
                    )
                }
            }

            // SMS History Import Action
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { startSmsHistoryImport() },
                enabled = !isImportingHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("import_sms_history_btn"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                if (isImportingHistory) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scanning Inbox...",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasReadSms) "Scan & Import Past SMS History" else "Import SMS History (Requires READ_SMS)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Permission denied feedback
            AnimatedVisibility(visible = permissionDeniedMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = permissionDeniedMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Import status feedback
            AnimatedVisibility(visible = importResultMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = importResultMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


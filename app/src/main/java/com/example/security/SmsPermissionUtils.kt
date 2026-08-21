package com.example.security

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

typealias SmsPermissionCallback = (permission: String, isGranted: Boolean, permanentlyDenied: Boolean) -> Unit

object SmsPermissionUtils {
    private const val TAG = "KeshioSmsPermission"

    const val REQUEST_CODE_RECEIVE_SMS = 1001
    const val REQUEST_CODE_READ_SMS = 1002

    private var receiveSmsCallback: SmsPermissionCallback? = null
    private var readSmsCallback: SmsPermissionCallback? = null

    /**
     * Unwraps a Context to find the enclosing Activity if available.
     */
    fun findActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    /**
     * Checks if the device has hardware or system feature support for Telephony / SMS.
     */
    fun isTelephonySupported(context: Context): Boolean {
        val supported = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        Log.d(TAG, "Telephony system feature supported: $supported")
        return supported
    }

    /**
     * Checks if RECEIVE_SMS permission is granted.
     * This permission is required ONLY for automatic detection of NEW incoming transaction SMS.
     */
    fun hasReceiveSmsPermission(context: Context): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "hasReceiveSmsPermission check: $granted")
        return granted
    }

    /**
     * Checks if READ_SMS permission is granted.
     * This permission is required ONLY for importing/scanning EXISTING SMS history.
     */
    fun hasReadSmsPermission(context: Context): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "hasReadSmsPermission check: $granted")
        return granted
    }

    /**
     * Determines whether Android suggests showing an educational rationale before requesting RECEIVE_SMS permission.
     */
    fun shouldShowReceiveSmsRationale(context: Context): Boolean {
        val activity = findActivity(context) ?: return false
        val rationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECEIVE_SMS)
        Log.d(TAG, "shouldShowReceiveSmsRationale: $rationale")
        return rationale
    }

    /**
     * Determines whether Android suggests showing an educational rationale before requesting READ_SMS permission.
     */
    fun shouldShowReadSmsRationale(context: Context): Boolean {
        val activity = findActivity(context) ?: return false
        val rationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_SMS)
        Log.d(TAG, "shouldShowReadSmsRationale: $rationale")
        return rationale
    }

    /**
     * Navigates the user safely to Keshio's application settings in Android Settings.
     */
    fun openAppSettings(context: Context): Boolean {
        return try {
            Log.d(TAG, "Opening Android Settings page for package: ${context.packageName}")
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error launching ACTION_APPLICATION_DETAILS_SETTINGS: ${e.javaClass.simpleName} - ${e.message}", e)
            false
        }
    }

    /**
     * Requests RECEIVE_SMS permission using direct 16-bit request code to avoid FragmentActivity request code limitations.
     */
    fun requestReceiveSmsPermission(context: Context, callback: SmsPermissionCallback) {
        val activity = findActivity(context)
        if (activity == null) {
            Log.e(TAG, "requestReceiveSmsPermission failed: Activity context not found")
            callback(Manifest.permission.RECEIVE_SMS, false, false)
            return
        }

        if (hasReceiveSmsPermission(context)) {
            Log.d(TAG, "RECEIVE_SMS permission already granted")
            callback(Manifest.permission.RECEIVE_SMS, true, false)
            return
        }

        receiveSmsCallback = callback
        try {
            Log.d(TAG, "Launching ActivityCompat.requestPermissions for RECEIVE_SMS (requestCode = $REQUEST_CODE_RECEIVE_SMS)")
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECEIVE_SMS),
                REQUEST_CODE_RECEIVE_SMS
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception requesting RECEIVE_SMS permission", e)
            receiveSmsCallback = null
            callback(Manifest.permission.RECEIVE_SMS, false, false)
        }
    }

    /**
     * Requests READ_SMS permission using direct 16-bit request code to avoid FragmentActivity request code limitations.
     */
    fun requestReadSmsPermission(context: Context, callback: SmsPermissionCallback) {
        val activity = findActivity(context)
        if (activity == null) {
            Log.e(TAG, "requestReadSmsPermission failed: Activity context not found")
            callback(Manifest.permission.READ_SMS, false, false)
            return
        }

        if (hasReadSmsPermission(context)) {
            Log.d(TAG, "READ_SMS permission already granted")
            callback(Manifest.permission.READ_SMS, true, false)
            return
        }

        readSmsCallback = callback
        try {
            Log.d(TAG, "Launching ActivityCompat.requestPermissions for READ_SMS (requestCode = $REQUEST_CODE_READ_SMS)")
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_SMS),
                REQUEST_CODE_READ_SMS
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception requesting READ_SMS permission", e)
            readSmsCallback = null
            callback(Manifest.permission.READ_SMS, false, false)
        }
    }

    /**
     * Handles permission results returned to MainActivity's onRequestPermissionsResult.
     */
    fun handlePermissionResult(
        activity: Activity,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val isGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "handlePermissionResult: requestCode=$requestCode, isGranted=$isGranted")

        if (requestCode == REQUEST_CODE_RECEIVE_SMS) {
            val rationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECEIVE_SMS)
            val permanentlyDenied = !isGranted && !rationale
            receiveSmsCallback?.invoke(Manifest.permission.RECEIVE_SMS, isGranted, permanentlyDenied)
            receiveSmsCallback = null
        } else if (requestCode == REQUEST_CODE_READ_SMS) {
            val rationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_SMS)
            val permanentlyDenied = !isGranted && !rationale
            readSmsCallback?.invoke(Manifest.permission.READ_SMS, isGranted, permanentlyDenied)
            readSmsCallback = null
        }
    }

    /**
     * Safely invokes the ActivityResultLauncher for a single permission (RECEIVE_SMS or READ_SMS).
     * Automatically falls back to direct ActivityCompat.requestPermissions if Launcher fails.
     */
    fun safeLaunchSinglePermissionRequest(
        launcher: ActivityResultLauncher<String>?,
        permission: String,
        context: Context,
        onError: (String) -> Unit
    ) {
        try {
            if (launcher != null) {
                Log.d(TAG, "Attempting to launch single permission prompt via ActivityResultLauncher for permission: $permission")
                launcher.launch(permission)
                return
            }
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "ActivityResultLauncher failed with 16-bit constraint: ${e.message}. Falling back to direct ActivityCompat.requestPermissions.")
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "ActivityNotFoundException caught during permission launch: ${e.message}", e)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException caught during permission launch: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception caught during permission launch: ${e.javaClass.simpleName} - ${e.message}", e)
        }

        // Direct fallback
        val activity = findActivity(context)
        if (activity != null) {
            val requestCode = if (permission == Manifest.permission.RECEIVE_SMS) REQUEST_CODE_RECEIVE_SMS else REQUEST_CODE_READ_SMS
            try {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
            } catch (e: Exception) {
                Log.e(TAG, "ActivityCompat.requestPermissions fallback failed", e)
                onError("Unable to request SMS permission. Keshio continues working normally.")
            }
        } else {
            onError("Unable to find Activity context to request permission.")
        }
    }
}


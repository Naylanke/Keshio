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

object SmsPermissionUtils {
    private const val TAG = "KeshioSmsPermission"

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
     */
    fun hasReceiveSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if READ_SMS permission is granted.
     */
    fun hasReadSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if any SMS permission required for tracking is granted.
     */
    fun hasAnySmsPermission(context: Context): Boolean {
        val hasReceive = hasReceiveSmsPermission(context)
        val hasRead = hasReadSmsPermission(context)
        Log.d(TAG, "SMS Permission check: RECEIVE_SMS=$hasReceive, READ_SMS=$hasRead")
        return hasReceive || hasRead
    }

    /**
     * Determines whether Android suggests showing an educational rationale before requesting permission.
     */
    fun shouldShowRationale(context: Context): Boolean {
        val activity = findActivity(context) ?: return false
        val receiveRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECEIVE_SMS)
        val readRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_SMS)
        Log.d(TAG, "Should show rationale check: receive=$receiveRationale, read=$readRationale")
        return receiveRationale || readRationale
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
     * Safely invokes the ActivityResultLauncher to request SMS permissions.
     * Prevents app crashes from ActivityNotFoundException, SecurityException, IllegalStateException, etc.
     */
    fun safeLaunchPermissionRequest(
        launcher: ActivityResultLauncher<Array<String>>,
        permissions: Array<String> = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS),
        onError: (String) -> Unit
    ) {
        try {
            Log.d(TAG, "Attempting to launch SMS runtime permission prompt for permissions: ${permissions.joinToString()}")
            launcher.launch(permissions)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "ActivityNotFoundException caught during permission launch: ${e.message}", e)
            onError("This device does not support permission prompts. You can grant access manually in Android Settings.")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException caught during permission launch: ${e.message}", e)
            onError("Security restriction blocked permission prompt. Please grant SMS access in Android Settings.")
        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException caught during permission launch: ${e.message}", e)
            onError("Permission dialog could not be displayed. Please try again.")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected Exception caught during permission launch: ${e.javaClass.simpleName} - ${e.message}", e)
            onError("Unable to request SMS permissions. Keshio continues working normally with manual transactions.")
        }
    }
}

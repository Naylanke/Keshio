package com.example.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.data.local.KeshioDatabase
import com.example.data.repository.KeshioRepository
import com.example.security.SmsPermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsBroadcastReceiver : BroadcastReceiver() {
    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // Verify RECEIVE_SMS permission before processing
        if (!SmsPermissionUtils.hasReceiveSmsPermission(context)) {
            Log.w("KeshioSmsReceiver", "SMS_RECEIVED broadcast received, but RECEIVE_SMS permission is not granted.")
            return
        }

        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                val db = KeshioDatabase.getDatabase(context)
                val userSettings = db.userSettingsDao().getUserSettingsDirect()

                // Check if user has enabled SMS tracking in settings
                if (userSettings == null || !userSettings.smsTrackingEnabled) {
                    return@launch
                }

                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if (messages.isNullOrEmpty()) return@launch

                val sender = messages[0].displayOriginatingAddress ?: messages[0].originatingAddress
                val timestamp = if (messages[0].timestampMillis > 0) messages[0].timestampMillis else System.currentTimeMillis()

                // Combine multi-part SMS bodies into a single complete string
                val fullBody = buildString {
                    for (sms in messages) {
                        val body = sms.displayMessageBody ?: sms.messageBody
                        if (!body.isNullOrEmpty()) {
                            append(body)
                        }
                    }
                }

                if (fullBody.isNotBlank()) {
                    val repo = KeshioRepository(db.transactionDao(), db.userSettingsDao(), db.savingsGoalDao())
                    val engine = FinancialSmsEngine.createForRepository(repo)
                    engine.processAndSaveSms(
                        sender = sender,
                        message = fullBody,
                        timestamp = timestamp,
                        source = "SMS"
                    )
                }
            } catch (e: Exception) {
                // Fail silently and safely on-device without crashing Keshio
                Log.e("KeshioSmsReceiver", "Error in SmsBroadcastReceiver processing: ${e.javaClass.simpleName} - ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

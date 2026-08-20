package com.example.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.data.local.KeshioDatabase
import com.example.data.repository.KeshioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsBroadcastReceiver : BroadcastReceiver() {
    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                val db = KeshioDatabase.getDatabase(context)
                val userSettings = db.userSettingsDao().getUserSettingsDirect()

                // Check if user has enabled SMS tracking
                if (userSettings == null || !userSettings.smsTrackingEnabled) {
                    return@launch
                }

                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if (messages.isNullOrEmpty()) return@launch

                val sender = messages[0].displayOriginatingAddress
                val timestamp = messages[0].timestampMillis

                // Combine multi-part SMS bodies
                val fullBody = buildString {
                    for (sms in messages) {
                        append(sms.displayMessageBody)
                    }
                }

                if (fullBody.isNotBlank()) {
                    val repo = KeshioRepository(db.transactionDao(), db.userSettingsDao(), db.savingsGoalDao())
                    val engine = FinancialSmsEngine(repo)
                    engine.processAndSaveSms(
                        sender = sender,
                        message = fullBody,
                        timestamp = timestamp,
                        source = "SMS"
                    )
                }
            } catch (_: Exception) {
                // Fail silently and safely on-device without crashing
            } finally {
                pendingResult.finish()
            }
        }
    }
}

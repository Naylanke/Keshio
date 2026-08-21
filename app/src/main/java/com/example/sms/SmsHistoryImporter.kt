package com.example.sms

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.local.KeshioDatabase
import com.example.data.repository.KeshioRepository
import com.example.security.SmsPermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SmsImportResult(
    val totalProcessed: Int = 0,
    val importedCount: Int = 0,
    val duplicateCount: Int = 0,
    val skippedCount: Int = 0,
    val errorMessage: String? = null
)

object SmsHistoryImporter {
    private const val TAG = "KeshioSmsImporter"

    /**
     * Scans existing SMS inbox for financial messages and imports them into Keshio.
     * Requires READ_SMS permission.
     */
    suspend fun importSmsHistory(context: Context): SmsImportResult = withContext(Dispatchers.IO) {
        if (!SmsPermissionUtils.hasReadSmsPermission(context)) {
            Log.e(TAG, "importSmsHistory failed: READ_SMS permission not granted")
            return@withContext SmsImportResult(
                errorMessage = "READ_SMS permission is required to import existing SMS history."
            )
        }

        var totalProcessed = 0
        var importedCount = 0
        var duplicateCount = 0
        var skippedCount = 0

        try {
            val db = KeshioDatabase.getDatabase(context)
            val repo = KeshioRepository(db.transactionDao(), db.userSettingsDao(), db.savingsGoalDao())
            val engine = FinancialSmsEngine.createForRepository(repo)

            val uri = Uri.parse("content://sms/inbox")
            val projection = arrayOf("address", "body", "date")
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "date DESC LIMIT 500"
            )

            cursor?.use { c ->
                val addressIdx = c.getColumnIndex("address")
                val bodyIdx = c.getColumnIndex("body")
                val dateIdx = c.getColumnIndex("date")

                while (c.moveToNext()) {
                    totalProcessed++
                    val address = if (addressIdx >= 0) c.getString(addressIdx) else null
                    val body = if (bodyIdx >= 0) c.getString(bodyIdx) else null
                    val date = if (dateIdx >= 0) c.getLong(dateIdx) else System.currentTimeMillis()

                    if (!body.isNullOrBlank()) {
                        val result = engine.processAndSaveSms(
                            sender = address,
                            message = body,
                            timestamp = date,
                            source = "SMS_IMPORT"
                        )

                        when (result) {
                            is ProcessSmsResult.Success -> importedCount++
                            is ProcessSmsResult.Duplicate -> duplicateCount++
                            else -> skippedCount++
                        }
                    } else {
                        skippedCount++
                    }
                }
            }

            Log.d(TAG, "SMS History Import finished: total=$totalProcessed, imported=$importedCount, duplicates=$duplicateCount, skipped=$skippedCount")
            SmsImportResult(
                totalProcessed = totalProcessed,
                importedCount = importedCount,
                duplicateCount = duplicateCount,
                skippedCount = skippedCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error importing SMS history: ${e.javaClass.simpleName} - ${e.message}", e)
            SmsImportResult(
                errorMessage = "Failed to scan SMS inbox: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }
}

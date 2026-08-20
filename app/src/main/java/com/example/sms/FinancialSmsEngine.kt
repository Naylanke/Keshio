package com.example.sms

import android.content.Context
import com.example.data.local.KeshioDatabase
import com.example.data.local.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.KeshioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinancialSmsEngine(
    private val repository: KeshioRepository,
    private val smartBudgetManager: com.example.notification.SmartBudgetManager? = null,
    private val parsers: List<FinancialSmsParser> = listOf(MpesaSmsParser())
) {
    companion object {
        @Volatile
        private var INSTANCE: FinancialSmsEngine? = null

        fun getInstance(context: Context): FinancialSmsEngine {
            return INSTANCE ?: synchronized(this) {
                val db = KeshioDatabase.getDatabase(context)
                val repo = KeshioRepository(db.transactionDao(), db.userSettingsDao())
                val budgetManager = com.example.notification.SmartBudgetManager.getInstance(context, repo)
                val instance = FinancialSmsEngine(repo, budgetManager)
                INSTANCE = instance
                instance
            }
        }

        fun createForRepository(
            repository: KeshioRepository,
            budgetManager: com.example.notification.SmartBudgetManager? = null
        ): FinancialSmsEngine {
            return FinancialSmsEngine(repository, budgetManager)
        }
    }

    /**
     * Inspects and parses incoming SMS without persisting.
     * Useful for live developer preview, testing, and pre-checks.
     */
    fun parseOnly(sender: String?, message: String, timestamp: Long = System.currentTimeMillis()): ParsedSmsTransaction? {
        for (parser in parsers) {
            if (parser.canParse(sender, message)) {
                val result = parser.parse(sender, message, timestamp)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    /**
     * Complete detection pipeline:
     * 1. Detect & parse financial message
     * 2. Check for duplicates using referenceId or fingerprint
     * 3. Insert transaction into Room DB
     * 4. Return result
     */
    suspend fun processAndSaveSms(
        sender: String?,
        message: String,
        timestamp: Long = System.currentTimeMillis(),
        source: String = "SMS"
    ): ProcessSmsResult = withContext(Dispatchers.IO) {
        val parsed = parseOnly(sender, message, timestamp)
            ?: return@withContext ProcessSmsResult.NotFinancialMessage(
                "Message is not a recognized or supported financial transaction."
            )

        // Duplicate Check: Reference ID
        if (!parsed.referenceId.isNullOrBlank()) {
            val existingByRef = repository.getTransactionByReferenceId(parsed.referenceId)
            if (existingByRef != null) {
                return@withContext ProcessSmsResult.Duplicate(
                    referenceId = parsed.referenceId,
                    party = parsed.party,
                    amount = parsed.amount,
                    reason = "Transaction with reference ${parsed.referenceId} already recorded."
                )
            }
        }

        // Duplicate Check: Fingerprint
        val existingByFp = repository.getTransactionByFingerprint(parsed.fingerprint)
        if (existingByFp != null) {
            return@withContext ProcessSmsResult.Duplicate(
                referenceId = parsed.referenceId,
                party = parsed.party,
                amount = parsed.amount,
                reason = "Identical transaction already recorded (fingerprint match)."
            )
        }

        // Format informative note
        val noteParts = mutableListOf<String>()
        if (!parsed.referenceId.isNullOrBlank()) {
            noteParts.add("Ref: ${parsed.referenceId}")
        }
        if (parsed.balanceAfter != null) {
            noteParts.add("Bal: KSh ${"%,.2f".format(parsed.balanceAfter)}")
        }
        if (parsed.transactionFee != null && parsed.transactionFee > 0.0) {
            noteParts.add("Fee: KSh ${"%,.2f".format(parsed.transactionFee)}")
        }
        noteParts.add("Detected via ${parsed.provider}")

        val entity = TransactionEntity(
            title = parsed.party,
            amount = parsed.amount,
            type = parsed.type.name,
            category = parsed.category,
            note = noteParts.joinToString(" • "),
            timestamp = parsed.timestamp,
            referenceId = parsed.referenceId,
            fingerprint = parsed.fingerprint,
            balanceAfter = parsed.balanceAfter,
            transactionFee = parsed.transactionFee,
            source = source
        )

        val newId = repository.insertTransaction(entity)
        val savedEntity = entity.copy(id = newId)

        // Evaluate smart budget alerts and send notifications if enabled
        try {
            smartBudgetManager?.onTransactionRecorded(
                transaction = savedEntity,
                isAutomaticDetection = (source == "SMS" || source == "SIMULATION")
            )
        } catch (_: Exception) {
            // Keep app resilient and safe
        }

        ProcessSmsResult.Success(
            parsed = parsed,
            transactionId = newId,
            isNew = true
        )
    }
}

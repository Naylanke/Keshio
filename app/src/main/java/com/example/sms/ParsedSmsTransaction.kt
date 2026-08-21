package com.example.sms

import com.example.data.model.TransactionType

enum class SmsTransactionCategory(val displayName: String) {
    RECEIVED_MONEY("Money Received"),
    SENT_MONEY("Money Sent"),
    MERCHANT_PAYMENT("Merchant Payment"),
    PAYBILL("Paybill Payment"),
    WITHDRAWAL("Cash Withdrawal"),
    DEPOSIT("Cash Deposit"),
    AIRTIME_PURCHASE("Airtime / Data"),
    TRANSACTION_FEE("Transaction Fee"),
    UNKNOWN("Unknown")
}

data class ParsedSmsTransaction(
    val referenceId: String?,
    val amount: Double,
    val type: TransactionType,
    val party: String,
    val category: String,
    val timestamp: Long,
    val balanceAfter: Double?,
    val transactionFee: Double?,
    val rawCategory: SmsTransactionCategory,
    val provider: String,
    val fingerprint: String,
    val rawMessage: String,
    val phoneNumber: String? = null
)

sealed class ProcessSmsResult {
    data class Success(
        val parsed: ParsedSmsTransaction,
        val transactionId: Long,
        val isNew: Boolean
    ) : ProcessSmsResult()

    data class Duplicate(
        val referenceId: String?,
        val party: String,
        val amount: Double,
        val reason: String
    ) : ProcessSmsResult()

    data class NotFinancialMessage(
        val reason: String
    ) : ProcessSmsResult()

    data class ParseError(
        val reason: String
    ) : ProcessSmsResult()
}

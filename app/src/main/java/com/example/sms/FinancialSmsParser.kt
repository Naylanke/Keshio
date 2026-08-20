package com.example.sms

interface FinancialSmsParser {
    val providerName: String
    
    /**
     * Quickly checks if this message could belong to this provider.
     */
    fun canParse(sender: String?, message: String): Boolean
    
    /**
     * Parses the SMS message into a structured ParsedSmsTransaction.
     * Returns null if the message is not a valid financial transaction.
     */
    fun parse(sender: String?, message: String, smsTimestamp: Long): ParsedSmsTransaction?
}

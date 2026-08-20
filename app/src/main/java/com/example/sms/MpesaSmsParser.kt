package com.example.sms

import com.example.data.model.TransactionType
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

class MpesaSmsParser : FinancialSmsParser {
    override val providerName: String = "M-PESA"

    companion object {
        // Ref code pattern: 8 to 12 alphanumeric characters at the start followed by Confirmed
        private val REF_CODE_PATTERN = Pattern.compile(
            "^(?:(?:[A-Z0-9]{8,12}))(?=\\s+Confirmed)",
            Pattern.CASE_INSENSITIVE
        )

        // General amount pattern e.g. Ksh1,500.00 or Ksh 500
        private val AMOUNT_PATTERN = Pattern.compile(
            "Ksh\\.?\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE
        )

        // Balance pattern e.g. New M-PESA balance is Ksh5,200.00
        private val BALANCE_PATTERN = Pattern.compile(
            "(?:New\\s+)?(?:M-PESA\\s+)?balance\\s+is\\s+Ksh\\.?\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE
        )

        // Transaction fee pattern e.g. Transaction cost, Ksh29.00
        private val FEE_PATTERN = Pattern.compile(
            "Transaction\\s+cost,?\\s+Ksh\\.?\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE
        )

        // Date and Time pattern e.g. on 20/8/26 at 10:30 AM or on 20/08/2026 at 2:15 PM
        private val DATE_TIME_PATTERN = Pattern.compile(
            "on\\s+([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4})\\s+at\\s+([0-9]{1,2}:[0-9]{2}(?:\\s*(?:AM|PM|am|pm))?)",
            Pattern.CASE_INSENSITIVE
        )

        // Received pattern
        private val RECEIVED_PATTERN = Pattern.compile(
            "You\\s+have\\s+received\\s+Ksh\\.?\\s*([0-9,.]+)\\s+from\\s+([^.]+?)(?:\\s+on\\s+[0-9]|\\.\\s*New|\\.)",
            Pattern.CASE_INSENSITIVE
        )

        // Sent to pattern
        private val SENT_PATTERN = Pattern.compile(
            "Ksh\\.?\\s*([0-9,.]+)\\s+sent\\s+to\\s+([^.]+?)(?:\\s+on\\s+[0-9]|\\.\\s*New|\\.)",
            Pattern.CASE_INSENSITIVE
        )

        // Paid to merchant / Buy Goods / Paybill
        private val PAID_PATTERN = Pattern.compile(
            "Ksh\\.?\\s*([0-9,.]+)\\s+paid\\s+to\\s+([^.]+?)(?:\\s+on\\s+[0-9]|\\.\\s*New|\\.)",
            Pattern.CASE_INSENSITIVE
        )

        // Sent to paybill with account
        private val PAYBILL_SENT_PATTERN = Pattern.compile(
            "Ksh\\.?\\s*([0-9,.]+)\\s+sent\\s+to\\s+([^.]+?)\\s+for\\s+account\\s+([^.]+?)(?:\\s+on\\s+[0-9]|\\.\\s*New|\\.)",
            Pattern.CASE_INSENSITIVE
        )

        // Withdrawal pattern
        private val WITHDRAW_PATTERN = Pattern.compile(
            "Withdraw\\s+Ksh\\.?\\s*([0-9,.]+)\\s+from\\s+([^.]+?)(?:\\s+on\\s+[0-9]|\\.\\s*New|\\.)",
            Pattern.CASE_INSENSITIVE
        )

        // Deposit / Give cash pattern
        private val DEPOSIT_PATTERN = Pattern.compile(
            "Give\\s+Ksh\\.?\\s*([0-9,.]+)\\s+cash\\s+to\\s+([^.]+?)(?:\\s+on\\s+[0-9]|\\.\\s*New|\\.)",
            Pattern.CASE_INSENSITIVE
        )

        // Airtime pattern
        private val AIRTIME_PATTERN = Pattern.compile(
            "bought\\s+Ksh\\.?\\s*([0-9,.]+)\\s+of\\s+([A-Za-z0-9\\s]+?airtime)",
            Pattern.CASE_INSENSITIVE
        )
    }

    override fun canParse(sender: String?, message: String): Boolean {
        val cleanMsg = message.trim()
        val isMpesaSender = sender?.contains("MPESA", ignoreCase = true) == true ||
            sender?.contains("M-PESA", ignoreCase = true) == true ||
            sender?.contains("SAFARICOM", ignoreCase = true) == true

        val containsMpesaKeyword = cleanMsg.contains("M-PESA", ignoreCase = true) ||
            cleanMsg.contains("MPESA", ignoreCase = true)

        val containsConfirmed = cleanMsg.contains("Confirmed", ignoreCase = true)
        val containsKsh = cleanMsg.contains("Ksh", ignoreCase = true)

        return (isMpesaSender || containsMpesaKeyword) && containsConfirmed && containsKsh
    }

    override fun parse(sender: String?, message: String, smsTimestamp: Long): ParsedSmsTransaction? {
        val cleanMsg = message.trim()

        // 1. Strict anti-spam / promotional filter:
        if (isPromotionalOrSpam(cleanMsg)) {
            return null
        }

        // 2. Extract Reference ID
        val refCode = extractReferenceCode(cleanMsg)

        // 3. Extract Amount
        val amount = extractFirstAmount(cleanMsg) ?: return null
        if (amount <= 0.0) return null

        // 4. Extract Balance
        val balance = extractBalance(cleanMsg)

        // 5. Extract Fee
        val fee = extractFee(cleanMsg)

        // 6. Extract Timestamp
        val parsedTimestamp = extractTimestamp(cleanMsg) ?: smsTimestamp

        // 7. Determine Transaction Nature (Category, Party, Type)
        val detection = detectTransactionTypeAndParty(cleanMsg, amount) ?: return null

        // 8. Auto-categorize
        val finalCategory = categorize(detection.party, detection.type, detection.smsCategory, cleanMsg)

        // 9. Generate deterministic fingerprint
        val fingerprint = generateFingerprint(
            refCode = refCode,
            amount = amount,
            type = detection.type,
            party = detection.party,
            timestamp = parsedTimestamp
        )

        return ParsedSmsTransaction(
            referenceId = refCode,
            amount = amount,
            type = detection.type,
            party = detection.party,
            category = finalCategory,
            timestamp = parsedTimestamp,
            balanceAfter = balance,
            transactionFee = fee,
            rawCategory = detection.smsCategory,
            provider = providerName,
            fingerprint = fingerprint,
            rawMessage = cleanMsg
        )
    }

    private data class TypeDetection(
        val type: TransactionType,
        val smsCategory: SmsTransactionCategory,
        val party: String
    )

    private fun detectTransactionTypeAndParty(message: String, amount: Double): TypeDetection? {
        // Check Received Money
        val recvMatcher = RECEIVED_PATTERN.matcher(message)
        if (recvMatcher.find()) {
            val senderRaw = recvMatcher.group(2)?.trim().orEmpty()
            val cleanParty = cleanPartyName(senderRaw)
            return TypeDetection(
                type = TransactionType.INCOME,
                smsCategory = SmsTransactionCategory.RECEIVED_MONEY,
                party = cleanParty.ifBlank { "Received Funds" }
            )
        }

        // Check Airtime Purchase
        val airtimeMatcher = AIRTIME_PATTERN.matcher(message)
        if (airtimeMatcher.find() || message.contains("airtime", ignoreCase = true) && message.contains("bought", ignoreCase = true)) {
            return TypeDetection(
                type = TransactionType.EXPENSE,
                smsCategory = SmsTransactionCategory.AIRTIME_PURCHASE,
                party = "Safaricom Airtime"
            )
        }

        // Check Sent to Paybill with Account
        val paybillMatcher = PAYBILL_SENT_PATTERN.matcher(message)
        if (paybillMatcher.find()) {
            val merchant = paybillMatcher.group(2)?.trim().orEmpty()
            val account = paybillMatcher.group(3)?.trim().orEmpty()
            val partyName = if (account.isNotBlank()) "$merchant (Acc: $account)" else merchant
            return TypeDetection(
                type = TransactionType.EXPENSE,
                smsCategory = SmsTransactionCategory.PAYBILL,
                party = cleanPartyName(partyName)
            )
        }

        // Check Merchant / Buy Goods Payment
        val paidMatcher = PAID_PATTERN.matcher(message)
        if (paidMatcher.find()) {
            val merchant = paidMatcher.group(2)?.trim().orEmpty()
            return TypeDetection(
                type = TransactionType.EXPENSE,
                smsCategory = SmsTransactionCategory.MERCHANT_PAYMENT,
                party = cleanPartyName(merchant).ifBlank { "Merchant Payment" }
            )
        }

        // Check Sent to Person
        val sentMatcher = SENT_PATTERN.matcher(message)
        if (sentMatcher.find()) {
            val recipient = sentMatcher.group(2)?.trim().orEmpty()
            return TypeDetection(
                type = TransactionType.EXPENSE,
                smsCategory = SmsTransactionCategory.SENT_MONEY,
                party = cleanPartyName(recipient).ifBlank { "Sent Funds" }
            )
        }

        // Check Withdrawal
        val withdrawMatcher = WITHDRAW_PATTERN.matcher(message)
        if (withdrawMatcher.find() || message.contains("Withdraw", ignoreCase = true)) {
            val agent = if (withdrawMatcher.find()) withdrawMatcher.group(2)?.trim().orEmpty() else ""
            val cleanAgent = cleanPartyName(agent)
            val title = if (cleanAgent.isNotBlank()) "Withdrawal - $cleanAgent" else "Cash Withdrawal"
            return TypeDetection(
                type = TransactionType.EXPENSE,
                smsCategory = SmsTransactionCategory.WITHDRAWAL,
                party = title
            )
        }

        // Check Deposit
        val depositMatcher = DEPOSIT_PATTERN.matcher(message)
        if (depositMatcher.find() || (message.contains("Give", ignoreCase = true) && message.contains("cash to", ignoreCase = true))) {
            val agent = if (depositMatcher.find()) depositMatcher.group(2)?.trim().orEmpty() else ""
            val cleanAgent = cleanPartyName(agent)
            val title = if (cleanAgent.isNotBlank()) "Deposit - $cleanAgent" else "Cash Deposit"
            return TypeDetection(
                type = TransactionType.INCOME,
                smsCategory = SmsTransactionCategory.DEPOSIT,
                party = title
            )
        }

        // Fallback checks
        if (message.contains("received", ignoreCase = true)) {
            return TypeDetection(
                type = TransactionType.INCOME,
                smsCategory = SmsTransactionCategory.RECEIVED_MONEY,
                party = "Received Funds"
            )
        }

        if (message.contains("sent to", ignoreCase = true) || message.contains("paid to", ignoreCase = true)) {
            return TypeDetection(
                type = TransactionType.EXPENSE,
                smsCategory = SmsTransactionCategory.SENT_MONEY,
                party = "Payment"
            )
        }

        return null
    }

    private fun categorize(party: String, type: TransactionType, smsCat: SmsTransactionCategory, message: String): String {
        val lowerParty = party.lowercase(Locale.ROOT)
        val lowerMsg = message.lowercase(Locale.ROOT)

        if (smsCat == SmsTransactionCategory.AIRTIME_PURCHASE || lowerMsg.contains("airtime")) {
            return "Bills & Utilities"
        }

        if (smsCat == SmsTransactionCategory.WITHDRAWAL || smsCat == SmsTransactionCategory.DEPOSIT) {
            return "Transfer & Deposit"
        }

        // Supermarkets & Groceries
        if (containsAny(lowerParty, "quickmart", "naivas", "carrefour", "chandarana", "supermarket", "cleanshelf", "tuskys", "uchumi", "grocer", "market", "greens", "butchery", "foodplus")) {
            return "Groceries"
        }

        // Food & Dining
        if (containsAny(lowerParty, "java", "kfc", "artcaffe", "pizza", "burger", "cafe", "restaurant", "bistro", "bakery", "coffee", "chicken inn", "galitos", "bar", "grill", "eats", "dominos", "subway", "kitchen", "cater")) {
            return "Food & Dining"
        }

        // Transport & Fuel
        if (containsAny(lowerParty, "shell", "total", "rubis", "ola", "petrol", "fuel", "oil", "uber", "bolt", "little cab", "matatu", "metro", "2nk", "coach", "boda", "transit", "expressway", "sgr")) {
            return "Transport"
        }

        // Bills & Utilities
        if (containsAny(lowerParty, "kplc", "kenya power", "water", "nairobi water", "zuku", "safaricom", "faiba", "dstv", "gotv", "startimes", "token", "internet", "wifi", "rent", "nhif", "nssf")) {
            return "Bills & Utilities"
        }

        // Health & Medical
        if (containsAny(lowerParty, "pharmacy", "chemist", "hospital", "clinic", "med", "doctor", "lab", "dental", "optician", "agakhan", "knh", "mater")) {
            return "Health & Medical"
        }

        // Education
        if (containsAny(lowerParty, "school", "college", "university", "academy", "tuition", "polytechnic", "primary", "secondary", "kindergarten")) {
            return "Education"
        }

        // Entertainment
        if (containsAny(lowerParty, "cinema", "movie", "netflix", "spotify", "gaming", "playstation", "lounge", "resort", "hotel")) {
            return "Entertainment"
        }

        // Shopping & Retail
        if (containsAny(lowerParty, "jumia", "kilimall", "store", "shop", "boutique", "electronics", "fashion", "hardware", "outfit", "mall")) {
            return "Shopping"
        }

        // Income categorizations
        if (type == TransactionType.INCOME) {
            if (containsAny(lowerParty + lowerMsg, "salary", "wages", "payroll", "allowance", "stipend")) {
                return "Salary & Wages"
            }
            if (containsAny(lowerParty + lowerMsg, "client", "consulting", "freelance", "business", "project", "design", "invoice")) {
                return "Freelance & Business"
            }
            if (containsAny(lowerParty + lowerMsg, "dividend", "interest", "investment", "shares", "fund")) {
                return "Investment & Returns"
            }
            return "Transfer & Deposit"
        }

        // Default expense category
        return if (smsCat == SmsTransactionCategory.SENT_MONEY) "Transfer & Deposit" else "Shopping"
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { text.contains(it, ignoreCase = true) }
    }

    private fun cleanPartyName(raw: String): String {
        return raw.trim()
            .replace(Regex("\\s+on\\s+[0-9].*$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\.?\\s*New\\s+M-PESA.*$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\.?\\s*Transaction\\s+cost.*$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\.+$"), "")
            .trim()
    }

    private fun extractReferenceCode(message: String): String? {
        val parts = message.split("\\s+".toRegex())
        if (parts.isNotEmpty()) {
            val candidate = parts[0].replace(".", "").trim()
            if (candidate.matches(Regex("^[A-Z0-9]{8,12}$", RegexOption.IGNORE_CASE))) {
                return candidate.uppercase(Locale.ROOT)
            }
        }
        val matcher = Pattern.compile("([A-Z0-9]{8,12})(?=\\s+Confirmed)", Pattern.CASE_INSENSITIVE).matcher(message)
        return if (matcher.find()) matcher.group(1)?.uppercase(Locale.ROOT) else null
    }

    private fun extractFirstAmount(message: String): Double? {
        val matcher = AMOUNT_PATTERN.matcher(message)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "")
            return amountStr?.toDoubleOrNull()
        }
        return null
    }

    private fun extractBalance(message: String): Double? {
        val matcher = BALANCE_PATTERN.matcher(message)
        if (matcher.find()) {
            val balStr = matcher.group(1)?.replace(",", "")
            return balStr?.toDoubleOrNull()
        }
        return null
    }

    private fun extractFee(message: String): Double? {
        val matcher = FEE_PATTERN.matcher(message)
        if (matcher.find()) {
            val feeStr = matcher.group(1)?.replace(",", "")
            return feeStr?.toDoubleOrNull()
        }
        return null
    }

    private fun extractTimestamp(message: String): Long? {
        val matcher = DATE_TIME_PATTERN.matcher(message)
        if (matcher.find()) {
            val dateStr = matcher.group(1)?.trim()
            val timeStr = matcher.group(2)?.trim()
            if (dateStr != null && timeStr != null) {
                return parseDateTime(dateStr, timeStr)
            }
        }
        return null
    }

    private fun parseDateTime(datePart: String, timePart: String): Long? {
        val normalizedTime = timePart.replace("\\s+".toRegex(), " ").uppercase(Locale.ROOT)
        val combined = "$datePart $normalizedTime"

        val patterns = listOf(
            "d/M/yy h:mm a",
            "dd/MM/yy h:mm a",
            "d/M/yyyy h:mm a",
            "dd/MM/yyyy h:mm a",
            "d-M-yy h:mm a",
            "dd-MM-yy h:mm a",
            "d/M/yy HH:mm",
            "dd/MM/yyyy HH:mm"
        )

        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.isLenient = true
                val parsedDate = sdf.parse(combined)
                if (parsedDate != null) {
                    val cal = Calendar.getInstance()
                    cal.time = parsedDate
                    // Adjust 2-digit year to 2000s if necessary
                    if (cal.get(Calendar.YEAR) < 1970) {
                        cal.add(Calendar.YEAR, 100)
                    }
                    return cal.timeInMillis
                }
            } catch (_: Exception) {}
        }
        return null
    }

    private fun isPromotionalOrSpam(message: String): Boolean {
        val lower = message.lowercase(Locale.ROOT)

        val spamKeywords = listOf(
            "dial *",
            "*544#",
            "*456#",
            "*100#",
            "*234#",
            "offer valid",
            "congratulations! you have won",
            "win up to",
            "grand draw",
            "send yes to",
            "postpaid bill of", // bill reminder
            "reminder:",
            "free data",
            "special offer",
            "airtime bonus",
            "opt out",
            "subscribe to"
        )

        for (keyword in spamKeywords) {
            if (lower.contains(keyword)) {
                return true
            }
        }

        // Must contain Confirmed or M-PESA
        if (!lower.contains("confirmed") && !lower.contains("m-pesa")) {
            return true
        }

        return false
    }

    private fun generateFingerprint(
        refCode: String?,
        amount: Double,
        type: TransactionType,
        party: String,
        timestamp: Long
    ): String {
        if (!refCode.isNullOrBlank()) {
            return "REF_${refCode.trim().uppercase(Locale.ROOT)}"
        }

        // Composite fingerprint: amount + type + clean party + minute-bucket timestamp
        val minuteBucket = timestamp / 60000L
        val raw = "MPESA|${"%.2f".format(Locale.US, amount)}|${type.name}|${party.trim().lowercase(Locale.ROOT)}|$minuteBucket"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(raw.toByteArray())
        return "FP_" + digest.joinToString("") { "%02x".format(it) }.take(16)
    }
}

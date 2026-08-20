package com.example.sms

data class SampleSmsItem(
    val id: String,
    val title: String,
    val description: String,
    val sender: String,
    val body: String,
    val expectedCategory: String,
    val isFinancial: Boolean
)

object SampleFinancialSms {
    val samples = listOf(
        SampleSmsItem(
            id = "recv_mpesa",
            title = "Money Received (+KSh 1,500)",
            description = "P2P funds received from John Doe",
            sender = "MPESA",
            body = "QA12BC34DE Confirmed. You have received Ksh1,500.00 from JOHN DOE 0712345678 on 20/8/26 at 10:30 AM. New M-PESA balance is Ksh5,200.00. Transaction cost, Ksh0.00.",
            expectedCategory = "Transfer & Deposit",
            isFinancial = true
        ),
        SampleSmsItem(
            id = "sent_mpesa",
            title = "Money Sent (-KSh 500)",
            description = "P2P transfer sent to Jane Doe",
            sender = "MPESA",
            body = "QA12BC34DF Confirmed. Ksh500.00 sent to JANE DOE 0723456789 on 20/8/26 at 10:32 AM. New M-PESA balance is Ksh4,700.00. Transaction cost, Ksh0.00.",
            expectedCategory = "Transfer & Deposit",
            isFinancial = true
        ),
        SampleSmsItem(
            id = "supermarket_merchant",
            title = "Supermarket Merchant (-KSh 1,240)",
            description = "Buy goods at Quickmart Supermarket",
            sender = "MPESA",
            body = "QA12BC34DG Confirmed. Ksh1,240.00 paid to QUICKMART SUPERMARKET. on 20/8/26 at 2:15 PM. New M-PESA balance is Ksh3,460.00. Transaction cost, Ksh0.00.",
            expectedCategory = "Groceries",
            isFinancial = true
        ),
        SampleSmsItem(
            id = "paybill_utility",
            title = "Paybill Electricity (-KSh 3,500)",
            description = "KPLC Prepaid token payment",
            sender = "MPESA",
            body = "RK78MN12OP Confirmed. Ksh3,500.00 sent to KPLC PREPAID for account 14234567890 on 20/8/26 at 8:00 PM. New M-PESA balance is Ksh1,200.00. Transaction cost, Ksh23.00.",
            expectedCategory = "Bills & Utilities",
            isFinancial = true
        ),
        SampleSmsItem(
            id = "restaurant_merchant",
            title = "Dining & Coffee (-KSh 450)",
            description = "Merchant payment at Java House",
            sender = "MPESA",
            body = "RK78MN12OQ Confirmed. Ksh450.00 paid to JAVA HOUSE KOBIL on 20/8/26 at 1:15 PM. New M-PESA balance is Ksh750.00. Transaction cost, Ksh0.00.",
            expectedCategory = "Food & Dining",
            isFinancial = true
        ),
        SampleSmsItem(
            id = "cash_withdrawal",
            title = "Cash Withdrawal (-KSh 2,000)",
            description = "Agent withdrawal with transaction fee",
            sender = "MPESA",
            body = "QA12BC34DH Confirmed. on 20/8/26 at 4:45 PM Withdraw Ksh2,000.00 from 123456 - AGENT NAIROBI CBD. New M-PESA balance is Ksh1,460.00. Transaction cost, Ksh29.00.",
            expectedCategory = "Transfer & Deposit",
            isFinancial = true
        ),
        SampleSmsItem(
            id = "agent_deposit",
            title = "Cash Deposit (+KSh 3,000)",
            description = "Cash deposit at M-Pesa agent",
            sender = "MPESA",
            body = "QA12BC34DI Confirmed. on 20/8/26 at 9:00 AM Give Ksh3,000.00 cash to 654321 - TOTAL SERVICES AGENT. New M-PESA balance is Ksh8,200.00.",
            expectedCategory = "Transfer & Deposit",
            isFinancial = true
        ),
        SampleSmsItem(
            id = "airtime_purchase",
            title = "Airtime Purchase (-KSh 100)",
            description = "Safaricom Airtime purchase",
            sender = "MPESA",
            body = "QA12BC34DJ Confirmed. You bought Ksh100.00 of airtime on 20/8/26 at 11:00 AM. New M-PESA balance is Ksh4,600.00. Transaction cost, Ksh0.00.",
            expectedCategory = "Bills & Utilities",
            isFinancial = true
        ),
        SampleSmsItem(
            id = "spam_promo",
            title = "Promotional Offer (Non-Financial)",
            description = "Marketing SMS that must be safely ignored",
            sender = "Safaricom",
            body = "Get 500MB + 30 mins for only Ksh 50 valid for 24hrs. Dial *544# to activate now. Offer valid until midnight.",
            expectedCategory = "None",
            isFinancial = false
        ),
        SampleSmsItem(
            id = "spam_lottery",
            title = "Spam / Lottery Scam (Non-Financial)",
            description = "Unsolicited text message with cash claims",
            sender = "Promo",
            body = "Congratulations! You have won Ksh 1,000,000 in the weekly lottery. Send YES to 20456 to claim your prize.",
            expectedCategory = "None",
            isFinancial = false
        )
    )
}

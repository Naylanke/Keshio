package com.example.sms

import com.example.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialSmsParserTest {

    private val parser = MpesaSmsParser()

    @Test
    fun testParseMoneyReceived() {
        val message = "QA12BC34DE Confirmed. You have received Ksh1,500.00 from JOHN DOE 0712345678 on 20/8/26 at 10:30 AM. New M-PESA balance is Ksh5,200.00. Transaction cost, Ksh0.00."
        assertTrue(parser.canParse("MPESA", message))

        val parsed = parser.parse("MPESA", message)
        assertNotNull(parsed)
        assertEquals("QA12BC34DE", parsed?.referenceId)
        assertEquals(1500.00, parsed?.amount ?: 0.0, 0.001)
        assertEquals(TransactionType.INCOME, parsed?.type)
        assertEquals("JOHN DOE", parsed?.party)
        assertEquals(5200.00, parsed?.balanceAfter ?: 0.0, 0.001)
        assertEquals(0.00, parsed?.transactionFee ?: -1.0, 0.001)
        assertEquals("Transfer & Deposit", parsed?.category)
    }

    @Test
    fun testParseMoneySent() {
        val message = "QA12BC34DF Confirmed. Ksh500.00 sent to JANE DOE 0723456789 on 20/8/26 at 10:32 AM. New M-PESA balance is Ksh4,700.00. Transaction cost, Ksh0.00."
        val parsed = parser.parse("MPESA", message)
        assertNotNull(parsed)
        assertEquals("QA12BC34DF", parsed?.referenceId)
        assertEquals(500.00, parsed?.amount ?: 0.0, 0.001)
        assertEquals(TransactionType.EXPENSE, parsed?.type)
        assertEquals("JANE DOE", parsed?.party)
        assertEquals(4700.00, parsed?.balanceAfter ?: 0.0, 0.001)
    }

    @Test
    fun testParseMerchantPayment() {
        val message = "QA12BC34DG Confirmed. Ksh1,240.00 paid to QUICKMART SUPERMARKET. on 20/8/26 at 2:15 PM. New M-PESA balance is Ksh3,460.00. Transaction cost, Ksh0.00."
        val parsed = parser.parse("MPESA", message)
        assertNotNull(parsed)
        assertEquals("QA12BC34DG", parsed?.referenceId)
        assertEquals(1240.00, parsed?.amount ?: 0.0, 0.001)
        assertEquals(TransactionType.EXPENSE, parsed?.type)
        assertEquals("QUICKMART SUPERMARKET", parsed?.party)
        assertEquals("Groceries", parsed?.category)
    }

    @Test
    fun testParsePaybillPayment() {
        val message = "RK78MN12OP Confirmed. Ksh3,500.00 sent to KPLC PREPAID for account 14234567890 on 20/8/26 at 8:00 PM. New M-PESA balance is Ksh1,200.00. Transaction cost, Ksh23.00."
        val parsed = parser.parse("MPESA", message)
        assertNotNull(parsed)
        assertEquals("RK78MN12OP", parsed?.referenceId)
        assertEquals(3500.00, parsed?.amount ?: 0.0, 0.001)
        assertEquals("KPLC PREPAID", parsed?.party)
        assertEquals(23.00, parsed?.transactionFee ?: 0.0, 0.001)
        assertEquals("Bills & Utilities", parsed?.category)
    }

    @Test
    fun testParseAgentWithdrawal() {
        val message = "QA12BC34DH Confirmed. on 20/8/26 at 4:45 PM Withdraw Ksh2,000.00 from 123456 - AGENT NAIROBI CBD. New M-PESA balance is Ksh1,460.00. Transaction cost, Ksh29.00."
        val parsed = parser.parse("MPESA", message)
        assertNotNull(parsed)
        assertEquals("QA12BC34DH", parsed?.referenceId)
        assertEquals(2000.00, parsed?.amount ?: 0.0, 0.001)
        assertEquals(TransactionType.EXPENSE, parsed?.type)
        assertEquals("123456 - AGENT NAIROBI CBD", parsed?.party)
        assertEquals(29.00, parsed?.transactionFee ?: 0.0, 0.001)
    }

    @Test
    fun testParseAirtimePurchase() {
        val message = "QA12BC34DJ Confirmed. You bought Ksh100.00 of airtime on 20/8/26 at 11:00 AM. New M-PESA balance is Ksh4,600.00. Transaction cost, Ksh0.00."
        val parsed = parser.parse("MPESA", message)
        assertNotNull(parsed)
        assertEquals("QA12BC34DJ", parsed?.referenceId)
        assertEquals(100.00, parsed?.amount ?: 0.0, 0.001)
        assertEquals(TransactionType.EXPENSE, parsed?.type)
        assertEquals("Safaricom Airtime", parsed?.party)
        assertEquals("Bills & Utilities", parsed?.category)
    }

    @Test
    fun testRejectNonFinancialOrSpamSms() {
        val promoMessage = "Get 500MB + 30 mins for only Ksh 50 valid for 24hrs. Dial *544# to activate now. Offer valid until midnight."
        assertFalse(parser.canParse("Safaricom", promoMessage))
        assertNull(parser.parse("Safaricom", promoMessage))

        val lotteryMessage = "Congratulations! You have won Ksh 1,000,000 in the weekly lottery. Send YES to 20456 to claim your prize."
        assertFalse(parser.canParse("Promo", lotteryMessage))
        assertNull(parser.parse("Promo", lotteryMessage))

        val randomText = "Hey, are we still meeting for lunch today at 1pm?"
        assertFalse(parser.canParse("Friend", randomText))
        assertNull(parser.parse("Friend", randomText))
    }

    @Test
    fun testFingerprintConsistency() {
        val message = "QA12BC34DF Confirmed. Ksh500.00 sent to JANE DOE 0723456789 on 20/8/26 at 10:32 AM. New M-PESA balance is Ksh4,700.00. Transaction cost, Ksh0.00."
        val parsed1 = parser.parse("MPESA", message)
        val parsed2 = parser.parse("MPESA", message)
        assertEquals(parsed1?.fingerprint, parsed2?.fingerprint)
        assertEquals("REF_QA12BC34DF", parsed1?.fingerprint)
    }
}

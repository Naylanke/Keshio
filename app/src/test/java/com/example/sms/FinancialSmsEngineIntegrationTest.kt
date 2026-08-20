package com.example.sms

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.KeshioDatabase
import com.example.data.model.TransactionType
import com.example.data.repository.KeshioRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FinancialSmsEngineIntegrationTest {

    private lateinit var database: KeshioDatabase
    private lateinit var repository: KeshioRepository
    private lateinit var engine: FinancialSmsEngine

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KeshioDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = KeshioRepository(database.transactionDao(), database.userSettingsDao())
        engine = FinancialSmsEngine(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testProcessAndSaveIncomingMpesaSms() = runBlocking {
        val sms = "QA12BC34DG Confirmed. Ksh1,240.00 paid to QUICKMART SUPERMARKET. on 20/8/26 at 2:15 PM. New M-PESA balance is Ksh3,460.00. Transaction cost, Ksh0.00."
        val result = engine.processAndSaveSms("MPESA", sms, source = "SMS")

        assertTrue(result is ProcessSmsResult.Success)
        val success = result as ProcessSmsResult.Success
        assertEquals(1240.00, success.parsed.amount, 0.001)
        assertEquals("QUICKMART SUPERMARKET", success.parsed.party)
        assertEquals(TransactionType.EXPENSE, success.parsed.type)

        // Verify in database
        val transactions = repository.allTransactions.first()
        assertEquals(1, transactions.size)
        assertEquals("QUICKMART SUPERMARKET", transactions[0].title)
        assertEquals(1240.00, transactions[0].amount, 0.001)
        assertEquals("QA12BC34DG", transactions[0].referenceId)
        assertEquals("SMS", transactions[0].source)
    }

    @Test
    fun testDuplicateMessageProtection() = runBlocking {
        val sms = "QA12BC34DG Confirmed. Ksh1,240.00 paid to QUICKMART SUPERMARKET. on 20/8/26 at 2:15 PM. New M-PESA balance is Ksh3,460.00. Transaction cost, Ksh0.00."

        // First ingestion -> Success
        val firstResult = engine.processAndSaveSms("MPESA", sms, source = "SMS")
        assertTrue(firstResult is ProcessSmsResult.Success)

        // Second ingestion with same reference ID -> Duplicate Result
        val secondResult = engine.processAndSaveSms("MPESA", sms, source = "SMS")
        assertTrue(secondResult is ProcessSmsResult.Duplicate)

        // Verify only 1 transaction remains in DB
        val transactions = repository.allTransactions.first()
        assertEquals(1, transactions.size)
    }

    @Test
    fun testNonFinancialSmsIgnored() = runBlocking {
        val spam = "Get 500MB + 30 mins for only Ksh 50 valid for 24hrs. Dial *544# to activate now."
        val result = engine.processAndSaveSms("Safaricom", spam, source = "SMS")

        assertTrue(result is ProcessSmsResult.NotFinancialMessage)
        val transactions = repository.allTransactions.first()
        assertEquals(0, transactions.size)
    }
}

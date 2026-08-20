package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["referenceId"], unique = false),
        Index(value = ["fingerprint"], unique = false)
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val referenceId: String? = null,
    val fingerprint: String? = null,
    val balanceAfter: Double? = null,
    val transactionFee: Double? = null,
    val source: String = "MANUAL" // "MANUAL", "SMS", "SIMULATION"
)


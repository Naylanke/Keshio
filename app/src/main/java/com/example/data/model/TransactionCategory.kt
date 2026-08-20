package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryInfo(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val defaultType: TransactionType
)

object TransactionCategories {
    val allCategories = listOf(
        CategoryInfo("Food & Dining", Icons.Default.Restaurant, Color(0xFFF97316), TransactionType.EXPENSE),
        CategoryInfo("Groceries", Icons.Default.ShoppingCart, Color(0xFF10B981), TransactionType.EXPENSE),
        CategoryInfo("Shopping", Icons.Default.LocalMall, Color(0xFFEC4899), TransactionType.EXPENSE),
        CategoryInfo("Transport", Icons.Default.DirectionsCar, Color(0xFF3B82F6), TransactionType.EXPENSE),
        CategoryInfo("Bills & Utilities", Icons.Default.ReceiptLong, Color(0xFF8B5CF6), TransactionType.EXPENSE),
        CategoryInfo("Entertainment", Icons.Default.SportsEsports, Color(0xFFA855F7), TransactionType.EXPENSE),
        CategoryInfo("Health & Medical", Icons.Default.LocalHospital, Color(0xFFEF4444), TransactionType.EXPENSE),
        CategoryInfo("Education", Icons.Default.School, Color(0xFF06B6D4), TransactionType.EXPENSE),
        CategoryInfo("Salary & Wages", Icons.Default.Payments, Color(0xFF059669), TransactionType.INCOME),
        CategoryInfo("Freelance & Business", Icons.Default.Work, Color(0xFF14B8A6), TransactionType.INCOME),
        CategoryInfo("Investment & Returns", Icons.Default.AccountBalance, Color(0xFF6366F1), TransactionType.INCOME),
        CategoryInfo("Transfer & Deposit", Icons.Default.SwapHoriz, Color(0xFF0EA5E9), TransactionType.INCOME),
        CategoryInfo("Other", Icons.Default.Category, Color(0xFF64748B), TransactionType.EXPENSE)
    )

    fun getCategoryInfo(name: String): CategoryInfo {
        return allCategories.find { it.name.equals(name, ignoreCase = true) }
            ?: CategoryInfo(name, Icons.Default.Category, Color(0xFF64748B), TransactionType.EXPENSE)
    }
}

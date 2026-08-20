package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val calendarNow = Calendar.getInstance().apply { timeInMillis = now }
        val calendarTarget = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isSameYear = calendarNow.get(Calendar.YEAR) == calendarTarget.get(Calendar.YEAR)
        val isToday = isSameYear && calendarNow.get(Calendar.DAY_OF_YEAR) == calendarTarget.get(Calendar.DAY_OF_YEAR)
        
        calendarNow.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = isSameYear && calendarNow.get(Calendar.DAY_OF_YEAR) == calendarTarget.get(Calendar.DAY_OF_YEAR)

        return when {
            isToday -> "Today, ${timeFormat.format(Date(timestamp))}"
            isYesterday -> "Yesterday, ${timeFormat.format(Date(timestamp))}"
            isSameYear -> "${shortDateFormat.format(Date(timestamp))}, ${timeFormat.format(Date(timestamp))}"
            else -> "${dateFormat.format(Date(timestamp))}"
        }
    }

    fun formatDateGroup(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val calendarNow = Calendar.getInstance().apply { timeInMillis = now }
        val calendarTarget = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isSameYear = calendarNow.get(Calendar.YEAR) == calendarTarget.get(Calendar.YEAR)
        val isToday = isSameYear && calendarNow.get(Calendar.DAY_OF_YEAR) == calendarTarget.get(Calendar.DAY_OF_YEAR)

        calendarNow.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = isSameYear && calendarNow.get(Calendar.DAY_OF_YEAR) == calendarTarget.get(Calendar.DAY_OF_YEAR)

        return when {
            isToday -> "Today"
            isYesterday -> "Yesterday"
            isSameYear -> shortDateFormat.format(Date(timestamp))
            else -> dateFormat.format(Date(timestamp))
        }
    }

    fun formatCurrency(amount: Double, symbol: String = "$"): String {
        return "$symbol%,.2f".format(Locale.US, amount)
    }
}

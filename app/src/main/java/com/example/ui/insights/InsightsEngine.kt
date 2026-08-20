package com.example.ui.insights

import com.example.data.local.TransactionEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.model.TransactionType
import com.example.util.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class InsightsData(
    val hasEnoughData: Boolean,
    val totalTransactionsCount: Int,
    val currentPeriodLabel: String,
    val totalSpentCurrentPeriod: Double,
    val totalReceivedCurrentPeriod: Double,
    val totalSpentPreviousPeriod: Double,
    val periodDifference: Double,
    val periodPercentageChange: Double,
    val hasPreviousPeriodData: Boolean,
    val averageDailySpending: Double,
    val topCategoryName: String?,
    val topCategoryAmount: Double,
    
    val weeklySummary: PeriodSummary,
    val monthlySummary: PeriodSummary,
    
    val categoryComparisons: List<CategoryComparison>,
    val trendInsights: List<InsightItem>,
    val moneyLeakInsight: InsightItem?,
    val recurringInsights: List<InsightItem>,
    val unusualTransactions: List<InsightItem>
)

data class PeriodSummary(
    val title: String,
    val received: Double,
    val spent: Double,
    val target: Double,
    val remainingBudget: Double,
    val averageDailySpending: Double,
    val topCategory: String,
    val budgetStatusLabel: String,
    val isWithinTarget: Boolean
)

data class CategoryComparison(
    val category: String,
    val currentAmount: Double,
    val previousAmount: Double,
    val percentageChange: Double,
    val isIncrease: Boolean
)

enum class InsightType {
    TREND, MONEY_LEAK, RECURRING, UNUSUAL
}

data class InsightItem(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val type: InsightType,
    val highlightAmount: Double? = null
)

object InsightsEngine {

    fun calculateInsights(
        transactions: List<TransactionEntity>,
        settings: UserSettingsEntity,
        timeframe: String = "WEEK" // "WEEK", "MONTH", "ALL"
    ): InsightsData {
        val currency = settings.currencySymbol
        val expensesOnly = transactions.filter { it.type == TransactionType.EXPENSE.name }

        if (transactions.size < 2 || expensesOnly.isEmpty()) {
            return emptyInsightsData(transactions.size, settings)
        }

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // 1. Determine Current vs Previous Period Timestamps
        val (currentStart, currentEnd, prevStart, prevEnd, periodLabel) = getPeriodBoundaries(timeframe, now)

        val currentTx = transactions.filter { it.timestamp in currentStart..currentEnd }
        val prevTx = transactions.filter { it.timestamp in prevStart..prevEnd }

        val currentExpenses = currentTx.filter { it.type == TransactionType.EXPENSE.name }
        val currentIncome = currentTx.filter { it.type == TransactionType.INCOME.name }
        val prevExpenses = prevTx.filter { it.type == TransactionType.EXPENSE.name }

        val currentSpent = currentExpenses.sumOf { it.amount }
        val currentReceived = currentIncome.sumOf { it.amount }
        val prevSpent = prevExpenses.sumOf { it.amount }

        val hasPrevData = prevExpenses.isNotEmpty()
        val periodDiff = currentSpent - prevSpent
        val periodPctChange = if (prevSpent > 0) ((periodDiff / prevSpent) * 100) else 0.0

        // 2. Average Daily Spending
        val daysInPeriod = calculateDaysInPeriod(timeframe, currentStart, now)
        val avgDailySpending = if (daysInPeriod > 0) currentSpent / daysInPeriod else currentSpent

        // 3. Top Category in Current Period
        val categoryGroups = currentExpenses.groupBy { it.category }
        val topCategoryEntry = categoryGroups.maxByOrNull { group -> group.value.sumOf { it.amount } }
        val topCategoryName = topCategoryEntry?.key
        val topCategoryAmount = topCategoryEntry?.value?.sumOf { it.amount } ?: 0.0

        // 4. Weekly Summary & Monthly Summary
        val weeklySummary = computeWeeklySummary(transactions, settings, now)
        val monthlySummary = computeMonthlySummary(transactions, settings, now)

        // 5. Category Comparisons (Current vs Previous)
        val categoryComparisons = if (hasPrevData) {
            val prevCatGroups = prevExpenses.groupBy { it.category }
            categoryGroups.mapNotNull { (cat, currentCatTx) ->
                val currCatAmount = currentCatTx.sumOf { it.amount }
                val prevCatTx = prevCatGroups[cat] ?: emptyList()
                val prevCatAmount = prevCatTx.sumOf { it.amount }

                if (prevCatAmount > 0) {
                    val diff = currCatAmount - prevCatAmount
                    val pct = (diff / prevCatAmount) * 100
                    CategoryComparison(
                        category = cat,
                        currentAmount = currCatAmount,
                        previousAmount = prevCatAmount,
                        percentageChange = pct,
                        isIncrease = diff >= 0
                    )
                } else null
            }.sortedByDescending { Math.abs(it.percentageChange) }
        } else {
            emptyList()
        }

        // 6. Trend Insights
        val trendInsights = mutableListOf<InsightItem>()
        if (hasPrevData && Math.abs(periodDiff) > 100) {
            val isIncrease = periodDiff > 0
            val formattedDiff = DateTimeUtils.formatCurrency(Math.abs(periodDiff), currency)
            val formattedCurrAvg = DateTimeUtils.formatCurrency(avgDailySpending, currency)
            val prevDays = if (timeframe == "WEEK") 7 else 30
            val prevAvg = prevSpent / prevDays
            val formattedPrevAvg = DateTimeUtils.formatCurrency(prevAvg, currency)

            val title = if (isIncrease) "🟠 Spending is increasing" else "🟢 Spending is decreasing"
            val desc = if (isIncrease) {
                "Your average daily spending this ${timeframe.lowercase()} is $formattedCurrAvg, compared with $formattedPrevAvg last ${timeframe.lowercase()}."
            } else {
                "You spent $formattedDiff less this ${timeframe.lowercase()} compared to last ${timeframe.lowercase()}."
            }

            trendInsights.add(
                InsightItem(
                    id = "trend_period_change",
                    title = title,
                    description = desc,
                    emoji = if (isIncrease) "🟠" else "🟢",
                    type = InsightType.TREND,
                    highlightAmount = periodDiff
                )
            )
        }

        // High Spending Day
        val dayOfWeekGroups = currentExpenses.groupBy {
            val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            c.get(Calendar.DAY_OF_WEEK)
        }
        val highestDayEntry = dayOfWeekGroups.maxByOrNull { group -> group.value.sumOf { it.amount } }
        if (highestDayEntry != null && highestDayEntry.value.isNotEmpty()) {
            val dayName = getDayName(highestDayEntry.key)
            val dayTotal = highestDayEntry.value.sumOf { it.amount }
            if (dayTotal > 0 && currentExpenses.size >= 3) {
                trendInsights.add(
                    InsightItem(
                        id = "trend_high_day",
                        title = "Highest Spending Day",
                        description = "$dayName was your highest spending day this ${timeframe.lowercase()}, totaling ${DateTimeUtils.formatCurrency(dayTotal, currency)}.",
                        emoji = "📅",
                        type = InsightType.TREND,
                        highlightAmount = dayTotal
                    )
                )
            }
        }

        // Frequently used category
        val mostFrequentGroup = categoryGroups.maxByOrNull { it.value.size }
        if (mostFrequentGroup != null && mostFrequentGroup.value.size >= 3) {
            trendInsights.add(
                InsightItem(
                    id = "trend_frequent_cat",
                    title = "Most Frequent Purchases",
                    description = "${mostFrequentGroup.key} is your most frequent spending area with ${mostFrequentGroup.value.size} purchases.",
                    emoji = "🛍️",
                    type = InsightType.TREND
                )
            )
        }

        // 7. Money Leak Detection
        val smallPurchasesThreshold = 500.0 // KSh 500 or under
        val smallPurchases = currentExpenses.filter { it.amount <= smallPurchasesThreshold }
        val moneyLeakInsight = if (smallPurchases.size >= 3) {
            val totalSmall = smallPurchases.sumOf { it.amount }
            val formattedTotalSmall = DateTimeUtils.formatCurrency(totalSmall, currency)
            val formattedThreshold = DateTimeUtils.formatCurrency(smallPurchasesThreshold, currency)
            InsightItem(
                id = "money_leak_small_purchases",
                title = "Small purchases added up",
                description = "You made ${smallPurchases.size} purchases under $formattedThreshold this ${timeframe.lowercase()}. Together they totaled $formattedTotalSmall.",
                emoji = "🚨",
                type = InsightType.MONEY_LEAK,
                highlightAmount = totalSmall
            )
        } else null

        // 8. Recurring Transactions Detection
        val recurringInsights = detectRecurringTransactions(expensesOnly, currency)

        // 9. Unusual Spending Detection
        val unusualTransactions = detectUnusualTransactions(currentExpenses, expensesOnly, currency)

        return InsightsData(
            hasEnoughData = true,
            totalTransactionsCount = currentTx.size,
            currentPeriodLabel = periodLabel,
            totalSpentCurrentPeriod = currentSpent,
            totalReceivedCurrentPeriod = currentReceived,
            totalSpentPreviousPeriod = prevSpent,
            periodDifference = periodDiff,
            periodPercentageChange = periodPctChange,
            hasPreviousPeriodData = hasPrevData,
            averageDailySpending = avgDailySpending,
            topCategoryName = topCategoryName,
            topCategoryAmount = topCategoryAmount,
            weeklySummary = weeklySummary,
            monthlySummary = monthlySummary,
            categoryComparisons = categoryComparisons,
            trendInsights = trendInsights,
            moneyLeakInsight = moneyLeakInsight,
            recurringInsights = recurringInsights,
            unusualTransactions = unusualTransactions
        )
    }

    private fun emptyInsightsData(totalCount: Int, settings: UserSettingsEntity): InsightsData {
        val now = System.currentTimeMillis()
        val dummySummary = PeriodSummary(
            title = "Summary",
            received = 0.0,
            spent = 0.0,
            target = settings.dailyTarget,
            remainingBudget = settings.dailyTarget,
            averageDailySpending = 0.0,
            topCategory = "None",
            budgetStatusLabel = "🟢 Within target",
            isWithinTarget = true
        )
        return InsightsData(
            hasEnoughData = false,
            totalTransactionsCount = totalCount,
            currentPeriodLabel = "This Week",
            totalSpentCurrentPeriod = 0.0,
            totalReceivedCurrentPeriod = 0.0,
            totalSpentPreviousPeriod = 0.0,
            periodDifference = 0.0,
            periodPercentageChange = 0.0,
            hasPreviousPeriodData = false,
            averageDailySpending = 0.0,
            topCategoryName = null,
            topCategoryAmount = 0.0,
            weeklySummary = dummySummary,
            monthlySummary = dummySummary,
            categoryComparisons = emptyList(),
            trendInsights = emptyList(),
            moneyLeakInsight = null,
            recurringInsights = emptyList(),
            unusualTransactions = emptyList()
        )
    }

    private fun getPeriodBoundaries(timeframe: String, now: Long): PeriodBoundaries {
        val cal = Calendar.getInstance().apply { timeInMillis = now }

        return when (timeframe) {
            "WEEK" -> {
                // Current week
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val currStart = cal.timeInMillis

                val currEnd = now

                // Previous week
                cal.add(Calendar.WEEK_OF_YEAR, -1)
                val prevStart = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val prevEnd = cal.timeInMillis

                PeriodBoundaries(currStart, currEnd, prevStart, prevEnd, "This Week")
            }
            "MONTH" -> {
                // Current month
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val currStart = cal.timeInMillis

                val currEnd = now

                // Previous month
                cal.add(Calendar.MONTH, -1)
                val prevStart = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val prevEnd = cal.timeInMillis

                PeriodBoundaries(currStart, currEnd, prevStart, prevEnd, "This Month")
            }
            else -> {
                // All Time
                PeriodBoundaries(0L, now, 0L, 0L, "All Time")
            }
        }
    }

    private data class PeriodBoundaries(
        val currentStart: Long,
        val currentEnd: Long,
        val prevStart: Long,
        val prevEnd: Long,
        val label: String
    )

    private fun calculateDaysInPeriod(timeframe: String, startTimestamp: Long, now: Long): Int {
        val diffMs = (now - startTimestamp).coerceAtLeast(0L)
        val days = (diffMs / (1000 * 60 * 60 * 24)).toInt() + 1
        return when (timeframe) {
            "WEEK" -> days.coerceIn(1, 7)
            "MONTH" -> days.coerceIn(1, 31)
            else -> days.coerceAtLeast(1)
        }
    }

    private fun computeWeeklySummary(
        transactions: List<TransactionEntity>,
        settings: UserSettingsEntity,
        now: Long
    ): PeriodSummary {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfWeek = cal.timeInMillis

        val weekTx = transactions.filter { it.timestamp >= startOfWeek }
        val weekExpenses = weekTx.filter { it.type == TransactionType.EXPENSE.name }
        val weekIncome = weekTx.filter { it.type == TransactionType.INCOME.name }

        val spent = weekExpenses.sumOf { it.amount }
        val received = weekIncome.sumOf { it.amount }

        val daysSoFar = calculateDaysInPeriod("WEEK", startOfWeek, now)
        val avgDaily = spent / daysSoFar

        val topCat = weekExpenses.groupBy { it.category }
            .maxByOrNull { it.value.sumOf { tx -> tx.amount } }?.key ?: "None"

        val weeklyTarget = settings.dailyTarget * 7
        val isWithin = spent <= weeklyTarget

        return PeriodSummary(
            title = "This Week",
            received = received,
            spent = spent,
            target = weeklyTarget,
            remainingBudget = (weeklyTarget - spent).coerceAtLeast(0.0),
            averageDailySpending = avgDaily,
            topCategory = topCat,
            budgetStatusLabel = if (isWithin) "🟢 Within target" else "🔴 Target exceeded",
            isWithinTarget = isWithin
        )
    }

    private fun computeMonthlySummary(
        transactions: List<TransactionEntity>,
        settings: UserSettingsEntity,
        now: Long
    ): PeriodSummary {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfMonth = cal.timeInMillis

        val monthTx = transactions.filter { it.timestamp >= startOfMonth }
        val monthExpenses = monthTx.filter { it.type == TransactionType.EXPENSE.name }
        val monthIncome = monthTx.filter { it.type == TransactionType.INCOME.name }

        val spent = monthExpenses.sumOf { it.amount }
        val received = monthIncome.sumOf { it.amount }

        val dayOfMonth = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        val avgDaily = spent / dayOfMonth

        val topCat = monthExpenses.groupBy { it.category }
            .maxByOrNull { it.value.sumOf { tx -> tx.amount } }?.key ?: "None"

        val monthlyTarget = settings.monthlyTarget
        val isWithin = spent <= monthlyTarget

        return PeriodSummary(
            title = "This Month",
            received = received,
            spent = spent,
            target = monthlyTarget,
            remainingBudget = (monthlyTarget - spent).coerceAtLeast(0.0),
            averageDailySpending = avgDaily,
            topCategory = topCat,
            budgetStatusLabel = if (isWithin) "🟢 Within target" else "🔴 Target exceeded",
            isWithinTarget = isWithin
        )
    }

    private fun detectRecurringTransactions(
        allExpenses: List<TransactionEntity>,
        currency: String
    ): List<InsightItem> {
        val results = mutableListOf<InsightItem>()
        if (allExpenses.size < 4) return results

        // Group by title/party name (lowercase)
        val grouped = allExpenses.groupBy { it.title.trim().lowercase(Locale.ROOT) }

        grouped.forEach { (rawTitle, txList) ->
            if (txList.size >= 2) {
                val sample = txList.first()
                val displayTitle = sample.title
                val avgAmount = txList.map { it.amount }.average()
                
                // Check if amounts are similar (within 15% tolerance)
                val matchesAmount = txList.all { Math.abs(it.amount - avgAmount) / avgAmount <= 0.15 }

                if (matchesAmount) {
                    val formattedAmt = DateTimeUtils.formatCurrency(avgAmount, currency)
                    results.add(
                        InsightItem(
                            id = "recurring_$rawTitle",
                            title = "Possible recurring payment",
                            description = "$formattedAmt appears regularly for $displayTitle.",
                            emoji = "🔄",
                            type = InsightType.RECURRING,
                            highlightAmount = avgAmount
                        )
                    )
                }
            }
        }

        return results.take(3)
    }

    private fun detectUnusualTransactions(
        currentExpenses: List<TransactionEntity>,
        allExpenses: List<TransactionEntity>,
        currency: String
    ): List<InsightItem> {
        if (currentExpenses.isEmpty() || allExpenses.size < 3) return emptyList()

        val avgAmount = allExpenses.map { it.amount }.average()
        val threshold = (avgAmount * 2.5).coerceAtLeast(1000.0)

        return currentExpenses.filter { it.amount >= threshold }
            .sortedByDescending { it.amount }
            .take(2)
            .map { tx ->
                val formattedAmt = DateTimeUtils.formatCurrency(tx.amount, currency)
                InsightItem(
                    id = "unusual_${tx.id}",
                    title = "Unusually large transaction",
                    description = "$formattedAmt for ${tx.title} is significantly higher than your average transaction.",
                    emoji = "🟠",
                    type = InsightType.UNUSUAL,
                    highlightAmount = tx.amount
                )
            }
    }

    private fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Day"
        }
    }
}

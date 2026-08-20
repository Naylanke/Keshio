package com.example.data.model

enum class BudgetStatus(
    val label: String,
    val headline: String,
    val description: String,
    val emoji: String
) {
    SAFE(
        label = "Safe",
        headline = "You're doing well",
        description = "Spending is well within today's target",
        emoji = "🟢"
    ),
    GETTING_CLOSE(
        label = "Getting Close",
        headline = "You're getting close",
        description = "Approaching your daily spending target",
        emoji = "🟠"
    ),
    OVER_BUDGET(
        label = "Over Budget",
        headline = "Daily target exceeded",
        description = "You have exceeded today's spending target",
        emoji = "🔴"
    );

    companion object {
        // Compatibility aliases
        val ON_TRACK = SAFE
        val APPROACHING_LIMIT = GETTING_CLOSE

        fun fromPercentage(percentage: Float): BudgetStatus {
            return when {
                percentage >= 100f -> OVER_BUDGET
                percentage >= 70f -> GETTING_CLOSE
                else -> SAFE
            }
        }
    }
}


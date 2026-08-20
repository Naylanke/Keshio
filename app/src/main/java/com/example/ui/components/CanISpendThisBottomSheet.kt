package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavingsGoalEntity
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import com.example.util.DateTimeUtils

@Composable
fun CanISpendThisCard(
    onOpenTool: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenTool() }
            .testTag("can_i_spend_this_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionMark,
                        contentDescription = "Can I Spend This",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Can I Spend This?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Test a planned purchase against your budget & goals",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Open Tool",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanISpendThisBottomSheet(
    todaySpent: Double,
    dailyTarget: Double,
    currencySymbol: String = "KSh",
    activeGoals: List<SavingsGoalEntity> = emptyList(),
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountInput by remember { mutableStateOf("") }

    val plannedAmount = amountInput.toDoubleOrNull() ?: 0.0
    val remainingBefore = dailyTarget - todaySpent
    val projectedTotal = todaySpent + plannedAmount
    val projectedRemaining = dailyTarget - projectedTotal

    val quickAmounts = listOf(100.0, 200.0, 500.0, 1000.0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("can_i_spend_this_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Decision Tool",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Can I Spend This?",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Enter an amount to see how it affects your daily budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Amount Buttons
            Text(
                text = "Quick Amounts",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickAmounts.forEach { amt ->
                    val isSelected = amountInput == amt.toInt().toString()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                amountInput = amt.toInt().toString()
                            }
                            .padding(vertical = 10.dp)
                            .testTag("quick_amount_${amt.toInt()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$currencySymbol ${amt.toInt()}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Amount Input
            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Planned Amount ($currencySymbol)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("planned_amount_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Evaluation Result Section
            if (plannedAmount > 0) {
                val isWithin = projectedRemaining >= 0
                val wasAlreadyOver = remainingBefore < 0

                val statusTitle: String
                val statusMessage: String
                val statusColor: Color
                val bgContainer: Color

                when {
                    isWithin -> {
                        statusTitle = "🟢 Within today's target"
                        statusMessage = "This fits your current budget. You would have approximately ${DateTimeUtils.formatCurrency(projectedRemaining, currencySymbol)} remaining today."
                        statusColor = IncomeGreen
                        bgContainer = IncomeGreen.copy(alpha = 0.08f)
                    }
                    !wasAlreadyOver && remainingBefore > 0 -> {
                        val overBy = plannedAmount - remainingBefore
                        statusTitle = "🟠 Be careful"
                        statusMessage = "You have ${DateTimeUtils.formatCurrency(remainingBefore, currencySymbol)} remaining in today's target. Spending ${DateTimeUtils.formatCurrency(plannedAmount, currencySymbol)} would put you ${DateTimeUtils.formatCurrency(overBy, currencySymbol)} over it."
                        statusColor = StatusOrange
                        bgContainer = StatusOrange.copy(alpha = 0.08f)
                    }
                    else -> {
                        val totalOver = Math.abs(projectedRemaining)
                        statusTitle = "🔴 Over today's target"
                        statusMessage = "This purchase would put you ${DateTimeUtils.formatCurrency(totalOver, currencySymbol)} over today's target."
                        statusColor = StatusRed
                        bgContainer = StatusRed.copy(alpha = 0.08f)
                    }
                }

                // Status Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("spend_evaluation_result_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = bgContainer),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = statusTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Budget Impact Breakdown
                        Text(
                            text = "Budget Impact Breakdown",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        BreakdownRow("Current spending today:", DateTimeUtils.formatCurrency(todaySpent, currencySymbol))
                        BreakdownRow("Planned purchase:", "+${DateTimeUtils.formatCurrency(plannedAmount, currencySymbol)}", isBold = true)
                        BreakdownRow("Total projected spending:", DateTimeUtils.formatCurrency(projectedTotal, currencySymbol))
                        BreakdownRow("Daily target:", DateTimeUtils.formatCurrency(dailyTarget, currencySymbol))

                        Spacer(modifier = Modifier.height(6.dp))

                        if (isWithin) {
                            BreakdownRow("Remaining target:", DateTimeUtils.formatCurrency(projectedRemaining, currencySymbol), valueColor = IncomeGreen, isBold = true)
                        } else {
                            BreakdownRow("Amount over target:", DateTimeUtils.formatCurrency(Math.abs(projectedRemaining), currencySymbol), valueColor = StatusRed, isBold = true)
                        }
                    }
                }

                // Savings Goal Impact section
                val topGoal = activeGoals.firstOrNull()
                if (topGoal != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val goalRemaining = (topGoal.targetAmount - topGoal.currentAmount).coerceAtLeast(0.0)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = "Savings Goal",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Your '${topGoal.name}' goal is ${DateTimeUtils.formatCurrency(goalRemaining, currencySymbol)} away from completion.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Enter an amount above or choose a quick option to see real-time budget impact.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            ),
            color = valueColor
        )
    }
}

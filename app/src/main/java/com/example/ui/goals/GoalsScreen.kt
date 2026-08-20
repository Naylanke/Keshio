package com.example.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavingsGoalEntity
import com.example.ui.KeshioUiState
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.StatusOrange
import com.example.util.DateTimeUtils

@Composable
fun GoalsScreen(
    uiState: KeshioUiState,
    onAddGoalClick: () -> Unit,
    onEditGoalClick: (SavingsGoalEntity) -> Unit,
    onAddMoneyClick: (SavingsGoalEntity) -> Unit,
    onTogglePauseGoal: (SavingsGoalEntity) -> Unit,
    onToggleCompleteGoal: (SavingsGoalEntity) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = uiState.userSettings.currencySymbol
    val goals = uiState.goals

    val activeGoals = goals.filter { !it.isCompleted }
    val completedGoals = goals.filter { it.isCompleted }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("goals_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header & Create Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Savings Goals",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Track progress toward what you are saving for",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAddGoalClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("create_goal_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Goal",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Goal", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Empty State
        if (goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .testTag("goals_empty_state_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = "Goals",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Savings Goals Yet",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Set up a goal for a new phone, emergency fund, or trip to stay motivated.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onAddGoalClick,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("empty_state_add_goal_btn")
                        ) {
                            Text("Create Your First Goal")
                        }
                    }
                }
            }
        } else {
            // Active Goals Header
            if (activeGoals.isNotEmpty()) {
                item {
                    Text(
                        text = "Active Goals (${activeGoals.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 8.dp)
                    )
                }

                items(activeGoals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        currencySymbol = currency,
                        onEditGoalClick = onEditGoalClick,
                        onAddMoneyClick = onAddMoneyClick,
                        onTogglePauseGoal = onTogglePauseGoal,
                        onToggleCompleteGoal = onToggleCompleteGoal,
                        onDeleteGoal = onDeleteGoal
                    )
                }
            }

            // Completed Goals Header
            if (completedGoals.isNotEmpty()) {
                item {
                    Text(
                        text = "Completed Goals 🎉 (${completedGoals.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = IncomeGreen,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
                    )
                }

                items(completedGoals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        currencySymbol = currency,
                        onEditGoalClick = onEditGoalClick,
                        onAddMoneyClick = onAddMoneyClick,
                        onTogglePauseGoal = onTogglePauseGoal,
                        onToggleCompleteGoal = onToggleCompleteGoal,
                        onDeleteGoal = onDeleteGoal
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: SavingsGoalEntity,
    currencySymbol: String,
    onEditGoalClick: (SavingsGoalEntity) -> Unit,
    onAddMoneyClick: (SavingsGoalEntity) -> Unit,
    onTogglePauseGoal: (SavingsGoalEntity) -> Unit,
    onToggleCompleteGoal: (SavingsGoalEntity) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
    val pct = if (goal.targetAmount > 0) ((goal.currentAmount / goal.targetAmount) * 100).coerceAtMost(100.0) else 0.0
    val is100Percent = pct >= 100.0 || goal.isCompleted

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("goal_card_${goal.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (is100Percent) IncomeGreen.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (is100Percent) IncomeGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Completion Banner if 100%
            if (is100Percent) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(IncomeGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎉", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Goal completed! You've reached your ${DateTimeUtils.formatCurrency(goal.targetAmount, currencySymbol)} target for ${goal.name}.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = IncomeGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Title & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    is100Percent -> IncomeGreen.copy(alpha = 0.2f)
                                    goal.isPaused -> StatusOrange.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (is100Percent) Icons.Default.CheckCircle else Icons.Default.Flag,
                            contentDescription = goal.name,
                            tint = if (is100Percent) IncomeGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (goal.isPaused) {
                            Text(
                                text = "Paused",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = StatusOrange
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "%.0f%% complete".format(pct),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (is100Percent) IncomeGreen else MaterialTheme.colorScheme.primary
                        )
                    )

                    Box {
                        IconButton(onClick = { isMenuExpanded = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Goal") },
                                onClick = {
                                    isMenuExpanded = false
                                    onEditGoalClick(goal)
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )

                            DropdownMenuItem(
                                text = { Text(if (goal.isPaused) "Resume Goal" else "Pause Goal") },
                                onClick = {
                                    isMenuExpanded = false
                                    onTogglePauseGoal(goal)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (goal.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = null
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (goal.isCompleted) "Re-open Goal" else "Mark Completed") },
                                onClick = {
                                    isMenuExpanded = false
                                    onToggleCompleteGoal(goal)
                                },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                            )

                            DropdownMenuItem(
                                text = { Text("Delete Goal", color = ExpenseRose) },
                                onClick = {
                                    isMenuExpanded = false
                                    onDeleteGoal(goal)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { (pct / 100f).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (is100Percent) IncomeGreen else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Financial breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Target", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = DateTimeUtils.formatCurrency(goal.targetAmount, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = DateTimeUtils.formatCurrency(goal.currentAmount, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = IncomeGreen)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = DateTimeUtils.formatCurrency(remaining, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            // Target Date & Saving Recommendation if available
            if (goal.targetDate != null && !is100Percent) {
                val now = System.currentTimeMillis()
                val diffMs = (goal.targetDate - now).coerceAtLeast(0L)
                val daysRemaining = (diffMs / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)

                val dailyRec = if (daysRemaining > 0) remaining / daysRemaining else remaining

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Days",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$daysRemaining days remaining",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "Rec: ${DateTimeUtils.formatCurrency(dailyRec, currencySymbol)}/day",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "💡 Calculation based on target date, not financial advice.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Quick Add Savings Button if not 100%
            if (!is100Percent) {
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { onAddMoneyClick(goal) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("add_savings_btn_${goal.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = "Add Savings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Savings Progress",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

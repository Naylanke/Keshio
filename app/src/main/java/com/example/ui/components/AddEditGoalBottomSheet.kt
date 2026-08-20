package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavingsGoalEntity
import com.example.ui.theme.ExpenseRose
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGoalBottomSheet(
    goalToEdit: SavingsGoalEntity? = null,
    currencySymbol: String = "KSh",
    onDismiss: () -> Unit,
    onSave: (id: Long?, name: String, targetAmount: Double, currentAmount: Double, targetDate: Long?) -> Unit,
    onDelete: ((SavingsGoalEntity) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(goalToEdit?.name ?: "") }
    var targetAmountText by remember { mutableStateOf(goalToEdit?.targetAmount?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var currentAmountText by remember { mutableStateOf(goalToEdit?.currentAmount?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var targetDate by remember { mutableStateOf<Long?>(goalToEdit?.targetDate) }

    var isDatePickerOpen by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("add_edit_goal_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Goal",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (goalToEdit != null) "Edit Savings Goal" else "Create Savings Goal",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (goalToEdit != null && onDelete != null) {
                    IconButton(
                        onClick = {
                            onDelete(goalToEdit)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("delete_goal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Goal",
                            tint = ExpenseRose
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                label = { Text("Goal Name (e.g. New Phone, Emergency Fund)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_name_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Target Amount
            OutlinedTextField(
                value = targetAmountText,
                onValueChange = {
                    targetAmountText = it
                    errorMessage = null
                },
                label = { Text("Target Amount ($currencySymbol)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_target_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Initial Saved Amount
            OutlinedTextField(
                value = currentAmountText,
                onValueChange = {
                    currentAmountText = it
                    errorMessage = null
                },
                label = { Text("Current Saved Amount ($currencySymbol)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_current_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Target Date Selector
            OutlinedButton(
                onClick = { isDatePickerOpen = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("goal_target_date_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (targetDate != null) "Target Date: ${DateTimeUtils.formatDate(targetDate!!)}" else "Set Target Date (Optional)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (targetDate != null) {
                        TextButton(
                            onClick = { targetDate = null }
                        ) {
                            Text("Clear", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = ExpenseRose
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    val parsedTarget = targetAmountText.toDoubleOrNull()
                    val parsedCurrent = currentAmountText.toDoubleOrNull() ?: 0.0

                    if (name.trim().isEmpty()) {
                        errorMessage = "Please enter a name for your goal"
                        return@Button
                    }
                    if (parsedTarget == null || parsedTarget <= 0) {
                        errorMessage = "Please enter a valid target amount"
                        return@Button
                    }

                    onSave(
                        goalToEdit?.id,
                        name.trim(),
                        parsedTarget,
                        parsedCurrent,
                        targetDate
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_goal_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (goalToEdit != null) "Update Goal" else "Save Goal",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    if (isDatePickerOpen) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = targetDate ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
        )
        DatePickerDialog(
            onDismissRequest = { isDatePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        targetDate = datePickerState.selectedDateMillis
                        isDatePickerOpen = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerOpen = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.data.model.TransactionCategories
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.IncomeGreen
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionBottomSheet(
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    transactionToEdit: TransactionEntity? = null,
    currencySymbol: String = "$",
    onDismiss: () -> Unit,
    onSave: (
        id: Long?,
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        note: String,
        timestamp: Long
    ) -> Unit,
    onDelete: ((TransactionEntity) -> Unit)? = null
) {
    val isEditing = transactionToEdit != null

    var title by remember { mutableStateOf(transactionToEdit?.title ?: "") }
    var amountText by remember {
        mutableStateOf(
            if (transactionToEdit != null) {
                if (transactionToEdit.amount % 1.0 == 0.0) {
                    transactionToEdit.amount.toInt().toString()
                } else {
                    transactionToEdit.amount.toString()
                }
            } else ""
        )
    }
    var selectedType by remember {
        mutableStateOf(
            if (transactionToEdit != null) {
                if (transactionToEdit.type == TransactionType.INCOME.name) TransactionType.INCOME else TransactionType.EXPENSE
            } else TransactionType.EXPENSE
        )
    }
    var selectedCategory by remember {
        mutableStateOf(
            transactionToEdit?.category ?: if (selectedType == TransactionType.EXPENSE) "Food & Dining" else "Salary & Wages"
        )
    }
    var note by remember { mutableStateOf(transactionToEdit?.note ?: "") }
    var timestamp by remember { mutableStateOf(transactionToEdit?.timestamp ?: System.currentTimeMillis()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Edit Transaction" else "Add Transaction",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Type Toggle (Spent vs Received)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                // Spent Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedType == TransactionType.EXPENSE) ExpenseRose else Color.Transparent)
                        .clickable {
                            selectedType = TransactionType.EXPENSE
                            if (selectedCategory == "Salary & Wages" || selectedCategory == "Freelance & Business") {
                                selectedCategory = "Food & Dining"
                            }
                        }
                        .padding(vertical = 10.dp)
                        .testTag("type_spent_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Spent (Expense)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selectedType == TransactionType.EXPENSE) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (selectedType == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Received Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedType == TransactionType.INCOME) IncomeGreen else Color.Transparent)
                        .clickable {
                            selectedType = TransactionType.INCOME
                            if (selectedCategory == "Food & Dining" || selectedCategory == "Groceries") {
                                selectedCategory = "Salary & Wages"
                            }
                        }
                        .padding(vertical = 10.dp)
                        .testTag("type_received_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Received (Income)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selectedType == TransactionType.INCOME) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (selectedType == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Input
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                        amountText = input
                        errorMessage = null
                    }
                },
                label = { Text("Amount ($currencySymbol)") },
                placeholder = { Text("0.00") },
                prefix = {
                    Text(
                        text = "$currencySymbol ",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRose
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Title / Merchant Input
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorMessage = null
                },
                label = { Text("Description / Merchant") },
                placeholder = {
                    Text(
                        if (selectedType == TransactionType.EXPENSE) "e.g., Grocery Supermarket, Coffee, Lunch"
                        else "e.g., Salary, Client Project Payout, Transfer"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("title_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Category Selection Header & FlowRow
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            val availableCategories = TransactionCategories.allCategories.filter {
                if (selectedType == TransactionType.INCOME) {
                    it.defaultType == TransactionType.INCOME || it.name == "Other"
                } else {
                    it.defaultType == TransactionType.EXPENSE || it.name == "Other"
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCategories.forEach { cat ->
                    val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) cat.color.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) cat.color else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedCategory = cat.name }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("category_chip_${cat.name}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = cat.name,
                            tint = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Date Selection Shortcut Chips
            Text(
                text = "Date",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()

            val isTodaySelected = remember(timestamp) {
                cal.timeInMillis = now
                val nowDay = cal.get(Calendar.DAY_OF_YEAR)
                val nowYear = cal.get(Calendar.YEAR)
                cal.timeInMillis = timestamp
                cal.get(Calendar.DAY_OF_YEAR) == nowDay && cal.get(Calendar.YEAR) == nowYear
            }

            val isYesterdaySelected = remember(timestamp) {
                cal.timeInMillis = now
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yestDay = cal.get(Calendar.DAY_OF_YEAR)
                val yestYear = cal.get(Calendar.YEAR)
                cal.timeInMillis = timestamp
                cal.get(Calendar.DAY_OF_YEAR) == yestDay && cal.get(Calendar.YEAR) == yestYear
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Today chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isTodaySelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            1.dp,
                            if (isTodaySelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { timestamp = System.currentTimeMillis() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isTodaySelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isTodaySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Yesterday chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isYesterdaySelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            1.dp,
                            if (isYesterdaySelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            val c = Calendar.getInstance()
                            c.add(Calendar.DAY_OF_YEAR, -1)
                            timestamp = c.timeInMillis
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Yesterday",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isYesterdaySelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isYesterdaySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Optional Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note / Memo (Optional)") },
                placeholder = { Text("Add extra details...") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input"),
                shape = RoundedCornerShape(14.dp)
            )

            if (isEditing && transactionToEdit != null && (transactionToEdit.referenceId != null || transactionToEdit.source != "MANUAL")) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "TRANSACTION METADATA",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (transactionToEdit.referenceId != null) {
                            Text(
                                text = "Reference: ${transactionToEdit.referenceId}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (transactionToEdit.balanceAfter != null) {
                            Text(
                                text = "M-Pesa Balance After: $currencySymbol ${"%,.2f".format(transactionToEdit.balanceAfter)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (transactionToEdit.transactionFee != null && transactionToEdit.transactionFee > 0.0) {
                            Text(
                                text = "Transaction Fee: $currencySymbol ${"%,.2f".format(transactionToEdit.transactionFee)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Origin Source: ${transactionToEdit.source}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isEditing && onDelete != null && transactionToEdit != null) {
                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            onDelete(transactionToEdit)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("delete_transaction_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete")
                    }
                }

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            errorMessage = "Please enter a valid amount greater than 0"
                            return@Button
                        }
                        if (title.isBlank()) {
                            errorMessage = "Please enter a description or merchant name"
                            return@Button
                        }

                        focusManager.clearFocus()
                        onSave(
                            transactionToEdit?.id,
                            title,
                            amount,
                            selectedType,
                            selectedCategory,
                            note,
                            timestamp
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(if (isEditing) 1.5f else 1f)
                        .height(50.dp)
                        .testTag("save_transaction_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == TransactionType.INCOME) IncomeGreen else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Save",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Update Transaction" else "Save Transaction",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

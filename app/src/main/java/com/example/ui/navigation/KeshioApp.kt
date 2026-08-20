package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.KeshioViewModel
import com.example.ui.components.AddEditTransactionBottomSheet
import com.example.ui.home.HomeScreen
import com.example.ui.insights.InsightsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.transactions.TransactionsScreen

enum class KeshioTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
    GOALS("Goals", Icons.Filled.Savings, Icons.Outlined.Savings, "nav_tab_goals"),
    TRANSACTIONS("Transactions", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_tab_transactions"),
    INSIGHTS("Insights", Icons.Filled.PieChart, Icons.Outlined.PieChart, "nav_tab_insights"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_tab_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeshioApp(
    viewModel: KeshioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.onAppBackgrounded()
            } else if (event == Lifecycle.Event.ON_START) {
                viewModel.onAppResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var currentTab by remember { mutableStateOf(KeshioTab.HOME) }
    var isAddEditSheetOpen by remember { mutableStateOf(false) }
    var isSmsSimulatorOpen by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    var isAddEditGoalSheetOpen by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var addingMoneyGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    MyApplicationTheme(themeMode = uiState.userSettings.themeMode) {
        if (!uiState.userSettings.onboardingCompleted) {
            com.example.ui.onboarding.OnboardingScreen(
                onCompleteOnboarding = { daily, monthly, lockType, pin, sms ->
                    viewModel.completeOnboarding(daily, monthly, lockType, pin, sms)
                }
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            windowInsets = WindowInsets.navigationBars,
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                .testTag("keshio_bottom_nav")
                        ) {
                            KeshioTab.entries.forEach { tab ->
                                val isSelected = currentTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            KeshioTab.HOME -> {
                                HomeScreen(
                                    uiState = uiState,
                                    onAddTransactionClick = {
                                        editingTransaction = null
                                        isAddEditSheetOpen = true
                                    },
                                    onEditTransactionClick = { tx ->
                                        editingTransaction = tx
                                        isAddEditSheetOpen = true
                                    },
                                    onDeleteTransactionClick = { tx ->
                                        viewModel.deleteTransaction(tx)
                                    },
                                    onNavigateToTransactions = {
                                        currentTab = KeshioTab.TRANSACTIONS
                                    },
                                    onNavigateToGoals = {
                                        currentTab = KeshioTab.GOALS
                                    },
                                    onUpdateSmsTracking = { viewModel.updateSmsTracking(it) },
                                    onOpenSmsSimulator = { isSmsSimulatorOpen = true }
                                )
                            }

                            KeshioTab.GOALS -> {
                                com.example.ui.goals.GoalsScreen(
                                    uiState = uiState,
                                    onAddGoalClick = {
                                        editingGoal = null
                                        isAddEditGoalSheetOpen = true
                                    },
                                    onEditGoalClick = { goal ->
                                        editingGoal = goal
                                        isAddEditGoalSheetOpen = true
                                    },
                                    onAddMoneyClick = { goal ->
                                        addingMoneyGoal = goal
                                    },
                                    onTogglePauseGoal = { goal ->
                                        viewModel.togglePauseGoal(goal)
                                    },
                                    onToggleCompleteGoal = { goal ->
                                        viewModel.toggleCompleteGoal(goal)
                                    },
                                    onDeleteGoal = { goal ->
                                        viewModel.deleteGoal(goal)
                                    }
                                )
                            }

                            KeshioTab.TRANSACTIONS -> {
                                TransactionsScreen(
                                    uiState = uiState,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onTypeFilterChange = { viewModel.setTypeFilter(it) },
                                    onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                                    onAddTransactionClick = {
                                        editingTransaction = null
                                        isAddEditSheetOpen = true
                                    },
                                    onEditTransactionClick = { tx ->
                                        editingTransaction = tx
                                        isAddEditSheetOpen = true
                                    },
                                    onDeleteTransactionClick = { tx ->
                                        viewModel.deleteTransaction(tx)
                                    }
                                )
                            }

                            KeshioTab.INSIGHTS -> {
                                InsightsScreen(
                                    uiState = uiState,
                                    onTimeframeChange = { viewModel.setInsightsTimeframe(it) }
                                )
                            }

                            KeshioTab.SETTINGS -> {
                                SettingsScreen(
                                    uiState = uiState,
                                    onUpdateDailyTarget = { viewModel.updateDailyTarget(it) },
                                    onUpdateMonthlyTarget = { viewModel.updateMonthlyTarget(it) },
                                    onUpdateCurrency = { viewModel.updateCurrency(it) },
                                    onUpdateThemeMode = { viewModel.updateThemeMode(it) },
                                    onUpdateSmsTracking = { viewModel.updateSmsTracking(it) },
                                    onUpdateTestMode = { viewModel.updateTestMode(it) },
                                    onUpdateBudgetWarnings = { viewModel.updateBudgetWarnings(it) },
                                    onUpdateTransactionNotifications = { viewModel.updateTransactionNotifications(it) },
                                    onUpdateEndOfDaySummary = { viewModel.updateEndOfDaySummary(it) },
                                    onOpenSmsSimulator = { isSmsSimulatorOpen = true },
                                    onRestoreSampleData = { viewModel.restoreSampleData() },
                                    onClearAllData = { viewModel.clearAllTransactionsOnly() },
                                    onDeleteAllKeshioData = { viewModel.deleteAllKeshioData() },
                                    onUpdateDetailedNotifications = { viewModel.updateDetailedNotifications(it) },
                                    onUpdateAppLock = { enabled, lockType, pin, timing ->
                                        viewModel.updateAppLock(enabled, lockType, pin, timing)
                                    }
                                )
                            }
                        }
                    }

                    if (isSmsSimulatorOpen) {
                        com.example.ui.components.SmsSimulatorBottomSheet(
                            onDismiss = { isSmsSimulatorOpen = false },
                            testModeEnabled = uiState.userSettings.testModeEnabled,
                            onTestModeToggled = { viewModel.updateTestMode(it) },
                            onParsePreview = { sender, message ->
                                viewModel.parseSmsPreview(sender, message)
                            },
                            onProcessSms = { sender, message, isSim, onResult ->
                                viewModel.processAndSaveSms(sender, message, isSim, onResult)
                            }
                        )
                    }

                    if (isAddEditSheetOpen) {
                        AddEditTransactionBottomSheet(
                            transactionToEdit = editingTransaction,
                            currencySymbol = uiState.userSettings.currencySymbol,
                            onDismiss = {
                                isAddEditSheetOpen = false
                                editingTransaction = null
                            },
                            onSave = { id, title, amount, type, category, note, timestamp ->
                                if (id != null) {
                                    viewModel.updateTransaction(
                                        id = id,
                                        title = title,
                                        amount = amount,
                                        type = type,
                                        category = category,
                                        note = note,
                                        timestamp = timestamp
                                    )
                                } else {
                                    viewModel.addTransaction(
                                        title = title,
                                        amount = amount,
                                        type = type,
                                        category = category,
                                        note = note,
                                        timestamp = timestamp
                                    )
                                }
                            },
                            onDelete = { tx ->
                                viewModel.deleteTransaction(tx)
                            }
                        )
                    }

                    if (isAddEditGoalSheetOpen) {
                        com.example.ui.components.AddEditGoalBottomSheet(
                            goalToEdit = editingGoal,
                            currencySymbol = uiState.userSettings.currencySymbol,
                            onDismiss = {
                                isAddEditGoalSheetOpen = false
                                editingGoal = null
                            },
                            onSave = { id, name, targetAmount, currentAmount, targetDate ->
                                if (id != null) {
                                    viewModel.updateGoal(id, name, targetAmount, currentAmount, targetDate)
                                } else {
                                    viewModel.addGoal(name, targetAmount, currentAmount, targetDate)
                                }
                            },
                            onDelete = { goal ->
                                viewModel.deleteGoal(goal)
                            }
                        )
                    }

                    if (addingMoneyGoal != null) {
                        com.example.ui.components.AddMoneyToGoalDialog(
                            goal = addingMoneyGoal!!,
                            currencySymbol = uiState.userSettings.currencySymbol,
                            onDismiss = { addingMoneyGoal = null },
                            onAddMoney = { amount ->
                                viewModel.addMoneyToGoal(addingMoneyGoal!!, amount)
                                addingMoneyGoal = null
                            }
                        )
                    }
                }

                if (uiState.isAppLocked) {
                    com.example.ui.security.AppLockOverlayScreen(
                        appLockType = uiState.userSettings.appLockType,
                        storedPinHash = uiState.userSettings.pinHash,
                        onUnlocked = { viewModel.unlockApp() }
                    )
                }
            }
        }
    }
}

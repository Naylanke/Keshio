package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KeshioDatabase
import com.example.data.local.TransactionEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.model.BudgetStatus
import com.example.data.model.CategoryInfo
import com.example.data.model.TransactionCategories
import com.example.data.model.TransactionType
import com.example.data.repository.KeshioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategorySpend(
    val category: String,
    val categoryInfo: CategoryInfo,
    val totalAmount: Double,
    val percentage: Float,
    val count: Int
)

data class KeshioUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val userSettings: UserSettingsEntity = UserSettingsEntity(),
    
    // Today's metrics
    val todayReceived: Double = 0.0,
    val todaySpent: Double = 0.0,
    val dailyTarget: Double = 1500.0,
    val dailyRemaining: Double = 1500.0,
    val dailyBudgetPercentage: Float = 0f,
    val dailyBudgetStatus: BudgetStatus = BudgetStatus.SAFE,
    
    // Monthly metrics & dynamic recommendations
    val monthReceived: Double = 0.0,
    val monthSpent: Double = 0.0,
    val monthlyTarget: Double = 45000.0,
    val monthlyRemaining: Double = 45000.0,
    val monthlyBudgetPercentage: Float = 0f,
    val daysRemainingInMonth: Int = 30,
    val recommendedDailySpending: Double = 1500.0,
    val isMonthlyBudgetExceeded: Boolean = false,
    
    // Insights
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val allTimeIncome: Double = 0.0,
    val allTimeExpense: Double = 0.0,
    val netBalance: Double = 0.0,
    val averageDailySpend: Double = 0.0,
    
    // UI Filters
    val searchQuery: String = "",
    val selectedTypeFilter: String = "ALL", // "ALL", "EXPENSE", "INCOME"
    val selectedCategoryFilter: String? = null,
    val insightsTimeframe: String = "MONTH", // "WEEK", "MONTH", "ALL"
    val isLoading: Boolean = false
)

private data class FilterState(
    val search: String,
    val typeFilter: String,
    val categoryFilter: String?,
    val timeframe: String
)

class KeshioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KeshioRepository
    private val smartBudgetManager: com.example.notification.SmartBudgetManager

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTypeFilter = MutableStateFlow("ALL")
    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    private val _insightsTimeframe = MutableStateFlow("MONTH")

    private val smsEngine: com.example.sms.FinancialSmsEngine

    init {
        val database = KeshioDatabase.getDatabase(application)
        repository = KeshioRepository(database.transactionDao(), database.userSettingsDao())
        smartBudgetManager = com.example.notification.SmartBudgetManager.getInstance(application, repository)
        smsEngine = com.example.sms.FinancialSmsEngine.createForRepository(repository, smartBudgetManager)
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
        }
    }

    fun updateSmsTracking(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSmsTracking(enabled)
        }
    }

    fun updateTestMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateTestMode(enabled)
        }
    }

    fun updateBudgetWarnings(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateBudgetWarnings(enabled)
        }
    }

    fun updateTransactionNotifications(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateTransactionNotifications(enabled)
        }
    }

    fun updateEndOfDaySummary(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateEndOfDaySummary(enabled)
        }
    }

    // Testing and preview triggers for Settings
    fun testApproachingNotification() {
        viewModelScope.launch {
            smartBudgetManager.testApproachingNotification()
        }
    }

    fun testExceededNotification() {
        viewModelScope.launch {
            smartBudgetManager.testExceededNotification()
        }
    }

    fun testTransactionNotification() {
        viewModelScope.launch {
            smartBudgetManager.testTransactionNotification()
        }
    }

    fun testSummaryNotification() {
        viewModelScope.launch {
            smartBudgetManager.testSummaryNotification()
        }
    }

    fun parseSmsPreview(sender: String?, message: String): com.example.sms.ParsedSmsTransaction? {
        return smsEngine.parseOnly(sender, message)
    }

    fun processAndSaveSms(
        sender: String?,
        message: String,
        isSimulation: Boolean = false,
        onResult: (com.example.sms.ProcessSmsResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = smsEngine.processAndSaveSms(
                sender = sender,
                message = message,
                source = if (isSimulation) "SIMULATION" else "SMS"
            )
            onResult(result)
        }
    }

    private val _filterState = combine(
        _searchQuery,
        _selectedTypeFilter,
        _selectedCategoryFilter,
        _insightsTimeframe
    ) { search, typeFilter, categoryFilter, timeframe ->
        FilterState(search, typeFilter, categoryFilter, timeframe)
    }

    val uiState: StateFlow<KeshioUiState> = combine(
        repository.allTransactions,
        repository.userSettings,
        _filterState
    ) { transactions, settings, filters ->
        val safeSettings = settings ?: UserSettingsEntity()
        calculateUiState(
            transactions = transactions,
            settings = safeSettings,
            searchQuery = filters.search,
            typeFilter = filters.typeFilter,
            categoryFilter = filters.categoryFilter,
            timeframe = filters.timeframe
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = KeshioUiState()
    )

    private fun calculateUiState(
        transactions: List<TransactionEntity>,
        settings: UserSettingsEntity,
        searchQuery: String,
        typeFilter: String,
        categoryFilter: String?,
        timeframe: String
    ): KeshioUiState {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // Today's range
        calendar.timeInMillis = now
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfToday = calendar.timeInMillis

        // Month's range
        calendar.timeInMillis = now
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        // Week's range
        calendar.timeInMillis = now
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis

        var todayReceived = 0.0
        var todaySpent = 0.0
        var monthReceived = 0.0
        var monthSpent = 0.0
        var allTimeIncome = 0.0
        var allTimeExpense = 0.0

        transactions.forEach { tx ->
            val isIncome = tx.type == TransactionType.INCOME.name
            if (isIncome) {
                allTimeIncome += tx.amount
                if (tx.timestamp in startOfToday..endOfToday) {
                    todayReceived += tx.amount
                }
                if (tx.timestamp >= startOfMonth) {
                    monthReceived += tx.amount
                }
            } else {
                allTimeExpense += tx.amount
                if (tx.timestamp in startOfToday..endOfToday) {
                    todaySpent += tx.amount
                }
                if (tx.timestamp >= startOfMonth) {
                    monthSpent += tx.amount
                }
            }
        }

        val dailyTarget = settings.dailyTarget
        val dailyRemaining = (dailyTarget - todaySpent).coerceAtLeast(0.0)
        val dailyPercentage = if (dailyTarget > 0) ((todaySpent / dailyTarget) * 100).toFloat() else 0f
        val dailyStatus = when {
            todaySpent >= dailyTarget -> BudgetStatus.OVER_BUDGET
            todaySpent >= dailyTarget * 0.70 -> BudgetStatus.GETTING_CLOSE
            else -> BudgetStatus.SAFE
        }

        val monthlyTarget = settings.monthlyTarget
        val monthlyRemaining = (monthlyTarget - monthSpent).coerceAtLeast(0.0)
        val monthlyPercentage = if (monthlyTarget > 0) ((monthSpent / monthlyTarget) * 100).toFloat() else 0f

        val currentCal = Calendar.getInstance().apply { timeInMillis = now }
        val maxDaysInMonth = currentCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayOfMonth = currentCal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        val daysRemainingInMonth = (maxDaysInMonth - dayOfMonth + 1).coerceAtLeast(1)
        val recommendedDailySpending = if (monthlyRemaining > 0) (monthlyRemaining / daysRemainingInMonth) else 0.0
        val isMonthlyBudgetExceeded = monthSpent > monthlyTarget

        // Filtered transactions for list
        val filtered = transactions.filter { tx ->
            val matchesSearch = searchQuery.isBlank() ||
                tx.title.contains(searchQuery, ignoreCase = true) ||
                tx.category.contains(searchQuery, ignoreCase = true) ||
                tx.note.contains(searchQuery, ignoreCase = true)

            val matchesType = when (typeFilter) {
                "EXPENSE" -> tx.type == TransactionType.EXPENSE.name
                "INCOME" -> tx.type == TransactionType.INCOME.name
                else -> true
            }

            val matchesCategory = categoryFilter == null || tx.category.equals(categoryFilter, ignoreCase = true)

            matchesSearch && matchesType && matchesCategory
        }

        // Category breakdown for Insights based on timeframe
        val insightTransactions = transactions.filter { tx ->
            when (timeframe) {
                "WEEK" -> tx.timestamp >= startOfWeek
                "MONTH" -> tx.timestamp >= startOfMonth
                else -> true
            }
        }

        val expensesOnly = insightTransactions.filter { it.type == TransactionType.EXPENSE.name }
        val totalExpenseInTimeframe = expensesOnly.sumOf { it.amount }

        val categoryGroups = expensesOnly.groupBy { it.category }
        val categoryBreakdown = categoryGroups.map { (catName, txList) ->
            val total = txList.sumOf { it.amount }
            val pct = if (totalExpenseInTimeframe > 0) ((total / totalExpenseInTimeframe) * 100).toFloat() else 0f
            CategorySpend(
                category = catName,
                categoryInfo = TransactionCategories.getCategoryInfo(catName),
                totalAmount = total,
                percentage = pct,
                count = txList.size
            )
        }.sortedByDescending { it.totalAmount }

        // Average daily spend calculation for the current month
        val avgDaily = monthSpent / dayOfMonth

        return KeshioUiState(
            transactions = transactions,
            filteredTransactions = filtered,
            userSettings = settings,
            todayReceived = todayReceived,
            todaySpent = todaySpent,
            dailyTarget = dailyTarget,
            dailyRemaining = dailyRemaining,
            dailyBudgetPercentage = dailyPercentage,
            dailyBudgetStatus = dailyStatus,
            monthReceived = monthReceived,
            monthSpent = monthSpent,
            monthlyTarget = monthlyTarget,
            monthlyRemaining = monthlyRemaining,
            monthlyBudgetPercentage = monthlyPercentage,
            daysRemainingInMonth = daysRemainingInMonth,
            recommendedDailySpending = recommendedDailySpending,
            isMonthlyBudgetExceeded = isMonthlyBudgetExceeded,
            categoryBreakdown = categoryBreakdown,
            allTimeIncome = allTimeIncome,
            allTimeExpense = allTimeExpense,
            netBalance = allTimeIncome - allTimeExpense,
            averageDailySpend = avgDaily,
            searchQuery = searchQuery,
            selectedTypeFilter = typeFilter,
            selectedCategoryFilter = categoryFilter,
            insightsTimeframe = timeframe
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: String) {
        _selectedTypeFilter.value = type
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setInsightsTimeframe(timeframe: String) {
        _insightsTimeframe.value = timeframe
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        note: String = "",
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                title = title.trim(),
                amount = amount,
                type = type.name,
                category = category,
                note = note.trim(),
                timestamp = timestamp
            )
            val newId = repository.insertTransaction(entity)
            val savedEntity = entity.copy(id = newId)
            smartBudgetManager.onTransactionRecorded(savedEntity, isAutomaticDetection = false)
        }
    }

    fun updateTransaction(
        id: Long,
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        note: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                id = id,
                title = title.trim(),
                amount = amount,
                type = type.name,
                category = category,
                note = note.trim(),
                timestamp = timestamp
            )
            repository.updateTransaction(entity)
            smartBudgetManager.onTransactionRecorded(entity, isAutomaticDetection = false)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun deleteTransactionById(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun updateDailyTarget(target: Double) {
        viewModelScope.launch {
            repository.updateDailyTarget(target)
        }
    }

    fun updateMonthlyTarget(target: Double) {
        viewModelScope.launch {
            repository.updateMonthlyTarget(target)
        }
    }

    fun updateCurrency(symbol: String) {
        viewModelScope.launch {
            repository.updateCurrency(symbol)
        }
    }

    fun updateThemeMode(theme: String) {
        viewModelScope.launch {
            repository.updateThemeMode(theme)
        }
    }

    fun restoreSampleData() {
        viewModelScope.launch {
            repository.restoreSampleData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAllTransactions()
        }
    }
}

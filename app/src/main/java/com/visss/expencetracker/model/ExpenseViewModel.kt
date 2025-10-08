package com.visss.expencetracker.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.visss.expencetracker.data.Expense
import com.visss.expencetracker.data.ExpenseType
import com.visss.expencetracker.preferences.CurrencyType
import com.visss.expencetracker.preferences.CurrencyUtils
import com.visss.expencetracker.preferences.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ExpenseViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Use StateFlow for Compose compatibility
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _selectedCurrency = mutableStateOf(CurrencyType.RUPEE)
    val selectedCurrency: State<CurrencyType> = _selectedCurrency

    private val _currentUserId = mutableStateOf("")
    val currentUserId: State<String> = _currentUserId

    // Use mutableStateOf for Compose state
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        // Load user preferences
        viewModelScope.launch {
            userPreferences.userId.collect { userId ->
                _currentUserId.value = userId
                loadUserExpenses(userId)
            }
        }

        viewModelScope.launch {
            userPreferences.selectedCurrency.collect { currency ->
                _selectedCurrency.value = currency
            }
        }
    }

    // Load expenses for specific user only
    private fun loadUserExpenses(userId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        db.collection("expenses")
            .whereEqualTo("userId", userId) // Only get expenses for this user
//            .orderBy("timestamp", Query.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false

                if (error != null) {
                    _errorMessage.value = "Error loading expenses: ${error.message}"
                    println("Firestore Error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    _expenses.value = emptyList()
                    return@addSnapshotListener
                }

                try {
                    val expensesList = snapshot.documents.mapNotNull { document ->
                        try {
                            val data = document.data ?: return@mapNotNull null
                            Expense.fromMap(data)
                        } catch (e: Exception) {
                            println("Error parsing document ${document.id}: ${e.message}")
                            null
                        }
                    }

                    _expenses.value = expensesList
                    println("Loaded ${expensesList.size} expenses for user: $userId")
                } catch (e: Exception) {
                    _errorMessage.value = "Error processing expenses: ${e.message}"
                    println("Processing Error: ${e.message}")
                }
            }
    }

    // Add expense for current user only
    suspend fun addExpense(expense: Expense): Boolean {
        return try {
            println("Starting to add expense for user: ${_currentUserId.value}")

            // Ensure the expense has the current user's ID
            val expenseWithUser = expense.copy(
                userId = _currentUserId.value, // Set the user ID
                currency = CurrencyUtils.getCurrencyCode(_selectedCurrency.value)
            )

            println("Expense with user data: $expenseWithUser")
            println("User ID: ${_currentUserId.value}")

            // Use manual map to avoid serialization issues
            val expenseData = expenseWithUser.toMap()
            println("Expense data to save: $expenseData")

            // Add to Firestore with user-specific data
            db.collection("expenses")
                .document(expenseWithUser.id)
                .set(expenseData)
                .await()

            println("Expense successfully saved to Firestore for user: ${_currentUserId.value}!")
            true

        } catch (e: Exception) {
            _errorMessage.value = "Error adding expense: ${e.message}"
            println("Error saving expense: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    // Update currency preference
    fun updateCurrency(currency: CurrencyType) {
        viewModelScope.launch {
            userPreferences.setSelectedCurrency(currency)
        }
    }

    // Get expenses by type for current user
    fun getExpensesByType(type: ExpenseType): List<Expense> {
        return _expenses.value.filter { it.type == type }
    }

    // Get total income/expense for current user
    fun getTotalAmount(type: ExpenseType): Double {
        return _expenses.value
            .filter { it.type == type }
            .sumOf { it.amount }
    }

    // Get net balance for current user
    fun getNetBalance(): Double {
        val totalIncome = getTotalAmount(ExpenseType.INCOME)
        val totalExpense = getTotalAmount(ExpenseType.EXPENSE)
        return totalIncome - totalExpense
    }

    // Get expenses by category for current user
    fun getExpensesByCategory(): Map<String, Double> {
        return _expenses.value
            .filter { it.type == ExpenseType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }

    // Get recent expenses for current user
    fun getRecentExpenses(): List<Expense> {
        return _expenses.value.take(5)
    }

    // Clear error message
    fun clearError() {
        _errorMessage.value = null
    }
}
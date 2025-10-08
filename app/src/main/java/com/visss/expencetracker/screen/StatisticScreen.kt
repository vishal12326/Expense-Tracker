package com.visss.expencetracker.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.visss.expencetracker.R
import com.visss.expencetracker.admob.AdaptiveAdMobBanner
import com.visss.expencetracker.admob.AdConfig
import com.visss.expencetracker.data.Expense
import com.visss.expencetracker.data.ExpenseType
import com.visss.expencetracker.preferences.UserPreferences
import com.visss.expencetracker.state.rememberCurrencyState
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen() {
    val db = Firebase.firestore
    val currentUser = Firebase.auth.currentUser

    // Get currency state
    val userPreferences = UserPreferences(LocalContext.current)
    val currencyState = rememberCurrencyState(userPreferences)

    // State variables
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Load expenses for current user only
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            loadExpensesForStats(db, userId, expenses, isLoading, error,
                { expenses = it }, { isLoading = it }, { error = it })
        }
    }

    // Calculate statistics
    val totalIncome = expenses.filter { it.type == ExpenseType.INCOME }.sumOf { it.amount }
    val totalExpense = expenses.filter { it.type == ExpenseType.EXPENSE }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    // Expense by category
    val expenseByCategory = expenses
        .filter { it.type == ExpenseType.EXPENSE }
        .groupBy { it.category }
        .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    // Income by category
    val incomeByCategory = expenses
        .filter { it.type == ExpenseType.INCOME }
        .groupBy { it.category }
        .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    // Monthly data
    val monthlyData = expenses
        .groupBy {
            val date = it.timestamp?.toDate() ?: Date()
            SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
        }
        .mapValues { (_, expenses) ->
            val monthlyIncome = expenses.filter { it.type == ExpenseType.INCOME }.sumOf { it.amount }
            val monthlyExpense = expenses.filter { it.type == ExpenseType.EXPENSE }.sumOf { it.amount }
            Pair(monthlyIncome, monthlyExpense)
        }
        .toList()
        .sortedByDescending { it.first }

    // Create a list that includes both data items and ads
    val itemsWithAds = remember(expenseByCategory, incomeByCategory, monthlyData) {
        val allItems = mutableListOf<StatisticsItem>()

        // Add summary items
        allItems.add(StatisticsItem.SummaryItem)
        allItems.add(StatisticsItem.BalanceItem)

        // Add expense categories with ads after every 2 items
        if (expenseByCategory.isNotEmpty()) {
            allItems.add(StatisticsItem.HeaderItem("Expenses"))
            expenseByCategory.forEachIndexed { index, category ->
                allItems.add(StatisticsItem.CategoryItem(category, false))
                // Add ad after every 2 items
                if ((index + 1) % 2 == 0 && index < expenseByCategory.size - 1) {
                    allItems.add(StatisticsItem.AdItem)
                }
            }
        }

        // Add income categories with ads after every 2 items
        if (incomeByCategory.isNotEmpty()) {
            allItems.add(StatisticsItem.HeaderItem("Incomes"))
            incomeByCategory.forEachIndexed { index, category ->
                allItems.add(StatisticsItem.CategoryItem(category, true))
                // Add ad after every 2 items
                if ((index + 1) % 2 == 0 && index < incomeByCategory.size - 1) {
                    allItems.add(StatisticsItem.AdItem)
                }
            }
        }

        // Add monthly data with ads after every 2 items
        if (monthlyData.isNotEmpty()) {
            allItems.add(StatisticsItem.HeaderItem("Monthly Breakdown"))
            monthlyData.take(6).forEachIndexed { index, monthly ->
                allItems.add(StatisticsItem.MonthlyItem(monthly))
                // Add ad after every 2 items
                if ((index + 1) % 2 == 0 && index < monthlyData.take(6).size - 1) {
                    allItems.add(StatisticsItem.AdItem)
                }
            }
        }

        allItems
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Financial Statistics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${expenses.size} transactions analyzed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Loading State
            if (isLoading && expenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading statistics...")
                        }
                    }
                }
            }

            // Use the combined list with ads
            items(itemsWithAds) { item ->
                when (item) {
                    is StatisticsItem.SummaryItem -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Total Income",
                                amount = totalIncome,
                                icon = Icons.Default.KeyboardArrowUp,
                                iconColor = Color(0xFF00C853),
                                currencyState = currencyState,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Total Expense",
                                amount = totalExpense,
                                icon = Icons.Default.KeyboardArrowDown,
                                iconColor = Color(0xFFFF3D00),
                                currencyState = currencyState,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    is StatisticsItem.BalanceItem -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (balance >= 0) Color(0xFFE8F5E8) else Color(0xFFFFEBEE)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Net Balance",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = currencyState.formatAmount(balance),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (balance >= 0) Color(0xFF00C853) else Color(0xFFFF3D00)
                                    )
                                }
                                Image(
                                    painter = painterResource(id = R.drawable.wallet),
                                    contentDescription = "wallet",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                    is StatisticsItem.HeaderItem -> {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    is StatisticsItem.CategoryItem -> {
                        CategoryProgressItem(
                            category = item.categoryData.first,
                            amount = item.categoryData.second,
                            totalAmount = if (item.isIncome) totalIncome else totalExpense,
                            isIncome = item.isIncome,
                            currencyState = currencyState
                        )
                    }
                    is StatisticsItem.MonthlyItem -> {
                        MonthlyItem(
                            month = item.monthlyData.first,
                            income = item.monthlyData.second.first,
                            expense = item.monthlyData.second.second,
                            currencyState = currencyState
                        )
                    }
                    is StatisticsItem.AdItem -> {
                        // Real AdMob Banner
                        RealAdBanner()
                    }
                }
            }

            // Empty State
            if (expenses.isEmpty() && !isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.wallet),
                                contentDescription = "wallet",
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "No data available",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Add transactions to see statistics",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    amount: Double,
    icon: ImageVector,
    iconColor: Color,
    currencyState: com.visss.expencetracker.state.CurrencyState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = currencyState.formatAmount(amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CategoryProgressItem(
    category: String,
    amount: Double,
    totalAmount: Double,
    isIncome: Boolean,
    currencyState: com.visss.expencetracker.state.CurrencyState
) {
    val percentage = if (totalAmount > 0) (amount / totalAmount * 100).toFloat() else 0f
    val progressColor = if (isIncome) Color(0xFF00C853) else Color(0xFFFF3D00)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = currencyState.formatAmount(amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isIncome) Color(0xFF00C853) else Color(0xFFFF3D00)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentage / 100)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(progressColor)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${String.format("%.1f", percentage)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun MonthlyItem(
    month: String,
    income: Double,
    expense: Double,
    currencyState: com.visss.expencetracker.state.CurrencyState
) {
    val net = income - expense

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = month,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyState.formatAmount(income),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF00C853),
                        fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyState.formatAmount(expense),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFF3D00),
                        fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text(
                        text = "Net",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyState.formatAmount(net),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (net >= 0) Color(0xFF00C853) else Color(0xFFFF3D00),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Real AdMob Banner Composable
@Composable
fun RealAdBanner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Ad Label
        Text(
            text = "Advertisement",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
        )

        // Real AdMob Banner
        AdaptiveAdMobBanner(
            adUnitId = AdConfig.getBannerAdId(),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp) // Standard banner height
        )
    }
}

// Sealed class for different types of items in the statistics screen
sealed class StatisticsItem {
    object SummaryItem : StatisticsItem()
    object BalanceItem : StatisticsItem()
    data class HeaderItem(val title: String) : StatisticsItem()
    data class CategoryItem(val categoryData: Pair<String, Double>, val isIncome: Boolean) : StatisticsItem()
    data class MonthlyItem(val monthlyData: Pair<String, Pair<Double, Double>>) : StatisticsItem()
    object AdItem : StatisticsItem()
}

// Updated Firestore loading function for statistics with user filtering
private suspend fun loadExpensesForStats(
    db: com.google.firebase.firestore.FirebaseFirestore,
    userId: String,
    currentExpenses: List<Expense>,
    currentLoading: Boolean,
    currentError: String?,
    onExpensesUpdate: (List<Expense>) -> Unit,
    onLoadingUpdate: (Boolean) -> Unit,
    onErrorUpdate: (String?) -> Unit
) {
    try {
        onLoadingUpdate(true)
        onErrorUpdate(null)

        // Query expenses only for the current user
        val querySnapshot = db.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val expensesList = querySnapshot.documents.mapNotNull { document ->
            try {
                val data = document.data ?: return@mapNotNull null
                convertToExpenseForStats(data)
            } catch (e: Exception) {
                null
            }
        }

        // Manual sorting by timestamp
        val sortedExpenses = expensesList.sortedByDescending { expense ->
            expense.timestamp?.seconds ?: 0
        }

        onExpensesUpdate(sortedExpenses)

    } catch (e: Exception) {
        onErrorUpdate("Failed to load expenses: ${e.message}")
    } finally {
        onLoadingUpdate(false)
    }
}

// Direct conversion function for statistics
private fun convertToExpenseForStats(data: Map<String, Any>): Expense? {
    return try {
        val id = data["id"] as? String ?: return null
        val title = data["title"] as? String ?: return null
        val amount = (data["amount"] as? Number)?.toDouble() ?: return null
        val category = data["category"] as? String ?: return null

        // Type conversion
        val typeString = data["type"] as? String ?: return null
        val type = when (typeString.uppercase()) {
            "INCOME" -> ExpenseType.INCOME
            "EXPENSE" -> ExpenseType.EXPENSE
            else -> ExpenseType.EXPENSE
        }

        val timestamp = data["timestamp"] as? com.google.firebase.Timestamp
        val userId = data["userId"] as? String

        userId?.let {
            timestamp?.let { it1 ->
                Expense(
                    id = id,
                    title = title,
                    amount = amount,
                    category = category,
                    type = type,
                    timestamp = it1,
                    userId = it
                )
            }
        }
    } catch (e: Exception) {
        null
    }
}
package com.visss.expencetracker.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.visss.expencetracker.R
import com.visss.expencetracker.admob.AdaptiveAdMobBanner
import com.visss.expencetracker.admob.AdConfig
import com.visss.expencetracker.data.Expense
import com.visss.expencetracker.data.ExpenseType
import com.visss.expencetracker.model.ExpenseViewModelFactory
import com.visss.expencetracker.preferences.UserPreferences
import com.visss.expencetracker.state.rememberCurrencyState
import com.visss.expencetracker.viewmodel.ExpenseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    expenseViewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory(LocalContext.current)
    )
) {
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    // Get currency state
    val userPreferences = UserPreferences(LocalContext.current)
    val currencyState = rememberCurrencyState(userPreferences)

    // State variables
    val expenses by expenseViewModel.expenses.collectAsState()
    val isLoading = expenseViewModel.isLoading.value
    val error = expenseViewModel.errorMessage.value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // State for view more functionality
    var visibleTransactionsCount by remember { mutableStateOf(15) }
    var isLoadingMore by remember { mutableStateOf(false) }

    // Calculate totals
    val totalIncome = expenses
        .filter { it.type == ExpenseType.INCOME }
        .sumOf { it.amount }

    val totalExpense = expenses
        .filter { it.type == ExpenseType.EXPENSE }
        .sumOf { it.amount }

    val balance = totalIncome - totalExpense

    // Show error messages
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            snackbarHostState.showSnackbar(errorMessage)
            expenseViewModel.clearError()
        }
    }

    // Function to load more transactions
    fun loadMoreTransactions() {
        if (visibleTransactionsCount < expenses.size && !isLoadingMore) {
            isLoadingMore = true
            scope.launch {
                // Simulate loading delay
                delay(1000)
                visibleTransactionsCount += 4
                // Ensure we don't exceed the total number of expenses
                if (visibleTransactionsCount > expenses.size) {
                    visibleTransactionsCount = expenses.size
                }
                isLoadingMore = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Expensory",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (currentUser != null) {
                        Text(
                            text = "Welcome, ${currentUser.email?.split("@")?.firstOrNull() ?: "User"}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row {
                    IconButton(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Refreshing...")
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
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
                                Text("Loading your transactions...")
                            }
                        }
                    }
                }

                // Balance Card
                item {
                    BalanceCard(
                        balance = balance,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        currencyState = currencyState,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Quick Stats Section
                item {
                    Text(
                        text = "Quick Stats",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuickStatCard(
                            title = "Income",
                            amount = totalIncome,
                            icon = Icons.Default.KeyboardArrowUp,
                            iconColor = Color(0xFF00C853),
                            currencyState = currencyState,
                            modifier = Modifier.weight(1f)
                        )
                        QuickStatCard(
                            title = "Expense",
                            amount = totalExpense,
                            icon = Icons.Default.KeyboardArrowDown,
                            iconColor = Color(0xFFFF3D00),
                            currencyState = currencyState,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Recent Transactions Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Transactions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${expenses.size} transactions",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Empty State
                if (expenses.isEmpty() && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.wallet),
                                    contentDescription = "wallet",
                                    modifier = Modifier.size(80.dp)
                                )
                                Text(
                                    "No transactions yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Add your first transaction to get started",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Transactions List with Real Ads
                itemsIndexed(
                    items = expenses.take(visibleTransactionsCount),
                    key = { index, expense -> expense.id }
                ) { index, expense ->
                    // Show real ad after every 3 transactions
                    if (index > 0 && index % 3 == 0) {
                        RealAdBanner(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    TransactionItem(
                        expense = expense,
                        currencyState = currencyState,
                        modifier = Modifier
                            .animateItemPlacement()
                            .padding(vertical = 4.dp)
                    )
                }

                // Show more indicator with loading
                if (expenses.size > visibleTransactionsCount) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Loading indicator when loading more
                            if (isLoadingMore) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    "Loading more transactions...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                // View More Button
                                Button(
                                    onClick = { loadMoreTransactions() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 0.dp,
                                        pressedElevation = 0.dp
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(44.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = "View More",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            "View ${minOf(4, expenses.size - visibleTransactionsCount)} more",
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Show "All transactions loaded" message
                if (expenses.isNotEmpty() && visibleTransactionsCount >= expenses.size) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "All transactions loaded",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Bottom spacer
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun RealAdBanner(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
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

@Composable
fun BalanceCard(
    balance: Double,
    totalIncome: Double,
    totalExpense: Double,
    currencyState: com.visss.expencetracker.state.CurrencyState,
    modifier: Modifier = Modifier
) {
    // Decide gradient based on positive/negative balance
    val isPositive = balance >= 0
    val gradientColors = if (isPositive) {
        listOf(Color(0xFF667eea), Color(0xFF764ba2))
    } else {
        listOf(Color(0xFFf5576c), Color(0xFFf093fb))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                clip = true
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.linearGradient(colors = gradientColors))
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp
                    )
                    Image(
                        painter = painterResource(id = R.drawable.wallet),
                        contentDescription = "wallet",
                        modifier = Modifier.size(26.dp),
                        colorFilter = ColorFilter.tint(Color.White)

                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currencyState.formatAmount(balance),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BalanceBreakdownItem(
                        label = "Income",
                        amount = totalIncome,
                        isPositive = true,
                        currencyState = currencyState
                    )
                    BalanceBreakdownItem(
                        label = "Expense",
                        amount = totalExpense,
                        isPositive = false,
                        currencyState = currencyState
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceBreakdownItem(
    label: String,
    amount: Double,
    isPositive: Boolean,
    currencyState: com.visss.expencetracker.state.CurrencyState
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp
        )
        Text(
            text = currencyState.formatAmount(amount),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )
    }
}

@Composable
fun QuickStatCard(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    currencyState: com.visss.expencetracker.state.CurrencyState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), clip = true),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = currencyState.formatAmount(amount),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    expense: Expense,
    currencyState: com.visss.expencetracker.state.CurrencyState,
    modifier: Modifier = Modifier
) {
    val isIncome = expense.type == ExpenseType.INCOME
    val icon = if (isIncome) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
    val iconColor = if (isIncome) Color(0xFF00C853) else Color(0xFFFF3D00)
    val backgroundColor = if (isIncome) Color(0xFFE8F5E8) else Color(0xFFFFEBEE)

    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val displayDate = try {
        expense.timestamp?.toDate()?.let { dateFormatter.format(it) } ?: "Unknown Date"
    } catch (e: Exception) {
        "Invalid Date"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), clip = true),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { /* Optional: click action */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Transaction Type",
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${expense.category} • $displayDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "${if (isIncome) "+" else "-"}${currencyState.formatAmount(expense.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isIncome) Color(0xFF00C853) else Color(0xFFFF3D00),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
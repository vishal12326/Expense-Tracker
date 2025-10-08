package com.visss.expencetracker.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.visss.expencetracker.R
import com.visss.expencetracker.data.Expense
import com.visss.expencetracker.data.ExpenseType
import com.visss.expencetracker.preferences.UserPreferences
import com.visss.expencetracker.state.rememberCurrencyState
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val currentUser = Firebase.auth.currentUser
    val userId = currentUser?.uid

    // Get currency state
    val userPreferences = UserPreferences(LocalContext.current)
    val currencyState = rememberCurrencyState(userPreferences)

    // State variables
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Load expenses for current user only
    LaunchedEffect(userId) {
        userId?.let { uid ->
            loadExpensesForProfile(db, uid, { expenses = it }, { isLoading = it })
        }
    }

    // Calculate statistics for profile
    val totalTransactions = expenses.size
    val totalIncome = expenses.filter { it.type == ExpenseType.INCOME }.sumOf { it.amount }
    val totalExpense = expenses.filter { it.type == ExpenseType.EXPENSE }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    // Most used categories
    val topExpenseCategories = expenses
        .filter { it.type == ExpenseType.EXPENSE }
        .groupBy { it.category }
        .mapValues { (_, expenses) -> expenses.size }
        .toList()
        .sortedByDescending { it.second }
        .take(3)

    val topIncomeCategories = expenses
        .filter { it.type == ExpenseType.INCOME }
        .groupBy { it.category }
        .mapValues { (_, expenses) -> expenses.size }
        .toList()
        .sortedByDescending { it.second }
        .take(3)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // Header Section with User Email
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF667eea),
                                Color(0xFF764ba2)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // User Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(40.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "User profile",
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Email
                    Text(
                        text = currentUser?.email ?: "User",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Expensory",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Financial Overview Cards - UPDATED WITH CURRENCY
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-24).dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileStatCard(
                    title = "Total Transactions",
                    value = totalTransactions.toString(),
                    iconRes = R.drawable.wallet,
                    iconColor = Color(0xFF667eea),
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    title = "Net Balance",
                    value = "${currencyState.getCurrencySymbol()}${String.format("%.2f", netBalance)}",
                    iconVector = if (netBalance >= 0) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    iconColor = if (netBalance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileStatCard(
                    title = "Total Income",
                    value = "${currencyState.getCurrencySymbol()}${String.format("%.2f", totalIncome)}",
                    iconVector = Icons.Default.KeyboardArrowUp,
                    iconColor = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    title = "Total Expense",
                    value = "${currencyState.getCurrencySymbol()}${String.format("%.2f", totalExpense)}",
                    iconVector = Icons.Default.KeyboardArrowDown,
                    iconColor = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Stats Section
        item {
            Text(
                text = "Quick Stats",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(24.dp)
            )
        }

        // Top Categories
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Expense Categories
                CategoryStatsCard(
                    title = "Top Expenses",
                    categories = topExpenseCategories,
                    iconVector = Icons.Default.KeyboardArrowDown,
                    iconColor = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )

                // Top Income Categories
                CategoryStatsCard(
                    title = "Top Income",
                    categories = topIncomeCategories,
                    iconVector = Icons.Default.KeyboardArrowUp,
                    iconColor = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Settings Section
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(24.dp)
            )
        }

        // Settings Options
        val settingsOptions = listOf(
            ProfileSettingOption(
                title = "Privacy & Security",
                subtitle = "Data privacy and security settings",
                iconRes = R.drawable.privacy,
                iconColor = Color(0xFF4CAF50),
                onClick = {
                    openUrl(context, "https://www.your-privacy-policy.com")
                }
            ),
            ProfileSettingOption(
                title = "Support & Help",
                subtitle = "Get help and contact support",
                iconRes = R.drawable.support,
                iconColor = Color(0xFFFF9800),
                onClick = {
                    sendEmail(context, "svk8190@gmail.com", "Support Request", "Hello, I need help with...")
                }
            ),
            ProfileSettingOption(
                title = "Rate Our App",
                subtitle = "Share your feedback with us",
                iconVector = Icons.Default.Star,
                iconColor = Color(0xFFFFC107),
                onClick = {
                    openUrl(context, "market://details?id=com.visss.expencetracker")
                }
            )
        )

        items(settingsOptions) { option ->
            ProfileSettingItem(option = option)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Logout Button
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF3D00).copy(alpha = 0.1f),
                        contentColor = Color(0xFFFF3D00)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.exit),
                        contentDescription = "logout",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ProfileStatCard with support for both drawable resources and Material Icons
@Composable
fun ProfileStatCard(
    title: String,
    value: String,
    iconRes: Int? = null,
    iconVector: ImageVector? = null,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                clip = true
            ),
        shape = RoundedCornerShape(16.dp),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                // Display icon based on available type
                when {
                    iconRes != null -> {
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = title,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    iconVector != null -> {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = title,
                            tint = iconColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
        }
    }
}

// CategoryStatsCard with support for both drawable resources and Material Icons
@Composable
fun CategoryStatsCard(
    title: String,
    categories: List<Pair<String, Int>>,
    iconRes: Int? = null,
    iconVector: ImageVector? = null,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                clip = true
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // Display icon based on available type
                when {
                    iconRes != null -> {
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = title,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    iconVector != null -> {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = title,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (categories.isNotEmpty()) {
                categories.forEachIndexed { index, (category, count) ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = iconColor
                        )
                    }
                }
            } else {
                Text(
                    text = "No data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingItem(option: ProfileSettingOption) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                clip = true
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = option.onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(option.iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                // Display icon based on available type
                when {
                    option.iconRes != null -> {
                        Image(
                            painter = painterResource(id = option.iconRes),
                            contentDescription = option.title,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    option.iconVector != null -> {
                        Icon(
                            imageVector = option.iconVector,
                            contentDescription = option.title,
                            tint = option.iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Text Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = option.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            // Chevron Icon
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


// Updated ProfileSettingOption to support both icon types
data class ProfileSettingOption(
    val title: String,
    val subtitle: String,
    val iconRes: Int? = null,
    val iconVector: ImageVector? = null,
    val iconColor: Color,
    val onClick: () -> Unit
) {
    init {
        require(iconRes != null || iconVector != null) {
            "Either iconRes or iconVector must be provided"
        }
    }
}

// Function to open URL in browser
private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error (no browser app)
    }
}

// Function to send email
private fun sendEmail(context: Context, email: String, subject: String, body: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(intent, "Send Email"))
    } catch (e: Exception) {
        // Handle error (no email app)
    }
}

// Updated Firestore loading function for profile with user filtering
private suspend fun loadExpensesForProfile(
    db: com.google.firebase.firestore.FirebaseFirestore,
    userId: String,
    onExpensesUpdate: (List<Expense>) -> Unit,
    onLoadingUpdate: (Boolean) -> Unit
) {
    try {
        onLoadingUpdate(true)

        // Query expenses only for the current user
        val querySnapshot = db.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val expensesList = querySnapshot.documents.mapNotNull { document ->
            try {
                val data = document.data ?: return@mapNotNull null
                convertToExpenseForProfile(data)
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
        // Silent fail for profile screen
        println("Error loading expenses: ${e.message}")
    } finally {
        onLoadingUpdate(false)
    }
}

// Direct conversion function for profile
private fun convertToExpenseForProfile(data: Map<String, Any>): Expense? {
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
package com.visss.expencetracker.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.visss.expencetracker.data.Expense
import com.visss.expencetracker.data.ExpenseType
import com.visss.expencetracker.model.ExpenseViewModelFactory
import com.visss.expencetracker.preferences.CurrencyType
import com.visss.expencetracker.preferences.CurrencyUtils
import com.visss.expencetracker.preferences.UserPreferences
import com.visss.expencetracker.state.rememberCurrencyState
import com.visss.expencetracker.viewmodel.ExpenseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {},
    expenseViewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val auth = Firebase.auth

    // Check if user is authenticated
    val isAuthenticated by remember { mutableStateOf(auth.currentUser != null) }

    // Get currency state - this will be shared across the whole app
    val userPreferences = UserPreferences(LocalContext.current)
    val currencyState = rememberCurrencyState(userPreferences)

    // If user is not authenticated, show message and return
    if (!isAuthenticated) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Not Authenticated",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Please login to add expenses",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You need to be logged in to save your expenses",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    // This will trigger navigation to AuthScreen
                    // through the auth state listener in MainActivity
                }
            ) {
                Text("Go to Login")
            }
        }
        return
    }

    // State variables
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food & Dining") }
    var expenseType by remember { mutableStateOf(ExpenseType.EXPENSE) }
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var showCurrencyMenu by remember { mutableStateOf(false) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Animation states
    val rotation by animateFloatAsState(
        targetValue = if (isLoading) 360f else 0f,
        label = "loading_rotation"
    )

    // Top 6 Currencies
    val topCurrencies = listOf(
        CurrencyType.RUPEE,    // Indian Rupee
        CurrencyType.DOLLAR,   // US Dollar
        CurrencyType.EURO,     // Euro
        CurrencyType.POUND,    // British Pound
        CurrencyType.YEN,      // Japanese Yen
        CurrencyType.AUSTRALIAN_DOLLAR // Australian Dollar
    )

    // Income Categories
    val incomeCategories = listOf(
        "Salary", "Business", "Freelance", "Investment",
        "Gift", "Bonus", "Rental", "Other Income"
    )

    // Expense Categories
    val expenseCategories = listOf(
        "Food & Dining", "Transport", "Shopping", "Entertainment",
        "Bills & Utilities", "Healthcare", "Education", "Travel", "Grocery", "Other"
    )

    // Get current categories based on expense type
    val currentCategories = when (expenseType) {
        ExpenseType.INCOME -> incomeCategories
        ExpenseType.EXPENSE -> expenseCategories
    }

    // Reset category when expense type changes
    LaunchedEffect(expenseType) {
        selectedCategory = when (expenseType) {
            ExpenseType.INCOME -> "Salary"
            ExpenseType.EXPENSE -> "Food & Dining"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Transaction",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    // Currency Selector in Top Bar
                    Box {
                        IconButton(
                            onClick = { showCurrencyMenu = true },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = CurrencyUtils.getCurrencySymbol(currencyState.currentCurrency.value),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Currency",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Currency Dropdown Menu
                        DropdownMenu(
                            expanded = showCurrencyMenu,
                            onDismissRequest = { showCurrencyMenu = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .width(200.dp)
                        ) {
                            // Header
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Select Currency",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = { }
                            )

                            Divider()

                            // Top 6 Currencies
                            topCurrencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Currency Symbol
                                            Text(
                                                text = CurrencyUtils.getCurrencySymbol(currency),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.width(30.dp)
                                            )

                                            // Currency Name and Code
                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = CurrencyUtils.getCurrencyName(currency),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = CurrencyUtils.getCurrencyCode(currency),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            // Checkmark for selected currency
                                            if (currencyState.currentCurrency.value == currency) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        scope.launch {
                                            currencyState.updateCurrency(currency)
                                            showCurrencyMenu = false
                                            snackbarHostState.showSnackbar(
                                                "Currency updated to ${CurrencyUtils.getCurrencyName(currency)}"
                                            )
                                        }
                                        showCurrencyMenu = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Currency updated to ${CurrencyUtils.getCurrencyName(currency)}"
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
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
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .verticalScroll(rememberScrollState())
        ) {
            // Main Content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .shadow(
                        elevation = 0.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = true
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Current Currency Display
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Current Currency",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyUtils.getCurrencyName(currencyState.currentCurrency.value),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = CurrencyUtils.getCurrencySymbol(currencyState.currentCurrency.value),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Transaction Type Selection
                    Column(
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = "TRANSACTION TYPE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Income Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (expenseType == ExpenseType.INCOME) {
                                            Color(0xFF4CAF50).copy(alpha = 0.15f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                        }
                                    )
                                    .clickable { expenseType = ExpenseType.INCOME }
                                    .border(
                                        width = 2.dp,
                                        color = if (expenseType == ExpenseType.INCOME) {
                                            Color(0xFF4CAF50)
                                        } else {
                                            Color.Transparent
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Income",
                                        tint = if (expenseType == ExpenseType.INCOME) {
                                            Color(0xFF4CAF50)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Income",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (expenseType == ExpenseType.INCOME) {
                                            Color(0xFF4CAF50)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }

                            // Expense Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (expenseType == ExpenseType.EXPENSE) {
                                            Color(0xFFF44336).copy(alpha = 0.15f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                        }
                                    )
                                    .clickable { expenseType = ExpenseType.EXPENSE }
                                    .border(
                                        width = 2.dp,
                                        color = if (expenseType == ExpenseType.EXPENSE) {
                                            Color(0xFFF44336)
                                        } else {
                                            Color.Transparent
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expense",
                                        tint = if (expenseType == ExpenseType.EXPENSE) {
                                            Color(0xFFF44336)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Expense",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (expenseType == ExpenseType.EXPENSE) {
                                            Color(0xFFF44336)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Form Fields
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Title Input
                        Column {
                            Text(
                                text = "TITLE",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = {
                                    Text(
                                        "Enter transaction title",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        // Amount Input
                        Column {
                            Text(
                                text = "AMOUNT",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { newValue ->
                                    if (newValue.matches(Regex("^\\d*\\.?\\d*$")) || newValue.isEmpty()) {
                                        amount = newValue
                                    }
                                },
                                placeholder = {
                                    Text(
                                        "0.00",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                leadingIcon = {
                                    Text(
                                        text = CurrencyUtils.getCurrencySymbol(currencyState.currentCurrency.value),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        // Category Selection
                        Column {
                            Text(
                                text = "CATEGORY",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedCategory,
                                    onValueChange = {},
                                    placeholder = {
                                        Text("Select category")
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    readOnly = true
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surface)
                                        .heightIn(max = 200.dp)
                                ) {
                                    currentCategories.forEach { category ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = category,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            },
                                            onClick = {
                                                selectedCategory = category
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (title.isBlank() || amount.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please fill all fields")
                                }
                                return@Button
                            }

                            val amountValue = amount.toDoubleOrNull()
                            if (amountValue == null || amountValue <= 0) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter a valid amount")
                                }
                                return@Button
                            }

                            isLoading = true

                            scope.launch {
                                try {
                                    println("Creating expense for user: ${auth.currentUser?.uid}")
                                    println("User email: ${auth.currentUser?.email}")

                                    val expense = Expense(
                                        id = UUID.randomUUID().toString(),
                                        title = title,
                                        amount = amountValue,
                                        category = selectedCategory,
                                        type = expenseType,
                                        timestamp = com.google.firebase.Timestamp.now(),
                                        userId = auth.currentUser?.uid ?: "" // Set user ID from Firebase Auth
                                    )

                                    println("Calling addExpense in ViewModel...")
                                    val success = expenseViewModel.addExpense(expense)

                                    if (success) {
                                        isSuccess = true
                                        snackbarHostState.showSnackbar("💰 Transaction saved successfully!")
                                        println("Expense saved successfully for user: ${auth.currentUser?.uid}")

                                        // Reset form
                                        title = ""
                                        amount = ""
                                        selectedCategory = when (expenseType) {
                                            ExpenseType.INCOME -> "Salary"
                                            ExpenseType.EXPENSE -> "Food & Dining"
                                        }

                                        // Navigate back after delay
                                        delay(1500)
                                        onSaveSuccess()
                                    } else {
                                        snackbarHostState.showSnackbar("❌ Failed to save transaction. Check logs.")
                                        println("Failed to save expense for user: ${auth.currentUser?.uid}")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("⚠️ Error: ${e.message}")
                                    println("Exception saving expense: ${e.message}")
                                    e.printStackTrace()
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading && !isSuccess && title.isNotBlank() && amount.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isSuccess -> Color(0xFF00C853)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(rotation),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Saving...")
                        } else if (isSuccess) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Transaction Saved!",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Save",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Save Transaction",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Quick Amount Buttons
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "QUICK AMOUNT",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Dynamic quick amounts based on selected currency
                    val quickAmounts = when (currencyState.currentCurrency.value) {
                        CurrencyType.RUPEE -> listOf("100", "500", "1000", "2000", "4000", "5000")
                        CurrencyType.DOLLAR -> listOf("10", "50", "100", "200", "400", "500")
                        CurrencyType.EURO -> listOf("10", "50", "100", "200", "400", "500")
                        CurrencyType.POUND -> listOf("10", "50", "100", "200", "400", "500")
                        CurrencyType.YEN -> listOf("1000", "5000", "10000", "20000", "40000", "50000")
                        CurrencyType.AUSTRALIAN_DOLLAR -> listOf("10", "50", "100", "200", "400", "500")
                    }

                    // Split quick amounts into two rows
                    val firstRow = quickAmounts.take(3)
                    val secondRow = quickAmounts.drop(3)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // First row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            firstRow.forEach { quickAmount ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when (expenseType) {
                                                ExpenseType.INCOME -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                                ExpenseType.EXPENSE -> Color(0xFFF44336).copy(alpha = 0.1f)
                                            }
                                        )
                                        .clickable {
                                            amount = quickAmount
                                            // Show feedback
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Amount set to ${CurrencyUtils.getCurrencySymbol(currencyState.currentCurrency.value)}$quickAmount"
                                                )
                                            }
                                        }
                                        .border(
                                            width = 1.dp,
                                            color = when (expenseType) {
                                                ExpenseType.INCOME -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                                                ExpenseType.EXPENSE -> Color(0xFFF44336).copy(alpha = 0.3f)
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${CurrencyUtils.getCurrencySymbol(currencyState.currentCurrency.value)}$quickAmount",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when (expenseType) {
                                            ExpenseType.INCOME -> Color(0xFF4CAF50)
                                            ExpenseType.EXPENSE -> Color(0xFFF44336)
                                        },
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Second row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            secondRow.forEach { quickAmount ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when (expenseType) {
                                                ExpenseType.INCOME -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                                ExpenseType.EXPENSE -> Color(0xFFF44336).copy(alpha = 0.1f)
                                            }
                                        )
                                        .clickable {
                                            amount = quickAmount
                                            // Show feedback
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Amount set to ${CurrencyUtils.getCurrencySymbol(currencyState.currentCurrency.value)}$quickAmount"
                                                )
                                            }
                                        }
                                        .border(
                                            width = 1.dp,
                                            color = when (expenseType) {
                                                ExpenseType.INCOME -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                                                ExpenseType.EXPENSE -> Color(0xFFF44336).copy(alpha = 0.3f)
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${CurrencyUtils.getCurrencySymbol(currencyState.currentCurrency.value)}$quickAmount",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when (expenseType) {
                                            ExpenseType.INCOME -> Color(0xFF4CAF50)
                                            ExpenseType.EXPENSE -> Color(0xFFF44336)
                                        },
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Bottom spacer for better scrolling
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // Success effect
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            delay(2000)
            isSuccess = false
        }
    }
}
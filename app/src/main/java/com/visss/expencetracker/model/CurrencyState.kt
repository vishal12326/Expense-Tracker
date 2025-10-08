package com.visss.expencetracker.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.visss.expencetracker.preferences.CurrencyType
import com.visss.expencetracker.preferences.CurrencyUtils
import com.visss.expencetracker.preferences.UserPreferences

@Stable
class CurrencyState(
    private val userPreferences: UserPreferences
) {
    val currentCurrency = mutableStateOf(CurrencyType.RUPEE)

    suspend fun updateCurrency(currency: CurrencyType) {
        userPreferences.setSelectedCurrency(currency)
        currentCurrency.value = currency
    }

    suspend fun initialize() {
        userPreferences.selectedCurrency.collect { currency ->
            currentCurrency.value = currency
        }
    }

    // Helper function to format amount with current currency
    fun formatAmount(amount: Double): String {
        return CurrencyUtils.formatAmount(amount, currentCurrency.value)
    }

    // Helper function to get currency symbol
    fun getCurrencySymbol(): String {
        return CurrencyUtils.getCurrencySymbol(currentCurrency.value)
    }
}

@Composable
fun rememberCurrencyState(userPreferences: UserPreferences): CurrencyState {
    val currencyState = remember { CurrencyState(userPreferences) }

    LaunchedEffect(currencyState) {
        currencyState.initialize()
    }

    return currencyState
}
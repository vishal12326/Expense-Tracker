package com.visss.expencetracker.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        private val SELECTED_CURRENCY = stringPreferencesKey("selected_currency")
        private val USER_ID = stringPreferencesKey("user_id")
    }

    // Get selected currency with proper error handling
    val selectedCurrency: Flow<CurrencyType> = context.dataStore.data
        .map { preferences ->
            try {
                val currencyString = preferences[SELECTED_CURRENCY] ?: CurrencyType.RUPEE.name
                CurrencyType.valueOf(currencyString)
            } catch (e: Exception) {
                // Default to RUPEE if invalid value
                CurrencyType.RUPEE
            }
        }

    // Get user ID - prefer Firebase user ID if available
    val userId: Flow<String> = context.dataStore.data
        .map { preferences ->
            // First try to get Firebase user ID
            val firebaseUser = Firebase.auth.currentUser
            firebaseUser?.uid ?: preferences[USER_ID] ?: generateUserId()
        }

    // Set selected currency
    suspend fun setSelectedCurrency(currency: CurrencyType) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_CURRENCY] = currency.name
        }
    }

    // Set user ID (for non-Firebase users)
    suspend fun setUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
        }
    }

    private fun generateUserId(): String {
        return "user_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

enum class CurrencyType {
    RUPEE,
    DOLLAR,
    EURO,
    POUND,
    YEN,
    AUSTRALIAN_DOLLAR
}

// Currency utility functions
object CurrencyUtils {
    fun getCurrencySymbol(currency: CurrencyType): String {
        return when (currency) {
            CurrencyType.RUPEE -> "₹"
            CurrencyType.DOLLAR -> "$"
            CurrencyType.EURO -> "€"
            CurrencyType.POUND -> "£"
            CurrencyType.YEN -> "¥"
            CurrencyType.AUSTRALIAN_DOLLAR -> "A$"
        }
    }

    fun getCurrencyCode(currency: CurrencyType): String {
        return when (currency) {
            CurrencyType.RUPEE -> "INR"
            CurrencyType.DOLLAR -> "USD"
            CurrencyType.EURO -> "EUR"
            CurrencyType.POUND -> "GBP"
            CurrencyType.YEN -> "JPY"
            CurrencyType.AUSTRALIAN_DOLLAR -> "AUD"
        }
    }

    fun getCurrencyName(currency: CurrencyType): String {
        return when (currency) {
            CurrencyType.RUPEE -> "Indian Rupee"
            CurrencyType.DOLLAR -> "US Dollar"
            CurrencyType.EURO -> "Euro"
            CurrencyType.POUND -> "British Pound"
            CurrencyType.YEN -> "Japanese Yen"
            CurrencyType.AUSTRALIAN_DOLLAR -> "Australian Dollar"
        }
    }

    fun formatAmount(amount: Double, currency: CurrencyType): String {
        val symbol = getCurrencySymbol(currency)
        return "$symbol${String.format("%.2f", amount)}"
    }

    // Get quick amounts based on currency
    fun getQuickAmounts(currency: CurrencyType): List<String> {
        return when (currency) {
            CurrencyType.RUPEE -> listOf("100", "500", "1000", "2000", "4000", "5000")
            CurrencyType.DOLLAR -> listOf("10", "50", "100", "200", "400", "500")
            CurrencyType.EURO -> listOf("10", "50", "100", "200", "400", "500")
            CurrencyType.POUND -> listOf("10", "50", "100", "200", "400", "500")
            CurrencyType.YEN -> listOf("1000", "5000", "10000", "20000", "40000", "50000")
            CurrencyType.AUSTRALIAN_DOLLAR -> listOf("10", "50", "100", "200", "400", "500")
        }
    }

    // Get all available currencies
    fun getAllCurrencies(): List<CurrencyType> {
        return CurrencyType.entries.toList()
    }

    // Get top 6 currencies for the dropdown
    fun getTopCurrencies(): List<CurrencyType> {
        return listOf(
            CurrencyType.RUPEE,
            CurrencyType.DOLLAR,
            CurrencyType.EURO,
            CurrencyType.POUND,
            CurrencyType.YEN,
            CurrencyType.AUSTRALIAN_DOLLAR
        )
    }
}
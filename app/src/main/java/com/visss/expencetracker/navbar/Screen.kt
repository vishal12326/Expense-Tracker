package com.visss.expencetracker.navbar


import com.visss.expencetracker.R

sealed class Screen(val route: String, val title: String, val icon: Int) {


    object Home : Screen("home", "Home", R.drawable.home)
    object AddExpense : Screen("add", "Add", R.drawable.plus)
    object Stats : Screen("stats", "Stats", R.drawable.progress) // bar chat
    object Profile : Screen("profile", "Profile", R.drawable.people)


}
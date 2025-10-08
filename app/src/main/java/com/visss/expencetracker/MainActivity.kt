////package com.visss.expencetracker
////
////import android.os.Bundle
////import androidx.activity.ComponentActivity
////import androidx.activity.compose.setContent
////import androidx.compose.foundation.layout.fillMaxSize
////import androidx.compose.foundation.layout.padding
////import androidx.compose.material3.MaterialTheme
////import androidx.compose.material3.Scaffold
////import androidx.compose.material3.Surface
////import androidx.compose.runtime.Composable
////import androidx.compose.ui.Modifier
////import androidx.navigation.compose.NavHost
////import androidx.navigation.compose.composable
////import androidx.navigation.compose.rememberNavController
////import com.visss.expencetracker.components.BottomNavigationBar
////import com.visss.expencetracker.screen.AddExpenseScreen
////import com.visss.expencetracker.screen.HomeScreen
////import com.visss.expencetracker.screen.ProfileScreen
////import com.visss.expencetracker.navbar.Screen
////import com.visss.expencetracker.screen.StatisticsScreen
//////import com.visss.expencetracker.screen.StatisticsScreen
////import com.visss.expencetracker.ui.theme.EXPENCETRACKERTheme
////
////class MainActivity : ComponentActivity() {
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
//////        enableEdgeToEdge()
////        setContent {
////            EXPENCETRACKERTheme {
////                Surface(
////                    modifier = Modifier.fillMaxSize(),
////                    color = MaterialTheme.colorScheme.background
////                ) {
////                    ExpenseTrackerApp()
////                }
////            }
////        }
////    }
////}
////
////@Composable
////fun ExpenseTrackerApp() {
////    val navController = rememberNavController()
////
////    Scaffold(
////        bottomBar = {
////            BottomNavigationBar(navController = navController)
////
////        }
////    ) { innerPadding ->
////        NavHost(
////            navController = navController,
////            startDestination = Screen.Home.route,
////            modifier = Modifier.padding(innerPadding)
////        ) {
////            composable(Screen.Home.route) {
////                HomeScreen()
////            }
////            composable(Screen.AddExpense.route) {
////                AddExpenseScreen()
////            }
////            composable(Screen.Stats.route) {
////                StatisticsScreen()
////            }
////            composable(Screen.Profile.route) {
////                ProfileScreen()
////            }
////        }
////    }
////}
////
////
////
////
////
////
////
////
////
////
//
//
//
//package com.visss.expencetracker
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Surface
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.navigation.NavController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.visss.expencetracker.components.BottomNavigationBar
//import com.visss.expencetracker.screen.AddExpenseScreen
//import com.visss.expencetracker.screen.HomeScreen
//import com.visss.expencetracker.screen.ProfileScreen
//import com.visss.expencetracker.screen.SplashScreen
//import com.visss.expencetracker.screen.StatisticsScreen
//import com.visss.expencetracker.navbar.Screen
//import com.visss.expencetracker.screen.AuthScreen
//import com.visss.expencetracker.ui.theme.EXPENCETRACKERTheme
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge() // hide status abr
//        setContent {
//            EXPENCETRACKERTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    AppNavigation()
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AppNavigation() {
//    var showSplash by remember { mutableStateOf(true) }
//    var isAuthenticated by remember { mutableStateOf(false) }
//
//    if (showSplash) {
//        SplashScreen {
//            showSplash = false
//        }
//    }
//
//        AuthScreen()
//    }
//    else{
//        ExpenseTrackerApp()
//
//    }
//}
//
//@Composable
//fun ExpenseTrackerApp() {
//    val navController = rememberNavController()
//
//    Scaffold(
//        bottomBar = {
//            BottomNavigationBar(navController = navController)
//        }
//    ) { innerPadding ->
//        NavHost(
//            navController = navController,
//            startDestination = Screen.Home.route,
//            modifier = Modifier.padding(innerPadding)
//        ) {
//            composable(Screen.Home.route) {
//                HomeScreen()
//            }
//            composable(Screen.AddExpense.route) {
//                AddExpenseScreen()
//            }
//            composable(Screen.Stats.route) {
//                StatisticsScreen()
//            }
//            composable(Screen.Profile.route) {
//                ProfileScreen()
//            }
//        }
//    }
//}
















package com.visss.expencetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.visss.expencetracker.components.BottomNavigationBar
import com.visss.expencetracker.screen.AddExpenseScreen
import com.visss.expencetracker.screen.AuthScreen
import com.visss.expencetracker.screen.HomeScreen
import com.visss.expencetracker.screen.ProfileScreen
import com.visss.expencetracker.screen.SplashScreen
import com.visss.expencetracker.screen.StatisticsScreen
import com.visss.expencetracker.navbar.Screen
import com.visss.expencetracker.ui.theme.EXPENCETRACKERTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EXPENCETRACKERTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var showSplash by remember { mutableStateOf(true) }
    val auth = Firebase.auth
    var isAuthenticated by remember { mutableStateOf(auth.currentUser != null) }

    // Listen for authentication state changes
    LaunchedEffect(auth) {
        auth.addAuthStateListener { firebaseAuth ->
            isAuthenticated = firebaseAuth.currentUser != null
        }
    }

    // Splash screen with delay
    LaunchedEffect(Unit) {
        delay(2000) // 2 seconds splash screen
        showSplash = false
    }

    if (showSplash) {
        SplashScreen {
            showSplash = false
        }
    } else {
        if (isAuthenticated) {
            ExpenseTrackerApp()
        } else {
            AuthScreen()
        }
    }
}

@Composable
fun ExpenseTrackerApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.AddExpense.route) {
                AddExpenseScreen(
                    onSaveSuccess = {
                        // Navigate back to home after saving
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Stats.route) {
                StatisticsScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        // This will trigger the auth state listener and show AuthScreen
                        Firebase.auth.signOut()
                    }
                )
            }
        }
    }
}
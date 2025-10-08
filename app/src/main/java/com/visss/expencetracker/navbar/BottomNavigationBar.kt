package com.visss.expencetracker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.visss.expencetracker.R
import com.visss.expencetracker.navbar.Screen

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(
            screen = Screen.Home,
            selectedIcon = R.drawable.home,
            unselectedIcon = R.drawable.home,
            label = "Home"
        ),
        BottomNavItem(
            screen = Screen.Stats,
            selectedIcon = R.drawable.progress,
            unselectedIcon = R.drawable.progress,
            label = "Stats"
        ),
        BottomNavItem(
            screen = Screen.AddExpense,
            selectedIcon = R.drawable.plus,
            unselectedIcon = R.drawable.plus,
            label = "Add"
        ),
        BottomNavItem(
            screen = Screen.Profile,
            selectedIcon = R.drawable.people,
            unselectedIcon = R.drawable.people,
            label = "Profile"
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                clip = true
            )
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.navigationBars),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        indication = null, // Remove ripple effect
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
            ) {
                // Special styling for Add button
                if (item.screen is Screen.AddExpense) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = item.selectedIcon),
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp),
                            tint = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                Color.White
                            }
                        )
                    }
                } else {
                    Icon(
                        painter = painterResource(
                            id = if (selected) item.selectedIcon else item.unselectedIcon
                        ),
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp),
                        tint = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // Label for all items except Add
                if (item.screen !is Screen.AddExpense) {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val screen: Screen,
    val selectedIcon: Int,
    val unselectedIcon: Int,
    val label: String
)
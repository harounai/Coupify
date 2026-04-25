package com.generativecity.wallet.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    data object Explore : BottomNavItem("explore", "Explore", Icons.Default.Explore)
    data object Gifts : BottomNavItem("gifts", "Gifts", Icons.Default.SportsEsports)
    data object Streak : BottomNavItem("streak", "Streak", Icons.Default.Whatshot)
    data object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}

val userBottomTabs = listOf(
    BottomNavItem.Explore,
    BottomNavItem.Gifts,
    BottomNavItem.Home,
    BottomNavItem.Streak,
    BottomNavItem.Profile
)

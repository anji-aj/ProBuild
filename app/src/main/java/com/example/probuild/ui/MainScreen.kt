package com.example.probuild.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.probuild.R
import com.example.probuild.navigation.ProBuildNavHost
import com.example.probuild.navigation.Screen

import androidx.compose.material.icons.filled.Draw

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var homeReselectedTrigger by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Projects,
                    BottomNavItem.Workspace,
                    BottomNavItem.Drawing,
                    BottomNavItem.Settings
                )
                items.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.hasRoute(item.screen::class) } == true
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                        label = { Text(stringResource(item.labelRes)) },
                        selected = selected,
                        onClick = {
                            if (selected) {
                                if (item == BottomNavItem.Home) {
                                    homeReselectedTrigger++
                                }
                            } else {
                                navController.navigate(item.screen) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        ProBuildNavHost(
            navController = navController,
            homeReselectedTrigger = homeReselectedTrigger,
            contentPadding = innerPadding
        )
    }
}

sealed class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val labelRes: Int
) {
    data object Home : BottomNavItem(Screen.Dashboard, Icons.Default.Home, R.string.nav_home)
    data object Projects : BottomNavItem(Screen.Projects, Icons.Default.PrecisionManufacturing, R.string.nav_projects)
    data object Workspace : BottomNavItem(Screen.EngineeringWorkspace, Icons.Default.Engineering, R.string.nav_workspace)
    data object Drawing : BottomNavItem(Screen.DrawingStudio(), Icons.Default.Draw, R.string.nav_drawing)
    data object Settings : BottomNavItem(Screen.Settings, Icons.Default.Settings, R.string.nav_settings)
}

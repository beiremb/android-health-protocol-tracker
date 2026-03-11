package com.example.protocoltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.protocoltracker.ui.theme.ProtocolTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProtocolTrackerTheme {
                ProtocolTrackerApp()
            }
        }
    }
}

sealed class AppScreen(
    val route: String,
    val label: String
) {
    data object Home : AppScreen("home", "Home")
    data object Log : AppScreen("log", "Log")
    data object Progress : AppScreen("progress", "Progress")
    data object Settings : AppScreen("settings", "Settings")
}

@Composable
fun ProtocolTrackerApp() {
    val navController = rememberNavController()

    val screens = listOf(
        AppScreen.Home,
        AppScreen.Log,
        AppScreen.Progress,
        AppScreen.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    val icon = when (screen) {
                        AppScreen.Home -> Icons.Filled.Home
                        AppScreen.Log -> Icons.AutoMirrored.Filled.List
                        AppScreen.Progress -> Icons.Filled.ShowChart
                        AppScreen.Settings -> Icons.Filled.Settings
                    }

                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppScreen.Home.route) {
                PlaceholderScreen(title = "Home")
            }
            composable(AppScreen.Log.route) {
                PlaceholderScreen(title = "Log")
            }
            composable(AppScreen.Progress.route) {
                PlaceholderScreen(title = "Progress")
            }
            composable(AppScreen.Settings.route) {
                PlaceholderScreen(title = "Settings")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$title screen")
    }
}
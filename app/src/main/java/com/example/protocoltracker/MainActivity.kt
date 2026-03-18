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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.protocoltracker.ui.home.HomeScreen
import com.example.protocoltracker.ui.log.LogScreen
import com.example.protocoltracker.ui.log.ReviewLogsScreen
import com.example.protocoltracker.ui.progress.ProgressScreen
import com.example.protocoltracker.ui.settings.SettingsScreen
import com.example.protocoltracker.ui.theme.ProtocolTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProtocolTrackerTheme {
                ProtocolTrackerRoot()
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
    data object ReviewLogs : AppScreen("review_logs", "Review logs")
    data object Progress : AppScreen("progress", "Progress")
    data object Settings : AppScreen("settings", "Settings")
}

@Composable
fun ProtocolTrackerRoot() {
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
                val currentRoute = navBackStackEntry?.destination?.route

                screens.forEach { screen ->
                    val icon = when (screen) {
                        AppScreen.Home -> Icons.Filled.Home
                        AppScreen.Log -> Icons.AutoMirrored.Filled.List
                        AppScreen.ReviewLogs -> Icons.AutoMirrored.Filled.List
                        AppScreen.Progress -> Icons.AutoMirrored.Filled.ShowChart
                        AppScreen.Settings -> Icons.Filled.Settings
                    }

                    val selected = when (screen) {
                        AppScreen.Log -> {
                            currentRoute == AppScreen.Log.route || currentRoute == AppScreen.ReviewLogs.route
                        }

                        else -> currentRoute == screen.route
                    }

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
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
                HomeScreen(
                    onOpenLog = {
                        navController.navigate(AppScreen.Log.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(AppScreen.Log.route) {
                LogScreen(
                    onOpenReviewLogs = {
                        navController.navigate(AppScreen.ReviewLogs.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(AppScreen.ReviewLogs.route) {
                ReviewLogsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppScreen.Progress.route) {
                ProgressScreen()
            }

            composable(AppScreen.Settings.route) {
                SettingsScreen()
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
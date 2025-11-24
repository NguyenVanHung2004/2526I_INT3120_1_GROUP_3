package com.example.aijournalingapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aijournalingapp.ui.entry.EntryScreen
import com.example.aijournalingapp.ui.home.HomeScreen
import com.example.aijournalingapp.ui.insight.InsightScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("entry") {
            EntryScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("insight/{journalId}") { backStackEntry ->
            val journalId = backStackEntry.arguments?.getString("journalId")
            if (journalId != null) {
                InsightScreen(journalId = journalId, onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
package com.example.aijournalingapp.ui.navigation

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aijournalingapp.ui.auth.AuthViewModel
import com.example.aijournalingapp.ui.auth.LoginScreen
import com.example.aijournalingapp.ui.auth.RegisterScreen
import com.example.aijournalingapp.ui.auth.WelcomeScreen
import com.example.aijournalingapp.ui.entry.EntryScreen
import com.example.aijournalingapp.ui.habit.HabitScreen
import com.example.aijournalingapp.ui.home.HomeScreen
import com.example.aijournalingapp.ui.insight.InsightScreen

@Composable
fun AppNavigation(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val user by authViewModel.user.collectAsState()

    // Sử dụng biến để xác định điểm bắt đầu DỰA TRÊN trạng thái Auth
    val startDestination = if (user != null) "home" else "welcome"

    // [MỚI] Sử dụng một loading screen đơn giản trong khi Firebase Auth đang kiểm tra token
    if (user == null && authViewModel.loading.collectAsState().value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("welcome") {
            WelcomeScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen(navController)
        }
        composable("home") {
            // Đảm bảo không thể quay lại màn hình Auth sau khi đăng nhập
            HomeScreen(navController, onLogout = { authViewModel.logout() })
        }
        composable("entry") {
            EntryScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("habit") {
            HabitScreen(navController = navController)
        }
        composable("insight/{journalId}") { backStackEntry ->
            val journalId = backStackEntry.arguments?.getString("journalId")
            if (journalId != null) {
                InsightScreen(journalId = journalId, onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
package com.example.aijournalingapp.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aijournalingapp.R
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Animation: Logo cây đung đưa/phóng to nhẹ
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

    // Kiểm tra trạng thái đăng nhập
    LaunchedEffect(Unit) {
        delay(2000) // Chờ 2 giây để user ngắm logo (hoặc load tài nguyên)
        if (Firebase.auth.currentUser != null) {
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true } // Xóa Splash khỏi lịch sử back
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F7F2)), // Màu nền Beige giống Home
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Logo
            Box(
                modifier = Modifier
                    .size(150.dp) // Tăng kích thước lên chút
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                // [THAY THẾ]: Dùng ảnh logo thay vì Text
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo App",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tên App
            Text(
                "AI Journal",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32) // Xanh đậm
            )
            Text(
                "Nuôi dưỡng tâm hồn",
                fontSize = 16.sp,
                color = Color(0xFF78909C),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Footer nhỏ bên dưới
        Text(
            "Đang tải dữ liệu...",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
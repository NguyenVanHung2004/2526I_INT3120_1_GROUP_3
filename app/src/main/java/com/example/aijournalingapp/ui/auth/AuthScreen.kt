package com.example.aijournalingapp.ui.auth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.draw.clip
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes


private const val FIREBASE_WEB_CLIENT_ID = "851826122530-o09b1qpor2ekl1ri90htlq3bpa1a8p70.apps.googleusercontent.com"
// Màu cho Google button
private val GoogleRed = Color(0xFFEA4335)
private val GoogleBlue = Color(0xFF4285F4)
private val TextDark = Color(0xFF37474F)

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("SoulLeaf", style = MaterialTheme.typography.headlineLarge, color = TextDark)
        Spacer(Modifier.height(16.dp))
        Text("Ghi lại cảm xúc của bạn", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(Modifier.height(32.dp))
        Button(onClick = {navController.navigate("login")}, modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)) { Text("Đăng nhập") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = { navController.navigate("register") }, modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)) { Text("Đăng ký") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val user by viewModel.user.collectAsState()

    // 1. Cấu hình Google Sign-in Options
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(FIREBASE_WEB_CLIENT_ID) // QUAN TRỌNG: Dùng Web Client ID
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    // 2. Launcher để nhận kết quả từ Google Sign-in Intent
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                viewModel.signInWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Hiển thị lỗi đăng nhập Google
                // viewModel.setError("Đăng nhập Google thất bại.") // Cần thêm hàm setError vào ViewModel
                // Xử lý lỗi đăng nhập Google
                Log.e("GoogleSignIn", "Đăng nhập Google thất bại, mã trạng thái: ${e.statusCode}")

                // Hiển thị thông báo lỗi đến người dùng thông qua ViewModel
                // Giả định ViewModel có hàm setError(String) hoặc tương đương
                val errorMessage = when (e.statusCode) {
                    // Thêm các mã lỗi phổ biến để cung cấp thông báo cụ thể hơn
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Đăng nhập bị hủy bởi người dùng."
                    GoogleSignInStatusCodes.NETWORK_ERROR -> "Lỗi mạng hoặc không có kết nối."
                    // Mặc định cho các lỗi khác
                    else -> "Đăng nhập Google thất bại (Mã: ${e.statusCode}). Vui lòng thử lại."
                }
            }
        }
    }

    // Khi user becomes non-null -> navigate to HOME
    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate("home") {
                popUpTo("welcome") { inclusive = true }
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Đăng nhập", style = MaterialTheme.typography.headlineLarge, color = TextDark)
        Spacer(Modifier.height(24.dp))

        // --- NÚT ĐĂNG NHẬP GOOGLE ---
        OutlinedButton(
            onClick = {
                // Khởi chạy Google Sign-in Intent
                launcher.launch(googleSignInClient.signInIntent)
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, GoogleBlue),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
        ) {
            Icon(Icons.Default.Lock, contentDescription = "Google Icon", tint = GoogleRed)
            Spacer(Modifier.width(8.dp))
            Text("Đăng nhập bằng Google")
        }

        Spacer(Modifier.height(16.dp))
        Text("hoặc", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(16.dp))

        // --- ĐĂNG NHẬP EMAIL/PASS ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextDark) }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextDark) }
        )
        Spacer(Modifier.height(16.dp))
        val err = error
        if (!err.isNullOrBlank()) {
            Text(err, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = {
                viewModel.login(email.trim(), pass)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (loading) "Đang xử lý..." else "Đăng nhập")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Quay lại")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val user by viewModel.user.collectAsState()

    // Cấu hình Google Sign-in Options (Tái sử dụng)
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(FIREBASE_WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    // Launcher để nhận kết quả từ Google Sign-in Intent
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                viewModel.signInWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Xử lý lỗi
            }
        }
    }

    // Khi user becomes non-null -> navigate to HOME
    LaunchedEffect(user) {
        if (user != null) {
            // Sau khi đăng ký/đăng nhập bằng Google thành công, chuyển thẳng đến Home
            navController.navigate("home") {
                popUpTo("welcome") { inclusive = true }
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Đăng ký", style = MaterialTheme.typography.headlineLarge, color = TextDark)
        Spacer(Modifier.height(24.dp))

        // --- NÚT ĐĂNG KÝ GOOGLE ---
//        OutlinedButton(
//            onClick = {
//                launcher.launch(googleSignInClient.signInIntent)
//            },
//            modifier = Modifier.fillMaxWidth().height(48.dp),
//            shape = RoundedCornerShape(8.dp),
//            border = BorderStroke(1.dp, GoogleBlue),
//            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
//        ) {
//            Icon(Icons.Default.Lock, contentDescription = "Google Icon", tint = GoogleRed)
//            Spacer(Modifier.width(8.dp))
//            Text("Đăng ký bằng Google")
//        }
//
//        Spacer(Modifier.height(16.dp))
//        Text("hoặc", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(16.dp))

        // --- ĐĂNG KÝ EMAIL/PASS ---
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên (tùy chọn)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextDark) }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextDark) }
        )
        Spacer(Modifier.height(16.dp))
        val err = error
        if (!err.isNullOrBlank()) {
            Text(err, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = {
                viewModel.signup(email.trim(), pass, name.ifBlank { null })
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (loading) "Đang xử lý..." else "Tạo tài khoản")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Quay lại")
        }
    }
}
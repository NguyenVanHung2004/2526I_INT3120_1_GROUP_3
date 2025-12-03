package com.example.aijournalingapp.ui.auth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aijournalingapp.ui.theme.AIJournalingAppTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

private val BgColor = Color(0xFFF9F7F2)
@Composable
fun WelcomeScreen(navController: NavController) {
    val context = LocalContext.current
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("SoulLeaf", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Text("Ghi lại cảm xúc của bạn", style = MaterialTheme.typography.bodyMedium)
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
    // Cần có WEB_CLIENT_ID của bạn từ Firebase Console -> Authentication -> Sign-in method -> Google -> Web SDK configuration
// (Thường lưu trong strings.xml hoặc hằng số)
    val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com" // ví dụ

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId) // YÊU CẦU ID TOKEN TỪ SERVER CLIENT ID
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                // Gửi ID Token đến ViewModel để đăng nhập Firebase
                viewModel.signInWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Xử lý lỗi (ví dụ: hiển thị Toast)
            }
        }
    }

    // When user becomes non-null -> navigate to HOME
    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate("home") {
                popUpTo("welcome") { inclusive = true }
            }
        }
    }

    // Optional: clear previous error when typing
    LaunchedEffect(email, pass) {
        // you could expose a method to clear error; for now do nothing
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Đăng nhập", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        val err = error
        if (!err.isNullOrBlank()) {
            Text(err, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = {
                // call viewModel.login (handles coroutine internally)
                viewModel.login(email.trim(), pass)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (loading) "Đang..." else "Đăng nhập")
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
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val user by viewModel.user.collectAsState()

    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate("login") {
                popUpTo("welcome") { inclusive = true }
            }
        }
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Đăng ký", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
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
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
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
            Text(if (loading) "Đang..." else "Tạo tài khoản")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Quay lại")
        }
    }
}

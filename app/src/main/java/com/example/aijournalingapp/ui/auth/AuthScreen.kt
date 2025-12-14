package com.example.aijournalingapp.ui.auth

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aijournalingapp.R // Đảm bảo bạn có icon google (hoặc xóa dòng này nếu dùng text)
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

// [QUAN TRỌNG]: Thay ID của bạn vào đây hoặc dùng R.string.default_web_client_id
private const val WEB_CLIENT_ID = "851826122530-o09b1qpor2ekl1ri90htlq3bpa1a8p70.apps.googleusercontent.com"

// Màu sắc chủ đạo
private val PrimaryGreen = Color(0xFF33691E)
private val LightGreen = Color(0xFFDCEDC8)
private val BeigeBg = Color(0xFFF9F7F2)

@Composable
fun AuthScreen(navController: NavController) {
    var isLoginMode by remember { mutableStateOf(true) }

    // State input
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = Firebase.auth
    val scope = rememberCoroutineScope()

    // --- CẤU HÌNH GOOGLE SIGN IN ---
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    isLoading = true
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            isLoading = false
                            if (authTask.isSuccessful) {
                                navController.navigate("home") { popUpTo("login") { inclusive = true } }
                            } else {
                                Toast.makeText(context, "Lỗi Firebase: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } catch (e: ApiException) {
                isLoading = false
                Toast.makeText(context, "Lỗi Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            isLoading = false
        }
    }

    fun startGoogleSignIn() {
        isLoading = true
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID) // Hoặc context.getString(R.string.default_web_client_id)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    // Logic đăng nhập thường (Email/Pass)
    fun handleEmailAuth() {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }
        isLoading = true

        if (isLoginMode) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                    } else {
                        Toast.makeText(context, "Lỗi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            if (name.isBlank()) {
                isLoading = false
                Toast.makeText(context, "Vui lòng nhập tên bạn", Toast.LENGTH_SHORT).show()
                return
            }
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val profileUpdates = userProfileChangeRequest { displayName = name }
                        auth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
                            isLoading = false
                            navController.navigate("home") { popUpTo("login") { inclusive = true } }
                        }
                    } else {
                        isLoading = false
                        Toast.makeText(context, "Lỗi đăng ký: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    Scaffold(containerColor = BeigeBg) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 1. Background Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Cao hơn chút để chứa logo
                    .clip(RoundedCornerShape(bottomStart = 60.dp, bottomEnd = 60.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF558B2F), Color(0xFF33691E))))
            )

            // 2. Nội dung chính
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp) // Kích thước vừa phải
                        .padding(bottom = 16.dp),
                )
                Text(
                    text = if (isLoginMode) "Chào mừng trở lại!" else "Bắt đầu hành trình",
                    fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White
                )
                Text(
                    text = if (isLoginMode) "Tiếp tục nuôi dưỡng tâm hồn bạn" else "Tạo tài khoản để gieo hạt giống đầu tiên",
                    fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                // Card Form
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (!isLoginMode) {
                            HealingTextField(name, { name = it }, "Tên của bạn", Icons.Default.Person)
                        }
                        HealingTextField(email, { email = it }, "Email", Icons.Default.Email, KeyboardType.Email)
                        HealingTextField(password, { password = it }, "Mật khẩu", Icons.Default.Lock, KeyboardType.Password, true, passwordVisible, { passwordVisible = !passwordVisible })

                        Spacer(modifier = Modifier.height(4.dp))

                        // Nút Đăng nhập/Đăng ký chính
                        Button(
                            onClick = { handleEmailAuth() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text(if (isLoginMode) "Đăng Nhập" else "Đăng Ký", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        // Phân cách "Hoặc"
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                            Text(" HOẶC ", color = Color.Gray, fontSize = 12.sp)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                        }

                        // Nút Google Sign In
                        OutlinedButton(
                            onClick = { startGoogleSignIn() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                            enabled = !isLoading
                        ) {
                            // Nếu bạn có file ảnh logo google trong res/drawable/ic_google_logo.xml
                            // Image(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = null, modifier = Modifier.size(24.dp))
                            // Spacer(modifier = Modifier.width(8.dp))
                            Text("Tiếp tục với Google", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (isLoginMode) "Chưa có tài khoản? " else "Đã có tài khoản? ", color = Color.Gray)
                    Text(
                        text = if (isLoginMode) "Đăng ký ngay" else "Đăng nhập",
                        color = PrimaryGreen, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { isLoginMode = !isLoginMode }
                    )
                }
            }
        }
    }
}

// Widget Input Field (Giữ nguyên như cũ)
@Composable
fun HealingTextField(
    value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text, isPassword: Boolean = false, passwordVisible: Boolean = false, onPasswordToggle: () -> Unit = {}
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = PrimaryGreen) },
        trailingIcon = if (isPassword) { { IconButton(onClick = onPasswordToggle) { Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Color.Gray) } } } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen, unfocusedBorderColor = Color.LightGray, focusedLabelColor = PrimaryGreen, cursorColor = PrimaryGreen),
        singleLine = true
    )
}
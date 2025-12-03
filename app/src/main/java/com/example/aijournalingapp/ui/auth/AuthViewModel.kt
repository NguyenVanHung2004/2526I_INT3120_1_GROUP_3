package com.example.aijournalingapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijournalingapp.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await // Rất quan trọng để chuyển đổi Firebase Task thành Coroutine

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    // Trạng thái người dùng ban đầu được lấy từ Firebase User
    private val _user = MutableStateFlow<User?>(auth.currentUser?.let {
        User(id = it.uid, email = it.email ?: "", name = it.displayName)
    })
    val user: StateFlow<User?> = _user

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Listener theo dõi sự thay đổi trạng thái đăng nhập thời gian thực
    init {
        // addAuthStateListener sẽ tự động cập nhật _user khi người dùng Đăng nhập/Đăng xuất
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            _user.value = firebaseUser?.let {
                User(id = it.uid, email = it.email ?: "", name = it.displayName)
            }
        }
    }

    // Đăng ký bằng Email và Mật khẩu
    fun signup(email: String, password: String, name: String?) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                // Sử dụng Firebase API để tạo tài khoản
                auth.createUserWithEmailAndPassword(email.trim(), password).await()
            } catch (e: Exception) {
                _error.value = "Lỗi đăng ký: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Đăng nhập bằng Email và Mật khẩu
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                // Sử dụng Firebase API để đăng nhập
                auth.signInWithEmailAndPassword(email.trim(), password).await()
            } catch (e: Exception) {
                _error.value = "Sai Email hoặc Mật khẩu."
            } finally {
                _loading.value = false
            }
        }
    }

    // Đăng nhập bằng Google ID Token
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                // Tạo Firebase Credential từ Google ID Token
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                // Đăng nhập Firebase với Credential (sẽ tự động đăng ký nếu chưa có)
                auth.signInWithCredential(credential).await()
            } catch (e: Exception) {
                _error.value = "Đăng nhập Google thất bại: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Đăng xuất
    fun logout() {
        auth.signOut()
        // Trạng thái sẽ được cập nhật bởi AuthStateListener trong init
    }
}
package com.example.aijournalingapp.data

import kotlinx.coroutines.delay
import com.example.aijournalingapp.model.User

class FakeAuthRepository {
    private var current: User? = null

    suspend fun signup(email: String, password: String, name: String?): Result<User> {
        delay(350)
        if (email.isBlank() || !email.contains("@")) return Result.failure(Exception("Email không hợp lệ"))
        if (password.length < 4) return Result.failure(Exception("Mật khẩu phải >= 4 ký tự"))
        val user = User(id = "u_${System.currentTimeMillis()}", email = email, name = name)
        current = user
        return Result.success(user)
    }

    suspend fun login(email: String, password: String): Result<User> {
        delay(300)
        return if (email.contains("@")) {
            val user = User(id = "u_${System.currentTimeMillis()}", email = email, name = null)
            current = user
            Result.success(user)
        } else {
            Result.failure(Exception("Sai email hoặc mật khẩu"))
        }
    }

    fun logout() { current = null }
    fun currentUser(): User? = current
}
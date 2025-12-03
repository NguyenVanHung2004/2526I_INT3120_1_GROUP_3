package com.example.aijournalingapp.ui.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijournalingapp.model.User
import com.example.aijournalingapp.data.FakeAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repo = FakeAuthRepository()
    private val _user = MutableStateFlow<User?>(repo.currentUser())
    val user: StateFlow<User?> = _user

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun signup(email: String, password: String, name: String?) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val res = repo.signup(email.trim(), password, name?.trim())
            if (res.isSuccess) {
                _user.value = res.getOrNull()
            } else {
                _error.value = res.exceptionOrNull()?.message
            }
            _loading.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val res = repo.login(email.trim(), password)
            if (res.isSuccess) {
                _user.value = res.getOrNull()
            } else {
                _error.value = res.exceptionOrNull()?.message
            }
            _loading.value = false
        }
    }

    fun logout() {
        repo.logout()
        _user.value = null
    }
}

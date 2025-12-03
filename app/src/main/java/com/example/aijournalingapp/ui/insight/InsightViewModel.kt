package com.example.aijournalingapp.ui.insight

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.aijournalingapp.model.JournalEntry
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.toObject

class InsightViewModel : ViewModel() {
    var entry by mutableStateOf<JournalEntry?>(null)
        private set
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    /**
     * Tải một mục nhật ký cụ thể từ Firestore dựa trên ID.
     */
    fun loadEntry(id: String) {
        val userId = auth.currentUser?.uid ?: return // Đảm bảo người dùng đã đăng nhập

        viewModelScope.launch {
            try {
                // Đường dẫn đến document: users/{userId}/journals/{id}
                val docRef = db.collection("users")
                    .document(userId)
                    .collection("journals")
                    .document(id)

                val snapshot = docRef.get().await()

                if (snapshot.exists()) {
                    // Chuyển đổi DocumentSnapshot sang JournalEntry
                    entry = snapshot.toObject<JournalEntry>()
                } else {
                    entry = null
                    println("Lỗi: Không tìm thấy nhật ký với ID: $id")
                }
            } catch (e: Exception) {
                entry = null
                println("Lỗi tải dữ liệu Firestore: ${e.message}")
            }
        }
    }
}
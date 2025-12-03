package com.example.aijournalingapp.model

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import com.google.firebase.firestore.DocumentId

// Thêm @DocumentId để Firestore tự động map ID của document
data class JournalEntry(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val content: String = "", // Cung cấp giá trị mặc định
    val mood: String = "",
    // Dùng timestamp để sắp xếp và tính toán dễ hơn
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = SimpleDateFormat("dd/MM", Locale("vi", "VN")).format(Date()),
    val fakeAiAdvice: String = "" // Giả lập lời khuyên AI
): Serializable
// Cập nhật User để không cần Serializable (Firestore không cần)
data class User(
    val id: String,
    val email: String,
    val name: String?
)
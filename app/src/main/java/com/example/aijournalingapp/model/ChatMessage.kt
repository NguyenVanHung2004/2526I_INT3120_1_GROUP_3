package com.example.aijournalingapp.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean, // true = User, false = AI
    val timestamp: Long = System.currentTimeMillis(),
    val isJournalCandidate: Boolean = false // Nếu true, hiển thị nút "Lưu vào nhật ký"
)
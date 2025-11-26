package com.example.aijournalingapp.model

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class JournalEntry(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val mood: String,
    val date: String = SimpleDateFormat("dd/MM", Locale("vi", "VN")).format(Date()),
    val fakeAiAdvice: String // Giả lập lời khuyên AI
): Serializable
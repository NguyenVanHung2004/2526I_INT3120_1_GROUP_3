package com.example.aijournalingapp.model

import java.util.UUID

data class JournalEntry(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val mood: String,
    val date: String = "Hôm nay",
    val fakeAiAdvice: String // Giả lập lời khuyên AI
)
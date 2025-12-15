package com.example.aijournalingapp.model

import java.io.Serializable

data class DailyTask(
    val id: String = "",
    val title: String = "",     // Ví dụ: "Uống nước", "Viết nhật ký"
    val session: String = "",   // "Sáng", "Trưa", "Chiều", "Tối"
    val points: Int = 5,        // Điểm thưởng khi hoàn thành
    val isCompleted: Boolean = false,
    val icon: String = "🌱"     // Emoji đại diện
) : Serializable
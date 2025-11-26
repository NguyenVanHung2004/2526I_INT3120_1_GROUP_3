package com.example.aijournalingapp.model

import java.io.Serializable

data class UserStats(
    val totalPoints: Int = 0,       // Tổng điểm tích lũy (để mở khóa cây)
    val currentStreak: Int = 0,     // Chuỗi ngày liên tục (như TikTok)
    val lastJournalDate: Long = 0L, // Thời điểm viết bài cuối cùng (dùng để check qua ngày)
    val dailyPoints: Int = 0        // Điểm đã kiếm được trong hôm nay (để giới hạn)
) : Serializable
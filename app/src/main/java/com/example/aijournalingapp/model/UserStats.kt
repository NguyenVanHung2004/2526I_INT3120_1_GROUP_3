package com.example.aijournalingapp.model

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class UserStats(
    // Cần phải có @PropertyName khi dùng Kotlin Data Class với Firestore
    @get:PropertyName("total_points")
    val totalPoints: Int = 0,       // Tổng điểm tích lũy (để mở khóa cây)

    @get:PropertyName("current_streak")
    val currentStreak: Int = 0,     // Chuỗi ngày liên tục

    @get:PropertyName("last_journal_date")
    val lastJournalDate: Long = 0L, // Thời điểm viết bài cuối cùng (dùng để check qua ngày)

    @get:PropertyName("daily_points")
    val dailyPoints: Int = 0        // Điểm đã kiếm được trong hôm nay (để giới hạn)
) : Serializable // Giữ lại Serializable vì file này ban đầu dùng cho file I/O
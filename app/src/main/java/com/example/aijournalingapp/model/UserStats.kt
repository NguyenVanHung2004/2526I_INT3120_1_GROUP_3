package com.example.aijournalingapp.model

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class UserStats(
    // [QUAN TRỌNG] Đổi hết 'val' thành 'var'
    // Hãy trả về mặc định = 0 (đừng để 100 nữa để test cho chuẩn)

    @PropertyName("total_points")
    var totalPoints: Int = 0,

    @PropertyName("current_streak")
    var currentStreak: Int = 0,

    @PropertyName("last_journal_date")
    var lastJournalDate: Long = 0L,

    @PropertyName("daily_points")
    var dailyPoints: Int = 0
) : Serializable
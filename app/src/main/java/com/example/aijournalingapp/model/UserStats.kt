package com.example.aijournalingapp.model

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class UserStats(
    // [FIX]: Đổi Int -> Long để khớp 100% với Firestore
    @PropertyName("total_points")
    var totalPoints: Long = 0,

    @PropertyName("current_streak")
    var currentStreak: Int = 0, // Streak thường nhỏ, Int cũng được, nhưng Long càng tốt

    @PropertyName("last_journal_date")
    var lastJournalDate: Long = 0L,

    @PropertyName("daily_points")
    var dailyPoints: Int = 0
) : Serializable
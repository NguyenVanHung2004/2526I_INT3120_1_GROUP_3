package com.example.aijournalingapp.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijournalingapp.data.FirebaseRepository // DÙNG REPOSITORY MỚI
import com.example.aijournalingapp.model.JournalEntry
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    // Flow từ Firebase Repository để nhận dữ liệu nhật ký theo thời gian thực
    var journals = mutableStateOf<List<JournalEntry>>(emptyList())
        private set

    // Flow từ Firebase Repository để nhận Stats theo thời gian thực
    var totalPoints = mutableStateOf(0)
    var currentStreak = mutableStateOf(0)

    var treeMoodScore = mutableStateOf(1.0f)

    init {
        // Khởi động lắng nghe dữ liệu khi ViewModel được tạo
        viewModelScope.launch {
            FirebaseRepository.getJournalEntriesFlow().collectLatest { data ->
                journals.value = data
                treeMoodScore.value = calculateMoodScore(data)
            }
        }

        viewModelScope.launch {
            FirebaseRepository.getUserStatsFlow().collectLatest { stats ->
                // Cập nhật UI từ stats mới nhất
                totalPoints.value = stats.totalPoints
                currentStreak.value = stats.currentStreak
            }
        }
    }

    // Hàm refreshData không còn cần Context vì không dùng SharedPreferences nữa
    fun refreshData() {
        // Logic refresh bị loại bỏ vì đã dùng Realtime Flow (onSnapshotListener)
        // Dữ liệu sẽ tự động cập nhật.
        // Tuy nhiên, ta có thể gọi lại hàm tính toán Mood Score nếu cần
    }


    private fun calculateMoodScore(list: List<JournalEntry>): Float {
        if (list.isEmpty()) return 1.0f
        var totalScore = 0.0f
        list.forEach { entry ->
            totalScore += when {
                entry.mood.contains("Vui") || entry.mood.contains("Hạnh phúc") || entry.mood.contains("Tuyệt") -> 1.0f
                entry.mood.contains("Buồn") || entry.mood.contains("Lo lắng") -> 0.0f
                else -> 0.5f
            }
        }
        return totalScore / list.size
    }
}
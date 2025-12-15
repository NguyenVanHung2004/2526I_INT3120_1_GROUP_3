package com.example.aijournalingapp.ui.home

import android.util.Log // [THÊM] Import Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijournalingapp.data.FirebaseRepository
import com.example.aijournalingapp.model.JournalEntry
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    var journals = mutableStateOf<List<JournalEntry>>(emptyList())
        private set

    // Lưu ý: totalPoints đã là Long (khớp với UserStats mới)
    var totalPoints = mutableStateOf(0L)
    var currentStreak = mutableStateOf(0)

    var treeMoodScore = mutableStateOf(1.0f)

    init {
        viewModelScope.launch {
            FirebaseRepository.getJournalEntriesFlow().collectLatest { data ->
                journals.value = data
                treeMoodScore.value = calculateMoodScore(data)
            }
        }

        viewModelScope.launch {
            FirebaseRepository.getUserStatsFlow().collectLatest { stats ->
                // [LOG DEBUG QUAN TRỌNG]
                // In ra giá trị nhận được và kiểu dữ liệu của nó để kiểm tra
                Log.d("DEBUG_UI", "ViewModel nhận được Stats - TotalPoints: ${stats.totalPoints} (Kiểu: ${stats.totalPoints::class.simpleName})")

                totalPoints.value = stats.totalPoints
                currentStreak.value = stats.currentStreak
            }
        }
    }

    fun refreshData() {
        // Không cần làm gì vì đã có realtime listener
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
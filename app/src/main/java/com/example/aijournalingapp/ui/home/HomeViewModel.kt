package com.example.aijournalingapp.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.aijournalingapp.data.FakeRepository
import com.example.aijournalingapp.model.JournalEntry

class HomeViewModel : ViewModel() {
    var journals = mutableStateOf(FakeRepository.getAll())
        private set

    var treeMoodScore = mutableStateOf(1.0f)

    // [MỚI] Thêm các biến trạng thái Game
    var totalPoints = mutableStateOf(0)
    var currentStreak = mutableStateOf(0)

    fun refreshData(context: android.content.Context) {
        FakeRepository.loadData(context)

        val data = FakeRepository.getAll()
        val stats = FakeRepository.getStats() // Lấy stats

        journals.value = data
        treeMoodScore.value = calculateMoodScore(data)

        // Cập nhật UI
        totalPoints.value = stats.totalPoints
        currentStreak.value = stats.currentStreak
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
package com.example.aijournalingapp.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.aijournalingapp.data.FakeRepository
import com.example.aijournalingapp.model.JournalEntry

class HomeViewModel : ViewModel() {
    var journals = mutableStateOf(FakeRepository.getAll())
        private set

    // Thêm 2 biến state cho cây
    var treeMoodScore = mutableStateOf(1.0f) // Mặc định là vui
    var entryCount = mutableStateOf(0)

    fun refreshData() {
        val data = FakeRepository.getAll()
        journals.value = data

        // Logic tính toán cho cây
        entryCount.value = data.size
        treeMoodScore.value = calculateMoodScore(data)
    }

    private fun calculateMoodScore(list: List<JournalEntry>): Float {
        if (list.isEmpty()) return 1.0f // Chưa viết gì thì mặc định cây xanh

        var totalScore = 0.0f
        list.forEach {
            totalScore += when (it.mood) {
                "Vui" -> 1.0f
                "Bình thường" -> 0.5f
                "Buồn", "Lo lắng" -> 0.0f
                else -> 0.5f
            }
        }
        return totalScore / list.size
    }
}
package com.example.aijournalingapp.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.aijournalingapp.data.FakeRepository
import com.example.aijournalingapp.model.JournalEntry

class HomeViewModel : ViewModel() {
    var journals = mutableStateOf(FakeRepository.getAll())
        private set

    var treeMoodScore = mutableStateOf(1.0f)
    var entryCount = mutableStateOf(0)

    fun refreshData(context: android.content.Context) {
        // Gọi hàm load từ file trước khi lấy dữ liệu
        FakeRepository.loadData(context)

        val data = FakeRepository.getAll()
        journals.value = data

        entryCount.value = data.size
        treeMoodScore.value = calculateMoodScore(data)
    }

    private fun calculateMoodScore(list: List<JournalEntry>): Float {
        if (list.isEmpty()) return 1.0f

        var totalScore = 0.0f
        list.forEach { entry ->
            // Mood bây giờ có dạng: "Emoji Tên" (VD: "😄 Vui", "🤯 Bận rộn")
            // Nên ta dùng contains để kiểm tra từ khóa thay vì so sánh bằng (==)
            totalScore += when {
                // Nhóm Tích cực (1.0 điểm)
                entry.mood.contains("Vui") ||
                        entry.mood.contains("Hạnh phúc") ||
                        entry.mood.contains("Tuyệt") ||
                        entry.mood.contains("Hào hứng") ||
                        entry.mood.contains("May mắn") -> 1.0f

                // Nhóm Tiêu cực (0.0 điểm)
                entry.mood.contains("Buồn") ||
                        entry.mood.contains("Lo lắng") ||
                        entry.mood.contains("Tệ") ||
                        entry.mood.contains("Mệt") ||
                        entry.mood.contains("Chán") -> 0.0f

                // Nhóm Bình thường / Trung tính (0.5 điểm)
                else -> 0.5f
            }
        }
        return totalScore / list.size
    }
}
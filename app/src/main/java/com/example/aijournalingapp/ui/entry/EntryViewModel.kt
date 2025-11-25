package com.example.aijournalingapp.ui.entry

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijournalingapp.data.FakeRepository
import com.example.aijournalingapp.model.JournalEntry
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class EntryViewModel : ViewModel() {
    var content by mutableStateOf("")
    var selectedMood by mutableStateOf("Bình thường")
    var selectedEmoji by mutableStateOf("😐") // [MỚI] Thêm biến Emoji riêng
    var generatedAdvice by mutableStateOf("")
    var isAnalyzing by mutableStateOf(false)

    // Key của bạn
    private val apiKey = "AIzaSyCyDYrMlL7l9E8DnDVM744v6pb-i8CqnXU"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    fun analyzeJournal() {
        if (content.isBlank()) return

        viewModelScope.launch {
            isAnalyzing = true
            try {
                // Prompt mới: Yêu cầu AI tự do sáng tạo cảm xúc
                val prompt = """
                    Phân tích nhật ký: "$content"
                    1. Xác định cảm xúc chủ đạo (Tự do chọn từ ngữ chính xác nhất, ví dụ: Hào hứng, Biết ơn, Tiếc nuối, Cô đơn...).
                    2. Chọn 1 Emoji phù hợp nhất với cảm xúc đó.
                    3. Đưa ra lời khuyên ngắn (dưới 30 từ), xưng hô "mình" - "bạn".
                    
                    Trả về đúng định dạng này (không thêm text thừa):
                    MOOD|EMOJI|ADVICE
                    
                    Ví dụ:
                    Biết ơn|🙏|Hạnh phúc đôi khi chỉ là những điều giản đơn thế này.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val text = response.text ?: ""

                if (text.contains("|")) {
                    val parts = text.split("|")
                    if (parts.size >= 3) {
                        selectedMood = parts[0].trim()  // Cảm xúc tự do (VD: Hào hứng)
                        selectedEmoji = parts[1].trim() // Emoji (VD: 🤩)
                        generatedAdvice = parts[2].trim()
                    }
                } else {
                    generatedAdvice = text
                }
            } catch (e: Exception) {
                e.printStackTrace()
                generatedAdvice = "Lỗi: ${e.message}"
            } finally {
                isAnalyzing = false
            }
        }
    }

    fun saveEntry(onSuccess: () -> Unit) {
        if (content.isNotBlank()) {
            // Lưu cả Mood và Emoji vào
            val finalMood = "$selectedEmoji $selectedMood"
            val finalAdvice = if (generatedAdvice.isNotBlank()) generatedAdvice else "Một ngày đáng nhớ!"

            val newEntry = JournalEntry(
                content = content,
                mood = finalMood, // Lưu dạng "🤩 Hào hứng"
                fakeAiAdvice = finalAdvice
            )
            FakeRepository.add(newEntry)
            onSuccess()
        }
    }
}
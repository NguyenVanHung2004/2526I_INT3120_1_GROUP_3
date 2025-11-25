package com.example.aijournalingapp.ui.entry

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.text
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijournalingapp.data.FakeRepository
import com.example.aijournalingapp.model.JournalEntry
import kotlinx.coroutines.launch
import com.google.ai.client.generativeai.GenerativeModel
class EntryViewModel : ViewModel() {
    var content by mutableStateOf("")
    var selectedMood by mutableStateOf("Bình thường") // Mặc định
    var generatedAdvice by mutableStateOf("") // Lời khuyên từ AI
    var isAnalyzing by mutableStateOf(false) // Trạng thái đang load

    // CẤU HÌNH GEMINI (Bạn cần lấy API KEY tại: https://aistudio.google.com/)
    // Vì đây là demo, bạn có thể hardcode, nhưng thực tế nên để trong local.properties
    private val apiKey = "YOUR_API_KEY_HERE"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    fun analyzeJournal() {
        if (content.isBlank()) return

        viewModelScope.launch {
            isAnalyzing = true
            try {
                // Prompt kỹ thuật (Prompt Engineering)
                val prompt = """
                    Bạn là một chuyên gia tâm lý thấu hiểu. Hãy đọc dòng nhật ký sau:
                    "$content"
                    
                    Yêu cầu:
                    1. Phân tích và chọn đúng 1 cảm xúc chủ đạo trong các từ sau: [Vui, Buồn, Lo lắng, Bình thường].
                    2. Viết một lời khuyên hoặc lời động viên ngắn gọn, ấm áp (dưới 30 từ) bằng tiếng Việt.
                    
                    Trả về kết quả theo đúng định dạng sau (không thêm text thừa):
                    MOOD|ADVICE
                    
                    Ví dụ:
                    Vui|Tuyệt vời, hãy giữ gìn năng lượng tích cực này nhé!
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val responseText = response.text ?: ""

                // Xử lý kết quả trả về (Split chuỗi theo dấu |)
                if (responseText.contains("|")) {
                    val parts = responseText.split("|")
                    if (parts.size >= 2) {
                        val aiMood = parts[0].trim()
                        val aiAdvice = parts[1].trim()

                        // Update UI
                        // Kiểm tra xem mood AI chọn có nằm trong danh sách app hỗ trợ không
                        if (aiMood in listOf("Vui", "Buồn", "Lo lắng", "Bình thường")) {
                            selectedMood = aiMood
                        }
                        generatedAdvice = aiAdvice
                    }
                }
            } catch (e: Exception) {
                generatedAdvice = "AI đang bận, chưa thể phân tích lúc này."
                e.printStackTrace()
            } finally {
                isAnalyzing = false
            }
        }
    }

    fun saveEntry(onSuccess: () -> Unit) {
        if (content.isNotBlank()) {
            // Nếu chưa có lời khuyên AI (do người dùng không bấm phân tích), dùng logic cũ
            val finalAdvice = if (generatedAdvice.isNotBlank()) generatedAdvice else
                (if (selectedMood == "Buồn") "Cố lên nhé, mọi chuyện sẽ ổn thôi!" else "Ngày mai sẽ lại là một ngày tuyệt vời!")

            val newEntry = JournalEntry(
                content = content,
                mood = selectedMood,
                fakeAiAdvice = finalAdvice
            )
            FakeRepository.add(newEntry)
            onSuccess()
        }
    }
}

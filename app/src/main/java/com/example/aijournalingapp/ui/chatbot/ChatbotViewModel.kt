package com.example.aijournalingapp.ui.chatbot

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijournalingapp.BuildConfig
import com.example.aijournalingapp.data.FirebaseRepository
import com.example.aijournalingapp.model.ChatMessage
import com.example.aijournalingapp.model.DailyTask
import com.example.aijournalingapp.model.JournalEntry
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class ChatbotViewModel : ViewModel() {
    // [QUAN TRỌNG]: Thay API Key của bạn vào đây
    private val apiKey = BuildConfig.GEMINI_API_KEY

    // Khởi tạo Gemini Model (Flash cho nhanh và rẻ)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    // Khởi tạo phiên chat với ngữ cảnh ban đầu
    private val chatSession = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text("Bạn là một người bạn tâm giao thân thiện, ân cần (AI Companion). Hãy nói chuyện ngắn gọn, tình cảm bằng tiếng Việt. Luôn xưng là 'mình' và gọi người dùng là 'bạn'.") },
            content(role = "model") { text("Chào bạn, mình rất vui được làm bạn với bạn. Mình sẽ luôn lắng nghe và chia sẻ cùng bạn.") }
        )
    )

    // Các biến trạng thái UI
    var messages = mutableStateListOf<ChatMessage>()
        private set

    var currentGreeting = mutableStateOf("")
    var suggestedTasks = mutableStateListOf<DailyTask>()

    // Trạng thái Loading
    var isAiTyping = mutableStateOf(false)
    var isSummarizing = mutableStateOf(false)

    init {
        initializeSession()
    }

    private fun initializeSession() {
        viewModelScope.launch {
            // 1. Xác định buổi trong ngày
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val session = when (hour) {
                in 5..10 -> "Sáng"
                in 11..13 -> "Trưa"
                in 14..17 -> "Chiều"
                else -> "Tối"
            }

            // 2. Tạo lời chào
            val greetingText = when (session) {
                "Sáng" -> "Chào buổi sáng! Ngày mới của bạn thế nào? ☀️"
                "Trưa" -> "Trưa rồi, bạn đã nghỉ ngơi chút nào chưa? 🍱"
                "Chiều" -> "Chào buổi chiều! Cố gắng thêm chút nữa nhé 💪"
                else -> "Chào buổi tối! Một ngày dài đã qua rồi nhỉ 🌙"
            }
            currentGreeting.value = greetingText

            // Thêm lời chào vào lịch sử chat
            messages.add(ChatMessage(content = greetingText, isUser = false))

            // 3. Lấy nhiệm vụ chưa hoàn thành trong buổi hiện tại
            FirebaseRepository.getDailyTasksFlow().collectLatest { tasks ->
                val sessionTasks = tasks.filter {
                    it.session.equals(session, ignoreCase = true) && !it.isCompleted
                }
                suggestedTasks.clear()
                suggestedTasks.addAll(sessionTasks)

                // Nếu có task, chatbot nhắc nhẹ
                if (sessionTasks.isNotEmpty()) {
                    val taskReminder = "Mình thấy bạn còn ${sessionTasks.size} việc cho buổi $session nè. Bạn có muốn mình giúp gì không?"
                    messages.add(ChatMessage(content = taskReminder, isUser = false))
                }
            }
        }
    }

    // Gửi tin nhắn và nhận phản hồi từ Gemini
    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // 1. Hiển thị tin nhắn của User
        messages.add(ChatMessage(content = userText, isUser = true))
        isAiTyping.value = true

        viewModelScope.launch {
            try {
                // 2. Gửi đến Gemini
                val response = chatSession.sendMessage(userText)
                val aiReply = response.text ?: "Xin lỗi, mình đang mất kết nối..."

                messages.add(ChatMessage(content = aiReply, isUser = false))
            } catch (e: Exception) {
                messages.add(ChatMessage(content = "Lỗi kết nối AI: ${e.message}", isUser = false))
            } finally {
                isAiTyping.value = false
            }
        }
    }

    // Kết thúc phiên chat: Tóm tắt nhật ký + Lấy lời khuyên
    fun endSessionAndSaveJournal(onComplete: () -> Unit) {
        viewModelScope.launch {
            isSummarizing.value = true
            try {
                // 1. Gom toàn bộ nội dung chat thành văn bản
                val historyText = messages.joinToString("\n") {
                    (if (it.isUser) "User: " else "Bot: ") + it.content
                }

                // 2. Tạo Prompt yêu cầu Gemini làm 2 việc
                val prompt = """
                    Dựa trên cuộc trò chuyện dưới đây:
                    $historyText
                    
                    Hãy thực hiện 2 nhiệm vụ:
                    1. Viết một đoạn nhật ký ngắn (khoảng 3-5 câu), xưng "Tôi", tóm tắt lại cảm xúc và sự kiện chính trong ngày mà người dùng đã chia sẻ.
                    2. Đưa ra một lời khuyên chân thành, ngắn gọn và hữu ích dành cho người dùng dựa trên tâm trạng của họ.
                    
                    Định dạng trả về BẮT BUỘC như sau (ngăn cách bởi dấu ba gạch đứng |||):
                    [Nội dung nhật ký] ||| [Lời khuyên]
                """.trimIndent()

                // 3. Gọi AI
                val response = generativeModel.generateContent(prompt)
                val fullText = response.text ?: ""

                // 4. Tách kết quả
                val parts = fullText.split("|||")
                val journalContent = parts.getOrElse(0) { "Hôm nay tôi đã có một cuộc trò chuyện thú vị." }.trim()
                val adviceContent = parts.getOrElse(1) { "Hãy luôn yêu thương bản thân nhé!" }.trim()

                // 5. Lưu vào Firebase
                val entry = JournalEntry(
                    content = journalContent,
                    mood = "AI Phân tích", // Hoặc có thể yêu cầu AI đoán mood trong prompt luôn
                    fakeAiAdvice = adviceContent, // Lưu lời khuyên thật vào đây
                    timestamp = System.currentTimeMillis()
                )

                FirebaseRepository.addJournalEntry(entry)

                // 6. Hoàn tất
                onComplete()

            } catch (e: Exception) {
                Log.e("ChatbotVM", "Lỗi khi tổng hợp nhật ký", e)
                // Có thể thêm thông báo lỗi UI ở đây nếu cần
            } finally {
                isSummarizing.value = false
            }
        }
    }
}
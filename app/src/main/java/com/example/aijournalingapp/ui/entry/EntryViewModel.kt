package com.example.aijournalingapp.ui.entry

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijournalingapp.MyNotificationListenerService
import com.example.aijournalingapp.data.FakeRepository
import com.example.aijournalingapp.model.JournalEntry
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import java.util.Calendar

class EntryViewModel : ViewModel() {
    var content by mutableStateOf("")
    var selectedMood by mutableStateOf("Bình thường")
    var selectedEmoji by mutableStateOf("😐")
    var generatedAdvice by mutableStateOf("")
    var isAnalyzing by mutableStateOf(false)
    var isAiMode by mutableStateOf(false)

    // 🔑 Key của bạn
    private val apiKey = "AIzaSyCyDYrMlL7l9E8DnDVM744v6pb-i8CqnXU"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    // 1. Phân tích cảm xúc (Giữ nguyên)
    fun analyzeJournal() {
        if (content.isBlank()) return
        viewModelScope.launch {
            isAnalyzing = true
            try {
                val prompt = """
                    Phân tích nhật ký: "$content"
                    1. Chọn 1 cảm xúc chủ đạo.
                    2. Chọn 1 Emoji.
                    3. Lời khuyên ngắn (dưới 30 từ), xưng hô "mình" - "bạn".
                    Format: MOOD|EMOJI|ADVICE
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val text = response.text ?: ""
                if (text.contains("|")) {
                    val parts = text.split("|")
                    if (parts.size >= 3) {
                        selectedMood = parts[0].trim()
                        selectedEmoji = parts[1].trim()
                        generatedAdvice = parts[2].trim()
                    }
                } else {
                    generatedAdvice = text
                }
            } catch (e: Exception) {
                generatedAdvice = "Lỗi AI: ${e.message}"
            } finally {
                isAnalyzing = false
            }
        }
    }

    // 2. AI Viết hộ (Smart Scan) - Logic Nâng Cấp
    fun generateSmartDiary(context: Context) {
        val notiSummary = MyNotificationListenerService.getNotificationHistory(context)
        val appUsage = getTopUsedApps(context)
        Log.e("Noti",notiSummary);
        // Nếu không có gì đặc biệt
        if (notiSummary.isBlank() && appUsage.isBlank()) {
            content = "Một ngày trôi qua thật nhẹ nhàng, điện thoại im ắng, mình cũng có thời gian cho riêng bản thân."
            analyzeJournal()
            return
        }

        viewModelScope.launch {
            isAnalyzing = true
            content = "Đang phân tích thói quen hôm nay của bạn..."
            try {
                // Prompt thông minh theo logic bạn yêu cầu
                val prompt = """
                    Dựa trên dữ liệu điện thoại hôm nay:
                    
                    1. [Ứng dụng dùng nhiều (>1 tiếng)]: $appUsage
                    2. [Danh sách thông báo]: 
                    $notiSummary
                    
                    Hãy đóng vai tôi viết nhật ký (3-4 câu) theo quy tắc sau:
                    
                    - Ưu tiên 1: Nếu có nhiều thông báo quan trọng -> Viết dựa trên các sự kiện đó.
                    
                    - Ưu tiên 2 (Nếu ít thông báo): Nhìn vào ứng dụng dùng nhiều để phán đoán:
                      + Nếu là App công việc (Zalo, Slack, Viber, Teams, Gmail...) -> Than thở nhẹ về một ngày bận rộn, cày cuốc vất vả.
                      + Nếu là App giải trí (TikTok, YouTube, Facebook, Game...) -> Thú nhận hôm nay hơi lười, chỉ nằm lướt mạng giải trí.
                      
                    - Giọng văn: Tự nhiên, đời thường, như đang tự sự.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                content = response.text ?: ""
                analyzeJournal()
            } catch (e: Exception) {
                content = "Lỗi: ${e.message}"
            } finally {
                isAnalyzing = false
            }
        }
    }

    // Hàm lọc App dùng nhiều (> 60 phút)
    // Hàm lọc App dùng nhiều (> 30 phút) & Dịch tên App cho chuẩn
    private fun getTopUsedApps(context: Context): String {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1) // Lấy dữ liệu 24h qua
        val startTime = calendar.timeInMillis

        val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        // TỪ ĐIỂN DỊCH TÊN APP (Map từ tên gói sang tên thường gọi)
        val appNameMap = mapOf(
            "com.ss.android.ugc.trill" to "TikTok",
            "com.zhiliaoapp.musically" to "TikTok",
            "com.facebook.katana" to "Facebook",
            "com.facebook.orca" to "Messenger",
            "com.google.android.youtube" to "YouTube",
            "com.zing.zalo" to "Zalo",
            "com.instagram.android" to "Instagram",
            "com.netflix.mediaclient" to "Netflix",
            "com.spotify.music" to "Spotify",
            "com.google.android.gm" to "Gmail",
            "com.microsoft.teams" to "Teams",
            "org.telegram.messenger" to "Telegram",
            "com.shopee.vn" to "Shopee"
        )

        return usageStatsList
            ?.filter {
                // Lọc bỏ chính app này và các app hệ thống
                it.packageName != context.packageName &&
                        it.totalTimeInForeground > 30 * 60 * 1000 && // Dùng trên 30 phút
                        !it.packageName.contains("android") &&
                        !it.packageName.contains("google.quicksearchbox") &&
                        !it.packageName.contains("launcher") // Bỏ qua màn hình chính
            }
            ?.sortedByDescending { it.totalTimeInForeground }
            ?.take(5) // Lấy top 5 app
            ?.joinToString(", ") {
                val rawName = it.packageName
                // Nếu có trong từ điển thì lấy tên đẹp, không thì mới lấy tên đuôi
                val name = appNameMap[rawName] ?: rawName.substringAfterLast('.')

                val totalMinutes = it.totalTimeInForeground / 60000
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                val timeString = if (hours > 0) "${hours}h${minutes}p" else "${minutes}p"

                "$name ($timeString)"
            } ?: ""
    }

    fun saveEntry(onSuccess: () -> Unit) {
        if (content.isNotBlank()) {
            val finalMood = "$selectedEmoji $selectedMood"
            val finalAdvice = if (generatedAdvice.isNotBlank()) generatedAdvice else "Một ngày đáng nhớ!"
            FakeRepository.add(JournalEntry(content = content, mood = finalMood, fakeAiAdvice = finalAdvice))
            onSuccess()
        }
    }
}
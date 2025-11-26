package com.example.aijournalingapp

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MyNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val PREF_NAME = "ai_journal_prefs"
        private const val KEY_HISTORY = "noti_history_log"

        // Hàm này được gọi từ ViewModel: Lấy lịch sử đã lưu trong máy
        fun getNotificationHistory(context: Context): String {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val rawData = prefs.getString(KEY_HISTORY, "") ?: ""

            if (rawData.isBlank()) return ""

            // Xử lý lọc dữ liệu cũ (> 24h)
            val oneDayAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000
            val validLogs = mutableListOf<String>()
            val newHistoryBuilder = StringBuilder()

            // Cấu trúc lưu: ThờiGian|||NộiDung###
            rawData.split("###").forEach { entry ->
                if (entry.isNotBlank()) {
                    val parts = entry.split("|||")
                    if (parts.size >= 2) {
                        val timestamp = parts[0].toLongOrNull() ?: 0L
                        // Chỉ lấy tin trong 24h qua
                        if (timestamp > oneDayAgo) {
                            validLogs.add(parts[1])
                            // Giữ lại để lưu bản mới gọn hơn
                            newHistoryBuilder.append(entry).append("###")
                        }
                    }
                }
            }

            // Lưu ngược lại danh sách đã lọc (để xóa bớt rác cũ)
            prefs.edit().putString(KEY_HISTORY, newHistoryBuilder.toString()).apply()

            // Trả về danh sách (đảo ngược để tin mới nhất lên đầu)
            return validLogs.reversed().joinToString("\n")
        }
    }

    // Khi có thông báo mới -> Ghi ngay vào bộ nhớ
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            val packageName = it.packageName
            val extras = it.notification.extras
            val title = extras.getString("android.title") ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""

            // Lọc rác: Bỏ qua thông báo hệ thống hoặc không có nội dung
            if (title.isNotBlank() && !packageName.contains("android.systemui") && !packageName.contains("com.android.system")) {

                // Format tên app cho đẹp (Demo vài cái chính)
                val appName = when {
                    packageName.contains("facebook.orca") -> "Messenger"
                    packageName.contains("facebook.katana") -> "Facebook"
                    packageName.contains("zalo") -> "Zalo"
                    packageName.contains("shopee") -> "Shopee"
                    packageName.contains("instagram") -> "Instagram"
                    packageName.contains("tiktok") -> "TikTok"
                    else -> packageName.substringAfterLast('.')
                }

                val logContent = "[$appName]: $title - $text"
                val timestamp = System.currentTimeMillis()

                // Chuỗi lưu: Time|||Content###
                val newEntry = "$timestamp|||$logContent###"

                // Ghi nối vào SharedPreferences
                val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                val currentHistory = prefs.getString(KEY_HISTORY, "")
                prefs.edit().putString(KEY_HISTORY, currentHistory + newEntry).apply()
            }
        }
    }
}
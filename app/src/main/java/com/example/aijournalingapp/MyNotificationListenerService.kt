package com.example.aijournalingapp

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MyNotificationListenerService : NotificationListenerService() {

    companion object {
        // Lưu tạm danh sách thông báo để ViewModel lấy
        // Cấu trúc: "Tên App: Nội dung thông báo"
        fun getActiveNotificationsSummary(): String {
            // Hàm này sẽ được gọi từ UI/ViewModel, nhưng vì Service chạy ngầm
            // nên ta cần một cách hack nhẹ là truy cập trực tiếp instance nếu có thể
            // Tuy nhiên, cách chuẩn nhất là dùng getActiveNotifications() từ ngữ cảnh Service.
            // Ở đây ta dùng biến static để lưu cache đơn giản.
            return recentNotifications.joinToString("\n")
        }

        private val recentNotifications = mutableListOf<String>()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            val packageName = it.packageName
            val extras = it.notification.extras
            val title = extras.getString("android.title")
            val text = extras.getCharSequence("android.text")?.toString()

            // Lọc bớt các thông báo rác (ví dụ: hệ thống android)
            if (!packageName.contains("android") && !title.isNullOrBlank()) {
                val info = "App: $packageName | Tiêu đề: $title | Nội dung: $text"
                // Lưu 20 thông báo gần nhất thôi
                if (recentNotifications.size > 20) recentNotifications.removeAt(0)
                recentNotifications.add(info)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Xử lý khi thông báo bị xóa nếu cần
    }
}
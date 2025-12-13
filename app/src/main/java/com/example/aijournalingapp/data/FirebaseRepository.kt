package com.example.aijournalingapp.data

import android.util.Log
import com.example.aijournalingapp.model.DailyTask
import com.example.aijournalingapp.model.JournalEntry
import com.example.aijournalingapp.model.UserStats
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Repository xử lý tương tác với Firebase Firestore.
 */
object FirebaseRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // CẤU HÌNH GAME (Giữ nguyên từ FakeRepository)
    private const val POINTS_PER_ENTRY = 10
    private const val MAX_POINTS_PER_DAY = 30

    // Lưu ý: User ID là chìa khóa để phân biệt dữ liệu giữa các người dùng
    private fun getUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User is not authenticated!")
    }

    // 1. LẤY DỮ LIỆU NHẬT KÝ THEO THỜI GIAN THỰC (REALTIME)
    /**
     * Lắng nghe và trả về danh sách nhật ký theo thời gian thực
     */
    fun getJournalEntriesFlow(): Flow<List<JournalEntry>> {
        val flow = MutableStateFlow<List<JournalEntry>>(emptyList())
        val userId = getUserId()

        // Path: users/{userId}/journals
        db.collection("users").document(userId).collection("journals")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Listen failed: $e")
                    return@addSnapshotListener
                }

                val entries = snapshot?.documents?.mapNotNull { it.toObject<JournalEntry>() } ?: emptyList()
                flow.value = entries
            }
        return flow
    }

    // 2. LẤY DỮ LIỆU THỐNG KÊ (STATS) THEO THỜI GIAN THỰC
    /**
     * Lắng nghe và trả về UserStats theo thời gian thực
     */
    fun getUserStatsFlow(): Flow<UserStats> {
        val flow = MutableStateFlow(UserStats())
        val userId = getUserId()
        val docRef = db.collection("users").document(userId).collection("stats").document("user_stats")

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                println("Listen failed: $e")
                return@addSnapshotListener
            }
            // Nếu có dữ liệu, chuyển đổi sang UserStats, nếu không, dùng giá trị mặc định
            val stats = snapshot?.toObject<UserStats>() ?: UserStats()
            flow.value = stats
        }
        return flow
    }

    // 3. THÊM MỘT ENTRY VÀ CẬP NHẬT STATS
    /**
     * Thêm một mục nhật ký mới và tính toán lại điểm/streak.
     */
    suspend fun addJournalEntry(entry: JournalEntry) {
        val userId = getUserId()
        val statsDocRef = db.collection("users").document(userId).collection("stats").document("user_stats")

        // 1. Tải UserStats hiện tại
        val currentStats = statsDocRef.get().await().toObject<UserStats>() ?: UserStats()

        // 2. Tính toán Stats mới
        val updatedStats = calculateUpdatedStats(currentStats)

        // Bắt đầu batch để đảm bảo 2 thao tác (Add Entry và Update Stats) đều thành công
        db.runBatch { batch ->
            // A. Thêm Journal Entry
            val journalCollectionRef = db.collection("users").document(userId).collection("journals")
            batch.set(journalCollectionRef.document(), entry) // Firestore tự tạo ID

            // B. Cập nhật User Stats
            batch.set(statsDocRef, updatedStats)
        }.await()
    }

    // LOGIC TÍNH ĐIỂM & STREAK (Giữ nguyên logic của bạn)
    private fun calculateUpdatedStats(lastStats: UserStats): UserStats {
        val now = System.currentTimeMillis()
        val lastDate = lastStats.lastJournalDate

        val isSameDay = isSameDay(now, lastDate)
        val isNextDay = isNextDay(now, lastDate)

        var newStreak = lastStats.currentStreak
        var newDailyPoints = lastStats.dailyPoints
        var newTotalPoints = lastStats.totalPoints

        if (isSameDay) {
            if (newDailyPoints < MAX_POINTS_PER_DAY) {
                newDailyPoints += POINTS_PER_ENTRY
                newTotalPoints += POINTS_PER_ENTRY
            }
        } else if (isNextDay) {
            newDailyPoints = POINTS_PER_ENTRY
            newTotalPoints += POINTS_PER_ENTRY
            newStreak += 1
        } else {
            // Mất chuỗi
            newDailyPoints = POINTS_PER_ENTRY
            newTotalPoints += POINTS_PER_ENTRY
            newStreak = 1
        }

        return lastStats.copy(
            totalPoints = newTotalPoints,
            currentStreak = newStreak,
            lastJournalDate = now,
            dailyPoints = newDailyPoints
        )
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) &&
                c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
    }

    private fun isNextDay(current: Long, last: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = current }
        val c2 = Calendar.getInstance().apply { timeInMillis = last }
        c2.add(Calendar.DAY_OF_YEAR, 1)
        return c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) &&
                c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
    }

    private val defaultTasks = listOf(
        DailyTask("t1", "Uống 1 cốc nước", "Sáng", 5, false, "💧"),
        DailyTask("t2", "Ăn sáng đầy đủ", "Sáng", 10, false, "🍳"),
        DailyTask("t3", "Vận động nhẹ 5p", "Trưa", 10, false, "🧘"),
        DailyTask("t4", "Ngủ trưa 15p", "Trưa", 5, false, "😴"),
        DailyTask("t5", "Đi bộ / Chạy bộ", "Chiều", 15, false, "🏃"),
        DailyTask("t6", "Viết nhật ký", "Tối", 20, false, "✍️"), // Cái này có thể auto-check nếu user viết entry
        DailyTask("t7", "Đọc sách", "Tối", 10, false, "📖")
    )

    /**
     * Lấy danh sách nhiệm vụ của ngày hôm nay.
     * Nếu chưa có (ngày mới), sẽ tự động tạo từ danh sách mẫu.
     */
    fun getDailyTasksFlow(): Flow<List<DailyTask>> {
        val flow = MutableStateFlow<List<DailyTask>>(emptyList())
        val userId = auth.currentUser?.uid ?: return flow

        val todayId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val tasksRef = db.collection("users").document(userId)
            .collection("daily_tracking").document(todayId)

        tasksRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val rawList = snapshot.get("tasks") as? List<Map<String, Any>>
                val tasks = rawList?.map { map ->
                    DailyTask(
                        id = map["id"] as? String ?: "",
                        title = map["title"] as? String ?: "Nhiệm vụ",
                        session = map["session"] as? String ?: "",

                        // [FIX QUAN TRỌNG]: Dùng 'as? Number' để nhận cả Int và Long
                        points = (map["points"] as? Number)?.toInt() ?: 0,

                        isCompleted = map["isCompleted"] as? Boolean ?: false,
                        icon = map["icon"] as? String ?: "📝"
                    )
                } ?: emptyList()
                flow.value = tasks
            } else {
                tasksRef.set(mapOf("tasks" to defaultTasks))
            }
        }
        return flow
    }

    // --- SỬA HÀM TOGGLE TASK ĐỂ LOG RA XEM NÓ CỘNG HAY KHÔNG ---
    suspend fun toggleTaskCompletion(task: DailyTask) {
        val userId = getUserId()
        val todayId = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        val taskDocRef = db.collection("users").document(userId)
            .collection("daily_tracking").document(todayId)
        val statsDocRef = db.collection("users").document(userId)
            .collection("stats").document("user_stats")

        android.util.Log.d("DEBUG_TASK", "Bắt đầu toggle task: ${task.title}, Điểm task này: ${task.points}")

        db.runTransaction { transaction ->
            // --- XỬ LÝ TASK (Giữ nguyên) ---
            val snapshot = transaction.get(taskDocRef)
            val rawList = snapshot.get("tasks") as? List<Map<String, Any>> ?: return@runTransaction
            var dbIsCompleted = false
            val updatedList = rawList.map { map ->
                if (map["id"] == task.id) {
                    dbIsCompleted = map["isCompleted"] as? Boolean ?: false
                    map.toMutableMap().apply { this["isCompleted"] = !dbIsCompleted }
                } else map
            }
            val pointChange = if (!dbIsCompleted) task.points else -task.points

            // --- XỬ LÝ STATS (SỬA LỖI TẠI ĐÂY) ---
            val statsSnapshot = transaction.get(statsDocRef)
            var currentPoints: Long = 0

            // [FIX 1]: Đọc đúng trường camelCase "totalPoints" thay vì "total_points"
            if (statsSnapshot.exists()) {
                currentPoints = statsSnapshot.getLong("totalPoints") ?: 0
            }

            var newTotalPoints = currentPoints + pointChange
            if (newTotalPoints < 0) newTotalPoints = 0

            android.util.Log.d("DEBUG_TASK", "Trạng thái cũ: $dbIsCompleted | Điểm cũ: $currentPoints -> Mới: $newTotalPoints")

            // Cập nhật Task
            transaction.update(taskDocRef, "tasks", updatedList)

            if (statsSnapshot.exists()) {
                // [FIX 2]: Update vào trường "totalPoints" (camelCase)
                transaction.update(statsDocRef, "totalPoints", newTotalPoints)
            } else {
                // [FIX 3]: Khởi tạo cũng phải dùng camelCase
                val initialStats = mapOf(
                    "totalPoints" to newTotalPoints,
                    "currentStreak" to 0,
                    "dailyPoints" to (if (pointChange > 0) pointChange else 0),
                    "lastJournalDate" to 0L
                )
                transaction.set(statsDocRef, initialStats)
            }
        }.await()
        android.util.Log.d("DEBUG_TASK", "Transaction thành công!")
    }
}
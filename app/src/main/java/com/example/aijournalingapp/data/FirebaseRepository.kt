package com.example.aijournalingapp.data

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
import java.util.Calendar

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
}
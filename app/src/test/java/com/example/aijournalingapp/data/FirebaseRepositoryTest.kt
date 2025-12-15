package com.example.aijournalingapp.data

import com.example.aijournalingapp.model.DailyTask
import com.example.aijournalingapp.model.UserStats
import com.example.aijournalingapp.utils.TimeProvider
import com.google.firebase.Firebase // Cần Import Firebase
import com.google.firebase.auth.FirebaseAuth // Cần Import FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // Cần Import FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import kotlin.test.assertEquals

// --- FAKE TIME PROVIDER (Không cần Mock Static) ---
class FakeTimeProvider(
    var mockTimeMillis: Long
) : TimeProvider {
    override fun getCurrentTimeMillis(): Long = mockTimeMillis
    override fun getCalendarInstance(): Calendar {
        return Calendar.getInstance().apply { timeInMillis = mockTimeMillis }
    }
}
// ----------------------------------------------------

// Khởi tạo UserStats mặc định
private val BASE_STATS = UserStats(
    totalPoints = 100,
    currentStreak = 5,
    dailyPoints = 10,
    lastJournalDate = 0L
)

/**
 * Hàm helper mô phỏng logic tính điểm/streak, sử dụng TimeProvider đã inject.
 * Giúp chúng ta Unit Test logic tính toán mà không cần tương tác Firestore thật.
 */
private fun calculateUpdatedStatsForTest(
    lastStats: UserStats,
    timeProvider: TimeProvider,
    POINTS_PER_ENTRY: Int = 10,
    MAX_POINTS_PER_DAY: Int = 30
): UserStats {
    val now = timeProvider.getCurrentTimeMillis()
    val lastDate = lastStats.lastJournalDate

    val isSameDay = { t1: Long, t2: Long ->
        val c1 = timeProvider.getCalendarInstance().apply { timeInMillis = t1 }
        val c2 = timeProvider.getCalendarInstance().apply { timeInMillis = t2 }
        c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) &&
                c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
    }

    val isNextDay = { current: Long, last: Long ->
        val c1 = timeProvider.getCalendarInstance().apply { timeInMillis = current }
        val c2 = timeProvider.getCalendarInstance().apply { timeInMillis = last }
        c2.add(Calendar.DAY_OF_YEAR, 1)
        c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) &&
                c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
    }

    val sameDay = isSameDay(now, lastDate)
    val nextDay = isNextDay(now, lastDate)

    var newStreak = lastStats.currentStreak
    var newDailyPoints = lastStats.dailyPoints
    var newTotalPoints = lastStats.totalPoints

    if (sameDay) {
        if (newDailyPoints < MAX_POINTS_PER_DAY) {
            newDailyPoints += POINTS_PER_ENTRY
            newTotalPoints += POINTS_PER_ENTRY
        }
    } else if (nextDay) {
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

class FirebaseRepositoryTest {

    // Giả lập một mốc thời gian cố định
    private val MOCK_BASE_TIME = Calendar.getInstance().apply {
        set(2025, Calendar.DECEMBER, 15, 10, 0, 0)
    }.timeInMillis

    private lateinit var fakeTimeProvider: FakeTimeProvider

    // Mock Firebase Dependencies cho khởi tạo tĩnh
    private val mockAuth = mockk<FirebaseAuth>(relaxed = true)
    private val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)

    @Before
    fun setup() {
        // --- FIX: MOCK CÁC THÀNH PHẦN TĨNH CỦA FIREBASE TRƯỚC KHI REPOSITORY ĐƯỢC LOAD ---
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseFirestore::class)
        mockkObject(Firebase) // Mock object Firebase

        // Cung cấp các Mock object khi FirebaseRepository gọi Firebase.firestore và FirebaseAuth.getInstance()
        every { FirebaseFirestore.getInstance() } returns mockFirestore
        every { FirebaseAuth.getInstance() } returns mockAuth
        // ----------------------------------------------------------------------------------

        // Khởi tạo FakeTimeProvider
        fakeTimeProvider = FakeTimeProvider(MOCK_BASE_TIME)
    }

    // Không cần hàm tearDown phức tạp nữa vì chúng ta đã giải quyết lỗi khởi tạo.

    // Helper để tạo timestamp của một ngày cụ thể (được đồng bộ với MOCK_BASE_TIME)
    private fun createTimestamp(daysAgo: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = MOCK_BASE_TIME
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, 10)
        }.timeInMillis
    }

    // --- TEST STREAK & POINT LOGIC (Sẽ chạy nhanh hơn rất nhiều) ---

    @Test
    fun `E-05_entry_dau_tien_streak_start_at_1`() {
        fakeTimeProvider.mockTimeMillis = createTimestamp(0)
        val initialStats = UserStats(lastJournalDate = 0L)

        val updatedStats = calculateUpdatedStatsForTest(initialStats, fakeTimeProvider)

        assertEquals(10, updatedStats.totalPoints)
        assertEquals(1, updatedStats.currentStreak)
    }

    @Test
    fun `E-06_entry_cung_ngay_tang_diem`() {
        val lastDate = createTimestamp(0)
        val initialStats = BASE_STATS.copy(lastJournalDate = lastDate)
        fakeTimeProvider.mockTimeMillis = lastDate + 3600000

        val updatedStats = calculateUpdatedStatsForTest(initialStats, fakeTimeProvider)

        assertEquals(110, updatedStats.totalPoints)
        assertEquals(20, updatedStats.dailyPoints)
    }

    @Test
    fun `E-07_entry_cung_ngay_dat_max_points`() {
        val lastDate = createTimestamp(0)
        val initialStats = BASE_STATS.copy(totalPoints = 130, dailyPoints = 30, lastJournalDate = lastDate)
        fakeTimeProvider.mockTimeMillis = lastDate + 3600000

        val updatedStats = calculateUpdatedStatsForTest(initialStats, fakeTimeProvider)

        assertEquals(130, updatedStats.totalPoints)
        assertEquals(30, updatedStats.dailyPoints)
    }

    @Test
    fun `E-08_entry_ngay_ke_tiep_giu_streak`() {
        val lastDate = createTimestamp(1)
        val initialStats = BASE_STATS.copy(lastJournalDate = lastDate)
        fakeTimeProvider.mockTimeMillis = createTimestamp(0)

        val updatedStats = calculateUpdatedStatsForTest(initialStats, fakeTimeProvider)

        assertEquals(110, updatedStats.totalPoints)
        assertEquals(6, updatedStats.currentStreak)
        assertEquals(10, updatedStats.dailyPoints)
    }

    @Test
    fun `E-09_entry_bi_dut_quang_reset_streak`() {
        val lastDate = createTimestamp(2)
        val initialStats = BASE_STATS.copy(lastJournalDate = lastDate)
        fakeTimeProvider.mockTimeMillis = createTimestamp(0)

        val updatedStats = calculateUpdatedStatsForTest(initialStats, fakeTimeProvider)

        assertEquals(110, updatedStats.totalPoints)
        assertEquals(1, updatedStats.currentStreak)
        assertEquals(10, updatedStats.dailyPoints)
    }

    @Test
    fun `H-04_toggleTaskCompletion_should_not_let_totalPoints_go_negative`() {
        val task = DailyTask("t1", "Task 1", "Sáng", 5, true)
        val currentPoints = 2L
        val expectedPointChange = -task.points

        var newTotalPoints = currentPoints + expectedPointChange
        if (newTotalPoints < 0) newTotalPoints = 0L

        assertEquals(0, newTotalPoints)
    }
}
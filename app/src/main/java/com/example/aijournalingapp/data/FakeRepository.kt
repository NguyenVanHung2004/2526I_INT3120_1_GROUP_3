package com.example.aijournalingapp.data

import android.content.Context
import com.example.aijournalingapp.model.JournalEntry
import com.example.aijournalingapp.model.UserStats
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Calendar

object FakeRepository {
    private var _journals = mutableListOf<JournalEntry>()
    private var _stats = UserStats() // Biến lưu chỉ số người chơi

    private const val JOURNAL_FILE = "my_journals.dat"
    private const val STATS_FILE = "my_stats.dat"

    // CẤU HÌNH GAME
    private const val POINTS_PER_ENTRY = 10 // 1 bài viết = 10 điểm
    private const val MAX_POINTS_PER_DAY = 30 // Tối đa 30 điểm/ngày (tránh spam)

    fun loadData(context: Context) {
        try {
            // Load Nhật ký
            context.openFileInput(JOURNAL_FILE).use {
                _journals = ObjectInputStream(it).readObject() as MutableList<JournalEntry>
            }
            // Load Stats
            context.openFileInput(STATS_FILE).use {
                _stats = ObjectInputStream(it).readObject() as UserStats
            }
        } catch (e: Exception) {
            if (_journals.isEmpty()) {
                _journals.add(JournalEntry(content = "Chào ngày mới!", mood = "😄 Vui", fakeAiAdvice = "Bắt đầu hành trình nuôi cây nhé!"))
            }
        }
    }

    private fun saveData(context: Context) {
        try {
            context.openFileOutput(JOURNAL_FILE, Context.MODE_PRIVATE).use { ObjectOutputStream(it).writeObject(_journals) }
            context.openFileOutput(STATS_FILE, Context.MODE_PRIVATE).use { ObjectOutputStream(it).writeObject(_stats) }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun getAll() = _journals.toList()
    fun getStats() = _stats

    fun add(entry: JournalEntry, context: Context) {
        _journals.add(0, entry)
        updateStats() // Tính toán điểm và streak
        saveData(context)
    }

    fun getById(id: String) = _journals.find { it.id == id }

    // LOGIC TÍNH ĐIỂM & STREAK
    private fun updateStats() {
        val now = System.currentTimeMillis()
        val lastDate = _stats.lastJournalDate

        val isSameDay = isSameDay(now, lastDate)
        val isNextDay = isNextDay(now, lastDate)

        var newStreak = _stats.currentStreak
        var newDailyPoints = _stats.dailyPoints
        var newTotalPoints = _stats.totalPoints

        if (isSameDay) {
            // Nếu vẫn là hôm nay: Chỉ cộng điểm nếu chưa max
            if (newDailyPoints < MAX_POINTS_PER_DAY) {
                newDailyPoints += POINTS_PER_ENTRY
                newTotalPoints += POINTS_PER_ENTRY
            }
        } else if (isNextDay) {
            // Nếu là ngày tiếp theo: Reset điểm ngày, Tăng Streak
            newDailyPoints = POINTS_PER_ENTRY
            newTotalPoints += POINTS_PER_ENTRY
            newStreak += 1
        } else {
            // Nếu bỏ lỡ quá 1 ngày (mất chuỗi): Reset Streak về 1
            newDailyPoints = POINTS_PER_ENTRY
            newTotalPoints += POINTS_PER_ENTRY
            newStreak = 1 // Bắt đầu chuỗi mới
        }

        _stats = _stats.copy(
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
        c2.add(Calendar.DAY_OF_YEAR, 1) // Cộng thêm 1 ngày vào ngày cũ
        return c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) &&
                c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
    }
}
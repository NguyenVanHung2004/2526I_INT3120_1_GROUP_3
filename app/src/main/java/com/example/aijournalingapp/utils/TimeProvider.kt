package com.example.aijournalingapp.utils

import java.util.Calendar

interface TimeProvider {
    fun getCurrentTimeMillis(): Long
    fun getCalendarInstance(): Calendar
}

// Lớp sử dụng trong code production
class RealTimeProvider : TimeProvider {
    override fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
    override fun getCalendarInstance(): Calendar = Calendar.getInstance()
}
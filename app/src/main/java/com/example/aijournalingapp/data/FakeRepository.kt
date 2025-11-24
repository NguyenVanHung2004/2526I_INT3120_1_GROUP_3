package com.example.aijournalingapp.data

import com.example.aijournalingapp.model.JournalEntry

object FakeRepository {
    private val _journals = mutableListOf<JournalEntry>()

    init {
        // Dữ liệu mẫu
        _journals.add(JournalEntry(content = "Chào ngày mới!", mood = "Vui", fakeAiAdvice = "Năng lượng tốt!"))
    }

    fun getAll() = _journals.toList()

    fun add(entry: JournalEntry) {
        _journals.add(0, entry)
    }

    fun getById(id: String) = _journals.find { it.id == id }
}
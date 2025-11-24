package com.example.aijournalingapp.ui.insight

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.aijournalingapp.data.FakeRepository
import com.example.aijournalingapp.model.JournalEntry

class InsightViewModel : ViewModel() {
    var entry by mutableStateOf<JournalEntry?>(null)
        private set

    fun loadEntry(id: String) {
        entry = FakeRepository.getById(id)
    }
}
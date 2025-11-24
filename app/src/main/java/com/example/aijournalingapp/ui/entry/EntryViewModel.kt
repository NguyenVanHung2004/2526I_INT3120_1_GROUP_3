package com.example.aijournalingapp.ui.entry

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.aijournalingapp.data.FakeRepository
import com.example.aijournalingapp.model.JournalEntry

class EntryViewModel : ViewModel() {
    var content by mutableStateOf("")
    var selectedMood by mutableStateOf("Vui")

    fun saveEntry(onSuccess: () -> Unit) {
        if (content.isNotBlank()) {
            val aiAdvice = if (selectedMood == "Buồn") "Cố lên nhé!" else "Tuyệt vời!"

            val newEntry = JournalEntry(
                content = content,
                mood = selectedMood,
                fakeAiAdvice = aiAdvice
            )
            FakeRepository.add(newEntry)
            onSuccess()
        }
    }
}
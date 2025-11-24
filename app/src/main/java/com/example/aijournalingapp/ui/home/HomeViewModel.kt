package com.example.aijournalingapp.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.aijournalingapp.data.FakeRepository

class HomeViewModel : ViewModel() {
    var journals = mutableStateOf(FakeRepository.getAll())
        private set

    fun refreshData() {
        journals.value = FakeRepository.getAll()
    }
}
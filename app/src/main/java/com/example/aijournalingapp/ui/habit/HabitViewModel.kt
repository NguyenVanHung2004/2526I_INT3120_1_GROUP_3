package com.example.aijournalingapp.ui.habit

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijournalingapp.data.FirebaseRepository
import com.example.aijournalingapp.model.DailyTask
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HabitViewModel : ViewModel() {
    var dailyTasks = mutableStateOf<List<DailyTask>>(emptyList())
        private set

    // Thêm loading state nếu cần
    var isLoading = mutableStateOf(true)

    init {
        // Lắng nghe danh sách nhiệm vụ realtime
        viewModelScope.launch {
            FirebaseRepository.getDailyTasksFlow().collectLatest { tasks ->
                dailyTasks.value = tasks
                isLoading.value = false
            }
        }
    }

    fun toggleTask(task: DailyTask) {
        viewModelScope.launch {
            // Hàm này đã bao gồm logic: Cập nhật Task + Cập nhật Điểm UserStats
            FirebaseRepository.toggleTaskCompletion(task)
        }
    }
}
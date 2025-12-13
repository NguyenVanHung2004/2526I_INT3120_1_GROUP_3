package com.example.aijournalingapp.ui.habit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aijournalingapp.model.DailyTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(
    navController: NavController,
    viewModel: HabitViewModel = viewModel()
) {
    val tasks = viewModel.dailyTasks.value
    // Nhóm tasks theo buổi
    val groupedTasks = tasks.groupBy { it.session }
    val sessions = listOf("Sáng", "Trưa", "Chiều", "Tối")

    Scaffold(
        containerColor = Color(0xFFF1F8E9), // Màu xanh nhạt dịu mắt
        topBar = {
            TopAppBar(
                title = { Text("Vườn ươm thói quen", fontWeight = FontWeight.Bold, color = Color(0xFF33691E)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color(0xFF33691E))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(horizontal = 24.dp).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text(
                    "Hoàn thành các nhiệm vụ nhỏ để nuôi dưỡng Cây Cảm Xúc của bạn nhé! 🌱",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF558B2F),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            sessions.forEach { session ->
                val sessionTasks = groupedTasks[session]
                if (!sessionTasks.isNullOrEmpty()) {
                    item {
                        Text(
                            session.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF689F38),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    items(sessionTasks) { task ->
                        HabitTaskItem(task = task, onToggle = { viewModel.toggleTask(task) })
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HabitTaskItem(task: DailyTask, onToggle: () -> Unit) {
    val isDone = task.isCompleted
    val bgColor = if (isDone) Color(0xFFDCEDC8) else Color.White
    val borderColor = if (isDone) Color(0xFFAED581) else Color.Transparent

    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isDone) 0.dp else 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon to
            Surface(
                color = if (isDone) Color(0xFFC5E1A5) else Color(0xFFF1F8E9),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(task.icon, fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isDone) FontWeight.Medium else FontWeight.SemiBold,
                    color = if (isDone) Color(0xFF558B2F) else Color(0xFF33691E),
                    textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                if (!isDone) {
                    Text(
                        "+${task.points} điểm năng lượng",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7CB342)
                    )
                }
            }

            Icon(
                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isDone) Color(0xFF558B2F) else Color(0xFFBDBDBD),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
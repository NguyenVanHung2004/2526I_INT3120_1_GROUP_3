package com.example.aijournalingapp.ui.entry

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    viewModel: EntryViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val moods = listOf("Vui", "Buồn", "Lo lắng", "Bình thường")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Viết nhật ký") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Cảm xúc của bạn:")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                moods.forEach { mood ->
                    FilterChip(
                        selected = viewModel.selectedMood == mood,
                        onClick = { viewModel.selectedMood = mood },
                        label = { Text(mood) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = viewModel.content,
                onValueChange = { viewModel.content = it },
                modifier = Modifier.fillMaxWidth().weight(1f),
                label = { Text("Nội dung") }
            )
            Button(
                onClick = { viewModel.saveEntry(onSuccess = onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.content.isNotBlank()
            ) {
                Text("Lưu")
            }
        }
    }
}
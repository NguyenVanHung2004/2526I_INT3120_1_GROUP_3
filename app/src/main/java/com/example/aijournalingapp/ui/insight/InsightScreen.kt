package com.example.aijournalingapp.ui.insight

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightScreen(
    journalId: String,
    viewModel: InsightViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(journalId) { viewModel.loadEntry(journalId) }
    val entry = viewModel.entry

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Góc nhìn AI") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        if (entry != null) {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("Nhật ký (${entry.mood})", style = MaterialTheme.typography.labelLarge)
                        Text(entry.content, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("Lời khuyên AI", fontWeight = FontWeight.Bold)
                        Text(entry.fakeAiAdvice)
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy nhật ký")
            }
        }
    }
}
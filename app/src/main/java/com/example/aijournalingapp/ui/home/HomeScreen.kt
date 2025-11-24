package com.example.aijournalingapp.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier // Nhớ import cái này
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aijournalingapp.ui.components.EmotionTree // Import cây vừa tạo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.refreshData() }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Nhật Ký & Cây Cảm Xúc") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("entry") }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(padding).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // === PHẦN MỚI: Cây Cảm Xúc ===
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Trạng thái tâm hồn", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Gọi Component Cây tại đây
                        EmotionTree(
                            moodScore = viewModel.treeMoodScore.value,
                            entryCount = viewModel.entryCount.value
                        )
                    }
                }
            }
            // ==============================

            item { Text("Gần đây", style = MaterialTheme.typography.titleSmall) }

            items(viewModel.journals.value) { journal ->
                Card(
                    onClick = { navController.navigate("insight/${journal.id}") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = journal.date, style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = journal.mood,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = journal.content,
                            maxLines = 2,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
package com.example.aijournalingapp.ui.insight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Màu cục bộ
private val BgColor = Color(0xFFF9F7F2)
private val TextDark = Color(0xFF37474F)
private val AICardColor = Color(0xFFE0F2F1) // Xanh ngọc rất nhạt

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
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = TextDark) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (entry != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Ngày tháng & Mood
                Text("Ngày 24 tháng 11", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(entry.mood, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = TextDark)

                Spacer(modifier = Modifier.height(32.dp))

                // 2. Nội dung gốc
                Text(
                    entry.content,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 28.sp),
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 3. Card Lời khuyên AI
                Card(
                    colors = CardDefaults.cardColors(containerColor = AICardColor),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF00695C))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thông điệp dành cho bạn", style = MaterialTheme.typography.titleMedium, color = Color(0xFF00695C))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            entry.fakeAiAdvice,
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = Color(0xFF004D40)
                        )
                    }
                }
            }
        }
    }
}
package com.example.aijournalingapp.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Màu cục bộ
private val BgColor = Color(0xFFF9F7F2)
private val TextDark = Color(0xFF37474F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(viewModel: EntryViewModel = viewModel(), onNavigateBack: () -> Unit) {
    val moods = listOf("Vui", "Buồn", "Lo lắng", "Bình thường")

    Scaffold(
        containerColor = BgColor,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = TextDark)
                }
                Text("Trang viết mới", style = MaterialTheme.typography.titleLarge, color = TextDark, modifier = Modifier.padding(start = 8.dp))
            }
        },
        bottomBar = {
            // Nút Lưu to nằm dưới cùng
            Button(
                onClick = { viewModel.saveEntry(onSuccess = onNavigateBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TextDark),
                shape = RoundedCornerShape(16.dp),
                enabled = viewModel.content.isNotBlank()
            ) {
                Text("Lưu vào ký ức", fontSize = 16.sp)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 24.dp)) {

            // 1. Mood Selector dạng Chip to
            Text("Cảm xúc lúc này?", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                moods.forEach { mood ->
                    MoodChipBig(
                        text = mood,
                        isSelected = viewModel.selectedMood == mood,
                        onClick = { viewModel.selectedMood = mood }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Ô nhập liệu (giả lập tờ giấy)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxHeight(0.6f).fillMaxWidth().border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(24.dp))
            ) {
                TextField(
                    value = viewModel.content,
                    onValueChange = { viewModel.content = it },
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    placeholder = { Text("Hãy kể cho mình nghe...", color = Color.LightGray, fontSize = 18.sp) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = TextDark)
                )
            }
        }
    }
}

@Composable
fun MoodChipBig(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Color(0xFF37474F) else Color.White
    val contentColor = if (isSelected) Color.White else Color(0xFF37474F)
    val borderColor = if (isSelected) Color.Transparent else Color(0xFFE0E0E0)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(40.dp)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .background(bgColor, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 16.dp)
    ) {
        Text(text, color = contentColor, fontWeight = FontWeight.Medium)
    }
}
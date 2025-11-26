package com.example.aijournalingapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aijournalingapp.ui.components.EmotionTreeArt
import androidx.compose.ui.platform.LocalContext

// Màu cục bộ
private val BgColor = Color(0xFFF9F7F2) // Trắng kem
private val CardBg = Color.White
private val TextDark = Color(0xFF37474F)
private val TextLight = Color(0xFF78909C)
private val AccentGreen = Color(0xFF81C784)

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.refreshData(context) }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp,top = 30.dp), // Chừa chỗ cho FAB
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Header
            item {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Chào bạn,", style = MaterialTheme.typography.headlineMedium.copy(color = TextLight))
                    Text("Hôm nay thế nào?", style = MaterialTheme.typography.headlineLarge.copy(color = TextDark, fontWeight = FontWeight.Bold))
                }
            }

            // 2. Cây cảm xúc
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    EmotionTreeArt(
                        moodScore = viewModel.treeMoodScore.value,
                        entryCount = viewModel.entryCount.value
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. Tiêu đề List
            item {
                Text(
                    "Dòng chảy ký ức",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextDark
                )
            }

            // 4. Danh sách Nhật ký (Style mới)
            items(viewModel.journals.value) { journal ->
                HealingJournalItem(
                    date = journal.date,
                    mood = journal.mood,
                    content = journal.content,
                    onClick = { navController.navigate("insight/${journal.id}") }
                )
            }
        }

        // FAB (Nút thêm mới)
        FloatingActionButton(
            onClick = { navController.navigate("entry") },
            containerColor = TextDark, // Màu tối cho nổi bật
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun HealingJournalItem(date: String, mood: String, content: String, onClick: () -> Unit) {
    // Màu mood chấm nhỏ
    val moodColor = when {

        mood.contains("Vui") ||
                mood.contains("Hạnh phúc") ||
                mood.contains("Tuyệt") ||
                mood.contains("Hào hứng") ||
                mood.contains("May mắn") -> Color(0xFF81C784)
        mood.contains("Buồn") ||
                mood.contains("Lo lắng") ||
                mood.contains("Tệ") ||
                mood.contains("Mệt") ||
                mood.contains("Chán") -> Color(0xFFFF8A65)
        else -> Color(0xFFFFD54F)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
            .background(CardBg, shape = RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Cột ngày tháng
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(date, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
            Text("NOV", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = TextLight)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Đường kẻ dọc
        Box(modifier = Modifier.width(2.dp).height(40.dp).background(Color(0xFFEEEEEE)))

        Spacer(modifier = Modifier.width(16.dp))

        // Nội dung
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(moodColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text(mood, style = MaterialTheme.typography.labelMedium, color = TextLight)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextDark,
                maxLines = 2
            )
        }
    }
}
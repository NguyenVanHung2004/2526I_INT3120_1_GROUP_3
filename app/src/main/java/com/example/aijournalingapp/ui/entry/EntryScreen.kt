package com.example.aijournalingapp.ui.entry

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome // Icon ngôi sao cho AI
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Màu sắc chủ đạo
private val BgGradientStart = Color(0xFFFDFBF7)
private val BgGradientEnd = Color(0xFFEFEBE9)
private val SurfaceColor = Color.White
private val TextPrimary = Color(0xFF455A64)
private val AccentColor = Color(0xFF78909C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(viewModel: EntryViewModel = viewModel(), onNavigateBack: () -> Unit) {
    val moodSuggestions = mapOf("Vui" to "😄", "Bình thường" to "😐", "Buồn" to "😢", "Lo lắng" to "😟")

    // Launcher giọng nói
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (spokenText != null) {
                viewModel.content = if (viewModel.content.isBlank()) spokenText else "${viewModel.content} $spokenText"
            }
        }
    }

    val currentDate = SimpleDateFormat("EEEE, d MMMM", Locale("vi", "VN")).format(Date())

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(8.dp).background(Color.White.copy(alpha = 0.6f), CircleShape)
                    ) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp).shadow(10.dp, RoundedCornerShape(16.dp))) {
                Button(
                    onClick = { viewModel.saveEntry(onSuccess = onNavigateBack) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
                    shape = RoundedCornerShape(16.dp),
                    enabled = viewModel.content.isNotBlank()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lưu ký ức", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BgGradientStart, BgGradientEnd))))

        Column(modifier = Modifier.padding(padding).padding(horizontal = 24.dp).fillMaxSize()) {
            // Header
            Text(
                text = currentDate.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = TextPrimary)
            )
            // [MỚI] Hiển thị Cảm xúc hiện tại (AI chọn hoặc Người dùng chọn)
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Cảm xúc:", style = MaterialTheme.typography.bodyMedium, color = AccentColor)
                Spacer(modifier = Modifier.width(8.dp))

                // Chip hiển thị cảm xúc Dynamic
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFFFF9C4), // Vàng nhạt
                    border = BorderStroke(1.dp, Color(0xFFFBC02D)),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "${viewModel.selectedEmoji} ${viewModel.selectedMood}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                }
            }

            // Danh sách gợi ý (để chọn nhanh nếu muốn đổi)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                moodSuggestions.forEach { (mood, emoji) ->
                    MoodItem(
                        text = mood,
                        emoji = emoji,
                        // Chỉ highlight nếu mood trùng khớp hoàn toàn
                        isSelected = viewModel.selectedMood == mood,
                        onClick = {
                            viewModel.selectedMood = mood
                            viewModel.selectedEmoji = emoji
                        }
                    )
                }
            }

            // [MỚI] Khu vực hiển thị Lời khuyên AI
            if (viewModel.generatedAdvice.isNotBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)), // Màu xanh ngọc nhạt
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF00695C), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = viewModel.generatedAdvice,
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic, lineHeight = 20.sp),
                            color = Color(0xFF004D40)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Vùng nhập liệu
            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(SurfaceColor)
            ) {
                TextField(
                    value = viewModel.content,
                    onValueChange = { viewModel.content = it },
                    modifier = Modifier.fillMaxSize().padding(16.dp).padding(bottom = 70.dp), // Chừa chỗ cho nút chức năng
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = TextPrimary
                    ),
                    placeholder = {
                        if (viewModel.isAnalyzing) {
                            Text("Đang suy ngẫm về câu chuyện của bạn...", color = AccentColor, fontStyle = FontStyle.Italic)
                        } else {
                            Text("Viết ra những suy nghĩ của bạn...\n(Hoặc bấm Micro để nói)", color = Color.LightGray, fontSize = 18.sp, fontFamily = FontFamily.Serif)
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = TextPrimary, lineHeight = 28.sp, fontFamily = FontFamily.Serif),
                    readOnly = viewModel.isAnalyzing
                )

                // Cụm nút chức năng (AI & Micro) ở góc phải dưới
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // [MỚI] Nút 1: AI Phân Tích (Màu tím)
                    FloatingActionButton(
                        onClick = { viewModel.analyzeJournal() },
                        containerColor = Color(0xFFF3E5F5), // Tím nhạt
                        contentColor = Color(0xFF7B1FA2),
                        elevation = FloatingActionButtonDefaults.elevation(2.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (viewModel.isAnalyzing) {
                            // Hiệu ứng loading khi đang phân tích
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF7B1FA2))
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Analyze", modifier = Modifier.size(24.dp))
                        }
                    }

                    // Nút 2: Voice Input (Màu xanh)
                    FloatingActionButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Đang lắng nghe...")
                            }
                            try { speechRecognizerLauncher.launch(intent) } catch (e: Exception) { }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = Color(0xFFE0F7FA),
                        contentColor = Color(0xFF006064),
                        elevation = FloatingActionButtonDefaults.elevation(2.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice", modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MoodItem(text: String, emoji: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(if (isSelected) Color(0xFFFFF176) else Color.White, label = "color")
    val shadowElevation = if (isSelected) 8.dp else 2.dp

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp).clickable(indication = null, interactionSource = null) { onClick() }) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(60.dp).shadow(shadowElevation, CircleShape).background(backgroundColor, CircleShape)
                .border(width = if (isSelected) 2.dp else 0.dp, color = if (isSelected) Color(0xFFFBC02D) else Color.Transparent, shape = CircleShape)
        ) { Text(emoji, fontSize = 28.sp) }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = if (isSelected) TextPrimary else Color.Gray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
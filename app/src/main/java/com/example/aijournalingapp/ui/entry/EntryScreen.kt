package com.example.aijournalingapp.ui.entry

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Màu sắc chủ đạo (Palette Pastel)
private val BgGradientStart = Color(0xFFFDFBF7) // Trắng kem
private val BgGradientEnd = Color(0xFFEFEBE9)   // Nâu nhạt xám
private val SurfaceColor = Color.White
private val TextPrimary = Color(0xFF455A64)
private val AccentColor = Color(0xFF78909C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(viewModel: EntryViewModel = viewModel(), onNavigateBack: () -> Unit) {
    // Map mood với Emoji tương ứng
    val moodMap = mapOf(
        "Vui" to "😄",
        "Bình thường" to "😐",
        "Buồn" to "😢",
        "Lo lắng" to "😟"
    )
    val context = LocalContext.current

    // Speech-to-Text Launcher
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

    // Lấy ngày tháng hiện tại
    val currentDate = SimpleDateFormat("EEEE, d MMMM", Locale("vi", "VN")).format(Date())

    Scaffold(
        containerColor = Color.Transparent, // Để hiện background gradient
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.White.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            // Nút Lưu nổi bật hơn
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .shadow(10.dp, RoundedCornerShape(16.dp))
            ) {
                Button(
                    onClick = { viewModel.saveEntry(onSuccess = onNavigateBack) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
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
        // Background Gradient toàn màn hình
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgGradientStart, BgGradientEnd)))
        )

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ) {
            // 1. Header Ngày tháng
            Text(
                text = currentDate.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = TextPrimary
                )
            )
            Text(
                "Bạn đang cảm thấy thế nào?",
                style = MaterialTheme.typography.bodyMedium,
                color = AccentColor,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // 2. Mood Selector đẹp hơn với Emoji
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                moodMap.forEach { (mood, emoji) ->
                    MoodItem(
                        text = mood,
                        emoji = emoji,
                        isSelected = viewModel.selectedMood == mood,
                        onClick = { viewModel.selectedMood = mood }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Vùng nhập liệu kiểu "Trang giấy"
            Box(
                modifier = Modifier
                    .weight(1f) // Chiếm hết phần còn lại
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(SurfaceColor)
            ) {
                TextField(
                    value = viewModel.content,
                    onValueChange = { viewModel.content = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = TextPrimary
                    ),
                    placeholder = {
                        Text(
                            "Viết ra những suy nghĩ của bạn...\n(Hoặc bấm Micro để nói)",
                            color = Color.LightGray,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Serif
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        color = TextPrimary,
                        lineHeight = 28.sp,
                        fontFamily = FontFamily.Serif // Font có chân tạo cảm giác viết lách
                    ),
                )

                // Nút Micro treo lơ lửng ở góc dưới phải của trang giấy
                FloatingActionButton(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Đang lắng nghe...")
                        }
                        try {
                            speechRecognizerLauncher.launch(intent)
                        } catch (e: Exception) { /* Ignore */ }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(48.dp),
                    containerColor = Color(0xFFE0F7FA), // Xanh nhạt
                    contentColor = Color(0xFF006064),
                    elevation = FloatingActionButtonDefaults.elevation(2.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Input", modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun MoodItem(text: String, emoji: String, isSelected: Boolean, onClick: () -> Unit) {
    // Animation màu nền khi chọn
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFFFF176) else Color.White, // Vàng nhạt khi chọn
        animationSpec = tween(durationMillis = 300), label = "color"
    )
    val scale = if (isSelected) 1.1f else 1.0f
    val shadowElevation = if (isSelected) 8.dp else 2.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable(indication = null, interactionSource = null) { onClick() } // Bỏ ripple mặc định
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp) // Kích thước vòng tròn mood
                .shadow(shadowElevation, CircleShape)
                .background(backgroundColor, CircleShape)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) Color(0xFFFBC02D) else Color.Transparent,
                    shape = CircleShape
                )
        ) {
            Text(emoji, fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) TextPrimary else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
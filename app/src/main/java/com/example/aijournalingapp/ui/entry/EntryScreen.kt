package com.example.aijournalingapp.ui.entry

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

// Màu sắc
private val BgGradientStart = Color(0xFFFDFBF7)
private val BgGradientEnd = Color(0xFFEFEBE9)
private val SurfaceColor = Color.White
private val TextPrimary = Color(0xFF455A64)
private val AccentColor = Color(0xFF78909C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(viewModel: EntryViewModel = viewModel(), onNavigateBack: () -> Unit) {
    val moodMap = mapOf("Vui" to "😄", "Bình thường" to "😐", "Buồn" to "😢", "Lo lắng" to "😟")
    val context = LocalContext.current

    // Launcher: Giọng nói
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (text != null) viewModel.content = if (viewModel.content.isBlank()) text else "${viewModel.content} $text"
        }
    }

    val currentDate = SimpleDateFormat("EEEE, d MMMM", Locale("vi", "VN")).format(Date())

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.padding(8.dp).background(Color.White.copy(0.6f), CircleShape)) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
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
                    enabled = viewModel.content.isNotBlank() && !viewModel.isAnalyzing
                ) {
                    if (viewModel.isAnalyzing) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lưu ký ức", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BgGradientStart, BgGradientEnd))))

        Column(modifier = Modifier.padding(padding).padding(horizontal = 24.dp).fillMaxSize()) {

            Text(currentDate.replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = TextPrimary))
            Spacer(modifier = Modifier.height(16.dp))

            // --- TOGGLE CHUYỂN CHẾ ĐỘ ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                ModeChip(text = "Tự viết ✍️", isSelected = !viewModel.isAiMode) { viewModel.isAiMode = false }
                Spacer(modifier = Modifier.width(12.dp))
                ModeChip(text = "AI Thám tử 🕵️", isSelected = viewModel.isAiMode) { viewModel.isAiMode = true }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // MOOD DISPLAY
            AnimatedVisibility(visible = !viewModel.isAiMode, enter = fadeIn(), exit = fadeOut()) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    moodMap.forEach { (mood, emoji) ->
                        MoodItem(text = mood, emoji = emoji, isSelected = viewModel.selectedMood == mood, onClick = { viewModel.selectedMood = mood; viewModel.selectedEmoji = emoji })
                    }
                }
            }
            // Khi AI chọn Mood thì hiện chip kết quả
            AnimatedVisibility(visible = viewModel.isAiMode && viewModel.selectedMood != "Bình thường", enter = fadeIn()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tâm trạng:", style = MaterialTheme.typography.labelMedium, color = AccentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(50), color = Color(0xFFFFF9C4), border = BorderStroke(1.dp, Color(0xFFFBC02D))) {
                        Text("${viewModel.selectedEmoji} ${viewModel.selectedMood}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary))
                    }
                }
            }

            // AI ADVICE CARD
            if (viewModel.generatedAdvice.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF00695C), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(viewModel.generatedAdvice, style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic), color = Color(0xFF004D40))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TEXT FIELD
            Box(modifier = Modifier.weight(1f).shadow(4.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(SurfaceColor)) {
                TextField(
                    value = viewModel.content,
                    onValueChange = { viewModel.content = it },
                    modifier = Modifier.fillMaxSize().padding(16.dp).padding(bottom = if (viewModel.isAiMode) 70.dp else 0.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = TextPrimary),
                    placeholder = {
                        if (viewModel.isAnalyzing) Text("AI đang điều tra...", color = AccentColor, fontStyle = FontStyle.Italic)
                        else Text(if (viewModel.isAiMode) "Bấm nút bên dưới để AI soi điện thoại bạn..." else "Viết gì đó đi...", color = Color.LightGray, fontSize = 18.sp, fontFamily = FontFamily.Serif)
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = TextPrimary, lineHeight = 28.sp, fontFamily = FontFamily.Serif),
                    readOnly = viewModel.isAnalyzing
                )

                // THANH CÔNG CỤ (Chỉ hiện Mic & AI Scan)
                Row(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Nút Voice (Hiện ở cả 2 chế độ cho tiện)
                    FloatingActionButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
                            }
                            try { speechLauncher.launch(intent) } catch (e: Exception) { }
                        },
                        modifier = Modifier.size(48.dp), containerColor = Color(0xFFE0F7FA), contentColor = Color(0xFF006064)
                    ) { Icon(Icons.Default.Mic, "Voice") }

                    // Nút Smart Scan (Chỉ hiện khi ở chế độ AI)
                    AnimatedVisibility(visible = viewModel.isAiMode, enter = expandHorizontally(), exit = shrinkHorizontally()) {
                        FloatingActionButton(
                            onClick = {
                                // Check quyền Usage Stats
                                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                                val mode = appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
                                val hasUsage = mode == android.app.AppOpsManager.MODE_ALLOWED

                                // Check quyền Notification
                                val listeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                                val hasNoti = listeners != null && listeners.contains(context.packageName)

                                if (hasUsage && hasNoti) {
                                    viewModel.generateSmartDiary(context)
                                } else {
                                    if (!hasUsage) context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                    else context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                }
                            },
                            containerColor = Color(0xFFE3F2FD), contentColor = Color(0xFF1565C0), modifier = Modifier.size(48.dp).padding(start = 12.dp)
                        ) { Icon(Icons.Default.Visibility, "Smart Scan") }
                    }

                    // Nút Analyze thủ công (Chỉ hiện khi ở chế độ Tự viết)
                    AnimatedVisibility(visible = !viewModel.isAiMode && viewModel.content.isNotBlank(), enter = expandHorizontally(), exit = shrinkHorizontally()) {
                        FloatingActionButton(
                            onClick = { viewModel.analyzeJournal() },
                            containerColor = Color(0xFFF3E5F5), contentColor = Color(0xFF7B1FA2), modifier = Modifier.size(48.dp).padding(start = 12.dp)
                        ) { Icon(Icons.Default.AutoAwesome, "Analyze") }
                    }
                }
            }
        }
    }
}

@Composable
fun ModeChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(if (isSelected) Color(0xFF455A64) else Color.White, label = "bg")
    val textColor by animateColorAsState(if (isSelected) Color.White else Color(0xFF455A64), label = "text")

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.dp, Color(0xFF455A64), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun MoodItem(text: String, emoji: String, isSelected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (isSelected) Color(0xFFFFF176) else Color.White, label = "color")
    val shadow = if (isSelected) 8.dp else 2.dp
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp).clickable(indication = null, interactionSource = null) { onClick() }) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp).shadow(shadow, CircleShape).background(bg, CircleShape).border(if (isSelected) 2.dp else 0.dp, if (isSelected) Color(0xFFFBC02D) else Color.Transparent, CircleShape)) { Text(emoji, fontSize = 28.sp) }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = if (isSelected) TextPrimary else Color.Gray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
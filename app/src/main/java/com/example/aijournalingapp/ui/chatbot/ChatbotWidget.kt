package com.example.aijournalingapp.ui.chatbot

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aijournalingapp.model.ChatMessage

// Màu sắc đồng bộ với App
private val BotBubbleColor = Color(0xFFF1F8E9) // Xanh nhạt (giống nền App)
private val UserBubbleColor = Color(0xFF33691E) // Xanh đậm (giống Header)
private val AccentYellow = Color(0xFFFFD54F)    // Vàng điểm nhấn

@Composable
fun ChatbotFab(
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

    FloatingActionButton(
        onClick = onClick,
        containerColor =  Color(0xFFFFF3E0),
        contentColor = Color(0xFFFFF3E0),
        shape = CircleShape,
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .border(3.dp, Color.White, CircleShape)
            .shadow(6.dp, CircleShape)
    ) {
        Icon(imageVector = Icons.Default.Face,
            contentDescription = "Chat AI",
            modifier = Modifier.size(28.dp),
            tint = Color(0xFFFF9800)
        )
    }
}

// [GIAO DIỆN MỚI]: Dùng ModalBottomSheet thay vì AlertDialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBottomSheet(
    viewModel: ChatbotViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFAFAFA), // Màu nền trắng kem sạch sẽ
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        ChatContent(viewModel, onDismiss)
    }
}

@Composable
fun ChatContent(viewModel: ChatbotViewModel, onDismiss: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    val messages = viewModel.messages
    val isSummarizing = viewModel.isSummarizing.value
    val listState = rememberLazyListState()

    // Auto scroll
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- HEADER CÓ NÚT ĐÓNG ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp), // Tăng padding chút cho thoáng
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Avatar & Title
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(shape = CircleShape, color = AccentYellow, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text("🤖", fontSize = 20.sp) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Trợ lý cảm xúc", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
                    if (isSummarizing) {
                        Text("Đang viết nhật ký...", style = MaterialTheme.typography.bodySmall, color = UserBubbleColor)
                    } else {
                        Text("Luôn lắng nghe bạn", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            // 2. Nút Lưu (Nếu chưa lưu)
            if (!isSummarizing) {
                Button(
                    onClick = { viewModel.endSessionAndSaveJournal { onDismiss() } },
                    colors = ButtonDefaults.buttonColors(containerColor = UserBubbleColor.copy(alpha = 0.1f), contentColor = UserBubbleColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lưu", fontSize = 12.sp)
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = UserBubbleColor)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. [MỚI] Nút Đóng (X)
            IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color.Gray)
            }
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

        // --- DANH SÁCH TIN NHẮN (Giữ nguyên) ---
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { MascotGreetingCard(viewModel) }
            items(messages) { msg ->
                if (messages.indexOf(msg) > 0 || msg.isUser) ChatBubbleItem(msg)
            }
            if (viewModel.isAiTyping.value) item { TypingIndicator() }
        }

        // --- INPUT BAR (Giữ nguyên) ---
        Surface(shadowElevation = 8.dp, color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp).padding(bottom = 16.dp).fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                TextField(
                    value = inputText, onValueChange = { inputText = it },
                    placeholder = { Text("Kể cho mình nghe...", color = Color.Gray) },
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(Color(0xFFF5F5F5)),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF5F5F5), unfocusedContainerColor = Color(0xFFF5F5F5), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.width(8.dp))
                val canSend = inputText.isNotBlank()
                Surface(onClick = { viewModel.sendMessage(inputText); inputText = "" }, enabled = canSend, shape = CircleShape, color = if (canSend) UserBubbleColor else Color.LightGray, modifier = Modifier.size(50.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.ArrowUpward, contentDescription = "Gửi", tint = Color.White) }
                }
            }
        }
    }
}

// Bubble Chat được thiết kế lại
@Composable
fun ChatBubbleItem(message: ChatMessage) {
    val isUser = message.isUser

    // Hình dáng bong bóng chat (Bo góc khác nhau để tạo hướng)
    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    }

    val bgColor = if (isUser) UserBubbleColor else BotBubbleColor
    val textColor = if (isUser) Color.White else Color(0xFF37474F)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Avatar Bot (chỉ hiện bên trái)
        if (!isUser) {
            Surface(shape = CircleShape, color = AccentYellow, modifier = Modifier.size(28.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("🤖", fontSize = 14.sp) }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = bubbleShape,
            color = bgColor,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
            )
        }
    }
}

// Card hiển thị lời chào (Trông giống 1 tấm thẻ hơn là tin nhắn)
@Composable
fun MascotGreetingCard(viewModel: ChatbotViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "✨ ${viewModel.currentGreeting.value}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF455A64)
            )

            if (viewModel.suggestedTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Gợi ý nhỏ:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = AccentYellow)
                viewModel.suggestedTasks.take(2).forEach {
                    Text("• ${it.title}", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = Color.LightGray.copy(alpha=0.5f), modifier = Modifier.size(8.dp)) {}
        Spacer(modifier = Modifier.width(4.dp))
        Surface(shape = CircleShape, color = Color.LightGray.copy(alpha=0.5f), modifier = Modifier.size(8.dp)) {}
        Spacer(modifier = Modifier.width(4.dp))
        Surface(shape = CircleShape, color = Color.LightGray.copy(alpha=0.5f), modifier = Modifier.size(8.dp)) {}
    }
}
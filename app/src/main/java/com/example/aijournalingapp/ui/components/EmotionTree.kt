package com.example.aijournalingapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aijournalingapp.R

// Data (Giữ nguyên)
data class TreeLevelInfo(val level: Int, val minPoints: Int, val name: String, val baseColor: Color, val drawableId: Int)

val TreeLevels = listOf(
    TreeLevelInfo(1, 0, "Hạt Giống Ngủ Quên", Color(0xFF81C784), R.drawable.flower_lv1),
    TreeLevelInfo(2, 50, "Mầm Non Tò Mò", Color(0xFF66BB6A), R.drawable.flower_lv2),
    TreeLevelInfo(3, 150, "Cây Tí Hon", Color(0xFF4CAF50), R.drawable.flower_lv3),
    TreeLevelInfo(4, 300, "Nụ Hoa E Ấp", Color(0xFF2E7D32), R.drawable.flower_lv4),
    TreeLevelInfo(5, 500, "Nụ Hoa Lớn", Color(0xFFAD1457), R.drawable.flower_lv5),
    TreeLevelInfo(6, 800, "Sắp Nở", Color(0xFFC2185B), R.drawable.flower_lv6),
    TreeLevelInfo(7, 1200, "Nở Hàm Tiếu", Color(0xFFD81B60), R.drawable.flower_lv7),
    TreeLevelInfo(8, 1700, "Mãn Khai Rực Rỡ", Color(0xFFE91E63), R.drawable.flower_lv8),
    TreeLevelInfo(9, 2300, "Tinh Thể Hóa", Color(0xFF880E4F), R.drawable.flower_lv9),
    TreeLevelInfo(10, 3000, "Linh Thần Hoa", Color(0xFFFFD700), R.drawable.flower_lv10)
)

@Composable
fun EmotionTreeArt(moodScore: Float, totalPoints: Int, onTreeClick: () -> Unit = {}) {
    // 1. Tính toán Level THỰC TẾ của người dùng
    val actualLevelInfo = TreeLevels.lastOrNull { totalPoints >= it.minPoints } ?: TreeLevels.first()
    val actualIndex = TreeLevels.indexOf(actualLevelInfo)

    // 2. Biến State cho Level ĐANG XEM (để bấm qua lại)
    // Mặc định ban đầu sẽ hiển thị Level thực tế
    var displayIndex by remember { mutableIntStateOf(actualIndex) }

    // Logic: Nếu điểm thay đổi (người dùng làm nhiệm vụ), tự động cập nhật lại view về level thực
    LaunchedEffect(actualIndex) {
        displayIndex = actualIndex
    }

    // Lấy thông tin Level đang hiển thị
    val displayInfo = TreeLevels[displayIndex]
    // Kiểm tra xem Level đang hiển thị có bị khóa không (là level tương lai?)
    val isLocked = displayIndex > actualIndex

    // Màu sắc (Nếu bị khóa thì dùng màu xám, không thì dùng màu của cây)
    val displayColor = if (isLocked) Color.Gray else displayInfo.baseColor

    // Animation
    val infiniteTransition = rememberInfiniteTransition(label = "tree")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse), label = "float"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // --- KHU VỰC CÂY VÀ MŨI TÊN ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Nút TRÁI (Ẩn nếu đang ở Level 1)
            IconButton(
                onClick = { if (displayIndex > 0) displayIndex-- },
                enabled = displayIndex > 0
            ) {
                if (displayIndex > 0) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Trước", tint = Color.Gray, modifier = Modifier.size(32.dp))
                }
            }

            // HÌNH CÂY
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp).clickable { onTreeClick() }) {
                // Hào quang (Ẩn nếu bị khóa để bớt rực rỡ)
                if (!isLocked) {
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .scale(scale * 1.1f)
                            .background(Brush.radialGradient(listOf(displayColor.copy(alpha = 0.3f), Color.Transparent)), CircleShape)
                    )
                }

                // Xử lý ảnh (Đen trắng nếu bị Lock)
//                val colorFilter = if (isLocked) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null

                Image(
                    painter = painterResource(id = displayInfo.drawableId),
                    contentDescription = displayInfo.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .offset(y = floatY.dp),
                    contentScale = ContentScale.Fit,
//                    colorFilter = colorFilter,
                    alpha = if (isLocked) 0.6f else 1f
                )

                // Icon Ổ khóa nếu đang xem level tương lai
                if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.DarkGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Nút PHẢI (Ẩn nếu đang ở Level 10)
            IconButton(
                onClick = { if (displayIndex < TreeLevels.size - 1) displayIndex++ },
                enabled = displayIndex < TreeLevels.size - 1
            ) {
                if (displayIndex < TreeLevels.size - 1) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Sau", tint = Color.Gray, modifier = Modifier.size(32.dp))
                }
            }
        }

        // --- THÔNG TIN TEXT ---

        // Tên cây
        Text(
            text = displayInfo.name,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = if (isLocked) Color.Gray else Color(0xFF455A64))
        )

        // Level Text (Thêm chữ "Preview" nếu đang xem trước)
        if (isLocked) {
            Text("(Cấp độ tương lai)", fontSize = 12.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        } else {
            // Hiển thị level hiện tại
            Text("Level ${displayInfo.level}", fontSize = 12.sp, color = displayInfo.baseColor, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- THANH TIẾN TRÌNH (Luôn hiển thị tiến độ THỰC TẾ của người dùng) ---
        // Dù người dùng xem level nào, thanh này vẫn nhắc nhở họ đang ở đâu

        val nextLevelForActual = TreeLevels.getOrNull(actualIndex + 1)
        val nextLevelPoints = nextLevelForActual?.minPoints ?: (totalPoints + 100)
        val currentLevelMinPoints = TreeLevels[actualIndex].minPoints

        val progressInRange = if (nextLevelForActual == null) 1f else {
            (totalPoints - currentLevelMinPoints).toFloat() / (nextLevelPoints - currentLevelMinPoints)
        }
        val animatedProgress by animateFloatAsState(targetValue = progressInRange.coerceIn(0f, 1f), label = "progress")

        // Chỉ hiển thị thanh tiến trình khi KHÔNG ở chế độ xem trước (hoặc luôn hiển thị tùy bạn)
        // Ở đây tôi chọn: Luôn hiển thị tiến độ thực tế, nhưng làm mờ đi nếu đang xem level khác
        val progressAlpha = if (displayIndex != actualIndex) 0.3f else 1f

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(progressAlpha)) {
            Box(modifier = Modifier.width(200.dp).height(6.dp).background(Color(0xFFECEFF1), CircleShape)) {
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animatedProgress).background(TreeLevels[actualIndex].baseColor, CircleShape))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tiến độ thực: $totalPoints / $nextLevelPoints ✨",
                fontSize = 12.sp,
                color = Color(0xFF78909C)
            )
        }
    }
}
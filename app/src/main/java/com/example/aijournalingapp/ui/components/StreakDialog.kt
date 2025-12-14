import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog // [QUAN TRỌNG] Import cái này

data class StreakMilestone(
    val days: Int,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// Danh sách các mốc
val Milestones = listOf(
    StreakMilestone(1, "Khởi đầu", Icons.Default.LocalFireDepartment),
    StreakMilestone(3, "Nóng máy", Icons.Default.Whatshot),
    StreakMilestone(7, "Bền bỉ", Icons.Default.ElectricBolt),
    StreakMilestone(14, "Thói quen", Icons.Default.Loop),
    StreakMilestone(21, "Kỷ luật", Icons.Default.Verified),
    StreakMilestone(30, "Bậc thầy", Icons.Default.EmojiEvents),
    StreakMilestone(60, "Huyền thoại", Icons.Default.Diamond),
    StreakMilestone(100, "Thần thánh", Icons.Default.AutoAwesome)
)

@Composable
fun StreakJourneyDialog(
    currentStreak: Int,
    onDismiss: () -> Unit
) {
    // Tìm mốc tiếp theo
    val nextMilestone = Milestones.firstOrNull { it.days > currentStreak } ?: Milestones.last()

    // Tính phần trăm
    val prevMilestoneDays = Milestones.lastOrNull { it.days <= currentStreak }?.days ?: 0
    val progress = if (currentStreak >= nextMilestone.days) 1f else {
        (currentStreak - prevMilestoneDays).toFloat() / (nextMilestone.days - prevMilestoneDays)
    }

    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        val index = Milestones.indexOfFirst { it.days > currentStreak }.coerceAtLeast(0)
        listState.scrollToItem(index)
    }

    // [SỬA LỖI]: Dùng Dialog thay vì AlertDialog
    Dialog(onDismissRequest = onDismiss) {
        // Vì Dialog trong suốt, ta cần một cái Card làm nền
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp), // Padding nội dung bên trong Card
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Header: Số ngày
                Box(
                    modifier = Modifier
                        .size(150.dp) // [SỬA]: Tăng từ 100.dp lên 150.dp cho thoáng
                        .background(Color(0xFFFFF3E0), CircleShape)
                        .border(6.dp, Color(0xFFFF9800), CircleShape), // Viền dày hơn chút cho nổi
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Icon lửa (nhỏ lại chút để nhường chỗ cho số)
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(28.dp)
                        )

                        // Số ngày (Dùng headlineLarge thay vì displayMedium để không bị quá khổ)
                        Text(
                            text = "$currentStreak",
                            style = MaterialTheme.typography.headlineLarge, // [SỬA]: Font nhỏ hơn xíu nhưng vẫn to
                            fontWeight = FontWeight.ExtraBold, // Đậm hơn cho ngầu
                            color = Color(0xFFE65100),
                            modifier = Modifier.offset(y = (-4).dp) // Đẩy lên xíu cho cân
                        )

                        // Chữ "ngày"
                        Text(
                            text = "NGÀY LIÊN TIẾP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFB8C00),
                            letterSpacing = 1.sp // Giãn chữ ra cho sang
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Lời nhắn
                val message = if (currentStreak >= nextMilestone.days) {
                    "Bạn đã chinh phục tất cả các mốc! Đỉnh quá! 🏆"
                } else {
                    "Cố lên! Chỉ còn ${nextMilestone.days - currentStreak} ngày nữa là đạt mốc ${nextMilestone.title}!"
                }
                Text(
                    text = message,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Danh sách mốc
                Text("Lộ trình vinh quang", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(Milestones) { milestone ->
                        MilestoneItem(milestone = milestone, currentStreak = currentStreak)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Thanh tiến trình
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mốc kế: ${nextMilestone.days} ngày", fontSize = 12.sp, color = Color.Gray)
                        Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFF9800),
                        trackColor = Color(0xFFFFE0B2),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 5. Nút Đóng
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tiếp tục giữ lửa 🔥")
                }
            }
        }
    }
}

// Widget con: Hiển thị từng mốc
@Composable
fun MilestoneItem(milestone: StreakMilestone, currentStreak: Int) {
    val isUnlocked = currentStreak >= milestone.days
    val isNext = !isUnlocked && Milestones.firstOrNull { it.days > currentStreak } == milestone

    val bgColor = if (isUnlocked) Color(0xFFFF9800) else if (isNext) Color(0xFFFFF3E0) else Color(0xFFF5F5F5)
    val iconColor = if (isUnlocked) Color.White else if (isNext) Color(0xFFFF9800) else Color.LightGray
    val textColor = if (isUnlocked) Color(0xFFE65100) else if (isNext) Color(0xFFFF9800) else Color.Gray

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        // Icon tròn
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(bgColor, CircleShape)
                .border(if (isNext) 2.dp else 0.dp, if (isNext) Color(0xFFFF9800) else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(milestone.icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Số ngày
        Text(
            "${milestone.days} ngày",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        // Tên danh hiệu
        Text(
            milestone.title,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = Color.Gray,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}
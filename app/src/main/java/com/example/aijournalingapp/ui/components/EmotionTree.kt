package com.example.aijournalingapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EmotionTree(
    moodScore: Float, // 0.0 (Buồn) -> 1.0 (Vui)
    entryCount: Int // Số lượng nhật ký
) {
    // 1. Tính toán màu sắc dựa trên moodScore
    val leafColor = when {
        moodScore >= 0.7f -> Color(0xFF4CAF50) // Xanh lá (Vui)
        moodScore >= 0.4f -> Color(0xFFFFC107) // Vàng (Bình thường)
        else -> Color(0xFFFF5722) // Cam đỏ (Buồn/Lo)
    }

    // 2. Tính toán kích thước tán cây dựa trên số lượng bài viết (Max 50 bài là full size)
    val growthFactor = (entryCount / 10f).coerceIn(0.5f, 1.5f)
    val animatedScale by animateFloatAsState(targetValue = growthFactor, label = "treeScale")

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // --- Vẽ Thân cây (Cố định) ---
            val trunkWidth = canvasWidth * 0.1f
            val trunkHeight = canvasHeight * 0.4f

            drawRect(
                color = Color(0xFF795548), // Màu nâu
                topLeft = Offset((canvasWidth - trunkWidth) / 2, canvasHeight - trunkHeight),
                size = Size(trunkWidth, trunkHeight)
            )

            // --- Vẽ Tán lá (Thay đổi theo biến) ---
            // Dùng scale để phóng to/thu nhỏ tán lá
            val leafRadius = (canvasWidth * 0.35f) * animatedScale

            drawCircle(
                color = leafColor,
                radius = leafRadius,
                center = Offset(canvasWidth / 2, canvasHeight / 2 - (trunkHeight * 0.2f))
            )

            // Vẽ thêm 2 tán phụ nhỏ hơn cho đẹp
            drawCircle(
                color = leafColor.copy(alpha = 0.8f),
                radius = leafRadius * 0.7f,
                center = Offset(canvasWidth / 2 - leafRadius * 0.6f, canvasHeight / 2)
            )
            drawCircle(
                color = leafColor.copy(alpha = 0.8f),
                radius = leafRadius * 0.7f,
                center = Offset(canvasWidth / 2 + leafRadius * 0.6f, canvasHeight / 2)
            )
        }

        // Hiển thị text trạng thái
        Text(
            text = if (moodScore >= 0.5) "Cây đang vui" else "Cây cần chăm sóc",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
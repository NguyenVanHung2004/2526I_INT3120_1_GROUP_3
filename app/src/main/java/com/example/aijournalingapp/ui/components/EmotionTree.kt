package com.example.aijournalingapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmotionTreeArt(
    moodScore: Float,
    entryCount: Int
) {
    // Animation nhịp thở nhẹ nhàng
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    // Màu sắc cây thay đổi theo Mood
    val treeColor = when {
        moodScore >= 0.7f -> Color(0xFF81C784) // Green 300
        moodScore >= 0.4f -> Color(0xFFFFD54F) // Amber 300
        else -> Color(0xFFFF8A65) // Deep Orange 300
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(240.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFF1F8E9))
                ),
                shape = CircleShape
            )
    ) {
        Canvas(modifier = Modifier.size(180.dp).scale(scale)) {
            val w = size.width
            val h = size.height

            // 1. Vẽ thân cây (Dáng cong mềm mại)
            val trunkPath = Path().apply {
                moveTo(w * 0.45f, h)
                quadraticBezierTo(w * 0.5f, h * 0.8f, w * 0.5f, h * 0.6f)
                quadraticBezierTo(w * 0.5f, h * 0.8f, w * 0.55f, h)
                close()
            }
            drawPath(path = trunkPath, color = Color(0xFF8D6E63)) // Brown

            // 2. Vẽ tán lá (Nhiều vòng tròn xếp chồng)
            // Tán càng to nếu entryCount càng lớn (max size = 1.5)
            val growth = (1f + (entryCount * 0.05f)).coerceAtMost(1.5f)

            val radiusMain = w * 0.35f * growth

            // Tán chính
            drawCircle(color = treeColor, center = Offset(w * 0.5f, h * 0.4f), radius = radiusMain)
            // Tán phụ trái
            drawCircle(color = treeColor.copy(alpha = 0.9f), center = Offset(w * 0.3f, h * 0.5f), radius = radiusMain * 0.7f)
            // Tán phụ phải
            drawCircle(color = treeColor.copy(alpha = 0.9f), center = Offset(w * 0.7f, h * 0.5f), radius = radiusMain * 0.7f)
        }

        // Text chỉ số
        Text(
            text = "${(moodScore * 100).toInt()}% An Yên",
            color = Color(0xFF546E7A),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
        )
    }
}
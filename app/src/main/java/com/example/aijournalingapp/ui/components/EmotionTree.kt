package com.example.aijournalingapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

// Màu sắc ảo diệu (Neon/Pastel) - Giữ nguyên của bạn
val NeonGreen = Color(0xFF69F0AE)
val NeonBlue = Color(0xFF40C4FF)
val NeonPink = Color(0xFFFF4081)
val NeonGold = Color(0xFFFFD740)
val DeepPurple = Color(0xFF311B92)

@Composable
fun EmotionTreeArt(
    moodScore: Float, // [SỬA]: Đổi sang Int (0-100) để khớp với ViewModel
    totalPoints: Int
) {
    // [LOGIC LEVEL MỚI]: Cứ 50 điểm là 1 mốc để thanh năng lượng chạy đẹp hơn
    // Level 1: < 50 điểm
    // Level 2: < 150 điểm
    // Level 3: >= 150 điểm
    val treeLevel = when {
        totalPoints < 50 -> 1
        totalPoints < 150 -> 2
        else -> 3
    }

    // [SỬA]: Chuyển đổi thang điểm 0-100 sang màu sắc
    val primaryColor = when {
        moodScore >= 80 -> NeonGreen // Vui (80-100)
        moodScore >= 50 -> NeonGold  // Bình thường (50-79)
        else -> NeonBlue             // Buồn (<50)
    }

    // Nếu Level 3 -> Luôn là màu Hồng/Tím huyền ảo
    val finalColor = if (treeLevel == 3) NeonPink else primaryColor

    // Hệ thống hạt (Particles)
    val particles = remember { mutableStateListOf<Particle>() }

    // Loop animation hạt bay
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { _ ->
                val maxParticles = if (treeLevel == 1) 30 else if (treeLevel == 2) 80 else 150
                if (particles.size < maxParticles) {
                    particles.add(generateParticle(treeLevel, finalColor))
                }
                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.update()
                    if (p.isDead()) iterator.remove()
                }
            }
        }
    }

    // Animation thở nhẹ (Glow)
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "alpha"
    )

    // Animation cho thanh Progress chạy mượt khi cộng điểm
    val nextLevelPoints = if (treeLevel == 1) 50 else if (treeLevel == 2) 150 else totalPoints
    // Tính toán % hiển thị:
    // Nếu ở Level 2 (VD: 60 điểm), ta muốn hiển thị đoạn 60/150
    val targetProgress = if (treeLevel == 3) 1f else totalPoints.toFloat() / nextLevelPoints

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(320.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. Vẽ Nền Hào Quang
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(finalColor.copy(alpha = 0.2f), Color.Transparent),
                        center = Offset(w/2, h * 0.6f),
                        radius = w * 0.6f
                    ),
                    center = Offset(w/2, h * 0.6f),
                    radius = w * 0.6f
                )

                // 2. Vẽ Cây theo Level
                when (treeLevel) {
                    1 -> drawGlowingSeed(w, h, finalColor, glowAlpha)
                    2 -> drawCrystalTree(w, h, finalColor, glowAlpha)
                    3 -> drawSpiritTree(w, h, finalColor, glowAlpha)
                }

                // 3. Vẽ Hạt Bay (Particles) - Giữ nguyên logic cũ
                with(drawContext.canvas.nativeCanvas) {
                    val checkPoint = saveLayer(null, null)
                    particles.forEach { p ->
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha),
                            radius = p.size,
                            center = Offset(w/2 + p.x, h * 0.7f - p.y)
                        )
                    }
                    restoreToCount(checkPoint)
                }
            }
        }

        // Text thông tin Level
        val levelTitle = when(treeLevel) {
            1 -> "✨ Tinh Thể Hy Vọng"
            2 -> "💎 Cây Pha Lê"
            3 -> "🌸 Thần Mộc Rực Rỡ"
            else -> ""
        }

        Text(levelTitle, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Light, color = Color(0xFF455A64), letterSpacing = 2.sp))

        Spacer(modifier = Modifier.height(12.dp))

        // Thanh EXP mảnh mai
        Box(modifier = Modifier.width(200.dp).height(4.dp).background(Color(0xFFCFD8DC), androidx.compose.foundation.shape.CircleShape)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress) // Dùng giá trị Animation
                    .background(finalColor, androidx.compose.foundation.shape.CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Text hiển thị điểm
        Text(
            text = "$totalPoints / $nextLevelPoints năng lượng",
            fontSize = 12.sp,
            color = Color(0xFF90A4AE),
            fontWeight = FontWeight.Bold
        )
    }
}

// --- GIỮ NGUYÊN CÁC HÀM VẼ CŨ CỦA BẠN BÊN DƯỚI ---
// (Copy lại các hàm drawGlowingSeed, drawCrystalTree, drawSpiritTree, Particle class...)

fun DrawScope.drawGlowingSeed(w: Float, h: Float, color: Color, glow: Float) {
    val cx = w / 2
    val cy = h * 0.65f
    drawCircle(color = color.copy(alpha = glow * 0.5f), radius = 30f, center = Offset(cx, cy))
    drawCircle(color = color, radius = 10f, center = Offset(cx, cy))
    val leafPath = Path().apply {
        moveTo(cx, cy)
        quadraticBezierTo(cx - 20f, cy - 20f, cx - 30f, cy - 40f)
        quadraticBezierTo(cx - 10f, cy - 30f, cx, cy)
        close()
    }
    drawPath(leafPath, color.copy(alpha = 0.8f))
    val leafPath2 = Path().apply {
        moveTo(cx, cy)
        quadraticBezierTo(cx + 20f, cy - 20f, cx + 30f, cy - 40f)
        quadraticBezierTo(cx + 10f, cy - 30f, cx, cy)
        close()
    }
    drawPath(leafPath2, color.copy(alpha = 0.8f))
}

fun DrawScope.drawCrystalTree(w: Float, h: Float, color: Color, glow: Float) {
    val cx = w / 2
    val rootY = h * 0.8f
    val trunkPath = Path().apply {
        moveTo(cx, rootY)
        quadraticBezierTo(cx - 10f, rootY - 50f, cx, rootY - 100f)
        lineTo(cx, rootY - 100f)
    }
    drawPath(trunkPath, color = Color.Gray, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f, cap = StrokeCap.Round))
    drawLine(Color.Gray, Offset(cx, rootY - 60f), Offset(cx - 30f, rootY - 90f), strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(Color.Gray, Offset(cx, rootY - 70f), Offset(cx + 30f, rootY - 100f), strokeWidth = 3f, cap = StrokeCap.Round)
    drawCircle(brush = Brush.radialGradient(listOf(color, Color.Transparent)), radius = 40f * glow + 20f, center = Offset(cx, rootY - 110f))
    drawCircle(brush = Brush.radialGradient(listOf(color, Color.Transparent)), radius = 30f * glow + 10f, center = Offset(cx - 35f, rootY - 95f))
    drawCircle(brush = Brush.radialGradient(listOf(color, Color.Transparent)), radius = 30f * glow + 10f, center = Offset(cx + 35f, rootY - 105f))
}

fun DrawScope.drawSpiritTree(w: Float, h: Float, color: Color, glow: Float) {
    val cx = w / 2
    val rootY = h * 0.85f
    val trunkPath = Path().apply {
        moveTo(cx - 20f, rootY)
        quadraticBezierTo(cx - 10f, rootY - 100f, cx - 40f, rootY - 150f)
        lineTo(cx + 40f, rootY - 150f)
        quadraticBezierTo(cx + 10f, rootY - 100f, cx + 20f, rootY)
        close()
    }
    drawPath(trunkPath, brush = Brush.verticalGradient(listOf(Color(0xFF455A64), Color(0xFF263238))))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.8f), color.copy(alpha = 0.4f), Color.Transparent),
            center = Offset(cx, rootY - 160f),
            radius = 140f
        )
    )
    drawCircle(
        color = Color.White.copy(alpha = glow),
        radius = 40f,
        center = Offset(cx, rootY - 160f)
    )
}

class Particle(
    var x: Float, var y: Float, var vx: Float, var vy: Float,
    var size: Float, var alpha: Float, var color: Color, var life: Float
) {
    fun update() {
        x += vx
        y += vy
        alpha -= 0.005f
        life -= 1f
    }
    fun isDead() = alpha <= 0f || life <= 0f
}

fun generateParticle(level: Int, color: Color): Particle {
    val random = Random.Default
    val speed = if (level == 3) 1.5f else 0.8f
    return Particle(
        x = (random.nextFloat() - 0.5f) * 200f,
        y = (random.nextFloat()) * 50f,
        vx = (random.nextFloat() - 0.5f) * speed,
        vy = random.nextFloat() * speed + 0.5f,
        size = random.nextFloat() * 6f + 2f,
        alpha = 1f,
        color = if (random.nextBoolean()) color else Color.White,
        life = 100f + random.nextFloat() * 100f
    )
}
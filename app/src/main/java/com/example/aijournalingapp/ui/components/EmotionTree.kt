package com.example.aijournalingapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

// Màu sắc ảo diệu (Neon/Pastel)
val NeonGreen = Color(0xFF69F0AE)
val NeonBlue = Color(0xFF40C4FF)
val NeonPink = Color(0xFFFF4081)
val NeonGold = Color(0xFFFFD740)
val DeepPurple = Color(0xFF311B92)

@Composable
fun EmotionTreeArt(
    moodScore: Float,
    totalPoints: Int
) {
    // Level 1: Hạt mầm sáng (Glowing Seed)
    // Level 2: Cây pha lê (Crystal Tree)
    // Level 3: Cây Thần (Spirit Tree - Nở hoa sáng rực)
    val treeLevel = when {
        totalPoints < 50 -> 1
        totalPoints < 150 -> 2
        else -> 3
    }

    // Màu chủ đạo theo Mood
    val primaryColor = when {
        moodScore >= 0.8f -> NeonGreen // Vui -> Xanh Neon
        moodScore >= 0.5f -> NeonGold  // Bình thường -> Vàng kim
        else -> NeonBlue // Buồn -> Xanh dương lạnh
    }

    // Nếu Level 3 -> Luôn là màu Hồng/Tím huyền ảo
    val finalColor = if (treeLevel == 3) NeonPink else primaryColor

    // Hệ thống hạt (Particles)
    val particles = remember { mutableStateListOf<Particle>() }

    // Loop animation thủ công để điều khiển hạt bay
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { time ->
                // Thêm hạt mới (nếu chưa đủ số lượng tối đa)
                val maxParticles = if (treeLevel == 1) 30 else if (treeLevel == 2) 80 else 150
                if (particles.size < maxParticles) {
                    particles.add(generateParticle(treeLevel, finalColor))
                }

                // Cập nhật vị trí hạt
                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.update()
                    if (p.isDead()) iterator.remove()
                }
            }
        }
    }

    // Animation thở nhẹ cho thân cây
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "alpha"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(320.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. Vẽ Nền Hào Quang (Aura)
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

                // 3. Vẽ Hạt Bay (Particles)
                // Dùng BlendMode.Add để tạo hiệu ứng phát sáng rực rỡ (chỉ hoạt động tốt trên nền tối, nhưng trên nền sáng cũng tạo cảm giác trong trẻo)
                with(drawContext.canvas.nativeCanvas) {
                    val checkPoint = saveLayer(null, null)
                    particles.forEach { p ->
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha),
                            radius = p.size,
                            center = Offset(w/2 + p.x, h * 0.7f - p.y) // Tọa độ tính từ gốc cây đi lên
                        )
                    }
                    restoreToCount(checkPoint)
                }
            }
        }

        // Text thông tin (Style tối giản sang trọng)
        val levelTitle = when(treeLevel) {
            1 -> "✨ Tinh Thể Hy Vọng"
            2 -> "💎 Cây Pha Lê"
            3 -> "🌸 Thần Mộc Rực Rỡ"
            else -> ""
        }

        Text(levelTitle, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Light, color = Color(0xFF455A64), letterSpacing = 2.sp))

        // Thanh EXP mảnh mai
        val nextLevel = if (treeLevel == 1) 50 else if (treeLevel == 2) 150 else totalPoints
        val progress = if (treeLevel == 3) 1f else totalPoints.toFloat() / nextLevel

        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.width(200.dp).height(2.dp).background(Color(0xFFCFD8DC))) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress).background(finalColor))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("$totalPoints / $nextLevel năng lượng", fontSize = 12.sp, color = Color(0xFF90A4AE))
    }
}

// --- CÁC HÀM VẼ CHI TIẾT ---

// Level 1: Hạt giống đang bay lơ lửng phát sáng
fun DrawScope.drawGlowingSeed(w: Float, h: Float, color: Color, glow: Float) {
    val cx = w / 2
    val cy = h * 0.65f

    // Vòng sáng bao quanh
    drawCircle(
        color = color.copy(alpha = glow * 0.5f),
        radius = 30f,
        center = Offset(cx, cy)
    )
    // Hạt nhân
    drawCircle(
        color = color,
        radius = 10f,
        center = Offset(cx, cy)
    )
    // 2 chiếc lá năng lượng nhỏ xoay quanh
    val leafPath = Path().apply {
        moveTo(cx, cy)
        quadraticBezierTo(cx - 20f, cy - 20f, cx - 30f, cy - 40f)
        quadraticBezierTo(cx - 10f, cy - 30f, cx, cy)
        close()
    }
    drawPath(leafPath, color.copy(alpha = 0.8f))

    // Lá thứ 2 (đối xứng)
    val leafPath2 = Path().apply {
        moveTo(cx, cy)
        quadraticBezierTo(cx + 20f, cy - 20f, cx + 30f, cy - 40f)
        quadraticBezierTo(cx + 10f, cy - 30f, cx, cy)
        close()
    }
    drawPath(leafPath2, color.copy(alpha = 0.8f))
}

// Level 2: Cây thân mảnh, tán lá là các khối pha lê
fun DrawScope.drawCrystalTree(w: Float, h: Float, color: Color, glow: Float) {
    val cx = w / 2
    val rootY = h * 0.8f

    // Thân cây (Đường cong mảnh)
    val trunkPath = Path().apply {
        moveTo(cx, rootY)
        quadraticBezierTo(cx - 10f, rootY - 50f, cx, rootY - 100f) // Thân chính
        lineTo(cx, rootY - 100f)
    }
    drawPath(trunkPath, color = Color.Gray, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f, cap = StrokeCap.Round))

    // Các cành
    drawLine(Color.Gray, Offset(cx, rootY - 60f), Offset(cx - 30f, rootY - 90f), strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(Color.Gray, Offset(cx, rootY - 70f), Offset(cx + 30f, rootY - 100f), strokeWidth = 3f, cap = StrokeCap.Round)

    // Tán cây là các quả cầu năng lượng
    drawCircle(brush = Brush.radialGradient(listOf(color, Color.Transparent)), radius = 40f * glow + 20f, center = Offset(cx, rootY - 110f))
    drawCircle(brush = Brush.radialGradient(listOf(color, Color.Transparent)), radius = 30f * glow + 10f, center = Offset(cx - 35f, rootY - 95f))
    drawCircle(brush = Brush.radialGradient(listOf(color, Color.Transparent)), radius = 30f * glow + 10f, center = Offset(cx + 35f, rootY - 105f))
}

// Level 3: Cây cổ thụ thần tiên (Tán rộng, rực rỡ)
fun DrawScope.drawSpiritTree(w: Float, h: Float, color: Color, glow: Float) {
    val cx = w / 2
    val rootY = h * 0.85f

    // Thân cây to, màu tối để làm nền cho ánh sáng
    val trunkPath = Path().apply {
        moveTo(cx - 20f, rootY)
        quadraticBezierTo(cx - 10f, rootY - 100f, cx - 40f, rootY - 150f) // Nhánh trái
        lineTo(cx + 40f, rootY - 150f) // Nhánh phải
        quadraticBezierTo(cx + 10f, rootY - 100f, cx + 20f, rootY)
        close()
    }
    drawPath(trunkPath, brush = Brush.verticalGradient(listOf(Color(0xFF455A64), Color(0xFF263238))))

    // Tán cây là một vùng hào quang lớn
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.8f), color.copy(alpha = 0.4f), Color.Transparent),
            center = Offset(cx, rootY - 160f),
            radius = 140f
        )
    )

    // Lõi sáng rực ở giữa tán
    drawCircle(
        color = Color.White.copy(alpha = glow),
        radius = 40f,
        center = Offset(cx, rootY - 160f)
    )
}

// --- HỆ THỐNG HẠT (PARTICLE SYSTEM) ---
class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var alpha: Float,
    var color: Color,
    var life: Float
) {
    fun update() {
        x += vx
        y += vy
        alpha -= 0.005f // Mờ dần
        life -= 1f
    }

    fun isDead() = alpha <= 0f || life <= 0f
}

fun generateParticle(level: Int, color: Color): Particle {
    val random = Random.Default
    val speed = if (level == 3) 1.5f else 0.8f

    return Particle(
        x = (random.nextFloat() - 0.5f) * 200f, // Phân bố ngang gốc cây
        y = (random.nextFloat()) * 50f, // Bắt đầu từ thấp
        vx = (random.nextFloat() - 0.5f) * speed, // Bay ngang ngẫu nhiên
        vy = random.nextFloat() * speed + 0.5f, // Luôn bay lên
        size = random.nextFloat() * 6f + 2f,
        alpha = 1f,
        color = if (random.nextBoolean()) color else Color.White, // Pha trộn màu trắng cho lấp lánh
        life = 100f + random.nextFloat() * 100f
    )
}
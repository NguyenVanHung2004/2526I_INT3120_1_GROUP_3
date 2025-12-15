package com.example.aijournalingapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.aijournalingapp.R
// Composable này chỉ dùng để render ra ảnh, không dùng để tương tác
@Composable
fun ShareAchievementCard(
    userName: String,
    streak: Int,
    treeInfo: TreeLevelInfo,
    modifier: Modifier = Modifier
) {
    val today = SimpleDateFormat("dd 'tháng' MM, yyyy", Locale("vi", "VN")).format(Date())
    Card(
        modifier = modifier
            .width(350.dp) // Kích thước cố định cho ảnh đẹp
            .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFFDE7), Color(0xFFE8F5E9)) // Gradient Vàng kem -> Xanh nhạt
                    )
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // 1. Header: Logo & App Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // [THAY THẾ Icon BẰNG Image]
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Journal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
                Text("HÀNH TRÌNH NUÔI DƯỠNG TÂM HỒN", fontSize = 10.sp, color = Color(0xFF558B2F), letterSpacing = 2.sp, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Hero Image: Cây hiện tại
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(220.dp)
                ) {
                    // Hào quang nền
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(1.1f)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(treeInfo.baseColor.copy(alpha = 0.4f), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )
                    // Ảnh cây
                    Image(
                        painter = painterResource(id = treeInfo.drawableId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Text(treeInfo.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                Text("Level ${treeInfo.level}", fontSize = 14.sp, color = treeInfo.baseColor, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Stats Box (Streak & User)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFFFFE0B2))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Streak
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFFF9800), modifier = Modifier.size(32.dp))
                            Text("$streak Ngày", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            Text("Liên tiếp", fontSize = 12.sp, color = Color.Gray)
                        }
                        // User Info
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(userName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                            Text(today, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("“Hãy kiên nhẫn, mọi đóa hoa đều cần thời gian để nở.”",
                    fontSize = 12.sp,
                    color = Color(0xFF78909C),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
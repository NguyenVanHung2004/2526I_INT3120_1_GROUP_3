package com.example.aijournalingapp.ui.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aijournalingapp.ui.components.EmotionTreeArt
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Màu cục bộ
private val BgColor = Color(0xFFF9F7F2)
private val TextDark = Color(0xFF37474F)
private val TextLight = Color(0xFF78909C)
private val DrawerBg = Color(0xFFFFFFFF)
private val ActiveItemColor = Color(0xFFE8F5E9)
private val ActiveTextColor = Color(0xFF2E7D32)

@Composable
fun HomeScreen(navController: NavController, onLogout: () -> Unit, viewModel: HomeViewModel = viewModel()) {
    LaunchedEffect(Unit) { viewModel.refreshData() }

    val firebaseUser = Firebase.auth.currentUser
    val userName = firebaseUser?.displayName ?: "Người dùng"
    val userEmail = firebaseUser?.email ?: ""
    // Quản lý trạng thái Drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DrawerBg,
                modifier = Modifier.width(300.dp) // Độ rộng của menu
            ) {
                // --- DRAWER HEADER: Thông tin User ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F8E9)) // Nền xanh nhẹ cho header menu
                        .padding(24.dp)
                        .padding(top = 24.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Color(0xFFC5E1A5),
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFF33691E),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(userEmail, style = MaterialTheme.typography.bodySmall, color = TextLight)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- DANH SÁCH MENU ---
                DrawerMenuItem(
                    icon = Icons.Default.Home,
                    label = "Trang chủ",
                    isSelected = true, // Đang ở trang chủ
                    onClick = { scope.launch { drawerState.close() } }
                )

                DrawerMenuItem(
                    icon = Icons.Default.CheckCircle, // Icon Thói quen
                    label = "Vườn ươm thói quen",
                    isSelected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("habit")
                    }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Person,
                    label = "Hồ sơ của tôi",
                    isSelected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        // TODO: Navigate to Profile
                    }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Settings,
                    label = "Cài đặt",
                    isSelected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        // TODO: Navigate to Settings
                    }
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp))

                DrawerMenuItem(
                    icon = Icons.Default.ExitToApp,
                    label = "Đăng xuất",
                    isSelected = false,
                    isDestructive = true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    }
                )
            }
        }
    ) {
        // --- NỘI DUNG MÀN HÌNH CHÍNH ---
        Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Header Mới (Gọn gàng hơn)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 40.dp, bottom = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ngày tháng
                            val today = SimpleDateFormat("EEEE, d MMM", Locale("vi", "VN")).format(Date())
                            Text(
                                text = today.uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextLight
                            )

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Badge Streak
                                Surface(
                                    color = Color(0xFFFFF3E0),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, Color(0xFFFFE0B2))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${viewModel.currentStreak.value}", style = MaterialTheme.typography.labelLarge, color = Color(0xFFEF6C00), fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Nút Avatar -> Mở Side Menu
                                Surface(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable { scope.launch { drawerState.open() } }, // MỞ DRAWER
                                    shape = CircleShape,
                                    color = Color(0xFFEDE7F6),
                                    border = BorderStroke(1.dp, Color(0xFFD1C4E9))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF673AB7)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Chào bạn, $userName", style = MaterialTheme.typography.titleMedium, color = TextLight)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Hôm nay thế nào?", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold, color = TextDark))
                    }
                }

                // 2. Cây cảm xúc
                item {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        EmotionTreeArt(moodScore = viewModel.treeMoodScore.value*100, totalPoints = viewModel.totalPoints.value)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 3. Tiêu đề List
                item {
                    Text("Dòng chảy ký ức", modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), style = MaterialTheme.typography.titleMedium, color = TextDark)
                }

                // 4. Danh sách Nhật ký
                items(viewModel.journals.value, key = { it.id }) { journal ->
                    HealingJournalItem(
                        date = journal.date,
                        mood = journal.mood,
                        content = journal.content,
                        onClick = { navController.navigate("insight/${journal.id}") }
                    )
                }
            }

            // FAB
            FloatingActionButton(
                onClick = { navController.navigate("entry") },
                containerColor = TextDark,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .size(64.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp))
            }
        }
    }
}

// Component cho từng mục trong Menu Drawer
@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val colors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = ActiveItemColor,
        unselectedContainerColor = Color.Transparent,
        selectedTextColor = ActiveTextColor,
        unselectedTextColor = if (isDestructive) Color.Red else TextDark,
        selectedIconColor = ActiveTextColor,
        unselectedIconColor = if (isDestructive) Color.Red else Color.Gray
    )

    NavigationDrawerItem(
        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
        selected = isSelected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        colors = colors,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    )
}

// Giữ lại hàm HealingJournalItem ở đây hoặc import từ file cũ (nếu bạn tách file)
@Composable
fun HealingJournalItem(date: String, mood: String, content: String, onClick: () -> Unit) {
    // ... (Code cũ giữ nguyên, copy lại hàm này từ file HomeScreen cũ vào đây nếu cần)
    // Để tiết kiệm không gian tôi không paste lại hàm này, bạn giữ nguyên logic cũ nhé.
    val moodColor = when {
        mood.contains("Vui") || mood.contains("Hạnh phúc") || mood.contains("Tuyệt") -> Color(0xFF81C784)
        mood.contains("Buồn") || mood.contains("Lo lắng") -> Color(0xFFFF8A65)
        else -> Color(0xFFFFD54F)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
            .background(Color.White, shape = RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(date.split("/").first(), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
            Text("THÁNG ${date.split("/").last()}", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = TextLight)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier.width(2.dp).height(40.dp).background(Color(0xFFEEEEEE)))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(moodColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text(mood, style = MaterialTheme.typography.labelMedium, color = TextLight)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = content, style = MaterialTheme.typography.bodyMedium, color = TextDark, maxLines = 2)
        }
    }
}
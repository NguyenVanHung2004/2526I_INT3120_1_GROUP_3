package com.example.aijournalingapp.ui.home

import StreakJourneyDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aijournalingapp.ui.chatbot.ChatBottomSheet
import com.example.aijournalingapp.ui.components.EmotionTreeArt
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.aijournalingapp.ui.chatbot.ChatbotFab // [IMPORT QUAN TRỌNG]
import com.example.aijournalingapp.ui.chatbot.ChatbotViewModel
import com.example.aijournalingapp.ui.components.TreeLevels
import androidx.compose.material.icons.filled.Share
import com.example.aijournalingapp.ui.components.ShareAchievementCard
import com.example.aijournalingapp.utils.ImageShareUtils
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

    // [MỚI]: Khai báo ChatViewModel và trạng thái hiển thị Dialog ở đây
    val chatViewModel: ChatbotViewModel = viewModel()
    var showChatDialog by remember { mutableStateOf(false) }
    var showStreakDialog by remember { mutableStateOf(false) }
    var showSharePreview by remember { mutableStateOf(false) }
    var shouldCaptureImage by remember { mutableStateOf(false) }
    val firebaseUser = Firebase.auth.currentUser
    val userName = firebaseUser?.displayName ?: "Người dùng"
    val userEmail = firebaseUser?.email ?: ""
    // Quản lý trạng thái Drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
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
                    icon = Icons.Default.Face, // Dùng icon mặt người
                    label = "Trợ lý cảm xúc",
                    isSelected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showChatDialog = true // Mở hộp thoại chat
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Default.CheckCircle, // Icon Thói quen
                    label = "Nhiệm vụ hằng ngày",
                    isSelected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("habit")
                    }
                )

                // 4. [MỚI] Bộ sưu tập Linh Mộc (Thay cho Profile/Setting)
                DrawerMenuItem(
                    icon = Icons.Default.EmojiNature, // Icon hình thiên nhiên/cây cối
                    label = "Bộ sưu tập Linh Mộc",
                    isSelected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("tree_gallery")
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Default.Share,
                    label = "Chia sẻ thành tựu",
                    isSelected = false,
                    onClick = {
                        scope.launch { drawerState.close() } // Đóng menu
                        showSharePreview = true // Mở dialog preview
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

                DrawerMenuItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
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
            // [LOGIC MỚI]: Sắp xếp và Gom nhóm Nhật ký theo Ngày
            val groupedJournals = remember(viewModel.journals.value) {
                viewModel.journals.value
                    .sortedByDescending { it.timestamp } // Mới nhất lên đầu
                    .groupBy { journal ->
                        // Tạo key gom nhóm: "Hôm nay" hoặc "Thứ Ba, 14/12/2025"
                        val date = Date(journal.timestamp)
                        val today = Date()
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                        if (sdf.format(date) == sdf.format(today)) {
                            "Hôm nay"
                        } else {
                            // Format: Thứ Hai, 10 tháng 12
                            SimpleDateFormat("EEEE, d 'tháng' M", Locale("vi", "VN")).format(date).replaceFirstChar { it.uppercase() }
                        }
                    }
            }
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Header Mới (Đồng bộ & Sang trọng)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 48.dp, bottom = 24.dp) // Tăng padding top chút cho thoáng
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // A. Ngày tháng (Giữ nguyên)
                            Column {
                                val todayDate = SimpleDateFormat("d MMM", Locale("vi", "VN")).format(Date())
                                val todayDay = SimpleDateFormat("EEEE", Locale("vi", "VN")).format(Date())

                                Text(
                                    text = todayDay.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextLight.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = todayDate,
                                    style = MaterialTheme.typography.headlineSmall, // Chữ to hơn chút
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }

                            // B. Cụm nút Streak & Avatar (Đã sửa lại cho đồng bộ)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 1. Badge Streak: Nền trắng, bo tròn, đổ bóng nhẹ
                                Surface(
                                    shape = RoundedCornerShape(50), // Hình viên thuốc
                                    color = Color.White,
                                    modifier = Modifier.clickable { showStreakDialog = true },
                                    shadowElevation = 2.dp, // Đổ bóng nhẹ tạo độ nổi
                                    border = BorderStroke(1.dp, Color(0xFFF1F8E9)) // Viền xanh siêu nhạt
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                            .height(32.dp), // Cố định chiều cao
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Icon Lửa
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = Color(0xFFFF9800), // Màu cam lửa
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        // Số ngày
                                        Text(
                                            "${viewModel.currentStreak.value}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark
                                        )
                                    }
                                }

                                // 2. Avatar Button: Cũng nền trắng, đổ bóng y hệt Streak
                                Surface(
                                    modifier = Modifier
                                        .size(46.dp) // Kích thước vuông vức bao quanh (lớn hơn chiều cao streak chút)
                                        .clickable { scope.launch { drawerState.open() } },
                                    shape = CircleShape,
                                    color = Color.White,
                                    shadowElevation = 2.dp,
                                    border = BorderStroke(1.dp, Color(0xFFF1F8E9))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        // Vòng tròn màu bên trong (Avatar thực sự)
                                        Surface(
                                            modifier = Modifier.size(36.dp),
                                            shape = CircleShape,
                                            color = Color(0xFFE8F5E9) // Xanh nhạt (Theme App) thay vì Tím
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2E7D32) // Chữ xanh đậm
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Lời chào (Giữ nguyên)
                        Text("Chào bạn, $userName", style = MaterialTheme.typography.titleMedium, color = TextLight)
                        Text("Hôm nay thế nào?", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold, color = TextDark))
                    }
                }

                // 2. Cây cảm xúc
                item {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        EmotionTreeArt(moodScore = viewModel.treeMoodScore.value*100, totalPoints = viewModel.totalPoints.value.toInt(),
                                onTreeClick = { navController.navigate("tree_gallery") }
                        )
                        Text(
                            "(Chạm vào cây để xem bộ sưu tập)",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 3. Tiêu đề List
                item {
                    Text("Dòng chảy ký ức", modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), style = MaterialTheme.typography.titleMedium, color = TextDark)
                }

                groupedJournals.forEach { (dateString, journalsInDay) ->
                    // A. Header Ngày (Ví dụ: "Hôm nay", "Thứ Ba...")
                    item {
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF90A4AE),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }

                    // B. Danh sách Nhật ký trong ngày đó
                    items(journalsInDay, key = { it.id }) { journal ->
                        HealingJournalItem(
                            date = journal.date, // Vẫn giữ date hiển thị trên card để đẹp
                            mood = journal.mood,
                            content = journal.content,
                            onClick = { navController.navigate("insight/${journal.id}") }
                        )
                    }
                }

                // Nếu chưa có nhật ký nào
                if (groupedJournals.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("Chưa có ký ức nào...", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp) // Khoảng cách giữa 2 nút
            ) {
                // 1. Nút Chatbot AI (Nằm trên)
                ChatbotFab(onClick = { showChatDialog = true })

                // 2. Nút Thêm thủ công (Nằm dưới)
                FloatingActionButton(
                    onClick = { navController.navigate("entry") },
                    containerColor = TextDark,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp))
                }
            }
            if (showStreakDialog) {
                StreakJourneyDialog(
                    currentStreak = viewModel.currentStreak.value,
                    onDismiss = { showStreakDialog = false }
                )
            }
            if (showChatDialog) {
                ChatBottomSheet(
                    viewModel = chatViewModel,
                    onDismiss = { showChatDialog = false }
                )
            }
            if (showSharePreview) {
                // Lấy thông tin cây hiện tại từ ViewModel hoặc tính toán trực tiếp
                // Logic này giống hệt trong EmotionTreeArt
                val currentPoints = viewModel.totalPoints.value.toInt()
                val currentTreeInfo = TreeLevels.lastOrNull { currentPoints >= it.minPoints } ?: TreeLevels.first()

                SharePreviewDialog(
                    userName = userName,
                    streak = viewModel.currentStreak.value,
                    treeInfo = currentTreeInfo,
                    onDismiss = { showSharePreview = false },
                    onShareClick = {
                        try {
                            // Gọi hàm tiện ích chụp ảnh và share
                            ImageShareUtils.shareComposableAsImage(context) {
                                ShareAchievementCard(
                                    userName = userName,
                                    streak = viewModel.currentStreak.value,
                                    treeInfo = currentTreeInfo
                                )
                            }
                        }catch (e: Exception) {
                            e.printStackTrace()
                            // Hiện thông báo lỗi lên màn hình để biết đường sửa
                            android.widget.Toast.makeText(context, "Lỗi: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                            }
                        showSharePreview = false
                    }
                )
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
@Composable
fun SharePreviewDialog(
    userName: String,
    streak: Int,
    treeInfo: com.example.aijournalingapp.ui.components.TreeLevelInfo,
    onDismiss: () -> Unit,
    onShareClick: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // Để background trong suốt để thấy bo góc của Card
            modifier = Modifier.background(Color.Transparent)
        ) {
            // Hiển thị Card đẹp
            ShareAchievementCard(userName = userName, streak = streak, treeInfo = treeInfo)

            Spacer(modifier = Modifier.height(24.dp))

            // Các nút hành động
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Gray),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đóng")
                }

                Button(
                    onClick = onShareClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chia sẻ ngay")
                }
            }
        }
    }
}
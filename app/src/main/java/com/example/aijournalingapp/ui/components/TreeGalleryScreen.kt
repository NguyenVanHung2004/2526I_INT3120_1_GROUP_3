package com.example.aijournalingapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aijournalingapp.ui.home.HomeViewModel
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeGalleryScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel()
) {
    val totalPoints = homeViewModel.totalPoints.value

    Scaffold(
        containerColor = Color(0xFFF9FBE7),
        topBar = {
            TopAppBar(
                title = { Text("Bộ Sưu Tập Linh Mộc", fontWeight = FontWeight.Bold, color = Color(0xFF33691E)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFF33691E))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Header card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCEDC8)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tổng năng lượng hiện tại", fontSize = 14.sp, color = Color(0xFF558B2F))
                    Text("$totalPoints", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF33691E))
                    Text("Tích lũy thêm điểm để mở khóa các giống cây huyền thoại!", fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color(0xFF689F38))
                }
            }

            // Grid danh sách cây
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(TreeLevels) { tree ->
                    val isUnlocked = totalPoints >= tree.minPoints
                    TreeItemCard(tree, isUnlocked)
                }
            }
        }
    }
}

@Composable
fun TreeItemCard(tree: TreeLevelInfo, isUnlocked: Boolean) {
    val bgColor = if (isUnlocked) Color.White else Color(0xFFEEEEEE)
    val borderColor = if (isUnlocked) tree.baseColor else Color.Gray

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor.copy(alpha = if (isUnlocked) 0.5f else 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            // --- VÙNG HIỂN THỊ ẢNH CÂY ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp) // Kích thước ô ảnh trong gallery
                    .padding(8.dp)
            ) {
                // Tạo bộ lọc màu xám nếu chưa mở khóa
                val colorFilter = if (!isUnlocked) {
                    ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                } else null

                Image(
                    painter = painterResource(id = tree.drawableId),
                    contentDescription = tree.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    colorFilter = colorFilter, // Áp dụng bộ lọc xám
                    alpha = if (isUnlocked) 1f else 0.6f // Làm mờ nhẹ nếu khóa
                )

                // Nếu chưa mở khóa, phủ icon ổ khóa lên trên
                if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.DarkGray.copy(alpha = 0.8f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            // ------------------------------------

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                tree.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isUnlocked) Color.Black else Color.Gray,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (!isUnlocked) {
                Text(
                    "Cần ${tree.minPoints} ✨",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            } else {
                Text(
                    "Đã sở hữu",
                    fontSize = 12.sp,
                    color = tree.baseColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
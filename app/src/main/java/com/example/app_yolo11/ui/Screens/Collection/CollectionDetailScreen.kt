package com.example.app_yolo11.ui.Screens.Collection

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Locale

private val OceanGradientStart = Color(0xFF4FC3F7)
private val OceanGradientEnd = Color(0xFF006994)
private val OceanSurface = Color(0xFFF0F4F8)
private val TextPrimary = Color(0xFF102A43)
private val TextSecondary = Color(0xFF627D98)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    itemId: String,
    navController: NavHostController,
    viewModel: CollectionViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val item = items.find { it.id == itemId }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (item == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = OceanGradientEnd)
        }
        return
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa mẫu vật?", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text("Bạn có chắc muốn xóa '${item.name}' khỏi bộ sưu tập không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCollection(item.id,
                            onSuccess = {
                                Toast.makeText(context, "Đã xóa thành công", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onError = { Toast.makeText(context, "Lỗi: $it", Toast.LENGTH_SHORT).show() }
                        )
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) { Text("Xóa bỏ") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        containerColor = OceanSurface
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    if (item.images.isNotEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { item.images.size })
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AsyncImage(
                                model = item.images[page],
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                        )

                        if (item.images.size > 1) {
                            Row(
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 24.dp)
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(item.images.size) { i ->
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (pagerState.currentPage == i) Color.White
                                                else Color.White.copy(alpha = 0.5f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-20).dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = OceanSurface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = item.name,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (!item.scientificName.isNullOrBlank()) {
                            Text(
                                text = item.scientificName,
                                fontSize = 18.sp,
                                fontStyle = FontStyle.Italic,
                                color = OceanGradientEnd,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item.collectedDate?.let {
                                InfoChip(
                                    icon = Icons.Default.CalendarToday,
                                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                                )
                            }
                            if (!item.location.isNullOrBlank()) {
                                InfoChip(
                                    icon = Icons.Default.LocationOn,
                                    text = item.location
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Ghi chú",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (item.description.isNullOrBlank()) "Không có mô tả chi tiết cho mẫu vật này." else item.description,
                            fontSize = 15.sp,
                            color = TextSecondary,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .shadow(4.dp, CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(
                        onClick = { navController.navigate("edit_collection/${item.id}") },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                            .shadow(4.dp, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = OceanGradientEnd
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                            .shadow(4.dp, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF5252)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(icon: ImageVector, text: String) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OceanGradientEnd,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
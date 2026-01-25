package com.example.app_yolo11.ui.Screens.History

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app_yolo11.model.SeaSnails

val OceanGradientStart = Color(0xFF4FC3F7)
val OceanGradientEnd = Color(0xFF006994)
val OceanSurface = Color(0xFFF0F4F8)
val TextPrimary = Color(0xFF102A43)
val TextSecondary = Color(0xFF627D98)

val MainGradientBrush = Brush.linearGradient(
    colors = listOf(OceanGradientStart, OceanGradientEnd),
    tileMode = TileMode.Clamp
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavHostController,
    viewModel: HistoryViewModel
) {
    val historyItems by viewModel.historyList.collectAsState()

    Scaffold(
        containerColor = OceanSurface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Lịch sử tra cứu",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {

                    IconButton(onClick = { navController.popBackStack() }) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                actions = {
                    if (historyItems.isNotEmpty()) {

                        IconButton(onClick = { viewModel.clearAll() }) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFFFEBEE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Xóa tất cả",
                                    tint = Color(0xFFD32F2F), // Icon đỏ đậm
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = OceanSurface,
                    scrolledContainerColor = OceanSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            if (historyItems.isEmpty()) {
                ModernHistoryEmptyState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
                    modifier = Modifier.animateContentSize()
                ) {
                    items(historyItems, key = { it.id }) { item ->
                        ModernHistoryItem(
                            snail = item,
                            onItemClick = { id ->
                                navController.navigate("detail_screen/$id")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernHistoryItem(snail: SeaSnails, onItemClick: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color.LightGray.copy(alpha = 0.3f))
            .clickable { onItemClick(snail.id) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(70.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (snail.imageUrl.isNotEmpty()) {

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(snail.imageUrl).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            alpha = 0.3f
                        )

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(snail.imageUrl).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                            error = rememberVectorPainter(Icons.Default.ImageNotSupported),
                            placeholder = rememberVectorPainter(Icons.Default.Waves)
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFFE1F5FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Waves, contentDescription = null, tint = OceanGradientStart)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = snail.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(OceanGradientEnd, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = snail.scientificName,
                        fontSize = 14.sp,
                        color = OceanGradientEnd,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }


            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}


@Composable
fun ModernHistoryEmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.White, CircleShape)
                .border(2.dp, OceanSurface, CircleShape)
                .shadow(8.dp, CircleShape, spotColor = Color.LightGray.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = OceanGradientStart.copy(alpha = 0.6f),
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Lịch sử trống",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Các loài ốc bạn đã xem sẽ xuất hiện tại đây.",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 40.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
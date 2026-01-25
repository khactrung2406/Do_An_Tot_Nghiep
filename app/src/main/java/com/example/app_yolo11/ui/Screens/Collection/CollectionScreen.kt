package com.example.app_yolo11.ui.Screens.Collection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.app_yolo11.model.CollectionItem
import java.text.SimpleDateFormat
import java.util.Locale


private val OceanGradientStart = Color(0xFF4FC3F7)
private val OceanGradientEnd = Color(0xFF006994)
private val OceanSurface = Color(0xFFF0F4F8)
private val TextPrimary = Color(0xFF102A43)
private val TextSecondary = Color(0xFF627D98)

private val MainGradientBrush = Brush.linearGradient(
    colors = listOf(OceanGradientStart, OceanGradientEnd),
    tileMode = TileMode.Clamp
)

@Composable
fun CollectionScreen(
    navController: NavHostController,
    viewModel: CollectionViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = OceanSurface,
        floatingActionButton = {

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(8.dp, CircleShape, spotColor = OceanGradientEnd.copy(alpha = 0.5f))
                    .clip(CircleShape)
                    .background(MainGradientBrush)
                    .clickable { navController.navigate("add_collection") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        if (isLoading && items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OceanGradientEnd)
            }
        } else if (items.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Bộ sưu tập",
                    color = OceanGradientEnd,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp),
                    textAlign = TextAlign.Center
                )

                ModernEmptyCollectionState()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 20.dp,
                    bottom = 100.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item(span = { GridItemSpan(2) }) {
                    Column {
                        Text(
                            text = "Bộ sưu tập",
                            color = OceanGradientEnd,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Lưu giữ những khám phá của bạn",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }


                items(items) { item ->
                    ModernCollectionItemCard(item) {
                        navController.navigate("detail_collection/${item.id}")
                    }
                }
            }
        }
    }
}

@Composable
fun ModernCollectionItemCard(item: CollectionItem, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color.LightGray.copy(alpha = 0.4f))
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(OceanSurface)
            ) {
                AsyncImage(
                    model = item.images.firstOrNull(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Default.ImageNotSupported)
                )
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.05f)), startY = 100f)))
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.name.ifBlank { "Chưa đặt tên" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!item.scientificName.isNullOrBlank()) {
                    Text(
                        text = item.scientificName,
                        fontSize = 13.sp,
                        color = OceanGradientEnd,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Spacer(modifier = Modifier.height(18.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = OceanSurface, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.collectedDate?.let {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                        } ?: "--/--/----",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                if (!item.location.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.location,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // ---------------------------------
            }
        }
    }
}

@Composable
fun ModernEmptyCollectionState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(OceanSurface, CircleShape)
                .border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Collections,
                contentDescription = null,
                tint = OceanGradientStart.copy(alpha = 0.5f),
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Bộ sưu tập trống",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Hãy thêm mẫu vật đầu tiên của bạn!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}
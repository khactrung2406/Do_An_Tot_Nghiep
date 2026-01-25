package com.example.app_yolo11.ui.Screens.Information

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
// Đã xóa các import không dùng đến (ColorFilter, ColorMatrix)
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app_yolo11.model.SeaSnails

val OceanBlueDark = Color(0xFF006994)
val OceanBlueLight = Color(0xFF4FC3F7)
val SoftSurface = Color(0xFFF8FDFF)

@Composable
fun DetailScreen(
    navController: NavHostController,
    snail: SeaSnails,
    viewModel: InformationViewModel
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(SoftSurface)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)

                    .background(SoftSurface)
            ) {

                // CHỈ GIỮ LẠI 1 ẢNH CHÍNH
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(snail.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = snail.name,

                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    SoftSurface
                                ),
                                startY = 250f
                            )
                        )
                )
            }



            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(SoftSurface)
                    .padding(24.dp)
            ) {

                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.LightGray.copy(alpha = 0.5f))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = snail.name,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = OceanBlueDark,
                    lineHeight = 36.sp
                )

                Text(
                    text = snail.scientificName,
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(24.dp))
                InfoItem(Icons.Default.Category, "Họ (Family)", snail.family)
                InfoItem(Icons.Outlined.Eco, "Mô tả nhận dạng", snail.description)
                InfoItem(Icons.Default.Water, "Môi trường sống", snail.habitat)
                InfoItem(Icons.Default.Pets, "Tập tính", snail.behavior)
                InfoItem(Icons.Default.Map, "Phân bố", snail.distribution)
                InfoItem(Icons.Default.Shield, "Tình trạng bảo tồn", snail.conservationStatus, highlight = true)
                InfoItem(Icons.Default.MonetizationOn, "Giá trị kinh tế/Sử dụng", snail.value)

                Spacer(modifier = Modifier.height(50.dp))
            }
        }

        // Nút Back
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(top = 40.dp, start = 16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.3f))
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    highlight: Boolean = false
) {
    if (value.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (highlight) Color(0xFFFFEBEE)
                        else OceanBlueLight.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (highlight) Color(0xFFD32F2F) else OceanBlueDark,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = if (highlight) Color(0xFFD32F2F) else Color(0xFF263238),
                    fontWeight = if (highlight) FontWeight.Medium else FontWeight.Normal,
                    lineHeight = 24.sp
                )
            }
        }
    }
}
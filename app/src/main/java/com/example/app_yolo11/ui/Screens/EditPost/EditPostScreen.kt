package com.example.app_yolo11.ui.Screens.EditPost

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage


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
fun EditPostScreen(
    navController: NavHostController,
    postId: String,
    viewModel: EditPostViewModel = hiltViewModel()
) {
    val content by viewModel.postContent.collectAsState()
    val imageUrl by viewModel.postImage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUpdated by viewModel.isUpdated.collectAsState()


    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }


    LaunchedEffect(isUpdated) {
        if (isUpdated) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = OceanSurface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Chỉnh sửa bài viết",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
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
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                actions = {

                    TextButton(
                        onClick = { viewModel.saveChanges(postId) },
                        enabled = !isLoading && content.isNotBlank()
                    ) {
                        Text(
                            "LƯU",
                            fontWeight = FontWeight.Bold,
                            color = if (isLoading) Color.Gray else OceanGradientEnd,
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = OceanSurface)
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isLoading && content.isEmpty()) {

                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = OceanGradientEnd
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))


                    Text(
                        text = "Nội dung",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )

                    ModernEditInput(
                        value = content,
                        onValueChange = { viewModel.updateContent(it) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (imageUrl.isNotEmpty()) {
                        Text(
                            text = "Ảnh đính kèm",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                        )

                        ModernImagePreview(imageUrl = imageUrl)
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }


            if (isLoading && content.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OceanGradientEnd)
                }
            }
        }
    }
}



@Composable
fun ModernEditInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            placeholder = { Text("Nhập nội dung bài viết...", color = Color.LightGray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = OceanGradientEnd,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
        )
    }
}

@Composable
fun ModernImagePreview(imageUrl: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(OceanSurface),
                contentScale = ContentScale.Crop
            )


            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ảnh hiện tại",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
package com.example.app_yolo11.ui.Screens.Create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
fun CreatePostScreen(
    navController: NavHostController,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    var content by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isHelpRequest by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) imageUri = uri
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.resetState()
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = OceanSurface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Tạo bài viết",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 20.sp
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = OceanSurface)
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))


                ModernImagePicker(
                    imageUri = imageUri,
                    onPickImage = { imagePicker.launch("image/*") },
                    onRemoveImage = { imageUri = null }
                )

                Spacer(modifier = Modifier.height(24.dp))


                ModernTextInput(
                    value = content,
                    onValueChange = { content = it }
                )

                Spacer(modifier = Modifier.height(20.dp))


                ModernHelpToggle(
                    isHelpRequest = isHelpRequest,
                    onToggle = { isHelpRequest = !isHelpRequest }
                )

                Spacer(modifier = Modifier.height(32.dp))


                Button(
                    onClick = {
                        viewModel.createPost(context, content, imageUri, isHelpRequest)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = OceanGradientEnd.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MainGradientBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "ĐĂNG NGAY",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }


            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(enabled = false) {}, // Chặn click
                    contentAlignment = Alignment.Center
                ) {

                }
            }
        }
    }
}



@Composable
fun ModernImagePicker(
    imageUri: Uri?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit
) {
    val stroke = Stroke(
        width = 4f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .then(
                if (imageUri == null) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = Color.LightGray,
                            style = stroke,
                            cornerRadius = CornerRadius(24.dp.toPx())
                        )
                    }
                } else Modifier
            )
            .clickable { onPickImage() },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clickable { onRemoveImage() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // Change Label
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Nhấn để thay đổi ảnh", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(OceanSurface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = OceanGradientEnd,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Thêm hình ảnh", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Chia sẻ khoảnh khắc đại dương", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ModernTextInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Bạn đang nghĩ gì thế? Hãy chia sẻ chi tiết...", color = Color.LightGray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = OceanGradientEnd,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
    }
}

@Composable
fun ModernHelpToggle(
    isHelpRequest: Boolean,
    onToggle: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isHelpRequest) Color(0xFFFFEBEE) else Color.White,
        animationSpec = tween(300),
        label = "bgColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isHelpRequest) Color(0xFFEF5350) else Color.Transparent,
        animationSpec = tween(300),
        label = "borderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if(isHelpRequest) 0.dp else 2.dp),
        border = if (isHelpRequest) androidx.compose.foundation.BorderStroke(1.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isHelpRequest) Color(0xFFEF5350) else OceanSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.HelpOutline,
                    contentDescription = null,
                    tint = if (isHelpRequest) Color.White else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cần định danh loài ốc?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isHelpRequest) Color(0xFFD32F2F) else TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if(isHelpRequest) "Đã bật! Cộng đồng sẽ hỗ trợ bạn." else "Bật lên để nhờ cộng đồng giúp đỡ.",
                    fontSize = 13.sp,
                    color = if(isHelpRequest) Color(0xFFE57373) else TextSecondary
                )
            }

            // Checkbox simulation
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(2.dp, if (isHelpRequest) Color(0xFFEF5350) else Color.LightGray, CircleShape)
                    .background(if (isHelpRequest) Color(0xFFEF5350) else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isHelpRequest) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
package com.example.app_yolo11.ui.Screens.Home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app_yolo11.R
import com.example.app_yolo11.ui.Screens.Community.CommunityScreen
import com.example.app_yolo11.ui.Screens.Notification.NotificationScreen

val OceanGradientStart = Color(0xFF4FC3F7)
val OceanGradientEnd = Color(0xFF006994)
val OceanSurface = Color(0xFFF0F4F8)
val TextPrimary = Color(0xFF102A43)

val MainGradientBrush = Brush.linearGradient(
    colors = listOf(OceanGradientStart, OceanGradientEnd),
    tileMode = TileMode.Clamp
)

@Composable
fun HomeScreenMain(navController: NavHostController, viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = {
                showLogoutDialog = false
                viewModel.signout()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    Scaffold(
        containerColor = OceanSurface,
        bottomBar = {
            ModernBottomNavigation(
                selectedItem = selectedTab,
                onItemSelected = { index -> viewModel.onTabSelected(index) }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ModernChatBotButton(
                    onClick = { navController.navigate("chat_bot") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ModernHomeTabContent(uiState.userName, avatarUrl = uiState.avatar, navController)
                1 -> CommunityScreen(navController)
                2 -> NotificationScreen(navController)
                3 -> SettingsTabContent(navController, onLogoutClick = { showLogoutDialog = true })
            }
        }
    }
}

@Composable
fun ModernChatBotButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .offset(y = (-10).dp)
            .size(68.dp)
            .shadow(10.dp, CircleShape)
            .background(Color.White, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(MainGradientBrush),
            contentAlignment = Alignment.Center
        ) {
            Image(

                painter = painterResource(id = R.drawable.snail),
                contentDescription = "Chat AI",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(12.dp)
                .background(Color(0xFFFF5252), CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

@Composable
fun ModernHomeTabContent(userName: String, avatarUrl: String?, navController: NavHostController) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(MainGradientBrush)
            )

            Box(modifier = Modifier.size(100.dp).offset(x = (-20).dp, y = (-20).dp).background(Color.White.copy(alpha = 0.1f), CircleShape))
            Box(modifier = Modifier.size(150.dp).align(Alignment.BottomEnd).offset(x = 40.dp, y = 40.dp).background(Color.White.copy(alpha = 0.1f), CircleShape))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Xin chào,",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .border(2.dp, Color.White, CircleShape)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { navController.navigate("profile") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(avatarUrl).crossfade(true).build(),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.LightGray
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = (-40).dp)
        ) {
            ModernHeroCard(
                onClick = { navController.navigate("camera") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Khám phá tiện ích",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModernGridItem(
                        modifier = Modifier.weight(1f),
                        title = "Tìm từ khóa",
                        icon = Icons.Default.Search,
                        colorStart = Color(0xFFE8F5E9),
                        colorIcon = Color(0xFF4CAF50),
                        onClick = { navController.navigate("search") }
                    )
                    ModernGridItem(
                        modifier = Modifier.weight(1f),
                        title = "Lịch sử",
                        icon = Icons.Default.History,
                        colorStart = Color(0xFFFFF3E0),
                        colorIcon = Color(0xFFFF9800),
                        onClick = { navController.navigate("history") }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModernGridItem(
                        modifier = Modifier.weight(1f),
                        title = "Bài viết",
                        icon = Icons.Default.EditNote,
                        colorStart = Color(0xFFF3E5F5),
                        colorIcon = Color(0xFF9C27B0),
                        onClick = { navController.navigate("manage_posts") }
                    )
                    ModernGridItem(
                        modifier = Modifier.weight(1f),
                        title = "Bộ sưu tập",
                        icon = Icons.Default.Collections,
                        colorStart = Color(0xFFFCE4EC),
                        colorIcon = Color(0xFFE91E63),
                        onClick = { navController.navigate("collection") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ModernHeroCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = OceanGradientStart.copy(alpha = 0.5f))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(OceanGradientStart.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("AI Scanner", color = OceanGradientEnd, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nhận diện\nCác loài ốc biển", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary, lineHeight = 26.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chụp ngay", color = Color.Gray, fontSize = 14.sp)
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                }
            }

            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxHeight()
                    .background(MainGradientBrush),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(120.dp).background(Color.White.copy(alpha = 0.1f), CircleShape))

                Icon(
                    imageVector = Icons.Default.CenterFocusWeak,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}

@Composable
fun ModernGridItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    colorStart: Color,
    colorIcon: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1.1f)
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color.LightGray.copy(alpha = 0.3f))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colorStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorIcon,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun ModernBottomNavigation(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier
            .shadow(20.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        val items = listOf(
            Triple("Home", Icons.Default.Home, Icons.Outlined.Home),
            Triple("News", Icons.Default.Newspaper, Icons.Outlined.Newspaper),
            Triple("Alert", Icons.Default.Notifications, Icons.Outlined.Notifications),
            Triple("Menu", Icons.Default.Settings, Icons.Outlined.Settings)
        )

        items.forEachIndexed { index, item ->
            val isSelected = selectedItem == index
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.second else item.third,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = null,
                selected = isSelected,
                onClick = { onItemSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OceanGradientEnd,
                    selectedTextColor = OceanGradientEnd,
                    indicatorColor = OceanGradientStart.copy(alpha = 0.15f),
                    unselectedIconColor = Color.LightGray
                )
            )
        }
    }
}

@Composable
fun SettingsTabContent(navController: NavHostController, onLogoutClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Cài đặt",
            color = OceanGradientEnd,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Tài khoản",
            fontSize = 14.sp,
            color = TextPrimary.copy(alpha = 0.6f),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                SettingItemRow(
                    icon = Icons.Default.PersonOutline,
                    title = "Hồ sơ cá nhân",
                    onClick = { navController.navigate("profile") }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 20.dp))
                SettingItemRow(
                    icon = Icons.Default.History,
                    title = "Lịch sử nhận diện",
                    onClick = { navController.navigate("history") }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 20.dp))
                SettingItemRow(
                    icon = Icons.Default.Info,
                    title = "Về ứng dụng",
                    showArrow = false,
                    onClick = { }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Hệ thống",
            fontSize = 14.sp,
            color = TextPrimary.copy(alpha = 0.6f),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogoutClick() }
                .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color.Red.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Đăng xuất",
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "Phiên bản 1.0.0",
                color = TextPrimary.copy(alpha = 0.4f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SettingItemRow(
    icon: ImageVector,
    title: String,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(OceanGradientStart.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OceanGradientEnd,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        if (showArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun LogoutConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Đăng xuất", fontWeight = FontWeight.Bold, color = OceanGradientEnd) },
        text = { Text("Bạn có muốn đăng xuất không?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Đăng xuất") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Ở lại", color = Color.Gray) }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}
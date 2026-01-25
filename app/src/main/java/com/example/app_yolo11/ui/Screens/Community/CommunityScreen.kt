package com.example.app_yolo11.ui.Screens.Community

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.app_yolo11.model.SocialPost
import com.google.firebase.auth.FirebaseAuth

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
fun CommunityScreen(
    navController: NavHostController,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userInfo by viewModel.currentUserInfo.collectAsState()

    Scaffold(
        containerColor = OceanSurface,
        floatingActionButton = {

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(10.dp, CircleShape, spotColor = OceanGradientEnd.copy(alpha = 0.5f))
                    .clip(CircleShape)
                    .background(MainGradientBrush)
                    .clickable { navController.navigate("create_post") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Đăng bài",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        topBar = {

            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Cộng đồng",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 24.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = OceanSurface,
                    scrolledContainerColor = OceanSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            ModernInputBar(
                userAvatar = userInfo?.second ?: "",
                userName = userInfo?.first ?: "Bạn",
                onClick = { navController.navigate("create_post") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading && posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OceanGradientEnd)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (posts.isEmpty()) {
                        item { ModernEmptyFeedMessage() }
                    } else {
                        items(posts) { post ->
                            ModernPostCard(post = post, viewModel = viewModel, navController = navController)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ModernInputBar(
    userAvatar: String,
    userName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color.LightGray.copy(alpha = 0.4f))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (userAvatar.isNotBlank()) {
                AsyncImage(
                    model = userAvatar,
                    contentDescription = "My Avatar",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.dp, OceanGradientStart, CircleShape),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Default.MoreHoriz)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F7FA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MoreHoriz, null, tint = OceanGradientEnd)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))


            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(50))
                    .background(OceanSurface)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bạn đang nghĩ gì thế?",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(Icons.Filled.PhotoLibrary, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
            }
        }
    }
}


@Composable
fun ModernPostCard(
    post: SocialPost,
    viewModel: CommunityViewModel,
    navController: NavHostController
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val isLiked = post.likes.contains(currentUserId)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.userAvatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F7FA)),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Default.MoreHoriz)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.userName.ifBlank { "Người dùng ẩn danh" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatTimeAgo(post.createdAt),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Public, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(10.dp))
                    }
                }


                if (post.isHelpRequest) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Cần tên?",
                            color = Color(0xFFD32F2F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }


            if (post.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .background(OceanSurface)
                )
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(if (isLiked) Color(0xFFFF5252) else OceanGradientStart, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Favorite, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "${post.likes.size}", color = TextSecondary, fontSize = 13.sp)
                }
                Text(text = "${post.commentCount} bình luận", color = TextSecondary, fontSize = 13.sp)
            }


            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModernInteractionButton(
                    icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    text = "Thích",
                    isActive = isLiked,
                    activeColor = Color(0xFFFF5252),
                    onClick = { viewModel.toggleLike(post) }
                )

                ModernInteractionButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    text = "Bình luận",
                    onClick = {
                        if (post.pId.isNotBlank()) {
                            navController.navigate("post_detail/${post.pId}")
                        }
                    }
                )


            }
        }
    }
}

@Composable
fun ModernInteractionButton(
    icon: ImageVector,
    text: String,
    isActive: Boolean = false,
    activeColor: Color = OceanGradientEnd,
    onClick: () -> Unit
) {
    val color = if (isActive) activeColor else TextSecondary
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = color,
            fontSize = 14.sp,
            fontWeight = if(isActive) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun ModernEmptyFeedMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.White, CircleShape)
                .border(2.dp, OceanSurface, CircleShape)
                .shadow(4.dp, CircleShape, spotColor = Color.LightGray.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = OceanGradientStart,
                modifier = Modifier.size(50.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Chưa có bài viết nào",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "Hãy là người đầu tiên chia sẻ!",
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}

fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    return DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.MINUTE_IN_MILLIS).toString()
}
package com.example.app_yolo11.ui.Screens.ManagePosts

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.app_yolo11.model.SocialPost

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
fun ManagePostsScreen(
    navController: NavHostController,
    viewModel: ManagePostsViewModel = hiltViewModel()
) {
    val posts by viewModel.userPosts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var postToDelete by remember { mutableStateOf<SocialPost?>(null) }

    if (postToDelete != null) {
        ModernDeleteDialog(
            onDismiss = { postToDelete = null },
            onConfirm = {
                viewModel.deletePost(postToDelete!!.pId)
                postToDelete = null
            }
        )
    }

    Scaffold(
        containerColor = OceanSurface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Bài viết của tôi",
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = OceanGradientEnd
                )
            } else if (posts.isEmpty()) {
                ModernEmptyPostState(
                    onCreateClick = { navController.navigate("create_post") }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(posts) { post ->
                        ModernManagePostItem(
                            post = post,
                            onClick = { navController.navigate("post_detail/${post.pId}") },
                            onDeleteClick = { postToDelete = post },
                            onEditClick = {
                                navController.navigate("edit_post/${post.pId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernManagePostItem(
    post: SocialPost,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color.LightGray.copy(alpha = 0.4f))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.Top
            ) {

                if (post.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(OceanSurface),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE1F5FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = OceanGradientStart.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Nội dung text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (post.content.isBlank()) "Bài viết hình ảnh" else post.content,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = DateUtils.getRelativeTimeSpanString(post.createdAt).toString(),
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stats
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.likes.size}", fontSize = 12.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(Icons.Outlined.ChatBubbleOutline, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.commentCount}", fontSize = 12.sp, color = TextSecondary)
                }

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Edit Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE3F2FD))
                            .clickable { onEditClick() }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Sửa",
                            tint = OceanGradientEnd,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFEBEE))
                            .clickable { onDeleteClick() }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ModernEmptyPostState(onCreateClick: () -> Unit) {
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
                imageVector = Icons.Default.EditNote,
                contentDescription = null,
                tint = OceanGradientStart,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Bạn chưa có bài viết nào",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Chia sẻ khoảnh khắc đầu tiên của bạn ngay!",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.shadow(8.dp, RoundedCornerShape(12.dp), spotColor = OceanGradientEnd.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .background(MainGradientBrush)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Đăng bài ngay", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun ModernDeleteDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Xóa bài viết?", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Text(
                "Hành động này không thể hoàn tác. Bạn có chắc chắn muốn tiếp tục?",
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Xóa vĩnh viễn", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy bỏ", color = TextSecondary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
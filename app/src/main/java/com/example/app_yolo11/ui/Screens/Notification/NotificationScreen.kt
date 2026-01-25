package com.example.app_yolo11.ui.Screens.Notification

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
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.app_yolo11.model.Notification
import com.example.app_yolo11.model.NotificationType

val OceanGradientStart = Color(0xFF4FC3F7)
val OceanGradientEnd = Color(0xFF006994)
val OceanSurface = Color(0xFFF0F4F8)
val TextPrimary = Color(0xFF102A43)
val TextSecondary = Color(0xFF627D98)
val UnreadBackground = Color(0xFFE1F5FE)

val MainGradientBrush = Brush.linearGradient(
    colors = listOf(OceanGradientStart, OceanGradientEnd),
    tileMode = TileMode.Clamp
)

@Composable
fun NotificationScreen(
    navController: NavHostController,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is NotificationEvent.NavigateToPost -> {
                    navController.navigate("post_detail/${event.postId}")
                }
                is NotificationEvent.ShowError -> {
                    errorDialogMessage = event.message
                }
            }
        }
    }

    if (errorDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { errorDialogMessage = null },
            title = { Text("Thông báo", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text(errorDialogMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { errorDialogMessage = null }) {
                    Text("Đóng", color = OceanGradientEnd, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        containerColor = OceanSurface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Thông báo",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                textAlign = TextAlign.Center
            )

            if (notifications.isEmpty()) {
                ModernEmptyNotificationState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(notifications) { notification ->
                        ModernNotificationItem(
                            notification = notification,
                            onClick = {
                                viewModel.handleNotificationClick(notification.id, notification.postId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernNotificationItem(notification: Notification, onClick: () -> Unit) {

    val backgroundColor = if (!notification.isRead) UnreadBackground else Color.White
    val borderWidth = if (!notification.isRead) 1.dp else 0.dp
    val borderColor = if (!notification.isRead) OceanGradientStart.copy(alpha = 0.3f) else Color.Transparent

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notification.isRead) 2.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {

            Box(modifier = Modifier.padding(end = 12.dp)) {
                if (notification.senderAvatar.isNotEmpty()) {
                    AsyncImage(
                        model = notification.senderAvatar,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape),
                        contentScale = ContentScale.Crop,
                        error = rememberVectorPainter(Icons.Default.Person)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                    }
                }

                val typeIcon = when (notification.type) {
                    NotificationType.LIKE, NotificationType.LIKE_POST, NotificationType.LIKE_COMMENT -> Icons.Default.Favorite
                    else -> Icons.Default.Comment
                }
                val badgeColor = when (notification.type) {
                    NotificationType.LIKE, NotificationType.LIKE_POST, NotificationType.LIKE_COMMENT -> Color(0xFFFF5252)
                    else -> OceanGradientEnd
                }

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 2.dp)
                        .background(Color.White, CircleShape)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }


            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                            append(notification.senderName + " ")
                        }

                        val actionText = when (notification.type) {
                            NotificationType.LIKE_POST, NotificationType.LIKE -> "đã thích bài viết của bạn."
                            NotificationType.COMMENT_POST, NotificationType.COMMENT -> "đã bình luận bài viết: "
                            NotificationType.LIKE_COMMENT -> "đã thích bình luận của bạn."
                            NotificationType.REPLY_COMMENT -> "đã trả lời bình luận của bạn: "
                        }

                        withStyle(style = SpanStyle(color = TextSecondary)) {
                            append(actionText)
                        }

                        if (notification.content.isNotEmpty() &&
                            (notification.type == NotificationType.COMMENT_POST ||
                                    notification.type == NotificationType.REPLY_COMMENT ||
                                    notification.type == NotificationType.COMMENT)) {
                            withStyle(style = SpanStyle(color = TextPrimary, fontWeight = FontWeight.Normal)) {
                                append(" \"${notification.content}\"")
                            }
                        }
                    },
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = DateUtils.getRelativeTimeSpanString(notification.timestamp).toString(),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }


            if (!notification.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(10.dp)
                        .background(OceanGradientEnd, CircleShape)
                        .shadow(4.dp, CircleShape, spotColor = OceanGradientEnd)
                )
            }
        }
    }
}


@Composable
fun ModernEmptyNotificationState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = (-40).dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(OceanSurface, CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .shadow(10.dp, CircleShape, spotColor = Color.LightGray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsNone,
                contentDescription = null,
                tint = OceanGradientStart.copy(alpha = 0.6f),
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Chưa có thông báo nào",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Khi có tương tác mới, chúng sẽ xuất hiện tại đây.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }
}
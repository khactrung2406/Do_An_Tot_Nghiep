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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.app_yolo11.model.Comment
import com.example.app_yolo11.model.SocialPost
import com.example.app_yolo11.ui.Screens.PostDetail.PostDetailViewModel
import com.google.firebase.auth.FirebaseAuth

private val DetailOceanStart = Color(0xFF4FC3F7)
private val DetailOceanEnd = Color(0xFF006994)
private val DetailSurface = Color(0xFFF0F4F8)
private val DetailTextPrimary = Color(0xFF102A43)
private val DetailTextSecondary = Color(0xFF627D98)

private val DetailGradientBrush = Brush.linearGradient(
    colors = listOf(DetailOceanStart, DetailOceanEnd),
    tileMode = TileMode.Clamp
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavHostController,
    postId: String,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val post by viewModel.post.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val parentComments = comments.filter { it.parentId.isEmpty() }
    val childCommentsMap = comments.filter { it.parentId.isNotEmpty() }.groupBy { it.parentId }

    var commentToEdit by remember { mutableStateOf<Comment?>(null) }
    val focusRequester = remember { FocusRequester() }
    var inputValue by remember { mutableStateOf(TextFieldValue("")) }
    var replyingToId by remember { mutableStateOf("") }
    var replyingToUserId by remember { mutableStateOf("") }

    if (commentToEdit != null) {
        ModernEditCommentDialog(
            initialContent = commentToEdit!!.content,
            onDismiss = { commentToEdit = null },
            onConfirm = { newContent ->
                viewModel.editComment(postId, commentToEdit!!.id, newContent)
                commentToEdit = null
            }
        )
    }

    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
        viewModel.loadComments(postId)
    }

    Scaffold(
        containerColor = DetailSurface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Chi tiết bài viết",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DetailTextPrimary
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
                                tint = DetailTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DetailSurface)
            )
        },
        bottomBar = {
            ModernCommentInputBar(
                value = inputValue,
                onValueChange = { inputValue = it },
                focusRequester = focusRequester,
                isReplying = replyingToId.isNotEmpty(),
                onCancelReply = {
                    replyingToId = ""
                    replyingToUserId = ""
                    inputValue = TextFieldValue("")
                },
                onSend = {
                    viewModel.sendComment(
                        postId = postId,
                        content = inputValue.text,
                        parentId = replyingToId,
                        replyToUserId = replyingToUserId
                    )
                    inputValue = TextFieldValue("")
                    replyingToId = ""
                    replyingToUserId = ""
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                post?.let { currentPost ->
                    ModernPostDetailContent(
                        post = currentPost,
                        commentCount = comments.size,
                        currentUserId = currentUserId,
                        onLikeClick = { viewModel.toggleLike(currentPost) },
                        onCommentClick = {
                            replyingToId = ""
                            replyingToUserId = ""
                            focusRequester.requestFocus()
                        }
                    )
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    thickness = 1.dp,
                    color = Color.LightGray.copy(alpha = 0.3f)
                )
            }

            if (comments.isEmpty()) {
                item {
                    ModernEmptyComments()
                }
            } else {
                items(parentComments) { parent ->
                    ModernCommentItem(
                        comment = parent,
                        isReply = false,
                        currentUserId = currentUserId,
                        onDelete = { viewModel.deleteComment(postId, parent.id) },
                        onEdit = { commentToEdit = parent },
                        onLike = { viewModel.toggleCommentLike(postId, parent.id, parent.userId) },
                        onReply = {
                            replyingToId = parent.id
                            replyingToUserId = parent.userId
                            val prefix = "@${parent.userName} "
                            inputValue = TextFieldValue(prefix, TextRange(prefix.length))
                            focusRequester.requestFocus()
                        }
                    )

                    val children = childCommentsMap[parent.id] ?: emptyList()
                    children.forEach { child ->
                        ModernCommentItem(
                            comment = child,
                            isReply = true,
                            currentUserId = currentUserId,
                            onDelete = { viewModel.deleteComment(postId, child.id) },
                            onEdit = { commentToEdit = child },
                            onLike = { viewModel.toggleCommentLike(postId, child.id, child.userId) },
                            onReply = {
                                replyingToId = parent.id
                                replyingToUserId = child.userId
                                val prefix = "@${child.userName} "
                                inputValue = TextFieldValue(prefix, TextRange(prefix.length))
                                focusRequester.requestFocus()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernPostDetailContent(
    post: SocialPost,
    commentCount: Int,
    currentUserId: String,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    val isLiked = post.likes.contains(currentUserId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color.LightGray.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.userAvatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DetailSurface),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Default.MoreVert)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(post.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DetailTextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            DateUtils.getRelativeTimeSpanString(post.createdAt).toString(),
                            fontSize = 12.sp,
                            color = DetailTextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Public, null, tint = DetailTextSecondary, modifier = Modifier.size(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = post.content,
                fontSize = 16.sp,
                color = DetailTextPrimary,
                lineHeight = 24.sp
            )

            if (post.imageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DetailSurface),
                    contentScale = ContentScale.FillWidth
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(if (isLiked) Color(0xFFFF5252) else DetailSurface, CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Filled.Favorite, null, tint = if(isLiked) Color.White else DetailTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "${post.likes.size}", color = DetailTextSecondary, fontWeight = FontWeight.SemiBold)
                }

                Text(text = "$commentCount bình luận", color = DetailTextSecondary, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModernDetailActionButton(
                    icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    text = "Thích",
                    isActive = isLiked,
                    activeColor = Color(0xFFFF5252),
                    onClick = onLikeClick
                )
                ModernDetailActionButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    text = "Bình luận",
                    onClick = onCommentClick
                )
            }
        }
    }
}

@Composable
fun ModernCommentInputBar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    focusRequester: FocusRequester,
    isReplying: Boolean,
    onCancelReply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column {
            if (isReplying) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DetailSurface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Đang trả lời bình luận...", fontSize = 12.sp, color = DetailTextSecondary)
                    Text(
                        "Hủy",
                        fontSize = 12.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onCancelReply() }
                    )
                }
            }

            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text("Viết bình luận...", color = Color.LightGray) },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .heightIn(min = 50.dp, max = 100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(DetailSurface),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DetailSurface,
                        unfocusedContainerColor = DetailSurface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = DetailOceanEnd
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (value.text.isNotBlank()) onSend() })
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (value.text.isNotBlank()) DetailGradientBrush
                            else Brush.linearGradient(listOf(Color.LightGray, Color.LightGray))
                        )
                        .clickable(enabled = value.text.isNotBlank()) { onSend() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp).offset(x = (-2).dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernCommentItem(
    comment: Comment,
    isReply: Boolean = false,
    currentUserId: String,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onLike: () -> Unit,
    onReply: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isLiked = comment.likes.contains(currentUserId)

    val startPadding = if (isReply) 56.dp else 16.dp
    val avatarSize = if (isReply) 32.dp else 40.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = startPadding,
                end = 16.dp,
                top = 4.dp,
                bottom = 4.dp
            ),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = comment.userAvatar,
            contentDescription = null,
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(DetailSurface),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                    .background(Color.White)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = comment.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DetailTextPrimary
                        )

                        if (comment.userId == currentUserId) {
                            Box {
                                Icon(
                                    Icons.Default.MoreVert,
                                    null,
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { showMenu = true }
                                )
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    containerColor = Color.White
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Sửa") },
                                        onClick = { showMenu = false; onEdit() },
                                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Xóa", color = Color.Red) },
                                        onClick = { showMenu = false; onDelete() },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = comment.content,
                        fontSize = 14.sp,
                        color = DetailTextPrimary,
                    )
                }

                if (comment.likes.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 8.dp, y = 8.dp)
                            .shadow(2.dp, CircleShape)
                            .background(Color.White, CircleShape)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Favorite, null, tint = Color(0xFFFF5252), modifier = Modifier.size(10.dp))
                            if (comment.likes.size > 1) {
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = "${comment.likes.size}", fontSize = 10.sp, color = DetailTextSecondary)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(start = 4.dp, top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = DateUtils.getRelativeTimeSpanString(comment.timestamp).toString(),
                    fontSize = 11.sp,
                    color = DetailTextSecondary
                )
                Text(
                    text = "Thích",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isLiked) Color(0xFFFF5252) else DetailTextSecondary,
                    modifier = Modifier.clickable { onLike() }
                )
                Text(
                    text = "Trả lời",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DetailTextSecondary,
                    modifier = Modifier.clickable { onReply() }
                )
            }
        }
    }
}

@Composable
fun ModernDetailActionButton(
    icon: ImageVector,
    text: String,
    isActive: Boolean = false,
    activeColor: Color = DetailOceanEnd,
    onClick: () -> Unit
) {
    val contentColor = if (isActive) activeColor else DetailTextSecondary
    val bgColor = if(isActive) activeColor.copy(alpha = 0.1f) else Color.Transparent

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = contentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ModernEmptyComments() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.ChatBubbleOutline,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Chưa có bình luận nào", color = DetailTextSecondary)
        Text("Hãy là người đầu tiên chia sẻ ý kiến!", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun ModernEditCommentDialog(initialContent: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initialContent) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa bình luận", fontWeight = FontWeight.Bold, color = DetailTextPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DetailOceanEnd,
                    cursorColor = DetailOceanEnd
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = DetailOceanEnd)
            ) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy", color = DetailTextSecondary) }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

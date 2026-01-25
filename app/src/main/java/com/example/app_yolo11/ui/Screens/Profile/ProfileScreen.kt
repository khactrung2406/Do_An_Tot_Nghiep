package com.example.app_yolo11.ui.Screens.Profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.app_yolo11.ui.Screens.ChangePass.ChangePasswordDialog

val OceanGradientStart = Color(0xFF4FC3F7)
val OceanGradientEnd = Color(0xFF006994)
val OceanSurface = Color(0xFFF0F4F8)
val TextPrimary = Color(0xFF102A43)
val TextSecondary = Color(0xFF627D98)

val MainGradientBrush = Brush.linearGradient(
    colors = listOf(OceanGradientStart, OceanGradientEnd),
    tileMode = TileMode.Clamp
)

@Composable
fun ProfileScreen(navController: NavHostController, viewModel: ProfileViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfileImage(uri)
        }
    }

    LaunchedEffect(uiState.isUpdateSuccessful) {
        if (uiState.isUpdateSuccessful) {
            viewModel.clearUpdateStatus()
        }
    }

    Scaffold(
        containerColor = OceanSurface,
    ) { paddingValues ->

        if (uiState.showRenameDialog) {
            ModernRenameDialog(
                currentName = uiState.fullName,
                onConfirm = { newName -> viewModel.updateUserName(newName) },
                onDismiss = { viewModel.dismissRenameDialog() },
                isUpdating = uiState.isUpdatingName
            )
        }

        if (uiState.showChangePasswordDialog) {
            ChangePasswordDialog(
                onDismiss = { viewModel.dismissChangePasswordDialog() },
                onConfirm = { current, new, confirm ->
                    viewModel.changePassword(current, new, confirm)
                },
                isLoading = uiState.isChangingPassword,
                errorMessage = uiState.changePasswordError
            )
        }

        if (uiState.isChangePasswordSuccess) {
            ModernSuccessDialog(onDismiss = { viewModel.dismissSuccessDialog() })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                        .background(MainGradientBrush)
                ) {
                    Box(modifier = Modifier.size(100.dp).offset(x = (-20).dp, y = (-20).dp).background(Color.White.copy(alpha = 0.1f), CircleShape))
                    Box(modifier = Modifier.size(80.dp).align(Alignment.BottomEnd).offset(x = 20.dp, y = (-40).dp).background(Color.White.copy(alpha = 0.1f), CircleShape))

                    Box(
                        modifier = Modifier
                            .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }

                        Text(
                            text = "Hồ sơ cá nhân",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(top = 130.dp)
                        .size(140.dp)
                ) {
                    AsyncImage(
                        model = if (uiState.avatarUrl.isNotEmpty()) uiState.avatarUrl else null,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                            .shadow(10.dp, CircleShape),
                        error = rememberVectorPainter(Icons.Default.Person),
                        placeholder = rememberVectorPainter(Icons.Default.Person)
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(36.dp)
                            .shadow(4.dp, CircleShape)
                            .background(Color.White, CircleShape)
                            .border(1.dp, OceanSurface, CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Avatar",
                            tint = OceanGradientEnd,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = OceanGradientEnd)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (uiState.isLoading) "Đang tải..." else uiState.fullName,
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.showRenameDialog() }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Name",
                        tint = OceanGradientEnd,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = uiState.email,
                color = TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {

                    ModernProfileItem(
                        icon = Icons.Default.LockReset,
                        text = "Đổi mật khẩu",
                        iconBgColor = Color(0xFFE3F2FD),
                        iconTint = Color(0xFF1976D2),
                        onClick = { viewModel.showChangePasswordDialog() }
                    )

                    HorizontalDivider(
                        color = OceanSurface,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        thickness = 1.dp
                    )

                    ModernProfileItem(
                        icon = Icons.Default.PersonRemove,
                        text = "Xóa tài khoản",
                        iconBgColor = Color(0xFFFFEBEE),
                        iconTint = Color(0xFFD32F2F),
                        isDestructive = true,
                        showChevron = false,
                        onClick = {  }
                    )
                }
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

@Composable
fun ModernProfileItem(
    icon: ImageVector,
    text: String,
    iconBgColor: Color,
    iconTint: Color,
    isDestructive: Boolean = false,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            color = if (isDestructive) Color(0xFFD32F2F) else TextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ModernRenameDialog(
    currentName: String,
    onConfirm: (newName: String) -> Unit,
    onDismiss: () -> Unit,
    isUpdating: Boolean
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Đổi tên hiển thị",
                fontWeight = FontWeight.Bold,
                color = OceanGradientEnd,
                fontSize = 20.sp
            )
        },
        text = {
            Column {
                Text("Nhập tên mới của bạn bên dưới:", fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text("Tên của bạn") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OceanGradientEnd,
                        focusedLabelColor = OceanGradientEnd,
                        cursorColor = OceanGradientEnd,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                if (isUpdating) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = OceanGradientEnd,
                        trackColor = OceanSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newName) },
                enabled = newName.isNotBlank() && !isUpdating,
                colors = ButtonDefaults.buttonColors(containerColor = OceanGradientEnd),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Lưu thay đổi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUpdating) {
                Text("Hủy", color = TextSecondary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ModernSuccessDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier.size(60.dp).background(Color(0xFFE8F5E9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
            }
        },
        title = {
            Text("Thành công!", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Text("Mật khẩu của bạn đã được cập nhật.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OceanGradientEnd),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Đóng")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}
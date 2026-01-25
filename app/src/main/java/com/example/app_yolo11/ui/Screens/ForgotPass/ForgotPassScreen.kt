package com.example.app_yolo11.ui.Screens.ForgotPass

import AppHeaderLogo // Đảm bảo đã có file này
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

val OceanBlueDark = Color(0xFF006994)
val OceanBlueLight = Color(0xFF4FC3F7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPassScreen(navController: NavHostController, viewModel: ForgotPassViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val showDialog = uiState.errorMessage != null || uiState.successMessage != null
    if (showDialog) {
        val isSuccess = uiState.successMessage != null
        val titleText = if (isSuccess) "Đã gửi yêu cầu!" else "Rất tiếc..."
        val messageText = uiState.successMessage ?: uiState.errorMessage ?: ""
        val titleColor = if (isSuccess) OceanBlueDark else Color.Red

        AlertDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = { Text(text = titleText, fontWeight = FontWeight.Bold, color = titleColor) },
            text = { Text(text = messageText) },
            confirmButton = {
                Button(
                    onClick = {
                        if (isSuccess) {
                            viewModel.resetState()
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            viewModel.clearMessages()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = titleColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Xác nhận")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(OceanBlueLight, OceanBlueDark)
                )
            )
    ) {

        Box(modifier = Modifier.size(200.dp).offset(x = (-60).dp, y = (-40).dp).background(Color.White.copy(alpha = 0.1f), CircleShape))
        Box(modifier = Modifier.size(120.dp).align(Alignment.BottomEnd).offset(x = 30.dp, y = 30.dp).background(Color.White.copy(alpha = 0.1f), CircleShape))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                AppHeaderLogo()
            }


            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp), // Bo góc lớn mềm mại
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Quên Mật Khẩu?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlueDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Hãy nhập email của bạn.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        label = { Text("Email đăng ký") },
                        placeholder = { Text("example@email.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = OceanBlueDark) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OceanBlueDark,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = OceanBlueDark,
                            cursorColor = OceanBlueDark
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = { viewModel.sendResetPassword() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(OceanBlueLight, OceanBlueDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = "GỬI YÊU CẦU",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Nút Quay lại
                    TextButton(
                        onClick = { navController.navigate("login") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Quay lại đăng nhập",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
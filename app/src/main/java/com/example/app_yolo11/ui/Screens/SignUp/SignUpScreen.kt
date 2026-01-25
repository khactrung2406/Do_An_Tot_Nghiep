package com.example.app_yolo11.ui.Screens.SignUp

import AppHeaderLogo // Đảm bảo bạn đã có Logo mới ở file trước
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

val OceanBlueDark = Color(0xFF006994)
val OceanBlueLight = Color(0xFF4FC3F7)

@Composable
fun SignUpScreen(navController: NavHostController, viewModel: SignupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    if (uiState.isSuccess) {
        SuccessDialog(
            onConfirm = {
                navController.navigate("login") {
                    popUpTo("signup") { inclusive = true }
                }

            }
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
        // Bong bóng trang trí background
        Box(modifier = Modifier.size(250.dp).offset(x = (-80).dp, y = (-80).dp).background(Color.White.copy(alpha = 0.1f), CircleShape))
        Box(modifier = Modifier.size(150.dp).align(Alignment.BottomEnd).offset(x = 40.dp, y = 40.dp).background(Color.White.copy(alpha = 0.1f), CircleShape))

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
                modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
            ) {
                AppHeaderLogo()
            }


            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
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
                        text = "Đăng Ký",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlueDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Tạo tài khoản mới",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )


                    OutlinedTextField(
                        value = uiState.fullName,
                        onValueChange = { viewModel.onFullNameChange(it) },
                        label = { Text("Họ và tên") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = OceanBlueDark) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = inputColors()
                    )


                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = OceanBlueDark) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = inputColors()
                    )


                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        label = { Text("Mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = OceanBlueDark) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password", tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = inputColors()
                    )


                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = { viewModel.onConfirmPasswordChange(it) },
                        label = { Text("Xác nhận mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = OceanBlueDark) },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password", tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = inputColors()
                    )


                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = Color.Red,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start)
                        )
                    }


                    Button(
                        onClick = { viewModel.onSignUpClick() },
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
                                    text = "ĐĂNG KÝ NGAY",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Bạn đã có tài khoản?", color = Color.Gray, fontSize = 14.sp)
                        TextButton(onClick = { navController.navigate("login") }) {
                            Text(
                                text = "Đăng nhập",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlueDark
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

@Composable
fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = OceanBlueDark,
    unfocusedBorderColor = Color.LightGray,
    focusedLabelColor = OceanBlueDark,
    cursorColor = OceanBlueDark
)

@Composable
fun SuccessDialog(onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(text = "Chào mừng thành viên mới!", fontWeight = FontWeight.Bold, color = OceanBlueDark)
        },
        text = {
            Text("Tài khoản của bạn đã được tạo thành công. Hãy đăng nhập để bắt đầu hành trình khám phá!")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlueDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Đăng nhập ngay")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
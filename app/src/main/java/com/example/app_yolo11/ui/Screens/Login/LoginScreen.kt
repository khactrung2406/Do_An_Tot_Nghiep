package com.example.app_yolo11.ui.Screens.Login

import AppHeaderLogo
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.app_yolo11.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException


val OceanBlueDark = Color(0xFF006994)
val OceanBlueLight = Color(0xFF4FC3F7)
val SandWhite = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("335293380764-66vndfcc3fj18bmj3iqu85unv7at17ts.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { viewModel.onGoogleSignIn(it) }
                    ?: Toast.makeText(context, "Lỗi: Không nhận được ID Token.", Toast.LENGTH_LONG).show()
            } catch (e: ApiException) {
                Toast.makeText(context, "Đăng nhập Google thất bại: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        }
    }

    if (uiState.isSuccess) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Chào mừng trở lại!", color = OceanBlueDark, fontWeight = FontWeight.Bold) },
            text = { Text("Đăng nhập thành công.") },
            confirmButton = {
                Button(
                    onClick = {
                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                        viewModel.resetState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlueDark)
                ) { Text("Vào trang chủ") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Có lỗi xảy ra!", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text(uiState.errorMessage ?: "Vui lòng thử lại.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("OK!", color = Color.Red) }
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
        Box(modifier = Modifier.size(200.dp).offset(x = (-50).dp, y = (-50).dp).background(Color.White.copy(alpha = 0.1f), CircleShape))
        Box(modifier = Modifier.size(100.dp).align(Alignment.BottomEnd).offset(x = 20.dp, y = 20.dp).background(Color.White.copy(alpha = 0.1f), CircleShape))

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
                modifier = Modifier.padding(bottom = 40.dp)
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
                        text = "Đăng Nhập",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlueDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Tiếp tục hành trình khám phá",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = OceanBlueDark) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp), // Bo góc Input
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OceanBlueDark,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = OceanBlueDark
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        label = { Text("Mật Khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = OceanBlueDark) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password",
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OceanBlueDark,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = OceanBlueDark
                        ),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { navController.navigate("forgot_password") }) {
                            Text(text = "Quên mật khẩu?", fontSize = 14.sp, color = OceanBlueDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    Button(
                        onClick = { viewModel.onLoginClick() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp)), // Clip để gradient không bị tràn
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
                                    text = "ĐĂNG NHẬP",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    // Hoặc đăng nhập bằng
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.LightGray)
                        Text(
                            text = "Hoặc",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.LightGray)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Google Button
                    Surface(
                        onClick = { launcher.launch(googleSignInClient.signInIntent) },
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                        color = Color.White
                    ) {
                        Box(modifier = Modifier.padding(10.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.gmail),
                                contentDescription = "Google Login",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))


                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Bạn chưa có tài khoản?", color = Color.Gray, fontSize = 14.sp)
                        TextButton(onClick = { navController.navigate("signup") }) {
                            Text(
                                text = "Đăng ký ngay",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlueDark
                            )
                        }
                    }
                }
            }
        }
    }
}
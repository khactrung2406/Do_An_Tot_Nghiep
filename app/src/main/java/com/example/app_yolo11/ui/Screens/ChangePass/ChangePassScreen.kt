package com.example.app_yolo11.ui.Screens.ChangePass

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val OceanBlueDark = Color(0xFF006994)

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = "Đổi mật khẩu",
                color = OceanBlueDark,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                PasswordTextField(
                    value = currentPass,
                    onValueChange = { currentPass = it },
                    label = "Mật khẩu hiện tại",
                    imeAction = ImeAction.Next
                )


                PasswordTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = "Mật khẩu mới",
                    imeAction = ImeAction.Next
                )

                PasswordTextField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it },
                    label = "Xác nhận mật khẩu",
                    imeAction = ImeAction.Done
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(currentPass, newPass, confirmPass) },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlueDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Lưu thay đổi")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Hủy", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction
) {
    var isVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = OceanBlueDark
            )
        },
        trailingIcon = {
            IconButton(onClick = { isVisible = !isVisible }) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle visibility",
                    tint = Color.Gray
                )
            }
        },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OceanBlueDark,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = OceanBlueDark,
            cursorColor = OceanBlueDark
        )
    )
}
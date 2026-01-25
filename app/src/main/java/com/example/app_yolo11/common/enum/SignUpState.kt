package com.example.app_yolo11.common.enum

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

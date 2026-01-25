package com.example.app_yolo11.common.enum

sealed class AuthState {
    object Authenticated : AuthState()
    object UnAuthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}

data class ForgotPassUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
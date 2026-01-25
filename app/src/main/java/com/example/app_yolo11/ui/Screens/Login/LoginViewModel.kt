package com.example.app_yolo11.ui.Screens.Login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.common.enum.LoginUiState
import com.example.app_yolo11.repositories.Auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: Auth
): ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    fun onEmailChange(newValue: String) {
        _uiState.update { it.copy(email = newValue, errorMessage = null) }
    }

    fun onPasswordChange(newValue: String) {
        _uiState.update { it.copy(password = newValue, errorMessage = null) }
    }

    fun onLoginClick() {
        val currentState = _uiState.value
        val email = currentState.email.trim()
        val password = currentState.password.trim()

        if (email.isBlank() || password.isBlank()) {

            _uiState.update { it.copy(errorMessage = "Vui lòng nhập đầy đủ Email và Mật khẩu.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authRepository.login(email = email, password = password)

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { error ->

                    _uiState.update {
                        it.copy(
                            isLoading = false,

                            errorMessage = error.message
                        )
                    }
                }
            )
        }
    }

    fun onGoogleSignIn(idToken: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { error ->

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Đăng nhập bằng Google thất bại. Vui lòng thử lại."
                        )
                    }
                }
            )
        }
    }

    fun resetState() {
        _uiState.update { LoginUiState() }
    }


    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
package com.example.app_yolo11.ui.Screens.SignUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.common.enum.SignUpUiState
import com.example.app_yolo11.repositories.Auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class SignupViewModel @Inject constructor(private val authRepository: Auth) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()


    fun onEmailChange(newValue: String) {
        _uiState.update { it.copy(email = newValue, errorMessage = null) }
    }

    fun onPasswordChange(newValue: String) {
        _uiState.update { it.copy(password = newValue, errorMessage = null) }
    }

    fun onFullNameChange(newValue: String) {
        _uiState.update { it.copy(fullName = newValue, errorMessage = null) }
    }

    fun onConfirmPasswordChange(newValue: String) {
        _uiState.update { it.copy(confirmPassword = newValue, errorMessage = null) }
    }


    fun onSignUpClick() {
        val currentState = _uiState.value


        val trimmedEmail = currentState.email.trim()
        val trimmedFullName = currentState.fullName.trim()
        val password = currentState.password
        val confirmPassword = currentState.confirmPassword


        if (trimmedEmail.isBlank() || password.isBlank() || trimmedFullName.isBlank() || confirmPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng điền đầy đủ thông tin.", isLoading = false) }
            return
        }


        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Mật khẩu xác nhận không khớp.", isLoading = false) }
            return
        }


        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Mật khẩu phải có ít nhất 6 ký tự.", isLoading = false) }
            return
        }


        _uiState.update { it.copy(isLoading = true, errorMessage = null) }


        viewModelScope.launch {

            val result = authRepository.signUp(
                email = trimmedEmail,
                password = password,
                fullName = trimmedFullName,
                confirmPassword = currentState.confirmPassword
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(isLoading = false, isSuccess = true)
                    }
                },
                onFailure = { error ->

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Đăng ký thất bại không xác định."
                        )
                    }
                }
            )
        }
    }
}
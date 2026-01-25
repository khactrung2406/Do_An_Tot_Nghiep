package com.example.app_yolo11.ui.Screens.ForgotPass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.common.enum.ForgotPassUiState
import com.example.app_yolo11.repositories.Auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject




@HiltViewModel

class ForgotPassViewModel @Inject constructor(
    private val authRepository: Auth
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPassUiState())
    val uiState: StateFlow<ForgotPassUiState> = _uiState.asStateFlow()

    fun onEmailChange(newValue: String) {
        _uiState.update {
            it.copy(email = newValue, errorMessage = null, successMessage = null)
        }
    }

    fun sendResetPassword() {
        val currentEmail = _uiState.value.email.trim()

        if (currentEmail.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập email!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }


            val isEmailExist = authRepository.checkEmailExists(currentEmail)

            if (!isEmailExist) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Email này chưa được đăng ký trong hệ thống!"
                    )
                }
                return@launch
            }

            // 2. Gửi mail
            val result = authRepository.sendNewPassword(currentEmail)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Đã gửi link xác nhận thành công. Vui lòng kiểm tra hộp thư."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Lỗi gửi mail thất bại"
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }


    fun resetState() {
        _uiState.update { ForgotPassUiState() }
    }
}
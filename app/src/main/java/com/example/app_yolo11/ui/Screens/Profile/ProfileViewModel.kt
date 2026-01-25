package com.example.app_yolo11.ui.Screens.Profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.repositories.Auth
import com.example.app_yolo11.repositories.CloudinaryRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val fullName: String = "Đang tải...",
    val email: String = "",
    val avatarUrl: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showRenameDialog: Boolean = false,
    val isUpdatingName: Boolean = false,
    val isUpdateSuccessful: Boolean = false,
    val showChangePasswordDialog: Boolean = false,
    val isChangingPassword: Boolean = false,
    val changePasswordError: String? = null,
    val isChangePasswordSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: Auth,
    private val firebaseAuth: FirebaseAuth,
    private val cloudinaryRepository: CloudinaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            _uiState.update { it.copy(isLoading = true) }

            viewModelScope.launch {
                val user = authRepository.getUserInfo(uid)

                if (user != null) {
                    _uiState.update {
                        it.copy(
                            fullName = user.fullName,
                            email = user.email,
                            avatarUrl = user.avatar,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Không tìm thấy thông tin người dùng"
                        )
                    }
                }
            }
        }
    }

    fun refreshProfile() {
        loadUserProfile()
    }

    fun updateProfileImage(uri: Uri) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val uploadResult = cloudinaryRepository.uploadImage(uri)

            uploadResult.fold(
                onSuccess = { url ->
                    val dbResult = authRepository.updateAvatar(url)

                    dbResult.fold(
                        onSuccess = {
                            _uiState.update {
                                it.copy(isLoading = false, avatarUrl = url)
                            }
                        },
                        onFailure = { error ->
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = "Lỗi lưu DB: ${error.message}")
                            }
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Lỗi upload ảnh: ${error.message}")
                    }
                }
            )
        }
    }

    fun showRenameDialog() {
        _uiState.update { it.copy(showRenameDialog = true) }
    }

    fun dismissRenameDialog() {
        _uiState.update { it.copy(showRenameDialog = false) }
    }

    fun updateUserName(newName: String) {
        if (newName.isBlank() || newName == uiState.value.fullName) {
            dismissRenameDialog()
            return
        }

        _uiState.update { it.copy(isUpdatingName = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authRepository.updatefullName(newName)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            fullName = newName,
                            isUpdatingName = false,
                            showRenameDialog = false,
                            isUpdateSuccessful = true
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isUpdatingName = false,
                            errorMessage = "Lỗi cập nhật tên: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun clearUpdateStatus() {
        _uiState.update { it.copy(isUpdateSuccessful = false, errorMessage = null) }
    }

    fun showChangePasswordDialog() {
        _uiState.update { it.copy(showChangePasswordDialog = true, changePasswordError = null) }
    }

    fun dismissChangePasswordDialog() {
        _uiState.update { it.copy(showChangePasswordDialog = false, changePasswordError = null) }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(isChangePasswordSuccess = false) }
    }

    fun changePassword(currentPass: String, newPass: String, confirmPass: String) {
        if (currentPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
            _uiState.update { it.copy(changePasswordError = "Vui lòng điền đầy đủ thông tin.") }
            return
        }

        if (newPass != confirmPass) {
            _uiState.update { it.copy(changePasswordError = "Mật khẩu mới và xác nhận không khớp.") }
            return
        }

        if (newPass.length < 6) {
            _uiState.update { it.copy(changePasswordError = "Mật khẩu mới phải có ít nhất 6 ký tự.") }
            return
        }

        _uiState.update { it.copy(isChangingPassword = true, changePasswordError = null) }

        viewModelScope.launch {
            val result = authRepository.changePassword(currentPass, newPass)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            showChangePasswordDialog = false,
                            changePasswordError = null,
                            isChangePasswordSuccess = true
                        )
                    }
                },
                onFailure = { error ->
                    val errorMessage = when (error) {
                        is FirebaseAuthInvalidCredentialsException -> "Mật khẩu hiện tại không đúng."
                        is FirebaseNetworkException -> "Không có kết nối mạng. Vui lòng kiểm tra lại."
                        is FirebaseAuthRecentLoginRequiredException -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại."
                        else -> {
                            val msg = error.message ?: ""
                            if (msg.contains("incorrect", ignoreCase = true) ||
                                msg.contains("credential", ignoreCase = true)
                            ) {
                                "Mật khẩu hiện tại không đúng."
                            } else {
                                "Đã xảy ra lỗi: $msg"
                            }
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            changePasswordError = errorMessage
                        )
                    }
                }
            )
        }
    }
}
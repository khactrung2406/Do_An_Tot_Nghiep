package com.example.app_yolo11.ui.Screens.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.repositories.Auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val welcomeMessage: String = "Xin chào,",
    val userName: String = "Bạn",
    val avatar: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: Auth
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    init {
        loadUserData()
        observeUserChanges()
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    fun signout() {
        authRepository.signout()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val nameResult = authRepository.getCurrentUserName()
            val photoUrlResult = authRepository.getUserAvatarUrl()

            nameResult.fold(
                onSuccess = { name ->
                    _uiState.update {
                        it.copy(
                            userName = name,
                            avatar = photoUrlResult.getOrNull(),
                            isLoading = false
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(userName = "Bạn", isLoading = false) }
                }
            )
        }
    }

    private fun observeUserChanges() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            userName = user.fullName,
                            avatar = user.avatar,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}
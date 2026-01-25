package com.example.app_yolo11.ui.Screens.ChatBot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.repositories.Auth
import com.example.app_yolo11.model.ChatMessage
import com.example.app_yolo11.repositories.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatBotViewModel @Inject constructor(
    private val authRepository: Auth,
    private val chatRepository: ChatRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private var currentUid: String? = null

    init {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            currentUid = currentUser.uid
            loadHistory(currentUser.uid)
        } else {
            _messages.value = listOf(ChatMessage(text = "Vui lòng đăng nhập để xem lịch sử chat.", isFromUser = false))
        }

        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    if (currentUid != user.uid) {
                        currentUid = user.uid
                        loadHistory(user.uid)
                    }
                } else {
                    if (firebaseAuth.currentUser == null) {
                        currentUid = null
                        _messages.value = listOf(ChatMessage(text = "Vui lòng đăng nhập để xem lịch sử chat.", isFromUser = false))
                    }
                }
            }
        }
    }

    private fun loadHistory(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = chatRepository.getChatHistory(uid)
            withContext(Dispatchers.Main) {
                result.onSuccess { history ->
                    if (history.isNotEmpty()) {
                        _messages.value = history
                    } else {
                        _messages.value = listOf(ChatMessage(text = "Xin chào! Mình là chuyên gia về ốc biển. Bạn cần giúp gì không?", isFromUser = false))
                    }
                }.onFailure { exception ->
                    val errorMsg = "Lỗi tải lịch sử: ${exception.message}"
                    _messages.value = listOf(ChatMessage(text = errorMsg, isFromUser = false, isError = true))
                }
            }
        }
    }

    fun sendMessage(userText: String) {
        val uid = currentUid ?: firebaseAuth.currentUser?.uid

        if (uid == null) {
            _messages.value = _messages.value + ChatMessage(text = "Bạn cần đăng nhập để chat!", isFromUser = false, isError = true)
            return
        }

        if (userText.isBlank()) return

        val userMsg = ChatMessage(text = userText, isFromUser = true)
        val loadingMsg = ChatMessage(text = "...", isFromUser = false, isLoading = true)

        _messages.value = _messages.value + userMsg + loadingMsg
        _isGenerating.value = true

        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.saveMessage(uid, userMsg)

            val botReply = callN8nWebhook(userText, sessionId = uid)
            val isError = botReply.startsWith("Lỗi") || botReply.startsWith("Xin lỗi")
            val botMsg = ChatMessage(text = botReply, isFromUser = false, isError = isError)

            chatRepository.saveMessage(uid, botMsg)

            withContext(Dispatchers.Main) {
                val currentList = _messages.value.toMutableList()
                if (currentList.isNotEmpty() && currentList.last().isLoading) {
                    currentList.removeAt(currentList.lastIndex)
                }
                currentList.add(botMsg)
                _messages.value = currentList
                _isGenerating.value = false
            }
        }
    }
}
package com.example.app_yolo11.ui.Screens.Notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NotificationEvent {
    data class NavigateToPost(val postId: String) : NotificationEvent()
    data class ShowError(val message: String) : NotificationEvent()
}

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _eventFlow = MutableSharedFlow<NotificationEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        listenToNotifications()
    }

    private fun listenToNotifications() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    _notifications.value = snapshot.toObjects(Notification::class.java)
                }
            }
    }

    fun handleNotificationClick(notificationId: String, postId: String) {
        markAsRead(notificationId)

        if (postId.isBlank()) return

        viewModelScope.launch {
            firestore.collection("posts").document(postId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        viewModelScope.launch {
                            _eventFlow.emit(NotificationEvent.NavigateToPost(postId))
                        }
                    } else {
                        viewModelScope.launch {
                            _eventFlow.emit(NotificationEvent.ShowError("Bài viết này không còn tồn tại hoặc đã bị xóa."))
                        }
                        deleteNotification(notificationId)
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _eventFlow.emit(NotificationEvent.ShowError("Lỗi kết nối. Vui lòng thử lại."))
                    }
                }
        }
    }

    private fun markAsRead(notificationId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("notifications").document(notificationId)
            .update("isRead", true)
    }

    private fun deleteNotification(notificationId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("notifications").document(notificationId)
            .delete()
    }
}
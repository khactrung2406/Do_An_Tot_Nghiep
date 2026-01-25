package com.example.app_yolo11.ui.Screens.Community

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.model.Notification
import com.example.app_yolo11.model.NotificationType // [1] Import Enum này
import com.example.app_yolo11.model.SocialPost
import com.example.app_yolo11.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth

) : ViewModel() {
    private val _posts = MutableStateFlow<List<SocialPost>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentUserInfo = MutableStateFlow<Pair<String, String>?>(null)
    val currentUserInfo = _currentUserInfo.asStateFlow()

    init {
        fetchCurrentUserFromFirestore()
        fetchPosts()
    }

    private fun fetchCurrentUserFromFirestore() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(Users::class.java)
                        val name = user?.fullName ?: "Bạn"
                        val avatar = user?.avatar ?: ""
                        _currentUserInfo.value = Pair(name, avatar)
                    }
                }
        }
    }

    private fun fetchPosts() {
        _isLoading.value = true
        firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val postList = snapshot.toObjects(SocialPost::class.java)
                    _posts.value = postList
                    _isLoading.value = false
                }
            }
    }


    fun toggleLike(post: SocialPost) {
        if (post.pId.isBlank()) {
            Log.e("CommunityViewModel", "Lỗi: ID bài viết (pid) bị rỗng.")
            return
        }

        val currentUserId = auth.currentUser?.uid ?: return
        val postRef = firestore.collection("posts").document(post.pId)
        val isLiked = post.likes.contains(currentUserId)

        if (isLiked) {
            postRef.update("likes", FieldValue.arrayRemove(currentUserId))
        } else {
            postRef.update("likes", FieldValue.arrayUnion(currentUserId))

            if (post.userId != currentUserId) {
                sendNotification(
                    recipientId = post.userId,
                    postId = post.pId,
                    type = NotificationType.LIKE_POST,
                    message = ""
                )
            }
        }
    }

    private fun sendNotification(recipientId: String, postId: String, type: NotificationType, message: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        val senderName = _currentUserInfo.value?.first ?: "Ai đó"
        val senderAvatar = _currentUserInfo.value?.second ?: ""

        val notifRef = firestore.collection("users").document(recipientId)
            .collection("notifications").document()

        val notification = Notification(
            id = notifRef.id,
            recipientId = recipientId,
            postId = postId,
            senderId = currentUserId,
            senderName = senderName,
            senderAvatar = senderAvatar,
            content = message,
            type = type,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        notifRef.set(notification)
    }
}
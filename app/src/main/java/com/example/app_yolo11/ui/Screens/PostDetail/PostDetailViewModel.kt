package com.example.app_yolo11.ui.Screens.PostDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.model.Comment
import com.example.app_yolo11.model.Notification
import com.example.app_yolo11.model.NotificationType
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
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _post = MutableStateFlow<SocialPost?>(null)
    val post = _post.asStateFlow()

    fun loadPost(postId: String) {
        firestore.collection("posts").document(postId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    _post.value = snapshot.toObject(SocialPost::class.java)
                }
            }
    }

    fun loadComments(postId: String) {
        firestore.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _comments.value = snapshot.toObjects(Comment::class.java)
                }
            }
    }

    // --- 1. LOGIC THÍCH BÀI VIẾT ---
    fun toggleLike(post: SocialPost) {
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
                    content = "",
                    type = NotificationType.LIKE_POST
                )
            }
        }
    }

    // --- 2. LOGIC THÍCH BÌNH LUẬN ---
    fun toggleCommentLike(postId: String, commentId: String, commentOwnerId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val commentRef = firestore.collection("posts").document(postId)
            .collection("comments").document(commentId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(commentRef)
            val likes = snapshot.get("likes") as? List<String> ?: emptyList()

            if (likes.contains(currentUserId)) {
                transaction.update(commentRef, "likes", FieldValue.arrayRemove(currentUserId))
                false
            } else {
                transaction.update(commentRef, "likes", FieldValue.arrayUnion(currentUserId))
                true
            }
        }.addOnSuccessListener { isLiked ->
            if (isLiked && commentOwnerId != currentUserId) {
                sendNotification(
                    recipientId = commentOwnerId,
                    postId = postId,
                    content = "",
                    type = NotificationType.LIKE_COMMENT
                )
            }
        }.addOnFailureListener { e -> e.printStackTrace() }
    }

    // --- 3. LOGIC GỬI BÌNH LUẬN / TRẢ LỜI ---
    // Đã thêm tham số replyToUserId để biết chính xác trả lời ai
    fun sendComment(postId: String, content: String, parentId: String = "", replyToUserId: String = "") {
        if (content.isBlank()) return
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users").document(currentUserId).get().await()
                val user = userDoc.toObject(Users::class.java) ?: return@launch

                val commentRef = firestore.collection("posts").document(postId).collection("comments").document()

                val newComment = Comment(
                    id = commentRef.id,
                    parentId = parentId,
                    userId = user.uid,
                    userName = user.fullName,
                    userAvatar = user.avatar,
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    likes = emptyList()
                )

                commentRef.set(newComment)
                firestore.collection("posts").document(postId).update("commentCount", FieldValue.increment(1))

                // XỬ LÝ THÔNG BÁO
                if (parentId.isNotEmpty()) {
                    // TRƯỜNG HỢP: TRẢ LỜI BÌNH LUẬN (REPLY)
                    // Ưu tiên dùng replyToUserId (người mình click trả lời), nếu không có thì tìm chủ comment gốc
                    var targetId = replyToUserId

                    if (targetId.isEmpty()) {
                        val parentCommentSnapshot = firestore.collection("posts").document(postId)
                            .collection("comments").document(parentId).get().await()
                        targetId = parentCommentSnapshot.getString("userId") ?: ""
                    }

                    if (targetId.isNotEmpty() && targetId != currentUserId) {
                        sendNotification(
                            recipientId = targetId,
                            postId = postId,
                            content = content,
                            type = NotificationType.REPLY_COMMENT
                        )
                    }

                } else {
                    // TRƯỜNG HỢP: BÌNH LUẬN BÀI VIẾT
                    val currentPost = _post.value
                    if (currentPost != null && currentPost.userId != currentUserId) {
                        sendNotification(
                            recipientId = currentPost.userId,
                            postId = postId,
                            content = content,
                            type = NotificationType.COMMENT_POST
                        )
                    }
                }

            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- HÀM GỬI THÔNG BÁO (ĐÃ SỬA LỖI) ---
    private fun sendNotification(recipientId: String, postId: String, content: String, type: NotificationType) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users").document(currentUserId).get().await()
                val senderName = userDoc.getString("fullName") ?: "Người dùng"
                val senderAvatar = userDoc.getString("avatar") ?: ""

                val notifRef = firestore.collection("users").document(recipientId)
                    .collection("notifications").document()

                val notification = Notification(
                    id = notifRef.id,
                    recipientId = recipientId, // <--- ĐÃ THÊM DÒNG NÀY (QUAN TRỌNG)
                    postId = postId,
                    senderId = currentUserId,
                    senderName = senderName,
                    senderAvatar = senderAvatar,
                    content = content,
                    type = type,
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
                notifRef.set(notification)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("posts").document(postId)
                    .collection("comments").document(commentId).delete().await()
                firestore.collection("posts").document(postId)
                    .update("commentCount", FieldValue.increment(-1))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun editComment(postId: String, commentId: String, newContent: String) {
        viewModelScope.launch {
            try {
                firestore.collection("posts").document(postId)
                    .collection("comments").document(commentId)
                    .update("content", newContent).await()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
package com.example.app_yolo11.model

enum class NotificationType {
    LIKE_POST,
    COMMENT_POST,
    LIKE_COMMENT,
    REPLY_COMMENT,


    COMMENT,
    LIKE
}

data class Notification(
    val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatar: String = "",
    val postId: String = "",
    val content: String = "",
    val type: NotificationType = NotificationType.COMMENT_POST,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
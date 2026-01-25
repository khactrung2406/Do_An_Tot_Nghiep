package com.example.app_yolo11.model

data class Comment(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val replyToName: String = "",
    val parentId: String = "",
    val likes: List<String> = emptyList()
)
package com.example.app_yolo11.model

import com.google.firebase.firestore.PropertyName

data class SocialPost(
    @get:PropertyName("pid") @set:PropertyName("pid")
    var pId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likes: List<String> = emptyList(),
    val commentCount: Int = 0,
    val isHelpRequest: Boolean = false
) {
    fun getTimeAgo(): String {

        return "Vừa xong"
    }
}
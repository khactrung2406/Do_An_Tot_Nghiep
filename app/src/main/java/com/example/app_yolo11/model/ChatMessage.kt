package com.example.app_yolo11.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.Timestamp
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isFromUser: Boolean = false,
    val timestamp: Timestamp = Timestamp.now(),
    @get:Exclude
    val isLoading: Boolean = false,
    @get:Exclude
    val isError: Boolean = false
)
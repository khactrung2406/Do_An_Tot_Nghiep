package com.example.app_yolo11.model

data class CollectionItem(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val scientificName: String? = null,
    val description: String? = null,
    val location: String? = null,
    val images: List<String> = emptyList(),
    val collectedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
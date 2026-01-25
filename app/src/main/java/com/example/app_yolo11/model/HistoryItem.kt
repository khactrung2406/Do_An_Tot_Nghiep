package com.example.app_yolo11.model

import com.google.firebase.Timestamp
data class HistoryItem(
    val id: String = "",
    val name: String = "",
    val scientificName: String = "",
    val imageUrl: String = "",
    val timestamp: Timestamp = Timestamp.now()
) {

    fun toSeaSnail(): SeaSnails {
        return SeaSnails(
            id = this.id,
            name = this.name,
            scientificName = this.scientificName,
            imageUrl = this.imageUrl,
            description = "",
            distribution = "",
            habitat = ""
        )
    }
}
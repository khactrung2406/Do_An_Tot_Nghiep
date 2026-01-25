package com.example.app_yolo11.repositories

import android.util.Log
import com.example.app_yolo11.model.SeaSnails
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await
import java.text.Normalizer
import java.util.regex.Pattern

class SeaSnailRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    suspend fun getSnailById(id: String): SeaSnails? {
        try {
            val querySnapshot = db.collection("seasnail")
                .whereEqualTo("id", id)
                .limit(1)
                .get()
                .await()

            return if (!querySnapshot.isEmpty) {
                Log.d("SeaSnailRepository", "Đã tìm thấy document!")
                mapDocumentToSnail(querySnapshot.documents[0])
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SeaSnailRepository", "Lỗi query: ${e.message}")
            return null
        }
    }

    suspend fun searchSnailsByName(query: String): List<SeaSnails> {
        if (query.isBlank()) return emptyList()

        try {
            val snapshot = db.collection("seasnail")
                .get()
                .await()

            val allSnails = snapshot.documents.mapNotNull { doc ->
                mapDocumentToSnail(doc)
            }

            val normalizedQuery = query.removeAccents()

            return allSnails.filter { snail ->
                val normalizedName = snail.name.removeAccents()
                normalizedName.contains(normalizedQuery)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    private fun mapDocumentToSnail(document: DocumentSnapshot): SeaSnails? {
        val data = document.data ?: return null

        return SeaSnails(
            id = data["id"] as? String ?: document.id,
            name = data["name"] as? String ?: "",
            scientificName = data["scientificName"] as? String ?: "",
            family = data["family"] as? String ?: "",
            description = data["description"] as? String ?: "",
            habitat = data["habitat"] as? String ?: "",
            behavior = data["behavior"] as? String ?: "",
            distribution = data["distribution"] as? String ?: "",
            conservationStatus = data["conservationStatus"] as? String ?: "",
            value = data["value"] as? String ?: "",
            imageUrl = data["imageUrl"] as? String ?: ""
        )
    }

    fun String.removeAccents(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(temp).replaceAll("").lowercase().replace("đ", "d")
    }

    suspend fun debugCheckAllData() {
        try {

            val snapshot = db.collection("seasnail").get().await()

            Log.d("DEBUG_FIREBASE", "Tổng số document tìm thấy: ${snapshot.documents.size}")

            for (doc in snapshot.documents) {
                val idVal = doc.getString("id")
                val nameVal = doc.getString("name")
                Log.d("DEBUG_FIREBASE", "Doc ID thật: '${doc.id}' - Field 'id': '$idVal' - Name: '$nameVal'")
            }
        } catch (e: Exception) {
            Log.e("DEBUG_FIREBASE", "Lỗi kết nối Firebase: ${e.message}")
        }
    }
}
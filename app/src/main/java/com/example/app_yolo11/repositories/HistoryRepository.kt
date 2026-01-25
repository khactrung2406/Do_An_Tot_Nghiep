package com.example.app_yolo11.repositories

import com.example.app_yolo11.model.HistoryItem
import com.example.app_yolo11.model.SeaSnails
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class HistoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String?
        get() = auth.currentUser?.uid

    suspend fun addToHistory(snail: SeaSnails) {
        if (userId == null) return

        val historyItem = HistoryItem(
            id = snail.id,
            name = snail.name,
            scientificName = snail.scientificName,
            imageUrl = snail.imageUrl,
            timestamp = Timestamp.now()
        )

        firestore.collection("users").document(userId!!)
            .collection("history").document(snail.id)
            .set(historyItem)
            .await()
    }


    fun getHistory(): Flow<List<HistoryItem>> = callbackFlow {
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val collectionRef = firestore.collection("users").document(userId!!)
            .collection("history")
            .orderBy("timestamp", Query.Direction.DESCENDING)


        val listener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val items = snapshot.toObjects(HistoryItem::class.java)
                trySend(items)
            }
        }

        awaitClose { listener.remove() }
    }


    suspend fun clearHistory() {
        if (userId == null) return
        val batch = firestore.batch()
        val snapshot = firestore.collection("users").document(userId!!)
            .collection("history").get().await()

        for (document in snapshot.documents) {
            batch.delete(document.reference)
        }
        batch.commit().await()
    }
}
package com.example.app_yolo11.repositories
import com.example.app_yolo11.model.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface ChatRepository {
    suspend fun getChatHistory(uid: String): Result<List<ChatMessage>>
    suspend fun saveMessage(uid: String, message: ChatMessage): Result<Unit>
}

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override suspend fun getChatHistory(uid: String): Result<List<ChatMessage>> {
        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("chatHistory")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            val messages = snapshot.documents.mapNotNull { doc ->

                ChatMessage(
                    id = doc.getString("id") ?: "",
                    text = doc.getString("text") ?: "",
                    isFromUser = doc.getBoolean("isFromUser") ?: false,
                    timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                    isError = false,
                    isLoading = false
                )
            }
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveMessage(uid: String, message: ChatMessage): Result<Unit> {
        return try {
            val data = hashMapOf(
                "id" to message.id,
                "text" to message.text,
                "isFromUser" to message.isFromUser,
                "timestamp" to message.timestamp
            )

            firestore.collection("users")
                .document(uid)
                .collection("chatHistory")
                .document(message.id)
                .set(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
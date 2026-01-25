package com.example.app_yolo11.ui.Screens.Collection

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.model.CollectionItem
import com.example.app_yolo11.repositories.CloudinaryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val cloudinaryRepository: CloudinaryRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _items = MutableStateFlow<List<CollectionItem>>(emptyList())
    val items = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchCollection()
    }
    private fun fetchCollection() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true
        firestore.collection("users").document(userId).collection("my_collection")
            .orderBy("collectedDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                _isLoading.value = false
                if (snapshot != null) {
                    _items.value = snapshot.toObjects(CollectionItem::class.java)
                }
            }
    }

    fun addToCollection(
        name: String, scientificName: String, description: String,
        date: Long?, location: String, imageUris: List<Uri>,
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val downloadUrls = imageUris.map { uri ->
                    async { cloudinaryRepository.uploadImage(uri).getOrThrow() }
                }.awaitAll()

                val newItem = CollectionItem(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    name = name,
                    scientificName = if (scientificName.isBlank()) null else scientificName,
                    description = if (description.isBlank()) null else description,
                    location = if (location.isBlank()) null else location,
                    images = downloadUrls,
                    collectedDate = date ?: System.currentTimeMillis()
                )

                firestore.collection("users").document(userId)
                    .collection("my_collection").document(newItem.id)
                    .set(newItem).await()

                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "Lỗi thêm dữ liệu")
            }
        }
    }

    fun updateCollection(
        itemId: String, name: String, scientificName: String, description: String,
        date: Long?, location: String, oldImageUrls: List<String>, newImageUris: List<Uri>,
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Upload ảnh mới
                val newDownloadUrls = newImageUris.map { uri ->
                    async { cloudinaryRepository.uploadImage(uri).getOrThrow() }
                }.awaitAll()

                val finalImages = oldImageUrls + newDownloadUrls

                if (finalImages.isEmpty()) {
                    _isLoading.value = false
                    onError("Cần ít nhất 1 hình ảnh")
                    return@launch
                }

                val updateData = mapOf(
                    "name" to name,
                    "scientificName" to if (scientificName.isBlank()) null else scientificName,
                    "description" to if (description.isBlank()) null else description,
                    "location" to if (location.isBlank()) null else location,
                    "collectedDate" to (date ?: System.currentTimeMillis()),
                    "images" to finalImages
                )

                firestore.collection("users").document(userId)
                    .collection("my_collection").document(itemId)
                    .update(updateData).await()

                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "Lỗi cập nhật")
            }
        }
    }

    fun deleteCollection(itemId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        firestore.collection("users").document(userId)
            .collection("my_collection").document(itemId)
            .delete()
            .addOnSuccessListener {
                _isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener {
                _isLoading.value = false
                onError(it.message ?: "Lỗi xóa")
            }
    }
}
package com.example.app_yolo11.ui.Screens.EditPost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.model.SocialPost
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _postContent = MutableStateFlow("")
    val postContent = _postContent.asStateFlow()

    private val _postImage = MutableStateFlow("")
    val postImage = _postImage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isUpdated = MutableStateFlow(false)
    val isUpdated = _isUpdated.asStateFlow()

    // 1. Load dữ liệu bài viết cũ lên màn hình
    fun loadPost(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val snapshot = firestore.collection("posts").document(postId).get().await()
            val post = snapshot.toObject(SocialPost::class.java)
            if (post != null) {
                _postContent.value = post.content
                _postImage.value = post.imageUrl
            }
            _isLoading.value = false
        }
    }

    fun updateContent(newContent: String) {
        _postContent.value = newContent
    }

    // 2. Lưu thay đổi lên Firestore
    fun saveChanges(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                firestore.collection("posts").document(postId)
                    .update("content", _postContent.value)
                    .await()
                _isUpdated.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
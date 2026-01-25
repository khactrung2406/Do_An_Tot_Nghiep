package com.example.app_yolo11.ui.Screens.Create

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.model.SocialPost
import com.example.app_yolo11.model.Users // Import model Users
import com.example.app_yolo11.repositories.CloudinaryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val cloudinaryRepository: CloudinaryRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    fun createPost(
        context: Context,
        content: String,
        imageUri: Uri?,
        isHelpRequest: Boolean
    ) {

        if (imageUri == null) {
            Toast.makeText(context, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show()
            return
        }

        if (content.isBlank()) {
            Toast.makeText(context, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val userDoc = firestore.collection("users").document(currentUserId).get().await()
                val user = userDoc.toObject(Users::class.java)

                if (user == null) {
                    Toast.makeText(context, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show()
                    _isLoading.value = false
                    return@launch
                }

                val uploadResult = cloudinaryRepository.uploadImage(imageUri)

                uploadResult.onSuccess { postImageUrl ->
                    val newPostId = firestore.collection("posts").document().id

                    val post = SocialPost(
                        pId = newPostId,
                        userId = user.uid,
                        userName = user.fullName,
                        userAvatar = user.avatar,
                        content = content,
                        imageUrl = postImageUrl,
                        createdAt = System.currentTimeMillis(),
                        likes = emptyList(),
                        commentCount = 0,
                        isHelpRequest = isHelpRequest
                    )

                    firestore.collection("posts").document(newPostId)
                        .set(post)
                        .addOnSuccessListener {
                            _isLoading.value = false
                            _isSuccess.value = true
                            Toast.makeText(context, "Đăng bài thành công!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            _isLoading.value = false
                            Toast.makeText(context, "Lỗi lưu bài: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }.onFailure { e ->
                    _isLoading.value = false
                    Toast.makeText(context, "Lỗi upload ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                _isLoading.value = false
                Toast.makeText(context, "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun resetState() {
        _isSuccess.value = false
    }
}
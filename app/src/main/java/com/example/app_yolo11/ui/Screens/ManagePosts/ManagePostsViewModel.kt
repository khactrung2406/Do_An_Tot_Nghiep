package com.example.app_yolo11.ui.Screens.ManagePosts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.model.SocialPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@HiltViewModel
class ManagePostsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _userPosts = MutableStateFlow<List<SocialPost>>(emptyList())
    val userPosts = _userPosts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchUserPosts()
    }

    fun fetchUserPosts() {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true

        firestore.collection("posts")
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _userPosts.value = snapshot.toObjects(SocialPost::class.java)
                }
                _isLoading.value = false
            }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {

                firestore.collection("posts").document(postId).delete().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
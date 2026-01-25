package com.example.app_yolo11.repositories

import androidx.lifecycle.LiveData
import com.example.app_yolo11.common.enum.AuthState
import com.example.app_yolo11.model.Users
import kotlinx.coroutines.flow.StateFlow

interface Auth {
    val currentUser: StateFlow<Users?>
    suspend fun signUp(email: String, password: String, fullName: String,confirmPassword: String): Result<Users>
    suspend fun login(email: String, password: String): Result<Users>
    suspend fun getUserInfo(uid: String): Users?
    fun signout()
    suspend fun checkEmailExists(email: String): Boolean
    suspend fun sendNewPassword(email: String):Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Users>
    suspend fun getCurrentUserName(): Result<String>
    suspend fun updateAvatar(avatarUrl: String): Result<Unit>

    suspend fun updatefullName(newName: String): Result<Unit>
    suspend fun changePassword(currentPass: String, newPass: String): Result<Unit>
    suspend fun getUserAvatarUrl(): Result<String?>

    fun isUserLoggedIn(): Boolean




}

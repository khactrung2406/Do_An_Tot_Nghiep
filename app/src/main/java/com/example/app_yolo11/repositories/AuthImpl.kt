package com.example.app_yolo11.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.app_yolo11.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import com.example.app_yolo11.common.enum.AuthState
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

@Singleton
class AuthImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,

) : Auth {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    private val _currentUser = MutableStateFlow<Users?>(null)
    override val currentUser = _currentUser.asStateFlow()

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        if (firebaseAuth.currentUser == null) {
            _authState.value = AuthState.UnAuthenticated
            _currentUser.value = null
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun updateLocalAvatar(newUrl: String) {
        _currentUser.update { currentUser ->
            currentUser?.copy(avatar = newUrl)
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        confirmPassword: String
    ): Result<Users> {
        return try {
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullName.isEmpty()) {
                val msg = "Vui lòng điền đầy đủ thông tin bắt buộc!"
                _authState.value = AuthState.Error(msg)
                return Result.failure(Exception(msg))
            }

            if (password != confirmPassword) {
                val msg = "Mật khẩu xác nhận không khớp. Vui lòng kiểm tra lại."
                _authState.value = AuthState.Error(msg)
                return Result.failure(Exception(msg))
            }

            _authState.value = AuthState.Loading

            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Tạo tài khoản thất bại")
            val newUser = Users(
                uid = uid,
                email = email,
                fullName = fullName,
            )

            firestore.collection("users")
                .document(uid)
                .set(newUser)
                .await()

            _currentUser.value = newUser
            _authState.value = AuthState.Authenticated
            Result.success(newUser)

        } catch (e: Exception) {
            val errorMsg = translateFirebaseError(e)
            _authState.value = AuthState.Error(errorMsg)
            return Result.failure(Exception(errorMsg))
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<Users> {
        return try {
            if (email.isEmpty() || password.isEmpty()) {
                throw Exception("Vui lòng nhập Email và Mật khẩu.")
            }

            firebaseAuth.signInWithEmailAndPassword(email, password).await()

            val uid = firebaseAuth.currentUser?.uid ?: throw Exception("User ID bị mất sau khi đăng nhập.")
            val user = getUserInfo(uid) ?: throw Exception("Không tìm thấy hồ sơ người dùng trong hệ thống.")

            _currentUser.value = user
            _authState.value = AuthState.Authenticated
            Result.success(user)
        } catch (e: Exception) {
            val errorMsg = translateFirebaseError(e)
            _authState.value = AuthState.Error(errorMsg)
            return Result.failure(Exception(errorMsg))
        }
    }

    override suspend fun getUserInfo(uid: String): Users? {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            val user = document.toObject(Users::class.java)
            if (user != null) {
                _currentUser.value = user
            }
            user
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun signout() {
        firebaseAuth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.UnAuthenticated
    }

    override suspend fun checkEmailExists(email: String): Boolean {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun sendNewPassword(email: String): Result<Unit> {
        return try {
            if (email.isBlank()) {
                throw Exception("Vui lòng nhập địa chỉ email để khôi phục mật khẩu.")
            }
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = translateFirebaseError(e)
            Result.failure(Exception(errorMsg))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Users> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Không thể lấy thông tin User ID sau khi đăng nhập Google.")

            val docSnapshot = firestore.collection("users").document(user.uid).get().await()

            val userModel: Users
            if (docSnapshot.exists()) {
                userModel = docSnapshot.toObject(Users::class.java)!!
            } else {
                userModel = Users(
                    uid = user.uid,
                    email = user.email ?: "",
                    fullName = user.displayName ?: "Người dùng mới"
                )
                firestore.collection("users").document(user.uid).set(userModel).await()
            }

            _currentUser.value = userModel
            _authState.value = AuthState.Authenticated
            Result.success(userModel)
        } catch (e: Exception) {
            val errorMsg = translateFirebaseError(e)
            _authState.value = AuthState.Error(errorMsg)
            Result.failure(Exception(errorMsg))
        }
    }

    override suspend fun getCurrentUserName(): Result<String> = withContext(Dispatchers.IO) {
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            return@withContext Result.failure(Exception("Không có người dùng nào đang đăng nhập."))
        }

        try {
            val userDocument = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (userDocument.exists()) {
                val fullName = userDocument.getString("fullName")
                return@withContext if (fullName != null) {
                    Result.success(fullName)
                } else {
                    Result.failure(Exception("Thông tin tên người dùng bị thiếu trong Firestore."))
                }
            } else {
                return@withContext Result.failure(Exception("Không tìm thấy hồ sơ người dùng trong Firestore."))
            }

        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    override suspend fun updateAvatar(avatarUrl: String): Result<Unit> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: throw Exception("Bạn chưa đăng nhập. Vui lòng đăng nhập lại.")

            firestore.collection("users").document(uid)
                .update("avatar", avatarUrl)
                .await()

            updateLocalAvatar(avatarUrl)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(translateFirebaseError(e)))
        }
    }

    override suspend fun updatefullName(newName: String): Result<Unit> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: throw Exception("Bạn chưa đăng nhập. Vui lòng đăng nhập lại.")

            firestore.collection("users").document(uid)
                .update("fullName", newName)
                .await()

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()
            firebaseAuth.currentUser?.updateProfile(profileUpdates)?.await()

            _currentUser.update { it?.copy(fullName = newName) }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(translateFirebaseError(e)))
        }
    }

    override suspend fun changePassword(
        currentPass: String,
        newPass: String
    ): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("Bạn chưa đăng nhập.")
            val email = user.email ?: throw Exception("Không tìm thấy email của người dùng.")

            val credential = EmailAuthProvider.getCredential(email, currentPass)
            user.reauthenticate(credential).await()

            user.updatePassword(newPass).await()
            Result.success(Unit)

        } catch (e: Exception) {
            val errorMsg = translateFirebaseError(e)
            Result.failure(Exception(errorMsg))
        }
    }

    override suspend fun getUserAvatarUrl(): Result<String?> = withContext(Dispatchers.IO) {
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            return@withContext Result.failure(Exception("Chưa đăng nhập."))
        }

        try {
            val userDocument = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (userDocument.exists()) {
                val avatar = userDocument.getString("avatar")
                Result.success(avatar)
            } else {
                Result.failure(Exception("Không tìm thấy dữ liệu người dùng."))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    private fun translateFirebaseError(e: Exception): String {
        return when (e) {
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Email không hợp lệ. Vui lòng kiểm tra lại định dạng email."
                    "ERROR_USER_NOT_FOUND" -> "Không tìm thấy tài khoản với Email này. Vui lòng đăng ký."
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Email này đã được sử dụng. Vui lòng dùng Email khác."
                    "ERROR_INVALID_CREDENTIAL" -> "Email hoặc mật khẩu không đúng. Vui lòng kiểm tra lại."
                    else -> "Lỗi Firebase: ${e.message ?: "Đã xảy ra lỗi không xác định."}"
                }
            }
            is IOException -> "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối Internet của bạn."
            else -> e.message ?: "Đã xảy ra lỗi không xác định. Vui lòng thử lại."
        }
    }
}
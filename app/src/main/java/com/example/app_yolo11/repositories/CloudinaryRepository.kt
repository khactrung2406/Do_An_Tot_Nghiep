package com.example.app_yolo11.repositories

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

@Singleton
class CloudinaryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val cloudName = "dnoej28bv"
    private val uploadPreset = "avatar"

    suspend fun uploadImage(imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {

            val file = uriToFile(imageUri) ?: return@withContext Result.failure(Exception("Lỗi đọc file"))

            val client = OkHttpClient()


            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
                .addFormDataPart("upload_preset", uploadPreset)
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string()

            if (response.isSuccessful && responseString != null) {
                val json = JSONObject(responseString)
                val secureUrl = json.getString("secure_url")
                Result.success(secureUrl)
            } else {
                Result.failure(Exception("Upload thất bại: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    private fun uriToFile(uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return tempFile
    }
}
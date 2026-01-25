package com.example.app_yolo11.ui.Screens.Camera

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.TFLiteObjectDetector
import com.example.app_yolo11.di.ImageUtils
import com.example.app_yolo11.model.Mapping
import com.example.app_yolo11.model.SeaSnails // Đảm bảo bạn đã import model SeaSnail
import com.example.app_yolo11.repositories.Auth
import com.example.app_yolo11.repositories.CloudinaryRepository
import com.example.app_yolo11.repositories.HistoryRepository
import com.example.app_yolo11.repositories.SeaSnailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val authRepository: Auth,
    private val cloudinaryRepository: CloudinaryRepository,
    private val historyRepository: HistoryRepository,
    private val seaSnailRepository: SeaSnailRepository
) : ViewModel() {

    private var detector: TFLiteObjectDetector? = null

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _detectionResult = MutableStateFlow<TFLiteObjectDetector.DetectionResult?>(null)
    val detectionResult: StateFlow<TFLiteObjectDetector.DetectionResult?> = _detectionResult

    private val _detectedSnail = MutableStateFlow<SeaSnails?>(null)
    val detectedSnail: StateFlow<SeaSnails?> = _detectedSnail
    private val _showResultOverlay = MutableStateFlow(false)
    val showResultOverlay: StateFlow<Boolean> = _showResultOverlay

    fun initializeClassifier(context: Context) {
        if (detector == null) {
            detector = TFLiteObjectDetector(context)
            Log.d("CameraViewModel", "TFLiteObjectDetector initialized with input 640!")
        }
    }

    fun resetState() {
        _detectionResult.value = null
        _detectedSnail.value = null
        _showResultOverlay.value = false
    }

    fun processImage(
        context: Context,
        imageUri: Uri,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            _showResultOverlay.value = false

            try {
                withContext(Dispatchers.IO) {
                    val bitmap = ImageUtils.getBitmapFromUri(context, imageUri)
                    if (bitmap == null) {
                        withContext(Dispatchers.Main) { onError() }
                        return@withContext
                    }

                    val resizedBitmap = ImageUtils.resizeBitmapAndPad(bitmap, 640)

                    val results = detector?.detect(resizedBitmap)
                    val topResult = results?.maxByOrNull { it.score }

                    if (topResult != null && topResult.score >= 0.3f) {
                        Log.d("YOLO", "Detected: ${topResult.label} score=${topResult.score}")

                        val mappedSnailId = Mapping.map[topResult.label]
                        if (mappedSnailId != null) {
                            val snailDetails = seaSnailRepository.getSnailById(mappedSnailId)

                            if (snailDetails != null) {
                                _detectionResult.value = topResult
                                _detectedSnail.value = snailDetails
                                _showResultOverlay.value = true

                                try {
                                    historyRepository.addToHistory(snailDetails)
                                } catch (e: Exception) {
                                    Log.e("History", "Failed to save history: ${e.message}")
                                }
                            } else {
                                withContext(Dispatchers.Main) { onError() }
                            }
                        } else {
                            withContext(Dispatchers.Main) { onError() }
                        }

                        cloudinaryRepository.uploadImage(imageUri)

                    } else {
                        withContext(Dispatchers.Main) { onError() }
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Error processing image: ${e.message}")
                withContext(Dispatchers.Main) { onError() }
            } finally {
                _isProcessing.value = false
            }
        }
    }
}
package com.example.app_yolo11.ui.Screens.Camera

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.app_yolo11.Screens
import com.example.app_yolo11.TFLiteObjectDetector
import com.example.app_yolo11.di.ImageUtils
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

val OceanBlueDark = Color(0xFF006994)
val OceanBlueLight = Color(0xFF4FC3F7)

@Composable
fun Camera(
    navController: NavHostController,
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }
    var zoomStepIndex by remember { mutableStateOf(0) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val preview = remember { Preview.Builder().build() }

    var showErrorDialog by remember { mutableStateOf(false) }
    var imageUriToProcess by remember { mutableStateOf<Uri?>(null) }
    var displayBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val isProcessing by viewModel.isProcessing.collectAsState()
    val detectionResult by viewModel.detectionResult.collectAsState()
    val detectedSnail by viewModel.detectedSnail.collectAsState()
    val showResultOverlay by viewModel.showResultOverlay.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeClassifier(context)
        cameraProvider = context.getCameraProvider()
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { imageUriToProcess = it } }

    LaunchedEffect(imageUriToProcess) {
        val uri = imageUriToProcess ?: return@LaunchedEffect
        showErrorDialog = false

        viewModel.resetState()

        val bitmap = ImageUtils.getBitmapFromUri(context, uri)
        displayBitmap = bitmap

        val fileToDelete = if (uri.scheme == "file") java.io.File(uri.path!!) else null

        viewModel.processImage(context, uri) {
            fileToDelete?.delete()
            showErrorDialog = true
        }
    }

    LaunchedEffect(cameraSelector, cameraProvider) {
        val provider = cameraProvider ?: return@LaunchedEffect
        try {
            provider.unbindAll()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            camera = provider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageCapture
            )
            isFlashOn = false
            zoomStepIndex = 0
            camera?.cameraControl?.setLinearZoom(0f)
        } catch (e: Exception) {
            Log.e("Camera", "Error binding camera", e)
        }
    }

    DisposableEffect(Unit) {
        onDispose { cameraProvider?.unbindAll() }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        if (displayBitmap != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(
                    bitmap = displayBitmap!!.asImageBitmap(),
                    contentDescription = "Captured Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                if (showResultOverlay && detectionResult != null) {
                    BoundingBoxOverlay(
                        detectionResult = detectionResult!!,
                        imageWidth = displayBitmap!!.width,
                        imageHeight = displayBitmap!!.height
                    )
                }
            }
        } else {
            AndroidView(
                factory = {
                    previewView.apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        setOnTouchListener { view, motionEvent ->
                            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                                val currentPreviewView = view as PreviewView
                                val factory = currentPreviewView.meteringPointFactory
                                val point = factory.createPoint(motionEvent.x, motionEvent.y)
                                val action = FocusMeteringAction.Builder(point)
                                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                                    .build()
                                camera?.cameraControl?.startFocusAndMetering(action)
                                view.performClick()
                            }
                            true
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            ScannerOverlay()
        }

        if (!isProcessing && !showResultOverlay) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CameraControlButton(
                    icon = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    isActive = isFlashOn,
                    onClick = {
                        isFlashOn = !isFlashOn
                        camera?.cameraControl?.enableTorch(isFlashOn)
                    }
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .clickable {
                            zoomStepIndex = (zoomStepIndex + 1) % 6
                            camera?.cameraControl?.setLinearZoom(zoomStepIndex * 0.2f)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "${zoomStepIndex + 1}x", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(bottom = 50.dp, top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { pickImageLauncher.launch("image/*") }) {
                        Icon(Icons.Default.PhotoLibrary, "Gallery", tint = Color.White, modifier = Modifier.size(32.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                capturePhoto(imageCapture, context) { uri ->
                                    imageUriToProcess = uri
                                }
                            }
                    )

                    IconButton(
                        onClick = {
                            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                                CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                        }
                    ) {
                        Icon(Icons.Default.Cameraswitch, "Switch Camera", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        if (isProcessing && !showResultOverlay && imageUriToProcess != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(60.dp), color = OceanBlueLight)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Đang phân tích...", color = Color.White, fontSize = 18.sp)
                }
            }
        }

        if (showResultOverlay && detectedSnail != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Đã phát hiện:", color = Color.Gray, fontSize = 14.sp)
                Text(
                    text = detectedSnail!!.name ?: "Không rõ tên",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = OceanBlueDark,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.resetState()
                            detectedSnail!!.id?.let { id ->
                                navController.navigate("detail_screen/$id")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBlueDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Xem thông tin chi tiết")
                    }

                    Button(
                        onClick = {
                            viewModel.resetState()
                            navController.navigate(Screens.ChatBot.route)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBlueLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hỏi trợ lý")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = {
                    imageUriToProcess = null
                    displayBitmap = null
                    viewModel.resetState()
                }) {
                    Text("Chụp ảnh khác", color = Color.Gray)
                }
            }
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = {
                    showErrorDialog = false
                    imageUriToProcess = null
                    displayBitmap = null
                    viewModel.resetState()
                },
                title = { Text("Chưa nhận diện được", color = Color.Red) },
                text = { Text("Không tìm thấy loài ốc nào trong ảnh này.") },
                confirmButton = {
                    Button(onClick = {
                        showErrorDialog = false
                        imageUriToProcess = null
                        displayBitmap = null
                        viewModel.resetState()
                    }, colors = ButtonDefaults.buttonColors(containerColor = OceanBlueDark)) {
                        Text("Thử lại")
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun BoundingBoxOverlay(
    detectionResult: TFLiteObjectDetector.DetectionResult,
    imageWidth: Int,
    imageHeight: Int
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val inputSize = 640f

        val inputScale = min(inputSize / imageWidth, inputSize / imageHeight)

        val padX = (inputSize - imageWidth * inputScale) / 2f
        val padY = (inputSize - imageHeight * inputScale) / 2f

        var boxLeft = detectionResult.boundingBox.left
        var boxTop = detectionResult.boundingBox.top
        var boxRight = detectionResult.boundingBox.right
        var boxBottom = detectionResult.boundingBox.bottom

        if (boxRight <= 1f && boxBottom <= 1f) {
            boxLeft *= inputSize
            boxTop *= inputSize
            boxRight *= inputSize
            boxBottom *= inputSize
        }

        val origLeft = (boxLeft - padX) / inputScale
        val origTop = (boxTop - padY) / inputScale
        val origRight = (boxRight - padX) / inputScale
        val origBottom = (boxBottom - padY) / inputScale

        val screenScale = min(canvasWidth / imageWidth, canvasHeight / imageHeight)
        val screenOffsetX = (canvasWidth - imageWidth * screenScale) / 2f
        val screenOffsetY = (canvasHeight - imageHeight * screenScale) / 2f

        val screenLeft = origLeft * screenScale + screenOffsetX
        val screenTop = origTop * screenScale + screenOffsetY
        val screenRight = origRight * screenScale + screenOffsetX
        val screenBottom = origBottom * screenScale + screenOffsetY

        drawRect(
            color = Color.Red,
            topLeft = Offset(screenLeft, screenTop),
            size = Size(screenRight - screenLeft, screenBottom - screenTop),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

@Composable
fun CameraControlButton(icon: androidx.compose.ui.graphics.vector.ImageVector, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(if (isActive) OceanBlueLight.copy(0.8f) else Color.Black.copy(0.5f), CircleShape)
            .border(1.dp, if (isActive) OceanBlueLight else Color.White.copy(0.5f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun ScannerOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width; val height = size.height
        val boxSize = width * 0.7f; val left = (width - boxSize) / 2; val top = (height - boxSize) / 2
        val cornerLength = 40.dp.toPx(); val dimColor = Color.Black.copy(alpha = 0.4f)

        drawRect(dimColor, Offset(0f, 0f), Size(width, top))
        drawRect(dimColor, Offset(0f, top + boxSize), Size(width, height - (top + boxSize)))
        drawRect(dimColor, Offset(0f, top), Size(left, boxSize))
        drawRect(dimColor, Offset(left + boxSize, top), Size(width - (left + boxSize), boxSize))
        val strokeColor = Color.White; val strokeWidth = 4.dp.toPx(); val cap = StrokeCap.Round
        drawLine(strokeColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth, cap)
        drawLine(strokeColor, Offset(left, top), Offset(left, top + cornerLength), strokeWidth, cap)
        drawLine(strokeColor, Offset(left + boxSize, top), Offset(left + boxSize - cornerLength, top), strokeWidth, cap)
        drawLine(strokeColor, Offset(left + boxSize, top), Offset(left + boxSize, top + cornerLength), strokeWidth, cap)
        drawLine(strokeColor, Offset(left, top + boxSize), Offset(left + cornerLength, top + boxSize), strokeWidth, cap)
        drawLine(strokeColor, Offset(left, top + boxSize), Offset(left, top + boxSize - cornerLength), strokeWidth, cap)
        drawLine(strokeColor, Offset(left + boxSize, top + boxSize), Offset(left + boxSize - cornerLength, top + boxSize), strokeWidth, cap)
        drawLine(strokeColor, Offset(left + boxSize, top + boxSize), Offset(left + boxSize, top + boxSize - cornerLength), strokeWidth, cap)
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        val provider = ProcessCameraProvider.getInstance(this)
        provider.addListener({ continuation.resume(provider.get()) }, ContextCompat.getMainExecutor(this))
    }

private fun capturePhoto(imageCapture: ImageCapture, context: Context, onImageSaved: (Uri) -> Unit) {
    val outputDirectory = context.cacheDir
    val photoFile = java.io.File(outputDirectory, "temp_capture_${System.currentTimeMillis()}.jpg")
    val output = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(output, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
        override fun onError(exc: ImageCaptureException) {
            Toast.makeText(context, "Lỗi: ${exc.message}", Toast.LENGTH_SHORT).show()
        }
        override fun onImageSaved(result: ImageCapture.OutputFileResults) {
            onImageSaved(Uri.fromFile(photoFile))
        }
    })
}
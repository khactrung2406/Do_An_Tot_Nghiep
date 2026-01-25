package com.example.app_yolo11.ui.Screens.Camera

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.app_yolo11.ui.Screens.Home.HomeViewModel
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper


@Composable
fun CameraScreen(navController: NavHostController, viewModel: CameraViewModel) {
    val context = LocalContext.current

    val hasPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    val isGranted = remember { mutableStateOf(hasPermission) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            isGranted.value = granted
        }
    )


    LaunchedEffect(Unit) {
        if (!isGranted.value) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }


    LaunchedEffect(Unit) {
        viewModel.initializeClassifier(context)
    }

    if (isGranted.value) {

        Camera(navController = navController, viewModel = viewModel)
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = {
                    launcher.launch(Manifest.permission.CAMERA)
                }
            ) {
                Text(text = "Cấp quyền Camera")
            }
        }
    }
}
fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be requested from an Activity")
}
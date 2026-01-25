package com.example.app_yolo11

import androidx.lifecycle.ViewModel
import com.example.app_yolo11.repositories.Auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: Auth
) : ViewModel(){
    fun getStartDestination(): String {
        return if (authRepository.isUserLoggedIn()) {
            Screens.HomeScreen.route
        } else {
            Screens.Login.route
        }
    }


}
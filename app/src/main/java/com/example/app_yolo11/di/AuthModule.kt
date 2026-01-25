package com.example.app_yolo11.di

import com.example.app_yolo11.repositories.Auth
import com.example.app_yolo11.repositories.AuthImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

class AuthModule {
    @Module
    @InstallIn(SingletonComponent::class)
    abstract class AuthModule {
        @Binds
        @Singleton
        abstract fun bindAuthRepository(
            authImpl: AuthImpl
        ): Auth
    }
}
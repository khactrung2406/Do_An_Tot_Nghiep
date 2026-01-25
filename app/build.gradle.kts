import org.jetbrains.kotlin.fir.expressions.FirEmptyArgumentList.arguments

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.app_yolo11"
    compileSdk = 36
    ndkVersion = "26.1.10909125"


    defaultConfig {
        applicationId = "com.example.app_yolo11"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"



        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(platform(libs.firebase.bom))


    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-firestore")
    implementation(libs.firebase.analytics)

    implementation("com.google.android.gms:play-services-auth:21.4.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)


    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)


    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.room.external.antlr)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.foundation)


    ksp(libs.hilt.android.compiler)


    implementation(libs.androidx.navigation.compose)
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel-compose:1.3.0")
    implementation(libs.androidx.hilt.navigation.fragment)


    implementation(libs.androidx.ui)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    val camerax_version = "1.6.0-alpha01"
    // The following line is optional, as the core library is included indirectly by camera-camera2
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    // If you want to additionally use the CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    // If you want to additionally use the CameraX VideoCapture library
    implementation("androidx.camera:camera-video:${camerax_version}")
    // If you want to additionally use the CameraX View class
    implementation("androidx.camera:camera-view:${camerax_version}")
    // If you want to additionally add CameraX ML Kit Vision Integration
    implementation("androidx.camera:camera-mlkit-vision:${camerax_version}")
    // If you want to additionally use the CameraX Extensions library
    implementation("androidx.camera:camera-extensions:${camerax_version}")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okio:okio:3.9.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
    implementation("com.google.mlkit:vision-common:17.0.0")

    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")

    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.3")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation(libs.generativeai)
}
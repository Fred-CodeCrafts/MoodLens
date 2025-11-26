plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt") // Add this line for Room annotation processing
    kotlin("plugin.serialization") version "1.9.22"

}

android {
    namespace = "com.fredcodecrafts.moodlens"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fredcodecrafts.moodlens"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86_64"))// <-- Kalo Kotlin pake .addAll(listOf(...))        }
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

// Supabase Auth + PostgREST
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

// Ktor client engine for Android
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-okhttp:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-client-logging:2.3.7")

    // FIX: Add these lines to resolve the credentials error
    val credentialsVersion = "1.3.0-alpha02" // A recent, working version
    implementation("androidx.credentials:credentials:$credentialsVersion")
    implementation("androidx.credentials:credentials-play-services-auth:$credentialsVersion")

// Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Kotlin serialization (required by supabase-kt)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

// Google Identity Services (for native Google login)
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

// Optional - Android Credentials support for Play Services
    implementation("androidx.credentials:credentials:1.1.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.1.0")


    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    // Google Sign-In / Credentials API
    implementation("com.google.android.gms:play-services-auth:20.5.0")  // Google Sign-In
    // Kotlin Coroutines (for Flow)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    // Jetpack Compose (if using Compose UI)
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material:material:1.6.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    // CameraX core
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
// PreviewView
    implementation("androidx.camera:camera-view:1.3.4")
// ML Kit / image capture (optional)
    implementation("androidx.camera:camera-extensions:1.3.4")
// Permissions
    implementation("androidx.activity:activity-compose:1.9.2")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation("org.tensorflow:tensorflow-lite:2.12.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

// Coroutines (optional but recommended with Room)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation ("androidx.room:room-runtime:2.8.1")
    implementation ("androidx.room:room-ktx:2.8.1")
    kapt ("androidx.room:room-compiler:2.8.1")

    implementation("androidx.navigation:navigation-compose:2.9.5")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

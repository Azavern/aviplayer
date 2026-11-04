// App module aviplayer — sesuai Blueprint Bagian 3 & Milestone 0.
// AGP 9: Kotlin built-in; plugin org.jetbrains.kotlin.android TIDAK diterapkan.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room3)
}

android {
    namespace = "com.azavern.aviplayer"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.azavern.aviplayer"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

// Skema Room 3 diekspor dan di-commit untuk migrasi berikutnya (Blueprint Bagian 7).
room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.animation.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.adaptive)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // Playback (Media3)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)

    // Dependency Injection (Hilt)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Storage (Room 3 + DataStore)
    implementation(libs.androidx.room3.runtime)
    ksp(libs.androidx.room3.compiler)
    implementation(libs.androidx.sqlite.framework)
    implementation(libs.androidx.datastore.preferences)

    // Lifecycle & Async
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // Image (Coil 3)
    implementation(libs.coil.compose)
    implementation(libs.coil.video)

    // Serialisasi
    implementation(libs.kotlinx.serialization.json)

    // Permissions
    implementation(libs.accompanist.permissions)
}

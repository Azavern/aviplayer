// Project level — deklarasi plugin saja (apply false). Versi: gradle/libs.versions.toml
// Sesuai Blueprint Bagian 3 & Milestone 0: AGP 9 punya Kotlin built-in,
// plugin org.jetbrains.kotlin.android TIDAK dipakai.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room3) apply false
}

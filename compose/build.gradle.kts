plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(23)
    }
}

dependencies {
    val kotlinVersion = org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
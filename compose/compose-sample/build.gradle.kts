plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(23)
        targetSdkVersion(29)
        applicationId = "com.neeplayer.compose.sample"
        versionCode = 1
        versionName = "1.0"
    }
    packagingOptions {
        exclude("META-INF/**.kotlin_module")
    }
}

dependencies {
    implementation(project(":compose"))
}
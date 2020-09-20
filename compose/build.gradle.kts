plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(23)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
        freeCompilerArgs = listOf("-Xallow-jvm-ir-dependencies", "-Xskip-prerelease-check")
    }
    composeOptions {
        kotlinCompilerVersion = "1.4.0"
        kotlinCompilerExtensionVersion = "1.0.0-alpha03"
    }
}

dependencies {
    val kotlinVersion = org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION
    api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    val composeVersion = "1.0.0-alpha03"
    api("androidx.compose.ui:ui:$composeVersion")
    api("androidx.compose.material:material:$composeVersion")
    api("androidx.ui:ui-tooling:$composeVersion")

    implementation("com.github.bumptech.glide:glide:4.11.0")
}
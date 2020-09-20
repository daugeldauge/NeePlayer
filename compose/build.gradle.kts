plugins {
    id("com.android.library")
    id("kotlin-android")
}

var compose: String by ext
compose = "1.0.0-alpha03"

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
        kotlinCompilerExtensionVersion = compose
    }
}

dependencies {
    val kotlinVersion = org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION
    api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    api("androidx.compose.ui:ui:$compose")
    api("androidx.compose.material:material:$compose")
    api("androidx.ui:ui-tooling:$compose")

    implementation("com.github.bumptech.glide:glide:4.11.0")
}
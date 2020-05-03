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

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerVersion = "1.3.70-dev-withExperimentalGoogleExtensions-20200424"
        kotlinCompilerExtensionVersion = "0.1.0-dev10"
    }
}

dependencies {
    val kotlinVersion = org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION
    api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    val composeVersion = "0.1.0-dev10"
    api("androidx.ui:ui-tooling:$composeVersion")
    api("androidx.ui:ui-layout:$composeVersion")
    api("androidx.ui:ui-material:$composeVersion")

    implementation("com.github.bumptech.glide:glide:4.10.0")
}
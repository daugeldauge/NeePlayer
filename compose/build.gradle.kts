plugins {
    id("com.android.application")
    id("kotlin-android")
}

var compose: String by ext
compose = "1.0.0"

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 29
        targetSdk = 31
        applicationId = "com.neeplayer.compose"
        versionCode = 1
        versionName = "1.0"
    }

    packagingOptions {
        resources.excludes.add("META-INF/**.kotlin_module")
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
        allWarningsAsErrors = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = compose
    }
}

kotlin.sourceSets.all {
    languageSettings {
        useExperimentalAnnotation("androidx.compose.material.ExperimentalMaterialApi")
        useExperimentalAnnotation("kotlin.Experimental")
        useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
        useExperimentalAnnotation("kotlinx.coroutines.FlowPreview")
    }
}

dependencies {
    implementation("androidx.compose.ui:ui:$compose")
    implementation("androidx.compose.ui:ui-tooling:$compose")
    implementation("androidx.compose.material:material:$compose")
    implementation("androidx.activity:activity-compose:1.3.0-rc02")

    implementation("io.coil-kt:coil-compose:1.3.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.1")
}

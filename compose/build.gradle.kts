plugins {
    id("com.android.application")
    id("kotlin-android")
}

var compose: String by ext
compose = "1.0.0-alpha04"

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(23)
        targetSdkVersion(29)
        applicationId = "com.neeplayer.compose"
        versionCode = 1
        versionName = "1.0"
    }

    packagingOptions {
        exclude("META-INF/**.kotlin_module")
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
        freeCompilerArgs = listOf(
            "-Xallow-jvm-ir-dependencies",
            "-Xskip-prerelease-check",
            "-Xopt-in=androidx.compose.material.ExperimentalMaterialApi"
        )
    }
    composeOptions {
        kotlinCompilerExtensionVersion = compose
    }
}

dependencies {
    implementation("androidx.compose.ui:ui:$compose")
    implementation("androidx.compose.material:material:$compose")
    implementation("androidx.ui:ui-tooling:$compose")

    implementation("dev.chrisbanes.accompanist:accompanist-coil:0.2.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xuse-experimental=kotlin.Experimental",
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=kotlinx.coroutines.FlowPreview"
        )
    }
}
import net.rdrei.android.buildtimetracker.ReporterExtension

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-android-extensions")
    id("kotlinx-serialization")
    id("build-time-tracker")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 29
        targetSdk = 31
        applicationId = "com.neeplayer"
        versionCode = 3
        versionName = "1.1"

        vectorDrawables.useSupportLibrary = true
    }
    buildFeatures {
        dataBinding = true
    }
    packagingOptions {
        resources.excludes.add("META-INF/**.kotlin_module")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            manifestPlaceholders["app_name"] = "@string/app_name"
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["app_name"] = "@string/app_name_debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        allWarningsAsErrors = true
    }
}

dependencies {
    implementation("androidx.media:media:1.3.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.google.android.material:material:1.4.0")

    implementation("com.github.bumptech.glide:glide:4.12.0")

    implementation("com.pushtorefresh.storio:content-resolver:1.8.0")

    implementation("com.jakewharton.timber:timber:4.7.1")

    val ktorVersion = "1.6.1"
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization-jvm:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.1")

    val koinVersion = "3.1.2"
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-android:$koinVersion")

    val exoplayerVersion = "2.14.1"
    implementation("com.google.android.exoplayer:exoplayer-core:$exoplayerVersion")
    implementation("com.google.android.exoplayer:extension-mediasession:$exoplayerVersion")

    val ffmpegExtensionAar = properties["exoplayer-ffmpeg-aar"]?.toString()
    if (!ffmpegExtensionAar.isNullOrBlank() && file(ffmpegExtensionAar).exists()) {
        implementation(files(ffmpegExtensionAar))
    } else {
        logger.error("Building without exoplayer ffmpeg extension. Please specify path to extensions aar in `exoplayer-ffmpeg-aar` property")
    }

}

androidExtensions {
    isExperimental = true
}

kotlin.sourceSets.all {
    languageSettings {
        useExperimentalAnnotation("kotlin.Experimental")
        useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
        useExperimentalAnnotation("kotlinx.coroutines.FlowPreview")
    }
}

buildtimetracker {
    reporters {
        ReporterExtension("summary").apply {
            options["threshold"] = "1000"
            options["shortenTaskNames"] = "false"
            add(this)
        }
    }
}


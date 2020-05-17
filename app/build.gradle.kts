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
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(23)
        targetSdkVersion(28)
        applicationId = "com.neeplayer"
        versionCode = 1
        versionName = "1.0"

        vectorDrawables.useSupportLibrary = true
    }
    buildFeatures {
        dataBinding = true
    }
    packagingOptions {
        exclude("META-INF/**.kotlin_module")
    }
}

dependencies {
    val kotlinVersion = org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    implementation("androidx.media:media:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("com.google.android.material:material:1.1.0")

    implementation("com.github.bumptech.glide:glide:4.11.0")

    implementation("com.pushtorefresh.storio:content-resolver:1.8.0")

    implementation("com.jakewharton.timber:timber:4.7.1")

    val ktorVersion = "1.3.2"
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization-jvm:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.6")

    val koinVersion = "2.0.1"
    implementation("org.koin:koin-core:$koinVersion")
    implementation("org.koin:koin-android:$koinVersion")
    implementation("org.koin:koin-android-scope:$koinVersion")
}

androidExtensions {
    isExperimental = true
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

buildtimetracker {
    reporters {
        ReporterExtension("summary").apply {
            options["threshold"] = "1000"
            options["shortenTaskNames"] = "false"
            add(this)
        }
    }
}


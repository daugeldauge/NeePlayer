pluginManagement {
    val kotlinVersion = "1.5.10"

    repositories {
        mavenCentral()
        google()
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application", "com.android.library" -> "com.android.tools.build:gradle:7.1.0-alpha05"
                "kotlin-android",
                "kotlin-kapt",
                "kotlin-android-extensions" -> "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
                "kotlinx-serialization" -> "org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion"
                "build-time-tracker" -> "net.rdrei.android.buildtimetracker:gradle-plugin:0.11.1"
                else -> null
            }?.let(::useModule)
        }
    }
}

include("app")
include("compose")

pluginManagement {
    val kotlinVersion = "1.3.71"

    repositories {
        jcenter()
        google()
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application", "com.android.library" -> "com.android.tools.build:gradle:4.2.0-alpha03"
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
include("compose:compose-sample")

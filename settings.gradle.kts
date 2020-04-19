pluginManagement {
    val kotlinVersion = "1.3.71"

    repositories {
        jcenter()
        google()
    }

    resolutionStrategy {
        eachPlugin {
            when (val pluginId = requested.id.id) {
                "com.android.application", "com.android.library" -> "com.android.tools.build:gradle:4.1.0-alpha06"
                "kotlin-android",
                "kotlin-kapt",
                "kotlin-android-extensions" -> "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
                "kotlinx-serialization" -> "org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion"
                "build-time-tracker" -> "net.rdrei.android.buildtimetracker:gradle-plugin:0.11.1"
                else -> error("Unknown plugin id: '$pluginId'")
            }.let(::useModule)
        }
    }
}

include("app")
include("compose")

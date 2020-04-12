pluginManagement {
    val kotlinVersion = "1.3.71"

    repositories {
        jcenter()
        google()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    }

    resolutionStrategy {
        eachPlugin {
            when (val pluginId = requested.id.id) {
                "com.android.application", "com.android.library" -> "com.android.tools.build:gradle:3.6.0"
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

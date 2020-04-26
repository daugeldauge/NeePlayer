pluginManagement {
    val kotlinVersion = "1.3.71"

    repositories {
        jcenter()
        google()
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
//                "com.android.application", "com.android.library" -> "com.android.tools.build:gradle:4.1.0-alpha06" // TODO uncomment this when https://issuetracker.google.com/issues/154388196 will be fixed
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

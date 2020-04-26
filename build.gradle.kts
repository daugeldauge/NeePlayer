// TODO remove this block when https://issuetracker.google.com/issues/154388196 will be fixed
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.0-alpha07")
        classpath(kotlin("gradle-plugin", version = "1.3.71"))
    }
}

allprojects {
    repositories {
        jcenter()
        google()
    }
}

# kotlinx-serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.neeplayer.**$$serializer { *; }
-keepclassmembers class com.neeplayer.** {
    *** Companion;
}
-keepclasseswithmembers class com.neeplayer.** {
    kotlinx.serialization.KSerializer serializer(...);
}

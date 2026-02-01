# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep LiteRT-LM classes
-keep class com.google.ai.edge.litertlm.** { *; }

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keepclassmembers class * {
    @dagger.hilt.* <methods>;
    @javax.inject.* <fields>;
}

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Compose 
-keep class androidx.compose.** { *; }

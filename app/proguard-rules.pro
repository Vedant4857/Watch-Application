# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Wear Compose - keep all composables
-keep class androidx.wear.compose.** { *; }
-keep class androidx.compose.** { *; }

# Keep data classes
-keep class com.school.erp.watch.data.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

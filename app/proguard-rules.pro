# General project rules
-keep class com.rpn.blockblaster.** { *; }

# Koin
-keep class org.koin.** { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.KoinInternalApi *;
}

# Room
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}

# Kotlin Serialization
-dontwarn kotlinx.serialization.**
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}

# Google Play Services & Games
-keep class com.google.android.gms.games.** { *; }
-keep class com.google.android.gms.common.** { *; }

# AdMob
-keep class com.google.android.gms.ads.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Retrofit/OkHttp if used (bundle bundles.ktor is used)
-keep class io.ktor.** { *; }

# Timber
-keep class timber.log.** { *; }

# Fix: Missing classes detected while running R8
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean

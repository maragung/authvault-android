# AuthVault ProGuard Rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Kotlin
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandlerImpl {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile long <fields>;
}

# Hilt/Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keepnames class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.android.internal.lifecycle.HiltViewModelFactory <init>(**);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# DataStore
-keep class androidx.datastore.** { *; }

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Timber
-keep class timber.log.** { *; }

# Lottie
-keep class com.airbnb.lottie.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# Kotlinx Serialization
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class in.iambhvsh.shelf.**$$serializer { *; }
-keepclassmembers class in.iambhvsh.shelf.** {
    *** Companion;
}
-keepclasseswithmembers class in.iambhvsh.shelf.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Missing classes
-dontwarn com.google.re2j.Matcher
-dontwarn com.google.re2j.Pattern

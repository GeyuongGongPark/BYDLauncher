# Add project specific ProGuard rules here.

# Hilt
-keepclassmembers,allowobfuscation class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

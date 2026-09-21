# Add project specific ProGuard rules here.

# Hilt
-keepclassmembers,allowobfuscation class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# R8: JUnit Platform Console (테스트 전용 클래스, 릴리즈 빌드에서 제외)
-dontwarn java.util.spi.ToolProvider

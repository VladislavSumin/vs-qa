-dontobfuscate
-dontoptimize

-keep class ru.vladislavsumin.qa.MainKt { *; }
-keep class org.apache.logging.log4j.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class com.arkivanov.decompose.** { *; }
-keep class org.jetbrains.** { *; }

-dontwarn org.apache.logging.log4j.**
-dontwarn androidx.compose.foundation.internal.**
-dontwarn com.android.tools.r8.**
-dontwarn kotlinx.coroutines.**

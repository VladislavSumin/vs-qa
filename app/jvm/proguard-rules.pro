-dontobfuscate
-dontoptimize

-keep class ru.vladislavsumin.qa.MainKt { *; }

# Классы Sqlite базы данных, там много на рефлексии, не трогаем их
-keep class androidx.sqlite.driver.bundled.** { *; }

-keep class com.arkivanov.decompose.extensions.compose.mainthread.SwingMainThreadChecker { *;}
-keep class kotlinx.coroutines.swing.SwingDispatcherFactory { *; }

# Тут много натива, не ясно как лучше вызовы фильровать
-keep class org.jetbrains.skia.** { *; }
-keep class org.jetbrains.skiko.** { *; }

-keep class org.apache.logging.log4j.** { *; }

-dontwarn org.apache.logging.log4j.**
-dontwarn com.android.tools.r8.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn android.annotation.SuppressLint
-dontwarn com.jetbrains.JBR
# ---- Crash reporting: keep line numbers, hide original file names ----
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- kotlinx.serialization (DTOs + type-safe navigation routes) ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class ai.reslt.kmpready.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class ai.reslt.kmpready.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- Ktor / OkHttp / coroutines: optional JVM-only references ----
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---- Strip verbose logging from release bytecode ----
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

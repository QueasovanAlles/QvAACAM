# Project specific ProGuard rules for QVAACAM

# Keep source file and line numbers for better crash reporting
-keepattributes SourceFile,LineNumberTable

# WebView + JavaScript interface configuration
-keepclassmembers class com.qva.qvaacam.AndroidInterface {
    public *;
}

# Model classes preservation
-keep class com.qvaacam.model.** { *; }

# WebRTC components
-keep class org.webrtc.** { *; }

# Angular/Web interface components
-keep class com.qvaacam.web.** { *; }
-keep class com.qvaacam.interface.** { *; }

# Keep important Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

# Preserve native methods
-keepclasseswithmembernames class * {
    native <methods>;
}


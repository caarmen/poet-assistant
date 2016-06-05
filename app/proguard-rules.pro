# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/calvarez/dev/20141018/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-dontobfuscate
-optimizations !code/allocation/variable
-keepattributes Signature,*Annotation*

# Support library:
# http://stackoverflow.com/questions/25660793/android-searchview-does-not-work
-keep class android.support.v7.widget.SearchView {
   public <init>(android.content.Context);
   public <init>(android.content.Context, android.util.AttributeSet);
}

# Eventbus:
# http://greenrobot.org/eventbus/documentation/proguard/
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# streamsupport:
-dontwarn java8.util.**

# retrolambda:
-dontwarn java.lang.invoke.*

# Remove logging:
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}



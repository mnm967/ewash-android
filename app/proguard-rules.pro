# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keepattributes Signature
-keepattributes *Annotation*

# This rule will properly ProGuard all the model classes in
# the package com.yourcompany.models. Modify to fit the structure
# of your app.
-keepclassmembers class com.mnm.ewash.models.** {
  *;
}
-keep class com.google.android.gms.** {*;}
-dontwarn com.google.android.gms**
-keep class com.takisoft.**
-dontwarn com.takisoft.**
-dontwarn com.crashlytics.**
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-keep class javax.** { *; }
-keep class com.sun.** { *; }
-dontwarn com.sun.**
-dontwarn javax.**
-dontwarn *
-verbose
-optimizationpasses 3
# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class org.sparcs.soap.App.Models.** { *; }
-keep class org.sparcs.soap.App.Networking.** { *; }
-keep interface org.sparcs.soap.App.Networking.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes KotlinMetadata
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-keep class retrofit2.Response { *; }
-keep interface retrofit2.Call { *; }
-keep class kotlin.coroutines.Continuation { *; }

# Crashlytics
-keepattributes SourceFile, LineNumberTable, *Annotation*
-dontwarn com.google.firebase.crashlytics.**

# Hilt
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Enums
-keep class org.sparcs.soap.App.Domain.** { *; }
-keepclassmembers enum org.sparcs.soap.App.Domain.Enums.** { *; }

# Widget
-keep class * extends android.appwidget.AppWidgetProvider { *; }
-keep class * extends android.widget.RemoteViewsService { *; }
-keep class org.sparcs.soap.Widget.** { *; }

# Kakao Map
-keep class com.kakao.vectormap.** { *; }
-keep interface com.kakao.vectormap.**
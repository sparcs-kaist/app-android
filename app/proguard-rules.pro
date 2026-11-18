# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
# DTO field names are part of the wire format. Some DTOs live beside the API interfaces.
-keep class org.sparcs.soap.app.networking.requestDTO.** { *; }
-keep class org.sparcs.soap.app.networking.responseDTO.** { *; }
-keep class org.sparcs.soap.app.networking.retrofitAPI.** { *; }

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

# Domain models are also serialized in navigation arguments and on-disk caches.
-keep class org.sparcs.soap.app.domain.models.** { *; }
-keepclassmembers enum org.sparcs.soap.app.domain.enums.** { *; }

# Widget
-keep class * extends android.appwidget.AppWidgetProvider { *; }
-keep class * extends android.widget.RemoteViewsService { *; }
-keep class org.sparcs.soap.Widget.** { *; }

# Kakao Map
-keep class com.kakao.vectormap.** { *; }
-keep interface com.kakao.vectormap.**

# ChannelTalk ships consumer rules for its plugin, serializers, and shaded libraries.

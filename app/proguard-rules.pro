# Keep WebView JS interface
-keepclassmembers class com.example.tvapp.js.JsBridge {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Room entities
-keep class com.example.tvapp.data.** { *; }

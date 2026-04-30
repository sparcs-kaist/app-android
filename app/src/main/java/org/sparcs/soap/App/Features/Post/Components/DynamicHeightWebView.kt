package org.sparcs.soap.App.Features.Post.Components

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.sparcs.soap.App.Domain.Helpers.TokenStorage
import org.sparcs.soap.App.theme.ui.isDark
import kotlin.math.abs

@Composable
fun DynamicHeightWebView(
    url: String,
    modifier: Modifier = Modifier,
    onHeightChanged: (Int) -> Unit,
    onLinkTapped: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val tokenStorage = remember { TokenStorage(context) }
    
    val accessToken = remember { tokenStorage.getAccessToken() }
    val isDark = MaterialTheme.colorScheme.isDark()
    
    val textColor = if (isDark) "#e0e0e0" else "#1a1a1a"
    val linkColor = if (isDark) "#80bfff" else "#0066cc"
    
    var currentHeight by remember { mutableStateOf(0) }
    var heightReportCount by remember { mutableStateOf(0) }
    var isFitted by remember { mutableStateOf(false) }

    val webAppInterface = remember {
        object {
            @JavascriptInterface
            fun postMessage(heightStr: String) {
                val height = heightStr.toDoubleOrNull()?.toInt() ?: 0
                
                if (height > 0 && Math.abs(height - currentHeight) > 10) {
                    Handler(Looper.getMainLooper()).post {
                        if (height > 0 && Math.abs(height - currentHeight) > 10) {
                            currentHeight = height
                            onHeightChanged(height)
                        }
                    }
                }
            }
        }
    }

    val applyThemeJs = """
        (function() {
          var id = '__ara_theme_style__';
          var existing = document.getElementById(id);
          if (existing) existing.remove();
          var s = document.createElement('style');
          s.id = id;
          s.textContent = `
            * {
              animation: none !important;
              transition: none !important;
            }
            html, body, body * {
              background: transparent !important;
              background-color: transparent !important;
            }
            body, body * {
              color: $textColor !important;
            }
            a, a * { color: $linkColor !important; }
            img { max-width: 100% !important; height: auto !important; }
          `;
          document.head.appendChild(s);
        })();
    """.trimIndent()

    val updatePageHeightJs = """
        Math.max(
          document.body.scrollHeight || 0,
          document.documentElement.scrollHeight || 0,
          document.body.offsetHeight || 0,
          document.documentElement.offsetHeight || 0
        )
    """.trimIndent()

    val injectObserverJs = """
        (function() {
          if (window._araResizeObserverInstalled) return;
          window._araResizeObserverInstalled = true;
          var lastHeight = 0;
          function reportHeight() {
            var h = Math.max(
              document.body.scrollHeight || 0,
              document.documentElement.scrollHeight || 0,
              document.body.offsetHeight || 0,
              document.documentElement.offsetHeight || 0
            );
            if (h !== lastHeight && h > 0) {
              lastHeight = h;
              if (window.HeightChannel && window.HeightChannel.postMessage) {
                window.HeightChannel.postMessage(h.toString());
              }
            }
          }
          if (typeof ResizeObserver !== 'undefined') {
            new ResizeObserver(function() { reportHeight(); }).observe(document.body);
          }
          new MutationObserver(function() { reportHeight(); }).observe(
            document.body, { childList: true, subtree: true, attributes: true }
          );
          var pollCount = 0;
          var pollInterval = setInterval(function() {
            reportHeight();
            pollCount++;
            if (pollCount > 20) clearInterval(pollInterval);
          }, 500);
          reportHeight();
        })();
    """.trimIndent()

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                
                setBackgroundColor(Color.TRANSPARENT)
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                overScrollMode = View.OVER_SCROLL_NEVER
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                
                addJavascriptInterface(webAppInterface, "HeightChannel")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (isFitted) return
                        isFitted = true
                        
                        view?.evaluateJavascript(applyThemeJs, null)
                        
                        view?.evaluateJavascript(updatePageHeightJs) { result ->
                            val h = result?.toDoubleOrNull()?.toInt() ?: 0
                            if (h > 0 && abs(h - currentHeight) > 10) {
                                currentHeight = h
                                onHeightChanged(h)
                            }
                        }
                        
                        view?.evaluateJavascript(injectObserverJs, null)
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val requestUrl = request?.url?.toString() ?: ""
                        if (request?.isForMainFrame == true) {
                            if (requestUrl == url) return false
                            if (requestUrl.contains("/web_view") || requestUrl.contains("/login")) {
                                return false
                            }
                            onLinkTapped?.invoke(requestUrl)
                            return true
                        }
                        return super.shouldOverrideUrlLoading(view, request)
                    }
                }

                accessToken?.let { token ->
                    val extraHeaders = mapOf("Authorization" to "Bearer $token")
                    loadUrl(url, extraHeaders)
                }
            }
        },
        update = { webView ->
            webView.evaluateJavascript(applyThemeJs, null)
            
            if (webView.url != url && accessToken != null) {
                isFitted = false
                currentHeight = 0
                heightReportCount = 0
                val extraHeaders = mapOf("Authorization" to "Bearer $accessToken")
                webView.loadUrl(url, extraHeaders)
            }
        },
        modifier = modifier
    )
}
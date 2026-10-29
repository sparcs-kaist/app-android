package org.sparcs.soap.app.features.post.components

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.sparcs.soap.app.theme.ui.Theme
import timber.log.Timber
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun DynamicHeightWebView(
    html: String,
    baseUrl: String,
    onLinkTapped: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var height by remember(html) { mutableIntStateOf(0) }
    val onLink by rememberUpdatedState(onLinkTapped)
    val colors = HtmlColors(
        text = MaterialTheme.colorScheme.onSurface,
        link = MaterialTheme.colorScheme.primary,
        rule = MaterialTheme.colorScheme.outlineVariant,
    )
    val document = remember(html, colors) { wrapHtml(html, colors, nonce = UUID.randomUUID().toString()) }

    Box(modifier.fillMaxWidth().height(maxOf(1, height).dp)) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    setBackgroundColor(Color.Transparent.toArgb())
                    overScrollMode = View.OVER_SCROLL_NEVER
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    settings.javaScriptEnabled = true
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.setSupportMultipleWindows(false)
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.domStorageEnabled = false
                    addJavascriptInterface(HeightBridge { contentHeight -> post { height = contentHeight } }, HEIGHT_BRIDGE)
                    webViewClient = PostWebViewClient(onLink = { onLink(it) })
                }
            },
            update = { webView ->
                if (webView.tag != document) {
                    webView.tag = document
                    webView.loadDataWithBaseURL(baseUrl, document, "text/html", "utf-8", null)
                }
            },
            onRelease = { webView ->
                webView.removeJavascriptInterface(HEIGHT_BRIDGE)
                webView.destroy()
            },
            modifier = Modifier.fillMaxWidth()
        )
        if (height == 0) {
            CircularProgressIndicator(Modifier.align(Alignment.Center).padding(16.dp))
        }
    }
}

private const val HEIGHT_BRIDGE = "soapHeight"
private const val MAX_CONTENT_HEIGHT = 200_000

private class HeightBridge(private val onHeight: (Int) -> Unit) {
    @JavascriptInterface
    fun report(height: Int) {
        if (height in 1..MAX_CONTENT_HEIGHT) onHeight(height)
    }
}

private class PostWebViewClient(private val onLink: (String) -> Unit) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if (request.hasGesture()) {
            val url = request.url
            when (url.scheme?.lowercase()) {
                "http", "https" -> onLink(url.toString())
                "mailto", "tel" -> openExternally(view, url)
            }
        }
        return true
    }

    private fun openExternally(view: WebView, url: Uri) {
        try {
            view.context.startActivity(Intent(Intent.ACTION_VIEW, url).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            Timber.e(e, "Failed to open $url")
        }
    }
}

private data class HtmlColors(val text: Color, val link: Color, val rule: Color)

private fun Color.css(): String {
    val argb = toArgb()
    return "rgba(${(argb shr 16) and 0xFF}, ${(argb shr 8) and 0xFF}, ${argb and 0xFF}, ${(alpha * 100).roundToInt() / 100.0})"
}

private fun heightScript(nonce: String) = """
    <script nonce="$nonce">
      (function() {
        var content = document.getElementById('content');
        if (!content || !window.$HEIGHT_BRIDGE) return;
        var last = 0;
        function report() {
          var height = Math.ceil(content.getBoundingClientRect().height);
          if (height !== last) {
            last = height;
            window.$HEIGHT_BRIDGE.report(height);
          }
        }
        new ResizeObserver(report).observe(content);
        report();
      })();
    </script>
""".trimIndent()

private fun wrapHtml(html: String, colors: HtmlColors, nonce: String): String = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <meta http-equiv="Content-Security-Policy" content="default-src * data: blob:; script-src 'nonce-$nonce'; object-src 'none'; frame-src https:; child-src https:; form-action 'none'; base-uri 'none'">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <style>
        html, body { margin: 0; padding: 0; -webkit-text-size-adjust: 100%; }
        body { font-family: sans-serif; font-size: 16px; line-height: 1.45; overflow-wrap: anywhere; }
        html, body, body * { background: transparent !important; background-color: transparent !important; }
        body, body * { color: ${colors.text.css()} !important; }
        a, a * { color: ${colors.link.css()} !important; }
        #content > :first-child { margin-top: 0; }
        #content > :last-child { margin-bottom: 0; }
        p { margin: 0 0 0.8em; }
        img, video, iframe { max-width: 100% !important; height: auto !important; }
        img { border-radius: 8px; }
        pre { white-space: pre-wrap; font-family: monospace; }
        blockquote { margin: 0 0 0.8em; padding-left: 12px; border-left: 3px solid ${colors.rule.css()}; }
        table { display: block; max-width: 100%; overflow-x: auto; border-collapse: collapse; }
        td, th { border: 1px solid ${colors.rule.css()}; padding: 4px 6px; }
        hr { border: none; border-top: 1px solid ${colors.rule.css()}; }
      </style>
    </head>
    <body><div id="content">$html</div>${heightScript(nonce)}</body>
    </html>
""".trimIndent()

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        DynamicHeightWebView(
            html = "<p>포탈 공지 <b>본문</b>입니다.</p><p><a href=\"https://portal.kaist.ac.kr\">링크</a></p>",
            baseUrl = "https://portal.kaist.ac.kr/",
            onLinkTapped = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

package org.sparcs.App.Features.Post.Components

import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun DynamicHeightWebView(
    htmlString: String,
    modifier: Modifier = Modifier,
    onHeightChanged: (Int) -> Unit,
    onLinkTapped: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    val bgColor = MaterialTheme.colorScheme.background.toArgb()
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val linkColor = MaterialTheme.colorScheme.primary.toArgb()

    fun Int.toHex(): String =
        String.format("#%06X", 0xFFFFFF and this)

    val bgHex = bgColor.toHex()
    val textHex = textColor.toHex()
    val linkHex = linkColor.toHex()

    AndroidView(
        factory = {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                settings.javaScriptEnabled = true
                setBackgroundColor(bgColor)

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        evaluateJavascript(
                            "(document.body.scrollHeight) || (document.documentElement.scrollHeight)"
                        ) { result ->
                            result?.toIntOrNull()?.let { contentHeight ->
                                onHeightChanged(contentHeight)
                            }
                        }
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        request?.url?.toString()?.let { url ->
                            onLinkTapped?.invoke(url)
                            return true
                        }
                        return super.shouldOverrideUrlLoading(view, request)
                    }
                }
            }
        },
        update = { webView ->

            val fullHTML = """
                <html>
                <head>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <style>
                    html, body {
                      margin: 0;
                      padding: 0;
                      width: 100%;
                      font-family: sans-serif;
                      background-color: $bgHex;
                      color: $textHex;
                    }

                    a {
                      color: $linkHex;
                    }

                    img {
                      max-width: 100%;
                      height: auto;
                      display: block;
                    }

                    p {
                      margin: 0 0 1em;
                    }
                  </style>
                </head>
                <body>
                  $htmlString
                </body>
                </html>
            """.trimIndent()

            webView.setBackgroundColor(bgColor)

            webView.loadDataWithBaseURL(
                null,
                fullHTML,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = modifier
    )
}

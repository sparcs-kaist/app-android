package com.sparcs.soap.Features.Post.Components

import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

    AndroidView(
        factory = {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                settings.javaScriptEnabled = true
                setBackgroundColor(Color.TRANSPARENT)

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
                  <meta name="viewport" content="width=device-width, initial-scale=1.0, shrink-to-fit=no">
                  <style>
                    html, body {
                      margin: 0;
                      padding: 0;
                      width: 100%;
                      font-family: sans-serif;
                      background-color: #fff;
                      color: #000;
                    }
                    
                    @media (prefers-color-scheme: dark) {
                      html, body {
                        background-color: #000;
                        color: #fff;
                      }
                      a { color: #80bfff; }
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

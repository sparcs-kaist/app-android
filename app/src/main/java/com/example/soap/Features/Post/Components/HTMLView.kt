import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HTMLView(
    htmlString: String,
    onContentHeightChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        super.onPageFinished(view, url)
                        view.post {
                            val scaledHeight = (view.contentHeight)
                            onContentHeightChanged(scaledHeight)
                        }
                    }
                }

                loadDataWithBaseURL(
                    null,
                    wrapHtml(htmlString),
                    "text/html",
                    "utf-8",
                    null
                )
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                null,
                wrapHtml(htmlString),
                "text/html",
                "utf-8",
                null
            )
        }
    )
}

private fun wrapHtml(body: String): String {
    return """
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <style>
            html, body {
              margin: 0;
              padding: 0;
              width: 100%;
              font-size: 16px;
              color: #222222;
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
            }
            img {
              max-width: 100%;
              height: auto;
              display: block;
            }
          </style>
        </head>
        <body>
          $body
        </body>
        </html>
    """.trimIndent()
}

@Preview(showBackground = true)
@Composable
private fun HTMLViewPreview() {
    Column {
        Text("WebView")
        HTMLView(
            htmlString = "<p style='color:red;'>🔥 Compose + WebView Test<br><b>오샐러드 최고!</b></p>",
            onContentHeightChanged = { height ->
                Log.d("HTMLView", "Content height: $height px")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}

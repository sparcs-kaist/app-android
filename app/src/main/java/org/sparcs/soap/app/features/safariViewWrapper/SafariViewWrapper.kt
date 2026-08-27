package org.sparcs.soap.app.features.safariViewWrapper

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme


@Composable
fun SafariViewWrapper(url: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
        text = {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        },
        containerColor = MaterialTheme.colorScheme.background
        
    )
}

@Preview
@Composable
private fun Preview(){
    Theme {
        SafariViewWrapper("") {}
    }
}
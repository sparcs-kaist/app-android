package org.sparcs.Features.Settings.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.ui.theme.Theme

@Composable
fun ScrollableTextView(file: String) {
    val text = loadMarkdown(file)
    BodyText(text = text ?: "Failed to load content")
}

@Composable
fun BodyText(text: String) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun loadMarkdown(file: String): String? {
    val context = LocalContext.current
    return try {
        context.assets.open("$file.md").bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        null
    }
}

@Preview
@Composable
private fun ScrollableTextViewPreviewLoaded() {
    Theme { ScrollableTextView("taxi_privacy_policy") }
}

@Preview
@Composable
private fun ScrollableTextViewPreviewError() {
   Theme { ScrollableTextView("non_existing_file") }
}

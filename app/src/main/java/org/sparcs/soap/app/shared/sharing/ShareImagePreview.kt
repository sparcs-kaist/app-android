package org.sparcs.soap.app.shared.sharing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

@Composable
fun ShareImagePreview(widthDp: Int = 390, heightDp: Int = 844, background: Brush? = null, content: @Composable () -> Unit) {
    Layout(
        modifier = Modifier.fillMaxWidth().height(280.dp).then(if (background != null) Modifier.background(background) else Modifier),
        content = { CompositionLocalProvider(LocalDensity provides Density(3f, 1f), content = content) },
    ) { measurables, constraints ->
        val imageWidth = widthDp * 3
        val imageHeight = heightDp * 3
        val child = measurables.single().measure(Constraints.fixed(imageWidth, imageHeight))
        val scale = minOf(constraints.maxWidth / imageWidth.toFloat(), constraints.maxHeight / imageHeight.toFloat())
        layout(constraints.maxWidth, constraints.maxHeight) {
            child.placeWithLayer(((constraints.maxWidth - imageWidth * scale) / 2).toInt(), 0) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShareImagePreviewPreview() {
    ShareImagePreview { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer)) }
}

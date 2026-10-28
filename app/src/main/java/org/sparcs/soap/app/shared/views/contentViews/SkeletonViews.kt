package org.sparcs.soap.app.shared.views.contentViews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

// Building blocks for loading placeholders. Skeletons mirror the layout they stand in for, using
// the same text styles and paddings, so nothing jumps when the content loads.

/** A bar as tall as the font, centered in a box one line of [style] tall. */
@Composable
fun SkeletonLine(style: TextStyle, modifier: Modifier) {
    val density = LocalDensity.current
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier.height(with(density) { style.lineHeight.toDp() })
    ) {
        SkeletonBlock(
            Modifier
                .fillMaxWidth()
                .height(with(density) { style.fontSize.toDp() })
        )
    }
}

/** A placeholder shape for an icon, chip or field. */
@Composable
fun SkeletonBlock(modifier: Modifier, shape: Shape = RoundedCornerShape(4.dp)) {
    // Tinted rather than a container color, so it shows both on the page and on a card.
    Box(modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), shape))
}

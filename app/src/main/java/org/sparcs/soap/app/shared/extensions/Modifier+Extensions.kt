package org.sparcs.soap.app.shared.extensions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import org.sparcs.soap.app.domain.enums.Event
import org.sparcs.soap.app.features.main.LocalAnalytics

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember {
            MutableInteractionSource()
        }) {
        onClick()
    }
}

fun Modifier.analyticsScreen(
    name: String,
    vararg extraParams: Pair<String, Any>
): Modifier = composed {
    val analytics = LocalAnalytics.current
    LaunchedEffect(Unit) {
        analytics.logEvent(object : Event {
            override val source: String = name
            override val name: String = "screen_view"
            override val parameters: Map<String, Any> = mapOf(
                "screen_name" to name,
                "screen_class" to name
            ) + extraParams.toMap()
        })
    }
    this
}

@Composable
fun Modifier.adaptiveIconSize(
    textStyle: TextStyle = LocalTextStyle.current,
    scaleFactor: Float = 1.2f
): Modifier {
    val density = LocalDensity.current
    val iconSize = with(density) {
        (textStyle.fontSize.value * scaleFactor).sp.toDp()
    }
    return this.size(iconSize)
}

fun Modifier.bleedHorizontally(amount: Dp): Modifier = layout { measurable, constraints ->
    if (!constraints.hasBoundedWidth) {
        val placeable = measurable.measure(constraints)
        return@layout layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
    val extra = amount.roundToPx()
    val width = constraints.maxWidth + extra * 2
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, placeable.height) { placeable.place(-extra, 0) }
}

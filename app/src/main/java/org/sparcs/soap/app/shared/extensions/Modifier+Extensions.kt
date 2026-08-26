package org.sparcs.soap.app.shared.extensions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

fun Modifier.glassBorder(
    shape: Shape,
    shadowElevation: Dp = 1.dp,
    borderWidth: Dp = 1.dp,
    alphaTop: Float = 0.3f,
    alphaBottom: Float = 0.03f
) = this
    .shadow(
        elevation = shadowElevation,
        shape = shape
    )
    .border(
        border = BorderStroke(
            width = borderWidth,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = alphaTop),
                    Color.White.copy(alpha = alphaBottom)
                )
            )
        ),
        shape = shape
    )
package org.sparcs.soap.App.Shared.Extensions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import org.sparcs.soap.App.Domain.Enums.Event
import org.sparcs.soap.App.Features.Main.LocalAnalytics

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
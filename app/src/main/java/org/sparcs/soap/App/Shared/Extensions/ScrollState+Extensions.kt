package org.sparcs.soap.App.Shared.Extensions

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ScrollState.elevation(): Dp {
    val elevation by animateDpAsState(
        if (value > 0) 4.dp else 0.dp
    )
    return elevation
}

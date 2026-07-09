package org.sparcs.soap.app.shared.extensions

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ScrollState.elevation(): Dp {
    val elevation by animateDpAsState(
        if (value > 0) 4.dp else 0.dp
    )
    return elevation
}

@Composable
fun LazyListState.elevation(): Dp {
    val isScrolled by remember {
        derivedStateOf { firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0 }
    }
    val elevation by animateDpAsState(
        if (isScrolled) 4.dp else 0.dp
    )
    return elevation
}

package org.sparcs.soap.app.shared.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection

// Edge-to-edge scrolling: a Scaffold's padding includes the gesture bar's inset at the bottom.
// Padding a scrolling container by all of it stops the content above the gesture bar and leaves a
// blank band under the handle. Instead pad the container by [withoutBottom] and hand [onlyBottom]
// to the list as content padding, so content scrolls behind the translucent gesture bar and the
// last item still ends clear of it.

/** This padding without its bottom edge, for a container whose content scrolls behind the gesture bar. */
@Composable
fun PaddingValues.withoutBottom(): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection),
        top = calculateTopPadding(),
        end = calculateEndPadding(layoutDirection),
    )
}

/** Only the bottom edge of this padding, typically the gesture bar's inset, for a list's content padding. */
fun PaddingValues.onlyBottom(): PaddingValues = PaddingValues(bottom = calculateBottomPadding())

/** Both paddings added edge by edge, e.g. a list's own content padding plus [onlyBottom]. */
@Composable
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + other.calculateStartPadding(layoutDirection),
        top = calculateTopPadding() + other.calculateTopPadding(),
        end = calculateEndPadding(layoutDirection) + other.calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + other.calculateBottomPadding(),
    )
}

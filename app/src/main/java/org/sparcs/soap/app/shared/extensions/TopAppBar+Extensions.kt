package org.sparcs.soap.app.shared.extensions

import android.content.res.Configuration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Hide-on-scroll behavior for top app bars, enabled only in landscape where
 * vertical space is scarce. Returns null in portrait so the bar stays pinned.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun landscapeHideOnScrollBehavior(): TopAppBarScrollBehavior? =
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    } else {
        null
    }

/**
 * Connects the scaffold content's nested scroll to [scrollBehavior] when present.
 */
@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.hideTopBarOnScroll(scrollBehavior: TopAppBarScrollBehavior?): Modifier =
    if (scrollBehavior != null) nestedScroll(scrollBehavior.nestedScrollConnection) else this

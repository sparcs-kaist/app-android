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
 * Scroll behavior for top app bars. The bar stays pinned in portrait and switches to its
 * scrolled container colour once content scrolls beneath it; in landscape, where vertical
 * space is scarce, it hides on scroll instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun topAppBarScrollBehavior(): TopAppBarScrollBehavior =
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    } else {
        TopAppBarDefaults.pinnedScrollBehavior()
    }

/**
 * Connects the scaffold content's nested scroll to [scrollBehavior] when present.
 */
@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.hideTopBarOnScroll(scrollBehavior: TopAppBarScrollBehavior?): Modifier =
    if (scrollBehavior != null) nestedScroll(scrollBehavior.nestedScrollConnection) else this

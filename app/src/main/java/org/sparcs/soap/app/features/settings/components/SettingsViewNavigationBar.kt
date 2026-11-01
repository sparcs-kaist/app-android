@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package org.sparcs.soap.app.features.settings.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.features.navigationBar.components.BackButton
import org.sparcs.soap.app.features.navigationBar.components.SearchButton

/**
 * Top app bar for settings screens.
 *
 * @param flexible when true, renders a [MediumFlexibleTopAppBar] whose large title collapses as
 *   the content scrolls. Pair it with [TopAppBarDefaults.exitUntilCollapsedScrollBehavior].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsViewNavigationBar(
    title: String,
    onDismiss: () -> Unit,
    isSearchEnabled: Boolean? = false,
    onClickSearch: () -> Unit = {},
    isSelected: Boolean = false,
    isEditable: Boolean? = false,
    isDoneEnabled: Boolean? = false,
    isSaving: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.background,
    onClickDone: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    flexible: Boolean = false,
) {
    val navigationIcon: @Composable () -> Unit = { BackButton(onClick = { onDismiss() }) }
    val titleContent: @Composable () -> Unit = {
        Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = containerColor
    )
    val actions: @Composable RowScope.() -> Unit = {
        if (isSearchEnabled == true) {
            SearchButton(
                onClick = { onClickSearch() },
                isSelected = isSelected
            )
        } else if (isEditable == true) {
            DoneButton(
                onDoneClick = { onClickDone() },
                isDoneEnabled = isDoneEnabled ?: false,
                isSaving = isSaving
            )
        }
    }

    if (flexible) {
        MediumFlexibleTopAppBar(
            navigationIcon = navigationIcon,
            title = titleContent,
            colors = colors,
            actions = actions,
            scrollBehavior = scrollBehavior
        )
    } else {
        TopAppBar(
            navigationIcon = navigationIcon,
            title = titleContent,
            colors = colors,
            actions = actions,
            scrollBehavior = scrollBehavior
        )
    }
}


@Composable
private fun DoneButton(
    isDoneEnabled: Boolean,
    isSaving: Boolean,
    onDoneClick: () -> Unit,
) {
    TextButton(
        onClick = {
            onDoneClick()
        },
        shapes = ButtonDefaults.shapes(),
        enabled = isDoneEnabled,
        modifier = Modifier.semantics { contentDescription = "Setting Button" }
    ) {
        if (isSaving) {
            LoadingIndicator(modifier = Modifier.size(24.dp))
        } else {
            Text(text = stringResource(R.string.done))
        }
    }
}

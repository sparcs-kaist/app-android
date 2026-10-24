package org.sparcs.soap.app.features.search.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.sparcs.soap.app.features.navigationBar.Channel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchNavigationBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = { Text(text = stringResource(Channel.SearchView.title)) },
        scrollBehavior = scrollBehavior
    )
}
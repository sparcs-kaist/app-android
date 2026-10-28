package org.sparcs.soap.app.features.boardList.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.theme.ui.Theme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardListNavigationBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        // Bold, as on every tab's root screen.
        title = { Text(text = stringResource(Channel.Boards.title), fontWeight = FontWeight.Bold) },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun Preview() {
    Theme {
        BoardListNavigationBar()
    }
}
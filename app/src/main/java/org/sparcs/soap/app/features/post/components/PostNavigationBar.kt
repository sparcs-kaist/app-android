package org.sparcs.soap.app.features.post.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.app.features.navigationBar.components.BackButton
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostNavigationBar(
    boardGroup: String,
    onClick: () -> Unit,
    menuState: PostMenuState,
    menuActions: PostMenuActions,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        navigationIcon = { BackButton(onClick = onClick) },
        title = {
            Text(
                text = boardGroup,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = { PostActionsMenu(state = menuState, actions = menuActions) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun Preview() {
    Theme {
        PostNavigationBar(
            boardGroup = "Board",
            onClick = {},
            menuState = PostMenuState(isMine = false),
            menuActions = PostMenuActions(),
        )
    }
}

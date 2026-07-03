package org.sparcs.soap.app.features.boardList.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.shared.extensions.elevation
import org.sparcs.soap.app.theme.ui.Theme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardListNavigationBar(
    scrollState: ScrollState,
) {
    TopAppBar(
        title = {
            Row(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = stringResource(Channel.Boards.title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.shadow(scrollState.elevation())
    )
}

@Composable
@Preview
private fun Preview() {
    Theme {
        BoardListNavigationBar(
            scrollState = ScrollState(0),
        )
    }
}
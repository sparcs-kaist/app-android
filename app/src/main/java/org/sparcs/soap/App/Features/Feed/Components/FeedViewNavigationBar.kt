package org.sparcs.soap.App.Features.Feed.Components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.sparcs.soap.App.Features.Feed.FeedViewModelProtocol
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.NavigationBar.Components.AddButton
import org.sparcs.soap.App.Shared.Extensions.elevation
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.BuddyPreviewSupport.Feed.PreviewFeedViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedViewNavigationBar(
    scrollState: ScrollState,
    viewModel: FeedViewModelProtocol,
    navController: NavController,
) {
    TopAppBar(
        title = {
            Row(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = stringResource(Channel.Start.title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            AddButton(
                contentDescription = "Create Feed",
                onClick = {
                    viewModel.writeFeedButtonTapped()
                    navController.navigate(Channel.FeedPostCompose.name)
                }
            )
            FeedViewDropDownMenu(
                onClickSettings = {
                    viewModel.openSettingsTapped()
                    navController.navigate(Channel.Settings.name)
                }
            )
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
        FeedViewNavigationBar(
            scrollState = ScrollState(0),
            navController = NavController(LocalContext.current),
            viewModel = PreviewFeedViewModel()
        )
    }
}
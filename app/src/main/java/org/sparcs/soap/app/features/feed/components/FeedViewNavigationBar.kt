package org.sparcs.soap.app.features.feed.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import org.sparcs.soap.app.features.feed.FeedViewModelProtocol
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.feed.PreviewFeedViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedViewNavigationBar(
    viewModel: FeedViewModelProtocol,
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = { Text(text = stringResource(Channel.Start.title)) },
        actions = {
            FeedViewDropDownMenu(
                onClickSettings = {
                    viewModel.openSettingsTapped()
                    navController.navigate(Channel.Settings.name)
                }
            )
        },
        scrollBehavior = scrollBehavior
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun Preview() {
    Theme {
        FeedViewNavigationBar(
            navController = NavController(LocalContext.current),
            viewModel = PreviewFeedViewModel()
        )
    }
}
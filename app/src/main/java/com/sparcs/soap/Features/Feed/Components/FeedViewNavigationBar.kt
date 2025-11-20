package com.sparcs.soap.Features.Feed.Components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Row
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
import androidx.navigation.NavController
import com.sparcs.soap.Features.Home.Components.HomeViewDropDownMenu
import com.sparcs.soap.Features.NavigationBar.Channel
import com.sparcs.soap.Features.NavigationBar.Components.AddButton
import com.sparcs.soap.Shared.Extensions.elevation
import com.sparcs.soap.ui.theme.Theme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedViewNavigationBar(
    scrollState: ScrollState,
    navController: NavController,
) {
    TopAppBar(
        title = {
            Row {
                Text(
                    text = stringResource(Channel.Start.title),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            AddButton(
                contentDescription = "Create Feed",
                onClick = { navController.navigate(Channel.FeedPostCompose.name) }
            )
            HomeViewDropDownMenu(
                onClickSettings = { navController.navigate(Channel.Settings.name) },
                onClickNotification = { }
            )
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
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
            navController = NavController(LocalContext.current)
        )
    }
}
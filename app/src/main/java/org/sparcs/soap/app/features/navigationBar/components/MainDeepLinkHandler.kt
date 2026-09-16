package org.sparcs.soap.app.features.navigationBar.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.DeepLink
import org.sparcs.soap.app.domain.enums.DeepLinkEventBus
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.navigationBar.MainTabBarViewModel

@Composable
fun MainDeepLinkHandler(
    viewModel: MainTabBarViewModel = hiltViewModel(),
    navController: NavHostController,
    onTabSelected: (Channel) -> Unit
) {
    LaunchedEffect(Unit) {
        DeepLinkEventBus.events.collect { deepLink ->
            when (deepLink) {
                is DeepLink.TaxiInvite -> {
                    onTabSelected(Channel.Taxi)
                    viewModel.resolveInvite(deepLink.code)
                }
                is DeepLink.AraPost -> {
                    onTabSelected(Channel.Boards)
                    viewModel.resolvePost(deepLink.id)
                }
                is DeepLink.Timetable -> {
                    onTabSelected(Channel.TimeTable)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { route ->
            if (route.startsWith(Channel.TimeTable.name)) {
                onTabSelected(Channel.TimeTable)
            } else if (route.startsWith(Channel.Taxi.name)) {
                onTabSelected(Channel.Taxi)
            } else if (route.startsWith(Channel.Boards.name) || route.startsWith(Channel.PostView.name)) {
                onTabSelected(Channel.Boards)
            }

            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    if (viewModel.isAlertPresented) {
        AlertDialog(
            onDismissRequest = { viewModel.isAlertPresented = false },
            title = { Text(stringResource(viewModel.alertState?.titleResId ?: R.string.error)) },
            text = { Text(stringResource(viewModel.alertState?.messageResId ?: R.string.post_not_found_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.isAlertPresented = false }) { Text(stringResource(R.string.ok)) }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}

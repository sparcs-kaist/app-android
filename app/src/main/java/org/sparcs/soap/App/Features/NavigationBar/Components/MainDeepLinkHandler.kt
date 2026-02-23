package org.sparcs.soap.App.Features.NavigationBar.Components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import org.sparcs.soap.App.Features.NavigationBar.MainTabBarViewModel
import org.sparcs.soap.R

@Composable
fun MainDeepLinkHandler(
    viewModel: MainTabBarViewModel = hiltViewModel(),
    navController: NavHostController
) {
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { route ->
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
                TextButton(onClick = { viewModel.isAlertPresented = false }) { Text("OK") }
            }
        )
    }
}
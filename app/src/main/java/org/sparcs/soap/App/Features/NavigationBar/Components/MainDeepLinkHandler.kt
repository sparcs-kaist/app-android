package org.sparcs.soap.App.Features.NavigationBar.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.DeepLink
import org.sparcs.soap.App.Domain.Enums.DeepLinkEventBus
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.NavigationBar.MainTabBarViewModel
import org.sparcs.soap.App.Features.TaxiPreview.TaxiPreviewView
import org.sparcs.soap.App.Features.TaxiPreview.TaxiPreviewViewModel
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDeepLinkHandler(
    viewModel: MainTabBarViewModel = hiltViewModel(),
    navController: NavHostController,
    onTabSelected: (Channel) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val taxiPreviewViewModel: TaxiPreviewViewModel = hiltViewModel()

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
            }
        }
    }

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
                TextButton(onClick = { viewModel.isAlertPresented = false }) { Text(stringResource(R.string.ok)) }
            }
        )
    }

    viewModel.invitedRoom?.let { room ->
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.invitedRoom = null
            },
            sheetState = sheetState,
            dragHandle = {
                Column {
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .padding(top = 4.dp)
                            .height(4.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.grayBB)
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            TaxiPreviewView(
                room = room,
                viewModel = taxiPreviewViewModel,
                onDismiss = {
                    coroutineScope.launch {
                        sheetState.hide()
                        viewModel.invitedRoom = null
                    }
                },
                navController = navController
            )
        }
    }
}
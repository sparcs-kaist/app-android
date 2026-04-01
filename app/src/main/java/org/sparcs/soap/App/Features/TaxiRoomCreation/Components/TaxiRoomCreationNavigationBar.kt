package org.sparcs.soap.App.Features.TaxiRoomCreation.Components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.Taxi.TaxiRoomBlockStatus
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Features.NavigationBar.Components.DismissButton
import org.sparcs.soap.App.Features.TaxiList.TaxiListViewModelProtocol
import org.sparcs.soap.App.Features.TaxiRoomCreation.TaxiRoomCreationViewModelProtocol
import org.sparcs.soap.App.Shared.Views.ContentViews.GlobalAlertDialog
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiRoomCreationNavigationBar(
    onDismiss: () -> Unit,
    isEnabled: Boolean,
    viewModel: TaxiListViewModelProtocol,
    taxiRoomCreationViewModel: TaxiRoomCreationViewModelProtocol,
    title: String,
) {
    CenterAlignedTopAppBar(
        navigationIcon = { DismissButton(onClick = onDismiss) },

        title = {
            Text(
                text = stringResource(R.string.new_room),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            SendButton(
                isEnabled = isEnabled,
                onClick = onDismiss,
                viewModel = viewModel,
                title = title
            )
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )

    LaunchedEffect(Unit) {
        taxiRoomCreationViewModel.fetchBlockStatus()
        when (val status = taxiRoomCreationViewModel.blockStatus.value) {
            is TaxiRoomBlockStatus.Error -> {
                viewModel.alertState = AlertState(message = status.errorMessage)
                viewModel.isAlertPresented = true
            }

            TaxiRoomBlockStatus.NotPaid -> {
                viewModel.alertState = AlertState(
                    titleResId = R.string.notice_board,
                    messageResId = R.string.not_paid_message
                )
                viewModel.isAlertPresented = true
            }

            TaxiRoomBlockStatus.TooManyRooms -> {
                viewModel.alertState = AlertState(
                    titleResId = R.string.notice_board,
                    messageResId = R.string.too_many_rooms_message
                )
                viewModel.isAlertPresented = true
            }

            else -> {}
        }
    }

    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        state = viewModel.alertState,
        onDismiss = { viewModel.isAlertPresented = false }
    )
}

@Composable
private fun SendButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    viewModel: TaxiListViewModelProtocol,
    title: String,
) {
    val coroutineScope = rememberCoroutineScope()
    TextButton(
        onClick = {
            coroutineScope.launch {
                val newRoomId = viewModel.createRoom(title)

                if (!newRoomId.isNullOrBlank()) {
                    viewModel.toggleCarrier(newRoomId, viewModel.roomHasCarrier)
                    viewModel.fetchData()
                    onClick()
                }
            }
        },
        enabled = isEnabled,
        modifier = Modifier.semantics { contentDescription = "Create Room Button" }
    ) {
        Text(
            text = stringResource(R.string.submit),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Normal,
            color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.grayBB
        )
    }
}


@Composable
@Preview
private fun Preview() {
    Theme { TaxiRoomCreationNavigationBar({}, false, viewModel(), viewModel(), "") }
}


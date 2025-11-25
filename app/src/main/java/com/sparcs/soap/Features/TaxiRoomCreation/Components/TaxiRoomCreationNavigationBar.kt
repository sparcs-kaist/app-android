package com.sparcs.soap.Features.TaxiRoomCreation.Components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sparcs.soap.Domain.Enums.Taxi.TaxiRoomBlockStatus
import com.sparcs.soap.Features.NavigationBar.Components.DismissButton
import com.sparcs.soap.Features.TaxiList.TaxiListViewModelProtocol
import com.sparcs.soap.Features.TaxiRoomCreation.TaxiRoomCreationViewModelProtocol
import com.sparcs.soap.R
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.grayBB
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiRoomCreationNavigationBar(
    onDismiss: () -> Unit,
    isEnabled: Boolean,
    viewModel: TaxiListViewModelProtocol,
    taxiRoomCreationViewModel: TaxiRoomCreationViewModelProtocol,
    title: String
    ) {
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var alertTitle by remember { mutableStateOf("") }

    val error = stringResource(R.string.error)
    val notice = stringResource(R.string.notice_board)
    val notPaid = stringResource(R.string.not_paid_message)
    val tooManyRooms = stringResource(R.string.too_many_rooms_message)
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
                onError = {
                    alertMessage = it
                    showAlert = true
                },
                viewModel = viewModel,
                title = title
            )
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text((alertTitle)) },
            text = { Text(alertMessage) },
            confirmButton = {
                TextButton(onClick = { showAlert = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
    LaunchedEffect(Unit) {
        taxiRoomCreationViewModel.fetchBlockStatus()
        when (val status = taxiRoomCreationViewModel.blockStatus.value) {
            is TaxiRoomBlockStatus.Error -> {
                alertTitle = error 
                alertMessage = status.errorMessage
                showAlert = true
            }
            TaxiRoomBlockStatus.NotPaid -> {
                alertTitle = notice
                alertMessage = notPaid
                showAlert = true
            }
            TaxiRoomBlockStatus.TooManyRooms -> {
                alertTitle = notice
                alertMessage = tooManyRooms
                showAlert = true
            }
            else -> {}
        }
    }

}

@Composable
private fun SendButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    onError: (String) -> Unit,
    viewModel: TaxiListViewModelProtocol,
    title: String
) {
    val coroutineScope = rememberCoroutineScope()
    TextButton(
        onClick = {
            coroutineScope.launch {
                try {
                     viewModel.createRoom(title)
                     viewModel.fetchData()
                    onClick()
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Unknown error")

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
private fun Preview(){
    Theme { TaxiRoomCreationNavigationBar({}, false, viewModel(), viewModel(),"") }
}


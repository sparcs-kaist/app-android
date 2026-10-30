package org.sparcs.soap.app.features.taxiRoomCreation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.taxi.TaxiRoomBlockStatus
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.taxiList.TaxiFilterCard
import org.sparcs.soap.app.features.taxiList.TaxiListViewModel
import org.sparcs.soap.app.features.taxiList.TaxiListViewModelProtocol
import org.sparcs.soap.app.features.taxiRoomCreation.components.TaxiCapacityPicker
import org.sparcs.soap.app.features.taxiRoomCreation.components.TaxiCarrierToggleButton
import org.sparcs.soap.app.features.taxiRoomCreation.components.TaxiDepartureTimePicker
import org.sparcs.soap.app.features.taxiRoomCreation.components.TaxiRoomCreationNavigationBar
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.viewModelMocks.taxi.MockTaxiRoomCreationViewModel
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.taxi.PreviewTaxiListViewModel
import java.util.Date

@Composable
fun TaxiRoomCreationView(
    navController: NavController,
    taxiListViewModel: TaxiListViewModelProtocol = hiltViewModel<TaxiListViewModel>(),
    taxiRoomCreationViewModel: TaxiRoomCreationViewModelProtocol = hiltViewModel<TaxiRoomCreationViewModel>(),
) {
    var title by remember { mutableStateOf("") }
    val randomRoomName = remember { Constants.taxiDefaultRoomNames.random() }

    val locations by taxiListViewModel.locations.collectAsState()
    val favoriteRoutes by taxiListViewModel.favoriteRoutes.collectAsState()

    val (isEnabled, validationMessage) = remember(
        title,
        taxiListViewModel.source,
        taxiListViewModel.destination,
        taxiListViewModel.roomDepartureTime,
    ) {
        isValid(taxiListViewModel, taxiRoomCreationViewModel, title)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TaxiRoomCreationNavigationBar(
                onDismiss = {
                    title = ""
                    taxiListViewModel.source = null
                    taxiListViewModel.destination = null
                    navController.navigate(Channel.Taxi.name) {
                        launchSingleTop = true
                    }
                },
                isEnabled = isEnabled,
                viewModel = taxiListViewModel,
                taxiRoomCreationViewModel = taxiRoomCreationViewModel,
                title = title.ifBlank { randomRoomName }
            )
        },
        modifier = Modifier.analyticsScreen("Taxi Room Creation")
    ) { innerPadding ->

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            TaxiFilterCard(
                viewModel = taxiListViewModel,
                locations = locations,
                favoriteRoutes = favoriteRoutes
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.title)) },
                placeholder = { Text(randomRoomName) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            TaxiDepartureTimePicker(
                departureTime = taxiListViewModel.roomDepartureTime,
                onDepartureTimeChange = { taxiListViewModel.roomDepartureTime = it }
            )

            TaxiCapacityPicker(
                capacity = taxiListViewModel.roomCapacity,
                onCapacityChange = { taxiListViewModel.roomCapacity = it }
            )

            TaxiCarrierToggleButton(
                hasCarrier = taxiListViewModel.roomHasCarrier,
                onToggle = { taxiListViewModel.roomHasCarrier = it }
            )

            if (!isEnabled && validationMessage != null) {
                Text(
                    text = stringResource(validationMessage),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

private fun isTitleValid(title: String): Boolean {
    val regex = Constants.taxiRoomNameRegex
    return regex.matches(title)
}

private fun isValid(
    viewModel: TaxiListViewModelProtocol,
    roomCreationViewModel: TaxiRoomCreationViewModelProtocol,
    title: String,
): Pair<Boolean, Int?> {
    val blockStatus = roomCreationViewModel.blockStatus.value
    if (blockStatus == TaxiRoomBlockStatus.TooManyRooms) return false to R.string.error_too_many_rooms
    if (blockStatus != TaxiRoomBlockStatus.Allow) return false to R.string.error_unsettled_room

    val source = viewModel.source
    val destination = viewModel.destination
    if (source == null || destination == null) return false to null

    if (source == destination) return false to R.string.error_source_equals_destination
    if (title.isNotBlank() && !isTitleValid(title)) return false to R.string.error_invalid_title
    if (viewModel.roomDepartureTime <= Date()) return false to R.string.error_departure_in_past

    return true to null
}

@Preview
@Composable
private fun Preview() {
    val loadedState = TaxiListViewModel.ViewState.Loaded(
        rooms = TaxiRoom.mockList(),
        locations = TaxiLocation.mockList()
    )
    val viewModel = PreviewTaxiListViewModel(initialState = loadedState)

    Theme {
        TaxiRoomCreationView(
            rememberNavController(),
            taxiListViewModel = viewModel,
            taxiRoomCreationViewModel = MockTaxiRoomCreationViewModel()
        )
    }
}
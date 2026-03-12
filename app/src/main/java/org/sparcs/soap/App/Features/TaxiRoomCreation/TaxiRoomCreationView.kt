package org.sparcs.soap.App.Features.TaxiRoomCreation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.App.Domain.Enums.Taxi.TaxiRoomBlockStatus
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiLocation
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.TaxiList.TaxiListViewModel
import org.sparcs.soap.App.Features.TaxiList.TaxiListViewModelProtocol
import org.sparcs.soap.App.Features.TaxiRoomCreation.Components.TaxiCapacityPicker
import org.sparcs.soap.App.Features.TaxiRoomCreation.Components.TaxiCarrierToggleButton
import org.sparcs.soap.App.Features.TaxiRoomCreation.Components.TaxiDepartureTimePicker
import org.sparcs.soap.App.Features.TaxiRoomCreation.Components.TaxiDestinationPicker
import org.sparcs.soap.App.Features.TaxiRoomCreation.Components.TaxiRoomCreationNavigationBar
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Mocks.mockList
import org.sparcs.soap.App.Shared.ViewModelMocks.Taxi.MockTaxiRoomCreationViewModel
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.BuddyPreviewSupport.Taxi.PreviewTaxiListViewModel
import org.sparcs.soap.R
import java.util.Date

@Composable
fun TaxiRoomCreationView(
    navController: NavController,
    taxiListViewModel: TaxiListViewModelProtocol = hiltViewModel(),
    taxiRoomCreationViewModel: TaxiRoomCreationViewModelProtocol = hiltViewModel(),
) {
    var title by remember { mutableStateOf("") }
    val locations by taxiListViewModel.locations.collectAsState()
    val (isEnabled, validationMessage) = remember(
        title,
        taxiListViewModel.source,
        taxiListViewModel.destination,
        taxiListViewModel.roomDepartureTime,
    ) {
        isValid(taxiListViewModel, taxiRoomCreationViewModel, title)
    }

    Scaffold(
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
                title = title
            )
        },
        modifier = Modifier.analyticsScreen("Taxi Room Creation")
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                TaxiDestinationPicker(
                    source = taxiListViewModel.source,
                    destination = taxiListViewModel.destination,
                    locations = locations,
                    onSourceChange = { newSource ->
                        taxiListViewModel.source = newSource
                    },
                    onDestinationChange = { newDestination ->
                        taxiListViewModel.destination = newDestination
                    }
                )
            }

            Spacer(Modifier.padding(16.dp))

            Card(
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) {
                            Text(
                                text = stringResource(R.string.title),
                                color = MaterialTheme.colorScheme.grayBB,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(Modifier.padding(16.dp))

            Card(
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    TaxiDepartureTimePicker(
                        departureTime = taxiListViewModel.roomDepartureTime,
                        onDepartureTimeChange = { departureTime ->
                            taxiListViewModel.roomDepartureTime = departureTime
                        }
                    )

                    HorizontalDivider(Modifier.padding(vertical = 16.dp))

                    TaxiCapacityPicker(
                        capacity = taxiListViewModel.roomCapacity,
                        onCapacityChange = { capacity ->
                            taxiListViewModel.roomCapacity = capacity
                        }
                    )

                    HorizontalDivider(Modifier.padding(vertical = 16.dp))

                    TaxiCarrierToggleButton(
                        hasCarrier = taxiListViewModel.roomHasCarrier,
                        onToggle = { newValue ->
                            taxiListViewModel.roomHasCarrier = newValue
                        }
                    )
                }
            }

            Spacer(Modifier.padding(16.dp))

            if(!isEnabled && validationMessage !== null) {
                Card(
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(validationMessage),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
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
    val source = viewModel.source
    val destination = viewModel.destination

    if (source == null) return false to null
    if (destination == null) return false to null
    if (source == destination) return false to R.string.error_source_equals_destination
    if (title.isBlank()) return false to null
    if (!isTitleValid(title)) return false to R.string.error_invalid_title
    if (viewModel.roomDepartureTime <= Date()) return false to R.string.error_departure_in_past
    if (roomCreationViewModel.blockStatus.value == TaxiRoomBlockStatus.TooManyRooms) return false to R.string.error_too_many_rooms
    if (roomCreationViewModel.blockStatus.value != TaxiRoomBlockStatus.Allow) return false to R.string.error_unsettled_room
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
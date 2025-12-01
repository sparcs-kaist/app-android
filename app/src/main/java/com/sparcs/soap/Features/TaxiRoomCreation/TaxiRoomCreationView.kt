package com.sparcs.soap.Features.TaxiRoomCreation

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
import com.sparcs.soap.Domain.Enums.Taxi.TaxiRoomBlockStatus
import com.sparcs.soap.Domain.Helpers.Constants
import com.sparcs.soap.Domain.Models.Taxi.TaxiLocation
import com.sparcs.soap.Domain.Models.Taxi.TaxiRoom
import com.sparcs.soap.Features.NavigationBar.Channel
import com.sparcs.soap.Features.TaxiList.TaxiListViewModel
import com.sparcs.soap.Features.TaxiList.TaxiListViewModelProtocol
import com.sparcs.soap.Features.TaxiRoomCreation.Components.TaxiCapacityPicker
import com.sparcs.soap.Features.TaxiRoomCreation.Components.TaxiDepartureTimePicker
import com.sparcs.soap.Features.TaxiRoomCreation.Components.TaxiDestinationPicker
import com.sparcs.soap.Features.TaxiRoomCreation.Components.TaxiRoomCreationNavigationBar
import com.sparcs.soap.R
import com.sparcs.soap.Shared.Mocks.mockList
import com.sparcs.soap.Shared.ViewModelMocks.Taxi.MockTaxiListViewModel
import com.sparcs.soap.Shared.ViewModelMocks.Taxi.MockTaxiRoomCreationViewModel
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.grayBB
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
        }
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
                    textStyle = MaterialTheme.typography.bodyLarge,
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
    if (roomCreationViewModel.blockStatus.value != TaxiRoomBlockStatus.Allow) return false to R.string.error_unsettled_room

    return true to null
}

@Preview
@Composable
private fun Preview() {
    val mockViewModel =
        remember { MockTaxiListViewModel(initialState = TaxiListViewModel.ViewState.Loaded(TaxiRoom.mockList(), TaxiLocation.mockList())) }

    Theme {
        TaxiRoomCreationView(
            rememberNavController(),
            taxiListViewModel = mockViewModel,
            taxiRoomCreationViewModel = MockTaxiRoomCreationViewModel()
        )
    }
}
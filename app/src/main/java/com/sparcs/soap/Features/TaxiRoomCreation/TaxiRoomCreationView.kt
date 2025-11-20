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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sparcs.soap.Domain.Enums.TaxiRoomBlockStatus
import com.sparcs.soap.Domain.Helpers.Constants
import com.sparcs.soap.Features.NavigationBar.Channel
import com.sparcs.soap.Features.TaxiList.TaxiListViewModel
import com.sparcs.soap.Features.TaxiList.TaxiListViewModelProtocol
import com.sparcs.soap.Features.TaxiRoomCreation.Components.TaxiCapacityPicker
import com.sparcs.soap.Features.TaxiRoomCreation.Components.TaxiDepartureTimePicker
import com.sparcs.soap.Features.TaxiRoomCreation.Components.TaxiDestinationPicker
import com.sparcs.soap.Features.TaxiRoomCreation.Components.TaxiRoomCreationNavigationBar
import com.sparcs.soap.Shared.ViewModel.MockTaxiListViewModel
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.grayBB
import java.util.Date

@Composable
fun TaxiRoomCreationView(
    navController: NavController,
    taxiListViewModel: TaxiListViewModelProtocol = hiltViewModel(),
    taxiRoomCreationViewModel: TaxiRoomCreationViewModel = hiltViewModel(),
) {
    var title by remember { mutableStateOf("") }

    val locations by taxiListViewModel.locations.collectAsState()
    val isEnabled = remember(
        title,
        taxiListViewModel.source,
        taxiListViewModel.destination,
        taxiListViewModel.roomDepartureTime
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
                                text = "Title",
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
        }
    }
}

private fun isTitleValid(title: String): Boolean {
    val regex = Constants.taxiRoomNameRegex
    return regex.matches(title)
}

private fun isValid(
    viewModel: TaxiListViewModelProtocol,
    roomCreationViewModel: TaxiRoomCreationViewModel,
    title: String,
): Boolean {
    val source = viewModel.source
    val destination = viewModel.destination
    return source != null &&
            destination != null &&
            source != destination &&
            title.isNotBlank() &&
            isTitleValid(title) &&
            viewModel.roomDepartureTime > Date() &&
            roomCreationViewModel.blockStatus.value == TaxiRoomBlockStatus.Allow
}

@Preview
@Composable
private fun Preview() {
    val mockViewModel =
        remember { MockTaxiListViewModel(initialState = TaxiListViewModel.ViewState.Loading) }

    Theme {
        TaxiRoomCreationView(
            rememberNavController(),
            taxiListViewModel = mockViewModel
        )
    }
}
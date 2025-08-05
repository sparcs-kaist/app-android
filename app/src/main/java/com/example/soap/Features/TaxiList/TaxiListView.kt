package com.example.soap.Features.TaxiList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Features.NavigationBar.AppBar
import com.example.soap.Features.NavigationBar.AppDownBar
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.TaxiList.Components.WeekDaySelector
import com.example.soap.Features.TaxiPreview.TaxiPreviewView
import com.example.soap.Features.TaxiRoomCreation.Components.TaxiDestinationPicker
import com.example.soap.R
import com.example.soap.Shared.Extensions.isDateInSameDay
import com.example.soap.Shared.Extensions.weekdaySymbol
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.Shared.ViewModel.MockTaxiListViewModel
import com.example.soap.Shared.Views.TaxiRoomCell.TaxiRoomCell
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiListView(
    viewModel: TaxiListViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.state.collectAsState()
    var selectedDate by remember { mutableStateOf(viewModel.selectedDate) }
    var showRoomCreation by remember { mutableStateOf(true) }
    var selectedRoom by remember { mutableStateOf<TaxiRoom?>(null) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            AppBar(
                currentScreen = Channel.Taxi,
                scrollOffset = scrollState.value,
                navController = navController,
                isButtonEnabled = showRoomCreation
            )
        },

        bottomBar = {
            AppDownBar(
                navController = navController,
                currentScreen = Channel.Taxi
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.soapColors.background)
                .verticalScroll(scrollState)
                .padding(innerPadding)
        ) {
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                Card(
                    colors = CardDefaults.cardColors(MaterialTheme.soapColors.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    TaxiDestinationPicker(
                        source = viewModel.source,
                        destination = viewModel.destination,
                        locations = viewModel.locations,
                        onSourceChange = { newSource ->
                            viewModel.source = newSource
                        },
                        onDestinationChange = { newDestination ->
                            viewModel.destination = newDestination
                        }
                    )
                }

                Spacer(Modifier.padding(8.dp))

                WeekDaySelector(
                    week = viewModel.week,
                    selectedDate = selectedDate,
                    onSelect = { newDate ->
                        selectedDate = newDate
                        viewModel.selectedDate = newDate
                    }
                )

            }

            Spacer(Modifier.padding(16.dp))

            when (uiState) {
                is TaxiListViewModel.ViewState.Loading -> {
                    LoadingView()
                }

                is TaxiListViewModel.ViewState.Loaded -> {
                    LoadedView(
                        rooms = (uiState as TaxiListViewModel.ViewState.Loaded).rooms,
                        locations = (uiState as TaxiListViewModel.ViewState.Loaded).locations,
                        week = viewModel.week,
                        source = viewModel.source,
                        destination = viewModel.destination,
                        onRoomSelected = { selectedRoom = it }
                    )
                }

                is TaxiListViewModel.ViewState.Empty -> {
                    EmptyResultView(locations = viewModel.locations)
                }

                is TaxiListViewModel.ViewState.Error -> {
                    ErrorView(
                        errorMessage = (uiState as TaxiListViewModel.ViewState.Error).message,
                        onRetry = {}
                    )
                }
            }
        }
    }

    selectedRoom?.let { room ->
        ModalBottomSheet(
            onDismissRequest = {
                selectedRoom = null
                coroutineScope.launch {
                    viewModel.fetchData()
                }
            },
            sheetState = sheetState,
            dragHandle = {
                Column{
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .padding(top = 4.dp)
                            .height(4.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.soapColors.grayBB)
                    )
                }
            },
            containerColor = MaterialTheme.soapColors.surface
        ) {
            TaxiPreviewView(
                room = room,
                onDismiss = {
                    coroutineScope.launch {
                        sheetState.hide()
                        selectedRoom = null
                        viewModel.fetchData()
                    }
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }
}

@Composable
private fun LoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(
            text = "Loading...",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun LoadedView(
    rooms: List<TaxiRoom>,
    locations: List<TaxiLocation>,
    week: List<Date>,
    source: TaxiLocation?,
    destination: TaxiLocation?,
    onRoomSelected: (TaxiRoom) -> Unit
) {
    val calendar = Calendar.getInstance()
    val filteredRooms = rooms.filter { room ->
        val matchesSource = source == null || room.source.id == source.id
        val matchesDestination = destination == null || room.destination.id == destination.id
        matchesSource && matchesDestination
    }

    Column {
        if (filteredRooms.isEmpty()) {
            EmptyResultView(locations)
        } else {
            week.forEach { day ->
                val roomsForDay = filteredRooms.filter { room ->
                    calendar.isDateInSameDay(room.departAt, day)
                }

                if (roomsForDay.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = day.weekdaySymbol(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        roomsForDay.forEach { room ->
                            TaxiRoomCell(
                                room = room,
                                onClick = { onRoomSelected(room) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorView(errorMessage: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Error",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onRetry() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Try Again")
        }
    }
}

@Composable
private fun EmptyResultView(locations: List<TaxiLocation>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.taxi),
            contentDescription = "No Rides This Week",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Rides This Week",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Looks like there are no rooms scheduled for this week. Be the first to create one!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /*Create */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create a New Room")
        }
    }
}



@Composable
@Preview
private fun TaxiListScreenLoadingPreview() {
    SoapTheme{ MockTaxiListScreen(TaxiListViewModel.ViewState.Loading) }
}

@Composable
@Preview
private fun TaxiListScreenLoadedPreview() {
    SoapTheme{
        MockTaxiListScreen(
            TaxiListViewModel.ViewState.Loaded(
                rooms = TaxiRoom.mockList(),
                locations = TaxiLocation.mockList()
            )
        )
    }
}

@Composable
@Preview
private fun TaxiListScreenEmptyPreview() {
    SoapTheme{ MockTaxiListScreen(TaxiListViewModel.ViewState.Empty(TaxiLocation.mockList())) }
}

@Composable
@Preview
private fun TaxiListScreenErrorPreview() {
    SoapTheme{ MockTaxiListScreen(TaxiListViewModel.ViewState.Error("Something went wrong")) }
}

@Composable
fun MockTaxiListScreen(state: TaxiListViewModel.ViewState) {
    val mockViewModel = remember { MockTaxiListViewModel(initialState = state) }
    TaxiListView(viewModel = mockViewModel, navController = NavController(LocalContext.current))
}

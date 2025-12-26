package org.sparcs.Features.TaxiList

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.Domain.Models.Taxi.TaxiLocation
import org.sparcs.Domain.Models.Taxi.TaxiRoom
import org.sparcs.Features.NavigationBar.AppDownBar
import org.sparcs.Features.NavigationBar.Channel
import org.sparcs.Features.TaxiList.Components.TaxiListNavigationBar
import org.sparcs.Features.TaxiList.Components.WeekDaySelector
import org.sparcs.Features.TaxiPreview.TaxiPreviewView
import org.sparcs.Features.TaxiPreview.TaxiPreviewViewModelProtocol
import org.sparcs.Features.TaxiRoomCreation.Components.TaxiDestinationPicker
import org.sparcs.R
import org.sparcs.Shared.Extensions.isDateInSameDay
import org.sparcs.Shared.Extensions.weekdaySymbol
import org.sparcs.Shared.Mocks.mockList
import org.sparcs.Shared.ViewModelMocks.Taxi.MockTaxiListViewModel
import org.sparcs.Shared.ViewModelMocks.Taxi.MockTaxiPreviewViewModel
import org.sparcs.Shared.Views.ContentViews.ErrorView
import org.sparcs.Shared.Views.TaxiRoomCell.TaxiRoomCell
import org.sparcs.Shared.Views.TaxiRoomCell.TaxiRoomSkeletonCell
import org.sparcs.ui.theme.Theme
import org.sparcs.ui.theme.grayBB
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiListView(
    viewModel: TaxiListViewModelProtocol = hiltViewModel(),
    taxiPreviewViewModel: TaxiPreviewViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val uiState by viewModel.state.collectAsState()
    var selectedDate = viewModel.selectedDate

    val showRoomCreation by remember { mutableStateOf(true) }
    val locations by viewModel.locations.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    var selectedRoom by remember { mutableStateOf<TaxiRoom?>(null) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TaxiListNavigationBar(
                scrollState = scrollState,
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    viewModel.fetchData()
                    isRefreshing = false
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(scrollState)
                    .padding(innerPadding)
            ) {
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxSize()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        TaxiDestinationPicker(
                            source = viewModel.source,
                            destination = viewModel.destination,
                            locations = locations,
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
                            viewModel.selectedDate = selectedDate
                        }
                    )
                }

                Spacer(Modifier.padding(16.dp))

                when (uiState) {
                    is TaxiListViewModel.ViewState.Loading -> {
                        LoadingView()
                    }

                    is TaxiListViewModel.ViewState.Loaded -> {
                        if (viewModel.roomId != null) {
                            selectedRoom = (uiState as TaxiListViewModel.ViewState.Loaded).rooms.find { it.id == viewModel.roomId }
                        }

                        LoadedView(
                            rooms = (uiState as TaxiListViewModel.ViewState.Loaded).rooms,
                            week = viewModel.week,
                            selectedDate = selectedDate,
                            source = viewModel.source,
                            destination = viewModel.destination,
                            onRoomSelected = { selectedRoom = it },
                            viewModel = viewModel,
                            navController = navController
                        )
                    }

                    is TaxiListViewModel.ViewState.Empty -> {
                        EmptyView(navController = navController)
                    }

                    is TaxiListViewModel.ViewState.Error -> {
                        ErrorView(
                            icon = Icons.Default.Warning,
                            errorMessage = (uiState as TaxiListViewModel.ViewState.Error).message,
                            onRetry = {
                                coroutineScope.launch {
                                    viewModel.fetchData()
                                }
                            }
                        )
                    }
                }
            }
        }

        selectedRoom?.let { room ->
            ModalBottomSheet(
                onDismissRequest = {
                    selectedRoom = null
                    viewModel.roomId = null
                    coroutineScope.launch {
                        viewModel.fetchData()
                    }
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
                        viewModel.roomId = null
                        selectedRoom = null
                        coroutineScope.launch {
                            sheetState.hide()
                            viewModel.fetchData()
                        }
                    },
                    navController = navController
                )
            }
        }

        LaunchedEffect(Unit) {
            viewModel.fetchData()
        }
    }
}

@Composable
private fun LoadingView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        repeat(2) {
            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .width(30.dp)
                    .height(15.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            repeat((1..2).random()) {
                TaxiRoomSkeletonCell()
                Spacer(Modifier.padding(4.dp))
            }
        }
    }
}

@Composable
private fun LoadedView(
    rooms: List<TaxiRoom>,
    week: List<Date>,
    selectedDate: Date?,
    source: TaxiLocation?,
    destination: TaxiLocation?,
    onRoomSelected: (TaxiRoom) -> Unit,
    viewModel: TaxiListViewModelProtocol,
    navController: NavController,
) {
    val calendar = Calendar.getInstance()
    val filteredRooms = rooms.filter { room ->
        val matchesSource = source == null || room.source.id == source.id
        val matchesDestination = destination == null || room.destination.id == destination.id
        matchesSource && matchesDestination
    }
    val targetDates = selectedDate?.let { listOf(it) } ?: week

    Column {
        if (filteredRooms.isEmpty()) {
            val description = if (selectedDate != null)
                stringResource(R.string.no_rooms_found_for_selected_week, selectedDate.weekdaySymbol())
            else
                stringResource(R.string.no_rooms_found)

            EmptyResultView(
                viewModel = viewModel,
                description = description,
                navController = navController
            )
        } else {
            targetDates.forEach { day ->
                val roomsForDay = filteredRooms.filter { room ->
                    calendar.isDateInSameDay(room.departAt, day)
                }

                val description: String = when {
                    viewModel.source != null && viewModel.destination != null ->
                        stringResource(
                            R.string.no_rooms_from_to,
                            viewModel.source?.title ?: "",
                            viewModel.destination?.title ?: "",
                            day.weekdaySymbol()
                        )
                    viewModel.source != null ->
                        stringResource(
                            R.string.no_rooms_from_any,
                            viewModel.source?.title ?: "",
                            day.weekdaySymbol()
                        )
                    viewModel.destination != null ->
                        stringResource(
                            R.string.no_rooms_to,
                            viewModel.destination?.title ?: "",
                            day.weekdaySymbol()
                        )
                    else ->
                        stringResource(
                            R.string.no_rooms_on,
                            day.weekdaySymbol()
                        )
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
                } else if (selectedDate != null) {
                    EmptyResultView(
                        viewModel = viewModel,
                        description = description,
                        navController = navController
                    )
                }
            }
        }
    }
}


@Composable
private fun EmptyView(navController: NavController) {
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
            text = stringResource(R.string.no_rides_this_week),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.no_rooms_this_week),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate(Channel.TaxiRoomCreation.name)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.create_a_new_group))
        }
    }
}

@Composable
private fun EmptyResultView(
    viewModel: TaxiListViewModelProtocol,
    description: String,
    navController: NavController,
) {
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
            text = stringResource(R.string.no_rides_this_week),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column {
            Button(
                onClick = {
                    navController.navigate(Channel.TaxiRoomCreation.name)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.create_a_new_group))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.source = null
                    viewModel.destination = null
                    viewModel.selectedDate = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.clear_selection))
            }
        }
    }
}


@Composable
@Preview
private fun TaxiListScreenLoadingPreview() {
    Theme { MockTaxiListScreen(TaxiListViewModel.ViewState.Loading) }
}

@Composable
@Preview
private fun TaxiListScreenLoadedPreview() {
    Theme {
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
private fun TaxiListScreenLoadedEmptyResultPreview() {
    Theme {
        MockTaxiListScreen(
            TaxiListViewModel.ViewState.Loaded(
                rooms = listOf(),
                locations = listOf()
            )
        )
    }
}

@Composable
@Preview
private fun TaxiListScreenEmptyPreview() {
    Theme { MockTaxiListScreen(TaxiListViewModel.ViewState.Empty(TaxiLocation.mockList())) }
}

@Composable
@Preview
private fun TaxiListScreenErrorPreview() {
    Theme { MockTaxiListScreen(TaxiListViewModel.ViewState.Error("Something went wrong")) }
}

@Composable
private fun MockTaxiListScreen(state: TaxiListViewModel.ViewState) {
    val mockViewModel = remember { MockTaxiListViewModel(initialState = state) }
    TaxiListView(viewModel = mockViewModel, taxiPreviewViewModel = MockTaxiPreviewViewModel(), navController = rememberNavController())
}

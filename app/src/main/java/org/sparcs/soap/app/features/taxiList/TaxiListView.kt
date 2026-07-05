package org.sparcs.soap.app.features.taxiList

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalTaxi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.LocalizedString
import org.sparcs.soap.app.domain.models.taxi.TaxiFavoriteRoute
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.navigationBar.AppDownBar
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.taxiList.components.TaxiListNavigationBar
import org.sparcs.soap.app.features.taxiList.components.WeekDaySelector
import org.sparcs.soap.app.features.taxiPreview.TaxiPreviewView
import org.sparcs.soap.app.features.taxiPreview.TaxiPreviewViewModel
import org.sparcs.soap.app.features.taxiPreview.TaxiPreviewViewModelProtocol
import org.sparcs.soap.app.features.taxiRoomCreation.components.TaxiDestinationPicker
import org.sparcs.soap.app.shared.extensions.PullToRefreshHapticHandler
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.extensions.isDateInSameDay
import org.sparcs.soap.app.shared.extensions.weekdaySymbol
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.taxiRoomCell.TaxiRoomCell
import org.sparcs.soap.app.shared.views.taxiRoomCell.TaxiRoomSkeletonCell
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.grayBB
import org.sparcs.soap.buddyPreviewSupport.taxi.PreviewTaxiListViewModel
import org.sparcs.soap.buddyPreviewSupport.taxi.PreviewTaxiPreviewViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiListView(
    viewModel: TaxiListViewModelProtocol = hiltViewModel<TaxiListViewModel>(),
    taxiPreviewViewModel: TaxiPreviewViewModelProtocol = hiltViewModel<TaxiPreviewViewModel>(),
    navController: NavController,
) {
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.state.collectAsState()
    val selectedDate = viewModel.selectedDate

    val locations by viewModel.locations.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    var selectedRoom by remember { mutableStateOf<TaxiRoom?>(null) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val pullState = rememberPullToRefreshState()

    val favoriteRoutes by viewModel.favoriteRoutes.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val description = getTaxiFilterDescription(
        sourceTitle = viewModel.source?.title,
        destinationTitle = viewModel.destination?.title,
        selectedDate = selectedDate
    )
    PullToRefreshHapticHandler(pullState, isRefreshing)

    Scaffold(
        topBar = {
            TaxiListNavigationBar(
                scrollState = scrollState,
                navController = navController,
                isButtonEnabled = true
            )
        },
        bottomBar = {
            AppDownBar(
                navController = navController,
                currentScreen = Channel.Taxi
            )
        },
        modifier = Modifier
            .navigationBarsPadding()
            .analyticsScreen("Taxi List")
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    viewModel.fetchData()
                    delay(500)
                    isRefreshing = false
                }
            },
            state = pullState,
            modifier = Modifier.padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                if (isLandscape) {
                    TaxiLandscapeLayout(
                        uiState = uiState,
                        viewModel = viewModel,
                        locations = locations,
                        favoriteRoutes = favoriteRoutes,
                        selectedDate = selectedDate,
                        description = description,
                        onRoomSelected = {
                            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            selectedRoom = it
                        },
                        navController = navController,
                        haptic = haptic,
                        scrollState = scrollState
                    )
                } else {
                    TaxiPortraitLayout(
                        uiState = uiState,
                        viewModel = viewModel,
                        locations = locations,
                        favoriteRoutes = favoriteRoutes,
                        selectedDate = selectedDate,
                        description = description,
                        onRoomSelected = {
                            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            selectedRoom = it
                        },
                        navController = navController,
                        haptic = haptic,
                        scrollState = scrollState
                    )
                }
            }
        }

        selectedRoom?.let { room ->
            ModalBottomSheet(
                onDismissRequest = {
                    selectedRoom = null
                    viewModel.roomId = null
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
                        }
                        viewModel.fetchData()
                    },
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun TaxiLandscapeLayout(
    uiState: TaxiListViewModel.ViewState,
    viewModel: TaxiListViewModelProtocol,
    locations: List<TaxiLocation>,
    favoriteRoutes: List<TaxiFavoriteRoute>,
    selectedDate: Date?,
    description: String,
    onRoomSelected: (TaxiRoom) -> Unit,
    navController: NavController,
    haptic: HapticFeedback,
    scrollState: ScrollState,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaxiFilterCard(viewModel, locations, favoriteRoutes)
            TaxiWeekSelectorCard(viewModel, selectedDate, haptic)
        }

        Column(
            modifier = Modifier
                .weight(1.5f)
                .verticalScroll(scrollState)
                .padding(end = 24.dp, top = 16.dp, bottom = 16.dp)
        ) {
            TaxiResultContent(
                uiState = uiState,
                viewModel = viewModel,
                selectedDate = selectedDate,
                description = description,
                onRoomSelected = onRoomSelected,
                navController = navController
            )
        }
    }
}

@Composable
private fun TaxiPortraitLayout(
    uiState: TaxiListViewModel.ViewState,
    viewModel: TaxiListViewModelProtocol,
    locations: List<TaxiLocation>,
    favoriteRoutes: List<TaxiFavoriteRoute>,
    selectedDate: Date?,
    description: String,
    onRoomSelected: (TaxiRoom) -> Unit,
    navController: NavController,
    haptic: HapticFeedback,
    scrollState: ScrollState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(scrollState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            TaxiFilterCard(viewModel, locations, favoriteRoutes)
            Spacer(modifier = Modifier.height(16.dp))
            TaxiWeekSelectorCard(viewModel, selectedDate, haptic)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TaxiResultContent(
            uiState = uiState,
            viewModel = viewModel,
            selectedDate = selectedDate,
            description = description,
            onRoomSelected = onRoomSelected,
            navController = navController
        )
    }
}

@Composable
private fun TaxiFilterCard(
    viewModel: TaxiListViewModelProtocol,
    locations: List<TaxiLocation>,
    favoriteRoutes: List<TaxiFavoriteRoute>,
) {
    Card(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.glassBorder(shape = RoundedCornerShape(16.dp))
    ) {
        TaxiDestinationPicker(
            source = viewModel.source,
            destination = viewModel.destination,
            locations = locations,
            onSourceChange = { viewModel.source = it },
            onDestinationChange = { viewModel.destination = it },
            favoriteRoutes = favoriteRoutes,
            onFavoriteSelect = { viewModel.selectFavoriteRoute(it) },
            onFavoriteDelete = { viewModel.deleteFavoriteRoute(it) },
            onFavoriteAdd = { viewModel.addFavoriteRoute() }
        )
    }
}

@Composable
private fun TaxiWeekSelectorCard(
    viewModel: TaxiListViewModelProtocol,
    selectedDate: Date?,
    haptic: HapticFeedback,
) {
    Card(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .glassBorder(shape = RoundedCornerShape(16.dp))
    ) {
        WeekDaySelector(
            week = viewModel.week,
            selectedDate = selectedDate,
            onSelect = { newDate ->
                viewModel.selectedDate = newDate
                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            }
        )
    }
}

@Composable
private fun TaxiResultContent(
    uiState: TaxiListViewModel.ViewState,
    viewModel: TaxiListViewModelProtocol,
    selectedDate: Date?,
    description: String,
    onRoomSelected: (TaxiRoom) -> Unit,
    navController: NavController,
) {
    when (uiState) {
        is TaxiListViewModel.ViewState.Loading -> LoadingView()
        is TaxiListViewModel.ViewState.Loaded -> {
            val rooms = uiState.rooms
            if (rooms.isEmpty()) {
                EmptyResultView(
                    description = description,
                    navController = navController,
                    onClear = {
                        viewModel.source = null
                        viewModel.destination = null
                        viewModel.selectedDate = null
                    }
                )
            } else {
                LoadedView(
                    rooms = rooms,
                    week = viewModel.week,
                    selectedDate = selectedDate,
                    source = viewModel.source,
                    destination = viewModel.destination,
                    onRoomSelected = onRoomSelected,
                    viewModel = viewModel,
                    description = description,
                    navController = navController
                )
            }
        }

        is TaxiListViewModel.ViewState.Empty -> EmptyView(navController = navController)
        is TaxiListViewModel.ViewState.Error -> {
            ErrorView(
                error = uiState.error,
                onRetry = { viewModel.fetchData() }
            )
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
                    .padding(start = 12.dp, top = 12.dp)
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
    description: String,
    navController: NavController,
) {
    val filteredRooms = rooms.filter { room ->
        val matchesSource = source == null || room.source.id == source.id
        val matchesDestination = destination == null || room.destination.id == destination.id
        matchesSource && matchesDestination
    }
    val targetDates = selectedDate?.let { listOf(it) } ?: week

    Column {
        if (filteredRooms.isEmpty()) {
            EmptyResultView(
                description = description,
                navController = navController,
                onClear = {
                    viewModel.source = null
                    viewModel.destination = null
                    viewModel.selectedDate = null
                }
            )
        } else {
            targetDates.forEach { day ->
                val roomsForDay = filteredRooms.filter { room ->
                    isDateInSameDay(room.departAt, day)
                }

                val dayDescription = getTaxiFilterDescription(
                    sourceTitle = viewModel.source?.title,
                    destinationTitle = viewModel.destination?.title,
                    selectedDate = day
                )

                if (roomsForDay.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        description = dayDescription,
                        navController = navController,
                        onClear = {
                            viewModel.source = null
                            viewModel.destination = null
                            viewModel.selectedDate = null
                        }
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
            imageVector = Icons.Rounded.LocalTaxi,
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
            modifier = Modifier.fillMaxWidth(0.6f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.create_a_new_group))
        }
    }
}

@Composable
private fun EmptyResultView(
    description: String,
    navController: NavController,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalTaxi,
            contentDescription = "No Rides This Week",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.no_rides_found),
            style = MaterialTheme.typography.titleMedium,
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
                modifier = Modifier.fillMaxWidth(0.6f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.create_a_new_group))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.fillMaxWidth(0.6f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.clear_selection))
            }
        }
    }
}

@Composable
private fun getTaxiFilterDescription(
    sourceTitle: LocalizedString?,
    destinationTitle: LocalizedString?,
    selectedDate: Date?,
): String {
    val daySymbol = selectedDate?.weekdaySymbol()
    val src = sourceTitle?.toString()
    val dest = destinationTitle?.toString()

    return when {
        src != null && dest != null -> {
            if (daySymbol != null) stringResource(R.string.no_rooms_from_to, src, dest, daySymbol)
            else stringResource(R.string.taxi_no_rooms_from_to, src, dest)
        }

        src != null -> {
            if (daySymbol != null) stringResource(R.string.no_rooms_from_any, src, daySymbol)
            else stringResource(R.string.taxi_no_rooms_from_any, src)
        }

        dest != null -> {
            if (daySymbol != null) stringResource(R.string.no_rooms_to, dest, daySymbol)
            else stringResource(R.string.taxi_no_rooms_to, dest)
        }

        daySymbol != null -> {
            stringResource(R.string.no_rooms_on, daySymbol)
        }

        else -> {
            stringResource(R.string.taxi_no_rooms_this_week)
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
@Preview(widthDp = 840, heightDp = 480)
private fun TaxiListScreenLoadedLandscapePreview() {
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
    Theme { MockTaxiListScreen(TaxiListViewModel.ViewState.Error(Exception())) }
}

@Composable
private fun MockTaxiListScreen(state: TaxiListViewModel.ViewState) {
    val mockViewModel = remember { PreviewTaxiListViewModel(initialState = state) }
    TaxiListView(
        viewModel = mockViewModel,
        taxiPreviewViewModel = PreviewTaxiPreviewViewModel(),
        navController = rememberNavController()
    )
}

package org.sparcs.soap.app.features.taxiChatList

import android.content.res.Configuration
import android.net.Uri
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.taxiChatList.components.TaxiChatListViewNavigationBar
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.UnavailableView
import org.sparcs.soap.app.shared.views.taxiRoomCell.TaxiRoomCell
import org.sparcs.soap.app.shared.views.taxiRoomCell.TaxiRoomSkeletonCell
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.taxi.PreviewTaxiChatListViewModel

@Composable
fun TaxiChatListView(
    viewModel: TaxiChatListViewModelProtocol = hiltViewModel<TaxiChatListViewModel>(),
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    Scaffold(
        topBar = {
            TaxiChatListViewNavigationBar { navController.popBackStack() }
        },
        modifier = Modifier.analyticsScreen("Taxi Chat List")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding)
        ) {
            if (isLandscape) {
                TaxiChatListLandscapeLayout(state, viewModel, navController)
            } else {
                TaxiChatListPortraitLayout(state, viewModel, navController)
            }
        }
    }
}

@Composable
private fun TaxiChatListLandscapeLayout(
    state: TaxiChatListViewModel.ViewState,
    viewModel: TaxiChatListViewModelProtocol,
    navController: NavController,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val onRoomClick: (TaxiRoom) -> Unit = { room ->
            val json = Uri.encode(Gson().toJson(room))
            navController.navigate(Channel.TaxiChatView.name + "?room_json=$json")
        }

        when (state) {
            is TaxiChatListViewModel.ViewState.Loading -> {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.active_groups),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    repeat(3) { TaxiRoomSkeletonCell(); Spacer(Modifier.height(4.dp)) }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.past_groups),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    repeat(2) { TaxiRoomSkeletonCell(); Spacer(Modifier.height(4.dp)) }
                }
            }

            is TaxiChatListViewModel.ViewState.Loaded -> {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.active_groups),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    if (state.onGoing.isEmpty()) {
                        UnavailableView(
                            icon = Icons.Outlined.ErrorOutline,
                            title = stringResource(R.string.no_result),
                            description = stringResource(R.string.no_active_groups)
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(state.onGoing) { room ->
                                TaxiRoomCell(
                                    room = room,
                                    taxiUser = viewModel.taxiUser,
                                    onClick = { onRoomClick(room) }
                                )
                            }
                        }
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.past_groups),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    if (state.done.isEmpty()) {
                        UnavailableView(
                            icon = Icons.Outlined.ErrorOutline,
                            title = stringResource(R.string.no_result),
                            description = stringResource(R.string.no_past_groups)
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(state.done) { room ->
                                TaxiRoomCell(
                                    room = room,
                                    taxiUser = viewModel.taxiUser,
                                    onClick = { onRoomClick(room) }
                                )
                            }
                        }
                    }
                }
            }

            is TaxiChatListViewModel.ViewState.Error -> {
                ErrorView(
                    error = state.error,
                    onRetry = { viewModel.fetchData() }
                )
            }
        }
    }
}

@Composable
private fun TaxiChatListPortraitLayout(
    state: TaxiChatListViewModel.ViewState,
    viewModel: TaxiChatListViewModelProtocol,
    navController: NavController,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state) {
            is TaxiChatListViewModel.ViewState.Loading -> {
                LoadingView()
            }

            is TaxiChatListViewModel.ViewState.Loaded -> {
                LoadedView(
                    viewModel = viewModel,
                    onGoing = state.onGoing,
                    done = state.done,
                    onRoomClick = { room ->
                        val json = Uri.encode(Gson().toJson(room))
                        navController.navigate(Channel.TaxiChatView.name + "?room_json=$json")
                    }
                )
            }

            is TaxiChatListViewModel.ViewState.Error -> {
                ErrorView(
                    error = state.error,
                    onRetry = { viewModel.fetchData() }
                )
            }
        }
    }
}

@Composable
private fun LoadingView() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.active_groups),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        repeat(4) {
            item {
                TaxiRoomSkeletonCell()
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.past_groups),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        repeat(2) {
            item {
                TaxiRoomSkeletonCell()
            }
        }
    }
}

@Composable
fun LoadedView(
    viewModel: TaxiChatListViewModelProtocol,
    onGoing: List<TaxiRoom>,
    done: List<TaxiRoom>,
    onRoomClick: (TaxiRoom) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.active_groups),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            if (onGoing.isEmpty()) {
                UnavailableView(
                    icon = Icons.Outlined.ErrorOutline,
                    title = stringResource(R.string.no_result),
                    description = stringResource(R.string.no_active_groups)
                )
            }
        }

        items(onGoing) { room ->
            TaxiRoomCell(
                room = room,
                onClick = { onRoomClick(room) },
                taxiUser = viewModel.taxiUser
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.past_groups),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            if (done.isEmpty()) {
                UnavailableView(
                    icon = Icons.Outlined.ErrorOutline,
                    title = stringResource(R.string.no_result),
                    description = stringResource(R.string.no_past_groups)
                )
            }
        }

        items(done) { room ->
            TaxiRoomCell(
                room = room,
                onClick = { onRoomClick(room) },
                taxiUser = viewModel.taxiUser
            )
        }
    }
}


@Preview
@Composable
private fun LoadingPreview() {
    Theme {
        TaxiChatListView(
            PreviewTaxiChatListViewModel(TaxiChatListViewModel.ViewState.Loading),
            rememberNavController()
        )
    }
}

@Preview(widthDp = 840, heightDp = 480)
@Composable
private fun LoadedLandscapePreview() {
    Theme {
        TaxiChatListView(
            PreviewTaxiChatListViewModel(
                TaxiChatListViewModel.ViewState.Loaded(
                    TaxiRoom.mockList().subList(1, 4),
                    TaxiRoom.mockList().subList(5, 7)
                )
            ),
            rememberNavController()
        )
    }
}

@Preview
@Composable
private fun LoadedPreview() {
    Theme {
        TaxiChatListView(
            PreviewTaxiChatListViewModel(
                TaxiChatListViewModel.ViewState.Loaded(
                    TaxiRoom.mockList().subList(1, 4),
                    TaxiRoom.mockList().subList(5, 7)
                )
            ),
            rememberNavController()
        )
    }
}

@Preview
@Composable
private fun ErrorPreview() {
    Theme {
        TaxiChatListView(
            PreviewTaxiChatListViewModel(TaxiChatListViewModel.ViewState.Error(Exception())),
            rememberNavController()
        )
    }
}

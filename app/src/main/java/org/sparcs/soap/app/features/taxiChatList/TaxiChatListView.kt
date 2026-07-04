package org.sparcs.soap.app.features.taxiChatList

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.taxiChatList.components.TaxiChatListViewNavigationBar
import org.sparcs.soap.app.features.taxiChatList.components.TaxiChatRoomList
import org.sparcs.soap.app.features.taxiChatList.components.TaxiChatRoomListSkeleton
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.taxi.PreviewTaxiChatListViewModel

@Composable
fun TaxiChatListView(
    viewModel: TaxiChatListViewModelProtocol = hiltViewModel<TaxiChatListViewModel>(),
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()

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
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
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
                        TaxiChatRoomListSkeleton()
                    }

                    is TaxiChatListViewModel.ViewState.Loaded -> {
                        val loaded = state as TaxiChatListViewModel.ViewState.Loaded
                        TaxiChatRoomList(
                            onGoing = loaded.onGoing,
                            done = loaded.done,
                            taxiUser = viewModel.taxiUser,
                            onRoomClick = { room ->
                                val json = Uri.encode(Gson().toJson(room))
                                navController.navigate(Channel.TaxiChatView.name + "?room_json=$json")
                            }
                        )
                    }

                    is TaxiChatListViewModel.ViewState.Error -> {
                        val error = state as TaxiChatListViewModel.ViewState.Error
                        ErrorView(
                            error = error.error,
                            onRetry = {
                                viewModel.fetchData()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    Theme {
        TaxiChatListView(
            PreviewTaxiChatListViewModel(TaxiChatListViewModel.ViewState.Loading),
            rememberNavController()
        )
    }
}

@Preview(showBackground = true, widthDp = 840, heightDp = 480)
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

@Preview(showBackground = true)
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

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    Theme {
        TaxiChatListView(
            PreviewTaxiChatListViewModel(TaxiChatListViewModel.ViewState.Error(Exception())),
            rememberNavController()
        )
    }
}

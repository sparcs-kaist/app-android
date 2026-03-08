package org.sparcs.soap.App.Features.TaxiChatList

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.TaxiChatList.Components.TaxiChatListViewNavigationBar
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Mocks.mockList
import org.sparcs.soap.App.Shared.ViewModelMocks.Taxi.MockTaxiChatListViewModel
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.Shared.Views.ContentViews.UnavailableView
import org.sparcs.soap.App.Shared.Views.TaxiRoomCell.TaxiRoomCell
import org.sparcs.soap.App.Shared.Views.TaxiRoomCell.TaxiRoomSkeletonCell
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R

@Composable
fun TaxiChatListView(
    viewModel: TaxiChatListViewModelProtocol = hiltViewModel(),
    navController: NavController
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
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
        when (state) {
            is TaxiChatListViewModel.ViewState.Loading -> {
                LoadingView()
            }

            is TaxiChatListViewModel.ViewState.Loaded -> {
                val loaded = state as TaxiChatListViewModel.ViewState.Loaded
                LoadedView(
                    viewModel = viewModel,
                    onGoing = loaded.onGoing,
                    done = loaded.done,
                    onRoomClick = { room ->
                        val json = Uri.encode(Gson().toJson(room))
                        navController.navigate(Channel.TaxiChatView.name + "?room_json=$json")
                    }
                )
            }

            is TaxiChatListViewModel.ViewState.Error -> {
                val error = state as TaxiChatListViewModel.ViewState.Error
                ErrorView(
                    icon = Icons.Default.Warning,
                    message = error.message,
                    onRetry = {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.fetchData()
                        }
                    }
                )
            }
        }
    }

}
}

@Composable
private fun LoadingView() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
    onRoomClick: (TaxiRoom) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
            if(onGoing.isEmpty()){
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

        item{
            if(done.isEmpty()){
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
private fun LoadingPreview(){
    Theme {
        TaxiChatListView(
            MockTaxiChatListViewModel(TaxiChatListViewModel.ViewState.Loading),
            rememberNavController()
        )
    }
}

@Preview
@Composable
private fun LoadedPreview(){
    Theme {
        TaxiChatListView(
            MockTaxiChatListViewModel(
                TaxiChatListViewModel.ViewState.Loaded(
                    TaxiRoom.mockList().subList(1, 4),
                TaxiRoom.mockList().subList(5, 7))),
            rememberNavController()
        )
    }
}

@Preview
@Composable
private fun ErrorPreview(){
    Theme {
        TaxiChatListView(
            MockTaxiChatListViewModel(TaxiChatListViewModel.ViewState.Error("Something went wrong")),
            rememberNavController()
        )
    }
}
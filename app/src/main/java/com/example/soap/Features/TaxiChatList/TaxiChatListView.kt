package com.example.soap.Features.TaxiChatList

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.TaxiChatList.Components.TaxiChatListViewNavigationBar
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.Shared.ViewModelMocks.MockTaxiChatListViewModel
import com.example.soap.Shared.Views.ErrorView.ErrorView
import com.example.soap.Shared.Views.TaxiRoomCell.TaxiRoomCell
import com.example.soap.ui.theme.Theme
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            TaxiChatListViewNavigationBar { navController.navigate(Channel.Taxi.name) }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                    errorMessage = error.message,
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
private fun LoadingView(){
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
                    text = "Active Rooms",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(TaxiRoom.mockList().subList(1, 4)) { room ->
            TaxiRoomCell(
                room = room,
                onClick = {}
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Past Rooms",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(TaxiRoom.mockList().subList(5,7)) { room ->
            TaxiRoomCell(
                room = room,
                onClick = {}
            )
        }
    }
}

@Composable
fun LoadedView(
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
                    text = "Active Rooms",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(onGoing) { room ->
            TaxiRoomCell(
                room = room,
                onClick = { onRoomClick(room) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Past Rooms",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(done) { room ->
            TaxiRoomCell(
                room = room,
                onClick = { onRoomClick(room) }
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
            MockTaxiChatListViewModel(TaxiChatListViewModel.ViewState.Loaded(TaxiRoom.mockList().subList(1, 4),
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
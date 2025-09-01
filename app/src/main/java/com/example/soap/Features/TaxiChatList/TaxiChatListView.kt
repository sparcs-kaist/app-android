package com.example.soap.Features.TaxiChatList

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.Shared.ViewModelMocks.MockTaxiChatListViewModel
import com.example.soap.Shared.Views.TaxiRoomCell.TaxiRoomCell
import com.example.soap.ui.theme.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiChatListView(viewModel: TaxiChatListViewModelProtocol = hiltViewModel()) {
    var selectedRoom: TaxiRoom?
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") }
            )
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
                        //TODO- 방으로 navigate(selectedRoom)
                    }
                )
            }

            is TaxiChatListViewModel.ViewState.Error -> {
                val error = state as TaxiChatListViewModel.ViewState.Error
                ErrorView(errorMessage = error.message)
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


@Composable
private fun ErrorView(errorMessage: String){
//TODO - ERRORVIEW 통합하기
    //refresh - fetchdata
}

@Preview
@Composable
private fun LoadingPreview(){
    Theme {
        TaxiChatListView(MockTaxiChatListViewModel(TaxiChatListViewModel.ViewState.Loading))
    }
}

@Preview
@Composable
private fun LoadedPreview(){
    Theme {
        TaxiChatListView(MockTaxiChatListViewModel(TaxiChatListViewModel.ViewState.Loaded(TaxiRoom.mockList().subList(1, 4), TaxiRoom.mockList().subList(5, 7))))
    }
}

@Preview
@Composable
private fun ErrorPreview(){
    Theme {
        TaxiChatListView(MockTaxiChatListViewModel(TaxiChatListViewModel.ViewState.Error("Something went wrong")))
    }
}
package com.example.soap.Features.TaxiList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Features.NavigationBar.AppBar
import com.example.soap.Features.NavigationBar.AppDownBar
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.TaxiList.Components.WeekDaySelector
import com.example.soap.Features.TaxiRoomCreation.Components.TaxiDestinationPicker
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiListView(
    navController: NavController,
    taxiListViewModel: TaxiListViewModel
) {

    var showCreationSheet by remember { mutableStateOf(false) }
    var selectedRoom by remember { mutableStateOf<TaxiRoom?>(null) }
    var showChat by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            AppBar(
                currentScreen = Channel.Taxi,
                scrollOffset = scrollState.value,
                navController = navController
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
            )
            {
                Card(
                    colors = CardDefaults.cardColors(MaterialTheme.soapColors.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    TaxiDestinationPicker(
                        source = TaxiLocation.mock(),
                        onSourceChange = { },
                        destination = TaxiLocation.mock(),
                        onDestinationChange = { },
                        locations = TaxiLocation.mockList()
                    )
                }

                Spacer(Modifier.padding(8.dp))

                WeekDaySelector(
                    selectedDate = taxiListViewModel.selectedDate,
                    week = taxiListViewModel.week,
                    onSelect = { taxiListViewModel.selectedDate = it }
                )
            }
        }
    }
}

@Composable
@Preview
private fun Preview(){
    val viewModel = remember { TaxiListViewModel() }
    SoapTheme { TaxiListView(rememberNavController(), viewModel) }
}
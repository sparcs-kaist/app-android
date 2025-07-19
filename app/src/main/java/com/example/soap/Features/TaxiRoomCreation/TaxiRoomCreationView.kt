package com.example.soap.Features.TaxiRoomCreation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.example.soap.Domain.Repositories.FakeTaxiRoomRepository
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.TaxiList.TaxiListViewModel
import com.example.soap.Features.TaxiRoomCreation.Components.TaxiCapacityPicker
import com.example.soap.Features.TaxiRoomCreation.Components.TaxiDepartureTimePicker
import com.example.soap.Features.TaxiRoomCreation.Components.TaxiDestinationPicker
import com.example.soap.Features.TaxiRoomCreation.Components.TaxiRoomCreationNavigationBar
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors
import java.util.Date

@Composable
fun TaxiRoomCreationView(
    navController: NavController,
    viewModel: TaxiListViewModel
) {
    var title by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val isEnabled = remember(title, viewModel.source, viewModel.destination, viewModel.roomDepartureTime) {
        isValid(viewModel, title)
    }

    Scaffold(
        topBar = {
            TaxiRoomCreationNavigationBar(
                onDismiss = { navController.navigate(Channel.Taxi.name) },
                isEnabled = isEnabled
            )
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.soapColors.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
                Card(
                    colors = CardDefaults.cardColors(MaterialTheme.soapColors.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    TaxiDestinationPicker(
                        source = viewModel.source,
                        onSourceChange = { viewModel.source = it },
                        destination = viewModel.destination,
                        onDestinationChange = { viewModel.destination = it },
                        locations = viewModel.locations
                    )
                }

                Spacer(Modifier.padding(16.dp))

                Card(
                    colors = CardDefaults.cardColors(MaterialTheme.soapColors.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        cursorBrush = SolidColor(MaterialTheme.soapColors.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        decorationBox = { innerTextField ->
                            if (title.isEmpty()) {
                                Text(
                                    text = "Title",
                                    color = MaterialTheme.soapColors.grayBB,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Spacer(Modifier.padding(16.dp))

                Card(
                    colors = CardDefaults.cardColors(MaterialTheme.soapColors.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        TaxiDepartureTimePicker(
                            departureTime = viewModel.roomDepartureTime,
                            onDepartureTimeChange = { viewModel.roomDepartureTime = it }
                        )

                        HorizontalDivider(Modifier.padding(vertical = 16.dp))

                        TaxiCapacityPicker(
                            capacity = viewModel.roomCapacity,
                            onCapacityChange = { viewModel.roomCapacity = it }
                        )
                    }
                }

        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text("Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text("Okay")
                    }
                }
            )
        }
    }
}


private fun isValid(viewModel: TaxiListViewModel, title: String): Boolean {
    val source = viewModel.source
    val destination = viewModel.destination
    return source != null &&
            destination != null &&
            source != destination &&
            title.isNotBlank() &&
            viewModel.roomDepartureTime > Date()
}





@Preview
@Composable
private fun Preview() {
    val fakeRepository = remember { FakeTaxiRoomRepository() }

    val viewModel = remember { TaxiListViewModel(fakeRepository) }.apply {
        source = TaxiLocation.mockList()[0]
        destination = TaxiLocation.mockList()[1]
        roomDepartureTime = Date(System.currentTimeMillis() + 3600_000)
        roomCapacity = 3
    }

    SoapTheme {
        TaxiRoomCreationView(
            rememberNavController(),
            viewModel = viewModel
        )
    }
}

package com.example.soap.Features.NavigationBar.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.example.soap.Features.TaxiList.TaxiListViewModel
import com.example.soap.Features.TaxiRoomCreation.TaxiRoomCreationView
import com.example.soap.R
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors
import java.util.Date

@Composable
fun NotificationButton(){
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .shadow(8.dp, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.soapColors.surface)
                .clickable { /* TODO: Menu */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.notification),
                contentDescription = "Menu",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.soapColors.darkGray
            )
        }
    }
}

@Composable
fun SettingButton() {
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .shadow(8.dp, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.soapColors.surface)
                .clickable { /* TODO: Menu */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.more_horiz),
                contentDescription = "Setting",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.soapColors.darkGray
            )
        }
    }
}

@Composable
fun TimetableAddButton() {
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .shadow(8.dp, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.soapColors.surface)
                .clickable { /* TODO: Add */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.round_add),
                contentDescription = "Add Timetable",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.soapColors.darkGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiAddButton() {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDialogView by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .shadow(8.dp, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.soapColors.surface)
                .clickable { showDialogView = true  },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.round_add),
                contentDescription = "Create Taxi Room",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.soapColors.darkGray
            )
        }
    }
    val viewModel = TaxiListViewModel().apply {
        source = TaxiLocation.mockList()[0]
        destination = TaxiLocation.mockList()[1]
        roomDepartureTime = Date(System.currentTimeMillis() + 3600_000)
        roomCapacity = 3
        locations = TaxiLocation.mockList()
    }
    if (showDialogView) {
        ModalBottomSheet(
            onDismissRequest = {
                showDialogView = false
            },
            sheetState = sheetState,
            modifier = Modifier.fillMaxHeight(),
            containerColor = MaterialTheme.soapColors.surface
        ) {
            TaxiRoomCreationView(
                viewModel = viewModel,
                onDismiss = {showDialogView = false}
            )
        }
    }
}


@Composable
fun SearchTopButtonWithoutCircle() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable { /* TODO: Menu */ },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.search),
            contentDescription = "Search",
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.soapColors.darkGray
        )

    }
}


@Composable
@Preview
private fun Preview(){
    SoapTheme {
        NotificationButton()
        

    }
}
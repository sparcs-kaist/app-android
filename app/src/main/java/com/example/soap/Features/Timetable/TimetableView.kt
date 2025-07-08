package com.example.soap.Features.Timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.AppBar
import com.example.soap.Features.NavigationBar.AppDownBar
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.Timetable.Components.CompactTimetableSelector
import com.example.soap.Features.Timetable.Components.TimetableGrid
import com.example.soap.Features.Timetable.Components.TimetableSummary
import com.example.soap.Models.TimeTable.Timetable
import com.example.soap.Utilities.Mocks.mockList
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableView(navController: NavController) {
    val mockTimetable = Timetable.mockList()
    val mockViewModel = remember {
        TimetableViewModel().apply {
            selectedTimetable = mockTimetable[1]
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                currentScreen = Channel.TimeTable
            )
        },

        bottomBar = {
            AppDownBar(
                navController = navController,
                currentScreen = Channel.TimeTable
            )
        }
    ){innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.soapColors.background)
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                CompactTimetableSelector()

                Spacer(Modifier.padding(8.dp))

                TimetableGrid(viewModel = mockViewModel,{navController.navigate(Channel.LectureDetail.name)})

                Spacer(Modifier.padding(8.dp))

                TimetableSummary()

            }
        }
    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme { TimetableView(rememberNavController()) }
}
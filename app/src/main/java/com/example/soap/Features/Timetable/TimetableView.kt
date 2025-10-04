package com.example.soap.Features.Timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Domain.Usecases.MockTimetableUseCase
import com.example.soap.Features.NavigationBar.AppBar
import com.example.soap.Features.NavigationBar.AppDownBar
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.Timetable.Components.CompactTimetableSelector
import com.example.soap.Features.Timetable.Components.TimetableBottomSheet
import com.example.soap.Features.Timetable.Components.TimetableCreditGraph
import com.example.soap.Features.Timetable.Components.TimetableGrid
import com.example.soap.Features.Timetable.Components.TimetableSummary
import com.example.soap.ui.theme.Theme
import com.google.gson.Gson

@Composable
fun TimetableView(
    viewModel: TimetableViewModel = hiltViewModel(),
    navController: NavController,
) {
    var showSearchSheet by remember { mutableStateOf(false) }
    var selectedLecture by remember { mutableStateOf<Lecture?>(null) }
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            AppBar(
                currentScreen = Channel.TimeTable,
                scrollOffset = scrollState.value,
                navController = navController,
                isButtonEnabled = viewModel.isEditable
            )
        },
        bottomBar = {
            AppDownBar(
                navController = navController,
                currentScreen = Channel.TimeTable
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            CompactTimetableSelector(viewModel)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LocalConfiguration.current.screenHeightDp.dp * 0.65f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {

                TimetableGrid(viewModel) { lecture ->
                    selectedLecture = lecture
                    val json = Gson().toJson(lecture)
                    navController.navigate(Channel.LectureDetail.name + "?lecture_json=$json")
                }
            }

            viewModel.selectedTimetable.collectAsState().value?.let { TimetableCreditGraph(it) }

            TimetableSummary(viewModel)

        }
    }

    if (showSearchSheet) {
        TimetableBottomSheet()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }
}

@Composable
@Preview
private fun Preview() {
    val vm by remember { mutableStateOf(TimetableViewModel(MockTimetableUseCase())) }
    Theme { TimetableView(vm, rememberNavController()) }
}
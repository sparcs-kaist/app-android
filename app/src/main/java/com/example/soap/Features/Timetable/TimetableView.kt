package com.example.soap.Features.Timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.TimeTable.Lecture
import com.example.soap.Features.LectureDetail.LectureDetailView
import com.example.soap.Features.NavigationBar.AppBar
import com.example.soap.Features.NavigationBar.AppDownBar
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.Timetable.Components.CompactTimetableSelector
import com.example.soap.Features.Timetable.Components.TimetableGrid
import com.example.soap.Features.Timetable.Components.TimetableSummary
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableView(navController: NavController) {
    val mockViewModel = remember { TimetableViewModel() }

    LaunchedEffect(Unit) {
        if (mockViewModel.timetables.isEmpty()) {
            mockViewModel.fetchData()
        }
    }

    val selectedLecture = remember { mutableStateOf<Lecture?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (mockViewModel.selectedTimetable != null) {
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
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.soapColors.background)
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {


                    CompactTimetableSelector(
                        timetableViewModel = mockViewModel,
                        selectedTimetable = mockViewModel.selectedTimetable!!
                    )

                    Spacer(Modifier.padding(8.dp))

                    TimetableGrid(
                        viewModel = mockViewModel,
                        selectedLecture = { lecture ->
                            selectedLecture.value = lecture
                            scope.launch { sheetState.show() }
                        }
                    )

                    Spacer(Modifier.padding(8.dp))

                    TimetableSummary()
                }
            }
        }

        selectedLecture.value?.let { lecture ->
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) selectedLecture.value = null
                    }
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.soapColors.surface,
                modifier = Modifier.fillMaxHeight(),
                dragHandle = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .width(30.dp)
                                .height(4.dp)
                                .background(
                                    MaterialTheme.soapColors.darkGray,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            ) {
                Box(Modifier.fillMaxSize()) { LectureDetailView(lecture = lecture) }
            }
        }
    }else{
        CircularProgressIndicator()
    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme { TimetableView(rememberNavController()) }
}
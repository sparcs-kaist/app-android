package com.sparcs.soap.Features.Timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.sparcs.soap.Domain.Models.OTL.Lecture
import com.sparcs.soap.Features.LectureSearch.LectureSearchView
import com.sparcs.soap.Features.LectureSearch.LectureSearchViewModel
import com.sparcs.soap.Features.NavigationBar.AppDownBar
import com.sparcs.soap.Features.NavigationBar.Channel
import com.sparcs.soap.Features.Timetable.Components.CompactTimetableSelector
import com.sparcs.soap.Features.Timetable.Components.TimetableBottomSheet
import com.sparcs.soap.Features.Timetable.Components.TimetableCreditGraph
import com.sparcs.soap.Features.Timetable.Components.TimetableGrid
import com.sparcs.soap.Features.Timetable.Components.TimetableSummary
import com.sparcs.soap.Features.Timetable.Components.TimetableViewNavigationBar
import com.sparcs.soap.R

@Composable
fun TimetableView(
    viewModel: TimetableViewModel = hiltViewModel(),
    lectureSearchViewModel: LectureSearchViewModel = hiltViewModel(),
    navController: NavController,
) {
    var selectedLecture by remember { mutableStateOf<Lecture?>(null) }
    val scrollState = rememberScrollState()

    var expanded by rememberSaveable { mutableStateOf(false) }
    var lectureToDelete by remember { mutableStateOf<Lecture?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val selectedTimetable by viewModel.timetableUseCase.selectedTimetable.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                TimetableViewNavigationBar(
                    scrollState = scrollState,
                    isButtonEnabled = viewModel.isEditable.collectAsState().value,
                    onClick = { expanded = true }
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
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                CompactTimetableSelector(viewModel)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.65f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                ) {
                    TimetableGrid(
                        viewModel = viewModel,
                        onLectureSelected = { lecture ->
                            selectedLecture = lecture
                            val json = Gson().toJson(lecture)
                            navController.navigate(Channel.LectureDetail.name + "?lecture_json=$json")
                        },
                        showDeleteDialog = { lecture ->
                            lectureToDelete = lecture
                            showDeleteDialog = true
                        }
                    )
                }

                selectedTimetable?.let { TimetableCreditGraph(it) }

                TimetableSummary(viewModel)
            }
        }

        if (expanded) {
            TimetableBottomSheet(
                onDismiss = {
                    expanded = false
                    lectureSearchViewModel.searchKeyword = ""
                }
            ) { onFold ->
                LectureSearchView(
                    navController = navController,
                    timetableViewModel = viewModel,
                    lectureSearchViewModel = lectureSearchViewModel
                ) {
                    onFold()
                }
            }
        }

        if (showDeleteDialog && lectureToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteLecture(lecture = lectureToDelete!!)
                        }
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                },
                title = { Text(stringResource(R.string.delete)) },
                text = { Text(stringResource(R.string.do_you_really_want_to_delete_this_table,lectureToDelete!!.title.localized())) }
            )
        }
    }
}

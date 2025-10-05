package com.example.soap.Features.LectureSearch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Features.LectureSearch.Components.LectureSearchViewNavigationBar
import com.example.soap.Features.Timetable.Components.SearchCourses
import com.example.soap.Features.Timetable.TimetableViewModel
import com.example.soap.ui.theme.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureSearchView(
    navController: NavController,
    timetableViewModel: TimetableViewModel = hiltViewModel(),
    lectureSearchViewModel: LectureSearchViewModel = hiltViewModel(),
    onDetentChange: (Float) -> Unit = {}
) {
    val searchKeyword = lectureSearchViewModel.searchKeyword
    val lectures by lectureSearchViewModel.lectures.collectAsState()

    val groupedByCourse = lectures.groupBy { it.course }

    val orderedCourses = remember(lectures) {
        val seen = mutableSetOf<Int>()
        val result = mutableListOf<Int>()
        for (lecture in lectures) {
            if (seen.add(lecture.course)) {
                result.add(lecture.course)
            }
        }
        result
    }

    Scaffold(
        topBar = {
            LectureSearchViewNavigationBar(
                title = "Add to \"${timetableViewModel.selectedTimetableDisplayName}\"",
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Search bar
            SearchCourses(
                value = searchKeyword,
                onValueChange = { lectureSearchViewModel.searchKeyword = it }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Lecture list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                orderedCourses.forEach { course ->
                    val courseLectures = groupedByCourse[course] ?: emptyList()
                    if (courseLectures.isNotEmpty()) {
                        stickyHeader {
                            val firstItem = courseLectures.first()
                            Surface(color = MaterialTheme.colorScheme.background) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = firstItem.title.localized(),
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(firstItem.code)
                                        Text(firstItem.typeDetail.localized())
                                    }
                                }
                            }
                        }

                        items(courseLectures) { lecture ->
                            LectureRow(
                                lecture = lecture,
                                onClick = {
                                    timetableViewModel.candidateLecture = lecture
//                                    onDetentChange(130f)
                                    navController.navigate("lectureDetail/${lecture.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        lectureSearchViewModel.bind()
    }

    DisposableEffect(Unit) {
        onDispose {
            timetableViewModel.candidateLecture = null
            onDetentChange(Float.MAX_VALUE)
        }
    }
}

@Composable
fun LectureRow(lecture: Lecture, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = lecture.section ?: "A",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = lecture.professors.firstOrNull()?.name?.localized() ?: "Unknown",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Composable
private fun Preview(){
    Theme {
        LectureSearchView(navController = rememberNavController(), timetableViewModel = viewModel(), lectureSearchViewModel = viewModel(), {})
    }
}
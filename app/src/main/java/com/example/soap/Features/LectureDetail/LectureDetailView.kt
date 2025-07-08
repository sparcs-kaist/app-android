package com.example.soap.Features.LectureDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.LectureDetail.Components.LectureInformation
import com.example.soap.Features.LectureDetail.Components.LectureReviews
import com.example.soap.Features.LectureDetail.Components.LectureSummary
import com.example.soap.Models.TimeTable.Lecture
import com.example.soap.Utilities.Mocks.mock
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureDetailView(lecture: Lecture, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(lecture.title.localized())
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.soapColors.surface)
            )
        },
        containerColor = MaterialTheme.soapColors.surface
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(innerPadding)
        ) {
            item {
                LectureSummary(lecture = lecture)
            }

            item {
                LectureInformation(lecture = lecture)
            }

            item {
                LectureReviews(lecture = lecture)
            }
        }
    }
}


@Composable
@Preview
private fun Preview(){
    SoapTheme { LectureDetailView(lecture = Lecture.mock(), rememberNavController()) }
}

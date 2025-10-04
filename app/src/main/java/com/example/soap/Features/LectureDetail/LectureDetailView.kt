package com.example.soap.Features.LectureDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Features.LectureDetail.Components.LectureDetailNavigationBar
import com.example.soap.Features.LectureDetail.Components.LectureInformation
import com.example.soap.Features.LectureDetail.Components.LectureReviews
import com.example.soap.Features.LectureDetail.Components.LectureSummary
import com.example.soap.ui.theme.Theme
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureDetailView(
    onAdd: (() -> Unit)?,
    navController: NavController
) {
    val scrollState = rememberScrollState()
    val backStackEntry = navController.currentBackStackEntry!!

    val json = backStackEntry.savedStateHandle.get<String>("lecture_json")!!
    val lecture = Gson().fromJson(json, Lecture::class.java)

    Scaffold(
        topBar = {
            LectureDetailNavigationBar(
                navController = navController,
                text = lecture.title.localized(),
                onAdd = onAdd
            )
        }
    ) { innerPadding->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp)
            ) {
                item {
                    LectureSummary(lecture = lecture)
                    HorizontalDivider(Modifier.padding(4.dp))
                }

                item {
                    LectureInformation(lecture = lecture)
                    HorizontalDivider(Modifier.padding(4.dp))
                }

                item {
                    LectureReviews(lecture = lecture)
                }
            }
        }
    }
}

@Composable
@Preview
private fun Preview(){
    Theme { LectureDetailView(onAdd = null, navController = rememberNavController()) }
}
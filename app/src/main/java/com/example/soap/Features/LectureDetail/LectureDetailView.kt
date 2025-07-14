package com.example.soap.Features.LectureDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Features.LectureDetail.Components.LectureInformation
import com.example.soap.Features.LectureDetail.Components.LectureReviews
import com.example.soap.Features.LectureDetail.Components.LectureSummary
import com.example.soap.Domain.Models.TimeTable.Lecture
import com.example.soap.Shared.Mocks.mock
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@Composable
fun LectureDetailView(lecture: Lecture) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.soapColors.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = lecture.title.localized(),
                style = MaterialTheme.typography.titleLarge
            )
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp)
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
    SoapTheme { LectureDetailView(lecture = Lecture.mock()) }
}

package com.example.soap.Features.LectureDetail.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.soap.Domain.Models.TimeTable.Lecture
import com.example.soap.R
import com.example.soap.Shared.Mocks.mock
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.darkGray
import com.example.soap.ui.theme.grayBB

@Composable
fun LectureSummary(lecture: Lecture){
    Box(
        Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ){
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){

            LectureSummaryRow(
                title = stringResource(R.string.language),
                description = if (lecture.isEnglish) "EN" else "한"
            )


            LectureSummaryRow(
                title = stringResource(R.string.credit),
                description = (lecture.credit + lecture.creditAu).toString()
            )


            LectureSummaryRow(
                title = stringResource(R.string.competition),
                description =
                if (lecture.capacity == 0 || lecture.numberOfPeople == 0)
                    "0.0:1"
                else "${
                    (String.format(
                        "%.1f",
                        (lecture.numberOfPeople / lecture.capacity).toFloat()
                    ))
                }:1"
            )

        }
    }
}

@Composable
fun LectureSummaryRow(
    title: String,
    description: String
){
    Column(horizontalAlignment = Alignment.CenterHorizontally){
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.grayBB
        )
        Text(
            text = description,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.darkGray
        )
    }
}


@Composable
@Preview
private fun Preview(){
    Theme { LectureSummary(lecture = Lecture.mock()) }
}
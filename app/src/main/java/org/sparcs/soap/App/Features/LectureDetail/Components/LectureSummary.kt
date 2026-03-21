package org.sparcs.soap.App.Features.LectureDetail.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Shared.Mocks.OTL.mock
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R
import java.util.Locale

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
                description = (lecture.credit + lecture.creditAU).toString()
            )

            LectureSummaryRow(
                title = stringResource(R.string.competition),
                description =
                if (lecture.capacity == 0 || lecture.enrolledCount == 0) {
                    "0.0:1"
                } else {
                    val ratio = lecture.enrolledCount.toFloat() / lecture.capacity.toFloat()
                    "${String.format(Locale.getDefault(), "%.1f", ratio)}:1"
                }
            )
        }
    }
}

@Composable
@Preview
private fun Preview(){
    Theme { LectureSummary(lecture = Lecture.mock()) }
}
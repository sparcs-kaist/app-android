package com.example.soap.Features.Timetable.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soap.Features.NavigationBar.Animation.AnimatedText
import com.example.soap.Features.Timetable.TimetableViewModel
import com.example.soap.Models.TimeTable.Timetable
import com.example.soap.R
import com.example.soap.Utilities.Mocks.mock
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@Composable
fun CompactTimetableSelector(
    timetableViewModel: TimetableViewModel,
    selectedTimetable: Timetable
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        SemesterSelector(timetableViewModel = timetableViewModel, selectedTimetable = selectedTimetable)

        Spacer(Modifier.weight(1f))

        TableSelector()
    }
}

@Composable
fun SemesterSelector(
    timetableViewModel: TimetableViewModel,
    selectedTimetable: Timetable
){
    val isEnabledPreviousButton = timetableViewModel.semesters.isNotEmpty() && (timetableViewModel.semesters.first() != selectedTimetable.semester)
    val isEnabledNextButton = timetableViewModel.semesters.isNotEmpty() && (timetableViewModel.semesters.last() != selectedTimetable.semester)


    Box(Modifier.shadow(4.dp, RoundedCornerShape(25.dp))){

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
                .background(MaterialTheme.soapColors.surface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_back_ios),
                contentDescription = "Select Previous Semester",
                tint = if (isEnabledPreviousButton) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.soapColors.grayBB,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .then(
                        if (isEnabledPreviousButton) Modifier.clickable {
                            timetableViewModel.selectPreviousSemester()
                        } else Modifier
                    )
            )

            AnimatedText(
                text = selectedTimetable.semester.description,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Icon(
                painter = painterResource(R.drawable.arrow_forward_ios),
                contentDescription = "Select Next Semester",
                tint = if (isEnabledNextButton) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.soapColors.grayBB,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .then(
                        if (isEnabledNextButton) Modifier.clickable {
                            timetableViewModel.selectNextSemester()
                        } else Modifier
                    )
            )
        }
    }
}

@Composable
fun TableSelector(){
    Box(Modifier.shadow(4.dp, RoundedCornerShape(25.dp))){
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
                .background(MaterialTheme.soapColors.surface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.my_table),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Icon(
                painter = painterResource(R.drawable.more_horiz),
                contentDescription = "Menu",
                tint = MaterialTheme.soapColors.onSurface,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable {  }
            )
        }
    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme {
        CompactTimetableSelector(
            timetableViewModel = viewModel(),
            selectedTimetable = Timetable.mock()
        )
    }
}
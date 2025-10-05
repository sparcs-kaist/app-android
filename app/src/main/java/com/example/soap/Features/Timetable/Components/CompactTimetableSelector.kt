package com.example.soap.Features.Timetable.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Domain.Usecases.MockTimetableUseCase
import com.example.soap.Features.NavigationBar.Animation.AnimatedText
import com.example.soap.Features.Timetable.TimetableViewModel
import com.example.soap.R
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB

@Composable
fun CompactTimetableSelector(
    viewModel: TimetableViewModel,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        SemesterSelector(viewModel = viewModel)

        Spacer(Modifier.weight(1f))

        TableSelector(viewModel = viewModel)
    }
}

@Composable
fun SemesterSelector(
    viewModel: TimetableViewModel,
) {
    val selectedSemester by viewModel.selectedSemester.collectAsState()
    val isEnabledPreviousButton =
        viewModel.semesters.collectAsState().value.firstOrNull() != selectedSemester
    val isEnabledNextButton =
        viewModel.semesters.collectAsState().value.lastOrNull() != selectedSemester


    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.arrow_back_ios),
            contentDescription = "Select Previous Semester",
            tint = if (isEnabledPreviousButton) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.grayBB,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .then(
                    if (isEnabledPreviousButton) Modifier.clickable {
                        viewModel.selectPreviousSemester()
                    } else Modifier
                )
        )

        AnimatedText(
            text = viewModel.selectedSemester.value?.description ?: "Unknown",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )

        Icon(
            painter = painterResource(R.drawable.arrow_forward_ios),
            contentDescription = "Select Next Semester",
            tint = if (isEnabledNextButton) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.grayBB,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .then(
                    if (isEnabledNextButton) Modifier.clickable {
                        viewModel.selectNextSemester()
                    } else Modifier
                )
        )
    }
}

@Composable
fun TableSelector(viewModel: TimetableViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.surface)
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
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .clickable { expanded = true }
        )

        TimetableDropDownMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
            viewModel = viewModel
        )
    }
}

@Composable
@Preview
private fun Preview() {
    val vm by remember { mutableStateOf(TimetableViewModel(MockTimetableUseCase())) }
    Theme { CompactTimetableSelector(viewModel = vm) }
}
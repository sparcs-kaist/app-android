package com.sparcs.soap.Features.Timetable.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sparcs.soap.Domain.Helpers.CrashlyticsHelper
import com.sparcs.soap.Domain.Usecases.MockTimetableUseCase
import com.sparcs.soap.Features.Timetable.TimetableViewModel
import com.sparcs.soap.R
import com.sparcs.soap.ui.theme.Theme

@Composable
fun TimetableSummary(
    viewModel: TimetableViewModel,
) {
    val selectedTimetable by viewModel.timetableUseCase.selectedTimetable.collectAsState()
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BigSummary(stringResource(R.string.credit), "${selectedTimetable?.credits ?: 0}")
            BigSummary(stringResource(R.string.au), "${selectedTimetable?.creditAUs ?: 0}")
            BigSummary(stringResource(R.string.grade), selectedTimetable?.gradeLetter ?: "?")
            BigSummary(stringResource(R.string.load), selectedTimetable?.loadLetter ?: "?")
            BigSummary(stringResource(R.string.speech), selectedTimetable?.speechLetter ?: "?")
        }
    }
}

@Composable
fun BigSummary(
    label: String,
    grade: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Text(
            text = grade,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
@Preview
private fun Preview() {
    val vm by remember { mutableStateOf(TimetableViewModel(MockTimetableUseCase(), CrashlyticsHelper())) }
    Theme { TimetableSummary(vm) }
}
package org.sparcs.Features.Timetable.Components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.Features.Timetable.TimetableViewModelProtocol
import org.sparcs.R
import org.sparcs.Shared.ViewModelMocks.OTL.MockTimetableViewModel
import org.sparcs.ui.theme.Theme

@Composable
fun TimetableSummary(
    viewModel: TimetableViewModelProtocol,
) {
    val selectedTimetable = viewModel.timetableUseCase?.selectedTimetable?.collectAsState()?.value
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
    Theme { TimetableSummary(MockTimetableViewModel()) }
}
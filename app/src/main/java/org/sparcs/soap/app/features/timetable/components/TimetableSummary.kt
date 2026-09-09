package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.LectureType
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun TimetableSummary(
    viewModel: TimetableViewModelProtocol,
    compact: Boolean = false
) {
    val selectedTimetable by viewModel.selectedTimetable.collectAsState()
    val candidateLecture by viewModel.candidateLecture.collectAsState()

    val totalCredits = (selectedTimetable?.credits ?: 0) + (candidateLecture?.credit ?: 0)
    val totalAUs = (selectedTimetable?.creditAUs ?: 0) + (candidateLecture?.creditAU ?: 0)

    if (compact) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryMiniItem(stringResource(R.string.filter_basic_required), "${selectedTimetable?.getCreditsFor(LectureType.BR) ?: 0}")
                    SummaryMiniItem(stringResource(R.string.filter_major_required), "${selectedTimetable?.getCreditsFor(LectureType.MR) ?: 0}")
                    SummaryMiniItem(stringResource(R.string.hse), "${selectedTimetable?.getCreditsFor(LectureType.HSE) ?: 0}")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryMiniItem(stringResource(R.string.filter_basic_elective), "${selectedTimetable?.getCreditsFor(LectureType.BE) ?: 0}")
                    SummaryMiniItem(stringResource(R.string.filter_major_elective), "${selectedTimetable?.getCreditsFor(LectureType.ME) ?: 0}")
                    SummaryMiniItem(stringResource(R.string.etc), "${selectedTimetable?.getCreditsFor(LectureType.ETC) ?: 0}")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                SummaryStatItem("$totalCredits", stringResource(R.string.credit))
                SummaryStatItem("$totalAUs", stringResource(R.string.au))
                SummaryStatItem(selectedTimetable?.gradeLetter ?: "?", stringResource(R.string.grade), isAccent = false)
                SummaryStatItem(selectedTimetable?.loadLetter ?: "?", stringResource(R.string.load))
                SummaryStatItem(selectedTimetable?.speechLetter ?: "?", stringResource(R.string.speech))
            }
        }
    } else {
        Card(
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .glassBorder(shape = RoundedCornerShape(20.dp))
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
}

@Composable
private fun SummaryMiniItem(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            fontWeight = FontWeight.Bold,
            color = if (value != "0") MaterialTheme.colorScheme.onSurface else Color.LightGray
        )
    }
}

@Composable
private fun SummaryStatItem(value: String, label: String, isAccent: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
            fontWeight = FontWeight.ExtraBold,
            color = if (isAccent) MaterialTheme.colorScheme.primary 
                    else if (value == "0" || value == "?") Color.LightGray 
                    else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
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
    Theme { TimetableSummary(PreviewTimetableViewModel()) }
}

@Composable
@Preview
private fun CompactPreview() {
    Theme { TimetableSummary(PreviewTimetableViewModel(), compact = true) }
}

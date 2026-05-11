package org.sparcs.soap.App.Features.Timetable.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Shared.Mocks.OTL.mock
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R

@Composable
fun LectureList(
    lectures: List<Lecture>,
    onLectureSelected: (Lecture) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = pluralStringResource(
                R.plurals.lectures_count,
                lectures.size,
                lectures.size
            ),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (lectures.isNotEmpty()) {
            lectures.forEachIndexed { index, lecture ->
                Column {
                    Box(modifier = Modifier.clickable { onLectureSelected(lecture) }) {
                        LectureListRow(lecture = lecture)
                    }
                    if (index < lectures.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 24.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.no_lecture_for_timetable),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LectureListPreview() {
    Theme {
        LectureList(
            lectures = listOf(Lecture.mock(), Lecture.mock(), Lecture.mock()),
            onLectureSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyLectureListPreview() {
    Theme {
        LectureList(
            lectures = emptyList(),
            onLectureSelected = {}
        )
    }
}

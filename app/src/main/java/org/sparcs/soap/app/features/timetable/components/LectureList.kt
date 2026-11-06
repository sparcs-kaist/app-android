package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.views.contentViews.SegmentedSection
import org.sparcs.soap.app.theme.ui.Theme

/**
 * The timetable's lectures as a Material 3 Expressive segmented group under a count header, one
 * tappable tile per lecture.
 */
@Composable
fun LectureList(
    lectures: List<Lecture>,
    onLectureSelected: (Lecture) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = pluralStringResource(R.plurals.lectures_count, lectures.size, lectures.size)
    if (lectures.isEmpty()) {
        SegmentedSection(title, count = 1, modifier) {
            EmptyListMessage(stringResource(id = R.string.no_lecture_for_timetable))
        }
        return
    }
    SegmentedSection(title, lectures.size, modifier, contentPadding = PaddingValues()) { index ->
        val lecture = lectures[index]
        Box(
            Modifier
                .fillMaxWidth()
                .clickable { onLectureSelected(lecture) }
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            LectureListRow(lecture = lecture)
        }
    }
}

/** The one tile of an empty list. */
@Composable
internal fun EmptyListMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
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

package org.sparcs.soap.app.features.lectureDetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

// Sections shared by the lecture detail and course screens.

/** Section title with an optional [count] beside it and [trailing] content at the end. */
@Composable
fun DetailSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        count?.let {
            Text(
                text = it.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.weight(1f))
        trailing()
    }
}

/**
 * Label and value rows as one Material 3 Expressive segmented group: the first and last rows take
 * the group's rounded corners and the rows between are split by small gaps, in place of dividers.
 * Optional [footer] content is the last row.
 */
@Composable
fun DetailInfoCard(
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val count = rows.size + if (footer != null) 1 else 0
    // The screen's other cards' color, so the group sits with the review cards below it.
    val color = CardDefaults.cardColors().containerColor
    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        modifier = modifier.fillMaxWidth()
    ) {
        rows.forEachIndexed { index, (label, value) ->
            Surface(shape = ListItemDefaults.segmentedShapes(index, count).shape, color = color) {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    LectureDetailRow(title = label, description = value)
                }
            }
        }
        if (footer != null) {
            Surface(shape = ListItemDefaults.segmentedShapes(count - 1, count).shape, color = color) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    content = footer
                )
            }
        }
    }
}

/** Average grade, load and speech letters for a lecture or course. */
@Composable
fun ReviewStatsCard(grade: String, load: String, speech: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LectureSummaryRow(title = stringResource(R.string.grade), description = grade)
            StatSeparator()
            LectureSummaryRow(title = stringResource(R.string.load), description = load)
            StatSeparator()
            LectureSummaryRow(title = stringResource(R.string.speech), description = speech)
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            DetailSectionHeader("Information")
            DetailInfoCard(listOf("Code" to "CS.20300", "Type" to "Major Elective"))
            DetailSectionHeader("Reviews", count = 12)
            ReviewStatsCard("A", "B+", "A-")
        }
    }
}

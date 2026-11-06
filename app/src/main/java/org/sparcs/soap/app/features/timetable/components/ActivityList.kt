package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableActivity
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.features.timetable.activity.activityTimeRange
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.views.contentViews.SegmentedSection
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun ActivityList(
    activities: List<TimetableActivity>,
    viewModel: TimetableViewModelProtocol,
    onEdit: (TimetableActivity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf<TimetableActivity?>(null) }
    var showActions by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val sorted = remember(activities) {
        activities.sortedWith(
            compareBy(
                { it.ranges.first().day },
                { it.ranges.first().begin },
                { it.id })
        )
    }
    val title = pluralStringResource(R.plurals.activities_count, activities.size, activities.size)
    if (sorted.isEmpty()) {
        SegmentedSection(title, count = 1, modifier) {
            EmptyListMessage(stringResource(R.string.activity_list_empty))
        }
    } else SegmentedSection(title, sorted.size, modifier, contentPadding = PaddingValues()) { index ->
        val activity = sorted[index]
        key(activity.id) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { selected = activity; showActions = false },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress); selected =
                            activity; showActions = true
                        }
                    )
                    .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(
                            LocalTimetableTheme.current.colorFor(activity.id),
                            CircleShape
                        )
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        activity.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (activity.location.isNotBlank()) Text(
                        activity.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        activity.ranges.map { activityTimeRange(it) }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { selected = activity; showActions = true }, shapes = IconButtonDefaults.shapes()) {
                    Icon(
                        Icons.Default.MoreVert,
                        stringResource(R.string.activity_options, activity.title)
                    )
                }
            }
        }
    }
    ActivityDetailsDialog(selected, showActions, viewModel, onEdit) { selected = null }
}


@Composable
@Preview
private fun Preview(){
    Theme {
        ActivityList(
            activities = listOf(
                TimetableActivity(1, "Study group", "Library", 0, 540, 600),
                TimetableActivity(2, "Meeting", "Room 101", 1, 600, 660),
                TimetableActivity(3, "Workshop", "Hall A", 2, 660, 720)
            ),
            viewModel = PreviewTimetableViewModel(
                Timetable.mock()
            ),
            onEdit = {}
        )
    }
}
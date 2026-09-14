package org.sparcs.soap.app.features.timetable.components

import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.TimetableActivity
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.features.timetable.activity.activityTime
import org.sparcs.soap.app.shared.extensions.glassBorder

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
    val sorted = remember(activities) { activities.sortedWith(compareBy({ it.day }, { it.begin }, { it.id })) }
    ElevatedCard(
        modifier = modifier.fillMaxWidth().glassBorder(shape = RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(pluralStringResource(R.plurals.activities_count, activities.size, activities.size),
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
            if (sorted.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.activity_list_empty), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
                }
            }
            sorted.forEachIndexed { index, activity ->
                key(activity.id) {
                    Row(Modifier.fillMaxWidth().combinedClickable(
                        onClick = { selected = activity; showActions = false },
                        onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selected = activity; showActions = true }
                    ).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).background(LocalTimetableTheme.current.colorFor(activity.id), CircleShape))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(activity.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            if (activity.location.isNotBlank()) Text(activity.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            val day = DayType.fromValue(activity.day)?.let { stringResource(it.stringValue) }.orEmpty()
                            Text("$day · ${activityTime(activity.begin)} – ${activityTime(activity.end)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { selected = activity; showActions = true }) {
                            Icon(Icons.Default.MoreVert, stringResource(R.string.activity_options, activity.title))
                        }
                    }
                    if (index < sorted.lastIndex) HorizontalDivider(Modifier.padding(start = 24.dp), thickness = .5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))
                }
            }
        }
    }
    ActivityDetailsDialog(selected, showActions, viewModel, onEdit) { selected = null }
}

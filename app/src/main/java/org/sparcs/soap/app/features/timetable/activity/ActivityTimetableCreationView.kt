package org.sparcs.soap.app.features.timetable.activity

import androidx.activity.compose.BackHandler
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.*
import org.sparcs.soap.app.features.timetable.components.TimetableGridCell
import kotlin.math.roundToInt

@Composable
internal fun ActivityConflictNotice(visible: Boolean) {
    AnimatedVisibility(visible, enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)), exit = shrinkVertically(spring())) {
        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Warning, null, Modifier.size(20.dp))
                Text(stringResource(R.string.activity_conflict), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private enum class DragPart { Move, Start, End }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTimetableCreationView(
    timetable: Timetable,
    selection: ActivityDraft,
    excludingID: Int?,
    onChange: (ActivityDraft) -> Unit,
    onBack: () -> Unit,
) {
    var day by rememberSaveable { mutableIntStateOf(selection.day) }
    var begin by rememberSaveable { mutableIntStateOf(selection.begin) }
    var end by rememberSaveable { mutableIntStateOf(selection.end) }
    val draft = selection.copy(day = day, begin = begin, end = end)
    // A weekend column appears only when explicitly chosen in the form.
    val days = remember(selection.day) { (DayType.weekdays().map { it.value } + selection.day).distinct().sorted() }
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val minutePx = with(density) { 80.dp.toPx() / 60f }
    val insetPx = with(density) { 24.dp.toPx() }
    var dragPart by remember { mutableStateOf<DragPart?>(null) }
    var delta by remember { mutableStateOf(Offset.Zero) }
    var origin by remember { mutableStateOf(draft) }
    var originScroll by remember { mutableIntStateOf(0) }
    var fingerY by remember { mutableFloatStateOf(0f) }
    var viewportHeight by remember { mutableFloatStateOf(0f) }
    val currentChange by rememberUpdatedState(onChange)
    BackHandler(onBack = onBack)
    LaunchedEffect(Unit) { scroll.scrollTo(((begin - 60).coerceAtLeast(0) * minutePx).roundToInt()) }
    LaunchedEffect(dragPart) {
        if (dragPart == null) return@LaunchedEffect
        var previous = withFrameNanos { it }
        while (dragPart != null) {
            val now = withFrameNanos { it }
            val seconds = ((now - previous) / 1_000_000_000f).coerceAtMost(.05f)
            previous = now
            val edge = with(density) { 64.dp.toPx() }
            val speed = when {
                fingerY < edge -> -((edge - fingerY) / edge).coerceIn(0f, 1f)
                fingerY > viewportHeight - edge -> ((fingerY - viewportHeight + edge) / edge).coerceIn(0f, 1f)
                else -> 0f
            }
            scroll.scrollBy(speed * minutePx * 240 * seconds)
        }
    }
    Scaffold(topBar = { TopAppBar(title = {}, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.activity_back)) } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(LocalTimetableTheme.current.backgroundColor ?: MaterialTheme.colorScheme.surface).padding(top = 16.dp)) {
            Row(Modifier.fillMaxWidth().padding(start = 44.dp, end = 16.dp, bottom = 12.dp)) {
                days.forEach { value -> Text(stringResource(DayType.fromValue(value)!!.stringValue), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = LocalTimetableTheme.current.gridLabelColor ?: MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).clipToBounds()) {
                val gutter = with(density) { 44.dp.toPx() }
                val trailing = with(density) { 16.dp.toPx() }
                val widthPx = with(density) { maxWidth.toPx() }
                val dayWidth = (widthPx - gutter - trailing) / days.size
                val heightPx = with(density) { maxHeight.toPx() }
                SideEffect { viewportHeight = heightPx }
                val dy = delta.y + scroll.value - originScroll
                val moving = dragPart != null
                fun proposedSelection(): ActivityDraft {
                    val movement = delta.y + scroll.value - originScroll
                    return when (dragPart) {
                        DragPart.Move -> origin.move(ActivityDraft.snap(origin.begin + movement / minutePx), days[(days.indexOf(origin.day) + (delta.x / dayWidth).roundToInt()).coerceIn(days.indices)])
                        DragPart.Start -> origin.resizeStart(ActivityDraft.snap(origin.begin + movement / minutePx))
                        DragPart.End -> origin.resizeEnd(ActivityDraft.snap(origin.end + movement / minutePx))
                        null -> draft
                    }
                }
                val proposed = proposedSelection()
                val conflict = proposed.conflict(timetable, excludingID)
                val settledX = gutter + days.indexOf(day) * dayWidth + 2
                val settledY = insetPx + begin * minutePx - scroll.value
                val rawY = when (dragPart) {
                    DragPart.Move -> insetPx + origin.begin * minutePx - originScroll + delta.y
                    DragPart.Start -> insetPx + (origin.begin * minutePx + dy).coerceIn(0f, (origin.end - 15) * minutePx) - scroll.value
                    else -> settledY
                }
                val rawHeight = when (dragPart) {
                    DragPart.Start -> insetPx + origin.end * minutePx - scroll.value - rawY
                    DragPart.End -> (origin.end * minutePx + dy).coerceIn((origin.begin + 15) * minutePx, 1440 * minutePx) - origin.begin * minutePx
                    else -> (end - begin) * minutePx
                }
                val x by animateFloatAsState(if (dragPart == DragPart.Move) gutter + days.indexOf(origin.day) * dayWidth + 2 + delta.x else settledX, if (moving) snap() else spring(stiffness = Spring.StiffnessMediumLow), label = "Activity x")
                val y by animateFloatAsState(rawY, if (moving || scroll.isScrollInProgress) snap() else spring(stiffness = Spring.StiffnessMediumLow), label = "Activity y")
                val cellHeight by animateFloatAsState(rawHeight, if (moving) snap() else spring(), label = "Activity duration")
                val scale by animateFloatAsState(if (dragPart == DragPart.Move) 1.04f else 1f, spring(stiffness = Spring.StiffnessMedium), label = "Activity lift")
                val startDrag: (DragPart, Offset) -> Unit = { part, position ->
                    origin = draft; originScroll = scroll.value; delta = Offset.Zero; dragPart = part
                    fingerY = y + position.y + if (part == DragPart.End) cellHeight - with(density) { 24.dp.toPx() } else if (part == DragPart.Start) -with(density) { 24.dp.toPx() } else 0f
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                val finish: () -> Unit = {
                    val dropped = proposedSelection()
                    day = dropped.day; begin = dropped.begin; end = dropped.end
                    if (!dropped.conflict(timetable, excludingID)) currentChange(dropped)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    dragPart = null; delta = Offset.Zero
                }
                val finishNow by rememberUpdatedState(finish)
                val startNow by rememberUpdatedState(startDrag)
                val cancel: () -> Unit = { dragPart = null; delta = Offset.Zero }
                val outline = LocalTimetableTheme.current.separatorColor ?: MaterialTheme.colorScheme.outlineVariant
                Column(Modifier.fillMaxSize().verticalScroll(scroll, enabled = !moving)) {
                    Box(Modifier.fillMaxWidth().height(1920.dp + 48.dp)) {
                        Canvas(Modifier.fillMaxSize()) {
                            for (hour in 0..24) {
                                val lineY = insetPx + hour * 60 * minutePx
                                drawLine(outline.copy(alpha = .6f), Offset(gutter, lineY), Offset(size.width - trailing, lineY), 1.dp.toPx())
                            }
                        }
                        for (hour in 0..23) Text("%02d".format(hour), Modifier.offset { IntOffset(0, (insetPx + hour * 60 * minutePx - 8.dp.toPx()).roundToInt()) }.width(36.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = LocalTimetableTheme.current.gridLabelColor ?: MaterialTheme.colorScheme.onSurfaceVariant)
                        days.forEachIndexed { index, value ->
                            timetable.getLectures(DayType.fromValue(value)!!, null).forEach { item ->
                                val h = with(density) { (item.lectureClass.duration * minutePx - 3).coerceAtLeast(1f).toDp() }
                                TimetableGridCell(item, false, h, Modifier.offset { IntOffset((gutter + index * dayWidth + 2).roundToInt(), (insetPx + item.lectureClass.begin * minutePx).roundToInt()) }.width(with(density) { (dayWidth - 4).toDp() }))
                            }
                            timetable.activities.filter { it.day == value && it.id != excludingID }.forEach { activity ->
                                Surface(color = LocalTimetableTheme.current.colorFor(activity.id), contentColor = LocalTimetableTheme.current.textColor, shape = RoundedCornerShape(4.dp), modifier = Modifier.offset { IntOffset((gutter + index * dayWidth + 2).roundToInt(), (insetPx + activity.begin * minutePx).roundToInt()) }.size(with(density) { (dayWidth - 4).toDp() }, with(density) { ((activity.end - activity.begin) * minutePx - 3).coerceAtLeast(1f).toDp() })) {
                                    Text(activity.title, Modifier.padding(4.dp), style = MaterialTheme.typography.labelSmall, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
                val accent = if (conflict) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                val startHandle = stringResource(R.string.activity_start_handle)
                val endHandle = stringResource(R.string.activity_end_handle)
                val moveEarlier = stringResource(R.string.activity_earlier)
                val moveLater = stringResource(R.string.activity_later)
                val title = draft.title.ifBlank { stringResource(R.string.activity_new) }
                val time = "${activityTime(proposed.begin)} – ${activityTime(proposed.end)}"
                Box(Modifier.offset { IntOffset(x.roundToInt(), y.roundToInt()) }.size(with(density) { (dayWidth - 4).toDp() }, with(density) { cellHeight.toDp() })
                    .graphicsLayer { scaleX = scale; scaleY = scale; shadowElevation = if (moving) 8.dp.toPx() else 0f; shape = RoundedCornerShape(6.dp) }
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = .88f), RoundedCornerShape(6.dp))
                    .background(accent.copy(alpha = .24f), RoundedCornerShape(6.dp)).border(1.5.dp, accent, RoundedCornerShape(6.dp))
                    .semantics {
                        contentDescription = "$title, $time"
                        customActions = listOf(CustomAccessibilityAction(moveEarlier) { val next = draft.move(begin - 15); day = next.day; begin = next.begin; end = next.end; if (!next.conflict(timetable, excludingID)) currentChange(next); true },
                            CustomAccessibilityAction(moveLater) { val next = draft.move(begin + 15); day = next.day; begin = next.begin; end = next.end; if (!next.conflict(timetable, excludingID)) currentChange(next); true })
                    }
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(onDragStart = { startNow(DragPart.Move, it) }, onDragEnd = { finishNow() }, onDragCancel = cancel) { change, amount ->
                            change.consume(); delta += amount; fingerY += amount.y
                        }
                    }) {
                    Column(Modifier.fillMaxSize().padding(6.dp)) {
                        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (cellHeight > with(density) { 48.dp.toPx() }) Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // Google Calendar places the start handle on the left and the end on the right.
                    listOf(DragPart.Start, DragPart.End).forEach { part ->
                        Box(Modifier.align(if (part == DragPart.Start) Alignment.TopStart else Alignment.BottomEnd)
                            .offset(x = if (part == DragPart.Start) (-8).dp else 8.dp, y = if (part == DragPart.Start) (-24).dp else 24.dp)
                            .size(48.dp)
                            .semantics { contentDescription = if (part == DragPart.Start) startHandle else endHandle }
                            .pointerInput(part) { detectDragGestures(onDragStart = { startNow(part, it) }, onDragEnd = { finishNow() }, onDragCancel = cancel) { change, amount -> change.consume(); delta += amount; fingerY += amount.y } }, contentAlignment = Alignment.Center) {
                            Box(
                                Modifier.size(16.dp)
                                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                                    .background(accent.copy(alpha = .16f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(Modifier.size(8.dp).background(accent, CircleShape))
                            }
                        }
                    }
                }
                Box(Modifier.align(Alignment.BottomCenter).padding(16.dp)) { ActivityConflictNotice(conflict) }
            }
        }
    }
}

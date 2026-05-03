package org.sparcs.soap.tile

import android.content.Context
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.ProgressIndicatorColors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import org.sparcs.soap.R
import org.sparcs.soap.data.WatchDataStore
import org.sparcs.soap.data.models.Semester
import org.sparcs.soap.data.models.Timetable
import org.sparcs.soap.shared.formatTimeRange
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

private const val RESOURCES_VERSION = "1"
private const val FRESHNESS_INTERVAL_MILLIS = 30 * 60 * 1000L

@OptIn(ExperimentalHorologistApi::class)
class DDayTileService : SuspendingTileService() {
    private val watchDataStore by lazy { WatchDataStore(applicationContext) }
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest) =
        ResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION).build()

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val semesterJson = watchDataStore.semesterJsonFlow.firstOrNull()
        val timetableJson = watchDataStore.timetableJsonFlow.firstOrNull()

        val semester = semesterJson?.let {
            try { json.decodeFromString<Semester>(it) } catch (_: Exception) { null }
        }
        val timetable = timetableJson?.let {
            try { json.decodeFromString<Timetable>(it) } catch (_: Exception) { null }
        }

        return dDayTile(requestParams, this, timetable, semester)
    }
}

private fun dDayTile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
    timetable: Timetable?,
    semester: Semester?,
): TileBuilders.Tile {
    val timelineBuilder = TimelineBuilders.Timeline.Builder()
    val now = Calendar.getInstance()
    val dayOfWeekString = getDayOfWeekString(now)

    val transitionPoints = mutableListOf<Long>()
    transitionPoints.add(getEndOfDayMillis())

    if (timetable != null && dayOfWeekString.isNotEmpty()) {
        timetable.lectures
            .flatMap { lecture -> lecture.classes.map { cl -> lecture to cl } }
            .filter { (_, cl) -> cl.day == dayOfWeekString }
            .forEach { (_, cl) ->
                val endCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, cl.end / 60)
                    set(Calendar.MINUTE, cl.end % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (endCal.timeInMillis > System.currentTimeMillis()) {
                    transitionPoints.add(endCal.timeInMillis)
                }
            }
    }

    val sortedTransitions = transitionPoints.distinct().sorted()
    var lastTransitionMillis = 0L

    if (sortedTransitions.isEmpty()) {
        timelineBuilder.addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder()
                .setLayout(LayoutElementBuilders.Layout.Builder()
                    .setRoot(dDayTileLayout(requestParams, context, timetable, semester, Calendar.getInstance()))
                    .build())
                .build()
        )
    } else {
        sortedTransitions.forEach { transitionTime ->
            val entryBuilder = TimelineBuilders.TimelineEntry.Builder()
            if (lastTransitionMillis == 0L) {
                entryBuilder.setValidity(TimelineBuilders.TimeInterval.Builder().setEndMillis(transitionTime).build())
            } else {
                entryBuilder.setValidity(TimelineBuilders.TimeInterval.Builder().setStartMillis(lastTransitionMillis).setEndMillis(transitionTime).build())
            }

            timelineBuilder.addTimelineEntry(
                entryBuilder.setLayout(LayoutElementBuilders.Layout.Builder()
                    .setRoot(dDayTileLayout(requestParams, context, timetable, semester, Calendar.getInstance().apply {
                        timeInMillis = if (lastTransitionMillis == 0L) System.currentTimeMillis() else lastTransitionMillis
                    }))
                    .build()).build()
            )
            lastTransitionMillis = transitionTime
        }

        timelineBuilder.addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder()
                .setValidity(TimelineBuilders.TimeInterval.Builder().setStartMillis(lastTransitionMillis).build())
                .setLayout(LayoutElementBuilders.Layout.Builder()
                    .setRoot(dDayTileLayout(requestParams, context, timetable, semester, Calendar.getInstance().apply { timeInMillis = lastTransitionMillis }))
                    .build()).build()
        )
    }

    return TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setTileTimeline(timelineBuilder.build())
        .setFreshnessIntervalMillis(FRESHNESS_INTERVAL_MILLIS)
        .build()
}

private fun dDayTileLayout(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
    timetable: Timetable?,
    semester: Semester?,
    nowCal: Calendar,
): LayoutElementBuilders.LayoutElement {
    val now = nowCal.time

    val primaryColor = Colors.DEFAULT.primary

    val semesterLabel: String
    val progress: Float
    val countdownText: String
    val dDayLabel: String

    if (semester != null) {
        semesterLabel = semester.name
        val begin = semester.beginDateMillis
        val end = semester.endDateMillis

        if (now.time < begin) {
            val daysLeft = daysBetween(now, Date(begin))
            progress = 0f
            countdownText = context.getString(R.string.d_day_widget_starts_in_days, Math.abs(daysLeft))
            dDayLabel = context.getString(R.string.d_day_widget_d_minus, Math.abs(daysLeft))
        } else {
            val totalDuration = (end - begin).coerceAtLeast(TimeUnit.DAYS.toMillis(1))
            val elapsed = (now.time - begin).coerceAtLeast(0L)
            progress = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
            val daysLeft = daysBetween(now, Date(end))
            countdownText = if (daysLeft > 0) {
                context.getString(R.string.d_day_widget_ends_in_days, Math.abs(daysLeft))
            } else if (daysLeft < 0) {
                context.getString(R.string.d_day_widget_ends_in_days, 0)
            } else {
                context.getString(R.string.d_day_widget_d_day)
            }
            dDayLabel = when {
                daysLeft == 0 -> context.getString(R.string.d_day_widget_d_day)
                daysLeft > 0 -> context.getString(R.string.d_day_widget_d_minus, daysLeft)
                else -> context.getString(R.string.d_day_widget_d_plus, -daysLeft)
            }
        }
    } else {
        semesterLabel = context.getString(R.string.no_sync)
        progress = 0f
        countdownText = context.getString(R.string.d_day_widget_no_data)
        dDayLabel = "—"
    }

    val dayOfWeekString = getDayOfWeekString(nowCal)
    val currentMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
    val nextLectureData = timetable?.lectures
        ?.flatMap { lecture -> lecture.classes.map { cl -> lecture to cl } }
        ?.filter { (_, cl) -> cl.day == dayOfWeekString && cl.end > currentMinutes }
        ?.minByOrNull { (_, cl) -> cl.begin }

    fun createDotListItem(title: String, subtitle: String, dotColor: Int): LayoutElementBuilders.LayoutElement {
        return LayoutElementBuilders.Column.Builder()
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .addContent(
                LayoutElementBuilders.Row.Builder()
                    .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
                    .addContent(
                        LayoutElementBuilders.Box.Builder()
                            .setWidth(DimensionBuilders.dp(6f))
                            .setHeight(DimensionBuilders.dp(6f))
                            .setModifiers(ModifiersBuilders.Modifiers.Builder()
                                .setBackground(ModifiersBuilders.Background.Builder()
                                    .setColor(argb(dotColor))
                                    .setCorner(ModifiersBuilders.Corner.Builder().setRadius(DimensionBuilders.dp(3f)).build())
                                    .build())
                                .build())
                            .build()
                    )
                    .addContent(LayoutElementBuilders.Spacer.Builder().setWidth(DimensionBuilders.dp(4f)).build())
                    .addContent(
                        Text.Builder(context, title)
                            .setTypography(Typography.TYPOGRAPHY_BODY2)
                            .setColor(argb(dotColor))
                            .setMaxLines(1)
                            .build()
                    )
                    .build()
            )
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(1f)).build())
            .addContent(
                Text.Builder(context, subtitle)
                    .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                    .setColor(argb(Colors.DEFAULT.onSurface))
                    .build()
            )
            .build()
    }

    val innerContent = LayoutElementBuilders.Column.Builder()
        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
        .addContent(
            Text.Builder(context, dDayLabel)
                .setTypography(Typography.TYPOGRAPHY_DISPLAY3)
                .setColor(argb(Colors.DEFAULT.onSurface))
                .build()
        )
        .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(6f)).build())
        .addContent(createDotListItem(semesterLabel, countdownText, primaryColor))
        .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(8f)).build())
        .addContent(
            if (nextLectureData != null) {
                val (lecture, cl) = nextLectureData
                createDotListItem(lecture.name, formatTimeRange(cl.begin, cl.end), primaryColor)
            } else {
                createDotListItem(context.getString(R.string.no_more_classes), "Enjoy your day", primaryColor)
            }
        )
        .build()

    val progressCircle = CircularProgressIndicator.Builder()
        .setProgress(progress)
        .setCircularProgressIndicatorColors(ProgressIndicatorColors(argb(primaryColor), argb(0x33FFFFFF)))
        .setStrokeWidth(DimensionBuilders.dp(4f))
        .build()

    return EdgeContentLayout.Builder(requestParams.deviceConfiguration)
        .setEdgeContent(progressCircle)
        .setContent(innerContent)
        .setResponsiveContentInsetEnabled(true)
        .build()
}

private fun getDayOfWeekString(cal: Calendar): String = when (cal.get(Calendar.DAY_OF_WEEK)) {
    Calendar.MONDAY -> "MON"; Calendar.TUESDAY -> "TUE"; Calendar.WEDNESDAY -> "WED"
    Calendar.THURSDAY -> "THU"; Calendar.FRIDAY -> "FRI"; else -> ""
}

private fun getEndOfDayMillis(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
}.timeInMillis

private fun daysBetween(from: Date, to: Date): Int {
    val f = Calendar.getInstance().apply { time = from; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
    val t = Calendar.getInstance().apply { time = to; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
    return TimeUnit.MILLISECONDS.toDays(t.timeInMillis - f.timeInMillis).toInt()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
private fun dDayTilePreview(context: Context) = TilePreviewData(::resources) {
    val mockSemester = Semester(
        "2026 Spring",
        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(58),
        System.currentTimeMillis() + TimeUnit.DAYS.toMillis(54)
    )
    dDayTile(it, context, null, mockSemester)
}
package org.sparcs.soap.tile

import android.content.Context
import androidx.core.graphics.toColorInt
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.sparcs.soap.R
import org.sparcs.soap.data.WatchDataStore
import org.sparcs.soap.data.models.Timetable
import org.sparcs.soap.shared.formatTimeRange
import java.util.Calendar

private const val RESOURCES_VERSION = "0"
private const val FRESHNESS_INTERVAL_MILLIS = 60L * 60 * 1000

@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {
    private val watchDataStore by lazy { WatchDataStore(applicationContext) }
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest,
    ) = resources(requestParams)

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest,
    ): TileBuilders.Tile {
        val timetableJson = watchDataStore.timetableJsonFlow.first()
        val timetable = timetableJson?.let {
            try {
                json.decodeFromString<Timetable>(it)
            } catch (_: Exception) {
                null
            }
        }
        return tile(requestParams, this, timetable)
    }
}

@Suppress("UNUSED_PARAMETER")
private fun resources(
    requestParams: RequestBuilders.ResourcesRequest,
): ResourceBuilders.Resources {
    return ResourceBuilders.Resources.Builder()
        .setVersion(RESOURCES_VERSION)
        .build()
}

private fun tile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
    timetable: Timetable?,
): TileBuilders.Tile {
    val timelineBuilder = TimelineBuilders.Timeline.Builder()

    val now = Calendar.getInstance()
    val today = when (now.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "MON"
        Calendar.TUESDAY -> "TUE"
        Calendar.WEDNESDAY -> "WED"
        Calendar.THURSDAY -> "THU"
        Calendar.FRIDAY -> "FRI"
        else -> "MON"
    }

    if (timetable != null && today.isNotEmpty()) {
        val todayClasses = timetable.lectures
            .flatMap { lecture -> lecture.classes.map { cl -> lecture to cl } }
            .filter { (_, cl) -> cl.day == today }
            .sortedBy { (_, cl) -> cl.end }

        var lastTransitionMillis = 0L

        todayClasses.forEach { (_, cl) ->
            val transitionCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, cl.end / 60)
                set(Calendar.MINUTE, cl.end % 60)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val transitionMillis = transitionCalendar.timeInMillis

            if (transitionMillis > System.currentTimeMillis()) {
                val entryBuilder = TimelineBuilders.TimelineEntry.Builder()
                if (lastTransitionMillis == 0L) {
                    entryBuilder.setValidity(
                        TimelineBuilders.TimeInterval.Builder()
                            .setEndMillis(transitionMillis)
                            .build()
                    )
                } else {
                    entryBuilder.setValidity(
                        TimelineBuilders.TimeInterval.Builder()
                            .setStartMillis(lastTransitionMillis + 1000)
                            .setEndMillis(transitionMillis)
                            .build()
                    )
                }
                timelineBuilder.addTimelineEntry(
                    entryBuilder
                        .setLayout(
                            LayoutElementBuilders.Layout.Builder()
                                .setRoot(
                                    tileLayout(
                                        requestParams,
                                        context,
                                        timetable,
                                        Calendar.getInstance().apply {
                                            timeInMillis =
                                                if (lastTransitionMillis == 0L) System.currentTimeMillis() else lastTransitionMillis + 5000
                                        })
                                )
                                .build()
                        )
                        .build()
                )
                lastTransitionMillis = transitionMillis
            }
        }

        // Add the final entry for "No more classes"
        val finalEntryBuilder = TimelineBuilders.TimelineEntry.Builder()
        if (lastTransitionMillis != 0L) {
            finalEntryBuilder.setValidity(
                TimelineBuilders.TimeInterval.Builder()
                    .setStartMillis(lastTransitionMillis + 1000)
                    .build()
            )
        }
        timelineBuilder.addTimelineEntry(
            finalEntryBuilder
                .setLayout(
                    LayoutElementBuilders.Layout.Builder()
                        .setRoot(tileLayout(requestParams, context, timetable, now))
                        .build()
                )
                .build()
        )
    } else {
        // Fallback for no timetable or weekend
        timelineBuilder.addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder()
                .setLayout(
                    LayoutElementBuilders.Layout.Builder()
                        .setRoot(tileLayout(requestParams, context, timetable, now))
                        .build()
                )
                .build()
        )
    }

    return TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setTileTimeline(timelineBuilder.build())
        .setFreshnessIntervalMillis(FRESHNESS_INTERVAL_MILLIS)
        .build()
}

private fun tileLayout(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
    timetable: Timetable?,
    now: Calendar,
): LayoutElementBuilders.LayoutElement {
    val dayOfWeekString = when (now.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "MON"
        Calendar.TUESDAY -> "TUE"
        Calendar.WEDNESDAY -> "WED"
        Calendar.THURSDAY -> "THU"
        Calendar.FRIDAY -> "FRI"
        else -> ""
    }
    val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val nextLectureData = timetable?.lectures
        ?.flatMap { lecture ->
            lecture.classes.map { cl -> lecture to cl }
        }
        ?.filter { (_, cl) -> cl.day == dayOfWeekString }
        ?.filter { (_, cl) -> cl.end > currentMinutes }
        ?.minByOrNull { (_, cl) -> cl.begin }

    val content = if (timetable == null) {
        LayoutElementBuilders.Column.Builder()
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .addContent(
                Text.Builder(context, context.getString(R.string.no_sync))
                    .setColor(argb(Colors.DEFAULT.onSurface))
                    .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                    .setMaxLines(3)
                    .build()
            )
            .build()
    } else if (nextLectureData != null) {
        val (lecture, cl) = nextLectureData
        val accentColor = try {
            lecture.color?.toColorInt() ?: Colors.DEFAULT.primary
        } catch (_: Exception) {
            Colors.DEFAULT.primary
        }

        LayoutElementBuilders.Column.Builder()
            .addContent(
                Text.Builder(context, context.getString(R.string.up_next))
                    .setColor(argb(accentColor))
                    .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Spacer.Builder()
                    .setHeight(DimensionBuilders.DpProp.Builder(6f).build())
                    .build()
            )
            .addContent(
                Text.Builder(context, lecture.name)
                    .setColor(argb(Colors.DEFAULT.onSurface))
                    .setTypography(Typography.TYPOGRAPHY_TITLE3)
                    .setMaxLines(2)
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Spacer.Builder()
                    .setHeight(DimensionBuilders.DpProp.Builder(8f).build())
                    .build()
            )
            .addContent(
                Text.Builder(context, formatTimeRange(cl.begin, cl.end))
                    .setColor(argb(accentColor))
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Spacer.Builder()
                    .setHeight(DimensionBuilders.DpProp.Builder(6f).build())
                    .build()
            )
            .addContent(
                Text.Builder(context, cl.location)
                    .setColor(argb(Colors.DEFAULT.onSurface))
                    .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                    .build()
            )
            .build()
    } else {
        LayoutElementBuilders.Column.Builder()
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .addContent(
                Text.Builder(context, context.getString(R.string.no_more_classes))
                    .setColor(argb(Colors.DEFAULT.primary))
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .build()
            )
            .addContent(
                Text.Builder(context, context.getString(R.string.enjoy_day))
                    .setColor(argb(Colors.DEFAULT.onSurface))
                    .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                    .build()
            )
            .build()
    }

    return PrimaryLayout.Builder(requestParams.deviceConfiguration)
        .setResponsiveContentInsetEnabled(true)
        .setContent(content)
        .build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
private fun tilePreview(context: Context) = TilePreviewData(::resources) {
    tile(it, context, null)
}
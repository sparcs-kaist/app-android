package org.sparcs.soap.complication

import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.sparcs.soap.R
import org.sparcs.soap.data.WatchDataStore
import org.sparcs.soap.data.models.Timetable
import org.sparcs.soap.data.models.upcomingEntry
import java.time.LocalDateTime
import java.util.Locale

class UpcomingClassComplicationService : SuspendingComplicationDataSourceService() {
    private val watchDataStore by lazy { WatchDataStore(applicationContext) }
    private val json = Json { ignoreUnknownKeys = true }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        val sampleTime = "14:30"
        val sampleLecture = "CS220"
        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(sampleTime).build(),
                contentDescription = PlainComplicationText.Builder(sampleLecture).build()
            ).setTitle(PlainComplicationText.Builder(sampleLecture).build()).build()
            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(sampleLecture).build(),
                contentDescription = PlainComplicationText.Builder(sampleLecture).build()
            ).setTitle(PlainComplicationText.Builder("$sampleTime | N1").build()).build()
            else -> null
        }
    }

    private fun truncateText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            text.take(maxLength - 1) + "…"
        } else {
            text
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val timetableJson = watchDataStore.timetableJsonFlow.first()
        val timetable = timetableJson?.let {
            try { json.decodeFromString<Timetable>(it) } catch (_: Exception) { null }
        }

        val now = LocalDateTime.now()
        val currentMinutes = now.hour * 60 + now.minute
        val entry = timetable?.upcomingEntry(now)
            ?: return createNoClassData(request.complicationType)
        val cl = entry.classTime
        val isOngoing = currentMinutes >= cl.begin
        val timeLabel = if (isOngoing) getString(R.string.comp_ongoing) else formatTime(cl.begin)

        val displayTitle = entry.code.ifEmpty { entry.title }
        val icon = MonochromaticImage.Builder(
            image = Icon.createWithResource(applicationContext, R.drawable.buddy_icon_flat)
        ).build()

        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(timeLabel).build(),
                    contentDescription = PlainComplicationText.Builder(
                        getString(R.string.comp_next_class_desc, entry.title, timeLabel)
                    ).build()
                )
                    .setTitle(PlainComplicationText.Builder(truncateText(displayTitle, 10)).build())
                    .setMonochromaticImage(icon)
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                val detailText = "$timeLabel | ${cl.location}"
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(detailText).build(),
                    contentDescription = PlainComplicationText.Builder(
                        getString(R.string.comp_next_class_desc, entry.title, timeLabel)
                    ).build()
                )
                    .setTitle(PlainComplicationText.Builder(truncateText(entry.title, 20)).build())
                    .setMonochromaticImage(icon)
                    .build()
            }
            else -> null
        }
    }

    private fun createNoClassData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(getString(R.string.comp_no_class)).build(),
                contentDescription = PlainComplicationText.Builder(getString(R.string.no_more_classes)).build()
            ).build()
            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(getString(R.string.no_more_classes)).build(),
                contentDescription = PlainComplicationText.Builder(getString(R.string.no_more_classes)).build()
            ).setTitle(PlainComplicationText.Builder("Buddy").build())
                .build()
            else -> null
        }
    }

    private fun formatTime(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }

}

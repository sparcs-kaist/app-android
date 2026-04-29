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
import java.util.Calendar
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

        val now = Calendar.getInstance()
        val dayOfWeek = getDayOfWeekString(now)
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val nextLectureData = timetable?.lectures
            ?.flatMap { lecture -> lecture.classes.map { cl -> lecture to cl } }
            ?.filter { (_, cl) -> cl.day == dayOfWeek && cl.end > currentMinutes }
            ?.minByOrNull { (_, cl) -> cl.begin }

        if (nextLectureData == null) {
            return createNoClassData(request.complicationType)
        }

        val (lecture, cl) = nextLectureData
        val isOngoing = currentMinutes >= cl.begin
        val timeLabel = if (isOngoing) getString(R.string.comp_ongoing) else formatTime(cl.begin)
        
        val displayTitle = lecture.code.ifEmpty { lecture.name }
        val icon = MonochromaticImage.Builder(
            image = Icon.createWithResource(applicationContext, R.drawable.buddy_icon_flat)
        ).build()

        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> {
                val shortTitle = if (lecture.code.isNotEmpty()) lecture.code else lecture.name
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(timeLabel).build(),
                    contentDescription = PlainComplicationText.Builder(
                        getString(R.string.comp_next_class_desc, lecture.name, timeLabel)
                    ).build()
                )
                    .setTitle(PlainComplicationText.Builder(truncateText(shortTitle, 10)).build())
                    .setMonochromaticImage(icon)
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                // LongText는 공간이 넓으므로 전체 이름을 사용
                val detailText = "$timeLabel | ${cl.location}"
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(truncateText(lecture.name, 20)).build(),
                    contentDescription = PlainComplicationText.Builder(
                        getString(R.string.comp_next_class_desc, lecture.name, timeLabel)
                    ).build()
                )
                    .setTitle(PlainComplicationText.Builder(detailText).build())
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

    private fun getDayOfWeekString(cal: Calendar): String = when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "MON"
        Calendar.TUESDAY -> "TUE"
        Calendar.WEDNESDAY -> "WED"
        Calendar.THURSDAY -> "THU"
        Calendar.FRIDAY -> "FRI"
        else -> ""
    }
}
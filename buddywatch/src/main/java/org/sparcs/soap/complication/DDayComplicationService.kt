package org.sparcs.soap.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import org.sparcs.soap.R
import org.sparcs.soap.data.WatchDataStore
import org.sparcs.soap.data.models.Semester
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class DDayComplicationService : SuspendingComplicationDataSourceService() {
    private val watchDataStore by lazy { WatchDataStore(applicationContext) }
    private val json = Json { ignoreUnknownKeys = true }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createComplicationData("D-54", "Spring", type)
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val semesterJson = watchDataStore.semesterJsonFlow.firstOrNull()
        val semester = semesterJson?.let {
            try { json.decodeFromString<Semester>(it) } catch (_: Exception) { null }
        }

        if (semester == null) {
            return createComplicationData("—", "No Sync", request.complicationType)
        }

        val now = Date()
        val begin = semester.beginDateMillis
        val end = semester.endDateMillis

        val dDayLabel: String
        val description: String

        val nameParts = semester.name.split(" ")
        val yearShort = if (nameParts.isNotEmpty()) nameParts.first().takeLast(2) else ""
        val season = if (nameParts.size > 1) nameParts.last() else ""
        description = "$yearShort $season"

        if (now.time < begin) {
            val daysUntil = daysBetween(now, Date(begin))
            dDayLabel = "D-$daysUntil"
        } else {
            val daysLeft = daysBetween(now, Date(end))
            dDayLabel = if (daysLeft == 0) "D-Day" else if (daysLeft > 0) "D-$daysLeft" else "D+${Math.abs(daysLeft)}"
        }

        return createComplicationData(dDayLabel, description, request.complicationType)
    }

    private fun createComplicationData(
        label: String,
        desc: String,
        type: ComplicationType
    ): ComplicationData? {
        val icon = MonochromaticImage.Builder(
            image = android.graphics.drawable.Icon.createWithResource(applicationContext, R.drawable.buddy_icon_flat)
        ).build()

        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(label).build(),
                    contentDescription = PlainComplicationText.Builder(getString(R.string.d_day_mock_title)).build()
                )
                    .setTitle(PlainComplicationText.Builder(desc).build())
                    .setMonochromaticImage(icon)
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(label).build(),
                    contentDescription = PlainComplicationText.Builder(getString(R.string.d_day_mock_title)).build()
                )
                    .setTitle(PlainComplicationText.Builder(desc).build())
                    .setMonochromaticImage(icon)
                    .build()
            }
            else -> null
        }
    }

    private fun daysBetween(from: Date, to: Date): Int {
        val f = Calendar.getInstance().apply { time = from; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val t = Calendar.getInstance().apply { time = to; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        return TimeUnit.MILLISECONDS.toDays(t.timeInMillis - f.timeInMillis).toInt()
    }
}
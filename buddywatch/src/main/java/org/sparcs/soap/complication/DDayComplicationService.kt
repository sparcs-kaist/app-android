package org.sparcs.soap.complication

import android.app.PendingIntent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
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

    private val tapActionIntent: PendingIntent? by lazy {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        PendingIntent.getActivity(
            this, 101, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.RANGED_VALUE -> {
                createComplicationData("D-54", "", type, 40f)
            }
            else -> {
                createComplicationData("D-54", "26 Spring", type, 40f)
            }
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val semesterJson = watchDataStore.semesterJsonFlow.firstOrNull()
        val semester = semesterJson?.let {
            try {
                json.decodeFromString<Semester>(it)
            } catch (_: Exception) {
                null
            }
        }

        if (semester == null) {
            return createComplicationData("—", "No Sync", request.complicationType, 0f)
        }

        val now = Date().time
        val begin = semester.beginDateMillis
        val end = semester.endDateMillis

        val dDayLabel = calculateDDayLabel(now, begin, end)
        val nameParts = semester.name.split(" ")
        val yearShort = if (nameParts.isNotEmpty()) nameParts.first().takeLast(2) else ""
        val season = if (nameParts.size > 1) nameParts.last() else ""
        val description = "$yearShort $season"

        val totalRange = end - begin
        val progress = if (totalRange > 0) {
            ((now - begin).toFloat() / totalRange.toFloat() * 100f).coerceIn(0f, 100f)
        } else 0f

        return createComplicationData(dDayLabel, description, request.complicationType, progress)
    }

    private fun createComplicationData(
        label: String,
        desc: String,
        type: ComplicationType,
        progress: Float
    ): ComplicationData? {
        val icon = MonochromaticImage.Builder(
            image = Icon.createWithResource(applicationContext, R.drawable.buddy_icon_flat)
        ).build()

        val textContent = PlainComplicationText.Builder(label).build()
        val titleContent = PlainComplicationText.Builder(desc).build()

        return when (type) {
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = progress,
                    min = 0f,
                    max = 100f,
                    contentDescription = PlainComplicationText.Builder("Semester Progress").build()
                )
                    .setText(textContent)
                    .setMonochromaticImage(icon)
                    .setTapAction(tapActionIntent)
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = textContent,
                    contentDescription = PlainComplicationText.Builder("D-Day").build()
                )
                    .setTitle(titleContent)
                    .setMonochromaticImage(icon)
                    .setTapAction(tapActionIntent)
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(label).build(),
                    contentDescription = PlainComplicationText.Builder(getString(R.string.d_day_mock_title))
                        .build()
                )
                    .setTitle(titleContent)
                    .setMonochromaticImage(icon)
                    .setTapAction(tapActionIntent)
                    .build()
            }
            else -> null
        }
    }

    private fun calculateDDayLabel(now: Long, begin: Long, end: Long): String {
        return if (now < begin) {
            val daysUntil = daysBetween(Date(now), Date(begin))
            "D-$daysUntil"
        } else {
            val daysLeft = daysBetween(Date(now), Date(end))
            when {
                daysLeft == 0 -> "D-Day"
                daysLeft > 0 -> "D-$daysLeft"
                else -> "D+${Math.abs(daysLeft)}"
            }
        }
    }

    private fun daysBetween(from: Date, to: Date): Int {
        val f = Calendar.getInstance().apply {
            time = from; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val t = Calendar.getInstance().apply {
            time = to; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return TimeUnit.MILLISECONDS.toDays(t.timeInMillis - f.timeInMillis).toInt()
    }
}
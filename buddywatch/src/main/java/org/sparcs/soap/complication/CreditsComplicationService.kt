package org.sparcs.soap.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.sparcs.soap.R
import org.sparcs.soap.data.WatchDataStore
import org.sparcs.soap.data.models.CreditSummarySnapshot
import java.text.DecimalFormat

class CreditsComplicationService : SuspendingComplicationDataSourceService() {
    private val json = Json { ignoreUnknownKeys = true }

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        createData(type, CreditSummarySnapshot(3.73, 96, 138))

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val stored = WatchDataStore(applicationContext).creditSummaryJsonFlow.first()
        val snapshot = stored?.let { runCatching { json.decodeFromString<CreditSummarySnapshot>(it) }.getOrNull() }
        return createData(request.complicationType, snapshot)
    }

    private fun createData(type: ComplicationType, snapshot: CreditSummarySnapshot?): ComplicationData? {
        val gpa = snapshot?.gpa?.let { DecimalFormat("0.0#").format(it) } ?: "—"
        val title = PlainComplicationText.Builder(getString(R.string.credits_title)).build()
        val summary = snapshot?.let { getString(R.string.credits_progress, it.earnedCredits, it.graduationCredits) }
            ?: getString(R.string.credits_open_phone)
        val description = PlainComplicationText.Builder("${getString(R.string.credits_gpa, gpa)} · $summary").build()
        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                PlainComplicationText.Builder(gpa).build(), description,
            ).setTitle(title).build()
            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(description, description)
                .setTitle(title).build()
            ComplicationType.RANGED_VALUE -> {
                val progress = snapshot?.let {
                    if (it.graduationCredits <= 0) 1f
                    else (it.earnedCredits.toFloat() / it.graduationCredits).coerceIn(0f, 1f)
                } ?: 0f
                RangedValueComplicationData.Builder(progress, 0f, 1f, description)
                    .setText(PlainComplicationText.Builder(snapshot?.earnedCredits?.toString() ?: "—").build())
                    .setTitle(title).build()
            }
            else -> null
        }
    }
}

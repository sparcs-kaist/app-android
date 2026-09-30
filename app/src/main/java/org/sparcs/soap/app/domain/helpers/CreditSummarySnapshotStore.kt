package org.sparcs.soap.app.domain.helpers

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sparcs.soap.app.domain.models.otl.CreditSummarySnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreditSummarySnapshotStore @Inject constructor(@ApplicationContext context: Context) {
    private val preferences = context.getSharedPreferences("credit_summary", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    val snapshot: CreditSummarySnapshot?
        get() = preferences.getString("snapshot", null)?.let {
            runCatching { json.decodeFromString<CreditSummarySnapshot>(it) }.getOrNull()
        }

    fun save(snapshot: CreditSummarySnapshot) {
        preferences.edit { putString("snapshot", json.encodeToString(snapshot)) }
    }

    fun clear() {
        preferences.edit { remove("snapshot") }
    }
}

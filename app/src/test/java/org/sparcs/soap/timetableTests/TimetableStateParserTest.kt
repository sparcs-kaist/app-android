package org.sparcs.soap.timetableTests

import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.TokenStorageProtocol
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.widgets.buddyTimetableWidget.TimetableStateParser
import org.sparcs.soap.widgets.buddyTimetableWidget.TimetableUiState
import org.sparcs.soap.widgets.buddyTimetableWidget.toWidgetUiState
import java.util.Date

class TimetableStateParserTest {
    private val stateKey = stringPreferencesKey("timetable_state")

    @Test
    fun signedOutSessionDoesNotDisplayCachedTimetable() {
        val cached = Timetable("12", emptyList()).toWidgetUiState()
        val prefs = preferencesOf(stateKey to Json.encodeToString(cached))

        val state = TimetableStateParser.parse(prefs, TestTokenStorage(null))

        assertTrue(state.signInRequired)
        assertNull(state.timetable)
    }

    @Test
    fun missingAccessTokenKeepsTimetableWhenSessionCanRefresh() {
        val cached = Timetable("12", emptyList()).toWidgetUiState()
        val prefs = preferencesOf(stateKey to Json.encodeToString(cached))

        val state = TimetableStateParser.parse(prefs, TestTokenStorage("refresh-token"))

        assertEquals(cached, state)
    }

    @Test
    fun staleLoginPromptRecoversAfterSignIn() {
        val prefs = preferencesOf(stateKey to Json.encodeToString(TimetableUiState()))

        val state = TimetableStateParser.parse(prefs, TestTokenStorage("refresh-token"))

        assertFalse(state.signInRequired)
        assertTrue(state.isLoading)
    }

    private class TestTokenStorage(private var refreshToken: String?) : TokenStorageProtocol {
        override fun save(accessToken: String, refreshToken: String) {
            this.refreshToken = refreshToken
        }

        override fun getAccessToken(): String? = null
        override fun getRefreshToken(): String? = refreshToken
        override fun isTokenExpired(): Boolean = true
        override fun getTokenExpirationDate(): Date? = null
        override fun clearTokens() {
            refreshToken = null
        }
    }
}

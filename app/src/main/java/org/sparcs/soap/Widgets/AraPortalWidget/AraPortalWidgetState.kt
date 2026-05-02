package org.sparcs.soap.Widgets.AraPortalWidget

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.sparcs.soap.App.Domain.Helpers.TokenStorageProtocol
import org.sparcs.soap.App.Domain.Models.Ara.AraPost

const val STATE_KEY = "ara_portal_state"
const val KEYWORD_ENABLED_KEY = "ara_portal_keyword_enabled"
const val KEYWORD_KEY = "ara_portal_keyword"
const val SHOW_TRENDING_KEY = "ara_portal_show_trending"
const val SELECTED_BOARD_IDS_KEY = "ara_portal_selected_board_ids"

@Serializable
data class AraPortalUiState(
    val notices: List<WidgetNoticeEntry> = emptyList(),
    val signInRequired: Boolean = false,
    val isLoading: Boolean = false,
    val showTrending: Boolean = false,
    val lastUpdated: Long = 0L,
)

@Serializable
data class WidgetNoticeEntry(
    val id: Int,
    val title: String,
    val author: String,
    val boardName: String,
    val displayBoardName: String,
    val boardSlug: String,
    val iconResId: Int,
)

data class AraPortalWidgetSettings(
    val keywordEnabled: Boolean = false,
    val keywords: List<String> = emptyList(),
    val showTrending: Boolean = true,
    val selectedBoardIds: Set<Int> = emptySet(),
)

object AraPortalStateParser {
    fun parse(prefs: Preferences, tokenStorage: TokenStorageProtocol): AraPortalUiState {
        val jsonString = prefs[stringPreferencesKey(STATE_KEY)]
        if (!jsonString.isNullOrBlank()) {
            return runCatching { Json.decodeFromString<AraPortalUiState>(jsonString) }
                .getOrElse { AraPortalUiState(signInRequired = true) }
        }

        return AraPortalUiState(
            signInRequired = tokenStorage.getAccessToken() == null || tokenStorage.isTokenExpired()
        )
    }
}

fun Preferences.toAraPortalWidgetSettings(): AraPortalWidgetSettings {
    return AraPortalWidgetSettings(
        keywordEnabled = this[booleanPreferencesKey(KEYWORD_ENABLED_KEY)] ?: false,
        keywords = this[stringPreferencesKey(KEYWORD_KEY)]
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList(),
        showTrending = this[booleanPreferencesKey(SHOW_TRENDING_KEY)] ?: true,
        selectedBoardIds = this[stringPreferencesKey(SELECTED_BOARD_IDS_KEY)]
            ?.split(",")
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    )
}

fun AraPost.toWidgetEntry(displayBoardName: String? = null): WidgetNoticeEntry {
    val resolvedBoard = board
    val boardSlug = resolvedBoard?.group?.slug ?: resolvedBoard?.slug ?: "default"
    val originalBoardName = resolvedBoard?.name?.localized() ?: topic?.name?.localized() ?: "Ara"
    return WidgetNoticeEntry(
        id = id,
        title = title ?: "Untitled",
        author = author.profile.nickname.ifBlank { author.username },
        boardName = originalBoardName,
        displayBoardName = displayBoardName ?: originalBoardName,
        boardSlug = boardSlug,
        iconResId = boardIconResId(boardSlug)
    )
}

private fun boardIconResId(slug: String): Int = when (slug) {
    "notice" -> org.sparcs.soap.R.drawable.ic_widget_notice
    "talk" -> org.sparcs.soap.R.drawable.ic_widget_talk
    "club" -> org.sparcs.soap.R.drawable.ic_widget_club
    "trade" -> org.sparcs.soap.R.drawable.ic_widget_trade
    "communication" -> org.sparcs.soap.R.drawable.ic_widget_communication
    "trending" -> org.sparcs.soap.R.drawable.ic_widget_trending
    else -> org.sparcs.soap.R.drawable.ic_widget_list
}
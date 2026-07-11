package org.sparcs.soap.widgets.araPortalWidget

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TokenStorageProtocol
import org.sparcs.soap.app.domain.models.ara.AraPortalNotice
import org.sparcs.soap.app.domain.models.ara.AraPost

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
        val hasRefreshToken = tokenStorage.getRefreshToken() != null
        val jsonString = prefs[stringPreferencesKey(STATE_KEY)]
        if (!jsonString.isNullOrBlank()) {
            val decoded = runCatching { Json.decodeFromString<AraPortalUiState>(jsonString) }
                .getOrElse { AraPortalUiState(signInRequired = true) }
            if (hasRefreshToken && decoded.signInRequired) {
                return AraPortalUiState(isLoading = true)
            }
            return decoded
        }
        return if (hasRefreshToken) {
            AraPortalUiState(isLoading = true)
        } else {
            AraPortalUiState(signInRequired = true)
        }
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

fun AraPortalNotice.toWidgetEntry(boardName: String): WidgetNoticeEntry = WidgetNoticeEntry(
    id = id,
    title = title,
    author = author,
    boardName = boardName,
    displayBoardName = boardName,
    boardSlug = "portal",
    iconResId = R.drawable.ic_widget_notice,
)

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
    "notice" -> R.drawable.ic_widget_notice
    "talk" -> R.drawable.ic_widget_talk
    "club" -> R.drawable.ic_widget_club
    "trade" -> R.drawable.ic_widget_trade
    "communication" -> R.drawable.ic_widget_communication
    "trending" -> R.drawable.ic_widget_trending
    else -> R.drawable.ic_widget_list
}

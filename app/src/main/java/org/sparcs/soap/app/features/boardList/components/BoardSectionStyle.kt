package org.sparcs.soap.app.features.boardList.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Drafts
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraBoardGroup
import org.sparcs.soap.app.domain.models.ara.BoardListSection

val AraBoardGroup.icon: ImageVector
    get() = when (slug) {
        "notice" -> Icons.Outlined.NotificationsActive
        "talk" -> Icons.AutoMirrored.Outlined.Chat
        "club" -> Icons.Outlined.Group
        "trade" -> Icons.Outlined.LocalOffer
        "communication" -> Icons.Outlined.Drafts
        else -> Icons.AutoMirrored.Outlined.FormatListBulleted
    }

@Composable
fun sectionTitle(section: BoardListSection, board: AraBoard?): String = when (section) {
    BoardListSection.Trending -> stringResource(R.string.board_section_trending)
    BoardListSection.Latest -> stringResource(R.string.board_section_latest)
    is BoardListSection.Board -> board?.name?.localized() ?: section.slug
}

fun sectionIcon(section: BoardListSection, board: AraBoard?): ImageVector = when (section) {
    BoardListSection.Trending -> Icons.Outlined.LocalFireDepartment
    BoardListSection.Latest -> Icons.Outlined.AutoAwesome
    is BoardListSection.Board -> when {
        section.slug == AraBoard.MARKET_SLUG -> Icons.Outlined.ShoppingBag
        board != null -> board.group.icon
        else -> Icons.AutoMirrored.Outlined.FormatListBulleted
    }
}

val BoardListSection.isRanked: Boolean
    get() = this == BoardListSection.Trending

val BoardListSection.isMarket: Boolean
    get() = this is BoardListSection.Board && slug == AraBoard.MARKET_SLUG

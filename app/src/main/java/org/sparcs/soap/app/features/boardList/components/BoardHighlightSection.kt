@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package org.sparcs.soap.app.features.boardList.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import org.sparcs.soap.app.features.postList.components.marketGrid.MarketGridCell
import org.sparcs.soap.app.features.postList.components.marketGrid.MarketGridSkeletonCell
import org.sparcs.soap.app.features.postList.components.postListRow.metaColor
import org.sparcs.soap.app.features.postList.components.postListRow.title
import org.sparcs.soap.app.features.postList.components.postListRow.titleColor
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.features.settings.components.settingsSegmentedColors
import org.sparcs.soap.app.shared.extensions.timeAgoDisplay
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.views.contentViews.SkeletonBlock
import org.sparcs.soap.app.shared.views.contentViews.SkeletonLine
import org.sparcs.soap.app.theme.ui.Theme

private val shelfCardWidth = 150.dp
private val shelfCardShape = RoundedCornerShape(12.dp)
private val rankWidth = 20.dp
private val rowSpacing = 12.dp
private const val PLACEHOLDER_ROWS = 3
private const val PLACEHOLDER_CARDS = 4

@Composable
fun BoardHighlightSection(
    section: BoardListSection,
    board: AraBoard?,
    posts: List<AraPost>?,
    onPostClick: (AraPost) -> Unit,
    onSeeAll: (() -> Unit)?,
) {
    if (posts?.isEmpty() == true) return
    val isAwaitingBoard = section is BoardListSection.Board && board == null
    val shownPosts = posts.takeUnless { isAwaitingBoard }

    Column(Modifier.fillMaxWidth()) {
        if (isAwaitingBoard) {
            SkeletonSectionHeader()
        } else {
            SectionHeader(
                title = sectionTitle(section, board),
                icon = sectionIcon(section, board),
                onSeeAll = onSeeAll.takeIf { shownPosts != null }
            )
        }
        if (section.isMarket) {
            MarketShelf(posts = shownPosts, onPostClick = onPostClick)
        } else {
            HighlightCard(
                posts = shownPosts,
                isRanked = section.isRanked,
                showsBoard = section !is BoardListSection.Board,
                onPostClick = onPostClick
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector, onSeeAll: (() -> Unit)?) {
    Row(verticalAlignment = Alignment.Bottom) {
        SettingsSectionHeader(text = title, icon = icon, modifier = Modifier.weight(1f))
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll, shapes = ButtonDefaults.shapes(), modifier = Modifier.padding(end = 16.dp)) {
                Text(stringResource(R.string.see_all))
            }
        }
    }
}

@Composable
private fun SkeletonSectionHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 8.dp)
    ) {
        SkeletonBlock(Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        SkeletonBlock(
            Modifier
                .width(96.dp)
                .height(14.dp)
        )
    }
}

/** A section's posts as one card of segmented rows, as on the settings pages and the board list below. */
@Composable
private fun HighlightCard(
    posts: List<AraPost>?,
    isRanked: Boolean,
    showsBoard: Boolean,
    onPostClick: (AraPost) -> Unit,
) {
    val rowCount = posts?.size ?: PLACEHOLDER_ROWS
    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        repeat(rowCount) { index ->
            val rank = if (isRanked) index + 1 else null
            val post = posts?.get(index)
            val shapes = ListItemDefaults.segmentedShapes(index, rowCount)
            if (post == null) {
                HighlightSkeletonRow(rank = rank, shapes = shapes)
            } else {
                HighlightRow(
                    post = post,
                    rank = rank,
                    showsBoard = showsBoard,
                    shapes = shapes,
                    onClick = { onPostClick(post) }
                )
            }
        }
    }
}

@Composable
fun HighlightRow(
    post: AraPost,
    rank: Int?,
    showsBoard: Boolean,
    shapes: ListItemShapes,
    onClick: () -> Unit,
) {
    val details = listOfNotNull(
        post.board?.name?.localized()?.takeIf { showsBoard },
        post.createdAt.timeAgoDisplay(),
        post.commentCount.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.board_highlight_comments, it, it) }
    ).joinToString(" · ")

    SegmentedListItem(
        onClick = onClick,
        enabled = !post.isHidden,
        shapes = shapes,
        colors = settingsSegmentedColors(),
        leadingContent = rank?.let { { RankText(it) } },
        supportingContent = {
            Text(
                text = details,
                color = post.metaColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
    ) {
        Text(
            text = title(post) { stringResource(it) },
            fontWeight = FontWeight.SemiBold,
            color = post.titleColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HighlightSkeletonRow(rank: Int?, shapes: ListItemShapes) {
    SegmentedListItem(
        onClick = {},
        enabled = false,
        shapes = shapes,
        colors = settingsSegmentedColors(),
        leadingContent = rank?.let { { RankText(it) } },
        supportingContent = { SkeletonLine(MaterialTheme.typography.bodyMedium, Modifier.fillMaxWidth(0.4f)) },
    ) {
        SkeletonLine(MaterialTheme.typography.bodyLarge, Modifier.fillMaxWidth(0.7f))
    }
}

@Composable
private fun RankText(rank: Int) {
    Text(
        text = rank.toString(),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(rankWidth)
    )
}

@Composable
private fun MarketShelf(posts: List<AraPost>?, onPostClick: (AraPost) -> Unit) {
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        flingBehavior = rememberSnapFlingBehavior(listState, SnapPosition.Start),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(rowSpacing),
        userScrollEnabled = posts != null,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (posts == null) {
            items(PLACEHOLDER_CARDS) { MarketGridSkeletonCell(Modifier.width(shelfCardWidth)) }
        } else {
            items(posts, key = { it.id }) { post ->
                MarketGridCell(
                    post = post,
                    titleMaxLines = 1,
                    modifier = Modifier
                        .width(shelfCardWidth)
                        .clip(shelfCardShape)
                        .clickable(enabled = !post.isHidden) { onPostClick(post) }
                )
            }
        }
    }
}

@Composable
fun NewPostsDot(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    )
}

@Preview(showBackground = true)
@Composable
private fun TrendingPreview() {
    Theme {
        BoardHighlightSection(
            section = BoardListSection.Trending,
            board = null,
            posts = AraPost.mockList().take(3),
            onPostClick = {},
            onSeeAll = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardPreview() {
    Theme {
        BoardHighlightSection(
            section = BoardListSection.Board(AraBoard.mock().slug),
            board = AraBoard.mock(),
            posts = AraPost.mockList().take(3),
            onPostClick = {},
            onSeeAll = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarketPreview() {
    Theme {
        BoardHighlightSection(
            section = BoardListSection.Board(AraBoard.MARKET_SLUG),
            board = AraBoard.mockList().first { it.isMarket },
            posts = AraPost.mockList(),
            onPostClick = {},
            onSeeAll = {}
        )
    }
}

@Composable
private fun ReadStatePreviewContent() {
    val post = AraPost.mock()
    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        modifier = Modifier.padding(16.dp)
    ) {
        HighlightRow(
            post.copy(readStatus = AraPost.ReadStatus.UNREAD), rank = 1, showsBoard = true,
            shapes = ListItemDefaults.segmentedShapes(0, 2), onClick = {}
        )
        HighlightRow(
            post.copy(readStatus = AraPost.ReadStatus.READ), rank = 2, showsBoard = true,
            shapes = ListItemDefaults.segmentedShapes(1, 2), onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Read vs unread")
@Composable
private fun ReadStatePreview() {
    Theme { ReadStatePreviewContent() }
}

@Preview(showBackground = true, backgroundColor = 0xFF1F1F1F, name = "Read vs unread dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReadStateDarkPreview() {
    Theme { ReadStatePreviewContent() }
}

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    Theme {
        Column {
            BoardHighlightSection(
                section = BoardListSection.Trending,
                board = null,
                posts = null,
                onPostClick = {},
                onSeeAll = {}
            )
            BoardHighlightSection(
                section = BoardListSection.Board(AraBoard.MARKET_SLUG),
                board = null,
                posts = null,
                onPostClick = {},
                onSeeAll = {}
            )
        }
    }
}

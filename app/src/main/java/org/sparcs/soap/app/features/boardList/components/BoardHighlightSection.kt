package org.sparcs.soap.app.features.boardList.components

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
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import org.sparcs.soap.app.features.postList.components.postListRow.title
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
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
            TextButton(onClick = onSeeAll, modifier = Modifier.padding(end = 4.dp)) {
                Text(stringResource(R.string.see_all))
            }
        }
    }
}

@Composable
private fun SkeletonSectionHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
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

@Composable
private fun HighlightCard(
    posts: List<AraPost>?,
    isRanked: Boolean,
    showsBoard: Boolean,
    onPostClick: (AraPost) -> Unit,
) {
    val rowCount = posts?.size ?: PLACEHOLDER_ROWS
    val dividerInset = if (isRanked) 16.dp + rankWidth + rowSpacing else 16.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        repeat(rowCount) { index ->
            val rank = if (isRanked) index + 1 else null
            val post = posts?.get(index)
            if (post == null) {
                HighlightSkeletonRow(rank = rank)
            } else {
                HighlightRow(
                    post = post,
                    rank = rank,
                    showsBoard = showsBoard,
                    modifier = Modifier.clickable(enabled = !post.isHidden) { onPostClick(post) }
                )
            }
            if (index < rowCount - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = dividerInset),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
    modifier: Modifier = Modifier,
) {
    val details = listOfNotNull(
        post.board?.name?.localized()?.takeIf { showsBoard },
        post.createdAt.timeAgoDisplay(),
        post.commentCount.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.board_highlight_comments, it, it) }
    ).joinToString(" · ")

    HighlightRowLayout(rank = rank, modifier = modifier) {
        Text(
            text = title(post) { stringResource(it) },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (post.isHidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = details,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HighlightSkeletonRow(rank: Int?) {
    HighlightRowLayout(rank = rank) {
        SkeletonLine(MaterialTheme.typography.titleSmall, Modifier.fillMaxWidth(0.7f))
        SkeletonLine(MaterialTheme.typography.bodySmall, Modifier.fillMaxWidth(0.4f))
    }
}

@Composable
private fun HighlightRowLayout(
    rank: Int?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(rowSpacing),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        if (rank != null) {
            Text(
                text = rank.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(rankWidth)
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f)
        ) {
            content()
        }
    }
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

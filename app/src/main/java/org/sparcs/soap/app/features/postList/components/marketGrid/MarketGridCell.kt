package org.sparcs.soap.app.features.postList.components.marketGrid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraMarketListing
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.features.postList.components.postListRow.PostListRowCommentLabel
import org.sparcs.soap.app.features.postList.components.postListRow.UpdatedMarker
import org.sparcs.soap.app.features.postList.components.postListRow.hiddenIcon
import org.sparcs.soap.app.features.postList.components.postListRow.isRead
import org.sparcs.soap.app.features.postList.components.postListRow.isUpdatedSinceRead
import org.sparcs.soap.app.features.postList.components.postListRow.marketPriceText
import org.sparcs.soap.app.features.postList.components.postListRow.title
import org.sparcs.soap.app.shared.extensions.timeAgoDisplay
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.views.contentViews.SkeletonBlock
import org.sparcs.soap.app.shared.views.contentViews.SkeletonLine
import org.sparcs.soap.app.theme.ui.Theme

private val thumbnailShape = RoundedCornerShape(12.dp)
private val grayscale = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
fun MarketGridCell(
    post: AraPost,
    modifier: Modifier = Modifier,
    titleMaxLines: Int = 2,
) {
    val listing = post.marketListing
    val isSold = listing?.state == AraMarketListing.State.SOLD_OUT

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box {
            MarketThumbnail(post, isSold)
            if (!post.isHidden && listing != null && listing.state != AraMarketListing.State.ON_SALE) {
                MarketStateBadge(listing.state, Modifier.padding(8.dp))
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            if (!post.isHidden) {
                MarketPrice(listing, isSold)
            }

            MarketTitle(post, titleMaxLines)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = post.createdAt.timeAgoDisplay(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                PostListRowCommentLabel(post.commentCount)
            }
        }
    }
}

@Composable
private fun MarketThumbnail(post: AraPost, isSold: Boolean) {
    val url = post.thumbnailURL?.takeUnless { post.isHidden }
    var failed by remember(url) { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(thumbnailShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .alpha(if (isSold) 0.6f else 1f)
    ) {
        if (url == null || failed) {
            Icon(
                imageVector = if (post.isHidden) post.hiddenIcon else Icons.Outlined.ShoppingBag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(32.dp)
            )
        } else {
            AsyncImage(
                model = url.toString(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = if (isSold) grayscale else null,
                onState = { failed = it is AsyncImagePainter.State.Error },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun MarketStateBadge(state: AraMarketListing.State, modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
        modifier = modifier
    ) {
        Text(
            text = stringResource(
                if (state == AraMarketListing.State.RESERVED) R.string.market_state_reserved
                else R.string.market_state_sold_out
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun MarketPrice(listing: AraMarketListing?, isSold: Boolean) {
    val hasPrice = listing?.price != null
    Text(
        text = listing?.let { marketPriceText(it) } ?: stringResource(R.string.market_price_none),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textDecoration = if (isSold) TextDecoration.LineThrough else null,
        color = if (!hasPrice || isSold) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun MarketTitle(post: AraPost, maxLines: Int) {
    val accent = MaterialTheme.colorScheme.primary.copy(alpha = if (post.isRead) 0.5f else 1f)
    val text = buildAnnotatedString {
        post.topic?.takeUnless { post.isHidden }?.let { topic ->
            withStyle(SpanStyle(color = accent)) { append("[${topic.name.localized()}] ") }
        }
        append(title(post) { stringResource(it) })
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (post.isUpdatedSinceRead) {
            UpdatedMarker(Modifier.padding(end = 4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (post.isHidden || post.isRead) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MarketGridSkeletonCell(modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        SkeletonBlock(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            thumbnailShape
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            SkeletonLine(typography.titleMedium, Modifier.width(72.dp))
            SkeletonLine(typography.bodyMedium, Modifier.fillMaxWidth(0.85f))
            SkeletonLine(typography.bodySmall, Modifier.width(48.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    val listings = listOf(
        AraMarketListing(price = 15_000),
        AraMarketListing(price = 0),
        AraMarketListing(price = 2_500_000, state = AraMarketListing.State.RESERVED),
        AraMarketListing(price = 8_000, state = AraMarketListing.State.SOLD_OUT),
        AraMarketListing(price = null),
    )
    val posts = AraPost.mockList().take(6).mapIndexed { index, post ->
        post.copy(marketListing = listings.getOrNull(index))
    }
    Theme {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            items(posts, key = { it.id }) { MarketGridCell(it) }
            item { MarketGridSkeletonCell() }
        }
    }
}

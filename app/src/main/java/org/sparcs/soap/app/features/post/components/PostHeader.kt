package org.sparcs.soap.app.features.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraMarketListing
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.features.postList.components.postListRow.marketPriceText
import org.sparcs.soap.app.features.postList.components.postListRow.title as listTitle
import org.sparcs.soap.app.shared.extensions.formattedString
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.theme.ui.Theme
import java.net.URL

@Composable
fun PostHeader(post: AraPost, translatedTitle: String?, onAuthorClick: () -> Unit) {
    Column {
        Text(text = postTitle(post, translatedTitle), style = MaterialTheme.typography.titleLarge)
        post.marketListing?.takeIf { post.board?.isMarket == true }?.let { listing ->
            PostMarketListing(listing, Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetaText(post.createdAt.formattedString())
            MetaText(stringResource(R.string.views, post.views))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(vertical = 4.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable(enabled = !post.isAnonymous, role = Role.Button, onClick = onAuthorClick)
                .padding(vertical = 8.dp)
        ) {
            AuthorAvatar(post.author.profile.profilePictureURL)
            Text(text = post.author.profile.nickname, style = MaterialTheme.typography.titleSmall)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
private fun PostMarketListing(listing: AraMarketListing, modifier: Modifier = Modifier) {
    val isSold = listing.state == AraMarketListing.State.SOLD_OUT
    if (listing.price == null && listing.state == AraMarketListing.State.ON_SALE) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        Text(
            text = marketPriceText(listing) ?: stringResource(R.string.market_price_none),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textDecoration = if (isSold) TextDecoration.LineThrough else null,
            color = if (isSold || listing.price == null) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
        )
        marketStateLabel(listing.state)?.let { label ->
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun marketStateLabel(state: AraMarketListing.State): String? = when (state) {
    AraMarketListing.State.ON_SALE -> null
    AraMarketListing.State.RESERVED -> stringResource(R.string.market_state_reserved)
    AraMarketListing.State.SOLD_OUT -> stringResource(R.string.market_state_sold_out)
}

@Composable
private fun MetaText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun AuthorAvatar(url: URL?, size: Dp = 32.dp) {
    val modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    if (url != null) {
        AsyncImage(model = url.toString(), contentDescription = null, modifier = modifier)
    } else {
        Box(modifier)
    }
}

@Composable
private fun postTitle(post: AraPost, translatedTitle: String?): AnnotatedString {
    val topicName = post.topic?.name?.localized()
    val title = if (post.isHidden || translatedTitle == null) listTitle(post) { stringResource(it) } else translatedTitle
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    return buildAnnotatedString {
        topicName?.let { withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primary)) { append("[$it] ") } }
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = onSurface)) { append(title) }
    }
}

@Preview(showBackground = true)
@Composable
private fun PostHeaderPreview() {
    Theme {
        Column(Modifier.padding(16.dp)) {
            PostHeader(AraPost.mock(), translatedTitle = null, onAuthorClick = {})
            PostMarketListing(AraMarketListing(price = 15_000, state = AraMarketListing.State.SOLD_OUT))
            PostMarketListing(AraMarketListing(price = null, state = AraMarketListing.State.RESERVED))
        }
    }
}

package org.sparcs.soap.app.features.feed.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.feed.FeedImage
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.shared.mocks.feed.mock
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun PostImagesStrip(
    images: List<FeedImage>,
    modifier: Modifier = Modifier,
) {
    val hPadding = 20.dp
    val spacing = 12.dp
    val minW = 100.dp

    var revealedIds by rememberSaveable(images) { mutableStateOf(emptySet<String>()) }
    var selectedId by rememberSaveable(images) { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val cappedWidth = if (maxWidth > 600.dp) 600.dp else maxWidth
        val parentWidth = if (images.size > 1) cappedWidth - 16.dp else cappedWidth
        val maxW = parentWidth - hPadding * 2
        val height = maxW.coerceAtMost(400.dp)

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(spacing),
            contentPadding = PaddingValues(horizontal = hPadding),
            modifier = Modifier.height(height)
        ) {
            items(images, key = { it.id }) { item ->
                val covered = PostImageGallery.isCovered(item, revealedIds)
                StripImage(
                    item = item,
                    covered = covered,
                    height = height,
                    minWidth = minW,
                    maxWidth = maxW,
                    onClick = {
                        if (covered) revealedIds = revealedIds + item.id else selectedId = item.id
                    }
                )
            }
        }
    }

    selectedId?.let { id ->
        PostImagesViewer(
            images = images,
            revealedIds = revealedIds,
            selectedId = id,
            onPageChange = { image ->
                selectedId = image.id
                val index = images.indexOfFirst { it.id == image.id }
                if (index >= 0) scope.launch { listState.animateScrollToItem(index) }
            },
            onDismiss = { selectedId = null }
        )
    }
}

@Composable
private fun StripImage(
    item: FeedImage,
    covered: Boolean,
    height: Dp,
    minWidth: Dp,
    maxWidth: Dp,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val decodeHeightPx = with(LocalDensity.current) { height.roundToPx() }
    val request = remember(item.url, decodeHeightPx) {
        ImageRequest.Builder(context).data(item.url).size(decodeHeightPx * 2).build()
    }
    val painter = rememberAsyncImagePainter(request)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        when (val state = painter.state) {
            is AsyncImagePainter.State.Success -> {
                val size = state.painter.intrinsicSize
                val aspect = if (size.height > 0) size.width / size.height else 16f / 9f
                val fitWidth = height * aspect
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .height(height)
                        .width(fitWidth.coerceIn(minWidth, maxWidth))
                        .then(if (covered) Modifier.blur(50.dp) else Modifier),
                    contentScale = if (fitWidth in minWidth..maxWidth) ContentScale.Fit else ContentScale.Crop
                )
            }

            is AsyncImagePainter.State.Error -> Placeholder(
                width = minWidth,
                height = height,
                systemImage = Icons.Default.Warning
            )

            else -> Placeholder(width = minWidth, height = height)
        }

        if (covered) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(0.3f))
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.show_spoiler),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun Placeholder(width: Dp, height: Dp, systemImage: ImageVector? = null) {
    Box(
        modifier = Modifier
            .size(width, height)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        systemImage?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PostImagesStripPreview() {
    Theme { PostImagesStrip(images = FeedPost.mock().images) }
}

package org.sparcs.soap.app.features.feed.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.sparcs.soap.app.domain.models.feed.FeedImage
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.features.fullscreenImage.FullscreenImagesViewer
import org.sparcs.soap.app.shared.mocks.feed.mock
import org.sparcs.soap.app.theme.ui.Theme

@Composable
internal fun PostImagesViewer(
    images: List<FeedImage>,
    revealedIds: Set<String>,
    selectedId: String,
    onPageChange: (FeedImage) -> Unit,
    onDismiss: () -> Unit,
) {
    val gallery = PostImageGallery.of(images, revealedIds, selectedId)
    val dismiss by rememberUpdatedState(onDismiss)
    if (gallery == null) {
        LaunchedEffect(Unit) { dismiss() }
        return
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        FullscreenImagesViewer(
            imageUrls = gallery.urls,
            startIndex = gallery.startIndex,
            onDismiss = onDismiss,
            onPageChange = { page -> gallery.imageAt(page)?.let(onPageChange) }
        )
    }
}

@Preview
@Composable
private fun PostImagesViewerPreview() {
    val images = FeedPost.mock().images
    Theme {
        PostImagesViewer(
            images = images,
            revealedIds = emptySet(),
            selectedId = images.firstOrNull()?.id.orEmpty(),
            onPageChange = {},
            onDismiss = {}
        )
    }
}

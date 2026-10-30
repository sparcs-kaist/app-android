package org.sparcs.soap.app.features.fullscreenImage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.features.fullscreenImage.components.ImageActionsSheet
import org.sparcs.soap.app.features.fullscreenImage.components.ViewerTopBar
import org.sparcs.soap.app.features.fullscreenImage.components.ZoomableImagePage
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme
import timber.log.Timber

@Composable
fun FullscreenImagesViewer(
    imageUrls: List<String>,
    startIndex: Int = 0,
    onDismiss: () -> Unit,
    onPageChange: ((Int) -> Unit)? = null,
) {
    if (imageUrls.isEmpty()) return

    val scope = rememberCoroutineScope()
    val dismiss by rememberUpdatedState(onDismiss)
    val pagerState = rememberPagerState(
        initialPage = ViewerPaging.clampedIndex(startIndex, imageUrls.size),
        pageCount = { imageUrls.size }
    )
    val zoomStates = remember(imageUrls) { imageUrls.map { ImageZoomState(scope) } }
    val loadedPages = remember(imageUrls) { mutableStateMapOf<Int, Boolean>() }
    val dismissState = remember { DismissDragState(scope) }
    val actions = rememberImageActionsController()

    val currentPage = ViewerPaging.clampedIndex(pagerState.currentPage, imageUrls.size)
    val currentZoom = zoomStates[currentPage]
    val dragYDp = with(LocalDensity.current) { dismissState.offset.y.toDp().value }

    PageChangeEffect(pagerState, zoomStates, onPageChange)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = DismissDrag.backdropAlpha(dragYDp)))
            .analyticsScreen("Fullscreen Images")
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !currentZoom.isZoomed,
            modifier = Modifier
                .fillMaxSize()
                .dismissDrag(
                    state = dismissState,
                    canStart = { zoomStates.getOrNull(pagerState.currentPage)?.isZoomed != true },
                    onDismiss = { dismiss() }
                )
                .graphicsLayer {
                    val scale = DismissDrag.contentScale(dragYDp)
                    scaleX = scale
                    scaleY = scale
                    translationX = dismissState.offset.x
                    translationY = dismissState.offset.y
                }
        ) { page ->
            ZoomableImagePage(
                url = imageUrls[page],
                zoomState = zoomStates[page],
                onLoadedChange = { loadedPages[page] = it },
                onLongPress = { actions.showSheet = true }
            )
        }

        AnimatedVisibility(
            visible = !dismissState.isDragging,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ViewerTopBar(
                title = if (ViewerPaging.showsCounter(imageUrls.size)) {
                    stringResource(R.string.viewer_page_count, currentPage + 1, imageUrls.size)
                } else {
                    null
                },
                actionsEnabled = loadedPages[currentPage] == true && !actions.isBusy,
                onClose = onDismiss,
                onAction = { actions.perform(it, imageUrls[currentPage]) }
            )
        }
    }

    if (actions.showSheet && loadedPages[currentPage] == true) {
        ImageActionsSheet(
            onAction = { action ->
                actions.showSheet = false
                actions.perform(action, imageUrls[currentPage])
            },
            onDismiss = { actions.showSheet = false }
        )
    }

    GlobalAlertDialog(
        isPresented = actions.alert != null,
        state = actions.alert,
        onDismiss = { actions.alert = null }
    )
}

@Composable
private fun PageChangeEffect(
    pagerState: PagerState,
    zoomStates: List<ImageZoomState>,
    onPageChange: ((Int) -> Unit)?,
) {
    val callback by rememberUpdatedState(onPageChange)
    LaunchedEffect(pagerState, zoomStates) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .drop(1)
            .collect { page ->
                zoomStates.forEachIndexed { index, state -> if (index != page) state.reset() }
                callback?.invoke(page)
            }
    }
}

private class ImageActionsController(
    private val performer: ImageActionPerformer,
    private val launch: (suspend () -> Unit) -> Unit,
) {
    var showSheet by mutableStateOf(false)
    var isBusy by mutableStateOf(false)
        private set
    var alert by mutableStateOf<AlertState?>(null)

    fun perform(action: ImageAction, url: String) {
        if (isBusy) return
        isBusy = true
        launch {
            try {
                performer.perform(action, url)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Image action %s failed", action)
                alert = AlertState(titleResId = action.failureTitle, message = e.localizedMessage)
            } finally {
                isBusy = false
            }
        }
    }
}

@Composable
private fun rememberImageActionsController(): ImageActionsController {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember(context, scope) {
        ImageActionsController(ImageActionPerformer(context)) { block -> scope.launch { block() } }
    }
}

@Preview
@Composable
private fun FullscreenImagesViewerPreview() {
    Theme {
        FullscreenImagesViewer(
            imageUrls = listOf("https://sparcs.org/img/1.png", "https://sparcs.org/img/2.png"),
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun FullscreenImagesViewerSinglePreview() {
    Theme {
        FullscreenImagesViewer(imageUrls = listOf("https://sparcs.org/img/1.png"), onDismiss = {})
    }
}

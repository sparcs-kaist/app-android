package org.sparcs.soap.fullscreenImageTests

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.features.fullscreenImage.DismissDrag
import org.sparcs.soap.app.features.fullscreenImage.ImageZoomMath
import org.sparcs.soap.app.features.fullscreenImage.SafeInsets
import org.sparcs.soap.app.features.fullscreenImage.ViewerPaging

class FullscreenImageMathTest {
    private val container = Size(1000f, 2000f)

    @Test
    fun clampedIndexKeepsIndexInRange() {
        assertEquals(0, ViewerPaging.clampedIndex(-3, 4))
        assertEquals(3, ViewerPaging.clampedIndex(9, 4))
        assertEquals(2, ViewerPaging.clampedIndex(2, 4))
        assertEquals(0, ViewerPaging.clampedIndex(5, 0))
    }

    @Test
    fun counterOnlyShowsForSeveralImages() {
        assertFalse(ViewerPaging.showsCounter(0))
        assertFalse(ViewerPaging.showsCounter(1))
        assertTrue(ViewerPaging.showsCounter(2))
    }

    @Test
    fun dismissNeedsDistanceOrDownwardFling() {
        assertFalse(DismissDrag.shouldDismiss(DismissDrag.DISTANCE_DP - 1f, 0f))
        assertTrue(DismissDrag.shouldDismiss(DismissDrag.DISTANCE_DP + 1f, 0f))
        assertTrue(DismissDrag.shouldDismiss(20f, DismissDrag.FLING_VELOCITY_DP + 1f))
        assertFalse(DismissDrag.shouldDismiss(-20f, DismissDrag.FLING_VELOCITY_DP + 1f))
    }

    @Test
    fun dismissDirectionIsMostlyDownward() {
        assertTrue(DismissDrag.isDismissDirection(Offset(3f, 10f)))
        assertFalse(DismissDrag.isDismissDirection(Offset(10f, 3f)))
        assertFalse(DismissDrag.isDismissDirection(Offset(0f, -10f)))
    }

    @Test
    fun dragShrinksAndFadesWithinLimits() {
        assertEquals(1f, DismissDrag.contentScale(-50f), 0f)
        assertEquals(0.75f, DismissDrag.contentScale(10_000f), 0.0001f)
        assertEquals(1f, DismissDrag.backdropAlpha(0f), 0f)
        assertEquals(0.2f, DismissDrag.backdropAlpha(10_000f), 0.0001f)
    }

    @Test
    fun fittedSizeKeepsAspectRatio() {
        assertEquals(Size(1000f, 500f), ImageZoomMath.fittedSize(Size(2000f, 1000f), container))
        assertEquals(container, ImageZoomMath.fittedSize(Size.Unspecified, container))
        assertEquals(Size.Zero, ImageZoomMath.fittedSize(Size(10f, 10f), Size.Zero))
    }

    @Test
    fun offsetIsClampedToImageEdges() {
        val content = Size(1000f, 500f)
        assertEquals(Offset.Zero, ImageZoomMath.clampOffset(Offset(300f, 300f), 1f, content, container))
        assertEquals(Offset(500f, 0f), ImageZoomMath.clampOffset(Offset(900f, 300f), 2f, content, container))
    }

    @Test
    fun zoomAnchorsAtFocus() {
        val anchor = Offset(100f, 50f)
        assertEquals(Offset(-100f, -50f), ImageZoomMath.anchoredOffset(Offset.Zero, 1f, 2f, anchor))
    }

    @Test
    fun shrinkingBelowFitSettlesInsideSafeArea() {
        val content = Size(1000f, 2000f)
        val safeArea = SafeInsets(0f, 200f, 0f, 100f).areaIn(container)
        val safeFit = ImageZoomMath.safeFitScale(content, safeArea)
        assertEquals(0.85f, safeFit, 0.0001f)
        assertEquals(safeFit, ImageZoomMath.settledScale(0.6f, 1f, safeFit), 0f)
        assertEquals(1f, ImageZoomMath.settledScale(0.9f, safeFit, safeFit), 0f)
        assertEquals(Offset(0f, 50f), ImageZoomMath.safeFitOffset(safeFit, safeFit, safeArea, container))
    }

    @Test
    fun imageThatFitsSafeAreaSettlesAtFit() {
        val content = Size(1000f, 500f)
        val safeArea = Rect(0f, 200f, 1000f, 1900f)
        val safeFit = ImageZoomMath.safeFitScale(content, safeArea)
        assertEquals(1f, safeFit, 0f)
        assertEquals(1f, ImageZoomMath.settledScale(0.6f, 1f, safeFit), 0f)
        assertEquals(ImageZoomMath.MAX_SCALE, ImageZoomMath.settledScale(9f, 1f, safeFit), 0f)
    }
}

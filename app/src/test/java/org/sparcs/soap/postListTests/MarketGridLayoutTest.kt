package org.sparcs.soap.postListTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostAttachment
import org.sparcs.soap.app.features.postList.components.marketGrid.MarketGridLayout
import org.sparcs.soap.app.shared.mocks.ara.mock
import java.net.URL
import java.util.Date

class MarketGridLayoutTest {

    @Test
    fun phoneHasTwoColumns() {
        assertEquals(2, MarketGridLayout.columnCount(411f))
    }

    @Test
    fun narrowScreenKeepsTwoColumns() {
        assertEquals(2, MarketGridLayout.columnCount(200f))
        assertEquals(2, MarketGridLayout.columnCount(0f))
    }

    @Test
    fun wideScreenAddsColumns() {
        assertTrue(MarketGridLayout.columnCount(900f) >= 4)
    }

    @Test
    fun columnsFillTheWidthBetweenMargins() {
        val width = 411f
        val count = MarketGridLayout.columnCount(width)
        val used = MarketGridLayout.columnWidth(width) * count + MarketGridLayout.SPACING * (count - 1)
        assertEquals(width - MarketGridLayout.MARGIN * 2, used, 0.5f)
    }

    @Test
    fun placeholderFillsTheViewport() {
        listOf(411f to 891f, 1032f to 1376f, 1376f to 1032f).forEach { (width, height) ->
            val columns = MarketGridLayout.columnCount(width)
            val count = MarketGridLayout.placeholderCount(width, height)
            val rows = count / columns
            assertEquals(0, count % columns)
            assertTrue(rows * (MarketGridLayout.columnWidth(width) + 60f + 20f) >= height)
        }
    }

    @Test
    fun thumbnailIsTheFirstImageAttachment() {
        val file = attachment(1, "application/pdf", "https://example.com/a.pdf")
        val image = attachment(2, "image/png", "https://example.com/b.png")
        val second = attachment(3, "image/jpeg", "https://example.com/c.jpg")
        val post = AraPost.mock().copy(attachments = listOf(file, image, second))

        assertEquals(URL("https://example.com/b.png"), post.thumbnailURL)
    }

    @Test
    fun noThumbnailWithoutImageAttachments() {
        assertEquals(null, AraPost.mock().copy(attachments = null).thumbnailURL)
        val file = attachment(1, "application/pdf", "https://example.com/a.pdf")
        assertEquals(null, AraPost.mock().copy(attachments = listOf(file)).thumbnailURL)
    }

    private fun attachment(id: Int, mimeType: String, url: String) = AraPostAttachment(
        id = id,
        createdAt = Date(),
        file = URL(url),
        filename = url.substringAfterLast('/'),
        size = 0,
        mimeType = mimeType
    )
}

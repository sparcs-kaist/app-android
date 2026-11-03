package org.sparcs.soap.feedTests

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.sparcs.soap.app.features.feed.feedSplit

class FeedSplitTest {
    @Test
    fun `panel requires room for both columns and stops growing at maximum width`() {
        assertNull(feedSplit(679.dp))
        assertEquals(320.dp, feedSplit(680.dp)?.panelWidth)
        assertEquals(400.dp, feedSplit(1200.dp)?.panelWidth)
    }

    @Test
    fun `vertical hinge stays in gutter even on a smaller folding display`() {
        val split = feedSplit(600.dp, verticalFold = 280.dp..300.dp)
        assertEquals(280.dp, split?.feedWidth)
        assertEquals(20.dp, split?.gutter)
        assertEquals(300.dp, split?.panelWidth)
    }

    @Test
    fun `horizontal hinge and unusably narrow folding columns disable panel`() {
        assertNull(feedSplit(1200.dp, hasHorizontalFold = true))
        assertNull(feedSplit(800.dp, verticalFold = 239.dp..260.dp))
        assertNull(feedSplit(800.dp, verticalFold = 550.dp..561.dp))
        assertEquals(240.dp, feedSplit(500.dp, verticalFold = 240.dp..260.dp)?.panelWidth)
    }
}

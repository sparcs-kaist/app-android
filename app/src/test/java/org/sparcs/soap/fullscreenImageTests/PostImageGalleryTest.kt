package org.sparcs.soap.fullscreenImageTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.sparcs.soap.app.domain.models.feed.FeedImage
import org.sparcs.soap.app.features.feed.components.PostImageGallery

class PostImageGalleryTest {
    private fun image(id: String, spoiler: Boolean?) = FeedImage(id, "https://img/$id", "image/png", 1, spoiler)

    private val images = listOf(image("a", false), image("b", true), image("c", null), image("d", true))

    @Test
    fun coveredSpoilersAreLeftOutAndIndexIsMapped() {
        val gallery = PostImageGallery.of(images, revealedIds = emptySet(), selectedId = "c")!!
        assertEquals(listOf("a", "c"), gallery.images.map { it.id })
        assertEquals(1, gallery.startIndex)
        assertEquals(listOf("https://img/a", "https://img/c"), gallery.urls)
    }

    @Test
    fun revealedSpoilersAreIncluded() {
        val gallery = PostImageGallery.of(images, revealedIds = setOf("b", "d"), selectedId = "d")!!
        assertEquals(listOf("a", "b", "c", "d"), gallery.images.map { it.id })
        assertEquals(3, gallery.startIndex)
    }

    @Test
    fun coveredOrMissingSelectionHasNoGallery() {
        assertNull(PostImageGallery.of(images, revealedIds = emptySet(), selectedId = "b"))
        assertNull(PostImageGallery.of(images, revealedIds = emptySet(), selectedId = "z"))
        assertNull(PostImageGallery.of(emptyList(), revealedIds = emptySet(), selectedId = "a"))
    }

    @Test
    fun imageAtToleratesOutOfRangePages() {
        val gallery = PostImageGallery.of(images, revealedIds = emptySet(), selectedId = "a")!!
        assertEquals("c", gallery.imageAt(1)?.id)
        assertNull(gallery.imageAt(5))
        assertNull(gallery.imageAt(-1))
    }
}

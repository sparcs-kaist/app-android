package org.sparcs.soap.app.features.feed.components

import org.sparcs.soap.app.domain.models.feed.FeedImage

data class PostImageGallery(val images: List<FeedImage>, val startIndex: Int) {
    val urls: List<String> get() = images.map { it.url }

    fun imageAt(page: Int): FeedImage? = images.getOrNull(page)

    companion object {
        fun isCovered(image: FeedImage, revealedIds: Set<String>): Boolean =
            image.spoiler == true && image.id !in revealedIds

        fun of(images: List<FeedImage>, revealedIds: Set<String>, selectedId: String): PostImageGallery? {
            val visible = images.filterNot { isCovered(it, revealedIds) }
            val index = visible.indexOfFirst { it.id == selectedId }
            return if (index < 0) null else PostImageGallery(visible, index)
        }
    }
}

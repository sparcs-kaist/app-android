package org.sparcs.soap.app.features.postList

import org.sparcs.soap.app.domain.models.ara.AraPost

fun AraPost.refreshed(updated: AraPost): AraPost = copy(
    upVotes = updated.upVotes,
    downVotes = updated.downVotes,
    commentCount = updated.commentCount,
    readStatus = updated.readStatus?.takeUnless { it == AraPost.ReadStatus.UNREAD } ?: readStatus
)

fun AraPost.markedRead(): AraPost = copy(readStatus = AraPost.ReadStatus.READ)

fun List<AraPost>.updating(postID: Int, transform: (AraPost) -> AraPost): List<AraPost>? {
    val idx = indexOfFirst { it.id == postID }
    if (idx == -1) return null
    val updated = transform(this[idx])
    if (updated == this[idx]) return null
    return toMutableList().apply { set(idx, updated) }
}

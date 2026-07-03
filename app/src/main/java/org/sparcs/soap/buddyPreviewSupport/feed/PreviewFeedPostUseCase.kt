package org.sparcs.soap.buddyPreviewSupport.feed

import org.sparcs.soap.app.domain.enums.feed.FeedReportType
import org.sparcs.soap.app.domain.enums.feed.FeedVoteType
import org.sparcs.soap.app.domain.models.feed.FeedCreatePost
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.domain.models.feed.FeedPostPage
import org.sparcs.soap.app.domain.usecases.feed.FeedPostUseCaseProtocol
import org.sparcs.soap.app.shared.mocks.feed.mock

class PreviewFeedPostUseCase : FeedPostUseCaseProtocol {
    override suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage {
        return FeedPostPage(
            items = emptyList(),
            nextCursor = null,
            hasNext = false
        )
    }

    override suspend fun fetchPost(postID: String): FeedPost {
        return FeedPost.mock()
    }
    override suspend fun writePost(request: FeedCreatePost) {}
    override suspend fun deletePost(postID: String) {}
    override suspend fun vote(postID: String, type: FeedVoteType) {}
    override suspend fun deleteVote(postID: String) {}
    override suspend fun reportPost(postID: String, reason: FeedReportType, detail: String) {}
}
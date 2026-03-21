package org.sparcs.soap.BuddyPreviewSupport.Feed

import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Models.Feed.FeedCreatePost
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Domain.Models.Feed.FeedPostPage
import org.sparcs.soap.App.Domain.Usecases.Feed.FeedPostUseCaseProtocol
import org.sparcs.soap.App.Shared.Mocks.Feed.mock

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
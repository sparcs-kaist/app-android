package org.sparcs.soap.BuddyTestSupport.UseCase

import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Models.Feed.FeedCreatePost
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Domain.Models.Feed.FeedPostPage
import org.sparcs.soap.App.Domain.Usecases.Feed.FeedPostUseCaseProtocol
import org.sparcs.soap.App.Shared.Mocks.mock

class MockFeedPostUseCase : FeedPostUseCaseProtocol {
    var fetchPostsResult: Result<FeedPostPage> = Result.success(FeedPostPage(items = emptyList(), nextCursor = null, hasNext = false))
    var fetchPostResult: Result<FeedPost> = Result.success(FeedPost.mock())
    var deletePostResult: Result<Unit> = Result.success(Unit)
    var voteResult: Result<Unit> = Result.success(Unit)
    var deleteVoteResult: Result<Unit> = Result.success(Unit)
    var reportPostResult: Result<Unit> = Result.success(Unit)
    var writePostResult: Result<Unit> = Result.success(Unit)

    var fetchPostsCallCount = 0
    var fetchPostCallCount = 0
    var deletePostCallCount = 0
    var voteCallCount = 0
    var deleteVoteCallCount = 0
    var reportPostCallCount = 0

    var lastFetchPostID: String? = null
    var lastVotePostID: String? = null
    var lastVoteType: FeedVoteType? = null
    var lastDeleteVotePostID: String? = null
    var lastDeletePostID: String? = null
    var lastReportPostID: String? = null
    var lastReportReason: FeedReportType? = null

    override suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage {
        fetchPostsCallCount += 1
        return fetchPostsResult.getOrThrow()
    }

    override suspend fun fetchPost(postID: String): FeedPost {
        fetchPostCallCount += 1
        lastFetchPostID = postID
        return fetchPostResult.getOrThrow()
    }

    override suspend fun writePost(request: FeedCreatePost) {
        writePostResult.getOrThrow()
    }

    override suspend fun deletePost(postID: String) {
        deletePostCallCount += 1
        lastDeletePostID = postID
        deletePostResult.getOrThrow()
    }

    override suspend fun vote(postID: String, type: FeedVoteType) {
        voteCallCount += 1
        lastVotePostID = postID
        lastVoteType = type
        voteResult.getOrThrow()
    }

    override suspend fun deleteVote(postID: String) {
        deleteVoteCallCount += 1
        lastDeleteVotePostID = postID
        deleteVoteResult.getOrThrow()
    }

    override suspend fun reportPost(postID: String, reason: FeedReportType, detail: String) {
        reportPostCallCount += 1
        lastReportPostID = postID
        lastReportReason = reason
        reportPostResult.getOrThrow()
    }
}
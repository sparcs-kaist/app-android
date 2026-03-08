package org.sparcs.soap.BuddyTestSupport.UseCase

import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Models.Feed.FeedComment
import org.sparcs.soap.App.Domain.Models.Feed.FeedCreateComment
import org.sparcs.soap.App.Domain.Usecases.Feed.FeedCommentUseCaseProtocol
import org.sparcs.soap.BuddyTestSupport.Error.TestError

class MockFeedCommentUseCase : FeedCommentUseCaseProtocol {
    var fetchCommentsResult: Result<List<FeedComment>> = Result.success(emptyList())
    var writeCommentResult: Result<FeedComment>? = null
    var writeReplyResult: Result<FeedComment>? = null
    var deleteCommentResult: Result<Unit> = Result.success(Unit)
    var voteResult: Result<Unit> = Result.success(Unit)
    var deleteVoteResult: Result<Unit> = Result.success(Unit)
    var reportCommentResult: Result<Unit> = Result.success(Unit)

    var voteCallCount = 0
    var deleteVoteCallCount = 0
    var deleteCommentCallCount = 0
    var reportCommentCallCount = 0

    var lastVoteCommentID: String? = null
    var lastVoteType: FeedVoteType? = null
    var lastDeleteVoteCommentID: String? = null
    var lastDeleteCommentID: String? = null
    var lastReportCommentID: String? = null
    var lastReportReason: FeedReportType? = null

    override suspend fun fetchComments(postID: String): List<FeedComment> {
        return fetchCommentsResult.getOrThrow()
    }

    override suspend fun writeComment(postID: String, request: FeedCreateComment): FeedComment {
        val result = writeCommentResult ?: throw TestError.NotConfigured
        return result.getOrThrow()
    }

    override suspend fun writeReply(commentID: String, request: FeedCreateComment): FeedComment {
        val result = writeReplyResult ?: throw TestError.NotConfigured
        return result.getOrThrow()
    }

    override suspend fun deleteComment(commentID: String) {
        deleteCommentCallCount += 1
        lastDeleteCommentID = commentID
        deleteCommentResult.getOrThrow()
    }

    override suspend fun vote(commentID: String, type: FeedVoteType) {
        voteCallCount += 1
        lastVoteCommentID = commentID
        lastVoteType = type
        voteResult.getOrThrow()
    }

    override suspend fun deleteVote(commentID: String) {
        deleteVoteCallCount += 1
        lastDeleteVoteCommentID = commentID
        deleteVoteResult.getOrThrow()
    }

    override suspend fun reportComment(commentID: String, reason: FeedReportType, detail: String) {
        reportCommentCallCount += 1
        lastReportCommentID = commentID
        lastReportReason = reason
        reportCommentResult.getOrThrow()
    }
}
package org.sparcs.soap.BuddyPreviewSupport.Feed

import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Models.Feed.FeedComment
import org.sparcs.soap.App.Domain.Models.Feed.FeedCreateComment
import org.sparcs.soap.App.Domain.Usecases.Feed.FeedCommentUseCaseProtocol
import org.sparcs.soap.BuddyTestSupport.Helper.UseCaseTestFixtures

class PreviewFeedCommentUseCase : FeedCommentUseCaseProtocol {
    override suspend fun fetchComments(postID: String): List<FeedComment> {
        return emptyList()
    }

    override suspend fun writeComment(postID: String, request: FeedCreateComment): FeedComment {
        return UseCaseTestFixtures.makeComment()
    }

    override suspend fun writeReply(commentID: String, request: FeedCreateComment): FeedComment {
        return UseCaseTestFixtures.makeComment()
    }

    override suspend fun deleteComment(commentID: String) {}

    override suspend fun vote(commentID: String, type: FeedVoteType) {}

    override suspend fun deleteVote(commentID: String) {}

    override suspend fun reportComment(commentID: String, reason: FeedReportType, detail: String) {}
}
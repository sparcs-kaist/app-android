package org.sparcs.soap.buddyPreviewSupport.feed

import org.sparcs.soap.app.domain.enums.feed.FeedReportType
import org.sparcs.soap.app.domain.enums.feed.FeedVoteType
import org.sparcs.soap.app.domain.models.feed.FeedComment
import org.sparcs.soap.app.domain.models.feed.FeedCreateComment
import org.sparcs.soap.app.domain.usecases.feed.FeedCommentUseCaseProtocol
import org.sparcs.soap.buddyTestSupport.helper.UseCaseTestFixtures

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
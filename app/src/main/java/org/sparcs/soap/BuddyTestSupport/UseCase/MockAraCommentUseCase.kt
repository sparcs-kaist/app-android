package org.sparcs.soap.BuddyTestSupport.UseCase

import org.sparcs.soap.App.Domain.Enums.Ara.AraContentReportType
import org.sparcs.soap.App.Domain.Models.Ara.AraPostComment
import org.sparcs.soap.App.Domain.Usecases.Ara.AraCommentUseCaseProtocol
import org.sparcs.soap.App.Shared.Mocks.Ara.mock

class MockAraCommentUseCase : AraCommentUseCaseProtocol {

    var writeCommentResult: Result<AraPostComment> = Result.success(AraPostComment.mock())
    var writeThreadedCommentResult: Result<AraPostComment> = Result.success(AraPostComment.mock())
    var editCommentResult: Result<AraPostComment> = Result.success(AraPostComment.mock())
    var deleteCommentResult: Result<Unit> = Result.success(Unit)
    var upvoteCommentResult: Result<Unit> = Result.success(Unit)
    var downvoteCommentResult: Result<Unit> = Result.success(Unit)
    var cancelVoteResult: Result<Unit> = Result.success(Unit)
    var reportCommentResult: Result<Unit> = Result.success(Unit)

    override suspend fun upVoteComment(commentID: Int) {
        upvoteCommentResult.getOrThrow()
    }

    override suspend fun downVoteComment(commentID: Int) {
        downvoteCommentResult.getOrThrow()
    }

    override suspend fun cancelVote(commentID: Int) {
        cancelVoteResult.getOrThrow()
    }

    override suspend fun writeComment(postID: Int, content: String): AraPostComment {
        return writeCommentResult.getOrThrow()
    }

    override suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment {
        return writeThreadedCommentResult.getOrThrow()
    }

    override suspend fun deleteComment(commentID: Int) {
        deleteCommentResult.getOrThrow()
    }

    override suspend fun editComment(commentID: Int, content: String): AraPostComment {
        return editCommentResult.getOrThrow()
    }

    override suspend fun reportComment(commentID: Int, type: AraContentReportType) {
        reportCommentResult.getOrThrow()
    }
}
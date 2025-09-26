package com.example.soap.Domain.Repositories.Ara

import com.example.soap.Domain.Enums.AraContentReportType
import com.example.soap.Domain.Models.Ara.AraPostComment
import com.example.soap.Networking.RetrofitAPI.Ara.AraCommentApi
import com.example.soap.Networking.RetrofitAPI.Ara.CommentPatchRequest
import com.example.soap.Networking.RetrofitAPI.Ara.CommentPostRequest
import com.example.soap.Networking.RetrofitAPI.Ara.CommentReportRequest
import com.example.soap.Networking.RetrofitAPI.Ara.ThreadedCommentPostRequest
import com.example.soap.Shared.Mocks.mock
import javax.inject.Inject

interface AraCommentRepositoryProtocol {

    suspend fun upVoteComment(commentID: Int)
    suspend fun downVoteComment(commentID: Int)
    suspend fun cancelVote(commentID: Int)
    suspend fun writeComment(postID: Int, content: String): AraPostComment
    suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment
    suspend fun deleteComment(commentID: Int)
    suspend fun editComment(commentID: Int, content: String): AraPostComment
    suspend fun reportComment(commentID: Int, type: AraContentReportType)
}

class FakeAraCommentRepository : AraCommentRepositoryProtocol {

    override suspend fun upVoteComment(commentID: Int) { /* no-op */
    }

    override suspend fun downVoteComment(commentID: Int) { /* no-op */
    }

    override suspend fun cancelVote(commentID: Int) { /* no-op */
    }

    override suspend fun writeComment(postID: Int, content: String) = AraPostComment.mock()
    override suspend fun writeThreadedComment(commentID: Int, content: String) =
        AraPostComment.mock()

    override suspend fun deleteComment(commentID: Int) { /* no-op */
    }

    override suspend fun editComment(commentID: Int, content: String) = AraPostComment.mock()
    override suspend fun reportComment(commentID: Int, type: AraContentReportType) { /* no-op */
    }

}

class AraCommentRepository @Inject constructor(
    private val araCommentApi: AraCommentApi
) : AraCommentRepositoryProtocol {

    override suspend fun upVoteComment(commentID: Int) {
        araCommentApi.upVoteComment(commentID)
    }

    override suspend fun downVoteComment(commentID: Int) {
        araCommentApi.downVoteComment(commentID)
    }

    override suspend fun cancelVote(commentID: Int) {
        araCommentApi.cancelVote(commentID)
    }

    override suspend fun writeComment(postID: Int, content: String): AraPostComment {
        val dto = araCommentApi.writeComment(
            CommentPostRequest(parent_article = postID, content = content)
        )
        return dto.toModel()
    }

    override suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment {
        val dto = araCommentApi.writeThreadedComment(
            ThreadedCommentPostRequest(parent_comment = commentID, content = content)
        )
        return dto.toModel()
    }

    override suspend fun deleteComment(commentID: Int) {
        araCommentApi.deleteComment(commentID)
    }

    override suspend fun editComment(commentID: Int, content: String): AraPostComment {
        val dto = araCommentApi.editComment(
            commentID,
            CommentPatchRequest(content = content)
        )
        return dto.toModel()
    }

    override suspend fun reportComment(commentID: Int, type: AraContentReportType) {
        araCommentApi.reportComment(
            CommentReportRequest(
                parent_comment = commentID,
                type = "others",
                content = type.name
            )
        )
    }
}

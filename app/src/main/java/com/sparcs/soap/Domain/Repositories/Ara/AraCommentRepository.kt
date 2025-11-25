package com.sparcs.soap.Domain.Repositories.Ara

import com.google.gson.Gson
import com.sparcs.soap.Domain.Enums.Ara.AraContentReportType
import com.sparcs.soap.Domain.Models.Ara.AraPostComment
import com.sparcs.soap.Networking.ResponseDTO.handleApiError
import com.sparcs.soap.Networking.RetrofitAPI.Ara.AraCommentApi
import com.sparcs.soap.Networking.RetrofitAPI.Ara.CommentPatchRequest
import com.sparcs.soap.Networking.RetrofitAPI.Ara.CommentPostRequest
import com.sparcs.soap.Networking.RetrofitAPI.Ara.CommentReportRequest
import com.sparcs.soap.Networking.RetrofitAPI.Ara.ThreadedCommentPostRequest
import com.sparcs.soap.Shared.Mocks.mock
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

class AraCommentRepository @Inject constructor(
    private val araCommentApi: AraCommentApi,
    private val gson: Gson = Gson(),
) : AraCommentRepositoryProtocol {

    override suspend fun upVoteComment(commentID: Int) = try {
        araCommentApi.upVoteComment(commentID)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun downVoteComment(commentID: Int) = try {
        araCommentApi.downVoteComment(commentID)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun cancelVote(commentID: Int) = try {
        araCommentApi.cancelVote(commentID)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun writeComment(postID: Int, content: String): AraPostComment {
        try {
            val dto = araCommentApi.writeComment(
                CommentPostRequest(parent_article = postID, content = content)
            )
            return dto.toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment {
        try {
            val dto = araCommentApi.writeThreadedComment(
                ThreadedCommentPostRequest(parent_comment = commentID, content = content)
            )
            return dto.toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun deleteComment(commentID: Int) = try {
        araCommentApi.deleteComment(commentID)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun editComment(commentID: Int, content: String): AraPostComment {
        try {
            val dto = araCommentApi.editComment(
                commentID,
                CommentPatchRequest(content = content)
            )
            return dto.toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun reportComment(commentID: Int, type: AraContentReportType) = try {
        araCommentApi.reportComment(
            CommentReportRequest(
                parent_comment = commentID,
                type = "others",
                content = type.name
            )
        )
    } catch (e: Exception) {
        handleApiError(gson, e)
    }
}

class FakeAraCommentRepository : AraCommentRepositoryProtocol {
    override suspend fun upVoteComment(commentID: Int) {}
    override suspend fun downVoteComment(commentID: Int) {}
    override suspend fun cancelVote(commentID: Int) {}
    override suspend fun writeComment(postID: Int, content: String) = AraPostComment.mock()
    override suspend fun writeThreadedComment(commentID: Int, content: String) = AraPostComment.mock()
    override suspend fun deleteComment(commentID: Int) {}
    override suspend fun editComment(commentID: Int, content: String) = AraPostComment.mock()
    override suspend fun reportComment(commentID: Int, type: AraContentReportType) {}
}
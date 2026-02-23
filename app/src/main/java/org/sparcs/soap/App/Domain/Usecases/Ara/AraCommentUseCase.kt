package org.sparcs.soap.App.Domain.Usecases.Ara

import org.sparcs.soap.App.Domain.Enums.Ara.AraContentReportType
import org.sparcs.soap.App.Domain.Error.Ara.AraCommentUseCaseError
import org.sparcs.soap.App.Domain.Error.CrashContext
import org.sparcs.soap.App.Domain.Error.NetworkError
import org.sparcs.soap.App.Domain.Models.Ara.AraPostComment
import org.sparcs.soap.App.Domain.Repositories.Ara.AraCommentRepositoryProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import javax.inject.Inject

interface AraCommentUseCaseProtocol {
    suspend fun upVoteComment(commentID: Int)
    suspend fun downVoteComment(commentID: Int)
    suspend fun cancelVote(commentID: Int)
    suspend fun writeComment(
        postID: Int,
        content: String
    ): AraPostComment
    suspend fun writeThreadedComment(
        commentID: Int,
        content: String
    ): AraPostComment
    suspend fun deleteComment(commentID: Int)
    suspend fun editComment(
        commentID: Int,
        content: String
    ): AraPostComment
    suspend fun reportComment(
        commentID: Int,
        type: AraContentReportType
    )
}

class AraCommentUseCase @Inject constructor(
    private val araCommentRepository: AraCommentRepositoryProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol?
) : AraCommentUseCaseProtocol {

    private val feature: String = "AraComment"

    override suspend fun upVoteComment(commentID: Int) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf("commentID" to commentID.toString())
        )
        execute(context) {
            araCommentRepository.upVoteComment(commentID)
        }
    }

    override suspend fun downVoteComment(commentID: Int) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf("commentID" to commentID.toString())
        )
        execute(context) {
            araCommentRepository.downVoteComment(commentID)
        }
    }

    override suspend fun cancelVote(commentID: Int) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf("commentID" to commentID.toString())
        )
        execute(context) {
            araCommentRepository.cancelVote(commentID)
        }
    }

    override suspend fun writeComment(postID: Int, content: String): AraPostComment {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "postID" to postID.toString(),
                "content" to content
            )
        )
        return execute(context) {
            araCommentRepository.writeComment(postID, content)
        }
    }

    override suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "commentID" to commentID.toString(),
                "content" to content
            )
        )
        return execute(context) {
            araCommentRepository.writeThreadedComment(commentID, content)
        }
    }

    override suspend fun deleteComment(commentID: Int) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf("commentID" to commentID.toString())
        )
        execute(context) {
            araCommentRepository.deleteComment(commentID)
        }
    }

    override suspend fun editComment(commentID: Int, content: String): AraPostComment {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "commentID" to commentID.toString(),
                "content" to content
            )
        )
        return execute(context) {
            araCommentRepository.editComment(commentID, content)
        }
    }

    override suspend fun reportComment(commentID: Int, type: AraContentReportType) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "commentID" to commentID.toString(),
                "type" to type.toString()
            )
        )
        execute(context) {
            araCommentRepository.reportComment(commentID, type)
        }
    }

    private suspend fun <T> execute(
        context: CrashContext,
        operation: suspend () -> T
    ): T {
        return try {
            operation()
        } catch (networkError: NetworkError) {
            crashlyticsService?.record(networkError as Throwable, context)
            throw networkError
        } catch (e: Exception) {
            val mappedError = AraCommentUseCaseError.Unknown(e)
            crashlyticsService?.record(mappedError as Throwable, context)
            throw mappedError
        }
    }
}
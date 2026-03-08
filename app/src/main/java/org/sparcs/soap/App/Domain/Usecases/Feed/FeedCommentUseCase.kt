package org.sparcs.soap.App.Domain.Usecases.Feed

import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Error.CrashContext
import org.sparcs.soap.App.Domain.Error.Feed.FeedCommentUseCaseError
import org.sparcs.soap.App.Domain.Error.NetworkError
import org.sparcs.soap.App.Domain.Models.Feed.FeedComment
import org.sparcs.soap.App.Domain.Models.Feed.FeedCreateComment
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import javax.inject.Inject

interface FeedCommentUseCaseProtocol {
    suspend fun fetchComments(postID: String): List<FeedComment>
    suspend fun writeComment(postID: String, request: FeedCreateComment): FeedComment
    suspend fun writeReply(commentID: String, request: FeedCreateComment): FeedComment
    suspend fun deleteComment(commentID: String)
    suspend fun vote(commentID: String, type: FeedVoteType)
    suspend fun deleteVote(commentID: String)
    suspend fun reportComment(commentID: String, reason: FeedReportType, detail: String)
}

class FeedCommentUseCase @Inject constructor(
    private val feedCommentRepository: FeedCommentRepositoryProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol?,
) : FeedCommentUseCaseProtocol {
    // MARK: - Properties
    private val feature = "FeedComment"

    // MARK: - Functions
    override suspend fun fetchComments(postID: String): List<FeedComment> {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf("postID" to postID)
        )
        return execute(context) {
            feedCommentRepository.fetchComments(postID)
        }
    }

    override suspend fun writeComment(postID: String, request: FeedCreateComment): FeedComment {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "postID" to postID,
                "content" to request.content,
                "isAnonymous" to "${request.isAnonymous}",
                "hasImages" to "${request.image != null}"
            )
        )
        return execute(context) {
            feedCommentRepository.writeComment(postID, request)
        }
    }

    override suspend fun writeReply(commentID: String, request: FeedCreateComment): FeedComment {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "commentID" to commentID,
                "content" to request.content,
                "isAnonymous" to "${request.isAnonymous}",
                "hasImages" to "${request.image != null}"
            )
        )
        return execute(context) {
            feedCommentRepository.writeReply(commentID, request)
        }
    }

    override suspend fun deleteComment(commentID: String) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf("commentID" to commentID)
        )
        execute(context) {
            feedCommentRepository.deleteComment(commentID)
        }
    }

    override suspend fun vote(commentID: String, type: FeedVoteType) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "commentID" to commentID,
                "type" to type.name
            )
        )
        execute(context) {
            feedCommentRepository.vote(commentID, type)
        }
    }

    override suspend fun deleteVote(commentID: String) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf("commentID" to commentID)
        )
        execute(context) {
            feedCommentRepository.deleteVote(commentID)
        }
    }

    override suspend fun reportComment(commentID: String, reason: FeedReportType, detail: String) {
        val context = CrashContext(
            feature = feature, metadata = mapOf(
                "commentID" to commentID,
                "reason" to reason.name,
                "detail" to detail
            )
        )
        execute(context) {
            feedCommentRepository.reportComment(commentID, reason, detail)
        }
    }

    // MARK: - Private
    private suspend fun <T> execute(
        context: CrashContext,
        operation: suspend () -> T,
    ): T {
        return try {
            operation()
        } catch (e: Exception) {
            when (e) {
                is NetworkError.ServerError -> {
                    if (e.code == 409) {
                        throw FeedCommentUseCaseError.CannotDeleteCommentWithVote(e.message)
                    }
                    crashlyticsService?.record(error = e as Throwable, context = context)
                    throw e
                }

                is NetworkError -> {
                    crashlyticsService?.record(error = e as Throwable, context = context)
                    throw e
                }

                else -> {
                    val mappedError = FeedCommentUseCaseError.Unknown(e)
                    crashlyticsService?.record(error = mappedError as Throwable, context = context)
                    throw mappedError
                }
            }
        }
    }
}
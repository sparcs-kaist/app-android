package org.sparcs.soap.App.Domain.Usecases.Feed

import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Error.CrashContext
import org.sparcs.soap.App.Domain.Error.Feed.FeedPostUseCaseError
import org.sparcs.soap.App.Domain.Error.NetworkError
import org.sparcs.soap.App.Domain.Models.Feed.FeedCreatePost
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Domain.Models.Feed.FeedPostPage
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import javax.inject.Inject

interface FeedPostUseCaseProtocol {
    suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage
    suspend fun fetchPost(postID: String): FeedPost
    suspend fun writePost(request: FeedCreatePost)
    suspend fun deletePost(postID: String)
    suspend fun vote(postID: String, type: FeedVoteType)
    suspend fun deleteVote(postID: String)
    suspend fun reportPost(postID: String, reason: FeedReportType, detail: String)
}

class FeedPostUseCase @Inject constructor(
    private val feedPostRepository: FeedPostRepositoryProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol?
) : FeedPostUseCaseProtocol {
    // MARK: - Properties
    private val feature: String = "FeedPost"

    // MARK: - Functions
    override suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "cursor" to (cursor ?: "null"),
                "page" to "$page"
            )
        )

        return execute(context) {
            feedPostRepository.fetchPosts(cursor, page)
        }
    }

    override suspend fun fetchPost(postID: String): FeedPost {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "postID" to postID
            )
        )

        return execute(context) {
            feedPostRepository.fetchPost(postID)
        }
    }

    override suspend fun writePost(request: FeedCreatePost) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "content" to request.content,
                "hasImages" to if (request.images.isEmpty()) "false" else "true",
                "isAnonymous" to "${request.isAnonymous}"
            )
        )

        execute(context) {
            feedPostRepository.writePost(request)
        }
    }

    override suspend fun deletePost(postID: String) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "postID" to postID
            )
        )

        execute(context) {
            feedPostRepository.deletePost(postID)
        }
    }

    override suspend fun vote(postID: String, type: FeedVoteType) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "postID" to postID,
                "type" to "$type"
            )
        )

        execute(context) {
            feedPostRepository.vote(postID, type)
        }
    }

    override suspend fun deleteVote(postID: String) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "postID" to postID
            )
        )

        execute(context) {
            feedPostRepository.deleteVote(postID)
        }
    }

    override suspend fun reportPost(postID: String, reason: FeedReportType, detail: String) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "postID" to postID,
                "reason" to "$reason",
                "detail" to detail
            )
        )

        execute(context) {
            feedPostRepository.reportPost(postID, reason, detail)
        }
    }

    // MARK: - Private
    private suspend fun <T> execute(
        context: CrashContext,
        operation: suspend () -> T
    ): T {
        return try {
            operation()
        } catch (e: Exception) {
            when (e) {
                is NetworkError.ServerError -> {
                    if (e.code == 409) {
                        throw FeedPostUseCaseError.CannotDeletePostWithVoteOrComment(e.message)
                    }
                    crashlyticsService?.record(error = e as Throwable, context = context)
                    throw e
                }

                is NetworkError -> {
                    crashlyticsService?.record(error = e as Throwable, context = context)
                    throw e
                }

                else -> {
                    val mappedError = FeedPostUseCaseError.Unknown(e)
                    crashlyticsService?.record(error = mappedError as Throwable, context = context)
                    throw mappedError
                }
            }
        }
    }
}
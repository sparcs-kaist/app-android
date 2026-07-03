package org.sparcs.soap.app.domain.usecases.feed

import org.sparcs.soap.app.domain.enums.feed.FeedReportType
import org.sparcs.soap.app.domain.enums.feed.FeedVoteType
import org.sparcs.soap.app.domain.error.CrashContext
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.error.feed.FeedPostUseCaseError
import org.sparcs.soap.app.domain.models.feed.FeedCreatePost
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.domain.models.feed.FeedPostPage
import org.sparcs.soap.app.domain.repositories.feed.FeedPostRepositoryProtocol
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
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
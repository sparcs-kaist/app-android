package org.sparcs.soap.App.Domain.Usecases.Ara

import android.graphics.Bitmap
import org.sparcs.soap.App.Domain.Enums.Ara.AraContentReportType
import org.sparcs.soap.App.Domain.Enums.Ara.PostListType
import org.sparcs.soap.App.Domain.Enums.Ara.PostOrigin
import org.sparcs.soap.App.Domain.Error.Ara.AraBoardUseCaseError
import org.sparcs.soap.App.Domain.Error.CrashContext
import org.sparcs.soap.App.Domain.Error.NetworkError
import org.sparcs.soap.App.Domain.Models.Ara.AraAttachment
import org.sparcs.soap.App.Domain.Models.Ara.AraBoard
import org.sparcs.soap.App.Domain.Models.Ara.AraCreatePost
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Domain.Models.Ara.AraPostPage
import org.sparcs.soap.App.Domain.Repositories.Ara.AraBoardRepositoryProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import org.sparcs.soap.App.Networking.ResponseDTO.Ara.AraPortalNoticeDTO
import javax.inject.Inject

interface AraBoardUseCaseProtocol {
    suspend fun fetchBoards(): List<AraBoard>

    suspend fun fetchPosts(
        type: PostListType,
        page: Int,
        pageSize: Int,
        searchKeyword: String?
    ): AraPostPage

    suspend fun fetchPost(
        origin: PostOrigin?,
        postID: Int
    ): AraPost

    suspend fun fetchBookmarks(
        page: Int,
        pageSize: Int
    ): AraPostPage

    suspend fun uploadImage(image: Bitmap): AraAttachment
    suspend fun writePost(request: AraCreatePost)
    suspend fun upVotePost(postID: Int)
    suspend fun downVotePost(postID: Int)
    suspend fun cancelVote(postID: Int)
    suspend fun reportPost(
        postID: Int,
        type: AraContentReportType
    )
    suspend fun deletePost(postID: Int)
    suspend fun addBookmark(postID: Int): Int
    suspend fun removeBookmark(bookmarkID: Int)

    suspend fun fetchPortalNotices(
        boardId: Int? = null,
        page: Int = 1,
        pageSize: Int = 5
    ): List<AraPortalNoticeDTO>

    suspend fun fetchTrendingPortalNotices(): List<AraPortalNoticeDTO>
}

class AraBoardUseCase @Inject constructor(
    private val araBoardRepository: AraBoardRepositoryProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol?
) : AraBoardUseCaseProtocol {

    private val feature: String = "AraBoard"

    override suspend fun fetchBoards(): List<AraBoard> {
        val context = CrashContext(feature = feature, metadata = emptyMap())
        return execute(context) {
            araBoardRepository.fetchBoards()
        }
    }

    override suspend fun fetchPosts(
        type: PostListType,
        page: Int,
        pageSize: Int,
        searchKeyword: String?
    ): AraPostPage {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "type" to type.toString(),
                "page" to page.toString(),
                "pageSize" to pageSize.toString(),
                "searchKeyword" to (searchKeyword ?: "null")
            )
        )
        return execute(context) {
            araBoardRepository.fetchPosts(type, page, pageSize, searchKeyword)
        }
    }

    override suspend fun fetchPost(origin: PostOrigin?, postID: Int): AraPost {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "origin" to (origin?.toString() ?: "null"),
                "postID" to postID.toString()
            )
        )
        return execute(context) {
            araBoardRepository.fetchPost(origin, postID)
        }
    }

    override suspend fun fetchBookmarks(page: Int, pageSize: Int): AraPostPage {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "page" to page.toString(),
                "pageSize" to pageSize.toString()
            )
        )
        return execute(context) {
            araBoardRepository.fetchBookmarks(page, pageSize)
        }
    }

    override suspend fun uploadImage(image: Bitmap): AraAttachment {
        val context = CrashContext(feature = feature, metadata = emptyMap())
        return execute(context) {
            araBoardRepository.uploadImage(image)
        }
    }

    override suspend fun writePost(request: AraCreatePost) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "title" to request.title,
                "hasContent" to request.content.isNotEmpty().toString()
            )
        )
        execute(context) {
            araBoardRepository.writePost(request)
        }
    }

    override suspend fun upVotePost(postID: Int) {
        val context = CrashContext(feature = feature, metadata = mapOf("postID" to postID.toString()))
        execute(context) { araBoardRepository.upVotePost(postID) }
    }

    override suspend fun downVotePost(postID: Int) {
        val context = CrashContext(feature = feature, metadata = mapOf("postID" to postID.toString()))
        execute(context) { araBoardRepository.downVotePost(postID) }
    }

    override suspend fun cancelVote(postID: Int) {
        val context = CrashContext(feature = feature, metadata = mapOf("postID" to postID.toString()))
        execute(context) { araBoardRepository.cancelVote(postID) }
    }

    override suspend fun reportPost(postID: Int, type: AraContentReportType) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "postID" to postID.toString(),
                "type" to type.toString()
            )
        )
        execute(context) { araBoardRepository.reportPost(postID, type) }
    }

    override suspend fun deletePost(postID: Int) {
        val context = CrashContext(feature = feature, metadata = mapOf("postID" to postID.toString()))
        execute(context) { araBoardRepository.deletePost(postID) }
    }

    override suspend fun addBookmark(postID: Int): Int {
        val context = CrashContext(feature = feature, metadata = mapOf("postID" to postID.toString()))
        return execute(context) {
            araBoardRepository.addBookmark(postID)
        }
    }

    override suspend fun removeBookmark(bookmarkID: Int) {
        val context = CrashContext(feature = feature, metadata = mapOf("bookmarkID" to bookmarkID.toString()))
        execute(context) { araBoardRepository.removeBookmark(bookmarkID) }
    }

    override suspend fun fetchPortalNotices(
        boardId: Int?,
        page: Int,
        pageSize: Int
    ): List<AraPortalNoticeDTO> {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "boardId" to (boardId?.toString() ?: "null"),
                "page" to page.toString(),
                "pageSize" to pageSize.toString()
            )
        )
        return execute(context) {
            araBoardRepository.fetchPortalNotices(boardId, page, pageSize)
        }
    }

    override suspend fun fetchTrendingPortalNotices(): List<AraPortalNoticeDTO> {
        val context = CrashContext(feature = feature, metadata = emptyMap())
        return execute(context) {
            araBoardRepository.fetchTrendingPortalNotices()
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
            val mappedError = AraBoardUseCaseError.Unknown(e)
            crashlyticsService?.record(mappedError as Throwable, context)
            throw mappedError
        }
    }
}
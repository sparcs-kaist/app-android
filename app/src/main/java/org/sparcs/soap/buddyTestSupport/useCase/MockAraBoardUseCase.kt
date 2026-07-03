package org.sparcs.soap.buddyTestSupport.useCase

import android.graphics.Bitmap
import org.sparcs.soap.app.domain.enums.ara.AraContentReportType
import org.sparcs.soap.app.domain.enums.ara.PostListType
import org.sparcs.soap.app.domain.enums.ara.PostOrigin
import org.sparcs.soap.app.domain.models.ara.AraAttachment
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraCreatePost
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostPage
import org.sparcs.soap.app.domain.usecases.ara.AraBoardUseCaseProtocol
import org.sparcs.soap.app.shared.mocks.ara.mock

class MockAraBoardUseCase : AraBoardUseCaseProtocol {

    var fetchPostResult: Result<AraPost> = Result.success(AraPost.mock())
    var deletePostResult: Result<Unit> = Result.success(Unit)
    var reportPostResult: Result<Unit> = Result.success(Unit)
    var upvotePostResult: Result<Unit> = Result.success(Unit)
    var downvotePostResult: Result<Unit> = Result.success(Unit)
    var cancelVoteResult: Result<Unit> = Result.success(Unit)
    var addBookmarkResult: Result<Unit> = Result.success(Unit)
    var removeBookmarkResult: Result<Unit> = Result.success(Unit)

    var fetchPostCallCount: Int = 0
    var lastFetchPostID: Int? = null
    var deletePostCallCount: Int = 0
    var reportPostCallCount: Int = 0

    override suspend fun fetchBoards(): List<AraBoard> = emptyList()

    override suspend fun fetchPosts(
        type: PostListType,
        page: Int,
        pageSize: Int,
        searchKeyword: String?
    ): AraPostPage = AraPostPage(pages = 0, items = 0, currentPage = 0, results = emptyList())

    override suspend fun fetchPost(origin: PostOrigin?, postID: Int): AraPost {
        fetchPostCallCount += 1
        lastFetchPostID = postID
        return fetchPostResult.getOrThrow()
    }

    override suspend fun fetchBookmarks(page: Int, pageSize: Int): AraPostPage =
        AraPostPage(pages = 0, items = 0, currentPage = 0, results = emptyList())

    override suspend fun uploadImage(image: Bitmap): AraAttachment {
        throw IllegalStateException("TestError.notConfigured")
    }

    override suspend fun writePost(request: AraCreatePost) {
        throw IllegalStateException("TestError.notConfigured")
    }

    override suspend fun upVotePost(postID: Int) {
        upvotePostResult.getOrThrow()
    }

    override suspend fun downVotePost(postID: Int) {
        downvotePostResult.getOrThrow()
    }

    override suspend fun cancelVote(postID: Int) {
        cancelVoteResult.getOrThrow()
    }

    override suspend fun reportPost(postID: Int, type: AraContentReportType) {
        reportPostCallCount += 1
        reportPostResult.getOrThrow()
    }

    override suspend fun deletePost(postID: Int) {
        deletePostCallCount += 1
        deletePostResult.getOrThrow()
    }

    override suspend fun addBookmark(postID: Int): Int {
        addBookmarkResult.getOrThrow()
        return 0
    }

    override suspend fun removeBookmark(bookmarkID: Int) {
        removeBookmarkResult.getOrThrow()
    }
}
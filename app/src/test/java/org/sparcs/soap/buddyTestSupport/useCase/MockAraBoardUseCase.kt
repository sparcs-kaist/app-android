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
    var fetchBoardsResult: Result<List<AraBoard>> = Result.success(emptyList())
    var fetchPostsResult: Result<AraPostPage> =
        Result.success(AraPostPage(pages = 0, items = 0, currentPage = 0, results = emptyList()))
    var fetchBookmarksResult: Result<AraPostPage> =
        Result.success(AraPostPage(pages = 0, items = 0, currentPage = 0, results = emptyList()))
    var deletePostResult: Result<Unit> = Result.success(Unit)
    var reportPostResult: Result<Unit> = Result.success(Unit)
    var upvotePostResult: Result<Unit> = Result.success(Unit)
    var downvotePostResult: Result<Unit> = Result.success(Unit)
    var cancelVoteResult: Result<Unit> = Result.success(Unit)
    var addBookmarkResult: Result<Unit> = Result.success(Unit)
    var removeBookmarkResult: Result<Unit> = Result.success(Unit)

    var fetchPostCallCount: Int = 0
    var lastFetchPostID: Int? = null
    var fetchBoardsCallCount: Int = 0
    var fetchPostsCallCount: Int = 0
    var deletePostCallCount: Int = 0
    var reportPostCallCount: Int = 0

    override suspend fun fetchBoards(): List<AraBoard> {
        fetchBoardsCallCount += 1
        return fetchBoardsResult.getOrThrow()
    }

    override suspend fun fetchPosts(
        type: PostListType,
        page: Int,
        pageSize: Int,
        searchKeyword: String?
    ): AraPostPage {
        fetchPostsCallCount += 1
        return fetchPostsResult.getOrThrow()
    }

    override suspend fun fetchPost(origin: PostOrigin?, postID: Int): AraPost {
        fetchPostCallCount += 1
        lastFetchPostID = postID
        return fetchPostResult.getOrThrow()
    }

    override suspend fun fetchBookmarks(page: Int, pageSize: Int): AraPostPage =
        fetchBookmarksResult.getOrThrow()

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
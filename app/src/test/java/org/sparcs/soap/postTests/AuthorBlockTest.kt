package org.sparcs.soap.postTests

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.models.ara.AraBlockedUser
import org.sparcs.soap.app.domain.models.ara.AraHiddenReason
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.isHiddenForBlockedAuthor
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationResultState
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationResult
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationUseCaseProtocol
import org.sparcs.soap.app.features.post.PostViewModel
import org.sparcs.soap.app.features.settings.ara.BlockedUsersViewModel
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.viewModels.TextProcessingDelegate
import org.sparcs.soap.buddyTestSupport.RecordingAnalyticsService
import org.sparcs.soap.buddyTestSupport.repository.MockAraUserRepository
import org.sparcs.soap.buddyTestSupport.useCase.MockAraBoardUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockAraCommentUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockReviewSummaryUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class AuthorBlockTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val board = MockAraBoardUseCase()
    private val users = MockAraUserRepository()
    private val analytics = RecordingAnalyticsService()

    private val translation = object : PostTranslationUseCaseProtocol {
        override fun availableLanguages(): List<String> = emptyList()
        override fun suggestedLanguages(): List<String> = emptyList()
        override fun deviceLanguage(): String = "en"
        override suspend fun translate(text: String, targetLanguage: String, isHtml: Boolean, allowDownload: Boolean) =
            PostTranslationResult.Unsupported
    }

    private val summarization = object : SummarizationUseCaseProtocol {
        override suspend fun isAvailable(): Boolean = false
        override suspend fun summarise(text: String, isHtml: Boolean) = SummarizationResultState.Unavailable
    }

    private fun author(id: String, isBlocked: Boolean?) = AraPost.mock().author.copy(id = id, isBlocked = isBlocked)

    private fun postViewModel() = PostViewModel(
        savedStateHandle = SavedStateHandle(mapOf("postId" to AraPost.mock().id)),
        araBoardUseCase = board,
        araCommentUseCase = MockAraCommentUseCase(),
        analyticsService = analytics,
        textProcessingDelegate = TextProcessingDelegate(translation, summarization, MockReviewSummaryUseCase()),
        araUserRepository = users,
    ).apply { backgroundDispatcher = Dispatchers.Unconfined }

    @Test
    fun aNicknameAuthorCanBeBlockedByTheirAccountId() {
        assertEquals(4821, author("4821", isBlocked = false).blockableUserId)
        assertEquals(4821, author("4821", isBlocked = true).blockableUserId)
        assertEquals(984, author("984.0", isBlocked = false).blockableUserId)
    }

    @Test
    fun anonymousAndRealNameAuthorsCannotBeBlocked() {
        assertNull(author("3f9a0c1e8b7d", isBlocked = null).blockableUserId)
        assertNull(author("90210", isBlocked = null).blockableUserId)
        assertNull(author("3f9a0c1e8b7d", isBlocked = false).blockableUserId)
    }

    @Test
    fun contentHiddenForABlockedAuthorSaysSo() {
        assertTrue(AraPost.mock().copy(isHidden = true, hiddenReason = listOf(AraHiddenReason.BLOCKED_USER_CONTENT)).isHiddenForBlockedAuthor)
        assertFalse(AraPost.mock().copy(isHidden = true, hiddenReason = listOf("DELETED_CONTENT")).isHiddenForBlockedAuthor)
    }

    @Test
    fun blockingAnAuthorBlocksTheirAccountAndReloadsThePost() {
        val viewModel = postViewModel()
        viewModel.setAuthorBlocked(author("984", isBlocked = false), blocked = true)
        assertEquals(listOf(984), users.blockedUserIds)
        assertEquals(1, board.fetchPostCallCount)
        assertFalse(viewModel.isAlertPresented)
    }

    @Test
    fun anEleventhBlockInADayExplainsTheLimit() {
        users.blockResult = Result.failure(NetworkError.ServerError(403))
        val viewModel = postViewModel()
        viewModel.setAuthorBlocked(author("984", isBlocked = false), blocked = true)
        assertTrue(viewModel.isAlertPresented)
        assertEquals(R.string.block_daily_limit, viewModel.alertState?.messageResId)
        assertNull(viewModel.alertState?.message)
        assertEquals(0, board.fetchPostCallCount)
    }

    @Test
    fun otherBlockFailuresKeepTheServersReason() {
        users.blockResult = Result.failure(NetworkError.ServerError(500, "서버 오류"))
        val viewModel = postViewModel()
        viewModel.setAuthorBlocked(author("984", isBlocked = false), blocked = true)
        assertTrue(viewModel.isAlertPresented)
        assertEquals(R.string.error_block_user, viewModel.alertState?.messageResId)
        assertEquals("서버 오류", viewModel.alertState?.message)
    }

    @Test
    fun unblockingAnAuthorUnblocksTheirAccountAndReloadsThePost() {
        val viewModel = postViewModel()
        viewModel.setAuthorBlocked(author("984", isBlocked = true), blocked = false)
        assertEquals(listOf(984), users.unblockedUserIds)
        assertEquals(1, board.fetchPostCallCount)
        assertEquals(listOf("author_block_toggled"), analytics.names)
    }

    @Test
    fun aFailedUnblockFromThePostSaysSo() {
        users.blockResult = Result.failure(NetworkError.ServerError(403))
        val viewModel = postViewModel()
        viewModel.setAuthorBlocked(author("984", isBlocked = true), blocked = false)
        assertEquals(R.string.error_unblock_user, viewModel.alertState?.messageResId)
        assertEquals(0, board.fetchPostCallCount)
    }

    @Test
    fun unblockableAuthorsAreIgnored() {
        val viewModel = postViewModel()
        viewModel.setAuthorBlocked(author("3f9a0c1e8b7d", isBlocked = null), blocked = true)
        assertTrue(users.blockedUserIds.isEmpty())
        assertFalse(viewModel.isAlertPresented)
    }

    @Test
    fun unblockingFromTheListTakesThePersonOffIt() {
        val first = AraBlockedUser(1, "a", null)
        val second = AraBlockedUser(2, "b", null)
        users.fetchBlockedUsersResult = Result.success(listOf(first, second))
        val viewModel = BlockedUsersViewModel(users)
        viewModel.load()
        viewModel.unblock(first)
        assertEquals(listOf(1), users.unblockedUserIds)
        assertEquals(BlockedUsersViewModel.ViewState.Loaded(listOf(second)), viewModel.state)
    }

    @Test
    fun aFailedUnblockPutsThePersonBack() {
        val first = AraBlockedUser(1, "a", null)
        users.fetchBlockedUsersResult = Result.success(listOf(first))
        val viewModel = BlockedUsersViewModel(users)
        viewModel.load()
        users.blockResult = Result.failure(IllegalStateException("boom"))
        viewModel.unblock(first)
        assertEquals(BlockedUsersViewModel.ViewState.Loaded(listOf(first)), viewModel.state)
        assertTrue(viewModel.isAlertPresented)
    }
}

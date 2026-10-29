package org.sparcs.soap.postTests

import android.net.Uri
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.sparcs.soap.app.domain.enums.ara.AraPostNicknameType
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapParser
import org.sparcs.soap.app.domain.helpers.tiptap.plainText
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraMarketListing
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostAttachment
import org.sparcs.soap.app.features.postCompose.ComposeBlock
import org.sparcs.soap.app.features.postCompose.PostComposeViewModel
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockAraBoardUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule
import java.net.URL
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class PostComposeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase = MockAraBoardUseCase()

    private fun board(slug: String = "talk", nicknameTypes: List<AraPostNicknameType> = emptyList()) =
        AraBoard.mock().copy(slug = slug, nicknameTypes = nicknameTypes)

    private fun viewModel(board: AraBoard = board(), post: AraPost? = null): PostComposeViewModel {
        val handle = SavedStateHandle(
            buildMap {
                put("board_json", Uri.encode(Gson().toJson(board)))
                post?.let { put("post_json", Uri.encode(Gson().toJson(it))) }
            }
        )
        return PostComposeViewModel(useCase, handle, MockAnalyticsService(), RuntimeEnvironment.getApplication())
    }

    private fun PostComposeViewModel.write(title: String, body: String) {
        this.title = title
        val block = editor.blocks.first() as ComposeBlock.Text
        editor.updateText(block.id, body, TextRange(body.length))
    }

    private fun attachment(id: Int, name: String, mimeType: String) = AraPostAttachment(
        id = id,
        createdAt = Date(0),
        file = URL("https://sparcs-newara.s3.amazonaws.com/files/$name"),
        filename = name,
        size = 1,
        mimeType = mimeType,
    )

    @Test
    fun startsAnonymousOnlyWhereTheBoardAllowsIt() {
        val talk = viewModel(board(nicknameTypes = listOf(AraPostNicknameType.REGULAR, AraPostNicknameType.ANONYMOUS)))
        assertTrue(talk.writeAsAnonymous)
        assertTrue(talk.canChooseAnonymity)

        val market = viewModel(board(AraBoard.MARKET_SLUG, listOf(AraPostNicknameType.REGULAR)))
        assertFalse(market.writeAsAnonymous)
        assertFalse(market.canChooseAnonymity)
    }

    @Test
    fun writesUnderANameTheBoardAllows() = runTest {
        val cases = listOf(
            Triple(listOf(AraPostNicknameType.REGULAR, AraPostNicknameType.ANONYMOUS), true, AraPostNicknameType.ANONYMOUS),
            Triple(listOf(AraPostNicknameType.REGULAR, AraPostNicknameType.ANONYMOUS), false, AraPostNicknameType.REGULAR),
            Triple(listOf(AraPostNicknameType.REALNAME), true, AraPostNicknameType.REALNAME),
            Triple(emptyList<AraPostNicknameType>(), false, AraPostNicknameType.REGULAR),
        )
        cases.forEach { (allowed, anonymous, expected) ->
            val viewModel = viewModel(board(nicknameTypes = allowed))
            viewModel.writeAsAnonymous = anonymous
            viewModel.write("제목", "본문")
            assertTrue(viewModel.submit())
            assertEquals(expected, useCase.writtenPosts.last().nicknameType)
        }
    }

    @Test
    fun clampsThePriceOnMarket() {
        val viewModel = viewModel(board(AraBoard.MARKET_SLUG))
        viewModel.price = -5
        assertEquals(0L, viewModel.price)
        viewModel.price = Long.MAX_VALUE
        assertEquals(AraMarketListing.MAX_PRICE, viewModel.price)
    }

    @Test
    fun postsInMarketWithoutAPrice() = runTest {
        val viewModel = viewModel(board(AraBoard.MARKET_SLUG))
        viewModel.write("의자", "상태 좋아요")
        assertTrue(viewModel.canSubmit)
        assertTrue(viewModel.submit())
        assertEquals(AraMarketListing(null), useCase.writtenPosts.last().marketListing)
    }

    @Test
    fun sendsAListingOnlyOnMarket() = runTest {
        val market = viewModel(board(AraBoard.MARKET_SLUG))
        market.write("의자", "팝니다")
        market.price = 12_000
        market.submit()
        assertEquals(AraMarketListing(12_000), useCase.writtenPosts.last().marketListing)

        val talk = viewModel()
        talk.write("잡담", "안녕")
        talk.price = 12_000
        talk.submit()
        assertNull(useCase.writtenPosts.last().marketListing)
    }

    @Test
    fun writesTheBodyAsTiptapJson() = runTest {
        val viewModel = viewModel()
        viewModel.write("제목", "첫 줄")
        viewModel.submit()
        val content = useCase.writtenPosts.single().content
        assertTrue(TiptapParser.isTiptapJson(content))
        assertEquals("첫 줄", TiptapParser().parse(content).plainText)
    }

    @Test
    fun editingLoadsThePostAndListsFilesAndPhotosNotInTheText() {
        val post = AraPost.mock().copy(
            title = "원래 제목",
            content = """{"type":"doc","content":[{"type":"paragraph","content":[{"type":"text","text":"본문"}]},{"type":"paragraph","content":[{"type":"image","attrs":{"src":"https://sparcs-newara.s3.amazonaws.com/files/inline.jpg"}}]}]}""",
            attachments = listOf(
                attachment(10, "inline.jpg", "image/jpeg"),
                attachment(11, "room.jpg", "image/jpeg"),
                attachment(12, "contract.pdf", "application/pdf"),
            ),
        )
        val viewModel = viewModel(post = post)
        assertEquals("원래 제목", viewModel.title)
        assertEquals("본문", (viewModel.editor.blocks.first() as ComposeBlock.Text).text)
        assertTrue(viewModel.editor.blocks.any { it is ComposeBlock.Image })
        assertEquals(listOf("room.jpg", "contract.pdf"), viewModel.files.map { it.name })
    }

    @Test
    fun editingKeepsListedFilesAndPhotosStillInTheText() = runTest {
        val post = AraPost.mock().copy(
            content = """{"type":"doc","content":[{"type":"paragraph","content":[{"type":"image","attrs":{"src":"https://sparcs-newara.s3.amazonaws.com/files/inline.jpg"}}]}]}""",
            attachments = listOf(
                attachment(10, "inline.jpg", "image/jpeg"),
                attachment(12, "contract.pdf", "application/pdf"),
                attachment(13, "old.pdf", "application/pdf"),
            ),
        )
        val viewModel = viewModel(post = post)
        viewModel.removeFile(viewModel.files.first { it.name == "old.pdf" })
        viewModel.title = "제목"
        assertTrue(viewModel.submit())
        val (postId, request) = useCase.updatedPosts.single()
        assertEquals(post.id, postId)
        assertEquals(listOf(12, 10), request.attachmentIds)
        assertTrue(useCase.writtenPosts.isEmpty())
    }

    @Test
    fun failedSubmissionShowsAnAlert() = runTest {
        useCase.writePostResult = Result.failure(IllegalStateException("boom"))
        val viewModel = viewModel()
        viewModel.write("제목", "본문")
        assertFalse(viewModel.submit())
        assertTrue(viewModel.isAlertPresented)
    }

    @Test
    fun followsIOSInAllowingAnyNonEmptyTitle() {
        val viewModel = viewModel()
        viewModel.write(" ", "본문")
        assertTrue(viewModel.canSubmit)
        viewModel.title = ""
        assertFalse(viewModel.canSubmit)
    }

    @Test
    fun reportsChangesOnlyAfterAnEdit() {
        val viewModel = viewModel()
        assertFalse(viewModel.hasChanges)
        viewModel.isNSFW = true
        assertTrue(viewModel.hasChanges)
        viewModel.isNSFW = false
        assertFalse(viewModel.hasChanges)
        viewModel.write("", "본문")
        assertTrue(viewModel.hasChanges)
    }

    @Test
    fun anUntouchedEditHasNoChanges() {
        val post = AraPost.mock().copy(
            title = "제목",
            content = """{"type":"doc","content":[{"type":"heading","attrs":{"level":3},"content":[{"type":"text","text":"소제목"}]}]}""",
        )
        val viewModel = viewModel(post = post)
        assertFalse(viewModel.hasChanges)
        viewModel.title = "새 제목"
        assertTrue(viewModel.hasChanges)
    }
}

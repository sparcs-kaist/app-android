package org.sparcs.soap.postTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostAttachment
import org.sparcs.soap.app.features.post.PostBody
import org.sparcs.soap.app.features.post.attachedImageUrls
import org.sparcs.soap.app.features.post.contentPlainText
import org.sparcs.soap.app.features.post.components.localFileName
import org.sparcs.soap.app.shared.mocks.ara.mock
import java.net.URL
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class PostBodyTest {

    private fun attachment(id: Int, fileName: String, mimeType: String) = AraPostAttachment(
        id = id,
        createdAt = Date(0),
        file = URL("https://sparcs-newara.s3.amazonaws.com/files/$fileName"),
        filename = fileName,
        size = 1,
        mimeType = mimeType,
    )

    private fun post(content: String, attachments: List<AraPostAttachment> = emptyList()) =
        AraPost.mock().copy(content = content, attachments = attachments)

    @Test
    fun rendersTiptapJsonNatively() {
        val body = PostBody.from("""{"type":"doc","content":[{"type":"paragraph","content":[{"type":"text","text":"안녕"}]}]}""", "talk")
        assertTrue(body is PostBody.Tiptap)
        assertEquals("안녕", (body as PostBody.Tiptap).plainText)
    }

    @Test
    fun fallsBackToHtmlForHtmlAndMalformedJson() {
        val html = PostBody.from("<p>Hello</p>", "talk")
        assertEquals(PostBody.Html("<p>Hello</p>", Constants.ARA_SHARE_URL), html)
        assertTrue(PostBody.from("{\"type\":\"doc\",", "talk") is PostBody.Html)
    }

    @Test
    fun resolvesPortalNoticeAgainstThePortal() {
        val body = PostBody.from("<img src=\"/files/a.png\">", "portal-notice") as PostBody.Html
        assertEquals("https://portal.kaist.ac.kr/", body.baseUrl)
    }

    @Test
    fun plainTextComesFromTiptapOrHtml() {
        assertEquals("a\nb", post("""{"type":"doc","content":[{"type":"paragraph","content":[{"type":"text","text":"a"}]},{"type":"paragraph","content":[{"type":"text","text":"b"}]}]}""").contentPlainText)
        assertEquals("Hello world", post("<p>Hello <b>world</b></p>").contentPlainText)
    }

    @Test
    fun listsPhotosAttachedButNotPlacedInTheText() {
        val post = post(
            content = """<p>Room</p><img src="https://sparcs-newara.s3.amazonaws.com/files/inline.jpg">""",
            attachments = listOf(
                attachment(1, "inline.jpg", "image/jpeg"),
                attachment(2, "kitchen.jpg", "image/jpeg"),
                attachment(3, "contract.pdf", "application/pdf"),
            )
        )
        assertEquals(listOf("kitchen.jpg"), post.attachedImageUrls.map { it.substringAfterLast('/') })
    }

    @Test
    fun savesAttachmentsUnderASingleSafeName() {
        val cases = listOf(
            "report.pdf" to "report.pdf",
            "%EB%B3%B4%EA%B3%A0%EC%84%9C.pdf" to "보고서.pdf",
            "..%2F..%2F..%2FDocuments" to ".._.._.._Documents",
            "../../Library/Preferences" to ".._.._Library_Preferences",
            ".." to "attachment",
            "%2F" to "_",
            "   " to "attachment",
            "a+b.pdf" to "a+b.pdf",
        )
        cases.forEach { (name, expected) ->
            assertEquals(name, expected, attachment(1, name, "application/pdf").localFileName)
        }
    }

    @Test
    fun matchesPercentEncodedFileNames() {
        val post = post(
            content = """{"type":"doc","content":[{"type":"image","attrs":{"src":"https://sparcs-newara.s3.amazonaws.com/files/%EB%B0%A9.jpg"}}]}""",
            attachments = listOf(attachment(1, "%EB%B0%A9.jpg", "image/jpeg"))
        )
        assertTrue(post.attachedImageUrls.isEmpty())
    }

    @Test
    fun matchesPhotosInPostsWrittenByTheApp() {
        val escapedSrc = "https://sparcs-newara.s3.amazonaws.com/files/desk.jpg"
            .replace("/", "\\u002F").replace(".", "\\u002E")
        val post = post(
            content = """{"type":"doc","content":[{"type":"image","attrs":{"src":"$escapedSrc"}}]}""",
            attachments = listOf(
                attachment(1, "desk.jpg", "image/jpeg"),
                attachment(2, "chair.jpg", "image/jpeg"),
            )
        )
        assertEquals(listOf("chair.jpg"), post.attachedImageUrls.map { it.substringAfterLast('/') })
    }
}

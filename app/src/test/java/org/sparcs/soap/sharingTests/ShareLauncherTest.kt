package org.sparcs.soap.sharingTests

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.app.domain.helpers.InstagramShareHelper
import org.sparcs.soap.app.shared.sharing.ShareContent
import org.sparcs.soap.app.shared.sharing.ShareLauncher
import org.sparcs.soap.app.shared.sharing.ShareRequest
import org.sparcs.soap.app.shared.sharing.ShareTarget

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32], application = Application::class)
class ShareLauncherTest {
    private val content = ShareContent("Timetable", "Semester", "#112233", "#445566")
    private val uri = Uri.parse("content://org.sparcs.soap.fileprovider/shared_images/test.png")
    private val context get() = RuntimeEnvironment.getApplication()

    @Test fun systemSharingIncludesImageAndReadPermission() {
        val recorder = RecordingContext(context)
        assertTrue(ShareLauncher(recorder).launch(ShareRequest(ShareTarget.More, content, uri), null))
        val chooser = recorder.intents.last()
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        val send = IntentCompat.getParcelableExtra(chooser, Intent.EXTRA_INTENT, Intent::class.java)!!
        assertEquals("image/png", send.type)
        assertEquals(uri, IntentCompat.getParcelableExtra(send, Intent.EXTRA_STREAM, Uri::class.java))
        assertEquals(uri, send.clipData!!.getItemAt(0).uri)
        assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test fun instagramStoryIncludesStickerThemeAndOptionalLink() {
        val intent = InstagramShareHelper.storyIntent(uri, content.topColor, content.bottomColor, "https://example.com/share")
        assertEquals("com.instagram.share.ADD_TO_STORY", intent.action)
        assertEquals("com.instagram.android", intent.`package`)
        assertEquals("#112233", intent.getStringExtra("top_background_color"))
        assertEquals("#445566", intent.getStringExtra("bottom_background_color"))
        assertEquals("https://example.com/share", intent.getStringExtra("content_url"))
        assertEquals(uri, intent.clipData!!.getItemAt(0).uri)
    }

    @Test fun absentInstagramUsesSystemSharing() {
        val recorder = RecordingContext(context)
        assertTrue(ShareLauncher(recorder).launch(ShareRequest(ShareTarget.Instagram, content, uri), null))
        assertEquals(Intent.ACTION_CHOOSER, recorder.intents.last().action)
    }

    @Test fun themeCodeCopiesWithoutImage() {
        val request = ShareRequest(ShareTarget.Copy, content.copy(copyText = "ABC123"), null)
        assertTrue(ShareLauncher(context).launch(request, null))
        assertEquals("ABC123", context.getSystemService(ClipboardManager::class.java).primaryClip!!.getItemAt(0).text)
    }

    @Test fun feedReceivesImageAndTextWithoutPublishing() {
        var received: Pair<Uri, String>? = null
        assertTrue(ShareLauncher(context).launch(ShareRequest(ShareTarget.Feed, content, uri)) { image, text -> received = image to text })
        assertEquals(uri to content.text, received)
    }

    private class RecordingContext(context: Context) : ContextWrapper(context) {
        val intents = mutableListOf<Intent>()
        override fun startActivity(intent: Intent) {
            intents += intent
            if (intent.`package` != null) throw ActivityNotFoundException()
        }
    }
}

package org.sparcs.soap.sharingTests

import android.app.Application
import android.graphics.Bitmap
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.app.shared.sharing.ShareContent
import org.sparcs.soap.app.shared.sharing.ShareImageStore
import org.sparcs.soap.app.shared.sharing.ShareTarget
import org.sparcs.soap.app.shared.sharing.ShareViewModel
import org.sparcs.soap.testSupport.MainDispatcherRule

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ShareViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    private val store get() = ShareImageStore(RuntimeEnvironment.getApplication())
    private val content = ShareContent("Timetable", "", "#FFFFFF", "#FFFFFF")

    @Test fun copyCodeDoesNotRequireCapture() {
        val model = ShareViewModel(store)
        model.prepare(ShareTarget.Copy, content.copy(copyText = "ABC123")) { error("Must not render") }
        assertNotNull(model.state.value.request)
        assertNull(model.state.value.request?.imageUri)
    }

    @Test fun failedCaptureCanBeRetried() {
        val model = ShareViewModel(store)
        model.prepare(ShareTarget.More, content) { error("No frame") }
        assertTrue(model.state.value.failed)
        model.prepare(ShareTarget.Copy, content.copy(copyText = "ABC123")) { error("Must not render") }
        assertFalse(model.state.value.failed)
        assertNotNull(model.state.value.request)
    }

    @Test fun duplicateTapAndDismissCannotLaunchALateShare() {
        val model = ShareViewModel(store)
        val capture = CompletableDeferred<Bitmap>()
        var calls = 0
        model.prepare(ShareTarget.More, content) { calls++; capture.await() }
        model.prepare(ShareTarget.Messages, content) { calls++; capture.await() }
        assertEquals(1, calls)
        model.reset()
        capture.complete(Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888))
        assertFalse(model.state.value.preparing)
        assertNull(model.state.value.request)
    }

}

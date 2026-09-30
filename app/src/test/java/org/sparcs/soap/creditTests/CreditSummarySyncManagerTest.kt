package org.sparcs.soap.creditTests

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.app.domain.helpers.CreditSummarySnapshotStore
import org.sparcs.soap.app.domain.helpers.TokenStorageProtocol
import org.sparcs.soap.app.domain.models.otl.CreditSummarySnapshot
import org.sparcs.soap.wearable.WearableDataManager
import org.sparcs.soap.widgets.buddyCreditsWidget.CreditSummarySyncManager
import java.lang.reflect.Proxy

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class CreditSummarySyncManagerTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private var accessToken: String? = "signed-in"
    private val tokens = Proxy.newProxyInstance(
        TokenStorageProtocol::class.java.classLoader, arrayOf(TokenStorageProtocol::class.java),
    ) { _, method, _ ->
        check(method.name == "getAccessToken")
        accessToken
    } as TokenStorageProtocol

    @Before fun setup() {
        // These tests exercise publishing, not Google Play Services or execution of queued work.
        if (runCatching { WorkManager.getInstance(context) }.isFailure) {
            WorkManager.initialize(context, Configuration.Builder()
                .setExecutor { }.setTaskExecutor { }.build())
        }
    }

    @Test fun `clearing invalidates earlier work even after the same account signs in again`() = runTest {
        val store = CreditSummarySnapshotStore(context)
        val manager = CreditSummarySyncManager(context, store, tokens, WearableDataManager(context))
        val oldRevision = manager.revision
        val old = CreditSummarySnapshot(4.0, 100, 138)
        manager.publish(old, oldRevision)
        assertEquals(old, store.snapshot)
        accessToken = null
        manager.clear()
        assertNull(store.snapshot)
        accessToken = "new-session"
        manager.publish(old, oldRevision)
        assertNull(store.snapshot)
        val fresh = CreditSummarySnapshot(3.0, 20, 138)
        manager.publish(fresh, manager.revision)
        assertEquals(fresh, store.snapshot)
    }

    @Test fun `missing token blocks publishing even before clear gets the lock`() = runTest {
        val store = CreditSummarySnapshotStore(context)
        val manager = CreditSummarySyncManager(context, store, tokens, WearableDataManager(context))
        store.clear()
        accessToken = null
        manager.publish(CreditSummarySnapshot(4.0, 100, 138), manager.revision)
        assertNull(store.snapshot)
    }
}

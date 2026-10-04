package org.sparcs.soap.nearbyTests

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.models.nearby.NearbyPeerState
import org.sparcs.soap.app.domain.models.otl.OTLUser
import org.sparcs.soap.app.domain.nearby.BeaconSighting
import org.sparcs.soap.app.domain.nearby.NearbyBeaconSourceProtocol
import org.sparcs.soap.app.domain.nearby.NearbyBluetoothAvailability
import org.sparcs.soap.app.domain.nearby.NearbyCrypto
import org.sparcs.soap.app.domain.nearby.NearbyJson
import org.sparcs.soap.app.domain.nearby.NearbyMessageBody
import org.sparcs.soap.app.domain.nearby.NearbyMessageType
import org.sparcs.soap.app.domain.nearby.NearbySession
import org.sparcs.soap.app.domain.nearby.PresenceCard
import org.sparcs.soap.app.domain.usecases.nearby.NearbyFriendsManager
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.repository.FakeNearbyRelayRepository
import org.sparcs.soap.buddyTestSupport.useCase.MockFriendUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockUserUseCase

class FakeBeaconSource : NearbyBeaconSourceProtocol {
    val heard = MutableSharedFlow<BeaconSighting>(extraBufferCapacity = 64)
    /** The token being advertised right now; `null` while the beacon is off. */
    var advertisedToken: ByteArray? = null
        private set
    /** Every token advertised, in order. */
    val advertisedHistory = mutableListOf<String>()
    override val requiredPermissions: Array<String> = emptyArray()
    override val canAdvertise: Boolean = true
    override fun availability(): Flow<NearbyBluetoothAvailability> = flowOf(NearbyBluetoothAvailability.Available)
    override fun sightings(token: ByteArray): Flow<BeaconSighting> = heard
        .onStart {
            advertisedToken = token
            advertisedHistory += NearbyCrypto.hex(token)
        }
        .onCompletion { if (advertisedToken.contentEquals(token)) advertisedToken = null }
}

/** One simulated phone on the Add Friends screen. */
class Phone(
    name: String,
    code: String,
    relay: FakeNearbyRelayRepository,
    scope: TestScope,
    deviceId: String = java.util.UUID.randomUUID().toString(),
) {
    val beacon = FakeBeaconSource()
    val friends = MockFriendUseCase().apply { fetchMyCodeResult = Result.success(code) }
    val users = MockUserUseCase().apply { otlUser = OTLUser.mock().copy(name = name) }
    /**
     * The current session; [reenter] swaps in a new one, as reopening the
     * screen does, and so does the manager when it rotates an expired one.
     */
    var session = NearbySession.generate()
        private set
    /** Handed out by the manager's next `newSession()` call. */
    private var pending: NearbySession? = session
    val manager = NearbyFriendsManager(
        relay = relay,
        beacon = beacon,
        friendUseCase = friends,
        userUseCase = users,
        crashlyticsService = MockCrashlyticsService(),
        clock = { scope.currentTime + EPOCH },
        newSession = {
            (pending ?: NearbySession.generate()).also {
                pending = null
                session = it
            }
        },
        newMessageId = { "$name-${++messageCount}" },
        deviceId = deviceId
    )

    /** Leaves Add Friends and opens it again in the same app launch. */
    fun TestScope.reenter() {
        job?.cancel()
        runCurrent()
        job = launch { manager.run() }
        runCurrent()
    }
    private var messageCount = 0
    var job: Job? = null

    fun state(of: Phone) = manager.peers.value.firstOrNull { it.id == of.session.tokenHex }?.state
    fun hear(other: Phone, rssi: Int = -50) {
        beacon.heard.tryEmit(BeaconSighting(other.session.token, rssi))
    }

    companion object {
        const val EPOCH = 1_790_000_000_000L
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class NearbyFriendsManagerTest {

    /** Two phones with sessions running; [configure] runs before either starts. */
    private fun TestScope.phones(
        configure: (alice: Phone, bob: Phone) -> Unit = { _, _ -> },
    ): Triple<Phone, Phone, FakeNearbyRelayRepository> {
        val relay = FakeNearbyRelayRepository { currentTime + Phone.EPOCH }
        val alice = Phone("Alice", "ALI456", relay, this)
        val bob = Phone("Bob", "BOB123", relay, this)
        configure(alice, bob)
        alice.job = launch { alice.manager.run() }
        bob.job = launch { bob.manager.run() }
        runCurrent()
        return Triple(alice, bob, relay)
    }

    /** Both keep hearing each other for [millis] of virtual time. */
    private fun TestScope.nearEachOther(alice: Phone, bob: Phone, millis: Long = 2_000) {
        var elapsed = 0L
        while (elapsed < millis) {
            alice.hear(bob)
            bob.hear(alice)
            advanceTimeBy(500)
            runCurrent()
            elapsed += 500
        }
    }

    private fun TestScope.stop(vararg phones: Phone) {
        phones.forEach { it.job?.cancel() }
        runCurrent()
    }

    @Test
    fun `peers resolve to verified names and are evicted after going quiet`() = runTest {
        val (alice, bob, relay) = phones()
        assertTrue(relay.hasPresence(alice.session.lookupId))

        nearEachOther(alice, bob)
        assertEquals("Bob", alice.manager.peers.value.single().name)
        assertEquals(NearbyPeerState.Idle, alice.state(bob))

        advanceTimeBy(11_000)
        runCurrent()
        assertTrue(alice.manager.peers.value.isEmpty())
        stop(alice, bob)
    }

    @Test
    fun `a phone that re-enters replaces its old bubble`() = runTest {
        val (alice, bob, relay) = phones()
        val carol = Phone("Carol", "CAR789", relay, this)
        carol.job = launch { carol.manager.run() }
        runCurrent()
        repeat(3) {
            alice.hear(bob)
            alice.hear(carol)
            advanceTimeBy(500)
            runCurrent()
        }
        val firstBob = bob.session.tokenHex
        assertEquals(listOf(firstBob, carol.session.tokenHex), alice.manager.peers.value.map { it.id })

        with(bob) { reenter() }
        assertTrue(firstBob != bob.session.tokenHex)
        repeat(3) {
            alice.hear(bob)
            alice.hear(carol)
            advanceTimeBy(500)
            runCurrent()
        }
        // Bob's new bubble takes the old one's slot, ahead of Carol.
        assertEquals(listOf(bob.session.tokenHex, carol.session.tokenHex), alice.manager.peers.value.map { it.id })
        stop(alice, bob, carol)
    }

    @Test
    fun `different phones with the same name stay separate`() = runTest {
        val (alice, bob, relay) = phones()
        val otherBob = Phone("Bob", "BOB999", relay, this)
        otherBob.job = launch { otherBob.manager.run() }
        runCurrent()
        repeat(3) {
            alice.hear(bob)
            alice.hear(otherBob)
            advanceTimeBy(500)
            runCurrent()
        }
        assertEquals(listOf("Bob", "Bob"), alice.manager.peers.value.map { it.name })
        stop(alice, bob, otherBob)
    }

    @Test
    fun `an added peer stays added when it re-enters`() = runTest {
        val (alice, bob, _) = phones()
        nearEachOther(alice, bob)
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 3_000)
        bob.manager.accept(alice.session.tokenHex)
        nearEachOther(alice, bob, 5_000)
        assertEquals(NearbyPeerState.Added, alice.state(bob))

        with(bob) { reenter() }
        nearEachOther(alice, bob)
        assertEquals(listOf(NearbyPeerState.Added), alice.manager.peers.value.map { it.state })
        assertEquals(bob.session.tokenHex, alice.manager.peers.value.single().id)
        stop(alice, bob)
    }

    @Test
    fun `weak signals are not shown`() = runTest {
        val (alice, bob, _) = phones()
        repeat(4) {
            alice.hear(bob, rssi = -95)
            advanceTimeBy(500)
            runCurrent()
        }
        assertTrue(alice.manager.peers.value.isEmpty())
        stop(alice, bob)
    }

    @Test
    fun `request, accept and confirm add the friend on both phones`() = runTest {
        val (alice, bob, _) = phones()
        nearEachOther(alice, bob)

        alice.manager.request(bob.session.tokenHex)
        runCurrent()
        assertEquals(NearbyPeerState.Requested, alice.state(bob))
        nearEachOther(alice, bob, 1_000)
        assertEquals(NearbyPeerState.Incoming, bob.state(alice))

        bob.manager.accept(alice.session.tokenHex)
        nearEachOther(alice, bob)

        assertEquals("BOB123", alice.friends.lastAddedCode)
        assertEquals("ALI456", bob.friends.lastAddedCode)
        assertEquals(NearbyPeerState.Added, alice.state(bob))
        assertEquals(NearbyPeerState.Added, bob.state(alice))
        stop(alice, bob)
    }

    @Test
    fun `simultaneous requests become one exchange`() = runTest {
        val (alice, bob, _) = phones()
        nearEachOther(alice, bob)

        alice.manager.request(bob.session.tokenHex)
        bob.manager.request(alice.session.tokenHex)
        nearEachOther(alice, bob, 3_000)

        assertEquals(NearbyPeerState.Added, alice.state(bob))
        assertEquals(NearbyPeerState.Added, bob.state(alice))
        assertEquals("BOB123", alice.friends.lastAddedCode)
        assertEquals("ALI456", bob.friends.lastAddedCode)
        stop(alice, bob)
    }

    @Test
    fun `declining shows declined to the requester, who can ask again`() = runTest {
        val (alice, bob, _) = phones()
        nearEachOther(alice, bob)
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)

        bob.manager.decline(alice.session.tokenHex)
        nearEachOther(alice, bob, 1_000)

        assertEquals(NearbyPeerState.Declined, alice.state(bob))
        assertEquals(NearbyPeerState.Idle, bob.state(alice))
        assertNull(alice.friends.lastAddedCode)

        // Tapping again sends a fresh request, which Bob sees as a new one.
        alice.manager.request(bob.session.tokenHex)
        runCurrent()
        assertEquals(NearbyPeerState.Requested, alice.state(bob))
        nearEachOther(alice, bob, 1_000)
        assertEquals(NearbyPeerState.Incoming, bob.state(alice))
        stop(alice, bob)
    }

    @Test
    fun `a declined peer still leaves once out of range`() = runTest {
        val (alice, bob, _) = phones()
        nearEachOther(alice, bob)
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        bob.manager.decline(alice.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        assertEquals(NearbyPeerState.Declined, alice.state(bob))

        advanceTimeBy(11_000)
        runCurrent()
        assertTrue(alice.manager.peers.value.isEmpty())
        stop(alice, bob)
    }

    @Test
    fun `the declined requester can still be asked by them`() = runTest {
        val (alice, bob, _) = phones()
        nearEachOther(alice, bob)
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        bob.manager.decline(alice.session.tokenHex)
        nearEachOther(alice, bob, 1_000)

        bob.manager.request(alice.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        assertEquals(NearbyPeerState.Incoming, alice.state(bob))
        stop(alice, bob)
    }

    @Test
    fun `an unanswered request times out and is withdrawn`() = runTest {
        val (alice, bob, _) = phones()
        nearEachOther(alice, bob)
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        assertEquals(NearbyPeerState.Incoming, bob.state(alice))

        nearEachOther(alice, bob, NearbyFriendsManager.REQUEST_TIMEOUT_MS)

        assertEquals(NearbyPeerState.Idle, alice.state(bob))
        assertEquals(NearbyPeerState.Idle, bob.state(alice))
        stop(alice, bob)
    }

    @Test
    fun `cancelling a request withdraws it on the other phone`() = runTest {
        val (alice, bob, _) = phones()
        nearEachOther(alice, bob)
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)

        alice.manager.cancel(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)

        assertEquals(NearbyPeerState.Idle, bob.state(alice))
        stop(alice, bob)
    }

    @Test
    fun `duplicate and stale messages are ignored`() = runTest {
        val (alice, bob, relay) = phones()
        nearEachOther(alice, bob)

        fun request(msgId: String, sentAt: Long) {
            val pairKey = NearbyCrypto.pairKey(
                alice.session.keyPair.private, bob.session.pubX963, alice.session.token, bob.session.token
            )
            val body = NearbyMessageBody(type = NearbyMessageType.Request, msgId = msgId, sentAt = sentAt)
            relay.inject(
                bob.session.lookupId,
                NearbyCrypto.seal(bob.session.presenceKey, alice.session.token, NearbyCrypto.HEADER_AAD + bob.session.lookupId),
                NearbyCrypto.seal(
                    pairKey,
                    NearbyJson.json.encodeToString(NearbyMessageBody.serializer(), body).toByteArray(),
                    NearbyCrypto.BODY_AAD + bob.session.lookupId + alice.session.token
                )
            )
        }

        request("stale", sentAt = currentTime + Phone.EPOCH - NearbyFriendsManager.MAX_CLOCK_SKEW_MS - 1)
        nearEachOther(alice, bob, 1_000)
        assertEquals(NearbyPeerState.Idle, bob.state(alice))

        request("dup", sentAt = currentTime + Phone.EPOCH)
        nearEachOther(alice, bob, 1_000)
        assertEquals(NearbyPeerState.Incoming, bob.state(alice))
        bob.manager.decline(alice.session.tokenHex)
        nearEachOther(alice, bob, 1_000)

        // Replayed with the same msgId: must not come back as a new request.
        request("dup", sentAt = currentTime + Phone.EPOCH)
        nearEachOther(alice, bob, 1_000)
        assertEquals(NearbyPeerState.Idle, bob.state(alice))
        stop(alice, bob)
    }

    @Test
    fun `a presence that doesn't match the beacon is never shown`() = runTest {
        val relay = FakeNearbyRelayRepository { currentTime + Phone.EPOCH }
        val alice = Phone("Alice", "ALI456", relay, this)
        alice.job = launch { alice.manager.run() }
        runCurrent()
        // A relay swapping in its own key under Bob's mailbox: correctly
        // encrypted for Bob's token, but the key doesn't hash to that token.
        val bob = NearbySession.generate()
        val mallory = NearbySession.generate()
        val card = PresenceCard(pub = NearbyCrypto.base64Url(mallory.pubX963), name = "Mallory")
        relay.putPresence(
            bob.lookupId,
            NearbyCrypto.seal(
                bob.presenceKey,
                NearbyJson.json.encodeToString(PresenceCard.serializer(), card).toByteArray(),
                NearbyCrypto.PRESENCE_INFO + bob.lookupId
            ),
            bob.ownerSecret
        )

        repeat(6) {
            alice.beacon.heard.tryEmit(BeaconSighting(bob.token, -50))
            advanceTimeBy(500)
            runCurrent()
        }
        assertTrue(alice.manager.peers.value.isEmpty())
        stop(alice)
    }

    /** Has Bob hear [others] (in order) for [millis] at the given RSSI each. */
    private fun TestScope.bobHears(bob: Phone, others: List<Pair<Phone, Int>>, millis: Long) {
        var elapsed = 0L
        while (elapsed < millis) {
            others.forEach { (other, rssi) -> bob.hear(other, rssi) }
            advanceTimeBy(500)
            runCurrent()
            elapsed += 500
        }
    }

    @Test
    fun `an unanswered incoming request lapses after 60 seconds`() = runTest {
        val (alice, bob, _) = phones()
        nearEachOther(alice, bob)
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        assertEquals(NearbyPeerState.Incoming, bob.state(alice))

        nearEachOther(alice, bob, NearbyFriendsManager.INCOMING_TTL_MS - 3_000)
        assertEquals(NearbyPeerState.Incoming, bob.state(alice))
        nearEachOther(alice, bob, 4_000)
        assertEquals(NearbyPeerState.Idle, bob.state(alice))

        // Too late to accept: nothing is sent and nothing is added.
        bob.manager.accept(alice.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        assertNull(bob.friends.lastAddedCode)
        stop(alice, bob)
    }

    @Test
    fun `an incoming request lapses early when the requester walks away`() = runTest {
        val (alice, bob, _) = phones()
        nearEachOther(alice, bob)
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        assertEquals(NearbyPeerState.Incoming, bob.state(alice))

        // Bob stops hearing Alice; well within the 60 s.
        advanceTimeBy(NearbyFriendsManager.PEER_TTL_MS + 2_000)
        runCurrent()
        assertNull(bob.state(alice))
        stop(alice, bob)
    }

    @Test
    fun `accepting without our code retries the fetch once then fails`() = runTest {
        val (alice, bob, _) = phones { _, bob ->
            bob.friends.fetchMyCodeResult = Result.failure(NetworkError.ServerError(404))
        }
        nearEachOther(alice, bob)
        assertEquals(1, bob.friends.fetchMyCodeCallCount)

        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        bob.manager.accept(alice.session.tokenHex)
        nearEachOther(alice, bob, 1_000)

        assertEquals(2, bob.friends.fetchMyCodeCallCount)
        assertEquals(NearbyPeerState.Failed, bob.state(alice))
        assertNull(bob.friends.lastAddedCode)
        assertEquals(NearbyPeerState.Requested, alice.state(bob))
        stop(alice, bob)
    }

    @Test
    fun `confirming without our code retries the fetch once then fails, and a tap finishes it`() = runTest {
        val (alice, bob, _) = phones { alice, _ ->
            alice.friends.fetchMyCodeResult = Result.failure(NetworkError.ServerError(404))
        }
        nearEachOther(alice, bob)
        assertEquals(1, alice.friends.fetchMyCodeCallCount)
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        bob.manager.accept(alice.session.tokenHex)
        nearEachOther(alice, bob, 1_000)

        assertEquals(2, alice.friends.fetchMyCodeCallCount)
        assertEquals(NearbyPeerState.Failed, alice.state(bob))
        assertNull(alice.friends.lastAddedCode)
        assertEquals(NearbyPeerState.Adding, bob.state(alice))

        // The code comes back; tapping the failed bubble confirms and adds.
        alice.friends.fetchMyCodeResult = Result.success("ALI456")
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 2_000)
        assertEquals("BOB123", alice.friends.lastAddedCode)
        assertEquals("ALI456", bob.friends.lastAddedCode)
        assertEquals(NearbyPeerState.Added, alice.state(bob))
        assertEquals(NearbyPeerState.Added, bob.state(alice))
        stop(alice, bob)
    }

    @Test
    fun `an accept whose confirm never arrives fails and a tap starts over`() = runTest {
        val relay = FakeNearbyRelayRepository { currentTime + Phone.EPOCH }
        val bob = Phone("Bob", "BOB123", relay, this)
        bob.job = launch { bob.manager.run() }
        runCurrent()
        // A requester that publishes and asks, then never answers again.
        val ghost = NearbySession.generate()
        val card = PresenceCard(pub = NearbyCrypto.base64Url(ghost.pubX963), name = "Ghost")
        relay.putPresence(
            ghost.lookupId,
            NearbyCrypto.seal(
                ghost.presenceKey,
                NearbyJson.json.encodeToString(PresenceCard.serializer(), card).toByteArray(),
                NearbyCrypto.PRESENCE_INFO + ghost.lookupId
            ),
            ghost.ownerSecret
        )
        fun ghostSends(type: NearbyMessageType, msgId: String) {
            val pairKey = NearbyCrypto.pairKey(ghost.keyPair.private, bob.session.pubX963, ghost.token, bob.session.token)
            val body = NearbyMessageBody(type = type, msgId = msgId, sentAt = currentTime + Phone.EPOCH)
            relay.inject(
                bob.session.lookupId,
                NearbyCrypto.seal(bob.session.presenceKey, ghost.token, NearbyCrypto.HEADER_AAD + bob.session.lookupId),
                NearbyCrypto.seal(
                    pairKey,
                    NearbyJson.json.encodeToString(NearbyMessageBody.serializer(), body).toByteArray(),
                    NearbyCrypto.BODY_AAD + bob.session.lookupId + ghost.token
                )
            )
        }
        fun hearGhost(millis: Long) {
            var elapsed = 0L
            while (elapsed < millis) {
                bob.beacon.heard.tryEmit(BeaconSighting(ghost.token, -50))
                advanceTimeBy(500)
                runCurrent()
                elapsed += 500
            }
        }
        val ghostId = ghost.tokenHex
        fun state() = bob.manager.peers.value.firstOrNull { it.id == ghostId }?.state

        hearGhost(2_000)
        ghostSends(NearbyMessageType.Request, "g1")
        hearGhost(1_000)
        assertEquals(NearbyPeerState.Incoming, state())

        bob.manager.accept(ghostId)
        hearGhost(NearbyFriendsManager.CONFIRM_TIMEOUT_MS - 2_000)
        assertEquals(NearbyPeerState.Adding, state())
        hearGhost(3_000)
        assertEquals(NearbyPeerState.Failed, state())
        assertNull(bob.friends.lastAddedCode)

        // No code from them, so the tap is a fresh request.
        bob.manager.request(ghostId)
        runCurrent()
        assertEquals(NearbyPeerState.Requested, state())
        stop(bob)
    }

    @Test
    fun `peers keep their first-seen order as signal strength changes`() = runTest {
        val relay = FakeNearbyRelayRepository { currentTime + Phone.EPOCH }
        val bob = Phone("Bob", "BOB123", relay, this)
        val alice = Phone("Alice", "ALI456", relay, this)
        val carol = Phone("Carol", "CAR789", relay, this)
        val dave = Phone("Dave", "DAV012", relay, this)
        val all = listOf(bob, alice, carol, dave)
        all.forEach { phone -> phone.job = launch { phone.manager.run() } }
        runCurrent()

        bobHears(bob, listOf(alice to -75), 1_500)
        bobHears(bob, listOf(alice to -75, carol to -70), 1_500)
        bobHears(bob, listOf(alice to -75, carol to -70, dave to -60), 1_500)
        fun names() = bob.manager.peers.value.map { it.name }
        assertEquals(listOf("Alice", "Carol", "Dave"), names())

        // Dave walks right up and Alice drifts out; order never changes.
        bobHears(bob, listOf(alice to -79, carol to -78, dave to -40), 3_000)
        assertEquals(listOf("Alice", "Carol", "Dave"), names())

        // Below −80 dBm smoothed hides a peer; coming back keeps its slot.
        bobHears(bob, listOf(alice to -95, carol to -70, dave to -40), 4_000)
        assertEquals(listOf("Carol", "Dave"), names())
        bobHears(bob, listOf(alice to -50, carol to -70, dave to -40), 4_000)
        assertEquals(listOf("Alice", "Carol", "Dave"), names())

        all.forEach { it.job?.cancel() }
        runCurrent()
    }

    @Test
    fun `already friends is a success`() = runTest {
        val (alice, bob, _) = phones()
        // OTL returns 200 for an existing friendship, so addFriend just succeeds.
        nearEachOther(alice, bob)
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        bob.manager.accept(alice.session.tokenHex)
        nearEachOther(alice, bob)
        assertEquals(NearbyPeerState.Added, alice.state(bob))
        stop(alice, bob)
    }

    @Test
    fun `an OTL failure is retryable with the stored code`() = runTest {
        val (alice, bob, _) = phones()
        alice.friends.addFriendResult = Result.failure(NetworkError.ServerError(500))
        nearEachOther(alice, bob)
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        bob.manager.accept(alice.session.tokenHex)
        nearEachOther(alice, bob)
        assertEquals(NearbyPeerState.Failed, alice.state(bob))
        // Bob still gets Alice's code and adds her.
        assertEquals(NearbyPeerState.Added, bob.state(alice))

        alice.friends.addFriendResult = Result.success(Unit)
        alice.friends.lastAddedCode = null
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        assertEquals("BOB123", alice.friends.lastAddedCode)
        assertEquals(NearbyPeerState.Added, alice.state(bob))
        stop(alice, bob)
    }

    @Test
    fun `stop withdraws pending requests and deletes the presence`() = runTest {
        val (alice, bob, relay) = phones()
        nearEachOther(alice, bob)
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        assertEquals(NearbyPeerState.Incoming, bob.state(alice))
        val postsBeforeStop = relay.postCount

        stop(alice)
        assertEquals(postsBeforeStop + 1, relay.postCount)
        assertFalse(relay.hasPresence(alice.session.lookupId))
        assertTrue(alice.manager.peers.value.isEmpty())

        // Bob already has Alice's card cached, so he can still read the cancel.
        bob.hear(alice)
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(NearbyPeerState.Idle, bob.state(alice))
        stop(bob)
    }

    @Test
    fun `the beacon only advertises once the presence is published`() = runTest {
        val relay = FakeNearbyRelayRepository { currentTime + Phone.EPOCH }.apply { failPuts = true }
        val alice = Phone("Alice", "ALI456", relay, this)
        alice.job = launch { alice.manager.run() }
        advanceTimeBy(5_000)
        runCurrent()
        assertNull(alice.beacon.advertisedToken)
        assertTrue(alice.beacon.advertisedHistory.isEmpty())

        relay.failPuts = false
        advanceTimeBy(20_000)
        runCurrent()
        assertTrue(relay.hasPresence(alice.session.lookupId))
        assertEquals(alice.session.tokenHex, alice.beacon.advertisedToken?.let(NearbyCrypto::hex))
        stop(alice)
    }

    @Test
    fun `an unrenewed presence rotates to a new session`() = runTest {
        val (alice, bob, relay) = phones()
        nearEachOther(alice, bob)
        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 3_000)
        bob.manager.accept(alice.session.tokenHex)
        nearEachOther(alice, bob, 5_000)
        assertEquals(NearbyPeerState.Added, alice.state(bob))
        val oldSession = alice.session
        val oldLookup = NearbyCrypto.hex(oldSession.lookupId)

        // Alice's renewals stop getting through, as if her process was frozen.
        val frozen = oldSession.ownerSecret.copyOf()
        relay.failPutsFor = { it.contentEquals(frozen) }
        advanceTimeBy(300_000L - 30_000L)
        runCurrent()
        // The stale token is off the air, its presence was deleted, and the
        // new session published before advertising.
        assertTrue(oldLookup in relay.deleted)
        val newSession = alice.session
        assertTrue(oldSession !== newSession)
        assertTrue(relay.hasPresence(newSession.lookupId))
        assertEquals(newSession.tokenHex, alice.beacon.advertisedToken?.let(NearbyCrypto::hex))
        assertEquals(listOf(oldSession.tokenHex, newSession.tokenHex), alice.beacon.advertisedHistory)

        // Bob still sees one Alice (same device ID), and she's still Added.
        nearEachOther(alice, bob)
        assertEquals(listOf(newSession.tokenHex), bob.manager.peers.value.map { it.id })
        assertEquals(NearbyPeerState.Added, alice.state(bob))
        stop(alice, bob)
    }

    @Test
    fun `rotation drops in-flight requests and cancels ours`() = runTest {
        val (alice, bob, relay) = phones()
        nearEachOther(alice, bob)
        val frozen = alice.session.ownerSecret.copyOf()
        val oldAlice = alice.session.tokenHex
        relay.failPutsFor = { it.contentEquals(frozen) }
        // Most of the way to expiry, with both still hearing each other.
        nearEachOther(alice, bob, 250_000L)

        alice.manager.request(bob.session.tokenHex)
        nearEachOther(alice, bob, 1_000)
        assertEquals(NearbyPeerState.Requested, alice.state(bob))
        assertEquals(NearbyPeerState.Incoming, bob.manager.peers.value.single { it.id == oldAlice }.state)

        // Crossing expiry − 30 s rotates Alice before her request times out.
        nearEachOther(alice, bob, 20_000L)
        assertTrue(oldAlice != alice.session.tokenHex)
        assertEquals(NearbyPeerState.Idle, alice.state(bob))
        // Bob got the cancel, and now sees one Alice with nothing pending.
        assertEquals(listOf(NearbyPeerState.Idle), bob.manager.peers.value.map { it.state })
        stop(alice, bob)
    }

    @Test
    fun `advertising stays off until a rotated session publishes`() = runTest {
        val relay = FakeNearbyRelayRepository { currentTime + Phone.EPOCH }
        val alice = Phone("Alice", "ALI456", relay, this)
        alice.job = launch { alice.manager.run() }
        runCurrent()
        assertTrue(alice.beacon.advertisedToken != null)

        relay.failPuts = true
        advanceTimeBy(300_000L)
        runCurrent()
        assertNull(alice.beacon.advertisedToken)
        advanceTimeBy(120_000L)
        runCurrent()
        // Still unpublished, so still silent.
        assertNull(alice.beacon.advertisedToken)
        assertEquals(1, alice.beacon.advertisedHistory.size)

        relay.failPuts = false
        advanceTimeBy(40_000)
        runCurrent()
        assertTrue(relay.hasPresence(alice.session.lookupId))
        assertEquals(alice.session.tokenHex, alice.beacon.advertisedToken?.let(NearbyCrypto::hex))
        stop(alice)
    }

    @Test
    fun `publish failures retry with backoff, then mailbox starts`() = runTest {
        val relay = FakeNearbyRelayRepository { currentTime + Phone.EPOCH }.apply { failPuts = true }
        val alice = Phone("Alice", "ALI456", relay, this)
        val job = launch { alice.manager.run() }
        advanceTimeBy(5_000)
        runCurrent()
        assertFalse(relay.hasPresence(alice.session.lookupId))
        val attempts = relay.putCount
        assertTrue("retried with backoff, got $attempts", attempts in 2..5)

        relay.failPuts = false
        advanceTimeBy(20_000)
        runCurrent()
        assertTrue(relay.hasPresence(alice.session.lookupId))
        job.cancel()
        runCurrent()
    }

    @Test
    fun `presence name falls back when OTL is unavailable`() = runTest {
        val relay = FakeNearbyRelayRepository { currentTime + Phone.EPOCH }
        val alice = Phone("Alice", "ALI456", relay, this).apply { users.otlUser = null }
        val bob = Phone("Bob", "BOB123", relay, this)
        alice.job = launch { alice.manager.run() }
        bob.job = launch { bob.manager.run() }
        runCurrent()
        nearEachOther(alice, bob)
        assertEquals(NearbyFriendsManager.FALLBACK_NAME, bob.manager.peers.value.single().name)
        stop(alice, bob)
    }
}

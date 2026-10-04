package org.sparcs.soap.app.domain.usecases.nearby

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.helpers.FriendCode
import org.sparcs.soap.app.domain.models.nearby.NearbyPeer
import org.sparcs.soap.app.domain.models.nearby.NearbyPeerState
import org.sparcs.soap.app.domain.nearby.BeaconSighting
import org.sparcs.soap.app.domain.nearby.NearbyBeaconSourceProtocol
import org.sparcs.soap.app.domain.nearby.NearbyCrypto
import org.sparcs.soap.app.domain.nearby.NearbyJson
import org.sparcs.soap.app.domain.nearby.NearbyMessageBody
import org.sparcs.soap.app.domain.nearby.NearbyMessageType
import org.sparcs.soap.app.domain.nearby.NearbySession
import org.sparcs.soap.app.domain.nearby.PresenceCard
import org.sparcs.soap.app.domain.repositories.nearby.NearbyRelayRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.nearby.RelayMessage
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.FriendUseCaseProtocol
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

/** Discovery and the request/accept exchange for the nearby section of Add Friends. */
interface NearbyFriendsManagerProtocol {
    /** Verified people nearby, closest first. */
    val peers: StateFlow<List<NearbyPeer>>

    /** Emits a peer's ID each time OTL confirms a friendship with them. */
    val friendAdded: SharedFlow<String>

    /**
     * Runs one session — beacon, presence, mailbox — until the caller is
     * cancelled, then withdraws pending requests and deletes the presence.
     * Throws if Bluetooth scanning can't start.
     */
    suspend fun run()

    fun request(peerId: String)
    fun cancel(peerId: String)
    fun accept(peerId: String)
    fun decline(peerId: String)
}

/**
 * Implements the nearby protocol (plan §2) and the per-peer state machine
 * (§2.6), with the same rules as iOS `NearbyFriendUseCase`.
 *
 * Not thread-safe: [run] and the actions must be called from one thread (the
 * main thread via `viewModelScope`). Network calls suspend without leaving it.
 */
class NearbyFriendsManager(
    private val relay: NearbyRelayRepositoryProtocol,
    private val beacon: NearbyBeaconSourceProtocol,
    private val friendUseCase: FriendUseCaseProtocol,
    private val userUseCase: UserUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol?,
    private val clock: () -> Long,
    private val newSession: () -> NearbySession,
    private val newMessageId: () -> String,
    private val deviceId: String = LAUNCH_DEVICE_ID,
) : NearbyFriendsManagerProtocol {

    @Inject
    constructor(
        relay: NearbyRelayRepositoryProtocol,
        beacon: NearbyBeaconSourceProtocol,
        friendUseCase: FriendUseCaseProtocol,
        userUseCase: UserUseCaseProtocol,
        crashlyticsService: CrashlyticsServiceProtocol,
    ) : this(
        relay = relay,
        beacon = beacon,
        friendUseCase = friendUseCase,
        userUseCase = userUseCase,
        crashlyticsService = crashlyticsService,
        clock = System::currentTimeMillis,
        newSession = NearbySession::generate,
        newMessageId = { UUID.randomUUID().toString() }
    )

    companion object {
        const val RSSI_ALPHA = 0.3
        const val RSSI_THRESHOLD = -80.0
        const val PEER_TTL_MS = 10_000L
        const val TICK_MS = 1_000L
        const val RESOLVE_RETRY_MS = 3_000L
        const val PRESENCE_RENEW_MS = 60_000L
        const val REQUEST_TIMEOUT_MS = 60_000L
        const val CONFIRM_TIMEOUT_MS = 60_000L
        const val INCOMING_TTL_MS = 60_000L
        const val MAX_CLOCK_SKEW_MS = 10 * 60_000L
        const val POLL_WAIT_SECONDS = 20
        const val MAX_BACKOFF_MS = 30_000L
        const val BATCH_LIMIT = 32
        const val TEARDOWN_TIMEOUT_MS = 3_000L
        const val FALLBACK_NAME = "Buddy"

        /** Identifies this app launch on presence cards; see [PresenceCard.device]. */
        val LAUNCH_DEVICE_ID: String = NearbyCrypto.base64Url(
            ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
        )

        /** States that stay on screen even when the peer's beacon goes quiet. */
        private val ACTIVE_STATES = setOf(
            NearbyPeerState.Requested,
            NearbyPeerState.Incoming,
            NearbyPeerState.Adding
        )
    }

    private class ResolvedCard(val name: String, val pairKey: ByteArray, val device: String?)

    private class Peer(val token: ByteArray, var order: Long) {
        val id: String = NearbyCrypto.hex(token)
        val lookupId: ByteArray = NearbyCrypto.lookupId(token)
        val lookupIdHex: String = NearbyCrypto.hex(lookupId)
        val presenceKey: ByteArray = NearbyCrypto.presenceKey(token)

        var rssi: Double? = null
        var lastSeen = 0L
        var card: ResolvedCard? = null

        /** Failed decryption or the commitment check: never shown, never retried. */
        var rejected = false
        var nextResolveAt = 0L

        var state = NearbyPeerState.Idle
        var incomingRequestId: String? = null
        var incomingAt = 0L
        var confirmSent = false

        /** Their accept, which we owe a confirm with our code for. */
        var acceptMsgId: String? = null
        var isConfirming = false
        var theirCode: String? = null
        var isPosting = false
        var timeout: Job? = null
    }

    private val _peers = MutableStateFlow<List<NearbyPeer>>(emptyList())
    override val peers: StateFlow<List<NearbyPeer>> = _peers.asStateFlow()

    private val _friendAdded = MutableSharedFlow<String>(extraBufferCapacity = 8)
    override val friendAdded: SharedFlow<String> = _friendAdded.asSharedFlow()

    private var session: NearbySession? = null
    private var scope: CoroutineScope? = null
    private val peersById = LinkedHashMap<String, Peer>()
    private val seenMessageIds = HashSet<String>()
    private var nextOrder = 0L
    private var myCode: String? = null
    private var republish = Channel<Unit>(Channel.CONFLATED)

    // MARK: - Lifecycle

    override suspend fun run() {
        val session = newSession()
        try {
            coroutineScope {
                this@NearbyFriendsManager.session = session
                scope = this
                peersById.clear()
                seenMessageIds.clear()
                republish = Channel(Channel.CONFLATED)
                publish()

                val published = CompletableDeferred<Unit>()
                launch { myCode = fetchMyCode() }
                launch { presenceLoop(session, published) }
                launch {
                    published.await()
                    pollLoop(session)
                }
                launch { tickLoop() }
                beacon.sightings(session.token).collect(::onSighting)
            }
        } finally {
            scope = null
            withContext(NonCancellable) { tearDown(session) }
        }
    }

    private suspend fun tearDown(session: NearbySession) {
        peersById.values.forEach { it.timeout?.cancel() }
        val requested = peersById.values.filter { it.state == NearbyPeerState.Requested }
        withTimeoutOrNull(TEARDOWN_TIMEOUT_MS) {
            requested.forEach { send(session, it, NearbyMessageType.Cancel) }
            try {
                relay.deletePresence(session.lookupId, session.ownerSecret)
            } catch (e: Exception) {
                Timber.w(e, "Nearby: failed to delete presence")
            }
        }
        session.clear()
        if (this.session === session) this.session = null
        peersById.clear()
        publish()
    }

    // MARK: - Presence

    private suspend fun presenceLoop(session: NearbySession, published: CompletableDeferred<Unit>) {
        val card = PresenceCard(
            pub = NearbyCrypto.base64Url(session.pubX963),
            name = NearbyJson.truncatedName(displayName()),
            device = deviceId
        )
        val plaintext = NearbyJson.json.encodeToString(PresenceCard.serializer(), card).toByteArray()
        var backoff = TICK_MS
        while (true) {
            val ok = try {
                val blob = NearbyCrypto.seal(
                    session.presenceKey,
                    plaintext,
                    NearbyCrypto.PRESENCE_INFO + session.lookupId
                )
                relay.putPresence(session.lookupId, blob, session.ownerSecret)
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w(e, "Nearby: failed to publish presence")
                false
            }
            if (ok) {
                published.complete(Unit)
                backoff = TICK_MS
            }
            val wait = if (ok) PRESENCE_RENEW_MS else backoff.also { backoff = (backoff * 2).coerceAtMost(MAX_BACKOFF_MS) }
            withTimeoutOrNull(wait) { republish.receive() }
        }
    }

    /** The OTL name, as on the friends list, so people recognise who they're adding. */
    private suspend fun displayName(): String {
        userUseCase.otlUser?.name?.takeIf { it.isNotBlank() }?.let { return it.trim() }
        try {
            userUseCase.fetchOTLUser()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Nearby: OTL user unavailable for the presence name")
        }
        return userUseCase.otlUser?.name?.takeIf { it.isNotBlank() }?.trim()
            ?: userUseCase.feedUser?.nickname?.takeIf { it.isNotBlank() }?.trim()
            ?: FALLBACK_NAME
    }

    private suspend fun fetchMyCode(): String? = try {
        friendUseCase.fetchMyCode()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Timber.w(e, "Nearby: friend code unavailable")
        null
    }

    // MARK: - Discovery

    private fun onSighting(sighting: BeaconSighting) {
        val session = session ?: return
        if (sighting.token.size != NearbyCrypto.TOKEN_LENGTH || sighting.token.contentEquals(session.token)) return
        val peer = peer(sighting.token)
        if (peer.rejected) return
        peer.rssi = peer.rssi?.let { it + RSSI_ALPHA * (sighting.rssi - it) } ?: sighting.rssi.toDouble()
        peer.lastSeen = clock()
        publish()
    }

    private suspend fun tickLoop() {
        while (true) {
            delay(TICK_MS)
            resolvePending()
            expire()
            publish()
        }
    }

    private fun Peer.isInRange(now: Long): Boolean =
        now - lastSeen <= PEER_TTL_MS && (rssi ?: Double.NEGATIVE_INFINITY) >= RSSI_THRESHOLD

    private suspend fun resolvePending() {
        val now = clock()
        val pending = peersById.values
            .filter { it.card == null && !it.rejected && it.nextResolveAt <= now && it.isInRange(now) }
            .take(BATCH_LIMIT)
        if (pending.isEmpty()) return
        val blobs = try {
            relay.batchGet(pending.map { it.lookupId })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Nearby: batch get failed")
            pending.forEach { it.nextResolveAt = now + RESOLVE_RETRY_MS }
            return
        }
        pending.forEach { peer ->
            val blob = blobs[peer.lookupIdHex]
            if (blob == null) {
                // They may not have published yet.
                peer.nextResolveAt = now + RESOLVE_RETRY_MS
            } else {
                resolve(peer, blob)
            }
        }
    }

    /** Decrypts a presence and checks it commits to the token we heard. */
    private fun resolve(peer: Peer, blob: ByteArray) {
        val session = session ?: return
        try {
            val plaintext = NearbyCrypto.open(peer.presenceKey, blob, NearbyCrypto.PRESENCE_INFO + peer.lookupId)
            val card = NearbyJson.json.decodeFromString(PresenceCard.serializer(), String(plaintext))
            val pub = NearbyCrypto.fromBase64Url(card.pub)
            check(NearbyCrypto.token(pub).contentEquals(peer.token)) { "Presence key doesn't match the beacon" }
            val pairKey = NearbyCrypto.pairKey(session.keyPair.private, pub, session.token, peer.token)
            peer.card = ResolvedCard(
                name = NearbyJson.truncatedName(card.name.trim()).ifBlank { FALLBACK_NAME },
                pairKey = pairKey,
                device = card.device
            )
            card.device?.let { device ->
                peersById.values
                    .firstOrNull { it !== peer && it.card?.device == device }
                    ?.let { replace(it, with = peer) }
            }
        } catch (e: Exception) {
            // Another app on the same UUID prefix, or a relay substituting keys.
            Timber.w("Nearby: ignoring an unverifiable presence")
            peer.rejected = true
        }
    }

    /**
     * The same phone came back with a new session: its new token takes over the
     * old bubble's place. Only a finished add carries over; anything in flight
     * was tied to the old session, which the peer no longer has.
     */
    private fun replace(old: Peer, with: Peer) {
        with.order = old.order
        if (old.state == NearbyPeerState.Added && with.state == NearbyPeerState.Idle) {
            with.state = NearbyPeerState.Added
        }
        old.timeout?.cancel()
        peersById.remove(old.id)
    }

    private suspend fun ensureResolved(peer: Peer): Boolean {
        if (peer.card != null) return true
        val blob = try {
            relay.batchGet(listOf(peer.lookupId))[peer.lookupIdHex]
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        } ?: return false
        resolve(peer, blob)
        return peer.card != null
    }

    private fun expire() {
        val now = clock()
        peersById.values.removeAll { peer ->
            // An unanswered request lapses after a fixed time, or as soon as the
            // requester walks away. Someone we only know from the mailbox (we
            // never heard their beacon) is held for the fixed time alone.
            val requesterGone = peer.lastSeen > 0 && now - peer.lastSeen > PEER_TTL_MS
            if (peer.state == NearbyPeerState.Incoming && (now - peer.incomingAt > INCOMING_TTL_MS || requesterGone)) {
                peer.state = NearbyPeerState.Idle
            }
            val settled = peer.state == NearbyPeerState.Idle || peer.state == NearbyPeerState.Declined
            !peer.rejected && settled && now - peer.lastSeen > PEER_TTL_MS
        }
    }

    private fun peer(token: ByteArray): Peer =
        peersById.getOrPut(NearbyCrypto.hex(token)) { Peer(token.copyOf(), nextOrder++) }

    private fun publish() {
        val now = clock()
        _peers.value = peersById.values
            .filter { peer ->
                val card = peer.card ?: return@filter false
                card.name.isNotEmpty() && (peer.state in ACTIVE_STATES || peer.isInRange(now))
            }
            // First-seen order, so bubbles never move; RSSI only gates visibility.
            .sortedBy { it.order }
            .map { NearbyPeer(id = it.id, name = it.card!!.name, state = it.state) }
    }

    // MARK: - Mailbox

    private suspend fun pollLoop(session: NearbySession) {
        var cursor: String? = null
        var backoff = TICK_MS
        while (true) {
            try {
                val page = relay.pollMessages(session.lookupId, session.ownerSecret, cursor, POLL_WAIT_SECONDS)
                page.messages.forEach { handle(session, it) }
                cursor = page.cursor
                backoff = TICK_MS
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if ((e as? NetworkError.ServerError)?.code == 404) republish.trySend(Unit)
                Timber.w(e, "Nearby: mailbox poll failed")
                delay(backoff)
                backoff = (backoff * 2).coerceAtMost(MAX_BACKOFF_MS)
            }
        }
    }

    private suspend fun handle(session: NearbySession, message: RelayMessage) {
        val senderToken = try {
            NearbyCrypto.open(session.presenceKey, message.header, NearbyCrypto.HEADER_AAD + session.lookupId)
        } catch (e: Exception) {
            return
        }
        if (senderToken.size != NearbyCrypto.TOKEN_LENGTH || senderToken.contentEquals(session.token)) return
        val peer = peer(senderToken)
        if (peer.rejected || !ensureResolved(peer)) return
        val card = peer.card ?: return
        val body = try {
            val plaintext = NearbyCrypto.open(
                card.pairKey,
                message.body,
                NearbyCrypto.BODY_AAD + session.lookupId + senderToken
            )
            NearbyJson.json.decodeFromString(NearbyMessageBody.serializer(), String(plaintext))
        } catch (e: Exception) {
            return
        }
        if (!seenMessageIds.add(body.msgId)) return
        if (abs(clock() - body.sentAt) > MAX_CLOCK_SKEW_MS) return
        val type = body.type ?: return
        apply(peer, type, body)
        publish()
    }

    private fun apply(peer: Peer, type: NearbyMessageType, body: NearbyMessageBody) {
        when (type) {
            NearbyMessageType.Request -> when (peer.state) {
                NearbyPeerState.Idle, NearbyPeerState.Declined, NearbyPeerState.Failed, NearbyPeerState.Incoming -> {
                    peer.state = NearbyPeerState.Incoming
                    peer.incomingRequestId = body.msgId
                    peer.incomingAt = clock()
                }
                // Both tapped: their request is an implicit accept of ours.
                NearbyPeerState.Requested -> {
                    peer.incomingRequestId = body.msgId
                    sendAccept(peer)
                }
                // They lost track of us; answer again so they can finish too.
                NearbyPeerState.Added -> {
                    peer.incomingRequestId = body.msgId
                    sendAccept(peer, keepState = true)
                }
                NearbyPeerState.Adding -> Unit
            }

            NearbyMessageType.Accept -> {
                if (peer.state != NearbyPeerState.Requested && peer.state != NearbyPeerState.Adding) return
                val code = body.friendCode?.let(FriendCode::normalized) ?: return
                if (peer.isConfirming) return
                peer.theirCode = code
                peer.timeout?.cancel()
                if (peer.confirmSent) {
                    addFriend(peer)
                } else {
                    peer.acceptMsgId = body.msgId
                    confirmAndAdd(peer)
                }
            }

            NearbyMessageType.Confirm -> {
                val awaiting = peer.state == NearbyPeerState.Adding || peer.state == NearbyPeerState.Failed
                if (!awaiting || peer.theirCode != null) return
                peer.theirCode = body.friendCode?.let(FriendCode::normalized) ?: return
                addFriend(peer)
            }

            NearbyMessageType.Decline -> if (peer.state == NearbyPeerState.Requested) {
                peer.timeout?.cancel()
                peer.state = NearbyPeerState.Declined
            }

            NearbyMessageType.Cancel -> {
                val withdrawn = peer.state == NearbyPeerState.Incoming ||
                    (peer.state == NearbyPeerState.Adding && peer.theirCode == null)
                if (withdrawn) {
                    peer.timeout?.cancel()
                    peer.state = NearbyPeerState.Idle
                }
            }
        }
    }

    // MARK: - Actions

    override fun request(peerId: String) {
        val peer = peersById[peerId]?.takeIf { it.card != null } ?: return
        when (peer.state) {
            NearbyPeerState.Failed -> when {
                peer.theirCode == null -> startRequest(peer)
                peer.acceptMsgId != null && !peer.confirmSent -> confirmAndAdd(peer)
                else -> addFriend(peer)
            }
            NearbyPeerState.Idle, NearbyPeerState.Declined -> startRequest(peer)
            else -> Unit
        }
    }

    override fun cancel(peerId: String) {
        val peer = peersById[peerId] ?: return
        if (peer.state != NearbyPeerState.Requested) return
        peer.timeout?.cancel()
        peer.state = NearbyPeerState.Idle
        publish()
        launch { send(peer, NearbyMessageType.Cancel) }
    }

    override fun accept(peerId: String) {
        val peer = peersById[peerId] ?: return
        if (peer.state == NearbyPeerState.Incoming) sendAccept(peer)
    }

    override fun decline(peerId: String) {
        val peer = peersById[peerId] ?: return
        if (peer.state != NearbyPeerState.Incoming) return
        peer.state = NearbyPeerState.Idle
        publish()
        val inReplyTo = peer.incomingRequestId
        launch { send(peer, NearbyMessageType.Decline, inReplyTo = inReplyTo) }
    }

    private fun startRequest(peer: Peer) {
        if (scope == null) return
        peer.state = NearbyPeerState.Requested
        peer.theirCode = null
        peer.confirmSent = false
        peer.acceptMsgId = null
        publish()
        startTimeout(peer, REQUEST_TIMEOUT_MS) {
            if (peer.state == NearbyPeerState.Requested) {
                peer.state = NearbyPeerState.Idle
                publish()
                send(peer, NearbyMessageType.Cancel)
            }
        }
        launch {
            if (!send(peer, NearbyMessageType.Request) && peer.state == NearbyPeerState.Requested) {
                peer.timeout?.cancel()
                fail(peer)
            }
        }
    }

    /** Agrees to their request by sending our code, then waits for theirs. */
    private fun sendAccept(peer: Peer, keepState: Boolean = false) {
        if (scope == null) return
        if (!keepState) {
            peer.timeout?.cancel()
            peer.state = NearbyPeerState.Adding
            publish()
        }
        val inReplyTo = peer.incomingRequestId
        launch {
            val sent = sendWithMyCode(peer, NearbyMessageType.Accept, inReplyTo = inReplyTo)
            if (keepState) return@launch
            if (!sent) {
                if (peer.state == NearbyPeerState.Adding && peer.theirCode == null) fail(peer)
                return@launch
            }
            if (peer.theirCode == null) {
                startTimeout(peer, CONFIRM_TIMEOUT_MS) {
                    if (peer.state == NearbyPeerState.Adding && peer.theirCode == null) fail(peer)
                }
            }
        }
    }

    private suspend fun sendWithMyCode(peer: Peer, type: NearbyMessageType, inReplyTo: String?): Boolean {
        val code = myCodeWithRetry() ?: return false
        return send(peer, type, inReplyTo = inReplyTo, friendCode = code)
    }

    /** The code fetched when the session started, or one more attempt if that failed. */
    private suspend fun myCodeWithRetry(): String? = myCode ?: fetchMyCode()?.also { myCode = it }

    /**
     * Requester side after their accept: send our code, then add theirs. Without
     * our code we can't finish the exchange, so the peer fails; tapping retries.
     */
    private fun confirmAndAdd(peer: Peer) {
        if (scope == null) return
        peer.isConfirming = true
        peer.state = NearbyPeerState.Adding
        publish()
        launch {
            val code = try {
                myCodeWithRetry()
            } finally {
                peer.isConfirming = false
            }
            if (code == null) {
                fail(peer)
                return@launch
            }
            // If this send is lost, they time out and can start over; the
            // friendship itself still exists once either side has added.
            peer.confirmSent = send(peer, NearbyMessageType.Confirm, inReplyTo = peer.acceptMsgId, friendCode = code)
            addFriend(peer)
        }
    }

    private fun addFriend(peer: Peer) {
        val code = peer.theirCode ?: return
        if (peer.isPosting || peer.state == NearbyPeerState.Added) return
        peer.isPosting = true
        peer.timeout?.cancel()
        peer.state = NearbyPeerState.Adding
        publish()
        launch {
            val added = try {
                // OTL creates both directions, and an existing friendship is a success.
                friendUseCase.addFriend(code)
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                crashlyticsService?.recordException(e)
                false
            } finally {
                peer.isPosting = false
            }
            peer.state = if (added) NearbyPeerState.Added else NearbyPeerState.Failed
            publish()
            if (added) _friendAdded.tryEmit(peer.id)
        }
    }

    private fun fail(peer: Peer) {
        peer.state = NearbyPeerState.Failed
        publish()
    }

    private fun startTimeout(peer: Peer, millis: Long, onTimeout: suspend () -> Unit) {
        peer.timeout?.cancel()
        peer.timeout = scope?.launch {
            delay(millis)
            onTimeout()
        }
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        scope?.launch(block = block)
    }

    private suspend fun send(
        peer: Peer,
        type: NearbyMessageType,
        inReplyTo: String? = null,
        friendCode: String? = null,
    ): Boolean {
        val session = session ?: return false
        return send(session, peer, type, inReplyTo, friendCode)
    }

    private suspend fun send(
        session: NearbySession,
        peer: Peer,
        type: NearbyMessageType,
        inReplyTo: String? = null,
        friendCode: String? = null,
    ): Boolean {
        val card = peer.card ?: return false
        val body = NearbyMessageBody(
            type = type,
            msgId = newMessageId(),
            sentAt = clock(),
            inReplyTo = inReplyTo,
            friendCode = friendCode
        )
        return try {
            val header = NearbyCrypto.seal(peer.presenceKey, session.token, NearbyCrypto.HEADER_AAD + peer.lookupId)
            val sealedBody = NearbyCrypto.seal(
                card.pairKey,
                NearbyJson.json.encodeToString(NearbyMessageBody.serializer(), body).toByteArray(),
                NearbyCrypto.BODY_AAD + peer.lookupId + session.token
            )
            relay.postMessage(peer.lookupId, header, sealedBody)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Nearby: failed to send %s", type)
            false
        }
    }
}

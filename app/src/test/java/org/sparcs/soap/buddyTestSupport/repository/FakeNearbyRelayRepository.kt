package org.sparcs.soap.buddyTestSupport.repository

import kotlinx.coroutines.delay
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.nearby.NearbyCrypto
import org.sparcs.soap.app.domain.repositories.nearby.NearbyRelayRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.nearby.RelayMessage
import org.sparcs.soap.app.domain.repositories.nearby.RelayMessagesPage

/**
 * An in-memory relay with the server's semantics (owner secrets, mailboxes,
 * cursor acknowledgement), shared by every simulated phone in a test.
 */
class FakeNearbyRelayRepository(
    /** Virtual clock (epoch ms) for the presence expiry returned by PUT. */
    private val clock: () -> Long = { 0L },
) : NearbyRelayRepositoryProtocol {
    private class Presence(val blob: ByteArray, val secret: ByteArray)

    private val presences = mutableMapOf<String, Presence>()
    private val mailboxes = mutableMapOf<String, MutableList<RelayMessage>>()
    private var nextId = 1L

    var failPuts = false
    /** Fails PUTs only for presences whose owner secret matches; simulates one frozen phone. */
    var failPutsFor: ((ownerSecret: ByteArray) -> Boolean)? = null
    var failPolls = false
    var putCount = 0
    var postCount = 0
    val deleted = mutableListOf<String>()

    /** Lifetime of a presence, as the relay's NEARBY_PRESENCE_TTL_SECONDS. */
    var presenceTtlMs = 300_000L
    /** Expiry returned by PUT; `null` simulates an unreadable timestamp. */
    var returnExpiry = true

    fun hasPresence(lookupId: ByteArray) = NearbyCrypto.hex(lookupId) in presences

    override suspend fun putPresence(lookupId: ByteArray, blob: ByteArray, ownerSecret: ByteArray): Long? {
        putCount += 1
        if (failPuts || failPutsFor?.invoke(ownerSecret) == true) throw NetworkError.NoConnection()
        val key = NearbyCrypto.hex(lookupId)
        presences[key]?.let { if (!it.secret.contentEquals(ownerSecret)) throw NetworkError.ServerError(409) }
        presences[key] = Presence(blob, ownerSecret)
        return if (returnExpiry) clock() + presenceTtlMs else null
    }

    override suspend fun batchGet(lookupIds: List<ByteArray>): Map<String, ByteArray> =
        lookupIds.map(NearbyCrypto::hex).mapNotNull { id -> presences[id]?.let { id to it.blob } }.toMap()

    override suspend fun deletePresence(lookupId: ByteArray, ownerSecret: ByteArray) {
        val key = NearbyCrypto.hex(lookupId)
        val presence = presences[key] ?: return
        if (!presence.secret.contentEquals(ownerSecret)) throw NetworkError.ServerError(403)
        presences.remove(key)
        mailboxes.remove(key)
        deleted += key
    }

    override suspend fun postMessage(lookupId: ByteArray, header: ByteArray, body: ByteArray) {
        val key = NearbyCrypto.hex(lookupId)
        if (key !in presences) throw NetworkError.ServerError(404)
        postCount += 1
        mailboxes.getOrPut(key) { mutableListOf() } += RelayMessage((nextId++).toString(), header, body)
    }

    override suspend fun pollMessages(
        lookupId: ByteArray,
        ownerSecret: ByteArray,
        after: String?,
        wait: Int,
    ): RelayMessagesPage {
        val key = NearbyCrypto.hex(lookupId)
        val afterId = after?.toLong() ?: 0
        if (failPolls) throw NetworkError.NoConnection()
        val presence = presences[key] ?: throw NetworkError.ServerError(404)
        if (!presence.secret.contentEquals(ownerSecret)) throw NetworkError.ServerError(403)
        mailboxes[key]?.removeAll { it.id.toLong() <= afterId }
        // Long poll: re-check every 100 ms of virtual time until `wait` passes.
        repeat(wait * 10 + 1) { attempt ->
            val pending = mailboxes[key].orEmpty().filter { it.id.toLong() > afterId }
            if (pending.isNotEmpty()) return RelayMessagesPage(pending.toList(), pending.last().id)
            if (attempt < wait * 10) delay(100)
        }
        return RelayMessagesPage(emptyList(), afterId.toString())
    }

    /** Puts an arbitrary message in a mailbox, as an attacker or a replay would. */
    fun inject(lookupId: ByteArray, header: ByteArray, body: ByteArray) {
        mailboxes.getOrPut(NearbyCrypto.hex(lookupId)) { mutableListOf() } +=
            RelayMessage((nextId++).toString(), header, body)
    }
}

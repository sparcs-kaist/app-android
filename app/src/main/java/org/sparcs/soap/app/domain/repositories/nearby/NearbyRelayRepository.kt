package org.sparcs.soap.app.domain.repositories.nearby

import com.google.gson.Gson
import org.sparcs.soap.app.domain.nearby.NearbyCrypto
import org.sparcs.soap.app.networking.requestDTO.nearby.BatchGetPresencesRequestDTO
import org.sparcs.soap.app.networking.requestDTO.nearby.PostNearbyMessageRequestDTO
import org.sparcs.soap.app.networking.requestDTO.nearby.PutPresenceRequestDTO
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.nearby.NearbyRelayApi
import java.time.Instant
import java.time.format.DateTimeParseException
import javax.inject.Inject

class RelayMessage(
    val id: String,
    val header: ByteArray,
    val body: ByteArray,
)

class RelayMessagesPage(
    val messages: List<RelayMessage>,
    val cursor: String,
)

/** Same surface as iOS `NearbyRelayRepositoryProtocol`; every value is raw bytes. */
interface NearbyRelayRepositoryProtocol {
    /** Creates or renews a presence; returns when it expires (epoch ms), or `null` if unreadable. */
    suspend fun putPresence(lookupId: ByteArray, blob: ByteArray, ownerSecret: ByteArray): Long?

    /** lookupId (hex) → blob, for the presences that still exist. */
    suspend fun batchGet(lookupIds: List<ByteArray>): Map<String, ByteArray>
    suspend fun deletePresence(lookupId: ByteArray, ownerSecret: ByteArray)
    suspend fun postMessage(lookupId: ByteArray, header: ByteArray, body: ByteArray)
    suspend fun pollMessages(lookupId: ByteArray, ownerSecret: ByteArray, after: String?, wait: Int): RelayMessagesPage
}

class NearbyRelayRepository @Inject constructor(
    private val api: NearbyRelayApi,
    private val gson: Gson = Gson(),
) : NearbyRelayRepositoryProtocol {

    override suspend fun putPresence(lookupId: ByteArray, blob: ByteArray, ownerSecret: ByteArray): Long? {
        val response = safeApiCall(gson) {
            api.putPresence(
                NearbyCrypto.hex(lookupId),
                NearbyCrypto.base64Url(ownerSecret),
                PutPresenceRequestDTO(blob = NearbyCrypto.base64Url(blob))
            )
        }
        return try {
            Instant.parse(response.expiresAt).toEpochMilli()
        } catch (e: DateTimeParseException) {
            null
        }
    }

    override suspend fun batchGet(lookupIds: List<ByteArray>): Map<String, ByteArray> {
        if (lookupIds.isEmpty()) return emptyMap()
        val response = safeApiCall(gson) {
            api.batchGet(BatchGetPresencesRequestDTO(lookupIds = lookupIds.map(NearbyCrypto::hex)))
        }
        return response.items.associate { it.lookupId to NearbyCrypto.fromBase64Url(it.blob) }
    }

    override suspend fun deletePresence(lookupId: ByteArray, ownerSecret: ByteArray) {
        safeApiCall(gson) {
            val response = api.deletePresence(NearbyCrypto.hex(lookupId), NearbyCrypto.base64Url(ownerSecret))
            if (!response.isSuccessful) throw retrofit2.HttpException(response)
        }
    }

    override suspend fun postMessage(lookupId: ByteArray, header: ByteArray, body: ByteArray) {
        safeApiCall(gson) {
            api.postMessage(
                NearbyCrypto.hex(lookupId),
                PostNearbyMessageRequestDTO(
                    header = NearbyCrypto.base64Url(header),
                    body = NearbyCrypto.base64Url(body)
                )
            )
        }
    }

    override suspend fun pollMessages(
        lookupId: ByteArray,
        ownerSecret: ByteArray,
        after: String?,
        wait: Int,
    ): RelayMessagesPage = safeApiCall(gson) {
        api.pollMessages(
            NearbyCrypto.hex(lookupId),
            NearbyCrypto.base64Url(ownerSecret),
            after = after?.toLongOrNull() ?: 0,
            wait = wait
        )
    }.toModel()
}

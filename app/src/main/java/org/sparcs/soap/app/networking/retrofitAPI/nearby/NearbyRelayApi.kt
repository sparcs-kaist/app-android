package org.sparcs.soap.app.networking.retrofitAPI.nearby

import org.sparcs.soap.app.networking.requestDTO.nearby.BatchGetPresencesRequestDTO
import org.sparcs.soap.app.networking.requestDTO.nearby.PostNearbyMessageRequestDTO
import org.sparcs.soap.app.networking.requestDTO.nearby.PutPresenceRequestDTO
import org.sparcs.soap.app.networking.responseDTO.nearby.BatchGetPresencesResponseDTO
import org.sparcs.soap.app.networking.responseDTO.nearby.MessageCreatedResponseDTO
import org.sparcs.soap.app.networking.responseDTO.nearby.MessagesPageResponseDTO
import org.sparcs.soap.app.networking.responseDTO.nearby.PresenceExpiryResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The Nearby Relay on the Buddy (feed) backend. It holds only ciphertext; the
 * OTL token never goes here — friendships are created by calling OTL directly.
 */
interface NearbyRelayApi {
    @PUT("nearby/presences/{lookupId}")
    suspend fun putPresence(
        @Path("lookupId") lookupId: String,
        @Header(OWNER_SECRET) ownerSecret: String,
        @Body request: PutPresenceRequestDTO,
    ): PresenceExpiryResponseDTO

    @POST("nearby/presences:batchGet")
    suspend fun batchGet(@Body request: BatchGetPresencesRequestDTO): BatchGetPresencesResponseDTO

    @DELETE("nearby/presences/{lookupId}")
    suspend fun deletePresence(
        @Path("lookupId") lookupId: String,
        @Header(OWNER_SECRET) ownerSecret: String,
    ): Response<Unit>

    @POST("nearby/presences/{lookupId}/messages")
    suspend fun postMessage(
        @Path("lookupId") lookupId: String,
        @Body request: PostNearbyMessageRequestDTO,
    ): MessageCreatedResponseDTO

    @GET("nearby/presences/{lookupId}/messages")
    suspend fun pollMessages(
        @Path("lookupId") lookupId: String,
        @Header(OWNER_SECRET) ownerSecret: String,
        @Query("after") after: Long,
        @Query("wait") wait: Int,
    ): MessagesPageResponseDTO

    companion object {
        const val OWNER_SECRET = "X-Owner-Secret"
    }
}

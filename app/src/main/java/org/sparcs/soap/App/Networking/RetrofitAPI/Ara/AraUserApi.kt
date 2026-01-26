package org.sparcs.soap.App.Networking.RetrofitAPI.Ara


import org.sparcs.soap.App.Networking.ResponseDTO.Ara.AraSignInResponseDTO
import org.sparcs.soap.App.Networking.ResponseDTO.Ara.AraUserDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AraUserApi {

    @POST("users/oneapp-login/")
    suspend fun register(
        @Body body: Map<String, String>
    ): AraSignInResponseDTO

    @PATCH("user_profiles/{id}/agree_terms_of_service/")
    suspend fun agreeTOS(
        @Path("id") userID: Int
    )

    @GET("me")
    suspend fun fetchMe(): AraUserDTO

    @PATCH("user_profiles/{id}/")
    @JvmSuppressWildcards
    suspend fun updateUser(
        @Path("id") userID: Int,
        @Body params: Map<String, Any>
    )
}

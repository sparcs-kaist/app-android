package org.sparcs.soap.app.networking.retrofitAPI.otl

import org.sparcs.soap.app.networking.requestDTO.otl.InterestedDepartmentsRequestDTO
import org.sparcs.soap.app.networking.responseDTO.otl.CourseLecturePageDTO
import org.sparcs.soap.app.networking.responseDTO.otl.OTLUserDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OTLUserApi {
    @GET("api/v2/users/{userID}/wishlist")
    suspend fun fetchWishlist(@Path("userID") userID: Int, @Query("year") year: Int, @Query("semester") semester: Int): CourseLecturePageDTO

    @PATCH("api/v2/users/{userID}/wishlist")
    suspend fun updateWishlist(@Path("userID") userID: Int, @Body params: Map<String, @JvmSuppressWildcards Any>)

    @PUT("api/v2/users/{userID}/interested-departments")
    suspend fun updateInterestedDepartments(
        @Path("userID") userID: Int,
        @Body params: InterestedDepartmentsRequestDTO,
    )


    @POST("session/register-oneapp")
    suspend fun register(
        @Body params: Map<String, String>
    )

    @GET("api/v2/users/info")
    suspend fun fetchUserInfo(): Response<OTLUserDTO>
}

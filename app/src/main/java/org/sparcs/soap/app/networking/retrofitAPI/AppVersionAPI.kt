package org.sparcs.soap.app.networking.retrofitAPI

import org.sparcs.soap.app.networking.responseDTO.MinimumRequiredAppVersionDTO
import retrofit2.http.GET

interface AppVersionApi {
    @GET("app_version/required")
    suspend fun getMinimumRequiredVersion(): MinimumRequiredAppVersionDTO
}
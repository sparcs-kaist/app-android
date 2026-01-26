package org.sparcs.soap.App.Networking.RetrofitAPI

import org.sparcs.soap.App.Networking.ResponseDTO.MinimumRequiredAppVersionDTO
import retrofit2.http.GET

interface AppVersionApi {
    @GET("app_version/required")
    suspend fun getMinimumRequiredVersion(): MinimumRequiredAppVersionDTO
}
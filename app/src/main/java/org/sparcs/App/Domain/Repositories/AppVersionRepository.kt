package org.sparcs.App.Domain.Repositories

import org.sparcs.App.Networking.ResponseDTO.MinimumRequiredAppVersionDTO
import org.sparcs.App.Networking.RetrofitAPI.AppVersionApi
import javax.inject.Inject

class AppVersionRepository @Inject constructor(
    private val appVersionApi: AppVersionApi
) {
    suspend fun fetchMinimumVersion(): MinimumRequiredAppVersionDTO {
        return try {
            appVersionApi.getMinimumRequiredVersion()
        } catch (e: Exception) {
            MinimumRequiredAppVersionDTO(android = "1.0.0")
        }
    }
}
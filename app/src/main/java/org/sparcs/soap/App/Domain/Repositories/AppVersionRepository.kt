package org.sparcs.soap.App.Domain.Repositories

import org.sparcs.soap.App.Domain.Services.CrashlyticsService
import org.sparcs.soap.App.Networking.ResponseDTO.MinimumRequiredAppVersionDTO
import org.sparcs.soap.App.Networking.RetrofitAPI.AppVersionApi
import javax.inject.Inject

class AppVersionRepository @Inject constructor(
    private val appVersionApi: AppVersionApi,
    private val crashlyticsService: CrashlyticsService
) {
    suspend fun fetchMinimumVersion(): MinimumRequiredAppVersionDTO {
        return try {
            appVersionApi.getMinimumRequiredVersion()
        } catch (e: Exception) {
            crashlyticsService.recordException(e)
            MinimumRequiredAppVersionDTO(android = "1.0.0")
        }
    }
}
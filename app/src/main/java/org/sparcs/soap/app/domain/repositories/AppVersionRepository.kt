package org.sparcs.soap.app.domain.repositories

import org.sparcs.soap.app.domain.services.CrashlyticsService
import org.sparcs.soap.app.networking.responseDTO.MinimumRequiredAppVersionDTO
import org.sparcs.soap.app.networking.retrofitAPI.AppVersionApi
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
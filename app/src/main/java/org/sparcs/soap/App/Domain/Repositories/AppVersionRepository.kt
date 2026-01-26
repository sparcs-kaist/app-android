package org.sparcs.soap.App.Domain.Repositories

import android.util.Log
import org.sparcs.soap.App.Domain.Helpers.CrashlyticsHelper
import org.sparcs.soap.App.Networking.ResponseDTO.MinimumRequiredAppVersionDTO
import org.sparcs.soap.App.Networking.RetrofitAPI.AppVersionApi
import javax.inject.Inject

class AppVersionRepository @Inject constructor(
    private val appVersionApi: AppVersionApi,
    private val crashlyticsHelper: CrashlyticsHelper
) {
    suspend fun fetchMinimumVersion(): MinimumRequiredAppVersionDTO {
        return try {
            appVersionApi.getMinimumRequiredVersion()
        } catch (e: Exception) {
            crashlyticsHelper.recordException(e)
            Log.e("AppVersionRepository", "Error fetching minimum version: $e")
            MinimumRequiredAppVersionDTO(android = "1.0.0")
        }
    }
}
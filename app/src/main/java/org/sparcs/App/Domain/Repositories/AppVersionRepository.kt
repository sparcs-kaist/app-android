package org.sparcs.App.Domain.Repositories

import android.util.Log
import org.sparcs.App.Domain.Helpers.CrashlyticsHelper
import org.sparcs.App.Networking.ResponseDTO.MinimumRequiredAppVersionDTO
import org.sparcs.App.Networking.RetrofitAPI.AppVersionApi
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
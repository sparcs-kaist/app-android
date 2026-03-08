package org.sparcs.soap.App.Networking.RetrofitAPI

import org.sparcs.soap.App.Networking.RequestDTO.ManageAlertRequestDTO
import org.sparcs.soap.App.Networking.RequestDTO.RegisterDeviceRequestDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FCMApi {

    @POST("notification/device_info")
    suspend fun registerDevice(
        @Body body: RegisterDeviceRequestDTO
    ): Response<Unit>

    @POST("notification/manage_alert")
    suspend fun manageAlert(
        @Body body: ManageAlertRequestDTO
    ): Response<Unit>
}
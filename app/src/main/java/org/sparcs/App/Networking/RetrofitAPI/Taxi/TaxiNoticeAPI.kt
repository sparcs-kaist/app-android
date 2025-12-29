package org.sparcs.App.Networking.RetrofitAPI.Taxi

import org.sparcs.App.Networking.ResponseDTO.Taxi.TaxiNoticeDTO
import retrofit2.Response
import retrofit2.http.GET

interface TaxiNoticeApi {

    @GET("notice/list")
    suspend fun fetchNotice(): Response<TaxiNoticeDTO>
}

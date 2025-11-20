package com.sparcs.soap.Networking.RetrofitAPI.Taxi

import com.sparcs.soap.Networking.ResponseDTO.Taxi.TaxiNoticeDTO
import retrofit2.Response
import retrofit2.http.GET

interface TaxiNoticeApi {

    @GET("notice/list")
    suspend fun fetchNotice(): Response<TaxiNoticeDTO>
}

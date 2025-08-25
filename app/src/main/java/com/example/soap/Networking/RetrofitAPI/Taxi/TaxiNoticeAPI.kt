package com.example.soap.Networking.RetrofitAPI.Taxi

import com.example.soap.Networking.ResponseDTO.Taxi.TaxiNoticeDTO
import retrofit2.Response
import retrofit2.http.GET

interface TaxiNoticeApi {

    @GET("notice/list")
    suspend fun fetchNotice(): Response<TaxiNoticeDTO>
}

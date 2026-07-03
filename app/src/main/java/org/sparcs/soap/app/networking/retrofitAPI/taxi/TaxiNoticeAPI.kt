package org.sparcs.soap.app.networking.retrofitAPI.taxi

import org.sparcs.soap.app.networking.responseDTO.taxi.TaxiNoticeDTO
import retrofit2.Response
import retrofit2.http.GET

interface TaxiNoticeApi {

    @GET("notice/list")
    suspend fun fetchNotice(): Response<TaxiNoticeDTO>
}

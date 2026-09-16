package org.sparcs.soap.app.networking.retrofitAPI

import org.sparcs.soap.app.networking.responseDTO.TimetableThemeDTO
import org.sparcs.soap.app.networking.responseDTO.TimetableThemeShareResponseDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TimetableThemeApi {
    @POST("timetable-themes/shares")
    suspend fun share(@Body theme: TimetableThemeDTO): TimetableThemeShareResponseDTO

    @GET("timetable-themes/shares/{code}")
    suspend fun fetch(@Path("code") code: String): TimetableThemeShareResponseDTO
}

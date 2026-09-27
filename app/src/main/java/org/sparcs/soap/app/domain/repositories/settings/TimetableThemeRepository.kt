package org.sparcs.soap.app.domain.repositories.settings

import com.google.gson.Gson
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.networking.responseDTO.TimetableThemeDTO
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.TimetableThemeApi
import javax.inject.Inject

interface TimetableThemeRepositoryProtocol {
    suspend fun share(theme: TimetableTheme): String
    suspend fun fetch(code: String): TimetableTheme
}

class TimetableThemeRepository @Inject constructor(
    private val api: TimetableThemeApi,
    private val gson: Gson,
) : TimetableThemeRepositoryProtocol {
    override suspend fun share(theme: TimetableTheme): String = safeApiCall(gson) {
        api.share(TimetableThemeDTO.fromModel(theme))
    }.code

    override suspend fun fetch(code: String): TimetableTheme = safeApiCall(gson) {
        api.fetch(code)
    }.theme.toModel()
}

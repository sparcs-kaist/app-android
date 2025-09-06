package com.example.soap.Domain.Repositories.Ara

import com.example.soap.Networking.ResponseDTO.Ara.AraSignInResponseDTO
import com.example.soap.Networking.RetrofitAPI.Ara.AraUserApi
import javax.inject.Inject

interface AraUserRepositoryProtocol {

    suspend fun register(ssoInfo: String): AraSignInResponseDTO
    suspend fun agreeTOS(userID: Int)
}

class AraUserRepository @Inject constructor(
    private val araUserApi: AraUserApi
) : AraUserRepositoryProtocol {

    override suspend fun register(ssoInfo: String): AraSignInResponseDTO {
        return araUserApi.register(ssoInfo)
    }

    override suspend fun agreeTOS(userID: Int) {
        araUserApi.agreeTOS(userID)
    }
}
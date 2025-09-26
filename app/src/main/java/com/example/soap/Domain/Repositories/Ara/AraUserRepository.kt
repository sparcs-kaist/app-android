package com.example.soap.Domain.Repositories.Ara

import com.example.soap.Domain.Models.Ara.AraUser
import com.example.soap.Networking.ResponseDTO.Ara.AraSignInResponseDTO
import com.example.soap.Networking.RetrofitAPI.Ara.AraUserApi
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

interface AraUserRepositoryProtocol {
    suspend fun register(ssoInfo: String): AraSignInResponseDTO
    suspend fun agreeTOS(userID: Int)
    suspend fun fetchUser(): AraUser
    suspend fun updateMe(id: Int, params: Map<String, Any>)
}

class AraUserRepository @Inject constructor(
    private val api: AraUserApi
) : AraUserRepositoryProtocol {

    override suspend fun register(ssoInfo: String): AraSignInResponseDTO {
        val response = api.register(mapOf("ssoInfo" to ssoInfo))
        if (!response.isSuccessful) throw HttpException(response)
        return response.body() ?: throw IOException("Empty response body")
    }

    override suspend fun agreeTOS(userID: Int) {
        val response = api.agreeTOS(userID)
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun fetchUser(): AraUser {
        val response = api.fetchMe()
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()?.toModel() ?: throw IOException("Empty response body")
    }

    override suspend fun updateMe(id: Int, params: Map<String, Any>) {
        val response = api.updateUser(id, params)
        if (!response.isSuccessful) throw HttpException(response)
    }
}
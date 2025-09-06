package com.example.soap.Domain.Usecases

interface FoundationModelsUseCaseProtocol {
    val isAvailable: Boolean

    suspend fun summarise(text: String, maxWords: Int, tone: String): String
}
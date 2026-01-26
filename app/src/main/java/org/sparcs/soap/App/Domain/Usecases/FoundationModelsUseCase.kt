package org.sparcs.soap.App.Domain.Usecases

import org.sparcs.soap.App.Domain.Repositories.Ara.AraUserRepositoryProtocol
import javax.inject.Inject
import javax.inject.Singleton

interface FoundationModelsUseCaseProtocol {
    val isAvailable: Boolean

    suspend fun summarise(text: String, maxWords: Int, tone: String): String
}

@Singleton
class FoundationModelsUseCase @Inject constructor(
    private val araUserRepository: AraUserRepositoryProtocol
) : FoundationModelsUseCaseProtocol {

    override val isAvailable: Boolean = true
    override suspend fun summarise(text: String, maxWords: Int, tone: String): String {
        return ""
    }
    //TODO - 요약기능, 좀 더 고민이 필요할 듯
}
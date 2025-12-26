package org.sparcs.Domain.Usecases

import org.sparcs.Domain.Repositories.Ara.AraUserRepositoryProtocol
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
package org.sparcs.soap.app.domain.usecases.otl

import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.repositories.otl.OTLUserRepositoryProtocol
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import javax.inject.Inject

class WishlistUseCase @Inject constructor(
    private val otlUserRepository: OTLUserRepositoryProtocol,
    private val userUseCase: UserUseCaseProtocol,
) {
    private suspend fun userID(): Int {
        if (userUseCase.otlUser == null) userUseCase.fetchOTLUser()
        return checkNotNull(userUseCase.otlUser).id
    }

    suspend fun fetchWishlist(semester: Semester): List<CourseLecture> =
        otlUserRepository.fetchWishlist(userID(), semester)

    suspend fun setWishlisted(isWishlisted: Boolean, lectureID: Int) =
        otlUserRepository.updateWishlist(userID(), lectureID, isWishlisted)
}

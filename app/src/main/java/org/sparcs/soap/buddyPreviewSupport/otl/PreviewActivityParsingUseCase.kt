package org.sparcs.soap.buddyPreviewSupport.otl

import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.ParsedActivity
import org.sparcs.soap.app.domain.usecases.otl.ActivityParsingUseCaseProtocol

class PreviewActivityParsingUseCase : ActivityParsingUseCaseProtocol {
    override suspend fun isAvailable(): Boolean = true
    override suspend fun parse(description: String, today: DayType): ParsedActivity =
        ParsedActivity(title = description, day = today)
}

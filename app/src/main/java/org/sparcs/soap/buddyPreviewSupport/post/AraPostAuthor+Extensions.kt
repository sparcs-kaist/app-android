package org.sparcs.soap.buddyPreviewSupport.post

import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostAuthor
import org.sparcs.soap.app.shared.mocks.ara.mock

val AraPostAuthor.Companion.previewAuthor: AraPostAuthor
    get() = AraPost.mock().author
package org.sparcs.soap.BuddyPreviewSupport.Post

import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Domain.Models.Ara.AraPostAuthor
import org.sparcs.soap.App.Shared.Mocks.Ara.mock

val AraPostAuthor.Companion.previewAuthor: AraPostAuthor
    get() = AraPost.mock().author
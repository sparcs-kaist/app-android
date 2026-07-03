package org.sparcs.soap.app.domain.models.ara

import java.net.URL

data class AraPostAuthorProfile(
    val id: String,
    val profilePictureURL: URL?,
    val nickname: String,
    val isOfficial: Boolean?,
    val isSchoolAdmin: Boolean?
)
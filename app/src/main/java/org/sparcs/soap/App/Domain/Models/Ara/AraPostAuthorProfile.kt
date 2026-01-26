package org.sparcs.soap.App.Domain.Models.Ara

import java.net.URL

data class AraPostAuthorProfile(
    val id: String,
    val profilePictureURL: URL?,
    val nickname: String,
    val isOfficial: Boolean?,
    val isSchoolAdmin: Boolean?
)
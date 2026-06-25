package org.sparcs.soap.app.domain.models.ara

import org.sparcs.soap.app.domain.helpers.LocalizedString


data class AraBoard(
    val id: Int,
    val slug: String,
    val name: LocalizedString,
    val group: AraBoardGroup,
    val topics: List<AraBoardTopic>,
    val isReadOnly: Boolean,
    val userReadable: Boolean?,
    val userWritable: Boolean?
){
    companion object
}
package org.sparcs.App.Domain.Models.Ara

import org.sparcs.App.Domain.Helpers.LocalizedString


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
    companion object{}
}
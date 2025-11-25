package com.sparcs.soap.Domain.Models.Ara

import com.sparcs.soap.Domain.Helpers.LocalizedString


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
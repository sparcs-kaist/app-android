package org.sparcs.soap.app.domain.models.ara

import org.sparcs.soap.app.domain.enums.ara.AraPostNicknameType
import org.sparcs.soap.app.domain.helpers.LocalizedString


data class AraBoard(
    val id: Int,
    val slug: String,
    val name: LocalizedString,
    val group: AraBoardGroup,
    val topics: List<AraBoardTopic>,
    val isReadOnly: Boolean,
    val userReadable: Boolean?,
    val userWritable: Boolean?,
    val nicknameTypes: List<AraPostNicknameType> = emptyList(),
){
    val isMarket: Boolean
        get() = slug == MARKET_SLUG

    fun allows(type: AraPostNicknameType): Boolean =
        if (nicknameTypes.isEmpty()) type != AraPostNicknameType.REALNAME else type in nicknameTypes

    companion object {
        const val MARKET_SLUG = "market"
    }
}

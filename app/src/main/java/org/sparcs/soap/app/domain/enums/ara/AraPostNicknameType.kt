package org.sparcs.soap.app.domain.enums.ara

enum class AraPostNicknameType(val type: String, private val flag: Int) {
    ANONYMOUS("ANONYMOUS", 2),
    REGULAR("REGULAR", 1),
    REALNAME("REALNAME", 4);

    companion object {
        fun fromCode(code: Int): AraPostNicknameType? = entries.firstOrNull { it.flag == code }

        fun allowed(flags: Int): List<AraPostNicknameType> =
            listOf(REGULAR, ANONYMOUS, REALNAME).filter { flags and it.flag != 0 }
    }
}

package org.sparcs.soap.App.Domain.Enums.OTL

import androidx.annotation.StringRes
import org.sparcs.soap.R

enum class LectureType(@StringRes val code: Int) {
    BR(R.string.br),
    BE(R.string.be),
    MR(R.string.mr),
    ME(R.string.me),
    HSE(R.string.hse),
    ETC(R.string.etc);

    companion object {
        fun fromString(string: String): LectureType {
            val index = listOf("기초필수", "기초선택", "전공필수", "전공선택", "인문사회선택").indexOfFirst { string.contains(it) }
            return if (index != -1) {
                entries[index]
            } else {
                entries.find { string.contains(it.name) } ?: ETC
            }
        }
    }
}
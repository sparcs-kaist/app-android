package org.sparcs.soap.app.domain.enums.otl

import androidx.annotation.StringRes
import org.sparcs.soap.R

enum class LectureType(@param:StringRes val labelRes: Int, val displayName: Int) {
    BR(R.string.br, R.string.lecture_type_br_full),
    BE(R.string.be, R.string.lecture_type_be_full),
    MR(R.string.mr, R.string.lecture_type_mr_full),
    ME(R.string.me, R.string.lecture_type_me_full),
    HSE(R.string.hse, R.string.lecture_type_hse_full),
    HSE_CORE(R.string.hse_core, R.string.lecture_type_hse_core_full),
    HSE_GENERAL(R.string.hse_general, R.string.lecture_type_hse_general_full),
    ETC(R.string.etc, R.string.lecture_type_etc_full);
    companion object {
        fun fromString(string: String): LectureType {
            val normalized = string.filterNot(Char::isWhitespace).lowercase()
            if (normalized.startsWith("인문사회") || normalized.startsWith("인선") ||
                normalized.startsWith("humanities") || normalized.startsWith("hse")) {
                return when {
                    normalized.contains("핵심") || normalized.contains("core") -> HSE_CORE
                    normalized.contains("일반") || normalized.contains("general") -> HSE_GENERAL
                    else -> HSE
                }
            }
            return when (normalized.substringBefore('(')) {
                "기초필수", "basicrequired", "br" -> BR
                "기초선택", "basicelective", "be" -> BE
                "전공필수", "majorrequired", "mr" -> MR
                "전공선택", "majorelective", "me" -> ME
                else -> ETC
            }
        }
    }
}

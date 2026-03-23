package org.sparcs.soap.App.Domain.Enums.OTL

import androidx.annotation.StringRes
import org.sparcs.soap.R

enum class LectureType(@StringRes val labelRes: Int, val displayName: Int) {
    BR(R.string.br, R.string.lecture_type_br_full),
    BE(R.string.be, R.string.lecture_type_be_full),
    MR(R.string.mr, R.string.lecture_type_mr_full),
    ME(R.string.me, R.string.lecture_type_me_full),
    HSE(R.string.hse, R.string.lecture_type_hse_full),
    ETC(R.string.etc, R.string.lecture_type_etc_full);
    companion object {
        fun fromString(string: String): LectureType {
            return when {
                string.contains("기초필수") || string.contains("Basic Required") -> BR
                string.contains("기초선택") || string.contains("Basic Elective") -> BE
                string.contains("전공필수") || string.contains("Major Required") -> MR
                string.contains("전공선택") || string.contains("Major Elective") -> ME
                string.contains("인문사회선택") || string.contains("Humanities and Social Elective") -> HSE

                else -> entries.find { string.contains(it.name, ignoreCase = true) } ?: ETC
            }
        }
    }
}
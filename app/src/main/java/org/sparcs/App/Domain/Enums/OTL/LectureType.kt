package org.sparcs.App.Domain.Enums.OTL

import org.sparcs.App.Domain.Helpers.LocalizedString

enum class LectureType(val code: LocalizedString, val displayName: LocalizedString) {
    BR(
        LocalizedString(mapOf("en" to "BR", "ko" to "기필")),
        LocalizedString(mapOf("en" to "Basic Required", "ko" to "기초필수"))
    ),
    BE(
        LocalizedString(mapOf("en" to "BE", "ko" to "기선")),
        LocalizedString(mapOf("en" to "Basic Elective", "ko" to "기초선택"))
    ),
    MR(
        LocalizedString(mapOf("en" to "MR", "ko" to "전필")),
        LocalizedString(mapOf("en" to "Major Required", "ko" to "전공필수"))
    ),
    ME(
        LocalizedString(mapOf("en" to "ME", "ko" to "전선")),
        LocalizedString(mapOf("en" to "Major Elective", "ko" to "전공선택"))
    ),
    HSE(
        LocalizedString(mapOf("en" to "HSE", "ko" to "인선")),
        LocalizedString(mapOf("en" to "Humanities and Social Elective", "ko" to "인문사회선택"))
    ),
    ETC(
        LocalizedString(mapOf("en" to "ETC", "ko" to "기타")),
        LocalizedString(mapOf("en" to "ETC", "ko" to "기타"))
    );

    companion object {
        fun fromRawValue(rawValue: String): LectureType {
            return when (rawValue) {
                "Basic Required" -> BR
                "Basic Elective" -> BE
                "Major Required" -> MR
                "Major Elective" -> ME
                "Humanities and Social Elective" -> HSE
                else -> ETC
            }
        }
    }
}
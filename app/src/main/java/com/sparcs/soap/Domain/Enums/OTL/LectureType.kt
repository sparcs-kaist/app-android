package com.sparcs.soap.Domain.Enums.OTL

import com.sparcs.soap.Domain.Helpers.LocalizedString

enum class LectureType(val code: String, val displayName: LocalizedString) {
    BR("BR", LocalizedString(mapOf("en" to "Basic Required", "ko" to "기초필수"))),
    BE("BE", LocalizedString(mapOf("en" to "Basic Elective", "ko" to "기초선택"))),
    MR("MR", LocalizedString(mapOf("en" to "Major Required", "ko" to "전공필수"))),
    ME("ME", LocalizedString(mapOf("en" to "Major Elective", "ko" to "전공선택"))),
    HSE("HSE", LocalizedString(mapOf("en" to "Humanities and Social Elective", "ko" to "인문사회선택"))),
    ETC("ETC", LocalizedString(mapOf("en" to "ETC", "ko" to "기타")));

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
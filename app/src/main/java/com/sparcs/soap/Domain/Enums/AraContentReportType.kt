package com.sparcs.soap.Domain.Enums

enum class AraContentReportType(val type: String) {
    HATE_SPEECH("hate_speech"),
    UNAUTHORIZED_SALES("unauthorized_sales_articles"),
    SPAM("spam"),
    FALSE_INFORMATION("fake_information"),
    DEFAMATION("defamation"),
    OTHER("other")
}

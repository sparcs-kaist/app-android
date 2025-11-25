package com.sparcs.soap.Domain.Enums.Ara

import androidx.annotation.StringRes
import com.sparcs.soap.Domain.Enums.Feed.ReportLabelProvider
import com.sparcs.soap.R

enum class AraContentReportType(
    val type: String,
    @StringRes override val labelRes: Int,
): ReportLabelProvider {
    HATE_SPEECH("hate_speech", R.string.report_hate_speech),
    UNAUTHORIZED_SALES("unauthorized_sales_articles", R.string.report_unauthorized_sales),
    SPAM("spam", R.string.report_spam),
    FALSE_INFORMATION("fake_information", R.string.report_false_information),
    DEFAMATION("defamation", R.string.report_defamation),
    OTHER("other", R.string.report_other)
}


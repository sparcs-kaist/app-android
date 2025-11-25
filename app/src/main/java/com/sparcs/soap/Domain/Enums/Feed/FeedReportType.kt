package com.sparcs.soap.Domain.Enums.Feed

import androidx.annotation.StringRes
import com.sparcs.soap.R

interface ReportLabelProvider {
    val labelRes: Int
}

enum class FeedReportType(
    val type: String,
    @StringRes override val labelRes: Int,
): ReportLabelProvider {
    EXTREME_POLITICS("EXTREME_POLITICS", R.string.report_extreme_politics),
    PORNOGRAPHY("PORNOGRAPHY", R.string.report_pornography),
    SPAM("SPAM", R.string.report_spam),
    ABUSIVE_LANGUAGE("ABUSIVE_LANGUAGE", R.string.report_abusive_language),
    IMPERSONATION_FRAUD("IMPERSONATION_FRAUD", R.string.report_impersonation_fraud),
    COMMERCIAL_AD("COMMERCIAL_AD", R.string.report_commercial_ad),
}


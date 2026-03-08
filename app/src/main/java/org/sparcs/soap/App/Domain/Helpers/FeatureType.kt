package org.sparcs.soap.App.Domain.Helpers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.sparcs.soap.R

enum class FeatureType(val rawValue: Int) {
    FEED(0),
    ARA(1),
    OTL(2),
    TAXI(3);

    @get:StringRes
    val prettyStringRes: Int
        get() = when (this) {
            FEED -> R.string.feed
            ARA -> R.string.ara
            OTL -> R.string.otl
            TAXI -> R.string.taxi
        }

    @get:DrawableRes
    val iconRes: Int
        get() = when (this) {
            FEED -> R.drawable.sparcs_logo
            ARA -> R.drawable.ara_logo
            OTL -> R.drawable.otl_logo
            TAXI -> R.drawable.taxi_logo
        }

    companion object {
        fun fromRawValue(value: Int): FeatureType? =
            entries.find { it.rawValue == value }
    }
}
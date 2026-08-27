package org.sparcs.soap.app.theme.ui

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import org.sparcs.soap.R

@OptIn(ExperimentalTextApi::class)
val PretendardFontFamily = FontFamily(
    Font(
        resId = R.font.pretendard_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.pretendard_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.pretendard_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = R.font.pretendard_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        resId = R.font.pretendard_variable,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800))
    )
)

private fun TextStyle.withPretendard(): TextStyle {
    return this.copy(
        fontFamily = PretendardFontFamily,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.Both
        )
    )
}

@Composable
fun getAppTypography(): Typography {
    val defaultTypography = Typography()

    return Typography(
        displayLarge = defaultTypography.displayLarge.withPretendard(),
        displayMedium = defaultTypography.displayMedium.withPretendard(),
        displaySmall = defaultTypography.displaySmall.withPretendard(),

        headlineLarge = defaultTypography.headlineLarge.withPretendard(),
        headlineMedium = defaultTypography.headlineMedium.withPretendard(),
        headlineSmall = defaultTypography.headlineSmall.withPretendard(),

        titleLarge = defaultTypography.titleLarge.withPretendard(),
        titleMedium = defaultTypography.titleMedium.withPretendard(),
        titleSmall = defaultTypography.titleSmall.withPretendard(),

        bodyLarge = defaultTypography.bodyLarge.withPretendard(),
        bodyMedium = defaultTypography.bodyMedium.withPretendard(),
        bodySmall = defaultTypography.bodySmall.withPretendard(),

        labelLarge = defaultTypography.labelLarge.withPretendard(),
        labelMedium = defaultTypography.labelMedium.withPretendard(),
        labelSmall = defaultTypography.labelSmall.withPretendard()
    )
}
package org.sparcs.soap.app.theme.ui

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit

private fun createTextStyle(
    fontFamily: FontFamily,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit,
    fontWeight: FontWeight
) = TextStyle(
    fontFamily = fontFamily,
    fontSize = fontSize,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    fontWeight = fontWeight,
    platformStyle = PlatformTextStyle(
        includeFontPadding = false
    ),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    )
)

@Composable
fun getAppTypography(): Typography {
    val isKorean = Locale.current.language == "ko"

    return if (isKorean) {
        Typography(
            // Display
            displayLarge = createTextStyle(
                KoreanTypeScaleTokens.DisplayLargeFont,
                KoreanTypeScaleTokens.DisplayLargeSize,
                KoreanTypeScaleTokens.DisplayLargeLineHeight,
                KoreanTypeScaleTokens.DisplayLargeTracking,
                KoreanTypeScaleTokens.DisplayLargeWeight
            ),
            displayMedium = createTextStyle(
                KoreanTypeScaleTokens.DisplayMediumFont,
                KoreanTypeScaleTokens.DisplayMediumSize,
                KoreanTypeScaleTokens.DisplayMediumLineHeight,
                KoreanTypeScaleTokens.DisplayMediumTracking,
                KoreanTypeScaleTokens.DisplayMediumWeight
            ),
            displaySmall = createTextStyle(
                KoreanTypeScaleTokens.DisplaySmallFont,
                KoreanTypeScaleTokens.DisplaySmallSize,
                KoreanTypeScaleTokens.DisplaySmallLineHeight,
                KoreanTypeScaleTokens.DisplaySmallTracking,
                KoreanTypeScaleTokens.DisplaySmallWeight
            ),

            // Headline
            headlineLarge = createTextStyle(
                KoreanTypeScaleTokens.HeadlineLargeFont,
                KoreanTypeScaleTokens.HeadlineLargeSize,
                KoreanTypeScaleTokens.HeadlineLargeLineHeight,
                KoreanTypeScaleTokens.HeadlineLargeTracking,
                KoreanTypeScaleTokens.HeadlineLargeWeight
            ),
            headlineMedium = createTextStyle(
                KoreanTypeScaleTokens.HeadlineMediumFont,
                KoreanTypeScaleTokens.HeadlineMediumSize,
                KoreanTypeScaleTokens.HeadlineMediumLineHeight,
                KoreanTypeScaleTokens.HeadlineMediumTracking,
                KoreanTypeScaleTokens.HeadlineMediumWeight
            ),
            headlineSmall = createTextStyle(
                KoreanTypeScaleTokens.HeadlineSmallFont,
                KoreanTypeScaleTokens.HeadlineSmallSize,
                KoreanTypeScaleTokens.HeadlineSmallLineHeight,
                KoreanTypeScaleTokens.HeadlineSmallTracking,
                KoreanTypeScaleTokens.HeadlineSmallWeight
            ),

            // Title
            titleLarge = createTextStyle(
                KoreanTypeScaleTokens.TitleLargeFont,
                KoreanTypeScaleTokens.TitleLargeSize,
                KoreanTypeScaleTokens.TitleLargeLineHeight,
                KoreanTypeScaleTokens.TitleLargeTracking,
                KoreanTypeScaleTokens.TitleLargeWeight
            ),
            titleMedium = createTextStyle(
                KoreanTypeScaleTokens.TitleMediumFont,
                KoreanTypeScaleTokens.TitleMediumSize,
                KoreanTypeScaleTokens.TitleMediumLineHeight,
                KoreanTypeScaleTokens.TitleMediumTracking,
                KoreanTypeScaleTokens.TitleMediumWeight
            ),
            titleSmall = createTextStyle(
                KoreanTypeScaleTokens.TitleSmallFont,
                KoreanTypeScaleTokens.TitleSmallSize,
                KoreanTypeScaleTokens.TitleSmallLineHeight,
                KoreanTypeScaleTokens.TitleSmallTracking,
                KoreanTypeScaleTokens.TitleSmallWeight
            ),

            // Body
            bodyLarge = createTextStyle(
                KoreanTypeScaleTokens.BodyLargeFont,
                KoreanTypeScaleTokens.BodyLargeSize,
                KoreanTypeScaleTokens.BodyLargeLineHeight,
                KoreanTypeScaleTokens.BodyLargeTracking,
                KoreanTypeScaleTokens.BodyLargeWeight
            ),
            bodyMedium = createTextStyle(
                KoreanTypeScaleTokens.BodyMediumFont,
                KoreanTypeScaleTokens.BodyMediumSize,
                KoreanTypeScaleTokens.BodyMediumLineHeight,
                KoreanTypeScaleTokens.BodyMediumTracking,
                KoreanTypeScaleTokens.BodyMediumWeight
            ),
            bodySmall = createTextStyle(
                KoreanTypeScaleTokens.BodySmallFont,
                KoreanTypeScaleTokens.BodySmallSize,
                KoreanTypeScaleTokens.BodySmallLineHeight,
                KoreanTypeScaleTokens.BodySmallTracking,
                KoreanTypeScaleTokens.BodySmallWeight
            ),

            // Label
            labelLarge = createTextStyle(
                KoreanTypeScaleTokens.LabelLargeFont,
                KoreanTypeScaleTokens.LabelLargeSize,
                KoreanTypeScaleTokens.LabelLargeLineHeight,
                KoreanTypeScaleTokens.LabelLargeTracking,
                KoreanTypeScaleTokens.LabelLargeWeight
            ),
            labelMedium = createTextStyle(
                KoreanTypeScaleTokens.LabelMediumFont,
                KoreanTypeScaleTokens.LabelMediumSize,
                KoreanTypeScaleTokens.LabelMediumLineHeight,
                KoreanTypeScaleTokens.LabelMediumTracking,
                KoreanTypeScaleTokens.LabelMediumWeight
            ),
            labelSmall = createTextStyle(
                KoreanTypeScaleTokens.LabelSmallFont,
                KoreanTypeScaleTokens.LabelSmallSize,
                KoreanTypeScaleTokens.LabelSmallLineHeight,
                KoreanTypeScaleTokens.LabelSmallTracking,
                KoreanTypeScaleTokens.LabelSmallWeight
            )
        )
    } else {
        Typography()
    }
}
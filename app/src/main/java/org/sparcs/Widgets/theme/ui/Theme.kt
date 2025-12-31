package org.sparcs.Widgets.theme.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.glance.color.ColorProvider
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders
import androidx.glance.unit.ColorProvider
import org.sparcs.App.theme.ui.theme_dark_background
import org.sparcs.App.theme.ui.theme_dark_darkGray
import org.sparcs.App.theme.ui.theme_dark_gray64
import org.sparcs.App.theme.ui.theme_dark_grayBB
import org.sparcs.App.theme.ui.theme_dark_grayF8
import org.sparcs.App.theme.ui.theme_dark_lightGray0
import org.sparcs.App.theme.ui.theme_dark_onBackground
import org.sparcs.App.theme.ui.theme_dark_onPrimary
import org.sparcs.App.theme.ui.theme_dark_onSurface
import org.sparcs.App.theme.ui.theme_dark_outline
import org.sparcs.App.theme.ui.theme_dark_primary
import org.sparcs.App.theme.ui.theme_dark_secondary
import org.sparcs.App.theme.ui.theme_dark_surface
import org.sparcs.App.theme.ui.theme_dark_tertiary
import org.sparcs.App.theme.ui.theme_light_background
import org.sparcs.App.theme.ui.theme_light_darkGray
import org.sparcs.App.theme.ui.theme_light_gray64
import org.sparcs.App.theme.ui.theme_light_grayBB
import org.sparcs.App.theme.ui.theme_light_grayF8
import org.sparcs.App.theme.ui.theme_light_lightGray0
import org.sparcs.App.theme.ui.theme_light_onBackground
import org.sparcs.App.theme.ui.theme_light_onPrimary
import org.sparcs.App.theme.ui.theme_light_onSurface
import org.sparcs.App.theme.ui.theme_light_outline
import org.sparcs.App.theme.ui.theme_light_primary
import org.sparcs.App.theme.ui.theme_light_secondary
import org.sparcs.App.theme.ui.theme_light_surface
import org.sparcs.App.theme.ui.theme_light_tertiary

object TimetableWidgetTheme {
    val colors = ColorProviders(
        light = lightColorScheme(
            primary = theme_light_primary,
            secondary = theme_light_secondary,
            tertiary = theme_light_tertiary,
            background = theme_light_background,
            surface = theme_light_surface,
            onPrimary = theme_light_onPrimary,
            onBackground = theme_light_onBackground,
            onSurface = theme_light_onSurface,
            outline = theme_light_outline
        ),
        dark = darkColorScheme(
            primary = theme_dark_primary,
            secondary = theme_dark_secondary,
            tertiary = theme_dark_tertiary,
            background = theme_dark_background,
            surface = theme_dark_surface,
            onPrimary = theme_dark_onPrimary,
            onBackground = theme_dark_onBackground,
            onSurface = theme_dark_onSurface,
            outline = theme_dark_outline
        )
    )

    val lightGray0 = ColorProvider(day = theme_light_lightGray0, night = theme_dark_lightGray0)
    val gray64 = ColorProvider(day = theme_light_gray64, night = theme_dark_gray64)
    val grayBB = ColorProvider(day = theme_light_grayBB, night = theme_dark_grayBB)
    val grayF8 = ColorProvider(day = theme_light_grayF8, night = theme_dark_grayF8)
    val darkGray = ColorProvider(day = theme_light_darkGray, night = theme_dark_darkGray)

    val ColorProviders.lightGray0: ColorProvider
        get() = TimetableWidgetTheme.lightGray0

    val ColorProviders.gray64: ColorProvider
        get() = TimetableWidgetTheme.gray64

    val ColorProviders.grayBB: ColorProvider
        get() = TimetableWidgetTheme.grayBB

    val ColorProviders.grayF8: ColorProvider
        get() = TimetableWidgetTheme.grayF8

    val ColorProviders.darkGray: ColorProvider
        get() = TimetableWidgetTheme.darkGray
}
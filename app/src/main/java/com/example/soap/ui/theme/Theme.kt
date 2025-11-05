package com.example.soap.ui.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

private val DarkColorScheme = darkColorScheme(
    primary = theme_dark_primary,
    secondary = theme_dark_secondary,
    tertiary = theme_dark_tertiary,
    primaryContainer = theme_dark_primaryContainer,
    onPrimaryContainer = theme_dark_onPrimaryContainer,
    secondaryContainer = theme_dark_secondaryContainer,
    onSecondaryContainer = theme_dark_onSecondaryContainer,
    tertiaryContainer = theme_dark_tertiaryContainer,
    onTertiaryContainer = theme_dark_onTertiaryContainer,
    error = theme_dark_error,
    errorContainer = theme_dark_errorContainer,
    onError = theme_dark_onError,
    onErrorContainer = theme_dark_onErrorContainer,
    background = theme_dark_background,
    surface = theme_dark_surface,
    onPrimary = theme_dark_onPrimary,
    onSecondary = theme_dark_onSecondary,
    onTertiary = theme_dark_onTertiary,
    onBackground = theme_dark_onBackground,
    onSurface = theme_dark_onSurface,
    scrim = theme_dark_scrim,

    surfaceVariant = theme_dark_surfaceVariant,
    onSurfaceVariant = theme_dark_onSurfaceVariant,
    outline = theme_dark_outline,
    outlineVariant = theme_dark_outlineVariant,
    inversePrimary = theme_dark_inversePrimary,
    surfaceTint = theme_dark_surfaceTint,
    inverseSurface = theme_dark_inverseSurface,
    inverseOnSurface = theme_dark_inverseOnSurface,

    surfaceBright = theme_dark_surfaceBright,
    surfaceContainer = theme_dark_surfaceContainer,
    surfaceContainerHigh = theme_dark_surfaceContainerHigh,
    surfaceContainerHighest = theme_dark_surfaceContainerHighest,
    surfaceContainerLow = theme_dark_surfaceContainerLow,
    surfaceContainerLowest = theme_dark_surfaceContainerLowest,
    surfaceDim = theme_dark_surfaceDim,
)

private val LightColorScheme = lightColorScheme(
    primary = theme_light_primary,
    secondary = theme_light_secondary,
    tertiary = theme_light_tertiary,
    primaryContainer = theme_light_primaryContainer,
    onPrimaryContainer = theme_light_onPrimaryContainer,
    secondaryContainer = theme_light_secondaryContainer,
    onSecondaryContainer = theme_light_onSecondaryContainer,
    tertiaryContainer = theme_light_tertiaryContainer,
    onTertiaryContainer = theme_light_onTertiaryContainer,
    error = theme_light_error,
    errorContainer = theme_light_errorContainer,
    onError = theme_light_onError,
    onErrorContainer = theme_light_onErrorContainer,
    background = theme_light_background,
    surface = theme_light_surface,
    onPrimary = theme_light_onPrimary,
    onSecondary = theme_light_onSecondary,
    onTertiary = theme_light_onTertiary,
    onBackground = theme_light_onBackground,
    onSurface = theme_light_onSurface,
    scrim = theme_light_scrim,

    surfaceVariant = theme_light_surfaceVariant,
    onSurfaceVariant = theme_light_onSurfaceVariant,
    outline = theme_light_outline,
    outlineVariant = theme_light_outlineVariant,
    inversePrimary = theme_light_inversePrimary,
    surfaceTint = theme_light_surfaceTint,
    inverseSurface = theme_light_inverseSurface,
    inverseOnSurface = theme_light_inverseOnSurface,

    surfaceBright = theme_light_surfaceBright,
    surfaceContainer = theme_light_surfaceContainer,
    surfaceContainerHigh = theme_light_surfaceContainerHigh,
    surfaceContainerHighest = theme_light_surfaceContainerHighest,
    surfaceContainerLow = theme_light_surfaceContainerLow,
    surfaceContainerLowest = theme_light_surfaceContainerLowest,
    surfaceDim = theme_light_surfaceDim,
)

val ColorScheme.lightGray0: Color
    @Composable
    get() = if (isSystemInDarkTheme()) theme_dark_lightGray0 else theme_light_lightGray0

val ColorScheme.gray64: Color
    @Composable
    get() = if (isSystemInDarkTheme()) theme_dark_gray64 else theme_light_gray64

val ColorScheme.grayBB: Color
    @Composable
    get() = if (isSystemInDarkTheme()) theme_dark_grayBB else theme_light_grayBB

val ColorScheme.grayF8: Color
    @Composable
    get() = if (isSystemInDarkTheme()) theme_dark_grayF8 else theme_light_grayF8

val ColorScheme.darkGray: Color
    @Composable
    get() = if (isSystemInDarkTheme()) theme_dark_darkGray else theme_light_darkGray



@Composable
fun Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val background = colorScheme.background

        SideEffect {
            val window = (view.context as Activity).window
            window.setBackgroundDrawable(ColorDrawable(background.toArgb()))
        }

        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)

            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
            window.setBackgroundDrawable(ColorDrawable(colorScheme.background.toArgb()))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.navigationBarColor = if (darkTheme) {
                    android.graphics.Color.BLACK
                } else {
                    android.graphics.Color.WHITE
                }
            }
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
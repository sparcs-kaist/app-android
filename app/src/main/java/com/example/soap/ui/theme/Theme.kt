package com.example.soap.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

//이름은 light theme 기준
data class SoapCustomColors(
    val primary: Color,
    val primaryContainer: Color,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val gray0Border: Color,
    val gray64Button: Color,
    val darkGray: Color,
    val grayBB: Color,
    val grayf8: Color
)

val LocalSoapCustomColors = staticCompositionLocalOf {
    lightColors
}

val lightColors = SoapCustomColors(
    primary = theme_light_primary,
    primaryContainer = theme_light_primaryContainer,
    background = theme_light_background,
    surface = theme_light_surface,
    onSurface = theme_light_onSurface,
    gray0Border = theme_light_lightGray0,
    gray64Button = theme_light_gray64,
    grayBB = theme_light_grayBB,
    grayf8 = theme_light_grayf8,
    darkGray = Color.DarkGray
)

//수정 필요
val darkColors = SoapCustomColors(
    primary = theme_dark_primary,
    primaryContainer = theme_light_primaryContainer,
    background = theme_dark_background,
    surface = theme_dark_surface,
    onSurface = theme_dark_onSurface,
    gray0Border = Color(0xFF444444),
    gray64Button = Color(0xFF888888),
    grayBB = Color.Gray,
    grayf8 = Color.Gray,
    darkGray = Color.LightGray
)


private val DarkColorScheme = darkColorScheme()

private val LightColorScheme = lightColorScheme()


@Composable
fun SoapTheme(
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

    val customColors = if (darkTheme) darkColors else lightColors


    CompositionLocalProvider(LocalSoapCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

val MaterialTheme.soapColors: SoapCustomColors
    @Composable
    get() = LocalSoapCustomColors.current

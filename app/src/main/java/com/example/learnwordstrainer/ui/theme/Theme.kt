package com.example.learnwordstrainer.ui.theme

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = White, // Текст на primary кнопках
    onSecondary = White,
    onBackground = DarkTextPrimary, // Основний текст
    onSurface = DarkTextPrimary, // Текст на картках
    error = DarkError,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = White,
    onSecondary = White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    error = LightError,
    onError = White
)

@Composable
fun LearnWordsTrainerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: Int? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        themeMode != null -> when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> DarkColorScheme
            AppCompatDelegate.MODE_NIGHT_NO -> LightColorScheme
            else -> if (darkTheme) DarkColorScheme else LightColorScheme
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val isDark = when (themeMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> darkTheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
package com.example.probuild.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA1B49B),
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = Color(0xFF202124),
    onBackground = Color(0xFFF1F1ED),
    surface = Color(0xFF2B2D30),
    onSurface = Color(0xFFF1F1ED),
    surfaceVariant = Color(0xFF30343B),
    onSurfaceVariant = Color(0xFFB5B7B9),
    outline = Color(0xFF42464D),
    outlineVariant = Color(0xFF383C42),
    error = ErrorRed,
    onError = Color(0xFF370000)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = WarmWhite,
    onBackground = GraphiteGray,
    surface = PureWhite,
    onSurface = GraphiteGray,
    surfaceVariant = SageContainer,
    onSurfaceVariant = MutedGray,
    outline = SoftGray,
    outlineVariant = WarmStone,
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = ErrorContainer,
    onErrorContainer = ErrorRed
)

@Composable
fun ProBuildTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is set to false by default to enforce ProBuild's signature architect aesthetic
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

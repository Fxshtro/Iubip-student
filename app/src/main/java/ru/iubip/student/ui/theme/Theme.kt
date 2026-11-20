package ru.iubip.student.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Светлая и тёмная цветовые схемы приложения.
 *
 * Dynamic color намеренно НЕ используется: цвета фиксированные и заданы в [Color.kt],
 * чтобы оформление было одинаковым на любом устройстве. Акцент — #0F48AD.
 * Тень и градиенты не используются: разделение идёт за счёт фона и скруглений.
 */
private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    inversePrimary = LightPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = Color.Transparent,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = DarkPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = Color.Transparent,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
)

/**
 * Дополнительные токены, которых нет в Material-схеме.
 *
 * @param accentPill заливка активной «таблетки» (primary с прозрачностью ~12%)
 * @param divider тонкая линия-разделитель (например, верхняя граница нижней панели)
 * @param statusOk зелёный: данные только что загружены с сайта
 * @param statusWarning жёлтый: данные есть, но из кеша (сеть недоступна)
 */
data class IubipExtraColors(
    val accentPill: Color,
    val divider: Color,
    val statusOk: Color,
    val statusWarning: Color,
)

private val LightExtraColors = IubipExtraColors(
    accentPill = AccentPillLight,
    divider = LightOutline,
    statusOk = StatusOkLight,
    statusWarning = StatusWarningLight,
)

private val DarkExtraColors = IubipExtraColors(
    accentPill = AccentPillDark,
    divider = DarkOutline,
    statusOk = StatusOkDark,
    statusWarning = StatusWarningDark,
)

internal val LocalIubipColors = staticCompositionLocalOf { LightExtraColors }

/** Доступ к токенам приложения из любого Composable темы. */
object IubipTheme {
    val colors: IubipExtraColors
        @Composable get() = LocalIubipColors.current
}

/**
 * @param darkTheme следует системной теме телефона (переопределяется только для превью).
 */
@Composable
fun IubipAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalIubipColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}

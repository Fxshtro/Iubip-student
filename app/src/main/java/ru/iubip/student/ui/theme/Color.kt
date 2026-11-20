package ru.iubip.student.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------- Акцент
// Фирменный цвет вуза — #0F48AD (снят с сайта iubip.ru). Используется как primary.
internal val BrandBlue = Color(0xFF0F48AD)

// ---------------------------------------------------------------- Светлая схема
internal val LightPrimary = BrandBlue
internal val LightOnPrimary = Color(0xFFFFFFFF)
internal val LightPrimaryContainer = Color(0xFFDCE5F8)
internal val LightOnPrimaryContainer = Color(0xFF001A4A)
internal val LightSecondary = Color(0xFF4A5A78)
internal val LightOnSecondary = Color(0xFFFFFFFF)
internal val LightSecondaryContainer = Color(0xFFDDE4F2)
internal val LightOnSecondaryContainer = Color(0xFF0C1B33)
internal val LightTertiary = Color(0xFF4A5A78)
internal val LightOnTertiary = Color(0xFFFFFFFF)

/** Фон приложения — спокойный светлый, не серая подложка Material. */
internal val LightBackground = Color(0xFFF7F8FA)
internal val LightOnBackground = Color(0xFF14181F)

/** Карточки и панели — белые поверхности. */
internal val LightSurface = Color(0xFFFFFFFF)
internal val LightOnSurface = Color(0xFF14181F)
internal val LightSurfaceVariant = Color(0xFFEDEFF3)
internal val LightOnSurfaceVariant = Color(0xFF6B7280)
internal val LightOutline = Color(0xFFDCE0E6)
internal val LightOutlineVariant = Color(0xFFEDEFF3)

internal val LightError = Color(0xFFBA1A1A)
internal val LightOnError = Color(0xFFFFFFFF)
internal val LightErrorContainer = Color(0xFFFDE7E7)
internal val LightOnErrorContainer = Color(0xFF7A1414)

// ---------------------------------------------------------------- Тёмная схема
internal val DarkPrimary = Color(0xFF8FB0F0)
internal val DarkOnPrimary = Color(0xFF001B4E)
internal val DarkPrimaryContainer = Color(0xFF1C3B76)
internal val DarkOnPrimaryContainer = Color(0xFFDCE5F8)
internal val DarkSecondary = Color(0xFFB4BCD2)
internal val DarkOnSecondary = Color(0xFF1F2A3C)
internal val DarkSecondaryContainer = Color(0xFF2A3547)
internal val DarkOnSecondaryContainer = Color(0xFFDDE4F2)
internal val DarkTertiary = Color(0xFFB4BCD2)
internal val DarkOnTertiary = Color(0xFF1F2A3C)

/** Тёмный фон приложения. */
internal val DarkBackground = Color(0xFF101318)
internal val DarkOnBackground = Color(0xFFE8EAEE)

/** Карточки в тёмной теме чуть светлее фона. */
internal val DarkSurface = Color(0xFF1A1E25)
internal val DarkOnSurface = Color(0xFFE8EAEE)
internal val DarkSurfaceVariant = Color(0xFF232A33)
internal val DarkOnSurfaceVariant = Color(0xFF9AA3B0)
internal val DarkOutline = Color(0xFF2C333D)
internal val DarkOutlineVariant = Color(0xFF232A33)

internal val DarkError = Color(0xFFFFB4AB)
internal val DarkOnError = Color(0xFF690005)
internal val DarkErrorContainer = Color(0xFF4E1F1F)
internal val DarkOnErrorContainer = Color(0xFFFFDAD6)

// ---------------------------------------------------------------- Отдельные токены вне Material-схемы

/** Активная «таблетка» в ленте дней и нижней панели — accent с прозрачностью ~12%. */
internal val AccentPillLight = LightPrimary.copy(alpha = 0.12f)
internal val AccentPillDark = DarkPrimary.copy(alpha = 0.16f)

/** Зелёный индикатор «данные актуальны» (доступный контраст на светлом/тёмном фоне). */
internal val StatusOkLight = Color(0xFF1B7F4B)
internal val StatusOkDark = Color(0xFF66D69A)

/** Жёлтый индикатор «данные из кеша, обновить не удалось». */
internal val StatusWarningLight = Color(0xFFB26A00)
internal val StatusWarningDark = Color(0xFFFFC861)

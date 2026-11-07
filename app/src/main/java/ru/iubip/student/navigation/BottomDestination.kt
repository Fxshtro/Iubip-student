package ru.iubip.student.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import ru.iubip.student.R
import ru.iubip.student.ui.icons.PhosphorIcons

/**
 * Маршруты навигации приложения.
 */
object Routes {
    const val SCHEDULE = "schedule"
    const val PROFILE = "profile"
}

/**
 * Пункт нижней навигационной панели.
 *
 * @param route маршрут Navigation Compose
 * @param labelRes подпись (строка из resources)
 * @param icon контурная иконка (активное состояние)
 * @param iconSelected залитая иконка (неактивное состояние)
 */
enum class BottomDestination(
    val route: String,
    @StringRes val labelRes: Int,
    @StringRes val contentDescriptionRes: Int,
    val icon: ImageVector,
    val iconSelected: ImageVector,
) {
    SCHEDULE(
        route = Routes.SCHEDULE,
        labelRes = R.string.nav_schedule,
        contentDescriptionRes = R.string.cd_icon_schedule,
        icon = PhosphorIcons.Thin.CalendarDots,
        iconSelected = PhosphorIcons.Fill.CalendarDots,
    ),
    PROFILE(
        route = Routes.PROFILE,
        labelRes = R.string.nav_profile,
        contentDescriptionRes = R.string.cd_icon_profile,
        icon = PhosphorIcons.Thin.UserCircle,
        iconSelected = PhosphorIcons.Fill.UserCircle,
    ),
}

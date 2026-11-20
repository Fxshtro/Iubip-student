package ru.iubip.student.ui.icons

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import ru.iubip.student.R

/**
 * Иконки Phosphor (https://github.com/phosphor-icons/core, лицензия MIT).
 *
 * Исходные SVG лежат в `res/drawable/ic_phosphor_*.xml` как VectorDrawable
 * (viewport 256×256, `fillColor = #FF000000`), здесь они обёрнуты в [ImageVector],
 * чтобы их можно было передавать в `Icon(imageVector = ...)` и красить через `tint`,
 * ровно как `Icons.Outlined.*` из `material-icons-extended`. Внешних зависимостей нет.
 *
 * Доступные стили: `Thin` (тонкие линии, для неактивного пункта нижней панели),
 * `Regular` (базовый) и `Fill` (заполненный, для активного пункта).
 */
object PhosphorIcons {

    /** Тонкие контурные иконки — неактивное состояние нижней панели. */
    object Thin {
        val CalendarBlank: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_calendar_blank, "PhosphorCalendarBlankThin")
        }
        val User: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_user, "PhosphorUserThin")
        }
        val CalendarDots: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_calendar_dots, "PhosphorCalendarDotsThin")
        }
        val UserCircle: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_user_circle, "PhosphorUserCircleThin")
        }
    }

    /** Базовые контурные иконки Phosphor. */
    object Regular {
        val CalendarBlank: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_calendar_blank_regular, "PhosphorCalendarBlankRegular")
        }
        val User: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_user_regular, "PhosphorUserRegular")
        }
        val CalendarDots: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_calendar_dots_regular, "PhosphorCalendarDotsRegular")
        }
        val UserCircle: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_user_circle_regular, "PhosphorUserCircleRegular")
        }
        val ChalkboardTeacher: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_chalkboard_teacher, "PhosphorChalkboardTeacher")
        }
        val Door: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_door, "PhosphorDoor")
        }
        val Clock: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_clock, "PhosphorClock")
        }

        /** Стрелки перелистывания месяца в календаре («‹», «›»). */
        val CaretLeft: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_caret_left_regular, "PhosphorCaretLeftRegular")
        }
        val CaretRight: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_caret_right_regular, "PhosphorCaretRightRegular")
        }

        /** Маленькая стрелка вниз: индикатор как лист сворачивается вниз. */
        val CaretDown: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_caret_down_regular, "PhosphorCaretDownRegular")
        }

        /** Крестик — закрыть лист календаря. */
        val X: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_x_regular, "PhosphorXRegular")
        }

        /** Ползунки настроек — плавающая кнопка «пульт выбора группы». */
        val SlidersHorizontal: ImageVector by lazy {
            vector(
                R.drawable.ic_phosphor_sliders_horizontal_regular,
                "PhosphorSlidersHorizontalRegular",
            )
        }

        /** Индикаторы актуальности данных в шапке расписания. */
        val Check: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_check_regular, "PhosphorCheckRegular")
        }
        val WarningCircle: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_warning_circle_regular, "PhosphorWarningCircleRegular")
        }
        val CircleDashed: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_circle_dashed_regular, "PhosphorCircleDashedRegular")
        }
    }

    /** Заполненные иконки — активное состояние нижней панели. */
    object Fill {
        val CalendarBlank: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_calendar_blank_fill, "PhosphorCalendarBlankFill")
        }
        val User: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_user_fill, "PhosphorUserFill")
        }
        val CalendarDots: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_calendar_dots_fill, "PhosphorCalendarDotsFill")
        }
        val UserCircle: ImageVector by lazy {
            vector(R.drawable.ic_phosphor_user_circle_fill, "PhosphorUserCircleFill")
        }
    }

    /**
     * Собирает [ImageVector] из того же pathData, что и VectorDrawable в `res/drawable`.
     * Чёрный цвет заливки перекрашивается `Icon`-ом через `tint`.
     */
    private fun vector(@DrawableRes resId: Int, name: String): ImageVector =
        PhosphorPathData.byResId.getValue(resId).let { pathData ->
            ImageVector.Builder(
                name = name,
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = VIEWPORT_SIZE,
                viewportHeight = VIEWPORT_SIZE,
            )
                .addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black))
                .build()
        }

    private const val VIEWPORT_SIZE = 256f
}

/**
 * Единственный источник pathData для иконок Phosphor.
 *
 * Дублирует содержимое `res/drawable/ic_phosphor_*.xml`: VectorDrawable нужен для
 * painterResource и превью без Compose-сборки, [ImageVector] — для `Icon(imageVector=)`.
 * При добавлении новой иконки обновите обе стороны.
 */
private object PhosphorPathData {
    val byResId: Map<Int, String> = mapOf(
        R.drawable.ic_phosphor_calendar_blank to
            "M208,36H180V24a4,4,0,0,0-8,0V36H84V24a4,4,0,0,0-8,0V36H48A12,12,0,0,0,36,48V208a12,12,0,0,0,12,12H208a12,12,0,0,0,12-12V48A12,12,0,0,0,208,36ZM48,44H76V56a4,4,0,0,0,8,0V44h88V56a4,4,0,0,0,8,0V44h28a4,4,0,0,1,4,4V84H44V48A4,4,0,0,1,48,44ZM208,212H48a4,4,0,0,1-4-4V92H212V208A4,4,0,0,1,208,212Z",
        R.drawable.ic_phosphor_user to
            "M227.46,214c-16.52-28.56-43-48.06-73.68-55.09a68,68,0,1,0-51.56,0c-30.64,7-57.16,26.53-73.68,55.09a4,4,0,0,0,6.92,4C55,184.19,89.62,164,128,164s73,20.19,92.54,54a4,4,0,0,0,3.46,2,3.93,3.93,0,0,0,2-.54A4,4,0,0,0,227.46,214ZM68,96a60,60,0,1,1,60,60A60.07,60.07,0,0,1,68,96Z",
        R.drawable.ic_phosphor_calendar_blank_fill to
            "M208,32H184V24a8,8,0,0,0-16,0v8H88V24a8,8,0,0,0-16,0v8H48A16,16,0,0,0,32,48V208a16,16,0,0,0,16,16H208a16,16,0,0,0,16-16V48A16,16,0,0,0,208,32Zm0,48H48V48H72v8a8,8,0,0,0,16,0V48h80v8a8,8,0,0,0,16,0V48h24Z",
        R.drawable.ic_phosphor_user_fill to
            "M230.93,220a8,8,0,0,1-6.93,4H32a8,8,0,0,1-6.92-12c15.23-26.33,38.7-45.21,66.09-54.16a72,72,0,1,1,73.66,0c27.39,8.95,50.86,27.83,66.09,54.16A8,8,0,0,1,230.93,220Z",
        R.drawable.ic_phosphor_calendar_blank_regular to
            "M208,32H184V24a8,8,0,0,0-16,0v8H88V24a8,8,0,0,0-16,0v8H48A16,16,0,0,0,32,48V208a16,16,0,0,0,16,16H208a16,16,0,0,0,16-16V48A16,16,0,0,0,208,32ZM72,48v8a8,8,0,0,0,16,0V48h80v8a8,8,0,0,0,16,0V48h24V80H48V48ZM208,208H48V96H208V208Z",
        R.drawable.ic_phosphor_user_regular to
            "M230.92,212c-15.23-26.33-38.7-45.21-66.09-54.16a72,72,0,1,0-73.66,0C63.78,166.78,40.31,185.66,25.08,212a8,8,0,1,0,13.85,8c18.84-32.56,52.14-52,89.07-52s70.23,19.44,89.07,52a8,8,0,1,0,13.85-8ZM72,96a56,56,0,1,1,56,56A56.06,56.06,0,0,1,72,96Z",
        R.drawable.ic_phosphor_chalkboard_teacher to
            "M216,40H40A16,16,0,0,0,24,56V200a16,16,0,0,0,16,16H53.39a8,8,0,0,0,7.23-4.57,48,48,0,0,1,86.76,0,8,8,0,0,0,7.23,4.57H216a16,16,0,0,0,16-16V56A16,16,0,0,0,216,40ZM80,144a24,24,0,1,1,24,24A24,24,0,0,1,80,144Zm136,56H159.43a64.39,64.39,0,0,0-28.83-26.16,40,40,0,1,0-53.2,0A64.39,64.39,0,0,0,48.57,200H40V56H216ZM56,96V80a8,8,0,0,1,8-8H192a8,8,0,0,1,8,8v96a8,8,0,0,1-8,8H176a8,8,0,0,1,0-16h8V88H72v8a8,8,0,0,1-16,0Z",
        R.drawable.ic_phosphor_door to
            "M232,216H208V40a16,16,0,0,0-16-16H64A16,16,0,0,0,48,40V216H24a8,8,0,0,0,0,16H232a8,8,0,0,0,0-16ZM64,40H192V216H64Zm104,92a12,12,0,1,1-12-12A12,12,0,0,1,168,132Z",
        R.drawable.ic_phosphor_calendar_dots to
            "M208,36H180V24a4,4,0,0,0-8,0V36H84V24a4,4,0,0,0-8,0V36H48A12,12,0,0,0,36,48V208a12,12,0,0,0,12,12H208a12,12,0,0,0,12-12V48A12,12,0,0,0,208,36ZM48,44H76V56a4,4,0,0,0,8,0V44h88V56a4,4,0,0,0,8,0V44h28a4,4,0,0,1,4,4V84H44V48A4,4,0,0,1,48,44ZM208,212H48a4,4,0,0,1-4-4V92H212V208A4,4,0,0,1,208,212Zm-72-80a8,8,0,1,1-8-8A8,8,0,0,1,136,132Zm44,0a8,8,0,1,1-8-8A8,8,0,0,1,180,132ZM92,172a8,8,0,1,1-8-8A8,8,0,0,1,92,172Zm44,0a8,8,0,1,1-8-8A8,8,0,0,1,136,172Zm44,0a8,8,0,1,1-8-8A8,8,0,0,1,180,172Z",
        R.drawable.ic_phosphor_calendar_dots_regular to
            "M208,32H184V24a8,8,0,0,0-16,0v8H88V24a8,8,0,0,0-16,0v8H48A16,16,0,0,0,32,48V208a16,16,0,0,0,16,16H208a16,16,0,0,0,16-16V48A16,16,0,0,0,208,32ZM72,48v8a8,8,0,0,0,16,0V48h80v8a8,8,0,0,0,16,0V48h24V80H48V48ZM208,208H48V96H208V208Zm-68-76a12,12,0,1,1-12-12A12,12,0,0,1,140,132Zm44,0a12,12,0,1,1-12-12A12,12,0,0,1,184,132ZM96,172a12,12,0,1,1-12-12A12,12,0,0,1,96,172Zm44,0a12,12,0,1,1-12-12A12,12,0,0,1,140,172Zm44,0a12,12,0,1,1-12-12A12,12,0,0,1,184,172Z",
        R.drawable.ic_phosphor_calendar_dots_fill to
            "M208,32H184V24a8,8,0,0,0-16,0v8H88V24a8,8,0,0,0-16,0v8H48A16,16,0,0,0,32,48V208a16,16,0,0,0,16,16H208a16,16,0,0,0,16-16V48A16,16,0,0,0,208,32ZM84,184a12,12,0,1,1,12-12A12,12,0,0,1,84,184Zm44,0a12,12,0,1,1,12-12A12,12,0,0,1,128,184Zm0-40a12,12,0,1,1,12-12A12,12,0,0,1,128,144Zm44,40a12,12,0,1,1,12-12A12,12,0,0,1,172,184Zm0-40a12,12,0,1,1,12-12A12,12,0,0,1,172,144Zm36-64H48V48H72v8a8,8,0,0,0,16,0V48h80v8a8,8,0,0,0,16,0V48h24Z",
        R.drawable.ic_phosphor_user_circle to
            "M128,28A100,100,0,1,0,228,128,100.11,100.11,0,0,0,128,28ZM68.87,198.42a68,68,0,0,1,118.26,0,91.8,91.8,0,0,1-118.26,0Zm124.3-5.55a75.61,75.61,0,0,0-44.51-34,44,44,0,1,0-41.32,0,75.61,75.61,0,0,0-44.51,34,92,92,0,1,1,130.34,0ZM128,156a36,36,0,1,1,36-36A36,36,0,0,1,128,156Z",
        R.drawable.ic_phosphor_user_circle_regular to
            "M128,24A104,104,0,1,0,232,128,104.11,104.11,0,0,0,128,24ZM74.08,197.5a64,64,0,0,1,107.84,0,87.83,87.83,0,0,1-107.84,0ZM96,120a32,32,0,1,1,32,32A32,32,0,0,1,96,120Zm97.76,66.41a79.66,79.66,0,0,0-36.06-28.75,48,48,0,1,0-59.4,0,79.66,79.66,0,0,0-36.06,28.75,88,88,0,1,1,131.52,0Z",
        R.drawable.ic_phosphor_user_circle_fill to
            "M172,120a44,44,0,1,1-44-44A44.05,44.05,0,0,1,172,120Zm60,8A104,104,0,1,1,128,24,104.11,104.11,0,0,1,232,128Zm-16,0a88.09,88.09,0,0,0-91.47-87.93C77.43,41.89,39.87,81.12,40,128.25a87.65,87.65,0,0,0,22.24,58.16A79.71,79.71,0,0,1,84,165.1a4,4,0,0,1,4.83.32,59.83,59.83,0,0,0,78.28,0,4,4,0,0,1,4.83-.32,79.71,79.71,0,0,1,21.79,21.31A87.62,87.62,0,0,0,216,128Z",
        R.drawable.ic_phosphor_clock to
            "M128,24A104,104,0,1,0,232,128,104.11,104.11,0,0,0,128,24Zm0,192a88,88,0,1,1,88-88A88.1,88.1,0,0,1,128,216Zm64-88a8,8,0,0,1-8,8H128a8,8,0,0,1-8-8V72a8,8,0,0,1,16,0v48h48A8,8,0,0,1,192,128Z",
        R.drawable.ic_phosphor_caret_left_regular to
            "M165.66,202.34a8,8,0,0,1-11.32,11.32l-80-80a8,8,0,0,1,0-11.32l80-80a8,8,0,0,1,11.32,11.32L91.31,128Z",
        R.drawable.ic_phosphor_caret_right_regular to
            "M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z",
        R.drawable.ic_phosphor_caret_down_regular to
            "M213.66,101.66l-80,80a8,8,0,0,1-11.32,0l-80-80A8,8,0,0,1,53.66,90.34L128,164.69l74.34-74.35a8,8,0,0,1,11.32,11.32Z",
        R.drawable.ic_phosphor_x_regular to
            "M205.66,194.34a8,8,0,0,1-11.32,11.32L128,139.31,61.66,205.66a8,8,0,0,1-11.32-11.32L116.69,128,50.34,61.66A8,8,0,0,1,61.66,50.34L128,116.69l66.34-66.35a8,8,0,0,1,11.32,11.32L139.31,128Z",
        R.drawable.ic_phosphor_sliders_horizontal_regular to
            "M40,88H73a32,32,0,0,0,62,0h81a8,8,0,0,0,0-16H135a32,32,0,0,0-62,0H40a8,8,0,0,0,0,16Zm64-24A16,16,0,1,1,88,80,16,16,0,0,1,104,64ZM216,168H199a32,32,0,0,0-62,0H40a8,8,0,0,0,0,16h97a32,32,0,0,0,62,0h17a8,8,0,0,0,0-16Zm-48,24a16,16,0,1,1,16-16A16,16,0,0,1,168,192Z",
        R.drawable.ic_phosphor_check_regular to
            "M229.66,77.66l-128,128a8,8,0,0,1-11.32,0l-56-56a8,8,0,0,1,11.32-11.32L96,188.69,218.34,66.34a8,8,0,0,1,11.32,11.32Z",
        R.drawable.ic_phosphor_warning_circle_regular to
            "M128,24A104,104,0,1,0,232,128,104.11,104.11,0,0,0,128,24Zm0,192a88,88,0,1,1,88-88A88.1,88.1,0,0,1,128,216Zm-8-80V80a8,8,0,0,1,16,0v56a8,8,0,0,1-16,0Zm20,36a12,12,0,1,1-12-12A12,12,0,0,1,140,172Z",
        R.drawable.ic_phosphor_circle_dashed_regular to
            "M96.26,37.05A8,8,0,0,1,102,27.29a104.11,104.11,0,0,1,52,0,8,8,0,0,1-2,15.75,8.15,8.15,0,0,1-2-.26,88.09,88.09,0,0,0-44,0A8,8,0,0,1,96.26,37.05ZM53.79,55.14a104.05,104.05,0,0,0-26,45,8,8,0,0,0,15.42,4.27,88,88,0,0,1,22-38.09A8,8,0,0,0,53.79,55.14ZM43.21,151.55a8,8,0,1,0-15.42,4.28,104.12,104.12,0,0,0,26,45,8,8,0,0,0,11.41-11.22A88.14,88.14,0,0,1,43.21,151.55ZM150,213.22a88,88,0,0,1-44,0,8,8,0,1,0-4,15.49,104.11,104.11,0,0,0,52,0,8,8,0,0,0-4-15.49ZM222.65,146a8,8,0,0,0-9.85,5.58,87.91,87.91,0,0,1-22,38.08,8,8,0,1,0,11.42,11.21,104,104,0,0,0,26-45A8,8,0,0,0,222.65,146Zm-9.86-41.54a8,8,0,0,0,15.42-4.28,104,104,0,0,0-26-45,8,8,0,1,0-11.41,11.22A88,88,0,0,1,212.79,104.45Z",
    )
}

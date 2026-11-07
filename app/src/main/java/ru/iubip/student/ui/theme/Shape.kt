package ru.iubip.student.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Дизайн-токены скруглений.
 *
 * Крупные радиусы: карточки 18 dp, панели и группы 20 dp, кнопки-таблетки — в полную высоту
 * ([Pill] с 50% от размера через [RoundedCornerShape.Percent]).
 */
object IubipShapes {
    /** Радиус карточки пары и прочих карточек списка. */
    val Card = RoundedCornerShape(18.dp)

    /** Радиус крупных панелей и блоков. */
    val Panel = RoundedCornerShape(20.dp)

    /** Малый радиус: чипы, иконки-квадраты. */
    val Small = RoundedCornerShape(12.dp)

    /** Таблетка: кнопки и активные пункты в полную высоту. */
    val Pill = RoundedCornerShape(percent = 50)
}

/** Карта форм Material 3 на основе токенов [IubipShapes]. */
internal val Shapes = Shapes(
    extraSmall = IubipShapes.Small,
    small = IubipShapes.Small,
    medium = RoundedCornerShape(16.dp),
    large = IubipShapes.Card,
    extraLarge = IubipShapes.Panel,
)

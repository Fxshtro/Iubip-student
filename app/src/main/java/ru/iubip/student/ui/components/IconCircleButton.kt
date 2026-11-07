package ru.iubip.student.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Круглая иконка-кнопка для действий в шапке экрана и в панелях.
 *
 * Фон прозрачный (показывается только иконка, без заливки и тени), размер клика — 48 dp,
 * иконка — 24 dp, цвет — `onSurfaceVariant` или `primary`. Стандартная квадратная рябь
 * Material отключена: клик идёт через `clickable(interactionSource = null, indication = null)`,
 * ровно как в нижней панели и чипах дней.
 *
 * @param icon векторная иконка (Phosphor regular)
 * @param contentDescription подпись для доступности — обязательна, кнопка без текста
 * @param onClick действие по нажатию
 * @param tint цвет иконки, по умолчанию `onSurfaceVariant`
 */
@Composable
fun IconCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconSize: Dp = 24.dp,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

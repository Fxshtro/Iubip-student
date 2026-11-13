package ru.iubip.student.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.iubip.student.R
import ru.iubip.student.ui.icons.PhosphorIcons

/**
 * Круглая заглушка аватарки: светлый круг с тонким контуром и контурным силуэтом.
 *
 * Силуэт намеренно не залитый и некрупный: заглушка не должна перетягивать внимание
 * с ФИО. Цвета — только из темы: фон `surface`, тонкий контур `outlineVariant`,
 * силуэт `onSurfaceVariant` с прозрачностью 45% (`outline` для этого не годится —
 * в светлой схеме он почти белый, `#DCE0E6`).
 *
 * @param size диаметр круга, по умолчанию 96 dp
 */
@Composable
fun ProfileAvatarPlaceholder(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = PhosphorIcons.Thin.User,
            contentDescription = stringResource(R.string.cd_profile_avatar),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            // Контурный силуэт чуть меньше половины круга: спокойная заглушка,
            // а не «жирное пятно» по центру экрана.
            modifier = Modifier.size(size * 0.46f),
        )
    }
}

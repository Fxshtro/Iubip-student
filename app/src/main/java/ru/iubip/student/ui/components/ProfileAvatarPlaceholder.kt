package ru.iubip.student.ui.components

import androidx.compose.foundation.background
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
 * Круглая заглушка аватарки (как в Moodle): серый круг с силуэтом человека.
 *
 * Цвета берутся только из темы: фон — `surfaceVariant`, силуэт — `onSurfaceVariant`,
 * поэтому в светлой и тёмной теме заглушка остаётся нейтральной.
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
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = PhosphorIcons.Fill.User,
            contentDescription = stringResource(R.string.cd_profile_avatar),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            // Силуэт занимает примерно половину круга, как заглушка Moodle.
            modifier = Modifier.size(size * 0.56f),
        )
    }
}

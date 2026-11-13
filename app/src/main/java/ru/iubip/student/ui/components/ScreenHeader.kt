package ru.iubip.student.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Шапка экрана: крупный полужирный заголовок, под ним второстепенная подпись.
 *
 * Если передан [action], заголовок и действие стоят в одной строке: заголовок занимает
 * всю доступную ширину (длинный обрезается многоточием), действие прижато к правому краю
 * и выровнено по заголовку. Без [action] шапка выглядит как раньше (например, профиль).
 *
 * @param title заголовок (например, «К3И1(9)» или «О себе»)
 * @param subtitle подпись под заголовком; null — подписи нет
 * @param maxSubtitleLines максимальное число строк подписи
 * @param action необязательное действие справа от заголовка (кнопка календаря на «Расписании»)
 * @param inlineSubtitle подпись справа от заголовка, в той же строке (горизонталь: дата
 *   вынесена вправо, чтобы не занимать высоту отдельной строкой). Если задана,
 *   [subtitle] не рисуется.
 * @param compact плотная шапка для горизонтали/планшета: тот же текст, меньше вертикальных
 *   отступов (`top` 4 dp вместо 12 dp, `bottom` 8 dp вместо 16 dp, зазор 2 dp вместо 6 dp) —
 *   освобождается место под список пар и колонку календаря.
 * @param topGap верхний зазор шапки в dp. В портрете передаётся увеличенный отступ от
 *   системной строки (16 dp), чтобы заголовок не прилипал к часам/статус-бару;
 *   в горизонтали остаётся компактный (4 dp) — там дорога каждая строка по высоте.
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    maxSubtitleLines: Int = 2,
    action: (@Composable () -> Unit)? = null,
    compact: Boolean = false,
    inlineSubtitle: String? = null,
    topGap: Dp? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = HEADER_SIDE_PADDING,
                end = HEADER_SIDE_PADDING,
                top = topGap ?: if (compact) 4.dp else 12.dp,
                bottom = if (compact) 8.dp else 16.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (inlineSubtitle != null) {
                Text(
                    text = inlineSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            action?.invoke()
        }
        if (subtitle != null && inlineSubtitle == null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = maxSubtitleLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Горизонтальный отступ шапки — как у контента экрана. */
private val HEADER_SIDE_PADDING = 20.dp

/**
 * Строка «иконка + текст» для метаданных карточки (преподаватель, аудитория).
 * Иконка контурная и маленькая, цвета [MaterialTheme.colorScheme.onSurfaceVariant].
 *
 * @param contentDescription описание иконки для доступности
 */
@Composable
fun MetaRow(
    icon: ImageVector,
    text: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

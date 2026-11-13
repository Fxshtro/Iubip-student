package ru.iubip.student.ui.screens.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ru.iubip.student.R
import ru.iubip.student.data.model.ClassPair
import ru.iubip.student.ui.components.MetaRow
import ru.iubip.student.ui.theme.IubipShapes
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Формат времени в карточке: «08:30». */
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Карточка одной пары.
 *
 * Сверху время «08:30–10:05» и справа «1-я пара» второстепенным цветом,
 * затем тип занятия «лек.» / «пр.», полужирный предмет, ниже преподаватель и аудитория
 * с маленькими контурными иконками. Без тени и цветной пометки типа занятия.
 *
 * В плотном режиме ([compact]) уменьшены только внутренние вертикальные отступы
 * (12 dp вместо 14 dp, зазор 4 dp вместо 6 dp) — на горизонтальном телефоне помещается
 * заметно больше пар. Порядок и иерархия строк не меняются.
 *
 * @param pair пара для отображения
 * @param compact плотная карточка для горизонтали/планшета
 */
@Composable
fun PairCard(
    pair: ClassPair,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    val timeRange = formatRange(pair.start, pair.end)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = IubipShapes.Card,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = if (compact) 12.dp else 14.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = timeRange,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.schedule_pair_number, pair.slot),
                    style = MaterialTheme.typography.bodySmall,
                    color = secondary,
                )
            }

            Text(
                text = buildAnnotatedString {
                    if (pair.type.isNotBlank()) {
                        withStyle(SpanStyle(color = secondary)) {
                            append(pair.type)
                            append("  ")
                        }
                    }
                    append(pair.subject)
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (pair.teacher.isNotBlank()) {
                MetaRow(
                    icon = Icons.Outlined.Person,
                    text = pair.teacher,
                    contentDescription = stringResource(R.string.cd_pair_teacher),
                )
            }
            if (pair.room.isNotBlank()) {
                MetaRow(
                    icon = Icons.Outlined.MeetingRoom,
                    text = pair.room,
                    contentDescription = stringResource(R.string.cd_pair_room),
                )
            }
        }
    }
}

/** Диапазон времени пары «08:30–10:05» (тире — U+2013). */
internal fun formatRange(start: LocalTime, end: LocalTime): String =
    "${start.format(TIME_FORMAT)}–${end.format(TIME_FORMAT)}"

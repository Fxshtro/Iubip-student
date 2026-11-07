package ru.iubip.student.ui.screens.schedule.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.iubip.student.R
import ru.iubip.student.data.DayAvailability
import ru.iubip.student.ui.screens.schedule.shortNameRes
import ru.iubip.student.ui.theme.IubipShapes
import java.time.LocalDate
import java.time.YearMonth

/** Высота строки сетки: шесть строк + заголовок листа влезают на экран телефона. */
private val ROW_HEIGHT = 42.dp

/** Диаметр круглой отметки дня внутри строки сетки. */
private val DAY_CIRCLE = 34.dp

/** Круглая стрелка месяца внутри 48 dp зоны нажатия. */
private val ARROW_CIRCLE = 40.dp

/** Непрозрачность цифры недоступного дня. */
internal const val UNAVAILABLE_NUMBER_ALPHA = 0.6f

/** Непрозрачность цифры доступного дня без пар. */
internal const val EMPTY_DAY_NUMBER_ALPHA = 0.75f

/** Непрозрачность числа дня соседнего месяца. */
internal const val OUT_OF_MONTH_ALPHA = 0.5f

/** Приглушённая иконка выключенной стрелки. */
private const val DISABLED_ALPHA = 0.38f

/** Толщина обводки сегодняшнего дня. */
private val TODAY_STROKE = 1.dp

/**
 * Стрелка перелистывания месяца «‹» / «›»: круглая кнопка без ряби.
 * На первой/последней странице стрелка выключена и для доступности помечена как disabled.
 *
 * @param icon иконка-стрелка Phosphor
 * @param contentDescription «Предыдущий месяц» / «Следующий месяц»
 * @param enabled есть ли соседний месяц с данными
 * @param onClick переход к соседнему месяцу
 */
@Composable
internal fun MonthArrowButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .width(48.dp)
            .clip(IubipShapes.Pill)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    Modifier.semantics { disabled() }
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(ARROW_CIRCLE)
                .clip(IubipShapes.Pill)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (enabled) 1f else DISABLED_ALPHA,
                ),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/** Строка дней недели «Пн Вт Ср Чт Пт Сб Вс» над сеткой. */
@Composable
internal fun MonthWeekdayHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        CalendarDays.ORDERED.forEach { day ->
            Text(
                text = stringResource(day.shortNameRes()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Одна ячейка месяца.
 *
 * Дни соседних месяцев ([outOfMonth]) попадают в сетку только как хвост недели,
 * пересекающей месяц. Они выглядят как обычные дни (не серые плашки), но не нажимаются:
 * на своей странице они доступны как настоящие дни месяца.
 *
 * @param date дата ячейки
 * @param state доступность даты по правилам [ru.iubip.student.data.CalendarAvailability]
 * @param selected дата выбрана
 * @param isToday дата сегодня
 * @param outOfMonth дата относится к соседнему месяцу
 * @param label подпись дня для экранного диктора: «11 ноября»
 * @param onClick выбор даты (не вызывается для [DayAvailability.UNAVAILABLE])
 */
@Composable
internal fun DayCell(
    date: LocalDate,
    state: DayAvailability,
    selected: Boolean,
    isToday: Boolean,
    outOfMonth: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val clickable = !outOfMonth && state.isSelectable
    val background = when {
        selected -> colors.primary
        // «Монолитная серая» плашка для дат, которых нет в выложенном расписании.
        state == DayAvailability.UNAVAILABLE -> colors.surfaceVariant
        else -> Color.Transparent
    }
    val border = when {
        selected -> null
        isToday -> colors.primary
        else -> null
    }
    val numberColor = when {
        selected -> colors.onPrimary
        state == DayAvailability.UNAVAILABLE -> colors.onSurfaceVariant.copy(alpha = UNAVAILABLE_NUMBER_ALPHA)
        state == DayAvailability.AVAILABLE_EMPTY -> colors.onSurface.copy(alpha = EMPTY_DAY_NUMBER_ALPHA)
        // День соседнего месяца: доступен в своём месяце, здесь только как хвост недели.
        outOfMonth -> colors.onSurface.copy(alpha = OUT_OF_MONTH_ALPHA)
        else -> colors.onSurface
    }
    val description = stringResource(
        when {
            outOfMonth -> R.string.cd_day_cell_other_month
            state == DayAvailability.UNAVAILABLE -> R.string.cd_day_cell_unavailable
            state == DayAvailability.AVAILABLE_EMPTY -> R.string.cd_day_cell_empty
            else -> R.string.cd_day_cell
        },
        CalendarDays.description(date, label),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ROW_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(DAY_CIRCLE)
                .clip(CircleShape)
                .background(background)
                .then(
                    if (border != null) Modifier.border(TODAY_STROKE, border, CircleShape) else Modifier,
                )
                .then(
                    if (clickable) {
                        Modifier.clickable(
                            interactionSource = null,
                            indication = null,
                            role = Role.Button,
                            onClick = onClick,
                        )
                    } else {
                        Modifier
                    },
                )
                .semantics {
                    contentDescription = description
                    this.selected = selected
                    if (!clickable) disabled()
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = numberColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Одна строка сетки: семь дней недели Пн..Вс, неделя целиком.
 *
 * @param dates даты строки по порядку Пн..Вс
 * @param inMonth подписи дней месяца на странице: дата → «11 ноября»
 * @param month месяц страницы — для дней соседних месяцев
 * @param stateOf доступность даты
 * @param selectedDate выбранный день
 * @param today сегодня
 * @param onDateSelected выбор дня
 */
@Composable
internal fun WeekRow(
    dates: List<LocalDate>,
    month: YearMonth,
    inMonth: Map<LocalDate, String>,
    stateOf: (LocalDate) -> DayAvailability,
    selectedDate: LocalDate?,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        dates.forEach { date ->
            Box(modifier = Modifier.weight(1f)) {
                DayCell(
                    date = date,
                    state = stateOf(date),
                    selected = date == selectedDate,
                    isToday = date == today,
                    outOfMonth = YearMonth.from(date) != month,
                    label = inMonth[date] ?: "${date.dayOfMonth} ${monthGenitive(date.monthValue)}",
                    onClick = { onDateSelected(date) },
                )
            }
        }
    }
}

/** Пояснение к серым плашкам под сеткой. */
@Composable
internal fun CalendarLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(IubipShapes.Pill)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.calendar_legend_unavailable),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Текст-заглушка по центру листа: загрузка, ошибка или пустые данные. */
@Composable
internal fun CalendarStateBlock(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

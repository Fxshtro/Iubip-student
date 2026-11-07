package ru.iubip.student.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.iubip.student.R
import ru.iubip.student.data.model.Day
import ru.iubip.student.data.source.WeekSchedule
import ru.iubip.student.ui.theme.IubipShapes
import ru.iubip.student.ui.theme.IubipTheme
import java.time.DayOfWeek
import java.time.LocalDate

/** Расстояние между ячейками ленты. */
private val DAY_CELL_GAP = 4.dp

/** Горизонтальный отступ ленты — меньше экранного (20 dp), чтобы 7 дней влезли. */
private val STRIP_SIDE_PADDING = 12.dp

/**
 * Лента дней недели, листаемая неделями.
 *
 * Показывается ровно одна неделя Пн..Вс. Свайп по ленте переключает неделю целиком
 * (одна прокрутка = одна неделя, а не свободная прокрутка по дням): страницы пейджера —
 * выложенные недели из `ScheduleTable.weeks`. На первой и последней неделе свайп наружу
 * упирается — лента не листается в пустую серую неделю.
 *
 * Внутри недели свободной прокрутки нет: семь ячеек делят ширину поровну, поэтому лента
 * не «ездит» по пикселям и не уходит за край даже при крупном системном шрифте.
 *
 * **Один источник истины.** Какая неделя активна и какой день выбран — знает только
 * `ScheduleViewModel`. Лента только показывает `weekIndex` и сообщает ОДНО событие жеста
 * (свайп завершён на другой странице); программная прокрутка из состояния ничего обратно
 * не отправляет. Поэтому выбор даты в календаре не может «откатиться».
 *
 * @param weeks выложенные недели — страницы ленты
 * @param weekIndex показанная неделя
 * @param selectedDate выбранный день
 * @param today сегодня (точка-подсказка под числом)
 * @param onUserSwipeNext пользователь свайпнул влево и отпустил на следующей неделе
 * @param onUserSwipePrevious пользователь свайпнул вправо и отпустил на предыдущей неделе
 * @param onDateClick выбор дня
 */
@Composable
fun WeekDayStrip(
    weeks: List<WeekSchedule>,
    weekIndex: Int,
    selectedDate: LocalDate?,
    today: LocalDate,
    onUserSwipeNext: () -> Unit,
    onUserSwipePrevious: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (weeks.isEmpty()) return

    // Пейджер создаётся под уже загруженные недели и живёт, пока этот набор недель тот же:
    // после повторной загрузки лента собирается заново с актуальной неделей (неделя —
    // единственный источник истины, он в ViewModel).
    key(weeks.first().start) {
        WeekPager(
            weeks = weeks,
            weekIndex = weekIndex,
            selectedDate = selectedDate,
            today = today,
            onUserSwipeNext = onUserSwipeNext,
            onUserSwipePrevious = onUserSwipePrevious,
            onDateClick = onDateClick,
            modifier = modifier,
        )
    }
}

/**
 * Пейджер недель.
 *
 * Данные о текущей неделе живут только в [ScheduleUiState]: лента их НЕ отдаёт обратно.
 * Пользовательский свайп сообщается одним событием — когда жест завершён и пейджер
 * «осел» на другой странице ([PagerState.settledPage]); промежуточные кадры и
 * программную прокрутку лента не озвучивает.
 */
@Composable
private fun WeekPager(
    weeks: List<WeekSchedule>,
    weekIndex: Int,
    selectedDate: LocalDate?,
    today: LocalDate,
    onUserSwipeNext: () -> Unit,
    onUserSwipePrevious: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(
        initialPage = weekIndex.coerceIn(0, weeks.lastIndex),
        pageCount = { weeks.size },
    )
    val currentSwipeNext by rememberUpdatedState(onUserSwipeNext)
    val currentSwipePrevious by rememberUpdatedState(onUserSwipePrevious)
    // Последняя неделя, о которой уже сообщили жестом.
    val lastReported = remember { mutableStateOf(pagerState.currentPage) }

    // Только явный жест пользователя: скролл закончился и страница сменилась.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage to pagerState.isScrollInProgress }
            .collect { (settled, scrolling) ->
                if (scrolling || settled == lastReported.value) return@collect
                if (settled > lastReported.value) currentSwipeNext() else currentSwipePrevious()
                lastReported.value = settled
            }
    }

    // Экран переключил неделю программно (календарь, повторная загрузка) — двигаем ленту.
    // Обратного сообщения отсюда нет: это односторонняя синхронизация из состояния.
    LaunchedEffect(weekIndex) {
        if (weekIndex in weeks.indices && pagerState.currentPage != weekIndex) {
            pagerState.animateScrollToPage(weekIndex)
            lastReported.value = weekIndex
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        beyondViewportPageCount = 1,
        userScrollEnabled = weeks.size > 1,
    ) { page ->
        WeekPage(
            week = weeks[page],
            selectedDate = if (page == pagerState.currentPage) selectedDate else null,
            today = today,
            onDateClick = onDateClick,
        )
    }
}

/**
 * Страница ленты — одна неделя Пн..Вс. Свайп по странице отдан пейджеру (смена недели),
 * внутренней прокрутки по дням нет: ячейки делят ширину поровну.
 */
@Composable
private fun WeekPage(
    week: WeekSchedule,
    selectedDate: LocalDate?,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = STRIP_SIDE_PADDING),
        horizontalArrangement = Arrangement.spacedBy(DAY_CELL_GAP),
    ) {
        week.days.forEach { day ->
            Box(modifier = Modifier.weight(1f)) {
                WeekDayCell(
                    day = day,
                    selected = day.date == selectedDate,
                    isToday = day.date == today,
                    onClick = { onDateClick(day.date) },
                )
            }
        }
    }
}

/** Один день в ленте: «Пн» и число месяца; выбранный — акцентная таблетка. */
@Composable
private fun WeekDayCell(
    day: Day,
    selected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val contentColor = if (selected) colors.primary else colors.onSurfaceVariant
    val description = stringResource(
        R.string.cd_day_chip,
        stringResource(day.date.dayOfWeek.fullNameRes()),
        day.date.dayOfMonth,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(IubipShapes.Panel)
            .background(if (selected) IubipTheme.colors.accentPill else Color.Transparent)
            // Своя подложка-плашка вместо стандартной прямоугольной ряби Material.
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            // Один узел для экранного диктора: подпись «Сб, 7» и признак «выбран»
            // должны попадать в тот же узел, что и нажатие (иначе TalkBack видит
            // немого соседа текста вместо выбранной вкладки).
            .semantics(mergeDescendants = true) {
                this.selected = selected
                contentDescription = description
            }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(day.date.dayOfWeek.shortNameRes()),
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) colors.primary else colors.onSurface,
            textAlign = TextAlign.Center,
        )
        // Точка «сегодня»: тонкая подсказка, не конкурирует с выбранным днём.
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(IubipShapes.Pill)
                .background(if (isToday) colors.primary else Color.Transparent),
        )
    }
}

/** Сокращение дня недели для ленты: Пн…Вс. */
internal fun DayOfWeek.shortNameRes(): Int = when (this) {
    DayOfWeek.MONDAY -> R.string.weekday_short_mon
    DayOfWeek.TUESDAY -> R.string.weekday_short_tue
    DayOfWeek.WEDNESDAY -> R.string.weekday_short_wed
    DayOfWeek.THURSDAY -> R.string.weekday_short_thu
    DayOfWeek.FRIDAY -> R.string.weekday_short_fri
    DayOfWeek.SATURDAY -> R.string.weekday_short_sat
    DayOfWeek.SUNDAY -> R.string.weekday_short_sun
}

/** Полное название дня недели для подписи в шапке. */
internal fun DayOfWeek.fullNameRes(): Int = when (this) {
    DayOfWeek.MONDAY -> R.string.weekday_full_mon
    DayOfWeek.TUESDAY -> R.string.weekday_full_tue
    DayOfWeek.WEDNESDAY -> R.string.weekday_full_wed
    DayOfWeek.THURSDAY -> R.string.weekday_full_thu
    DayOfWeek.FRIDAY -> R.string.weekday_full_fri
    DayOfWeek.SATURDAY -> R.string.weekday_full_sat
    DayOfWeek.SUNDAY -> R.string.weekday_full_sun
}

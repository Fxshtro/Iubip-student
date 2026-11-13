package ru.iubip.student.ui.screens.schedule.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.iubip.student.R
import ru.iubip.student.ui.icons.PhosphorIcons
import java.time.LocalDate

/**
 * Сетка календаря: заголовок месяца «‹ Ноябрь 2026 ›», подписи дней недели, пейджер
 * месяцев и легенда. Общая часть листа ([CalendarSheetContent]) и постоянной колонки
 * календаря ([CalendarColumn]) — сетка рисуется ОДИН раз, раскладка её не дублирует.
 *
 * Перелистывание месяцев — свайп по сетке или стрелки (границы упираются на первом
 * и последнем месяце с данными). Тап по доступной дате выбирает её; недоступные даты
 * (серые плашки) не нажимаются.
 *
 * @param model модель календаря (страницы месяцев, выбранный день, сегодня)
 * @param onDateSelected выбор доступной даты
 */
@Composable
internal fun CalendarGrid(
    model: CalendarScreenModel,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = model.startIndex.coerceIn(0, model.pages.lastIndex),
        pageCount = { model.pages.size },
    )

    Column(modifier = modifier.fillMaxWidth()) {
        MonthHeader(
            title = model.pages[pagerState.currentPage].title,
            canGoPrevious = pagerState.currentPage > 0,
            canGoNext = pagerState.currentPage < model.pages.lastIndex,
            onPrevious = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
            },
            onNext = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
        )
        Spacer(Modifier.height(8.dp))

        MonthWeekdayHeader()
        Spacer(Modifier.height(4.dp))

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
        ) { pageIndex ->
            val page = model.pages[pageIndex]
            Column(modifier = Modifier.fillMaxWidth()) {
                page.cells.forEach { dates ->
                    WeekRow(
                        dates = dates,
                        month = page.month,
                        inMonth = page.inMonth,
                        stateOf = { date -> dayState(date, model.table) },
                        selectedDate = model.selectedDate,
                        today = model.today,
                        onDateSelected = onDateSelected,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        CalendarLegend()
    }
}

/** «‹ Ноябрь 2026 ›» — заголовок месяца, по центру, стрелки по краям. */
@Composable
private fun MonthHeader(
    title: String,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonthArrowButton(
            icon = PhosphorIcons.Regular.CaretLeft,
            contentDescription = stringResource(R.string.cd_calendar_previous_month),
            enabled = canGoPrevious,
            onClick = onPrevious,
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        MonthArrowButton(
            icon = PhosphorIcons.Regular.CaretRight,
            contentDescription = stringResource(R.string.cd_calendar_next_month),
            enabled = canGoNext,
            onClick = onNext,
        )
    }
}

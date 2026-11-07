package ru.iubip.student.ui.screens.schedule.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import ru.iubip.student.data.DayAvailability
import ru.iubip.student.data.source.ScheduleTable
import ru.iubip.student.ui.components.IconCircleButton
import ru.iubip.student.ui.icons.PhosphorIcons
import java.time.LocalDate

/**
 * Содержимое листа календаря: заголовок листа, месяц со стрелками, сетка Пн..Вс и легенда.
 *
 * Перелистывание месяцев — свайп по сетке или стрелки (границы упираются на первом
 * и последнем месяце с данными). Тап по доступной дате выбирает её; недоступные даты
 * (серые плашки) не нажимаются.
 *
 * @param model модель листа (данные календаря)
 * @param onDateSelected выбор доступной даты
 * @param onDismiss закрыть лист без выбора
 */
@Composable
internal fun CalendarSheetContent(
    model: CalendarScreenModel,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.calendar_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            IconCircleButton(
                icon = PhosphorIcons.Regular.X,
                contentDescription = stringResource(R.string.cd_calendar_close),
                onClick = onDismiss,
            )
        }
        Spacer(Modifier.height(4.dp))

        when {
            model.isLoading -> CalendarStateBlock(stringResource(R.string.schedule_loading))
            model.errorMessage != null -> CalendarStateBlock(stringResource(R.string.schedule_error))
            model.pages.isEmpty() -> CalendarStateBlock(stringResource(R.string.schedule_no_data))
            else -> CalendarMonths(
                model = model,
                table = model.table,
                onDateSelected = onDateSelected,
            )
        }
    }
}

@Composable
private fun CalendarMonths(
    model: CalendarScreenModel,
    table: ScheduleTable,
    onDateSelected: (LocalDate) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = model.startIndex.coerceIn(0, model.pages.lastIndex),
        pageCount = { model.pages.size },
    )

    Spacer(Modifier.height(8.dp))
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
                    stateOf = { date -> dayState(date, table) },
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

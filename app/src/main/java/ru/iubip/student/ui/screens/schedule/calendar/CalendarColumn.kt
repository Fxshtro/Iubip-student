package ru.iubip.student.ui.screens.schedule.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.iubip.student.R
import ru.iubip.student.ui.theme.IubipShapes
import java.time.LocalDate

/** Ширина колонки календаря на широком экране (≥ [ru.iubip.student.ui.components.IubipBreakpoints.CALENDAR_COLUMN_MIN]). */
internal val CALENDAR_COLUMN_WIDTH = 320.dp

/**
 * Постоянная колонка календаря для широких экранов: заголовок «Календарь» и та же
 * сетка [CalendarGrid], что и в листе. Лист в этом режиме не открывается — календарь
 * всегда на виду справа от списка пар.
 *
 * Выбор даты работает так же, как в листе: одно событие `onDateSelected(date)`.
 *
 * @param model модель календаря
 * @param onDateSelected выбор доступной даты
 * @param width ширина колонки
 */
@Composable
internal fun CalendarColumn(
    model: CalendarScreenModel,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = CALENDAR_COLUMN_WIDTH,
) {
    Surface(
        modifier = modifier.width(width),
        shape = IubipShapes.Panel,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Text(
                text = stringResource(R.string.calendar_column_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp),
            )
            Spacer(Modifier.height(8.dp))
            when {
                model.isLoading -> CalendarStateBlock(stringResource(R.string.schedule_loading))
                model.errorMessage != null ->
                    CalendarStateBlock(stringResource(R.string.schedule_error))
                model.pages.isEmpty() ->
                    CalendarStateBlock(stringResource(R.string.schedule_no_data))
                else -> CalendarGrid(
                    model = model,
                    onDateSelected = onDateSelected,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

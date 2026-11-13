package ru.iubip.student.ui.screens.schedule.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.iubip.student.R
import ru.iubip.student.ui.components.IconCircleButton
import ru.iubip.student.ui.icons.PhosphorIcons
import java.time.LocalDate

/**
 * Содержимое листа календаря: заголовок листа, месяц со стрелками, сетка Пн..Вс и легенда.
 *
 * Сетка — общий [CalendarGrid]; лист календаря и колонка календаря на широком экране
 * рисуют один и тот же календарь ([CalendarColumn]).
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
            else -> {
                Spacer(Modifier.height(8.dp))
                CalendarGrid(
                    model = model,
                    onDateSelected = onDateSelected,
                )
            }
        }
    }
}

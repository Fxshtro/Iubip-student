package ru.iubip.student.ui.screens.schedule.calendar

import ru.iubip.student.data.CalendarAvailability
import ru.iubip.student.data.DayAvailability
import ru.iubip.student.data.source.ScheduleTable
import ru.iubip.student.data.source.WeekSchedule
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Модель листа календаря (без Compose — удобно тестировать).
 *
 * @param isLoading идёт загрузка расписания
 * @param errorMessage ошибка загрузки (текст показывается внутри листа)
 * @param pages страницы календаря; пусто, если показывать нечего
 * @param startIndex страница, которая открывается сразу
 * @param selectedDate выбранный в ленте день
 * @param today сегодня
 * @param table выложенные недели — по ним считается доступность каждого дня
 */
internal data class CalendarScreenModel(
    val isLoading: Boolean,
    val errorMessage: String?,
    val pages: List<CalendarPage>,
    val startIndex: Int,
    val selectedDate: LocalDate?,
    val today: LocalDate,
    val table: ScheduleTable,
)

/**
 * Страница календаря — один месяц, у которого есть выложенные недели.
 *
 * @param month месяц страницы
 * @param title заголовок «Ноябрь 2026»
 * @param cells ячейки сетки по строкам: 7 колонок Пн..Вс; неделя целиком — одна строка.
 *   Даты вне месяца (хвост соседних недель) тоже присутствуют — экран рисует их
 *   недоступными. Компактно и без «пустых» плашек в начале сетки.
 * @param inMonth подпись каждого дня месяца на странице: «2 ноября» — для экранного
 *   диктора (на странице соседнего месяца бывают те же числа, но другой месяц).
 */
internal data class CalendarPage(
    val month: YearMonth,
    val title: String,
    val cells: List<List<LocalDate>>,
    val inMonth: Map<LocalDate, String>,
) {
    /** Все недели сетки по порядку: первый день строки — понедельник. */
    val weekStarts: List<LocalDate> get() = cells.map { it.first() }
}

/**
 * Раскладка календаря для конкретных данных: только месяцы с выложенными неделями,
 * по строке на неделю, выходящие за месяц дни рисуются как недоступные.
 *
 * @param table выложенные недели группы
 * @param today сегодня
 */
internal fun buildCalendarModel(
    table: ScheduleTable,
    isLoading: Boolean,
    errorMessage: String?,
    selectedDate: LocalDate?,
    today: LocalDate,
): CalendarScreenModel {
    val pages = calendarPages(table)
    return CalendarScreenModel(
        isLoading = isLoading,
        errorMessage = errorMessage,
        pages = pages,
        startIndex = startPageIndex(pages, selectedDate, today),
        selectedDate = selectedDate,
        today = today,
        table = table,
    )
}

/**
 * Страницы календаря: месяц на страницу. Показываются только месяцы, в которых есть хотя бы
 * одна выложенная неделя (или её часть — неделя, перекрывающая границу месяца). Порядок —
 * учебного года: сентябрь → август.
 *
 * @param table выложенные недели группы
 */
internal fun calendarPages(table: ScheduleTable): List<CalendarPage> {
    val first = table.weeks.firstOrNull() ?: return emptyList()
    val last = table.weeks.last()

    val months = monthsBetween(YearMonth.from(first.start), YearMonth.from(last.end))
        .filter { month -> overlapsAny(month, table.weeks) }
        .groupBy { academicYearOf(it) }
        .toSortedMap()
        .values
        .flatMap { yearMonths -> yearMonths.sortedBy { academicOrderOf(it) } }

    return months.map { month ->
        CalendarPage(
            month = month,
            title = monthTitle(month),
            cells = cellsOf(month),
            inMonth = (1..month.lengthOfMonth()).associate { day ->
                val date = month.atDay(day)
                date to dayLabel(date)
            },
        )
    }
}

/**
 * Сетка месяца целиком (с «хвостом» соседних недель), чтобы неделя Пн..Вс всегда
 * занимала одну строку: от понедельника недели с 1-м числом до воскресенья недели
 * с последним числом месяца. Дни соседних месяцев рисуются как обычные дни месяца,
 * но недоступны — на их собственной странице они выглядят как настоящие.
 */
private fun cellsOf(month: YearMonth): List<List<LocalDate>> {
    val firstRowStart = month.atDay(1).minusDays((month.atDay(1).dayOfWeek.value - 1).toLong())
    val lastRowStart = month.atEndOfMonth().minusDays((month.atEndOfMonth().dayOfWeek.value - 1).toLong())
    val rows = ArrayList<List<LocalDate>>()
    var rowStart = firstRowStart
    while (!rowStart.isAfter(lastRowStart) && rows.size < MAX_ROWS) {
        rows.add((0 until DAYS_IN_WEEK).map { day -> rowStart.plusDays(day.toLong()) })
        rowStart = rowStart.plusDays(DAYS_IN_WEEK.toLong())
    }
    return rows
}

/**
 * Страница, открываемая сразу: месяц выбранного дня, если он есть; иначе месяц с сегодняшним
 * днём; иначе первая страница (ближайшая выложенная неделя).
 */
internal fun startPageIndex(pages: List<CalendarPage>, selectedDate: LocalDate?, today: LocalDate): Int {
    if (pages.isEmpty()) return 0
    selectedDate?.let { date ->
        val index = pages.indexOfFirst { it.month == YearMonth.from(date) }
        if (index >= 0) return index
    }
    val byToday = pages.indexOfFirst { it.month == YearMonth.from(today) }
    if (byToday >= 0) return byToday
    return 0
}

/** Подпись дня для экранного диктора: «2 ноября». */
private fun dayLabel(date: LocalDate): String =
    "${date.dayOfMonth} ${monthGenitive(date.monthValue)}"

/** Заголовок месяца: «Ноябрь 2026» (русский независимо от языка системы). */
internal fun monthTitle(month: YearMonth): String {
    val name = month.month.getDisplayName(TextStyle.FULL_STANDALONE, RUSSIAN)
        .replaceFirstChar { it.titlecase(RUSSIAN) }
    return "$name ${month.year}"
}

/** Состояние дня по таблице расписания. */
internal fun dayState(date: LocalDate, table: ScheduleTable): DayAvailability =
    CalendarAvailability.stateFor(date, table)

/** Порядковый номер месяца в учебном году: сентябрь = 0, …, август = 11. */
private fun academicOrderOf(month: YearMonth): Int = (month.monthValue - 9 + 12) % 12

/** Учебный год месяца: сентябрь..август. */
private fun academicYearOf(month: YearMonth): Int =
    if (month.monthValue >= 9) month.year else month.year - 1

/** Месяц пересекается с хоть одной выложенной неделей (учитывается и «хвост» недели). */
private fun overlapsAny(month: YearMonth, weeks: List<WeekSchedule>): Boolean =
    weeks.any { week ->
        !week.end.isBefore(month.atDay(1)) && !week.start.isAfter(month.atEndOfMonth())
    }

/** Месяцы от [from] до [to] включительно в хронологии. */
private fun monthsBetween(from: YearMonth, to: YearMonth): List<YearMonth> {
    val months = ArrayList<YearMonth>()
    var month = from
    while (!month.isAfter(to) && months.size < MAX_MONTHS) {
        months.add(month)
        month = month.plusMonths(1)
    }
    return months
}

private const val DAYS_IN_WEEK = 7

/** Сетка месяца занимает от 4 до 6 строк. */
private const val MAX_ROWS = 6

/** Защита от испорченных данных: больше тридцати месяцев подряд на экране не нужно. */
private const val MAX_MONTHS = 30

/** Интерфейс русский независимо от языка системы. */
internal val RUSSIAN: Locale = Locale.forLanguageTag("ru")

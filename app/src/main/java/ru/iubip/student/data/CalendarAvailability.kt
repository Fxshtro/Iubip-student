package ru.iubip.student.data

import ru.iubip.student.data.source.ScheduleTable
import ru.iubip.student.data.source.WeekSchedule
import java.time.LocalDate
import java.time.YearMonth

/**
 * Есть ли расписание на дату и можно ли её выбрать в календаре.
 *
 * Правило одно и то же для любого дня года — никаких Android-зависимостей:
 *
 * - [AVAILABLE] — дата есть в [ScheduleTable] и на этот день выложена хотя бы одна пара;
 * - [AVAILABLE_EMPTY] — дата есть в таблице, но пар на этот день нет
 *   (например воскресенье или выходной среди недели) — дата кликабельна, на экране
 *   будет «Пар нет»;
 * - [UNAVAILABLE] — даты нет в таблице: неделя ещё не выложена или уже удалена
 *   после выходных. Такие даты в календаре показываются серыми и не выбираются.
 *   Сюда же попадают даты до первой выложенной недели и после последней.
 */
enum class DayAvailability {
    AVAILABLE,
    AVAILABLE_EMPTY,
    UNAVAILABLE,
    ;

    /** Можно ли открыть этот день (и [AVAILABLE], и [AVAILABLE_EMPTY]). */
    val isSelectable: Boolean get() = this != UNAVAILABLE
}

/**
 * Состояния дат месяца для календаря.
 *
 * @param month месяц, для которого считан календарь
 * @param states состояние каждого дня месяца по ключу-дате; в нём ровно столько
 *   записей, сколько дней в месяце
 */
data class MonthAvailability(
    val month: YearMonth,
    val states: Map<LocalDate, DayAvailability>,
) {
    /** Состояние конкретного дня; даты вне месяца — [DayAvailability.UNAVAILABLE]. */
    fun stateFor(date: LocalDate): DayAvailability =
        states[date] ?: DayAvailability.UNAVAILABLE

    /** День является частью месяца (даже если недоступен). */
    fun belongsToMonth(date: LocalDate): Boolean = YearMonth.from(date) == month

    companion object {
        /** Календарь на месяц, в котором ни одного дня не доступно (например, таблица пустая). */
        fun unavailable(month: YearMonth): MonthAvailability = MonthAvailability(
            month = month,
            states = (1..month.lengthOfMonth()).associate { day ->
                month.atDay(day) to DayAvailability.UNAVAILABLE
            },
        )
    }
}

/**
 * Логика доступности дат расписания — то, что нужно календарю.
 *
 * Чистые функции без Android: вход — [ScheduleTable], выход — состояние или готовый
 * месяц. UI-экран сам решает, как это выглядит (серый, акцент, «Пар нет»).
 */
object CalendarAvailability {

    /**
     * Состояние одной даты.
     *
     * @param table выложенные недели группы
     * @param date проверяемая дата
     */
    fun stateFor(date: LocalDate, table: ScheduleTable): DayAvailability {
        val day = table.dayFor(date) ?: return DayAvailability.UNAVAILABLE
        return if (day.pairs.isEmpty()) DayAvailability.AVAILABLE_EMPTY else DayAvailability.AVAILABLE
    }

    /**
     * Состояния всех дней месяца.
     *
     * @param table выложенные недели группы
     * @param month год и месяц календаря
     */
    fun availability(table: ScheduleTable, month: YearMonth): MonthAvailability {
        val states = (1..month.lengthOfMonth()).associate { dayOfMonth ->
            val date = month.atDay(dayOfMonth)
            date to stateFor(date, table)
        }
        return MonthAvailability(month = month, states = states)
    }

    /** Перегрузка «месяц числом» для вызовов вида `availability(table, 11, 2026)`. */
    fun availability(table: ScheduleTable, month: Int, year: Int): MonthAvailability =
        availability(table, YearMonth.of(year, month))

    /**
     * Все доступные даты таблицы (с парами и без) по порядку.
     * Сегодня и соседние дни проверяются только по этой таблице — сетевого «today» нет.
     */
    fun availableDates(table: ScheduleTable): List<LocalDate> =
        table.weeks.flatMap { week: WeekSchedule -> week.days.map { it.date } }

    /** Можно ли дату открыть в календаре (доступна или доступна без пар). */
    fun isSelectable(date: LocalDate, table: ScheduleTable): Boolean =
        stateFor(date, table).isSelectable
}

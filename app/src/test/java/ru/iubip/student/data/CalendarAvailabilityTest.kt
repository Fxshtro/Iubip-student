package ru.iubip.student.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.iubip.student.data.model.ClassPair
import ru.iubip.student.data.model.Day
import ru.iubip.student.data.source.ScheduleTable
import ru.iubip.student.data.source.WeekSchedule
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

/**
 * Логика доступности дат для календаря. Чистый JVM-тест, без Android.
 *
 * Таблица повторяет тестовый файл: две недели 02–08.11 и 09–15.11,
 * прошлая неделя (26.10–01.11) не выложена, воскресенья пустые.
 */
class CalendarAvailabilityTest {

    private val table: ScheduleTable = ScheduleTable(
        group = "К3И1(9)",
        weeks = listOf(
            week(LocalDate.of(2026, 11, 2), busyDays = 1..6),
            week(LocalDate.of(2026, 11, 9), busyDays = 1..6),
        ),
    )

    @Test
    fun `дата с парами доступна`() {
        assertEquals(DayAvailability.AVAILABLE, CalendarAvailability.stateFor(LocalDate.of(2026, 11, 2), table))
        assertEquals(DayAvailability.AVAILABLE, CalendarAvailability.stateFor(LocalDate.of(2026, 11, 11), table))
    }

    @Test
    fun `дата без пар доступна, но помечена пустой`() {
        // воскресенья 08.11 и 15.11 есть в таблице, пар нет
        assertEquals(DayAvailability.AVAILABLE_EMPTY, CalendarAvailability.stateFor(LocalDate.of(2026, 11, 8), table))
        assertEquals(DayAvailability.AVAILABLE_EMPTY, CalendarAvailability.stateFor(LocalDate.of(2026, 11, 15), table))
        assertTrue(CalendarAvailability.stateFor(LocalDate.of(2026, 11, 8), table).isSelectable)
    }

    @Test
    fun `дата вне диапазона недоступна`() {
        // до первой недели — прошлое удалено
        assertEquals(DayAvailability.UNAVAILABLE, CalendarAvailability.stateFor(LocalDate.of(2026, 11, 1), table))
        assertEquals(DayAvailability.UNAVAILABLE, CalendarAvailability.stateFor(LocalDate.of(2026, 10, 31), table))
        // после последней недели — ещё не выложено
        assertEquals(DayAvailability.UNAVAILABLE, CalendarAvailability.stateFor(LocalDate.of(2026, 11, 16), table))
        assertEquals(DayAvailability.UNAVAILABLE, CalendarAvailability.stateFor(LocalDate.of(2026, 12, 1), table))
        assertFalse(CalendarAvailability.isSelectable(LocalDate.of(2026, 11, 16), table))
    }

    @Test
    fun `пропуск между неделями недоступен`() {
        val withGap = ScheduleTable(
            group = "К3И1(9)",
            weeks = listOf(
                week(LocalDate.of(2026, 11, 2), busyDays = 1..6),
                week(LocalDate.of(2026, 11, 16), busyDays = 1..6),
            ),
        )

        assertEquals(DayAvailability.UNAVAILABLE, CalendarAvailability.stateFor(LocalDate.of(2026, 11, 9), withGap))
        assertEquals(DayAvailability.AVAILABLE, CalendarAvailability.stateFor(LocalDate.of(2026, 11, 16), withGap))
    }

    @Test
    fun `все дни месяца получают состояние`() {
        val month = CalendarAvailability.availability(table, YearMonth.of(2026, 11))

        assertEquals(YearMonth.of(2026, 11), month.month)
        assertEquals(30, month.states.size)
        assertEquals((1..30).map { YearMonth.of(2026, 11).atDay(it) }, month.states.keys.toList())
    }

    @Test
    fun `границы ноября 2026`() {
        val month = CalendarAvailability.availability(table, 11, 2026)

        // 01.11 — прошлая неделя удалена → недоступно
        assertEquals(DayAvailability.UNAVAILABLE, month.stateFor(LocalDate.of(2026, 11, 1)))
        // 02–07.11 — Пн..Сб первой недели → доступно
        (2..7).forEach { day ->
            assertEquals(
                "день $day.11",
                DayAvailability.AVAILABLE,
                month.stateFor(LocalDate.of(2026, 11, day)),
            )
        }
        // 08.11 — воскресенье, в таблице есть, пар нет
        assertEquals(DayAvailability.AVAILABLE_EMPTY, month.stateFor(LocalDate.of(2026, 11, 8)))
        // 09–14.11 — следующая неделя → доступно
        (9..14).forEach { day ->
            assertEquals(
                "день $day.11",
                DayAvailability.AVAILABLE,
                month.stateFor(LocalDate.of(2026, 11, day)),
            )
        }
        // 15.11 — воскресенье второй недели
        assertEquals(DayAvailability.AVAILABLE_EMPTY, month.stateFor(LocalDate.of(2026, 11, 15)))
        // 16–30.11 — ещё не выложено
        (16..30).forEach { day ->
            assertEquals(
                "день $day.11",
                DayAvailability.UNAVAILABLE,
                month.stateFor(LocalDate.of(2026, 11, day)),
            )
        }
    }

    @Test
    fun `даты вне месяца месяца считаются недоступными`() {
        val month = CalendarAvailability.availability(table, YearMonth.of(2026, 11))

        assertFalse(month.belongsToMonth(LocalDate.of(2026, 12, 1)))
        assertEquals(DayAvailability.UNAVAILABLE, month.stateFor(LocalDate.of(2026, 12, 1)))
    }

    @Test
    fun `пустая таблица — весь месяц недоступен`() {
        val empty = ScheduleTable.empty("К3И1(9)")
        val month = CalendarAvailability.availability(empty, YearMonth.of(2026, 11))

        assertEquals(30, month.states.size)
        assertTrue(month.states.values.all { it == DayAvailability.UNAVAILABLE })
        assertEquals(0, CalendarAvailability.availableDates(empty).size)
    }

    @Test
    fun `availableDates перечисляет все даты таблицы`() {
        val dates = CalendarAvailability.availableDates(table)

        assertEquals(14, dates.size)
        assertEquals(LocalDate.of(2026, 11, 2), dates.first())
        assertEquals(LocalDate.of(2026, 11, 15), dates.last())
    }

    @Test
    fun `состояния имеют признак выбора`() {
        assertTrue(DayAvailability.AVAILABLE.isSelectable)
        assertTrue(DayAvailability.AVAILABLE_EMPTY.isSelectable)
        assertFalse(DayAvailability.UNAVAILABLE.isSelectable)
    }

    // --- Хелперы ---

    private fun week(start: LocalDate, busyDays: IntRange): WeekSchedule = WeekSchedule(
        group = "К3И1(9)",
        start = start,
        days = (0..6).map { index ->
            val date = start.plusDays(index.toLong())
            val dayNumber = index + 1
            val pairs = if (dayNumber in busyDays) listOf(PAIR) else emptyList()
            Day(date = date, pairs = pairs)
        },
    )

    private companion object {
        val PAIR = ClassPair(
            subject = "Математика",
            type = "пр.",
            teacher = "Кузнецов Д.С.",
            room = "2-118",
            slot = 1,
            start = LocalTime.of(8, 30),
            end = LocalTime.of(10, 5),
        )
    }
}

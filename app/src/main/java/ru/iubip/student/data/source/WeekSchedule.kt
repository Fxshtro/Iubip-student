package ru.iubip.student.data.source

import ru.iubip.student.data.model.Day
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Нормализованное расписание на одну неделю.
 *
 * @param group группа, для которой получены данные
 * @param start дата понедельника (дата из ответа; если не пришла — неделя текущей даты)
 * @param days семь дней Пн..Вс; день без пар — [Day] с пустым списком
 */
data class WeekSchedule(
    val group: String,
    val start: LocalDate,
    val days: List<Day>,
) {
    /** Дата дня по индексу 0..6 (0 — понедельник). */
    fun dateOf(dayIndex: Int): LocalDate = start.plusDays(dayIndex.toLong())

    /** День этой недели с указанной датой или null, если дата вне недели. */
    fun dayFor(date: LocalDate): Day? = days.firstOrNull { it.date == date }

    /** Дата воскресенья — последний день недели. */
    val end: LocalDate get() = start.plusDays((DAYS_IN_WEEK - 1).toLong())

    /** Есть ли дата в этой неделе (Пн..Вс). */
    fun hasDate(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)

    companion object {
        /** Дней в неделе. */
        const val DAYS_IN_WEEK = 7

        /**
         * Формат дат в ответе сайта: dd.MM.yyyy (период, `per`) и dd-MM-yyyy (поле DATE пары).
         * Сайт пишет день без ведущего нуля («3-11-2026»), поэтому формат с переменной шириной.
         */
        internal val PAIR_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("d-M-uuuu")
    }
}

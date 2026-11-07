package ru.iubip.student.data

import java.time.LocalTime

/**
 * Таблица звонков: номер пары → время начала и окончания.
 *
 * Единое место с расписанием звонков, больше нигде номера пар не переводятся во время.
 * ВНИМАНИЕ: время ориентировочное — уточнить в деканате.
 */
object BellSchedule {

    /** Пара: номер, начало, окончание. */
    data class Bell(val slot: Int, val start: LocalTime, val end: LocalTime)

    /** Звонки на 6 пар в учебный день. */
    val bells: List<Bell> = listOf(
        Bell(1, LocalTime.of(8, 30), LocalTime.of(10, 5)),
        Bell(2, LocalTime.of(10, 15), LocalTime.of(11, 50)),
        Bell(3, LocalTime.of(12, 0), LocalTime.of(13, 35)),
        Bell(4, LocalTime.of(14, 15), LocalTime.of(15, 50)),
        Bell(5, LocalTime.of(16, 0), LocalTime.of(17, 35)),
        Bell(6, LocalTime.of(17, 45), LocalTime.of(19, 20)),
    )

    /** Звонок по номеру пары или null, если номер вне таблицы. */
    fun bellFor(slot: Int): Bell? = bells.firstOrNull { it.slot == slot }
}

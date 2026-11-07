package ru.iubip.student.data.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Одна пара в конкретный календарный день.
 *
 * @param subject название предмета («Информационные технологии»)
 * @param type тип занятия строкой, как приходит из источника («лек.», «пр.»).
 *   Не enum: новые типы не требуют правок кода.
 * @param teacher преподаватель в формате «Иванов И.И.»
 * @param room аудитория в формате «1-305»
 * @param slot номер пары (1–6)
 * @param start время начала пары по таблице звонков
 * @param end время окончания пары по таблице звонков
 */
data class ClassPair(
    val subject: String,
    val type: String,
    val teacher: String,
    val room: String,
    val slot: Int,
    val start: LocalTime,
    val end: LocalTime,
)

/**
 * Учебный день с датой и списком пар, упорядоченным по номеру пары.
 *
 * @param date календарная дата дня
 * @param pairs пары дня (пустой список — «пар нет»)
 */
data class Day(
    val date: LocalDate,
    val pairs: List<ClassPair>,
)

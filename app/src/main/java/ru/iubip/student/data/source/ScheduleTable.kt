package ru.iubip.student.data.source

import ru.iubip.student.data.model.Day
import java.time.LocalDate

/**
 * Расписание группы на все недели, которые выложены в источнике.
 *
 * В колледже расписание выкладывают на 3 недели вперёд, а после выходных прошедшую
 * неделю удаляют. Поэтому таблица — это «острова» опубликованных недель, а не
 * непрерывный диапазон дат: даты вне [weeks] расписания не имеют.
 *
 * @param group группа, для которой получены данные
 * @param weeks недели по возрастанию даты начала, без дублей дат
 *
 * Инварианты (соблюдает [JsonScheduleParser.parseAll]): недели отсортированы,
 * даты между неделями не пересекаются. Конструктор не падает на нарушении —
 * методы просто отдают первое совпадение, — но нормализовать данные лучше здесь:
 * [of] сортирует и склеивает недели при сборке.
 */
data class ScheduleTable(
    val group: String,
    val weeks: List<WeekSchedule>,
) {
    /** Все даты таблицы по порядку; пустой список, если недель нет. */
    fun dates(): List<LocalDate> = weeks.flatMap { week -> week.days.map { it.date } }

    /** Первая дата таблицы или null, если данных нет. */
    val firstDate: LocalDate? get() = weeks.firstOrNull()?.start

    /** Последняя дата таблицы или null, если данных нет. */
    val lastDate: LocalDate? get() = weeks.lastOrNull()?.end

    /** Есть ли дата в таблице (то есть расписание на этот день опубликовано). */
    fun hasDate(date: LocalDate): Boolean = weeks.any { it.hasDate(date) }

    /** День с указанной датой или null, если такого дня в таблице нет. */
    fun dayFor(date: LocalDate): Day? = weeks.firstNotNullOfOrNull { it.dayFor(date) }

    /** Собрать таблицу из произвольного набора недель: сортировка по началу + без дублей дат. */
    companion object {
        /** Пустая таблица — источник ответил, но недель нет. */
        fun empty(group: String): ScheduleTable = ScheduleTable(group, emptyList())

        /**
         * Нормализует недели: сортирует по дате начала и склеивает так, чтобы ни одна
         * дата не повторилась (день из более ранней недели побеждает).
         */
        fun of(group: String, weeks: List<WeekSchedule>): ScheduleTable {
            val seen = HashSet<LocalDate>()
            val merged = weeks
                .sortedBy { it.start }
                .map { week ->
                    week.copy(days = week.days.filter { seen.add(it.date) })
                }
                .filter { it.days.isNotEmpty() }
            return ScheduleTable(group = group, weeks = merged)
        }
    }
}

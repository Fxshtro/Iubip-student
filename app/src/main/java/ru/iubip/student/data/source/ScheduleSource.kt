package ru.iubip.student.data.source

/**
 * Источник строки JSON расписания.
 *
 * Реализация скрыта от экрана: сейчас читает файл из `res/raw`,
 * позже — то же самое, но из сети.
 */
fun interface ScheduleSource {
    /** Возвращает JSON расписания для указанной группы, либо null, если данных нет. */
    suspend fun load(group: String): String?
}

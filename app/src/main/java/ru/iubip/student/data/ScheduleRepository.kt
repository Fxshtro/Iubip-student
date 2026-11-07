package ru.iubip.student.data

import ru.iubip.student.data.source.ScheduleTable
import ru.iubip.student.data.source.WeekSchedule

/**
 * Доступ к расписанию группы.
 *
 * Экран работает только с этим интерфейсом и не знает, откуда пришли данные
 * (`res/raw` сейчас, сеть — позже).
 */
interface ScheduleRepository {
    /**
     * Загружает расписание группы на неделю.
     *
     * Оставлен для экрана, который показывает одну неделю. Это первая неделя из [table]
     * (она же та, где идёт сегодня, когда сегодня попадает в выложенный период).
     *
     * @return нормализованная неделя; [Result.failure] — источник недоступен или ответ не разобран
     */
    suspend fun schedule(group: String): Result<WeekSchedule>

    /**
     * Загружает расписание группы на все выложенные недели — источник для календаря.
     *
     * @return таблица недель (может быть пустой, если недель нет);
     *   [Result.failure] — источник недоступен или ответ не разобран
     */
    suspend fun table(group: String): Result<ScheduleTable>
}

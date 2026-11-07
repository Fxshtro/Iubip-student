package ru.iubip.student.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.iubip.student.data.source.JsonScheduleParser
import ru.iubip.student.data.source.ScheduleSource
import ru.iubip.student.data.source.ScheduleTable
import ru.iubip.student.data.source.WeekSchedule

/**
 * Репозиторий расписания: берёт строку JSON у [source] и разбирает её.
 *
 * Сейчас [source] — файл из `res/raw`. Для сети достаточно передать здесь
 * сетевую реализацию [ScheduleSource]; интерфейс [ScheduleRepository] не меняется.
 */
class DefaultScheduleRepository(
    private val source: ScheduleSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ScheduleRepository {

    override suspend fun schedule(group: String): Result<WeekSchedule> = withContext(ioDispatcher) {
        runCatching {
            JsonScheduleParser.parse(rawFor(group), group)
        }
    }

    override suspend fun table(group: String): Result<ScheduleTable> = withContext(ioDispatcher) {
        runCatching {
            JsonScheduleParser.parseAll(rawFor(group), group)
        }
    }

    private suspend fun rawFor(group: String): String =
        source.load(group) ?: throw IllegalStateException("Нет данных расписания для группы $group")
}

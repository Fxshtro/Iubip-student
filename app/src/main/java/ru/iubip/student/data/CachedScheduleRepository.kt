package ru.iubip.student.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.iubip.student.data.model.Day
import ru.iubip.student.data.source.JsonScheduleParser
import ru.iubip.student.data.source.ScheduleTable
import ru.iubip.student.data.source.WeekSchedule

/**
 * Репозиторий с двухфазной загрузкой: сначала показывает сохранённое расписание (если есть),
 * потом обновляет его из сети. Если сеть недоступна — кеш остаётся, а вызывающий получает
 * [LoadOutcome.Stale] вместе с уже показанными данными.
 *
 * Кеш — только публичное расписание; выбор группы по-прежнему не сохраняется как настройка.
 */
class CachedScheduleRepository(
    private val remote: ScheduleRepository,
    private val cache: ScheduleCacheStore,
) : ScheduleRepository {

    /** Результат загрузки с информацией об актуальности. */
    sealed interface LoadOutcome {
        /** Данные только что получены с сайта. */
        data object Fresh : LoadOutcome

        /** Показаны сохранённые данные: либо сразу, либо после неудачного обновления. */
        data class Stale(val updatedAt: Long) : LoadOutcome

        /** Нет ни свежих, ни сохранённых данных. */
        data object Waiting : LoadOutcome
    }

    /** Кеш группы (сырой JSON + время), если есть. */
    fun cached(group: String): ScheduleCacheStore.CachedSchedule? = cache.read(group)

    /**
     * Загружает расписание: сначала кеш, затем обновление.
     *
     * @param onOutcome сообщает о смене фазы — VM показывает индикатор сразу после чтения кеша
     */
    suspend fun loadWithStatus(
        group: String,
        onOutcome: (LoadOutcome) -> Unit,
    ): Result<ScheduleTable> = withContext(Dispatchers.IO) {
        val cached = cache.read(group)
        if (cached != null) {
            runCatching { JsonScheduleParser.parseAll(cached.raw, group) }
                .onSuccess { onOutcome(LoadOutcome.Stale(cached.updatedAt)) }
        } else {
            onOutcome(LoadOutcome.Waiting)
        }

        val fresh = remote.table(group)
        fresh.fold(
            onSuccess = { table ->
                onOutcome(LoadOutcome.Fresh)
                Result.success(table)
            },
            onFailure = { error ->
                if (cached != null) {
                    runCatching { JsonScheduleParser.parseAll(cached.raw, group) }
                        .fold(
                            onSuccess = { table ->
                                onOutcome(LoadOutcome.Stale(cached.updatedAt))
                                Result.success(table)
                            },
                            onFailure = { Result.failure<ScheduleTable>(error) },
                        )
                } else {
                    Result.failure<ScheduleTable>(error)
                }
            },
        )
    }

    override suspend fun schedule(group: String): Result<WeekSchedule> = remote.schedule(group)

    override suspend fun table(group: String): Result<ScheduleTable> = remote.table(group)
}

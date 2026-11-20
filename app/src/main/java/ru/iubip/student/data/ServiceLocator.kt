package ru.iubip.student.data

import android.content.Context
import ru.iubip.student.data.source.RawScheduleSource
import ru.iubip.student.data.source.RemoteScheduleSource

/**
 * Простейший ручной DI-контейнер приложения.
 *
 * Без Hilt и лишних зависимостей: единственное место, где собирается граф объектов.
 */
object ServiceLocator {

    private const val DEFAULT_GROUP = "К3И1(9)"

    @Volatile
    private var localRepository: ScheduleRepository? = null

    @Volatile
    private var remoteRepository: CachedScheduleRepository? = null

    @Volatile
    private var cacheStore: ScheduleCacheStore? = null

    @Volatile
    private var groupsRepository: GroupsRepository? = null

    /**
     * Переключатель источника расписания для временного пульта.
     * `false` (по умолчанию) → старый [RawScheduleSource] из `res/raw`.
     * Пульт сам включает `true` при выборе группы. Флаг живёт только в памяти процесса.
     */
    @Volatile
    var useRemote: Boolean = false

    /** Группа по умолчанию на старте (локальные данные, без сети). */
    const val START_GROUP: String = DEFAULT_GROUP

    fun cacheStore(context: Context): ScheduleCacheStore {
        return cacheStore ?: synchronized(this) {
            cacheStore ?: ScheduleCacheStore(context.applicationContext).also { cacheStore = it }
        }
    }

    /**
     * Стартовая группа. По умолчанию — локальные тестовые данные К3И1(9).
     * Если же в кеше есть сохранённое расписание другой группы, показываем его:
     * это НЕ сохранение пользовательского выбора как настройки, а восстановление
     * закэшированных данных, чтобы расписание работало без сети после перезапуска.
     */
    fun initialGroup(context: Context): String {
        val cachedGroup = cacheStore(context).lastCachedGroup()
        if (!cachedGroup.isNullOrBlank()) {
            useRemote = true
            return cachedGroup
        }
        return START_GROUP
    }

    /** Репозиторий расписания; создаётся лениво при первом обращении. */
    fun scheduleRepository(context: Context): ScheduleRepository {
        return if (useRemote) {
            cachedScheduleRepository(context)
        } else {
            localRepository ?: synchronized(this) {
                localRepository ?: DefaultScheduleRepository(
                    source = RawScheduleSource(context.applicationContext),
                ).also { localRepository = it }
            }
        }
    }

    /** Репозиторий с кешем и статусом актуальности (только при включённом remote). */
    fun cachedScheduleRepository(context: Context): CachedScheduleRepository {
        return remoteRepository ?: synchronized(this) {
            remoteRepository ?: CachedScheduleRepository(
                remote = DefaultScheduleRepository(
                    source = RemoteScheduleSource(cache = cacheStore(context)),
                ),
                cache = cacheStore(context),
            ).also { remoteRepository = it }
        }
    }

    /** Репозиторий списка групп (только через шлюз). */
    fun groupsRepository(): GroupsRepository {
        return groupsRepository ?: synchronized(this) {
            groupsRepository ?: RemoteGroupsRepository().also { groupsRepository = it }
        }
    }
}

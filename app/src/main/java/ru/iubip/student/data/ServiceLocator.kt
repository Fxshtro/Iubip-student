package ru.iubip.student.data

import android.content.Context
import ru.iubip.student.data.source.RawScheduleSource

/**
 * Простейший ручной DI-контейнер приложения.
 *
 * Без Hilt и лишних зависимостей: единственное место, где собирается граф объектов.
 * Создаётся один раз в `Application` и живёт столько же.
 */
object ServiceLocator {

    @Volatile
    private var scheduleRepository: ScheduleRepository? = null

    /** Репозиторий расписания; создаётся лениво при первом обращении. */
    fun scheduleRepository(context: Context): ScheduleRepository {
        return scheduleRepository ?: synchronized(this) {
            scheduleRepository ?: DefaultScheduleRepository(
                source = RawScheduleSource(context.applicationContext),
            ).also { scheduleRepository = it }
        }
    }
}

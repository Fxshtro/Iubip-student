package ru.iubip.student.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Локальный кеш ответов расписания (только публичные данные, без данных LMS).
 *
 * SharedPreferences выбран потому, что DataStore в проекте не подключён, а новых
 * зависимостей добавлять нельзя. Храним сырой JSON сайта: парсер уже есть в приложении.
 *
 * ВАЖНО: это НЕ сохранение пользовательского выбора как настройки. Кеш хранит/group
 * данные и время последней успешной загрузки, чтобы показать сохранённое расписание
 * без сети. Пользовательский «пульт» по-прежнему временный: отдельного флага выбора
 * группы в настройках нет.
 */
class ScheduleCacheStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Данные одной группы, полученные с сайта через шлюз. */
    data class CachedSchedule(
        val group: String,
        val raw: String,
        val updatedAt: Long,
    )

    /** Читает кеш группы, если он есть. */
    fun read(group: String): CachedSchedule? {
        val raw = prefs.getString(rawKey(group), null) ?: return null
        val timestamp = prefs.getLong(timeKey(group), 0L)
        return CachedSchedule(group = group, raw = raw, updatedAt = timestamp)
    }

    /** Сохраняет успешный ответ сайта. */
    fun write(group: String, raw: String, updatedAt: Long = System.currentTimeMillis()) {
        prefs.edit()
            .putString(rawKey(group), raw)
            .putLong(timeKey(group), updatedAt)
            .putString(KEY_LAST_GROUP, group)
            .apply()
    }

    /**
     * Группа последнего успешно закэшированного ответа — нужна только для того,
     * чтобы после перезапуска без сети показать сохранённое расписание.
     */
    fun lastCachedGroup(): String? = prefs.getString(KEY_LAST_GROUP, null)

    private fun rawKey(group: String) = "$KEY_RAW_PREFIX$group"
    private fun timeKey(group: String) = "$KEY_TIME_PREFIX$group"

    companion object {
        private const val PREFS_NAME = "schedule_cache"
        private const val KEY_RAW_PREFIX = "raw_"
        private const val KEY_TIME_PREFIX = "time_"
        private const val KEY_LAST_GROUP = "last_group"
    }
}

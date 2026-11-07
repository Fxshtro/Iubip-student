package ru.iubip.student.data.source

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.iubip.student.R

/**
 * Источник расписания из файла `app/src/main/res/raw/schedule_k3i1_9.json`.
 *
 * Файл повторяет формат ответа сайта колледжа, поэтому при переходе на сеть
 * заменяется только реализация [ScheduleSource] — экран и разбор не меняются.
 */
class RawScheduleSource(
    private val context: Context,
) : ScheduleSource {

    override suspend fun load(group: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            context.resources.openRawResource(R.raw.schedule_k3i1_9).use { input ->
                input.readBytes().decodeToString()
            }
        }.getOrNull()
    }

    companion object {
        /** Группа, для которой лежит файл расписания. */
        const val GROUP = "К3И1(9)"
    }
}

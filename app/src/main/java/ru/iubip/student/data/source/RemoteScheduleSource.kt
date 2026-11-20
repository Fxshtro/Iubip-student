package ru.iubip.student.data.source

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.iubip.student.data.ScheduleCacheStore
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Источник расписания через локальный Node.js-шлюз.
 *
 * Дёргает `GET <baseUrl>/api/schedule?group=...` и возвращает ответ КАК ЕСТЬ:
 * разбором занимается [JsonScheduleParser] в репозитории, здесь только HTTP + текст.
 * Успешный ответ сохраняется в [ScheduleCacheStore], чтобы его можно было показать
 * без сети (через [CachedScheduleRepository]).
 *
 * Без OkHttp и новых зависимостей — обычный [HttpURLConnection] из JDK.
 *
 * @param baseUrl адрес шлюза; по умолчанию `http://10.0.2.2:3000` — так эмулятор
 *   Android видит хост-машину, на которой запущен `node gateway/server.js`
 */
class RemoteScheduleSource(
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val cache: ScheduleCacheStore? = null,
) : ScheduleSource {

    override suspend fun load(group: String): String? = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(group, Charsets.UTF_8.name())
        val connection = URL("$baseUrl/api/schedule?group=$encoded").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("Шлюз вернул HTTP ${connection.responseCode}")
            }
            val raw = connection.inputStream.use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            }
            val text = raw.ifBlank { null }
            if (text != null) cache?.write(group, text)
            text
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        /** Адрес шлюза по умолчанию (эмулятор → хост-машина). */
        const val DEFAULT_BASE_URL = "http://10.0.2.2:3000"

        private const val TIMEOUT_MS = 15_000
    }
}

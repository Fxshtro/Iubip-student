package ru.iubip.student.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.iubip.student.data.source.RemoteScheduleSource
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/** Академия с перечнем групп для пульта. */
@Serializable
data class AcademyGroups(
    @SerialName("name") val name: String,
    @SerialName("groups") val groups: List<String>,
)

/**
 * Список групп с сайта через шлюз `GET /api/groups`.
 *
 * Парсим ответ в kotlinx.serialization (уже есть в проекте): контракт шлюза —
 * `{ "academies": [ { "name": "…", "groups": ["…"] } ] }`.
 */
interface GroupsRepository {
    suspend fun academies(): Result<List<AcademyGroups>>
}

/** Реализация [GroupsRepository] через HTTP-шлюз, без новых зависимостей. */
class RemoteGroupsRepository(
    private val baseUrl: String = RemoteScheduleSource.DEFAULT_BASE_URL,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : GroupsRepository {

    override suspend fun academies(): Result<List<AcademyGroups>> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL("$baseUrl/api/groups").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IllegalStateException("Шлюз вернул HTTP ${connection.responseCode}")
                }
                connection.inputStream.use { stream ->
                    BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                        json.decodeFromString<GroupsResponse>(reader.readText()).academies
                    }
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    @Serializable
    private data class GroupsResponse(val academies: List<AcademyGroups> = emptyList())

    companion object {
        private const val TIMEOUT_MS = 15_000
    }
}

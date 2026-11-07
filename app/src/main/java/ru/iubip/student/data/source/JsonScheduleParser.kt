package ru.iubip.student.data.source

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ru.iubip.student.data.BellSchedule
import ru.iubip.student.data.model.ClassPair
import ru.iubip.student.data.model.Day
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.util.Locale

/**
 * Разбор ответа сайта колледжа: одна неделя ([parse]) или все недели ([parseAll]).
 *
 * Структура ответа (см. образец `/sched_js`):
 * ```
 * { "<ГРУППА>": [ "<период>", {
 *       "<номер недели>": [ {"per","today","days"},
 *                           {"<день 1..7>": {"<пара>": [pairs]}} ],
 *       "<номер недели>": [ ... ][, ...]
 *   } ] }
 * ```
 * В одном ответе может быть несколько недель (сайт выкладывает их на 3 вперёд), порядок
 * ключей произвольный. Все поля пары — строки, значения часто с хвостовыми пробелами;
 * необязательные поля (SUBJECT, NAME, AUD, LES, SUBJ_TYPE) могут отсутствовать, поэтому
 * значения читаются построчно как элементы JSON, а не через жёсткую @Serializable-схему.
 */
object JsonScheduleParser {

    private val json = Json { ignoreUnknownKeys = true }

    /** Поля, по которым объект в ответе распознаётся как пара. */
    private val PAIR_KEYS = setOf("LES", "SUBJECT", "DATE")

    /** Даты в ответе пишутся через точку или дефис — к разбору приводим к дефису. */
    private val DATE_FORMAT: DateTimeFormatter = WeekSchedule.PAIR_DATE

    /**
     * Словесные даты в `days[]` («02 ноября 2026 г.») — всегда по-русски, локаль системы
     * не влияет. Хвостовое «г.» необязательно; такая дата нужна только как резерв, когда
     * в ответе нет ни периода, ни поля DATE.
     */
    private val RU_DATE: DateTimeFormatter = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("d MMMM")
        .optionalStart()
        .appendPattern(" uuuu")
        .optionalEnd()
        .optionalStart()
        .appendPattern(" 'г.'")
        .optionalEnd()
        .parseDefaulting(ChronoField.YEAR, 1970)
        .toFormatter(Locale.forLanguageTag("ru"))

    /**
     * Разбирает JSON и возвращает **первую неделю** таблицы для [group].
     *
     * Оставлен для обратной совместимости с экраном, который показывает одну неделю.
     * Если нужны все недели (календарь) — [parseAll].
     *
     * @throws IllegalArgumentException если JSON нечитаем или группа не найдена
     */
    fun parse(raw: String, group: String): WeekSchedule = parseAll(raw, group).weeks.firstOrNull()
        ?: throw IllegalArgumentException("В ответе нет ни одной недели с датами для группы $group")

    /**
     * Разбирает JSON и возвращает **все** недели для [group] в виде [ScheduleTable].
     *
     * Неделя без разобранного периода не ломает результат — она пропускается,
     * остальные читаются как обычно. Недель может не остаться вовсе: тогда таблица пустая.
     *
     * @throws IllegalArgumentException если JSON нечитаем или группа не найдена
     */
    fun parseAll(raw: String, group: String): ScheduleTable {
        val root = try {
            json.parseToJsonElement(raw)
        } catch (e: Exception) {
            throw IllegalArgumentException("Некорректный JSON расписания", e)
        } as? JsonObject ?: throw IllegalArgumentException("Корень ответа — не объект")

        val groupNode = root[group]?.asArrayOrNull()
            ?: throw IllegalArgumentException("Группа $group не найдена в ответе")

        // Элементы массива группы: строковый период (необязателен) и объект
        // «номер недели» → [информация о неделе, дни]. Позиции не фиксированы: период
        // может отсутствовать, поэтому периоды и объект недель разбираются по типу элемента.
        val groupPeriod = groupNode.filterIsInstance<JsonPrimitive>()
            .firstOrNull()?.content

        // Порядок ключей в ответе произвольный, поэтому недели собираются все и сортируются.
        val weeksNode = groupNode.firstOrNull { it is JsonObject } as? JsonObject
        val weeks = weeksNode?.values.orEmpty()
            .mapNotNull { it.asArrayOrNull() }
            .map { weekNode -> parseWeek(group, weekNode, groupPeriod) }
            .filterNotNull()

        return ScheduleTable.of(group = group, weeks = weeks)
    }

    /**
     * Одна неделя из пары «информация о неделе, дни». null — если дату начала
     * восстановить не удалось: без неё нельзя понять, на какие календарные дни ложится
     * неделя, поэтому такая неделя пропускается, а остальные читаются как обычно.
     */
    private fun parseWeek(group: String, weekNode: JsonArray, groupPeriod: String?): WeekSchedule? {
        val infoNode = weekNode.getOrNull(0).asObjectOrNull()
        val daysNode = weekNode.getOrNull(1).asObjectOrNull()
        val start = parsePeriod(infoNode?.get("per").asString(), groupPeriod, infoNode, daysNode)
        if (start == null) return null
        val days = (0 until WeekSchedule.DAYS_IN_WEEK).map { index ->
            Day(date = start.plusDays(index.toLong()), pairs = parseDay(daysNode, index + 1))
        }
        return WeekSchedule(group = group, start = start, days = days)
    }

    private fun parseDay(daysNode: JsonObject?, dayNumber: Int): List<ClassPair> {
        val dayNode = daysNode?.get(dayNumber.toString()).asObjectOrNull() ?: return emptyList()
        return pairsOf(dayNode)
            .mapNotNull(::parsePair)
            .sortedWith(compareBy({ it.slot }, { it.start }))
    }

    /**
     * Все объекты пар из узла «день → номер пары → [пары]».
     * Формат ответа исторически менялся: массив пар может стоять как прямо под номером
     * пары, так и в дополнительной обёртке, поэтому обходим дерево, а не фиксированную глубину.
     */
    private fun pairsOf(dayNode: JsonObject?): List<JsonObject> {
        if (dayNode == null) return emptyList()
        val pairs = ArrayList<JsonObject>()
        val queue = ArrayDeque<JsonElement>(dayNode.values)
        while (queue.isNotEmpty()) {
            when (val element = queue.removeFirst()) {
                is JsonArray -> queue.addAll(element)
                is JsonObject -> {
                    // Пара отличается от обёртки «номер пары → [...]» наличием собственных полей.
                    if (element.keys.any { it in PAIR_KEYS }) pairs += element else queue.addAll(element.values)
                }
                else -> Unit
            }
        }
        return pairs
    }

    private fun parsePair(node: JsonObject): ClassPair? {
        val subject = node.trimmed("SUBJECT")
        if (subject.isBlank()) return null
        val slot = node.trimmed("LES").toIntOrNull() ?: return null
        val bell = BellSchedule.bellFor(slot)

        return ClassPair(
            subject = subject,
            type = node.trimmed("SUBJ_TYPE"),
            teacher = node.trimmed("NAME"),
            room = node.trimmed("AUD"),
            slot = slot,
            start = bell?.start ?: LocalTime.MIN,
            end = bell?.end ?: LocalTime.MIN,
        )
    }

    /**
     * Дата понедельника недели. Источники по убыванию надёжности:
     * 1) `per` самой недели («dd.MM.yyyy - dd.MM.yyyy»);
     * 2) первая дата в `days[]` (словесный формат «02 ноября 2026 г.», «Сегодня» пропускается),
     *    от неё отсчитываем понедельник;
     * 3) период группы — первая неделя ответа обычно совпадает с ним;
     * 4) поле DATE первой пары (неделя описывается только номером).
     */
    private fun parsePeriod(per: String?, groupPeriod: String?, infoNode: JsonObject?, daysNode: JsonObject?): LocalDate? {
        periodStart(per)?.let { return it }
        daysStart(infoNode)?.let { return it }
        periodStart(groupPeriod)?.let { return it }

        // Резерв: неделя описана только номером — дату берём из поля DATE любой пары.
        val pairDate = (1..WeekSchedule.DAYS_IN_WEEK)
            .asSequence()
            .flatMap { dayNumber -> pairsOf(daysNode?.get(dayNumber.toString()).asObjectOrNull()).asSequence() }
            .firstNotNullOfOrNull { pair -> pair.trimmed("DATE").takeIf { it.isNotEmpty() }?.let(::parseDateOrNull) }
        if (pairDate == null) return null
        return pairDate.minusDays(pairDate.dayOfWeek.value.toLong() - 1)
    }

    /** Начало периода «dd.MM.yyyy - dd.MM.yyyy» → дата; null, если не разбирается. */
    private fun periodStart(period: String?): LocalDate? =
        period?.substringBefore('-')?.trim()?.let(::parseDateOrNull)

    /**
     * Дата из массива `days[]` первой информации о неделе (словесный формат «02 ноября 2026 г.»):
     * так можно восстановить начало недели, если `per` пришёл пустым. «Сегодня» датой
     * не считается — по нему неизвестно число.
     */
    private fun daysStart(infoNode: JsonObject?): LocalDate? {
        val days = infoNode?.get("days").asArrayOrNull() ?: return null
        val parsed = days.mapNotNull { el ->
            val e = el.asString()?.let(::parseDateOrNull)
            e
        }
        return parsed.firstOrNull()
            ?.let { firstDay -> firstDay.minusDays(firstDay.dayOfWeek.value.toLong() - 1) }
    }

    private fun parseDateOrNull(text: String): LocalDate? = try {
        LocalDate.parse(text.trim().replace('.', '-').replace('/', '-'), DATE_FORMAT)
    } catch (e: DateTimeParseException) {
        try {
            LocalDate.parse(text.trim(), RU_DATE)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /** Строки в ответе приходят с хвостовыми пробелами — значения полей всегда триммим. */
    private fun JsonObject?.trimmed(key: String): String = this?.get(key).asString()?.trim().orEmpty()

    // --- Хелперы для неудобного JSON со строками и разнородными массивами ---

    private fun JsonElement?.asObjectOrNull(): JsonObject? = this as? JsonObject
    private fun JsonElement?.asArrayOrNull(): JsonArray? = this as? JsonArray
    private fun JsonElement?.asString(): String? = (this as? JsonPrimitive)?.content
}

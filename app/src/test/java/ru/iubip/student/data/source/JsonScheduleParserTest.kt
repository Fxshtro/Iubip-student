package ru.iubip.student.data.source

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Разбор ответа сайта: все недели, неполные данные, грязь в строках.
 * Чистый JVM-тест (JUnit4), без Android и Robolectric.
 */
class JsonScheduleParserTest {

    private val group = "К3И1(9)"

    // --- Разбор всех недель ---

    @Test
    fun `parseAll читает обе недели из тестового файла`() {
        val table = JsonScheduleParser.parseAll(testRawSchedule(), group)

        assertEquals(2, table.weeks.size)
        assertEquals(LocalDate.of(2026, 11, 2), table.firstDate)
        assertEquals(LocalDate.of(2026, 11, 15), table.lastDate)
        assertEquals(
            listOf(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 9)),
            table.weeks.map { it.start },
        )
    }

    @Test
    fun `parseAll даёт непрерывный список дат по порядку`() {
        val table = JsonScheduleParser.parseAll(testRawSchedule(), group)

        val dates = table.dates()
        assertEquals(14, dates.size)
        assertEquals(LocalDate.of(2026, 11, 2), dates.first())
        assertEquals(LocalDate.of(2026, 11, 15), dates.last())
        assertEquals(dates.sorted(), dates)
        assertEquals(14, dates.toSet().size)
    }

    @Test
    fun `parseAll недели сортируются по дате начала, даже если в JSON обратный порядок`() {
        val raw = scheduleJson(
            """ "46": [ {"per": "09.11.2026 - 15.11.2026"}, {"1": {"1": [${pairJson("1", "1", "09-11-2026")}]}} ], """ +
                """ "45": [ {"per": "02.11.2026 - 08.11.2026"}, {"1": {"1": [${pairJson("1", "1", "02-11-2026")}]}} ] """,
        )

        val table = JsonScheduleParser.parseAll(raw, group)

        assertEquals(2, table.weeks.size)
        assertEquals(LocalDate.of(2026, 11, 2), table.weeks[0].start)
        assertEquals(LocalDate.of(2026, 11, 9), table.weeks[1].start)
        assertEquals(LocalDate.of(2026, 11, 2), table.firstDate)
    }

    @Test
    fun `parseAll склеивает дубли дат без повторов`() {
        val raw = scheduleJson(
            """ "45": [ {"per": "02.11.2026 - 08.11.2026"}, {"1": {"1": [${pairJson("1", "1", "02-11-2026")}]}} ], """ +
                """ "45copy": [ {"per": "02.11.2026 - 08.11.2026"}, {"1": {"2": [${pairJson("1", "2", "02-11-2026")}]}} ] """,
        )

        val table = JsonScheduleParser.parseAll(raw, group)
        val dates = table.dates()

        // Обе недели описывают 02–08.11: дни не повторяются, остаётся одна неделя.
        assertEquals(1, table.weeks.size)
        assertEquals(7, dates.size)
        assertEquals(dates.toSet().size, dates.size)
        // День остаётся из первой (более ранней) недели — дубль не перетирает данные.
        val monday = table.dayFor(LocalDate.of(2026, 11, 2))
        assertEquals(1, monday?.pairs?.size)
        assertEquals(1, monday?.pairs?.single()?.slot)
    }

    // --- Неполные данные ---

    @Test
    fun `неделя без периода пропускается и не ломает остальные`() {
        val raw = scheduleJson(
            """ "45": [ {"per": "02.11.2026 - 08.11.2026"}, {"1": {"1": [${pairJson("1", "1", "02-11-2026")}]}} ], """ +
                """ "46": [ {"per": ""}, {"1": {"1": [${pairJson("1", "1", "09-11-2026")}]}} ], """ +
                """ "47": [ {"per": "16.11.2026 - 22.11.2026"}, {"1": {"1": [${pairJson("1", "1", "16-11-2026")}]}} ] """,
        )

        val table = JsonScheduleParser.parseAll(raw, group)

        assertEquals(2, table.weeks.size)
        assertEquals(listOf(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 16)), table.weeks.map { it.start })
    }

    @Test
    fun `неделя без периода и без дней восстановлена по периоду группы`() {
        val raw = scheduleJson(
            """ "45": [ {"per": ""}, {"2": {"1": [${pairJson("2", "1", "03-11-2026")}]}} ] """,
        )

        val table = JsonScheduleParser.parseAll(raw, group)

        assertEquals(1, table.weeks.size)
        assertEquals(LocalDate.of(2026, 11, 2), table.weeks.single().start)
    }

    @Test
    fun `неделя без per восстанавливается по словесным датам days`() {
        val raw = scheduleJson(
            """ "45": [ {"per": "", "days": ["02 ноября 2026 г.", "03 ноября 2026 г."]}, """ +
                """ {"1": {"1": [${pairJson("1", "1", "02-11-2026")}]}} ] """,
            groupPeriod = null,
        )

        val table = JsonScheduleParser.parseAll(raw, group)

        assertEquals(1, table.weeks.size)
        assertEquals(LocalDate.of(2026, 11, 2), table.weeks.single().start)
    }

    @Test
    fun `пустая таблица когда недель нет`() {
        val raw = scheduleJson("")

        val table = JsonScheduleParser.parseAll(raw, group)

        assertTrue(table.weeks.isEmpty())
        assertTrue(table.dates().isEmpty())
        assertNull(table.firstDate)
        assertNull(table.lastDate)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parseAll падает на неизвестной группе`() {
        JsonScheduleParser.parseAll(testRawSchedule(), "НЕТ ТАКОЙ ГРУППЫ")
    }

    // --- Совместимость со старым API ---

    @Test
    fun `parse возвращает первую неделю таблицы`() {
        val week = JsonScheduleParser.parse(testRawSchedule(), group)

        assertEquals(LocalDate.of(2026, 11, 2), week.start)
        assertEquals(LocalDate.of(2026, 11, 8), week.end)
        assertEquals(7, week.days.size)
        assertEquals(LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 2).plusDays(7))
    }

    // --- Пары и грязь в значениях ---

    @Test
    fun `значения пар триммятся`() {
        val table = JsonScheduleParser.parseAll(testRawSchedule(), group)
        val pair = table.dayFor(LocalDate.of(2026, 11, 2))!!.pairs.first()

        assertEquals("Информационные технологии", pair.subject)
        assertEquals("лек.", pair.type)
        assertEquals("Смирнова Е.А.", pair.teacher)
        assertEquals("1-305", pair.room)
    }

    @Test
    fun `однозначный день в DATE разбирается`() {
        val raw = scheduleJson(
            """ "46": [ {"per": ""}, {"1": {"1": [${pairJson("1", "1", "9-11-2026")}]}} ] """,
            groupPeriod = null,
        )

        val table = JsonScheduleParser.parseAll(raw, group)

        assertEquals(LocalDate.of(2026, 11, 9), table.weeks.single().start)
    }

    @Test
    fun `пара без предмета или номера не ломает разбор`() {
        val raw = scheduleJson(
            """ "45": [ {"per": "02.11.2026 - 08.11.2026"}, {"1": {"1": [{"LES": "1"}], "2": [{"SUBJECT": "Математика"}]}} ] """,
        )

        val table = JsonScheduleParser.parseAll(raw, group)

        assertEquals(1, table.weeks.size)
        assertTrue(table.dayFor(LocalDate.of(2026, 11, 2))!!.pairs.isEmpty())
    }

    @Test
    fun `воскресенье без ключа дня пустое`() {
        val table = JsonScheduleParser.parseAll(testRawSchedule(), group)

        val sunday = table.dayFor(LocalDate.of(2026, 11, 8))
        assertNotNull(sunday)
        assertTrue(sunday!!.pairs.isEmpty())
        assertTrue(table.hasDate(LocalDate.of(2026, 11, 8)))
    }

    @Test
    fun `даты вне недель не находятся`() {
        val table = JsonScheduleParser.parseAll(testRawSchedule(), group)

        assertFalse(table.hasDate(LocalDate.of(2026, 11, 1)))
        assertNull(table.dayFor(LocalDate.of(2026, 11, 16)))
        assertFalse(table.hasDate(LocalDate.of(2026, 11, 16)))
    }

    // --- Тестовые данные ---

    /** Читает настоящий файл `app/src/main/res/raw/schedule_k3i1_9.json` (unit-тест видит рабочую копию). */
    private fun testRawSchedule(): String =
        java.io.File("src/main/res/raw/schedule_k3i1_9.json").readText()

    private fun pairJson(day: String, les: String, date: String): String = buildString {
        append("""{"GROUP":" К3И1(9)   ",""")
        append(""""DAY":"$day   ","LES":"$les   ","AUD":" 1-305   ","WEEK":"45  ",""")
        append(""""SUBG":"0 ","POST":"- ","NAME":"Смирнова Е.А.   ",""")
        append(""""SUBJECT":"Информационные технологии   ","SUBJ_TYPE":"лек.   ",""")
        append(""""DATE":"$date  ","CAFEDRA":"- ","THEME":"- ","ID":"1 ",""")
        append(""""COURSE":"3 ","FACULTY":"Колледж ","FORMOBUCH":"- ","deleted":0}""")
    }

    /** Собирает ответ в формате сайта вокруг переданных недель. */
    private fun scheduleJson(weeks: String, groupPeriod: String? = "02.11.2026 - 15.11.2026"): String {
        val period = groupPeriod?.let { "\"$it\"," } ?: ""
        return """{"$group": [$period { $weeks }]}"""
    }
}

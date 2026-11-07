package ru.iubip.student.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.iubip.student.data.source.ScheduleSource
import java.time.LocalDate

/**
 * Репозиторий на тестовом файле `res/raw`: старая и новая точки входа дают согласованные данные.
 */
class DefaultScheduleRepositoryTest {

    private val raw = java.io.File("src/main/res/raw/schedule_k3i1_9.json").readText()
    private val repository = DefaultScheduleRepository(source = ScheduleSource { raw })

    @Test
    fun `table отдаёт все выложенные недели`() = runBlocking {
        val table = repository.table("К3И1(9)").getOrThrow()

        assertEquals(2, table.weeks.size)
        assertEquals(LocalDate.of(2026, 11, 2), table.firstDate)
        assertEquals(LocalDate.of(2026, 11, 15), table.lastDate)
    }

    @Test
    fun `schedule отдаёт первую неделю — старый экран не ломается`() = runBlocking {
        val week = repository.schedule("К3И1(9)").getOrThrow()
        val table = repository.table("К3И1(9)").getOrThrow()

        assertEquals(table.weeks.first(), week)
        assertEquals(LocalDate.of(2026, 11, 2), week.start)
        assertEquals(7, week.days.size)
    }

    @Test
    fun `нет данных — failure, а не исключение`() = runBlocking {
        val empty = DefaultScheduleRepository(source = ScheduleSource { null })

        assertTrue(empty.table("К3И1(9)").isFailure)
        assertTrue(empty.schedule("К3И1(9)").isFailure)
    }
}

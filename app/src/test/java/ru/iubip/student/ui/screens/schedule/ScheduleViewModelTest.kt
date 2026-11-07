package ru.iubip.student.ui.screens.schedule

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.iubip.student.data.ScheduleRepository
import ru.iubip.student.data.model.ClassPair
import ru.iubip.student.data.model.Day
import ru.iubip.student.data.source.ScheduleTable
import ru.iubip.student.data.source.WeekSchedule
import java.time.LocalDate
import java.time.LocalTime

/**
 * Неделя и выбранный день живут только в [ScheduleViewModel]. Тест проверяет правила
 * переключения недель: что сбрасывается при свайпе и что сохраняется при выборе даты.
 */
class ScheduleViewModelTest {

    /** Две выложенные недели: 02–08.11 и 09–15.11 (как в тестовом файле). */
    private val table = ScheduleTable(
        group = "К3И1(9)",
        weeks = listOf(
            week(LocalDate.of(2026, 11, 2)),
            week(LocalDate.of(2026, 11, 9)),
        ),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        // viewModelScope всегда на Dispatchers.Main — в JVM-тесте подменяем его.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `старт — неделя с сегодняшним днём и выбранная дата сегодня`() = run {
        val viewModel = viewModel(today = LocalDate.of(2026, 11, 7))

        val state = viewModel.uiState.value
        assertEquals(0, state.weekIndex)
        assertEquals(LocalDate.of(2026, 11, 7), state.selectedDate)
    }

    @Test
    fun `выбор даты из календаря переключает неделю и сохраняет дату`() = run {
        val viewModel = viewModel(today = LocalDate.of(2026, 11, 7))

        viewModel.selectDate(LocalDate.of(2026, 11, 11))

        val state = viewModel.uiState.value
        assertEquals(1, state.weekIndex)
        assertEquals(LocalDate.of(2026, 11, 11), state.selectedDate)
    }

    @Test
    fun `свайп недели сбрасывает день на сегодня или понедельник`() = run {
        val viewModel = viewModel(today = LocalDate.of(2026, 11, 7))

        viewModel.onUserSwipeNext()
        // сегодня 07.11 в неделе 09–15 нет → понедельник
        assertEquals(1, viewModel.uiState.value.weekIndex)
        assertEquals(LocalDate.of(2026, 11, 9), viewModel.uiState.value.selectedDate)

        viewModel.onUserSwipePrevious()
        assertEquals(0, viewModel.uiState.value.weekIndex)
        assertEquals(LocalDate.of(2026, 11, 7), viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `свайп за границей данных не переключает неделю`() = run {
        val viewModel = viewModel(today = LocalDate.of(2026, 11, 7))

        viewModel.onUserSwipePrevious()
        assertEquals(0, viewModel.uiState.value.weekIndex)

        viewModel.onUserSwipeNext()
        viewModel.onUserSwipeNext()
        assertEquals(1, viewModel.uiState.value.weekIndex)
    }

    @Test
    fun `недоступная дата не выбирается`() = run {
        val viewModel = viewModel(today = LocalDate.of(2026, 11, 7))

        viewModel.selectDate(LocalDate.of(2026, 11, 16))

        assertEquals(LocalDate.of(2026, 11, 7), viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `если данные начинаются с будущей недели — открывается первая неделя`() = run {
        val future = ScheduleTable(
            group = "К3И1(9)",
            weeks = listOf(week(LocalDate.of(2026, 12, 7)), week(LocalDate.of(2026, 12, 14))),
        )
        val viewModel = ScheduleViewModel(
            repository = FakeRepository(future),
            group = "К3И1(9)",
            today = { LocalDate.of(2026, 11, 7) },
            initialState = loadedState(future, LocalDate.of(2026, 11, 7)),
        )

        val state = viewModel.uiState.value
        assertEquals(0, state.weekIndex)
        assertEquals(LocalDate.of(2026, 12, 7), state.selectedDate)
        assertFalse(state.canGoPrevious)
        assertTrue(state.canGoNext)
    }

    // --- Хелперы ---

    private fun viewModel(today: LocalDate): ScheduleViewModel = ScheduleViewModel(
        repository = FakeRepository(table),
        group = "К3И1(9)",
        today = { today },
        initialState = loadedState(table, today),
    )

    /** Состояние сразу после загрузки таблицы — так же, как его соберёт [ScheduleViewModel.load]. */
    private fun loadedState(table: ScheduleTable, today: LocalDate): ScheduleUiState {
        val index = ScheduleViewModel.initialWeekIndex(table, today)
        return ScheduleUiState(
            group = "К3И1(9)",
            table = table,
            weekIndex = index,
            selectedDate = ScheduleViewModel.initialDate(table.weeks[index], today),
            isLoading = false,
        )
    }

    private fun week(start: LocalDate): WeekSchedule = WeekSchedule(
        group = "К3И1(9)",
        start = start,
        days = (0..6).map { index ->
            val date = start.plusDays(index.toLong())
            Day(date = date, pairs = if (index < 6) listOf(PAIR) else emptyList())
        },
    )

    private class FakeRepository(private val table: ScheduleTable) : ScheduleRepository {
        override suspend fun schedule(group: String): Result<WeekSchedule> =
            Result.success(table.weeks.first())

        override suspend fun table(group: String): Result<ScheduleTable> = Result.success(table)
    }

    private companion object {
        val PAIR = ClassPair(
            subject = "Математика",
            type = "пр.",
            teacher = "Кузнецов Д.С.",
            room = "2-118",
            slot = 1,
            start = LocalTime.of(8, 30),
            end = LocalTime.of(10, 5),
        )
    }
}

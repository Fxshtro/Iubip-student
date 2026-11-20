package ru.iubip.student.ui.screens.schedule

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.iubip.student.data.ScheduleRepository
import ru.iubip.student.data.source.ScheduleTable
import ru.iubip.student.data.source.WeekSchedule
import java.time.LocalDate

/** Проверка нового метода пульта [ScheduleViewModel.selectGroup]. */
class ScheduleViewModelSelectGroupTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selectGroup меняет шапку и перезагружает расписание выбранной группы`() = run {
        val repository = RecordingRepository()
        // Уже загруженное состояние: init не вызывает load(), проверяем только selectGroup.
        val loaded = ScheduleTable(
            group = "К3И1(9)",
            weeks = listOf(
                ru.iubip.student.data.source.WeekSchedule(
                    group = "К3И1(9)",
                    start = LocalDate.of(2026, 11, 2),
                    days = emptyList(),
                ),
            ),
        )
        val viewModel = ScheduleViewModel(
            repository = repository,
            group = "К3И1(9)",
            today = { LocalDate.of(2026, 11, 7) },
            initialState = ScheduleUiState(
                group = "К3И1(9)",
                table = loaded,
                isLoading = false,
            ),
        )

        viewModel.selectGroup("К2И1(11),К3И1(9)")

        assertEquals("К2И1(11),К3И1(9)", viewModel.uiState.value.group)
        assertEquals(listOf("К2И1(11),К3И1(9)"), repository.requestedGroups)
    }

    private class RecordingRepository : ScheduleRepository {
        val requestedGroups = mutableListOf<String>()

        override suspend fun schedule(group: String): Result<WeekSchedule> =
            Result.failure(IllegalStateException("not used"))

        override suspend fun table(group: String): Result<ScheduleTable> {
            requestedGroups += group
            return Result.success(ScheduleTable.empty(group))
        }
    }
}

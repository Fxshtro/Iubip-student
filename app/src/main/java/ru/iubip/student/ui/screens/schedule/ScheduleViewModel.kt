package ru.iubip.student.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.iubip.student.data.CalendarAvailability
import ru.iubip.student.data.DayAvailability
import ru.iubip.student.data.ScheduleRepository
import ru.iubip.student.data.ServiceLocator
import ru.iubip.student.data.model.Day
import ru.iubip.student.data.source.ScheduleTable
import ru.iubip.student.data.source.WeekSchedule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Состояние экрана «Расписание».
 *
 * Экран листается неделями: [weekIndex] — страница ленты (индекс недели в [table]),
 * [selectedDate] — выбранный день внутри этой недели.
 *
 * @param group группа в шапке (зашита, выбор группы — следующий этап)
 * @param table все выложенные недели группы (пустая таблица, пока идёт загрузка)
 * @param weekIndex индекс текущей недели в `table.weeks` (0..weeks.size-1)
 * @param selectedDate выбранный день (всегда внутри текущей недели)
 * @param isLoading идёт загрузка
 * @param errorMessage текст ошибки для показа (null — ошибки нет)
 */
data class ScheduleUiState(
    val group: String,
    val table: ScheduleTable = ScheduleTable.empty(group),
    val weekIndex: Int = 0,
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    /** Недели таблицы — страницы ленты. */
    val weeks: List<WeekSchedule> get() = table.weeks

    /** Текущая (показанная) неделя или null, пока данных нет. */
    val currentWeek: WeekSchedule? get() = weeks.getOrNull(weekIndex)

    /** Дни текущей недели Пн..Вс; пустой список, пока данных нет. */
    val days: List<Day> get() = currentWeek?.days.orEmpty()

    /** Выбранный день или null, пока данных нет. */
    val selectedDay: Day?
        get() = selectedDate?.let { date -> days.firstOrNull { it.date == date } }

    /**
     * Можно ли выбрать дату: она в таблице и попадает на показанную неделю.
     * Недоступные даты (неделя удалена / ещё не выложена) не выбираются.
     */
    fun canSelect(date: LocalDate): Boolean =
        CalendarAvailability.isSelectable(date, table) && currentWeek?.hasDate(date) == true

    /** Состояние выбранного дня по правилам календаря. */
    val selectedAvailability: DayAvailability
        get() = selectedDate?.let { CalendarAvailability.stateFor(it, table) }
            ?: DayAvailability.UNAVAILABLE

    /** Есть ли следующая неделя (свайп влево). */
    val canGoNext: Boolean get() = weekIndex < weeks.lastIndex

    /** Есть ли предыдущая неделя (свайп вправо). */
    val canGoPrevious: Boolean get() = weekIndex > 0
}

/**
 * Хранит состояние расписания и адресуется к [ScheduleRepository].
 * Экран не знает, откуда данные: `res/raw` сейчас, сеть — позже.
 *
 * @param repository источник расписания
 * @param group группа, расписание которой показывается
 * @param today текущая дата (в тестах подменяется)
 * @param initialState начальное состояние (только для тестов: подставляет загруженную
 *   таблицу вместо асинхронной загрузки из репозитория)
 */
class ScheduleViewModel(
    private val repository: ScheduleRepository,
    private val group: String,
    private val today: () -> LocalDate = { LocalDate.now() },
    initialState: ScheduleUiState = ScheduleUiState(group = group),
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)

    /** Поток состояния для Compose. */
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        // Уже есть загруженная таблица (тест) — не трогаем репозиторий.
        if (initialState.weeks.isEmpty()) load()
    }

    /** Загружает все выложенные недели (используется и для повтора после ошибки). */
    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.table(group)
                .onSuccess { table ->
                    val now = today()
                    val index = initialWeekIndex(table, now)
                    val date = table.weeks.getOrNull(index)?.let { week -> initialDate(week, now) }
                    _uiState.update { state ->
                        state.copy(
                            table = table,
                            weekIndex = index,
                            selectedDate = date,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(isLoading = false, errorMessage = it.message ?: "")
                    }
                }
        }
    }

    /**
     * Выбирает дату (из календаря или из ленты дней).
     *
     * Номер недели вычисляется по дате, поэтому лента переключается на нужную неделю
     * в том же обновлении состояния. Недоступную дату (нет в таблице) игнорирует.
     */
    fun selectDate(date: LocalDate) {
        _uiState.update { state ->
            val weekIndex = state.weeks.indexOfFirst { it.hasDate(date) }
            if (weekIndex < 0) state else state.copy(weekIndex = weekIndex, selectedDate = date)
        }
    }

    /** Выбирает день в показанной неделе по индексу 0..6 (0 — понедельник). */
    fun selectDay(index: Int) {
        if (index !in DAY_INDICES) return
        val date = _uiState.value.currentWeek?.dateOf(index) ?: return
        selectDate(date)
    }

    /**
     * Программный переход на неделю (календарь, кнопки): неделя и день меняются вместе.
     * День — выбранная дата, если она попадает в эту неделю, иначе сегодня/понедельник.
     */
    fun goToWeek(index: Int) {
        _uiState.update { state ->
            if (index !in state.weeks.indices || index == state.weekIndex) {
                state
            } else {
                val week = state.weeks[index]
                val selected = state.selectedDate?.takeIf { week.hasDate(it) }
                    ?: initialDate(week, today())
                state.copy(weekIndex = index, selectedDate = selected)
            }
        }
    }

    /**
     * Лента дней доведена пользователем свайпом до другой недели.
     * Выбор дня сбрасывается: сегодня, если оно в новой неделе, иначе понедельник.
     * Вызывается ТОЛЬКО по жесту — программная прокрутка ленты ничего не шлёт.
     */
    fun onUserSwipeToWeek(weekIndex: Int) {
        _uiState.update { state ->
            if (weekIndex !in state.weeks.indices || weekIndex == state.weekIndex) {
                state
            } else {
                state.copy(
                    weekIndex = weekIndex,
                    selectedDate = initialDate(state.weeks[weekIndex], today()),
                )
            }
        }
    }

    /** Свайп влево — следующая неделя; за границей данных ничего не делает. */
    fun onUserSwipeNext() = onUserSwipeToWeek(_uiState.value.weekIndex + 1)

    /** Свайп вправо — предыдущая неделя; за границей данных ничего не делает. */
    fun onUserSwipePrevious() = onUserSwipeToWeek(_uiState.value.weekIndex - 1)

    companion object {
        private val DAY_INDICES = 0..6

        /** 0 — понедельник, …, 6 — воскресенье. */
        fun weekdayIndex(date: LocalDate): Int = date.dayOfWeek.value - 1

        /** Понедельник недели, к которой относится дата. */
        fun weekStart(date: LocalDate): LocalDate =
            date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        /**
         * Неделя на старте: та, где идёт сегодня, иначе ближайшая опубликованная
         * (первая неделя в будущем, а если такой нет — последняя прошедшая).
         */
        fun initialWeekIndex(table: ScheduleTable, now: LocalDate): Int {
            if (table.weeks.isEmpty()) return 0
            val index = table.weeks.indexOfFirst { it.hasDate(now) }
            if (index >= 0) return index
            return table.weeks.indexOfFirst { it.start.isAfter(now) }.takeIf { it >= 0 }
                ?: table.weeks.lastIndex
        }

        /** Стартовый день недели: сегодня, если оно в этой неделе, иначе понедельник. */
        fun initialDate(week: WeekSchedule, now: LocalDate): LocalDate =
            if (week.hasDate(now)) now else week.start

        /**
         * Фабрика ViewModel без Hilt: репозиторий берётся из [ServiceLocator].
         *
         * @param group группа, расписание которой показывается
         */
        fun factory(applicationContext: android.content.Context, group: String): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ScheduleViewModel(
                        repository = ServiceLocator.scheduleRepository(applicationContext),
                        group = group,
                    )
                }
            }
    }
}

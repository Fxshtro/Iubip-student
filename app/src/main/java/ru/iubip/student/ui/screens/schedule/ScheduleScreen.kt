package ru.iubip.student.ui.screens.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.SheetValue
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ru.iubip.student.R
import ru.iubip.student.data.model.ClassPair
import ru.iubip.student.data.model.Day
import ru.iubip.student.data.source.ScheduleTable
import ru.iubip.student.data.source.WeekSchedule
import ru.iubip.student.data.ServiceLocator
import ru.iubip.student.ui.components.ContentColumn
import ru.iubip.student.ui.components.currentWindowWidthDp
import ru.iubip.student.ui.components.IconCircleButton
import ru.iubip.student.ui.components.IubipBreakpoints
import ru.iubip.student.ui.components.IubipWindowInfo
import ru.iubip.student.ui.components.toIubipWindowInfo
import ru.iubip.student.ui.components.ScreenHeader
import ru.iubip.student.ui.icons.PhosphorIcons
import ru.iubip.student.ui.screens.schedule.calendar.CalendarColumn
import ru.iubip.student.ui.screens.schedule.calendar.CalendarSheetContent
import ru.iubip.student.ui.screens.schedule.calendar.buildCalendarModel
import ru.iubip.student.ui.theme.IubipAppTheme
import ru.iubip.student.ui.theme.IubipShapes
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Верхний зазор шапки в портрете: воздух от системной строки до заголовка. */
private val PORTRAIT_HEADER_TOP_GAP = 16.dp


/**
 * Экран «Расписание».
 *
 * Экран не знает источник данных: состояние приходит из [ScheduleViewModel],
 * который обращается к `ScheduleRepository`. Справа от названия группы — кнопка
 * календаря; лента дней листается неделями.
 *
 * @param windowWidthSizeClass класс ширины окна — от него зависит раскладка
 */
@Composable
fun ScheduleScreen(
    windowWidthSizeClass: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cachedGroup = remember { ServiceLocator.initialGroup(context.applicationContext) }
    val group = stringResource(R.string.schedule_group)
    val startGroup = if (cachedGroup == ServiceLocator.START_GROUP) group else cachedGroup
    val viewModel: ScheduleViewModel = viewModel(
        key = startGroup,
        factory = ScheduleViewModel.factory(context.applicationContext, startGroup),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val window = windowWidthSizeClass.toIubipWindowInfo()
        .let { IubipWindowInfo.of(currentWindowWidthDp()) }

    // Плавающая кнопка — ОТДЕЛЬНЫЙ слой поверх экрана, не часть вёрстки контента.
    // Переключатель источника включается ТОЛЬКО здесь (при реальном выборе группы).
    Box(modifier = modifier.fillMaxSize()) {
        ScheduleContent(
            state = state,
            // Календарь-колонка решается по РЕАЛЬНОЙ ширине окна: класс `Expanded`
            // начинается с 840 dp, и телефон в горизонтали попадает туда же.
            window = window,
            onDateClick = viewModel::selectDate,
            onUserSwipeNext = viewModel::onUserSwipeNext,
            onUserSwipePrevious = viewModel::onUserSwipePrevious,
            onRetry = viewModel::load,
            freshnessDetails = viewModel.freshnessDetails(),
        )
        GroupPickerOverlay(
            navigationOnSide = window.isWide,
            onGroupSelected = { name ->
                ServiceLocator.useRemote = true
                viewModel.selectGroup(
                    name,
                    ServiceLocator.cachedScheduleRepository(context.applicationContext),
                )
            },
        )
    }
}

/**
 * Контент экрана без привязки к ViewModel — удобно для превью.
 *
 * Раскладка зависит от ширины окна ([IubipBreakpoints]):
 * - < 600 dp — как на телефоне в портрете: шапка, лента, список;
 * - ≥ 600 dp — компактная (плотная) шапка и карточки пар;
 * - ≥ 900 dp — календарь постоянной колонкой справа от списка, кнопка календаря в шапке
 *   скрыта (лист в этом режиме не нужен). Уже 900 dp — календарь открывается листом.
 *
 * Ширина контента ограничена [IubipBreakpoints.CONTENT_MAX_WIDTH] и центрируется
 * ([ContentColumn]). Список пар скроллится сам, шапка и лента остаются на месте.
 *
 * @param state состояние экрана
 * @param window ширина окна и производные признаки
 * @param onDateClick выбор даты в ленте или календаре
 * @param onUserSwipeNext свайп ленты влево — следующая неделя
 * @param onUserSwipePrevious свайп ленты вправо — предыдущая неделя
 * @param onRetry повтор загрузки после ошибки
 * @param freshnessDetails данные для диалога индикатора актуальности
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleContent(
    state: ScheduleUiState,
    window: IubipWindowInfo,
    onDateClick: (LocalDate) -> Unit,
    onUserSwipeNext: () -> Unit,
    onUserSwipePrevious: () -> Unit,
    onRetry: () -> Unit,
    freshnessDetails: FreshnessDetails = FreshnessDetails(state.freshness, state.group, null),
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val today = LocalDate.now()
    val compact = window.isWide

    // Дата, выбранная в календаре, применяется к экрану, когда лист уже уехал вниз.
    // Порядок важен: лист монтирует свой `rememberSaveable`-состояние (`show()`), а если
    // сначала поменять выбранный день, лента могла бы пересоздаться под открытым листом.
    val pendingDate = remember { mutableStateOf<LocalDate?>(null) }

    fun closeCalendar(selected: LocalDate? = null) = scope.launch {
        if (sheetState.isVisible) sheetState.hide()
        (selected ?: pendingDate.value)?.let { date ->
            onDateClick(date)
        }
        pendingDate.value = null
    }

    val calendarInColumn = window.isExtraWide && state.weeks.isNotEmpty()
    ContentColumn(
        modifier = modifier,
        // Две зоны (пары + календарь) шире одной колонки: им нужен больший лимит.
        maxWidth = if (calendarInColumn) {
            IubipBreakpoints.CONTENT_MAX_WIDTH_WITH_COLUMN
        } else {
            IubipBreakpoints.CONTENT_MAX_WIDTH
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val noData = state.weeks.isEmpty()

            if (state.isLoading && noData) {
                ScreenHeader(
                    title = state.group,
                    subtitle = stringResource(R.string.schedule_loading),
                    action = {
                        FreshnessIndicator(state.freshness, freshnessDetails)
                        CalendarButton(visible = false, onClick = {})
                    },
                    compact = compact,
                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                return@Column
            }

            if (state.errorMessage != null && noData) {
                ScreenHeader(
                    title = state.group,
                    subtitle = stringResource(R.string.schedule_error),
                    action = {
                        FreshnessIndicator(state.freshness, freshnessDetails)
                        CalendarButton(visible = false, onClick = {})
                    },
                    compact = compact,
                )
                CenteredMessage(
                    icon = PhosphorIcons.Regular.X,
                    title = stringResource(R.string.schedule_error),
                    action = {
                        Button(onClick = onRetry) {
                            Text(text = stringResource(R.string.schedule_retry))
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                return@Column
            }

            ScreenHeader(
                title = state.group,
                // В режиме календаря-колонки дата уходит вправо в строку с названием группы:
                // ленты дней нет, дату пользователь видит в самом календаре.
                subtitle = if (calendarInColumn) null else dayCaption(state.selectedDate),
                inlineSubtitle = if (calendarInColumn) dayCaption(state.selectedDate) else null,
                maxSubtitleLines = 1,
                action = {
                    FreshnessIndicator(state.freshness, freshnessDetails)
                    // Календарь уже на экране колонкой — лист ему не нужен.
                    CalendarButton(
                        visible = !calendarInColumn,
                        onClick = { scope.launch { sheetState.show() } },
                    )
                },
                compact = compact,
                topGap = if (compact) null else PORTRAIT_HEADER_TOP_GAP,
            )
            // Лента дней нужна только там, где календаря на экране нет: выбор даты
            // уже есть в календаре-колонке, а высота экрана дорогая.
            if (!calendarInColumn) {
                WeekDayStrip(
                    weeks = state.weeks,
                    weekIndex = state.weekIndex,
                    selectedDate = state.selectedDate,
                    today = today,
                    onUserSwipeNext = onUserSwipeNext,
                    onUserSwipePrevious = onUserSwipePrevious,
                    onDateClick = onDateClick,
                    modifier = Modifier.padding(bottom = if (compact) 12.dp else 16.dp),
                )
            }

            if (calendarInColumn) {
                CalendarWithPairs(
                    state = state,
                    today = today,
                    compact = compact,
                    onDateClick = onDateClick,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                PairList(
                    pairs = state.selectedDay?.pairs.orEmpty(),
                    availability = state.selectedAvailability,
                    compact = compact,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    val model = remember(state.table, state.isLoading, state.errorMessage, state.selectedDate, today) {
        buildCalendarModel(
            table = state.table,
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            selectedDate = state.selectedDate,
            today = today,
        )
    }

    // Лист календаря. В исходном (Hidden) состоянии ModalBottomSheet всё равно рисует
    // пустой каркас поверх контента, поэтому он монтируется только в Show/Expanded.
    // При ширине ≥ 900 dp календарь постоянной колонкой и лист не открывается.
    val sheetShown = !window.isExtraWide && (
        sheetState.currentValue != SheetValue.Hidden ||
            sheetState.targetValue != SheetValue.Hidden
        )
    if (sheetShown) {
        key(state.selectedDate) {
            ModalBottomSheet(
                onDismissRequest = { closeCalendar() },
                sheetState = sheetState,
                shape = IubipShapes.Panel,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                CalendarSheetContent(
                    model = model,
                    onDateSelected = { date -> pendingDate.value = date; closeCalendar() },
                    onDismiss = { closeCalendar() },
                )
            }
        }
    }
}

/**
 * Широкий режим (≥ 900 dp): слева — пары выбранного дня, справа — постоянная колонка
 * календаря с общим отступом 20 dp. Календарь получает то же событие выбора даты,
 * что и лист, — источник истины остаётся в ViewModel.
 */
@Composable
private fun CalendarWithPairs(
    state: ScheduleUiState,
    today: LocalDate,
    compact: Boolean,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val model = remember(state.table, state.isLoading, state.errorMessage, state.selectedDate, today) {
        buildCalendarModel(
            table = state.table,
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            selectedDate = state.selectedDate,
            today = today,
        )
    }
    Row(
        modifier = modifier.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        val pairs = state.selectedDay?.pairs.orEmpty()
        val padding = PaddingValues(bottom = 24.dp)
        if (state.selectedAvailability.isSelectable && pairs.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(pairs, key = { "${it.slot}-${it.subject}" }) { pair ->
                    PairCard(pair = pair, compact = compact)
                }
            }
        } else if (state.selectedAvailability.isSelectable) {
            EmptyDay(modifier = Modifier.weight(1f))
        } else {
            CenteredMessage(
                icon = PhosphorIcons.Regular.CalendarDots,
                title = stringResource(R.string.schedule_not_published),
                subtitle = stringResource(R.string.schedule_not_published_hint),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            )
        }
        CalendarColumn(
            model = model,
            onDateSelected = onDateClick,
            modifier = Modifier.padding(bottom = 24.dp),
        )
    }
}

/** Кнопка календаря в шапке: скрыта, пока данных нет (нажимать нечего). */
@Composable
private fun CalendarButton(visible: Boolean, onClick: () -> Unit) {
    if (!visible) return
    IconCircleButton(
        icon = PhosphorIcons.Regular.CalendarDots,
        contentDescription = stringResource(R.string.cd_open_calendar),
        onClick = onClick,
    )
}

/**
 * Список пар выбранного дня.
 *
 * День без пар — «Пар нет». Если выбранной даты нет в выложенном расписании (теоретически
 * не должно случаться, но бывает, когда данные начинаются с будущей недели) — нейтральное
 * «Расписание на эту дату не выложено».
 */
@Composable
private fun PairList(
    pairs: List<ClassPair>,
    availability: ru.iubip.student.data.DayAvailability,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!availability.isSelectable) {
        CenteredMessage(
            icon = PhosphorIcons.Regular.CalendarDots,
            title = stringResource(R.string.schedule_not_published),
            subtitle = stringResource(R.string.schedule_not_published_hint),
            modifier = modifier.padding(horizontal = 32.dp),
        )
        return
    }
    if (pairs.isEmpty()) {
        EmptyDay(modifier = modifier)
        return
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(pairs, key = { "${it.slot}-${it.subject}" }) { pair ->
            PairCard(pair = pair, compact = compact)
        }
    }
}

/** Пустой день: иконка и «Пар нет» по центру. */
@Composable
private fun EmptyDay(modifier: Modifier = Modifier) {
    CenteredMessage(
        icon = PhosphorIcons.Regular.CalendarDots,
        title = stringResource(R.string.schedule_no_pairs),
        subtitle = stringResource(R.string.schedule_no_pairs_hint),
        modifier = modifier.padding(horizontal = 32.dp),
    )
}

@Composable
private fun CenteredMessage(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        action?.invoke()
    }
}

/** Подпись дня в шапке: «Сегодня, Пятница, 06 ноября» / «Вторник, 03 ноября». */
@Composable
private fun dayCaption(date: LocalDate?): String {
    if (date == null) return ""
    val weekday = stringResource(date.dayOfWeek.fullNameRes())
    val formatted = dayDateFormat.format(date)
    val base = stringResource(R.string.schedule_day_caption, weekday, formatted)
    return if (date == LocalDate.now()) {
        stringResource(R.string.schedule_today_caption, base)
    } else {
        base
    }
}

/**
 * «06 ноября» — единый формат подписи дня на экране.
 * Локаль фиксирована: интерфейс русский, независимо от языка системы.
 */
private val dayDateFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMMM", Locale.forLanguageTag("ru"))

// ------------------------------------------------------------------------- Превью

private val previewPairs: List<ClassPair> = listOf(
    ClassPair(
        subject = "Информационные технологии",
        type = "лек.",
        teacher = "Смирнова Е.А.",
        room = "1-305",
        slot = 1,
        start = LocalTime.of(8, 30),
        end = LocalTime.of(10, 5),
    ),
    ClassPair(
        subject = "Математика",
        type = "пр.",
        teacher = "Кузнецов Д.С.",
        room = "2-118",
        slot = 2,
        start = LocalTime.of(10, 15),
        end = LocalTime.of(11, 50),
    ),
)

@Preview(name = "Расписание — портрет", showBackground = true, heightDp = 720)
@Composable
private fun ScheduleContentPreview() {
    PreviewScheduleContent(widthDp = 393)
}

@Preview(name = "Расписание — горизонталь", showBackground = true, widthDp = 914, heightDp = 411)
@Composable
private fun ScheduleContentLandscapePreview() {
    PreviewScheduleContent(widthDp = 914)
}

@Preview(name = "Расписание — планшет", showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun ScheduleContentTabletPreview() {
    PreviewScheduleContent(widthDp = 1280)
}

@Composable
private fun PreviewScheduleContent(widthDp: Int) {
    val monday = LocalDate.of(2026, 11, 2)
    val days = (0 until 7).map { offset ->
        Day(
            date = monday.plusDays(offset.toLong()),
            pairs = if (offset <= 1) previewPairs else emptyList(),
        )
    }
    val table = ScheduleTable(
        group = "К3И1(9)",
        weeks = listOf(WeekSchedule(group = "К3И1(9)", start = monday, days = days)),
    )
    IubipAppTheme {
        ScheduleContent(
            state = ScheduleUiState(
                group = "К3И1(9)",
                table = table,
                weekIndex = 0,
                selectedDate = monday.plusDays(1),
                isLoading = false,
            ),
            window = IubipWindowInfo(windowWidthDp = widthDp.dp),
            onDateClick = {},
            onUserSwipeNext = {},
            onUserSwipePrevious = {},
            onRetry = {},
        )
    }
}

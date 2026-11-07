# AGENTS.md — IubipApp

Android-приложение (студенческий проект ИУБиП). Текущий этап: экран «Расписание»
работает на локальных тестовых данных (JSON в `res/raw`), экран «О себе» — на тестовых
данных профиля; оба оформлены в дизайне с акцентом #0F48AD. Сеть, кеш, выбор группы — следующие этапы.

> **Статус данных:** тестовые пары сгенерированы (2 недели, прошлая удалена намеренно), группа К3И1(9) — в шапке;
> реальные данные не подключены. Календарь выбора даты и листание неделями — СДЕЛАНО (см. «Календарь и недели»).
> Профиль (`ProfileSample.kt`) — тестовые ФИО и поля, источником позже станет личный кабинет Moodle.

## Стек и версии

| Что | Версия / значение |
|---|---|
| Язык | Kotlin 2.1.0 |
| UI | Jetpack Compose (Material 3), Compose BOM 2025.02.00 |
| Навигация | Navigation Compose 2.8.7 |
| ViewModel | `androidx.lifecycle:lifecycle-viewmodel-compose` 2.8.7 |
| Тесты ViewModel | kotlinx-coroutines-test 1.9.0 (`testImplementation`, подмена `Dispatchers.Main`) |
| Сериализация | kotlinx.serialization (plugin `org.jetbrains.kotlin.plugin.serialization`), `kotlinx-serialization-json` 1.8.0 |
| Android Gradle Plugin | 8.9.1 |
| Gradle | 8.14.3 (wrapper, `distributionType=bin`) |
| JDK / JVM target | JDK 21 для сборки, `jvmTarget = 17` |
| compileSdk / targetSdk | 36 / 36 |
| minSdk | 26 |
| Пакет / applicationId / namespace | `ru.iubip.student` |
| rootProject.name | `IubipApp` |

## Структура каталогов

```
.
├── AGENTS.md
├── settings.gradle.kts        # rootProject.name = "IubipApp", модуль :app
├── build.gradle.kts           # плагины объявлены с apply false (в т.ч. kotlin.serialization)
├── gradle/libs.versions.toml  # единый источник версий (version catalog)
├── gradle.properties          # AndroidX, nonTransitiveRClass, jvmargs
├── gradlew / gradlew.bat
├── local.properties           # sdk.dir — НЕ коммитится (.gitignore)
└── app/
    ├── build.gradle.kts       # android { }, compileOptions, kotlinOptions, Compose, зависимости
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml            # одна Activity: .MainActivity (launcher)
        ├── java/ru/iubip/student/
        │   ├── MainActivity.kt            # ComponentActivity + enableEdgeToEdge + IubipApp()
        │   ├── navigation/
        │   │   └── BottomDestination.kt   # Routes ("schedule", "profile") + enum пунктов меню
        │   ├── data/                      # слой данных (экран его не знает)
        │   │   ├── ScheduleRepository.kt          # interface: schedule(group) + table(group)
        │   │   ├── DefaultScheduleRepository.kt   # источник → JSON → парсер
        │   │   ├── CalendarAvailability.kt        # доступность дат для календаря (чистые функции)
        │   │   ├── BellSchedule.kt                # ТАБЛИЦА ЗВОНОКОВ (единственное место)
        │   │   ├── ServiceLocator.kt              # ручной DI: репозиторий по Context
        │   │   ├── model/Schedule.kt              # data class Day, data class ClassPair
        │   │   └── source/
        │   │       ├── ScheduleSource.kt          # fun interface: строку JSON → suspend load(group)
        │   │       ├── RawScheduleSource.kt       # реализация: чтение res/raw
        │   │       ├── WeekSchedule.kt            # неделя: группа + понедельник + 7 дней
        │   │       ├── ScheduleTable.kt           # ВСЕ недели группы (таблица, dates/dayFor)
        │   │       └── JsonScheduleParser.kt      # разбор ответа сайта: parse(одна) / parseAll(все)
        │   └── ui/
        │       ├── components/
        │       │   ├── ScreenHeader.kt            # ScreenHeader (заголовок+подпись+слот action), MetaRow
        │       │   ├── IconCircleButton.kt        # круглая иконка-кнопка 48 dp без ряби (шапка, панели)
        │       │   ├── IubipBottomBar.kt          # нижняя панель: без заливки, разделитель, таблетка
        │       │   ├── ProfileAvatarPlaceholder.kt # круг-аватарка с силуэтом-заглушкой
        │       │   └── ProfileFieldRow.kt         # ProfileFieldRow, ProfileFieldsCard
        │       ├── icons/
        │       │   └── PhosphorIcons.kt           # ImageVector-иконки Phosphor (thin/regular/fill)
        │       ├── screens/
        │       │   ├── profile/
        │       │   │   ├── ProfileScreen.kt       # экран «О себе» + @Preview
        │       │   │   └── ProfileSample.kt       # Profile + тестовые данные SampleProfile
        │       │   └── schedule/
        │       │       ├── ScheduleScreen.kt      # ScheduleScreen + ScheduleContent (шапка+кнопка календаря, лента, список, лист)
        │       │       ├── ScheduleViewModel.kt   # ScheduleUiState (StateFlow): таблица, неделя, дата; load/selectDate/goToWeek/onUserSwipe*
        │       │       ├── WeekDayStrip.kt        # лента недели Пн..Вс на HorizontalPager, одна страница = неделя
        │       │       ├── PairCard.kt            # карточка пары + formatRange()
        │       │       └── calendar/              # лист календаря
        │       │           ├── CalendarSheet.kt        # CalendarSheetContent (заголовок, стрелки, пейджер месяцев)
        │       │           ├── CalendarComponents.kt   # MonthArrowButton, MonthWeekdayHeader, DayCell, WeekRow, легенда
        │       │           ├── CalendarDays.kt         # подписи дней недели и описание даты для диктора
        │       │           └── CalendarModel.kt        # CalendarScreenModel/CalendarPage, месяцы и ячейки (без Compose)
        │       └── theme/                         # Color.kt, Theme.kt, Type.kt, Shape.kt
        ├── test/java/ru/iubip/student/          # JUnit4 unit-тесты (без Android)
        │   ├── data/CalendarAvailabilityTest.kt       # состояния дат, границы ноября 2026
        │   ├── data/DefaultScheduleRepositoryTest.kt  # table() и schedule() на тестовом файле
        │   ├── data/source/JsonScheduleParserTest.kt  # разбор всех недель, неполные данные
        │   └── ui/screens/schedule/ScheduleViewModelTest.kt # неделя/дата в VM: выбор, свайп, границы (34 теста всего)
        └── res/
            ├── raw/schedule_k3i1_9.json   # тестовое расписание К3И1(9): ДВЕ недели, формат сайта
            ├── values/strings.xml         # ВСЕ строки интерфейса (ru)
            ├── values/colors.xml          # цвет фона иконки лаунчера (#0F48AD)
            ├── values/themes.xml          # Theme.IubipApp (parent android:Theme.Material.Light.NoActionBar)
            ├── values-night/themes.xml    # тёмный вариант темы окна
            ├── drawable/ic_launcher_foreground.xml
            ├── drawable/ic_phosphor_*.xml # иконки Phosphor + caret-left/right/down, x (календарь)
            └── mipmap-anydpi-v26/ic_launcher.xml   # adaptive icon
```

## Слой данных

- **Модели.** `Day(date: LocalDate, pairs: List<ClassPair>)` и
  `ClassPair(subject, type, teacher, room, slot, start: LocalTime, end: LocalTime)`.
  Тип занятия — **строка** (`«лек.»`, `«пр.»`), не enum: новые типы не требуют правок кода.
  Время начала/конца уже посчитано по таблице звонков — экран форматирует только `HH:mm`.
- **Таблица звонков** — `object BellSchedule` (`data/BellSchedule.kt`), единственное место
  с расписанием звонков. `BellSchedule.bellFor(slot)` / `BellSchedule.bells`:
  1) 08:30–10:05, 2) 10:15–11:50, 3) 12:00–13:35, 4) 14:15–15:50, 5) 16:00–17:35,
  6) 17:45–19:20 — **уточнить в деканате**.
- **Источник за интерфейсом.** `interface ScheduleRepository`:
  `suspend fun schedule(group): Result<WeekSchedule>` — ОДНА неделя (её использует старый экран);
  `suspend fun table(group): Result<ScheduleTable>` — ВСЕ выложенные недели (источник для календаря).
  Экран и ViewModel видят только его. Реализация `DefaultScheduleRepository(source: ScheduleSource)`
  берёт строку JSON из `ScheduleSource` и отдаёт в `JsonScheduleParser.parse` / `.parseAll`.
  `RawScheduleSource` читает `res/raw/schedule_k3i1_9.json` (IO-диспатчер, `readBytes`).
  Для сети: новый класс `ScheduleSource` + передать его в `DefaultScheduleRepository` в `ServiceLocator`.
- **DI без Hilt.** `object ServiceLocator.scheduleRepository(context)` — единственное место
  сборки графа; ViewModel создаётся `ScheduleViewModel.factory(context, group)` через `viewModelFactory`.
- **Модель «все недели» — `ScheduleTable`** (`data/source/ScheduleTable.kt`):
  `ScheduleTable(group, weeks: List<WeekSchedule>)` — упорядоченные по дате начала недели без дублей дат.
  Методы: `dates()` (все даты по порядку), `dayFor(date)`, `hasDate(date)`, `firstDate`, `lastDate`,
  `ScheduleTable.of(group, weeks)` (сортировка + склейка дублей), `ScheduleTable.empty(group)`.
  `WeekSchedule` не менялся по смыслу, добавлены `dayFor(date)`, `hasDate(date)`, `end` (воскресенье).
- **Разбор — `JsonScheduleParser`**: `parse(raw, group): WeekSchedule` — первая неделя таблицы
  (совместимость со старым экраном); `parseAll(raw, group): ScheduleTable` — ВСЕ недели.
  Группа в ответе — массив, элементы которого разбираются **по типу**, а не по позиции:
  строковый период группы (необязателен) и объект «номер недели». Поэтому ответ без периода
  тоже читается. Порядок недель в JSON произвольный — сортируются по дате начала.
  Неделя без восстановимой даты начала **пропускается**, остальные читаются нормально.
  Дата начала берётся по очереди: `per` недели → первый разборчивый элемент `days[]`
  («02 ноября 2026 г.», «Сегодня» пропускается) → период группы → `DATE` любой пары.
  Пары собираются обходом дерева дня, а не фиксированной глубины (`pairsOf`):
  `{ "<день>": { "<пара>": [ {...} ] } }` и лишняя обёртка над массивом одинаково работают.
  Все значения полей **триммятся** (сайт отдаёт строки с хвостовыми пробелами);
  `DATE` разбирается и без ведущего нуля (`3-11-2026`). Некорректный JSON / нет группы → `IllegalArgumentException`.
- **Структура `res/raw/schedule_k3i1_9.json`** — копия формата ответа сайта `/sched_js`:
  `{ "<ГРУППА>": [ "<период dd.MM.yyyy - dd.MM.yyyy>", { "<№ недели>": [ {per, today, days[]},
  { "<день 1..7>": { "<номер пары>": [ { GROUP, DAY, LES, AUD, WEEK, SUBG, POST, NAME, SUBJECT,
  SUBJ_TYPE, DATE, CAFEDRA, THEME, ID, COURSE, FACULTY, FORMOBUCH, deleted } ] } } ] } ] }`.
  Значения — строки (часто с пробелами). Пустой день = отсутствие ключа дня (пример: воскресенье).
  `LES` вне таблицы звонков пропускается из времени, но пара не теряется (время 00:00).
- **Состав `res/raw` сейчас:** ДВЕ недели группы К3И1(9) — №45 (`02.11.2026 – 08.11.2026`)
  и №46 (`09.11.2026 – 15.11.2026`), период группы в заголовке `02.11.2026 - 15.11.2026`.
  Каждая неделя: Пн–Сб по 2–4 пары, воскресенье пустое (ключа дня `7` нет).
  **тестовые данные содержат 2 недели, прошлая удалена намеренно** — поэтому 01.11.2026
  и всё до 02.11 в календаре недоступны (имитация «прошлую неделю удалили»), а с 16.11 —
  недоступны как «ещё не выложено». Предметы/преподаватели/аудитории вымышленные, формат значений
  повторяет сайт (хвостовые пробелы в полях).
- **Доступность дат для календаря — `object CalendarAvailability`** (`data/CalendarAvailability.kt`),
  чистые функции без Android (покрыты unit-тестами):
  - `stateFor(date, table): DayAvailability` — состояние одной даты;
  - `availability(table, month: YearMonth)` и перегрузка `availability(table, month: Int, year: Int)` →
    `MonthAvailability(month, states: Map<LocalDate, DayAvailability>)`, в `states` ровно все дни месяца;
    у `MonthAvailability` есть `stateFor(date)` (дата вне месяца → `UNAVAILABLE`) и `belongsToMonth(date)`;
  - `isSelectable(date, table)`, `availableDates(table)`.
  Состояния (`enum DayAvailability`), правило одно на весь календарь:
  - `AVAILABLE` — дата есть в `ScheduleTable` и в этот день есть хотя бы одна пара;
  - `AVAILABLE_EMPTY` — дата есть в таблице, но пар нет (воскресенье, выходной);
  - `UNAVAILABLE` — даты нет в таблице вообще: неделя удалена или ещё не выложена,
    включая даты до `firstDate` и после `lastDate`, и «дырки» между неделями
    (недостающие недели не заполняются пустыми днями).
  `isSelectable` = `AVAILABLE` или `AVAILABLE_EMPTY` — `UNAVAILABLE` не выбираются.
  «Монолитной серой» заливкой UI-календарь рисует именно `UNAVAILABLE`; `AVAILABLE_EMPTY`
  показывается как доступный день с пустым расписанием («Пар нет»).
- **Тесты.** `app/src/test/java/...`, JUnit4 (`junit:junit:4.13.2` из version catalog), без Android
  и Robolectric: `./gradlew testDebugUnitTest` (**34 теста**: 28 по данным + 6 по ViewModel).
  `ScheduleViewModelTest` — единственный, что зависит от `viewModelScope`: там подменяется
  `Dispatchers.Main` через `kotlinx-coroutines-test` (testImplementation, см. version catalog)
  и в ViewModel подаётся `initialState` с уже загруженной таблицей. Тесты разбора читают настоящий файл
  `src/main/res/raw/schedule_k3i1_9.json` по относительному пути — файл должен оставаться
  в формате сайта, иначе упадут и тесты.

## Экран «Расписание»

- Шапка: зашитая группа **К3И1(9)** (`R.string.schedule_group`), справа от неё в той же
  строке — кнопка календаря (Phosphor `CalendarDots` regular, 24 dp, `onSurfaceVariant`,
  `contentDescription` `R.string.cd_open_calendar`, клик без ряби). Ниже подпись дня
  («Сегодня, Пятница, 06 ноября» / «Вторник, 03 ноября»). Выбора группы нет.
- Лента дней — **одна неделя Пн–Вс** (`WeekDayStrip` на `HorizontalPager`, страница = неделя),
  активный день — таблетка `IubipTheme.colors.accentPill`, точка под числом — «сегодня».
  Старт: неделя, где идёт сегодня; дата — сегодня (или понедельник, если сегодня нет в данных).
- Кнопка календаря скрыта, пока данных нет (загрузка/ошибка).
- Список карточек `PairCard` в порядке номеров: время «08:30–10:05» и справа «1-я пара»
  второстепенным; строка «тип + предмет» (тип серым, предмет полужирным); преподаватель
  (`Icons.Outlined.Person`) и аудитория (`Icons.Outlined.MeetingRoom`) с мелкими иконками.
- Пустой день — иконка календаря + «Пар нет» по центру (`EmptyDay`).
- Выбранная дата, которой нет в выложенном расписании (возможно на старте, если данные
  начинаются с будущей недели) — нейтральное «Расписание на эту дату не выложено».
- Состояния: загрузка (progress), ошибка («Не удалось загрузить расписание» + «Повторить»).

## Календарь и недели

### Один источник истины (правило, обязательное)
Какая неделя активна и какой день выбран — знает **только `ScheduleViewModel`**
(`ScheduleUiState.weekIndex` + `selectedDate`). Лента дней и календарь — «глупые»:
показывают состояние и сообщают ОДНО действие пользователя.

- Лента (`WeekDayStrip`) сообщает только завершённый жест: `onUserSwipeNext` /
  `onUserSwipePrevious`, когда `pagerState.settledPage` отличается от последней
  известной страницы и `!isScrollInProgress`. Промежуточные кадры и программную
  прокрутку лента НЕ озвучивает (никакой обратной синхронизации из `snapshotFlow`
  и никакого `onWeekChange(index)`).
- Календарь сообщает только `onDateSelected(date)` (выбор даты) или закрытие.
- Синхронизация пейджера из состояния — односторонняя: `LaunchedEffect(weekIndex)` →
  `animateScrollToPage`. Она ничего не отправляет в ViewModel.
- `rememberPagerState(initialPage = weekIndex)` не реагирует на смену `weekIndex` при
  совпадении identity, поэтому лента обёрнута в `key(weeks.first().start)` и создаётся
  только под уже загруженные недели.

Методы ViewModel:
| Метод | Кто зовёт | Что делает |
|---|---|---|
| `selectDate(date)` | тап по дню в ленте, выбор в календаре | `weekIndex` по дате + `selectedDate = date` (недоступную дату игнорирует) |
| `goToWeek(index)` | программный переход | меняет неделю, сохраняет дату, если она в новой неделе, иначе сегодня/понедельник |
| `onUserSwipeToWeek(index)`, `onUserSwipeNext()` / `onUserSwipePrevious()` | только жест ленты | неделя + СБРОС дня: сегодня, если оно в неделе, иначе понедельник; за границей — no-op |
| `load()` | init / «Повторить» | `repository.table(group)`; неделя — где сегодня, иначе ближайшая опубликованная |

### Листание неделями
- Лента — `HorizontalPager` в `compose foundation` (зависимостей не добавляли),
  `beyondViewportPageCount = 1`. Страницы — только `ScheduleTable.weeks`: свайп за
  край данных не уводит (границы пейджера), «пустых недель» нет.
- Внутри недели свободной прокрутки нет: 7 ячеек делят ширину поровну (даже при
  крупном системном шрифте не уезжают за край).
- Одна прокрутка свайпом = ровно одна неделя.

### Лист календаря (`calendar/`)
- `ModalBottomSheet` (`skipPartiallyExpanded = true`, `IubipShapes.Panel`,
  `containerColor = surface`), внутри — `CalendarSheetContent`.
- Заголовок листа «Выберите дату» + крестик (`IconCircleButton`). Строка месяца
  «‹ Ноябрь 2026 ›»: стрелки `MonthArrowButton` (круг 40 dp в зоне 48 dp, без ряби);
  на первом/последнем месяце стрелка выключена (`semantics { disabled() }`).
- Сетка: подписи «Пн Вт Ср Чт Пт Сб Вс» (`labelMedium`, `onSurfaceVariant`), неделя —
  одна строка. Высота строки 42 dp, круг дня 34 dp — лист помещается на экран без прокрутки.
- Состояния дня (из `CalendarAvailability.stateFor`):
  - `AVAILABLE` — обычная цифра `onSurface`, нажимается;
  - `AVAILABLE_EMPTY` — обычная цифра с прозрачностью 0.75, нажимается;
  - `UNAVAILABLE` — монолитная серая плашка `surfaceVariant` (тёмная тема — `#232A33`),
    цифра `onSurfaceVariant` с прозрачностью 0.6, НЕ нажимается;
  - выбранная дата — круг `primary`, цифра `onPrimary`; «сегодня» — тонкая (1 dp)
    обводка `primary` (выбранный день обводки не имеет).
  - дни соседних месяцев (хвост недели) — обычная цифра с прозрачностью 0.5, не нажимаются.
- **Месяц показан целиком, только месяцы с данными.** Кадр месяца: от понедельника
  недели с 1-м числом до воскресенья недели с последним числом. Даты вне месяца — серая
  плашка (в сетке нет пустых клеток, неделя всегда — одна строка). Месяцы календаря =
  от месяца первой выложенной недели до месяца последней, отфильтрованные по пересечению
  с выложенными неделями, по порядку учебного года (сентябрь → август). Перелистывание
  месяца внутри листа — свайп по сетке или стрелки, последний месяц с данными не листается дальше.
- Под сеткой — легенда «Серым — расписания нет».
- Доступность: у дня `contentDescription` вида «среда, 11 ноября», «…, пар нет»,
  «…, расписания нет», «…, другой месяц»; у стрелок — «Предыдущий/Следующий месяц»,
  у крестика — «Закрыть календарь». Подписи дней берутся из `CalendarPage.inMonth`,
  чтобы на странице соседнего месяца не было двух одинаковых «11 ноября»
  (`CalendarDays.description(date, label)`).
- Выбор даты: лист монтирует своё `rememberSaveable`-состояние (`show()`), поэтому
  выбранная дата применяется к экрану **после** `sheetState.hide()` (в экране —
  `closeCalendar(selected)`), а не до него.
- В исходном `Hidden`-состоянии `ModalBottomSheet` всё равно рисует пустой каркас поверх
  контента — поэтому лист монтируется только при `currentValue/targetValue != Hidden`.

### Новые строки (`strings.xml`)
`cd_open_calendar` («Открыть календарь»), `calendar_title` («Выберите дату»),
`calendar_legend_unavailable` («Серым — расписания нет»), `cd_calendar_previous_month`,
`cd_calendar_next_month`, `cd_calendar_close`, `cd_day_cell` («%1$s»),
`cd_day_cell_unavailable` («%1$s, расписания нет»), `cd_day_cell_empty` («%1$s, пар нет»),
`cd_day_cell_other_month` («%1$s, другой месяц»), `cd_week_swipe` («Свайп влево или вправо — соседняя неделя»),
`schedule_not_published` («Расписание на эту дату не выложено»),
`schedule_not_published_hint`, `schedule_no_data` («Расписание ещё не выложено»).

### `ScreenHeader` — слот действия
`ScreenHeader(title, modifier, subtitle, maxSubtitleLines, action: (@Composable () -> Unit)? = null)`.
Если `action != null`, заголовок и действие стоят в одной `Row` (заголовок `weight(1f)`,
обрезается многоточием, действие прижато вправо). Без `action` шапка выглядит как раньше —
профиль (`ProfileScreen`) не менялся. Правило: действия в шапке — только через этот слот,
внутри `action` использовать `IconCircleButton` (48 dp зона, 24 dp иконка, без ряби).

## Дизайн-токены

- **Акцент вуза — `#0F48AD`** (`BrandBlue`, снят с iubip.ru) = `primary` светлой схемы;
  в тёмной схеме `primary` = `#8FB0F0` (для контраста на тёмном фоне).
- Фон светлой темы `#F7F8FA`, поверхности (карточки) `#FFFFFF`; тёмная тема:
  фон `#101318`, карточки `#1A1E25`. Вторичный текст — `onSurfaceVariant` (`#6B7280` / `#9AA3B0`).
- Все цвета — в `ui/theme/Color.kt`, схемы — в `Theme.kt` (`lightColorScheme`/`darkColorScheme`,
  `surfaceTint = Transparent`). Доп. токены `IubipTheme.colors` (`accentPill`, `divider`) через
  `LocalIubipColors`; `accentPill` = primary с прозрачностью 12% (`AccentPillLight`).
- Скругления — `ui/theme/Shape.kt`: `IubipShapes.Card` 18 dp, `Panel` 20 dp,
  `Small` 12 dp, `Pill` (percent 50 — кнопки/индикаторы в полную высоту).
- Типографика — `Type.kt`: заголовок экрана `headlineLarge` 28 sp SemiBold, группа
  `headlineMedium` 26 sp, предмет `titleLarge` 17 sp SemiBold, время `titleSmall` 15 sp,
  вторичный текст `bodySmall` 13 sp. Жирный только у заголовка и предмета.
- Иконки контурные (`Icons.Outlined.*`), тени не используются, градиентов нет.
- Локаль форматирования дат фиксирована `ru` (`dayDateFormat`), интерфейс следует
  системной теме (`isSystemInDarkTheme()`).

## Принятые решения

- **Навигация через Navigation Compose.** `NavHost` с `startDestination = "schedule"`,
  маршруты в `object Routes`. Переключение вкладок: `popUpTo(startDestination) { saveState = true }`
  + `launchSingleTop = true` + `restoreState = true`. Пункты панели — `enum class BottomDestination`.
- **Нижняя панель** — собственный `IubipBottomBar` (не `NavigationBar`): фон = фон приложения,
  тонкий `HorizontalDivider` сверху. Раскладка пункта вертикальная: иконка 22 dp сверху,
  подпись `labelSmall` под ней. Активный пункт — таблетка `accentPill` вокруг И КОЛОНКИ
  «иконка + подпись» целиком, внутри неё всё в `primary`, текст SemiBold.
  Неактивный — `onSurfaceVariant`, иконка thin, подпись Normal. Высота 64 dp.
- **Без квадратной ряби Material при нажатии.** Пункт панели кликается через
  `clickable(interactionSource = null, indication = null, role = Role.Tab)` +
  `semantics { selected = ... }`, а визуальное выделение даёт только собственная
  таблетка активного пункта. Не заменять обратно на `selectable`/`NavigationBarItem` —
  вернётся прямоугольная подсветка вокруг кнопки.
- **Иконки панели — свои Phosphor, не Material.** `BottomDestination.icon` (thin) для
  неактивного состояния и `iconSelected` (fill) для активного. Расписание —
  `CalendarDots` (календарь с точками), профиль — `UserCircle` (персона в круге), как
  в референсе интерфейса. Material Icons в проекте больше не используются.
- **Клики без стандартной ряби.** И в панели, и в чипах дней недели используется
  `clickable(interactionSource = null, indication = null)`: Android не должен рисовать
  прямоугольный отклик нажатия поверх наших скруглённых плашек.
- **JVM target 17.** `compileOptions { source/targetCompatibility = VERSION_17 }` и
  `kotlinOptions { jvmTarget = "17" }`. Без этого сборка падает с
  «Inconsistent JVM-target compatibility» (JDK 21 компилирует в 21, а AGP ждёт 17).
- **Строки в resources.** Весь пользовательский текст — в `res/values/strings.xml`
  (русский), в коде берётся через `stringResource(R.string...)`. Хардкода русского текста
  в Kotlin нет.
- **Версии в version catalog.** Все зависимости и плагины — в `gradle/libs.versions.toml`,
  не хардкодятся в `build.gradle.kts`.

## Правила вёрстки (обязательно для всех агентов)

Эти правила появились потому, что два агента, работавшие параллельно, разошлись в мелочах
(радиусы, обводки, заглушки, дубликаты экранов). Нарушение = задача не принята.

### 1. Цвета — только из темы
- Любой цвет берётся из `MaterialTheme.colorScheme.*`. HEX-код в файлах экранов и компонентов запрещён.
- `#0F48AD` (фирменный синий вуза) живёт ТОЛЬКО в `ui/theme/Color.kt` как `primary` светлой схемы.
  В тёмной схеме `primary` — `#8FB0F0`. Смысл: один акцент, ничего больше не «синее».
- Фон экрана — `background` (`#F7F8FA` светлая / `#101318` тёмная), карточки — `surface`.
  Не смешивать: если фон и карточка одинаковые, карточка исчезает.
- Второстепенный текст — `onSurfaceVariant`. Подписи полей и меты (время, преподаватель) — им.
- Акцент (`primary`) использовать скупо: активная вкладка, сегодняшний день, ссылки. Не красить им заголовки.

### 2. Скругления — через `IubipShapes`
- Только `IubipShapes.Card` (18 dp), `IubipShapes.Panel` (20 dp), `IubipShapes.Pill` (полностью круглое).
- Локальный `RoundedCornerShape(...)` в файлах экранов/компонентов запрещён. Если нужного размера нет —
  добавить токен в `ui/theme/Shape.kt` и описать его здесь.

### 3. Типографика — через `MaterialTheme.typography`
- Заголовок экрана — `headlineMedium` (28 sp, SemiBold) через `ScreenHeader`.
- Основной текст/значение — `bodyLarge`; мелкий текст и меты — `bodySmall`/`labelMedium`.
- Жирность вручную (`FontWeight.Bold`) в экранах не задавать — она уже в токенах типов.

### 4. Отступы
- Горизонтальный отступ экрана — 20 dp. Расстояние между карточками — 12 dp. Внутренний паддинг карточки — 16–20 dp.
- Не использовать «на глаз» 14 dp / 18 dp / 22 dp — только 4, 8, 12, 16, 20, 24, 32.

### 5. Компоненты не дублировать
Перед созданием нового компонента проверить `ui/components/`:
- `ScreenHeader` — заголовок экрана с подписью и необязательным слотом `action` справа;
- `IconCircleButton` — круглая иконка-кнопка 48 dp без ряби (действия в шапке, стрелки);
- `MetaRow` — строка «иконка + текст» (преподаватель, аудитория);
- `IubipBottomBar` — нижняя панель;
- `ProfileAvatarPlaceholder` — круглая аватарка-заглушка;
- `ProfileFieldRow`, `ProfileFieldsCard` — строки и карточка полей профиля.

Если нужного нет — создать и вписать сюда. Если похожий есть — расширить существующий.

### 6. Экраны — по одному файлу в своём пакете
- `ui/screens/schedule/` и `ui/screens/profile/`. Никаких «заглушек в общем файле»,
  никаких фасадов вида `SomethingCompat.kt` для обхода ошибок импорта — менять импорт в `MainActivity`.
- Имя функции экрана должно быть уникальным в проекте (`ScheduleScreen`, `ProfileScreen`).

### 7. Иконки
- Новая иконка: только Phosphor (SVG из `phosphor-icons/core` → VectorDrawable + `ImageVector`),
  без внешних библиотек иконок. Размер 24 dp, цвет через `tint` (`onSurfaceVariant` или `primary`).
- Material Icons — не добавлять.

### 8. Тем, кто пишет экран, запрещено
- Трогать `ui/theme/` (менять цвета/типы/формы — только с записью здесь и объяснением зачем).
- Трогать `MainActivity.kt` и `navigation/` (навигация — отдельная задача).
- Трогать `data/` и `res/raw/` (данные — отдельная задача).
- Добавлять свои `Color.kt`, свои `theme`-функции, свои нижние панели.

### Чек-лист перед сдачей задачи
1. `./gradlew assembleDebug` — BUILD SUCCESSFUL.
2. Приложение открывается на эмуляторе, экран открывается.
3. Скриншот светлой и тёмной темы (`adb shell cmd uimode night yes|no`).
4. Длинный текст не обрезается (проверить ФИО/предмет в 2 строки, font scale 1.3).
5. Пустое состояние осмысленное (например «Пар нет»), а не пустой экран.
6. Нет HEX-цветов, `RoundedCornerShape`, `FontWeight.Bold` в файлах экранов.
7. AGENTS.md обновлён: новые компоненты/файлы добавлены, удалённые — вычеркнуты.

## Календарь и листание недель

**Один источник истины.** Текущая неделя и выбранная дата живут ТОЛЬКО в
`ScheduleUiState` (`weekIndex`, `selectedDate`). Лента дней их не хранит и не сообщает
обратно номер страницы — она отправляет одно событие «пользователь свайпнул
направо/налево» (`onUserSwipeNext` / `onUserSwipePrevious`) по `pagerState.settledPage`
при `!isScrollInProgress`. Обратная запись страницы в состояние запрещена: это
приводило к тому, что выбор даты из календаря затирался сбросом недели.

**Сброс дня.** При свайпе на другую неделю день сбрасывается: на сегодня, если
сегодня в этой неделе, иначе на понедельник. При программном переходе
(`goToWeek`, выбор даты в календаре) дата НЕ сбрасывается — ставится выбранная.

**Границы.** Листаются только недели из `ScheduleTable.weeks`; свайп за границу
ничего не делает (Pager ограничен `pageCount`).

**Доступность дат** — `data/CalendarAvailability.kt`: `AVAILABLE` (есть пары),
`AVAILABLE_EMPTY` (дата есть, пар нет — нажимается, «Пар нет»), `UNAVAILABLE`
(недели нет в данных — «монолитная серая» плашка, не нажимается).

**Компоненты:** `ui/screens/schedule/calendar/` (`CalendarSheet`, `CalendarModel`,
`CalendarComponents`, `CalendarDays`), `ui/components/IconCircleButton.kt`.
`ScreenHeader` получил опциональный слот `action`.

**Тесты:** `ScheduleViewModelTest` — старт, выбор даты, сброс по свайпу, границы,
недоступная дата, данные с будущей недели.

## Сборка и запуск

Требуется JDK 21 (системная java 25 не подходит из-за JVM target), Android SDK 36.

```bash
export JAVA_HOME=/home/fxrar/Android/android-studio/jbr
export ANDROID_HOME=/home/fxrar/Android/sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH

# сборка
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk

# установка на подключённый эмулятор/устройство (например emulator-5554)
adb install -r app/build/outputs/apk/debug/app-debug.apk

# запуск
adb shell am start -n ru.iubip.student/.MainActivity

# тёмная тема эмулятора
adb shell cmd uimode night yes   # вернуть: night no
```

`local.properties` с `sdk.dir=/home/fxrar/Android/sdk` создан локально и не коммитится —
на другой машине его нужно создать заново (или задать `ANDROID_HOME`).

## Экран «О себе» (профиль)

Реализован. Порядок элементов сверху вниз строго такой:
аватарка (круг 96 dp с силуэтом) → ФИО крупно по центру → «Группа К3И1(9)» под ним →
карточка с полями: адрес электронной почты, страна, город, часовой пояс.
Подписи полей — `onSurfaceVariant`, значения — `onSurface`. Кнопок и редактирования нет.

### Файлы профиля

| Файл | Что внутри |
|---|---|
| `ui/screens/profile/ProfileScreen.kt` | `ProfileScreen(profile = SampleProfile)` — сам экран + 3 `@Preview` (светлая, тёмная, длинное ФИО) |
| `ui/screens/profile/ProfileSample.kt` | `data class Profile` и `SampleProfile`; `SampleProfileLongName` — только для превью |
| `ui/components/ProfileAvatarPlaceholder.kt` | `ProfileAvatarPlaceholder` — круглая заглушка аватарки |
| `ui/components/ProfileFieldRow.kt` | `ProfileFieldRow` (подпись + значение) и `ProfileFieldsCard` (карточка) |
| `ui/icons/PhosphorIcons.kt` | `ImageVector`-иконки Phosphor |

**Данные тестовые и игрушечные.** `SampleProfile` повторяет скриншот личного кабинета
(Волков Даниил Сергеевич, К3И1(9), 12k1i192024@mail.ru, Россия, Ростов-на-Дону,
Europe/Moscow). Источником в будущем станет личный кабинет Moodle — тогда `ProfileSample.kt`
удаляется и данные приходят из слоя `data/`. Все строки интерфейса — в `strings.xml`
(`profile_*`, `cd_profile_avatar`); выдуманные значения — это данные, они лежат в Kotlin.

### Стиль профиля

Только токены темы: `MaterialTheme.colorScheme.*` и `MaterialTheme.typography.*`.
Hex-цветов в файлах профиля нет; силуэт заглушки — `onSurfaceVariant` на `surfaceVariant`.
Радиус карточки и обводку задавать через токены темы: `IubipShapes.Card`/`Panel`
и `MaterialTheme.colorScheme.surfaceContainer*`; локальный `RoundedCornerShape` в файлах экранов
не использовать (см. раздел «Правила вёрстки»).

### Иконки Phosphor (без внешних зависимостей)

Зависимость Phosphor НЕ подключалась. SVG скачаны из официального репозитория
`https://github.com/phosphor-icons/core` (каталог `assets/<стиль>/`) и сконвертированы
в VectorDrawable: viewport `0 0 256 256`, `android:fillColor="#FF000000"`, цвет приходит
через `tint`. Лежат в `res/drawable/`:

| Файл | Стиль | Назначение |
|---|---|---|
| `ic_phosphor_calendar_blank.xml` | thin | «Расписание», неактивное |
| `ic_phosphor_user.xml` | thin | «О себе», неактивное |
| `ic_phosphor_calendar_blank_fill.xml` | fill | «Расписание», активное |
| `ic_phosphor_user_fill.xml` | fill | «О себе», активное (им же нарисован силуэт аватарки) |
| `ic_phosphor_calendar_blank_regular.xml`, `ic_phosphor_user_regular.xml` | regular | базовый контур |
| `ic_phosphor_chalkboard_teacher.xml` | regular | преподаватель (на будущее) |
| `ic_phosphor_door.xml` | regular | аудитория (на будущее) |
| `ic_phosphor_clock.xml` | regular | часы / время пары (на будущее) |
| `ic_phosphor_caret_left_regular.xml`, `ic_phosphor_caret_right_regular.xml` | regular | стрелки месяца в календаре |
| `ic_phosphor_caret_down_regular.xml` | regular | стрелка «вниз» (запас для листов) |
| `ic_phosphor_x_regular.xml` | regular | закрыть лист календаря |

В Kotlin они обёрнуты в `ImageVector` в `ui/icons/PhosphorIcons.kt`:
`PhosphorIcons.Thin.CalendarBlank` / `.User`, `PhosphorIcons.Fill.CalendarBlank` / `.User`,
`PhosphorIcons.Regular.CalendarBlank` / `.User` / `.ChalkboardTeacher` / `.Door` / `.Clock` /
`.CaretLeft` / `.CaretRight` / `.CaretDown` / `.X`.
Используются как обычные Material-иконки: `Icon(PhosphorIcons.Fill.User, null, tint = ...)`.
pathData продублирован в `PhosphorIcons.kt` и в `res/drawable` — при добавлении иконки
обновлять обе стороны (в конце `PhosphorIcons.kt` есть `PhosphorPathData`).

**Подключены к нижней панели.** `BottomDestination.icon` = `PhosphorIcons.Thin.*`
(неактивное состояние), `BottomDestination.iconSelected` = `PhosphorIcons.Fill.*`
(активное). Проверено на эмуляторе: иконки рендерятся и красятся через `tint`.

## Ограничения текущего этапа

Нет моделей данных профиля из Moodle, сети, кеша, уведомлений, выбора группы
и цветовых пометок типа занятия. `ScheduleScreen` показывает локальные тестовые данные,
`ProfileScreen` — тестовые данные из `ProfileSample.kt`.

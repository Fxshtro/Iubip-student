# 🎓 ИУБиП Студент — мобильное приложение для расписания и профиля

[![Kotlin 2.1](https://img.shields.io/badge/Kotlin-2.1-7F52FF.svg)](https://kotlinlang.org/) [![Jetpack Compose](https://img.shields.io/badge/Compose-BOM%202025.02-4285F4.svg)](https://developer.android.com/jetpack/compose) [![Material 3](https://img.shields.io/badge/Material-3-757575.svg)](https://m3.material.io/) [![Android 8+](https://img.shields.io/badge/Android-8.0%2B-3DDC84.svg)](https://www.android.com/) [![minSdk 26](https://img.shields.io/badge/minSdk-26-brightgreen.svg)](https://developer.android.com/) [![License](https://img.shields.io/badge/license-private-lightgrey.svg)](#-лицензия)

**Мобильное приложение для студентов** Южного Университета (ИУБиП), Ростов-на-Дону. Расписание группы и данные о себе — в одном месте, без переключения между сайтами колледжа.

Android-приложение на **Kotlin + Jetpack Compose (Material 3)**. Работает офлайн на локальных данных, архитектура готова к подключению источника колледжа.

---

## ✨ Возможности

- **Расписание на день** — карточки пар с временем, номером, типом занятия (лек. / пр.), преподавателем и аудиторией
- **Листание недель** — одна прокрутка свайпом = ровно одна неделя, за границы выложенного расписания не уходит
- **Календарь** — выбор даты в сетке месяца; даты, на которые расписание ещё не выложено или уже удалено, показаны серым и не выбираются
- **Пустой день** — «Пар нет» вместо пустого экрана
- **Профиль** — аватарка, ФИО, номер группы, контактные данные студента
- **Тёмная и светлая тема** — приложение следует системной настройке
- **Фирменный стиль** — акцентный синий ИУБиП, скруглённые поверхности, иконки Phosphor

---

## 🏗️ Архитектура

```
┌───────────────────────────────────────────────┐
│                 UI (Compose)                  │
│   ScheduleScreen   ProfileScreen   Calendar   │
└───────────────────────┬───────────────────────┘
                        │ StateFlow (uiState)
              ┌─────────▼──────────┐
              │    ViewModel       │  один источник истины:
              │  ScheduleViewModel │  неделя + выбранная дата
              └─────────┬──────────┘
                        │ suspend fun table(group)
              ┌─────────▼──────────┐
              │   Repository       │  интерфейс, экран не знает источника
              └─────────┬──────────┘
                        │
              ┌─────────▼──────────┐
              │  ScheduleSource    │  сейчас: res/raw (JSON сайта)
              │   + JsonParser     │  позже: сеть, без правки экранов
              └────────────────────┘
```

```
Iubip-App/
├── app/src/main/java/ru/iubip/student/
│   ├── MainActivity.kt              # точка входа, навигация
│   ├── navigation/                  # маршруты и пункты нижней панели
│   ├── data/                        # слой данных: модели, парсер, репозиторий
│   │   ├── model/                   # Day, ClassPair
│   │   ├── source/                  # ScheduleSource, JsonScheduleParser, ScheduleTable
│   │   ├── CalendarAvailability.kt  # доступность дат для календаря
│   │   └── BellSchedule.kt          # таблица звонков
│   └── ui/
│       ├── components/              # общие компоненты
│       ├── icons/                   # иконки Phosphor (ImageVector)
│       ├── screens/schedule/        # экран расписания + календарь
│       ├── screens/profile/         # экран «О себе»
│       └── theme/                   # цвета, типографика, формы
├── app/src/test/                    # unit-тесты (34)
├── app/src/main/res/raw/            # тестовое расписание в формате сайта
├── gradle/libs.versions.toml        # версии зависимостей
└── AGENTS.md                        # правила разработки проекта
```

**Один источник истины.** Какая неделя активна и какой день выбран — знает только `ScheduleViewModel`. Лента и календарь показывают состояние и сообщают одно действие пользователя.

---

## 🚀 Быстрый старт

### Требования

- **JDK 17+** (проверено на JDK 21 из Android Studio)
- **Android SDK 36**, build-tools 35+
- Эмулятор (API 26+) или устройство Android 8.0+

### 1. Настройка окружения

```bash
export JAVA_HOME="$HOME/Android/android-studio/jbr"
export ANDROID_HOME="$HOME/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$PATH"

# путь к SDK для Gradle (файл не коммитится)
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

### 2. Сборка

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### 3. Установка и запуск

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n ru.iubip.student/.MainActivity
```

### 4. Тесты

```bash
./gradlew testDebugUnitTest
```

---

## 🔗 Стек

| Слой | Технология |
|---|---|
| **Язык** | Kotlin 2.1.0 |
| **UI** | Jetpack Compose, Material 3, Compose BOM 2025.02.00 |
| **Навигация** | Navigation Compose 2.8.7 |
| **Состояние** | ViewModel + StateFlow (lifecycle-viewmodel-compose 2.8.7) |
| **Данные** | Репозиторий за интерфейсом, kotlinx.serialization 1.8.0 |
| **Сборка** | Android Gradle Plugin 8.9.1, Gradle 8.14.3, version catalog |
| **Тесты** | JUnit 4, kotlinx-coroutines-test |
| **Разрешения** | `minSdk 26`, `targetSdk 36` |

---

## 📱 Экраны

| Экран | Что показывает |
|---|---|
| **Расписание** | Группа, лента дней недели, карточки пар, пустой день, ошибки загрузки |
| **Календарь** | Сетка месяца, доступные и недоступные даты, выбор дня |
| **О себе** | Аватарка, ФИО, группа, email, город, страна, часовой пояс |

---

## 🛠️ Устранение неполадок

| Проблема | Что делать |
|---|---|
| **`Inconsistent JVM-target compatibility`** | Проверить `JAVA_HOME` — нужен JDK 17+, а `jvmTarget = 17` должен быть задан в `app/build.gradle.kts` |
| **`SDK location not found`** | Создать `local.properties` с `sdk.dir=<путь к Android SDK>` |
| **Эмулятор не найден** | `adb devices`; при пустом списке — запустить AVD из Android Studio |
| **Тёмная тема не переключается** | `adb shell cmd uimode night yes` / `night no` |
| **Расписание пустое** | Проверить `app/src/main/res/raw/schedule_k3i1_9.json` — тестовые данные лежат там |

---

## 🙏 Команда

| Направление | Ответственный |
|---|---|
| Android-разработка | Даниил Волков |

---

## 📄 Лицензия

Приватный проект — Южный Университет (ИУБиП) © 2026

---

# 🎓 IUBiP Student — Mobile App for Schedule and Profile

[![Kotlin 2.1](https://img.shields.io/badge/Kotlin-2.1-7F52FF.svg)](https://kotlinlang.org/) [![Jetpack Compose](https://img.shields.io/badge/Compose-BOM%202025.02-4285F4.svg)](https://developer.android.com/jetpack/compose) [![Material 3](https://img.shields.io/badge/Material-3-757575.svg)](https://m3.material.io/) [![Android 8+](https://img.shields.io/badge/Android-8.0%2B-3DDC84.svg)](https://www.android.com/) [![minSdk 26](https://img.shields.io/badge/minSdk-26-brightgreen.svg)](https://developer.android.com/) [![License](https://img.shields.io/badge/license-private-lightgrey.svg)](#-license)

**Mobile app for students** of Southern University (IUBiP), Rostov-on-Don. Group schedule and personal data — in one place, without switching between college websites.

Android app built with **Kotlin + Jetpack Compose (Material 3)**. Works offline on local data, architecture ready to connect the college source.

---

## ✨ Features

- **Daily schedule** — class cards with time, slot number, lesson type (lecture / practice), teacher and room
- **Week swiping** — one swipe = exactly one week, never scrolls past published schedule boundaries
- **Calendar** — pick a date in a month grid; dates with no published or already deleted schedule are shown grey and cannot be selected
- **Empty day** — "No classes" instead of a blank screen
- **Profile** — avatar, full name, group number, student contact details
- **Dark and light theme** — follows the system setting
- **Brand style** — IUBiP accent blue, rounded surfaces, Phosphor icons

---

## 🏗️ Architecture

```
┌───────────────────────────────────────────────┐
│                 UI (Compose)                  │
│   ScheduleScreen   ProfileScreen   Calendar   │
└───────────────────────┬───────────────────────┘
                        │ StateFlow (uiState)
              ┌─────────▼──────────┐
              │    ViewModel       │  single source of truth:
              │  ScheduleViewModel │  week + selected date
              └─────────┬──────────┘
                        │ suspend fun table(group)
              ┌─────────▼──────────┐
              │   Repository       │  interface, UI knows no source
              └─────────┬──────────┘
                        │
              ┌─────────▼──────────┐
              │  ScheduleSource    │  now: res/raw (site JSON)
              │   + JsonParser     │  later: network, no UI changes
              └────────────────────┘
```

```
Iubip-App/
├── app/src/main/java/ru/iubip/student/
│   ├── MainActivity.kt              # entry point, navigation
│   ├── navigation/                  # routes and bottom bar items
│   ├── data/                        # data layer: models, parser, repository
│   │   ├── model/                   # Day, ClassPair
│   │   ├── source/                  # ScheduleSource, JsonScheduleParser, ScheduleTable
│   │   ├── CalendarAvailability.kt  # date availability for the calendar
│   │   └── BellSchedule.kt          # bell schedule (class times)
│   └── ui/
│       ├── components/              # shared components
│       ├── icons/                   # Phosphor icons (ImageVector)
│       ├── screens/schedule/        # schedule screen + calendar
│       ├── screens/profile/         # "About me" screen
│       └── theme/                   # colors, typography, shapes
├── app/src/test/                    # unit tests (34)
├── app/src/main/res/raw/            # test schedule in site format
├── gradle/libs.versions.toml        # dependency versions
└── AGENTS.md                        # project development rules
```

**Single source of truth.** Active week and selected date live only in `ScheduleViewModel`. The day strip and calendar render state and report a single user action.

---

## 🚀 Quick Start

### Prerequisites

- **JDK 17+** (verified on JDK 21 bundled with Android Studio)
- **Android SDK 36**, build-tools 35+
- Emulator (API 26+) or an Android 8.0+ device

### 1. Environment setup

```bash
export JAVA_HOME="$HOME/Android/android-studio/jbr"
export ANDROID_HOME="$HOME/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$PATH"

# SDK path for Gradle (file is not committed)
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

### 2. Build

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### 3. Install and run

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n ru.iubip.student/.MainActivity
```

### 4. Tests

```bash
./gradlew testDebugUnitTest
```

---

## 🔗 Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin 2.1.0 |
| **UI** | Jetpack Compose, Material 3, Compose BOM 2025.02.00 |
| **Navigation** | Navigation Compose 2.8.7 |
| **State** | ViewModel + StateFlow (lifecycle-viewmodel-compose 2.8.7) |
| **Data** | Repository behind an interface, kotlinx.serialization 1.8.0 |
| **Build** | Android Gradle Plugin 8.9.1, Gradle 8.14.3, version catalog |
| **Tests** | JUnit 4, kotlinx-coroutines-test |
| **Config** | `minSdk 26`, `targetSdk 36` |

---

## 📱 Screens

| Screen | What it shows |
|---|---|
| **Schedule** | Group, week day strip, class cards, empty day, load errors |
| **Calendar** | Month grid, available and unavailable dates, day selection |
| **About me** | Avatar, full name, group, email, city, country, time zone |

---

## 🛠️ Troubleshooting

| Problem | What to try |
|---|---|
| **`Inconsistent JVM-target compatibility`** | Check `JAVA_HOME` — JDK 17+ is required, and `jvmTarget = 17` must be set in `app/build.gradle.kts` |
| **`SDK location not found`** | Create `local.properties` with `sdk.dir=<Android SDK path>` |
| **No emulator found** | `adb devices`; if empty, start an AVD from Android Studio |
| **Dark theme not switching** | `adb shell cmd uimode night yes` / `night no` |
| **Schedule is empty** | Check `app/src/main/res/raw/schedule_k3i1_9.json` — test data lives there |

---

## 🙏 Team

| Area | Lead |
|---|---|
| Android development | Daniil Volkov |

---

## 📄 License

Private project — Southern University (IUBiP) © 2026

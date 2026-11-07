package ru.iubip.student.ui.screens.profile

/**
 * Данные профиля студента для экрана «О себе».
 *
 * ВНИМАНИЕ: это ТЕСТОВЫЕ (игрушечные) данные, составлены по скриншоту личного кабинета.
 * Источником в будущем станет личный кабинет Moodle: модель переедет в слой данных,
 * а этот файл удалится вместе с [SampleProfile].
 */
data class Profile(
    /** ФИО полностью, может быть длинным — экран переносит его на несколько строк. */
    val fullName: String,
    /** Учебная группа. */
    val group: String,
    /** Адрес электронной почты. */
    val email: String,
    /** Страна. */
    val country: String,
    /** Город. */
    val city: String,
    /** Часовой пояс в формате IANA, например Europe/Moscow. */
    val timeZone: String,
)

/** Тестовый профиль. Данные выдуманные. */
val SampleProfile: Profile = Profile(
    fullName = "Волков Даниил Сергеевич",
    group = "К3И1(9)",
    email = "12k1i192024@mail.ru",
    country = "Россия",
    city = "Ростов-на-Дону",
    timeZone = "Europe/Moscow",
)

/**
 * Тестовый профиль с заведомо длинным ФИО — для превью проверки переноса на две строки.
 * На экране не используется.
 */
internal val SampleProfileLongName: Profile = SampleProfile.copy(
    fullName = "Волков Даниил Сергеевич-Александрович Константинопольский",
)

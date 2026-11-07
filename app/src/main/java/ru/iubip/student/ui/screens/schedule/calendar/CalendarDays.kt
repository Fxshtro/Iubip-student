package ru.iubip.student.ui.screens.schedule.calendar

import androidx.annotation.StringRes
import ru.iubip.student.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle

/** Месяц в родительном падеже: «2 ноября». */
internal fun monthGenitive(month: Int): String = when (Month.of(month)) {
    Month.JANUARY -> "января"
    Month.FEBRUARY -> "февраля"
    Month.MARCH -> "марта"
    Month.APRIL -> "апреля"
    Month.MAY -> "мая"
    Month.JUNE -> "июня"
    Month.JULY -> "июля"
    Month.AUGUST -> "августа"
    Month.SEPTEMBER -> "сентября"
    Month.OCTOBER -> "октября"
    Month.NOVEMBER -> "ноября"
    Month.DECEMBER -> "декабря"
}

/** Подписи дней недели: короткие (Пн), полные (Понедельник) и описание даты для доступности. */
internal object CalendarDays {

    /** Дни недели от понедельника до воскресенья — порядок колонок сетки. */
    val ORDERED: List<DayOfWeek> = DayOfWeek.entries.sortedBy { it.value }

    /** «Пн». */
    @StringRes
    fun DayOfWeek.shortRes(): Int = when (this) {
        DayOfWeek.MONDAY -> R.string.weekday_short_mon
        DayOfWeek.TUESDAY -> R.string.weekday_short_tue
        DayOfWeek.WEDNESDAY -> R.string.weekday_short_wed
        DayOfWeek.THURSDAY -> R.string.weekday_short_thu
        DayOfWeek.FRIDAY -> R.string.weekday_short_fri
        DayOfWeek.SATURDAY -> R.string.weekday_short_sat
        else -> R.string.weekday_short_sun
    }

    /** «Понедельник». */
    @StringRes
    fun DayOfWeek.fullRes(): Int = when (this) {
        DayOfWeek.MONDAY -> R.string.weekday_full_mon
        DayOfWeek.TUESDAY -> R.string.weekday_full_tue
        DayOfWeek.WEDNESDAY -> R.string.weekday_full_wed
        DayOfWeek.THURSDAY -> R.string.weekday_full_thu
        DayOfWeek.FRIDAY -> R.string.weekday_full_fri
        DayOfWeek.SATURDAY -> R.string.weekday_full_sat
        else -> R.string.weekday_full_sun
    }

    /**
     * «Понедельник, 2 ноября» — описание дня для экранного диктора.
     *
     * @param label подпись дня в своём месяце (из [CalendarPage.inMonth]) — на странице
     *   соседнего месяца бывает то же число месяца, и без подписи экранный диктор
     *   прочитал бы два одинаковых дня
     */
    fun description(date: LocalDate, label: String?): String {
        val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, RUSSIAN).lowercase(RUSSIAN)
        val day = label ?: "${date.dayOfMonth} ${monthGenitive(date.monthValue)}"
        return "$weekday, $day"
    }
}

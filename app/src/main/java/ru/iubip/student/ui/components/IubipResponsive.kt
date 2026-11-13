package ru.iubip.student.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Пороги адаптивности приложения. Решение принимается по **ширине окна** в dp
 * (`WindowSizeClass`), а не по ориентации: ориентация — следствие, а ширина описывает
 * и телефон в горизонтали, и планшет, и будущее разделённое окно.
 */
object IubipBreakpoints {
    /**
     * Граница «телефон / широкий экран», совпадает с Material `WindowWidthSizeClass.Compact`
     * (меньше 600 dp). Меньше — панель снизу; [COMPACT_MAX] dp и больше — панель справа.
     */
    val COMPACT_MAX = 600.dp

    /**
     * Граница «широкий планшет». От неё на «Расписании» календарь становится постоянной
     * колонкой справа, а не модальным листом.
     *
     * 840 dp, а не 900: типичный 10" планшет в вертикали — это ровно 800 dp по ширине,
     * и при пороге 900 он оставался без календаря-колонки (проверено на 1920x1200 @ 240 dpi).
     * Нижняя граница выбрана по раскладке: список пар ≥ 480 dp + колонка календаря ~300 dp
     * + отступы экрана 40 dp ≈ 820 dp. Телефон в горизонтали (~830–850 dp на большинстве
     * устройств) тоже попадает в этот режим — и это нормально: календарь там помещается.
     */
    val CALENDAR_COLUMN_MIN = 840.dp

    /**
     * Максимальная ширина контента экрана. Строки и карточки не растягиваются на всю
     * ширину планшета: длинная строка читается плохо. Контент центрируется по ширине,
     * пока окно шире этого значения.
     *
     * Если на «Расписании» показан календарь-колонкой, лимит включает и её: контент
     * (пары + календарь) в сумме не должен расползаться на весь планшет, иначе список
     * пар уезжает от календаря на пол-экрана. Поэтому для этого режима лимит шире —
     * см. `CONTENT_MAX_WIDTH_WITH_COLUMN`.
     */
    val CONTENT_MAX_WIDTH = 840.dp

    /**
     * Лимит ширины, когда на экране две зоны (список пар + колонка календаря ~300 dp):
     * 480 dp пары + 12 dp зазор + 300 dp календарь + 40 dp отступы ≈ 832 dp, с запасом — 1100 dp.
     * Дальше контент снова центрируется.
     */
    val CONTENT_MAX_WIDTH_WITH_COLUMN = 1100.dp
}

/**
 * Производные от ширины окна признаки, нужные раскладке.
 *
 * @param windowWidthDp ширина окна в dp
 */
data class IubipWindowInfo(
    val windowWidthDp: Dp,
) {
    /** Широкое окно: панель навигации справа вместо нижней. */
    val isWide: Boolean get() = windowWidthDp >= IubipBreakpoints.COMPACT_MAX

    /** Очень широкое окно: календарь постоянной колонкой рядом со списком пар. */
    val isExtraWide: Boolean get() = windowWidthDp >= IubipBreakpoints.CALENDAR_COLUMN_MIN

    companion object {
        /**
         * Признаки раскладки по НАСТОЯЩЕЙ ширине окна в dp — основной путь.
         * На телефоне в горизонтали это ≈ 838 dp: панель справа есть, календарь-колонки нет.
         */
        fun of(windowWidthDp: Dp): IubipWindowInfo = IubipWindowInfo(windowWidthDp)
    }
}

/**
 * Класс ширины окна → признаки раскладки.
 *
 * Материаловский `widthSizeClass` знает только три ступени (Compact / Medium / Expanded)
 * и НЕ сообщает точную ширину: `Expanded` начинается с 840 dp, а телефон в горизонтали
 * (≈ 850–900 dp) тоже попадает в `Expanded`. Поэтому по одному классу нельзя решить
 * про календарь-колонку — нужна настоящая ширина окна.
 *
 * Контракт: панель навигации (`isWide`) решается по классу, календарь-колонка
 * (`isExtraWide`) — только по реальной ширине из `IubipWindowInfo.of(...)`.
 * Здесь `isExtraWide` намеренно false: эта перегрузка используется лишь как
 * безопасный запасной вариант (превью, тесты), а экран получает реальную ширину.
 */
fun WindowWidthSizeClass.toIubipWindowInfo(): IubipWindowInfo = IubipWindowInfo(
    windowWidthDp = when (this) {
        WindowWidthSizeClass.Compact -> IubipBreakpoints.COMPACT_MAX - 1.dp
        WindowWidthSizeClass.Medium -> IubipBreakpoints.COMPACT_MAX
        else -> IubipBreakpoints.CONTENT_MAX_WIDTH
    },
)

/**
 * Реальная ширина окна в dp — источник для порогов раскладки.
 *
 * `widthSizeClass` даёт только три ступени; для решения «календарь колонкой или листом»
 * нужна точная ширина. Берём `LocalConfiguration.screenWidthDp` — он обновляется при
 * повороте и при смене размера окна (разделённый экран) и работает на всех API проекта.
 */
@Composable
fun currentWindowWidthDp(): Dp {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    return with(density) { configuration.screenWidthDp.dp }
}

/**
 * Ограничивает контент [IubipBreakpoints.CONTENT_MAX_WIDTH] и центрирует его по ширине.
 *
 * На телефоне в портрете ширина меньше лимита — контент занимает экран как раньше
 * (отступы 20 dp остаются внутри экрана). На планшете строки не растягиваются на пол-экрана.
 */
@Composable
fun ContentColumn(
    modifier: Modifier = Modifier,
    maxWidth: Dp = IubipBreakpoints.CONTENT_MAX_WIDTH,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // `widthIn(max = …)` и так берёт меньшую из двух ширин (констрейнт окна
                // или лимит), поэтому на телефоне контент остаётся на всю ширину.
                .widthIn(max = maxWidth),
            content = { content() },
        )
    }
}

package ru.iubip.student.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.iubip.student.navigation.BottomDestination
import ru.iubip.student.ui.theme.IubipTheme

/** Ширина боковой панели (вертикальный рейл с подписями под иконками). */
internal val SIDE_BAR_WIDTH = 64.dp

/** Высота нижней панели — ровно как было до адаптивности. */
internal val BOTTOM_BAR_HEIGHT = 64.dp

/**
 * Собственный воздух под панелью — ПОВЕРХ системной полосы навигации.
 *
 * 16 dp, а не 8: на жестовой навигации полоса (инсет `navigationBars` = 63 px ≈ 24 dp)
 * накрывает верхнюю половину своего капсульного «ползунка» и почти всё пространство
 * вокруг него, поэтому на экране от «таблетки» до видимой капсулы оставалось ~18 dp,
 * и воздух не читался (см. AGENTS.md, раздел «Адаптивность»). С 16 dp таблетка
 * гарантированно отодвинута от полосы на 16 dp (42 px при 420 dpi), и это тот же
 * воздух, что и сверху панели по правилам отступов (16 dp).
 */
internal val BOTTOM_BAR_BOTTOM_GAP = 16.dp

/**
 * Воздух между линией-разделителем и пунктами нижней панели.
 *
 * Без него «таблетка» активного раздела упирается в линию сверху. Зазор добавляется
 * СНАРУЖИ блока пунктов: высота самих пунктов остаётся 64 dp.
 * Относится только к нижней панели (портрет/узкое окно); боковая панель не использует.
 */
internal val BOTTOM_BAR_TOP_GAP = 8.dp

/**
 * Нижняя панель навигации в стиле приложения.
 *
 * Без цветной заливки: фон — фон приложения, сверху тонкая линия-разделитель.
 * Пункты расположены в ряд; каждый рисует [IubipNavigationItem].
 *
 * @param currentRoute маршрут активного раздела
 * @param onNavigate обработчик перехода
 */
@Composable
fun IubipBottomBar(
    currentRoute: String?,
    onNavigate: (BottomDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            // Отступ от системной полосы: инсеты навигации + собственный воздух.
            // Только `navigationBars` недостаточно — на жестовой навигации полоса тонкая,
            // и подпись пункта «прилипает» к краю экрана.
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = BOTTOM_BAR_BOTTOM_GAP),
    ) {
        HorizontalDivider(thickness = 1.dp, color = IubipTheme.colors.divider)
        // Воздух между линией-разделителем и кнопками: без него «таблетка» активного
        // раздела упирается в линию. Высота пунктов остаётся 64 dp, зазор добавляется СНАРУЖИ.
        Spacer(Modifier.height(BOTTOM_BAR_TOP_GAP))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BOTTOM_BAR_HEIGHT)
                .selectableGroup()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomDestination.entries.forEach { destination ->
                IubipNavigationItem(
                    destination = destination,
                    selected = destination.route == currentRoute,
                    onClick = { onNavigate(destination) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Боковая панель навигации для широких окон (телефон в горизонтали, планшет).
 *
 * Та же структура, что и у [IubipBottomBar], но вертикально: разделитель слева,
 * пункты в столбце сверху вниз, ширина [SIDE_BAR_WIDTH]. Пункты — тот же
 * [IubipNavigationItem], вёрстка пункта нигде не дублируется.
 *
 * Панель прижата к правому краю экрана. Безопасные отступы (вырез под камеру,
 * системные панели) добавляет вызывающий экран через `safeDrawing`: панель — часть
 * каркаса и не должна «подсовывать» свои инсеты внутрь.
 *
 * @param currentRoute маршрут активного раздела
 * @param onNavigate обработчик перехода
 */
@Composable
fun IubipSideBar(
    currentRoute: String?,
    onNavigate: (BottomDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .width(SIDE_BAR_WIDTH)
            // Панель занимает доступную ей высоту САМА (её не растягивает Row с инсетами
            // всей safeDrawing — иначе она упирается в системную панель навигации внизу
            // и выглядит обрезанной).
            .background(MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VerticalDivider(thickness = 1.dp, color = IubipTheme.colors.divider)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .selectableGroup()
                // Плотнее по вертикали: у панели 72 dp внутри должен дышать только «воздух»,
                // а не поля самого контейнера.
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BottomDestination.entries.forEach { destination ->
                    IubipNavigationItem(
                        destination = destination,
                        selected = destination.route == currentRoute,
                        onClick = { onNavigate(destination) },
                        compact = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
